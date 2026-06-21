package service

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"mime/multipart"
	"time"

	"github.com/google/uuid"
	"go.uber.org/zap"

	appconfig "miyashop-image-server/config"
	"miyashop-image-server/internal/model"
	"miyashop-image-server/internal/processor"
	"miyashop-image-server/internal/storage"
)

// ImageService orchestrates image upload, download, thumbnail, and delete operations.
type ImageService struct {
	store     storage.StorageClient
	proc      *processor.ImageProcessor
	cfg       *appconfig.Config
	logger    *zap.Logger
	publicURL string // base URL for constructing public image URLs
}

// NewImageService creates a new ImageService.
func NewImageService(
	store storage.StorageClient,
	proc *processor.ImageProcessor,
	cfg *appconfig.Config,
	logger *zap.Logger,
	publicURL string,
) *ImageService {
	return &ImageService{
		store:     store,
		proc:      proc,
		cfg:       cfg,
		logger:    logger,
		publicURL: publicURL,
	}
}

// UploadResult holds the result of a single image upload.
type UploadResult struct {
	URL        string            `json:"url"`
	Key        string            `json:"key"`
	Thumbnails map[string]string `json:"thumbnails"`
	Size       int64             `json:"size"`
	Width      int               `json:"width"`
	Height     int               `json:"height"`
}

// UploadSingle handles single file upload through the complete pipeline.
func (s *ImageService) UploadSingle(ctx context.Context, file multipart.File, header *multipart.FileHeader) (*UploadResult, error) {
	// 1. Validate
	if err := s.proc.Validate(file, header); err != nil {
		return nil, fmt.Errorf("文件验证失败: %w", err)
	}

	// Re-seek to start after Validate
	if _, err := file.Seek(0, io.SeekStart); err != nil {
		return nil, fmt.Errorf("无法重置文件指针: %w", err)
	}

	// 2. Read all bytes (we need to process multiple times)
	fileBytes, err := io.ReadAll(file)
	if err != nil {
		return nil, fmt.Errorf("读取文件失败: %w", err)
	}

	// 3. Detect content type
	contentType := header.Header.Get("Content-Type")

	// 4. Decode image
	img, format, err := s.proc.Decode(bytes.NewReader(fileBytes))
	if err != nil {
		// Try WebP decode if standard decode fails
		img, webpErr := s.proc.DecodeWebP(bytes.NewReader(fileBytes))
		if webpErr != nil {
			return nil, fmt.Errorf("图片解码失败: %w", err)
		}
		format = "webp"
		contentType = "image/webp"
		img = img // use successfully decoded webp image
	}
	_ = format // may be used for extension

	bounds := img.Bounds()
	origWidth := bounds.Dx()
	origHeight := bounds.Dy()

	// 5. Resize original to fit within max dimensions
	img = s.proc.ResizeToFit(img, s.cfg.Image.MaxWidth, s.cfg.Image.MaxHeight)

	// 6. Apply watermark to the original (before thumbnail generation)
	watermarkedImg := s.proc.ApplyWatermark(img)

	// 7. Generate thumbnails (from watermarked or original)
	thumbnails := s.proc.GenerateThumbnails(img)

	// 8. Generate a unique object key
	ext := processor.ExtFromContentType(contentType)
	objID := uuid.New().String()
	originalKey := "original/" + objID + ext

	// 9. Upload original
	origData, origCT, err := s.proc.EncodeToFormat(watermarkedImg, format)
	if err != nil {
		return nil, fmt.Errorf("编码原始图片失败: %w", err)
	}
	if err := s.store.Upload(ctx, originalKey, bytes.NewReader(origData), origCT); err != nil {
		return nil, fmt.Errorf("上传原始图片失败: %w", err)
	}

	// 10. Upload thumbnails
	thumbURLs := make(map[string]string, len(thumbnails))
	for name, thumb := range thumbnails {
		thumbKey := "thumb/" + name + "/" + objID + ext
		thumbData, thumbCT, err := s.proc.EncodeToFormat(thumb, format)
		if err != nil {
			s.logger.Warn("跳过缩略图生成", zap.String("size", name), zap.Error(err))
			continue
		}
		if err := s.store.Upload(ctx, thumbKey, bytes.NewReader(thumbData), thumbCT); err != nil {
			s.logger.Warn("缩略图上传失败", zap.String("size", name), zap.Error(err))
			continue
		}
		thumbURLs[name] = s.buildURL(thumbKey)
	}

	// 11. Upload watermark variant if enabled
	if s.cfg.Watermark.Enabled {
		wmKey := "watermark/" + objID + ext
		if err := s.store.Upload(ctx, wmKey, bytes.NewReader(origData), origCT); err != nil {
			s.logger.Warn("水印版本上传失败", zap.Error(err))
		}
		_ = wmKey // used for URL building if needed
	}

	return &UploadResult{
		URL:        s.buildURL(originalKey),
		Key:        originalKey,
		Thumbnails: thumbURLs,
		Size:       int64(len(origData)),
		Width:      origWidth,
		Height:     origHeight,
	}, nil
}

// UploadMultiple handles batch file upload.
func (s *ImageService) UploadMultiple(ctx context.Context, files map[string][]*multipart.FileHeader) ([]*UploadResult, error) {
	var results []*UploadResult

	// Support both "file" (single) and "files" (multiple) form field names
	fileHeaders := files["files"]
	if len(fileHeaders) == 0 {
		fileHeaders = files["file"]
	}

	for _, header := range fileHeaders {
		file, err := header.Open()
		if err != nil {
			s.logger.Warn("打开上传文件失败", zap.String("filename", header.Filename), zap.Error(err))
			continue
		}

		result, err := s.UploadSingle(ctx, file, header)
		file.Close()
		if err != nil {
			s.logger.Warn("上传文件失败", zap.String("filename", header.Filename), zap.Error(err))
			continue
		}
		results = append(results, result)
	}

	return results, nil
}

// Download retrieves an image from storage and returns it as a stream.
func (s *ImageService) Download(ctx context.Context, key string) (io.ReadCloser, string, error) {
	body, contentType, err := s.store.Download(ctx, key)
	if err != nil {
		if storage.IsNotFoundError(err) {
			return nil, "", model.Error(model.CodeImageNotFound, "图片不存在")
		}
		return nil, "", fmt.Errorf("下载图片失败: %w", err)
	}
	return body, contentType, nil
}

// GetThumbnail retrieves a thumbnail for the given size and original key.
func (s *ImageService) GetThumbnail(ctx context.Context, size, objID, ext string) (io.ReadCloser, string, error) {
	thumbKey := "thumb/" + size + "/" + objID + ext
	return s.Download(ctx, thumbKey)
}

// Delete removes an image and all its variants (thumbnails, watermark) from storage.
func (s *ImageService) Delete(ctx context.Context, key string) error {
	// Delete original
	if err := s.store.Delete(ctx, key); err != nil && !storage.IsNotFoundError(err) {
		s.logger.Warn("删除原始图片失败", zap.String("key", key), zap.Error(err))
	}

	// Parse the object ID and extension from the key
	// Key format: "original/{uuid}.{ext}"
	parts := splitKey(key) // e.g., ["original", "uuid.ext"]
	if len(parts) != 2 {
		// Key doesn't match expected format; just log and continue
		s.logger.Warn("无法解析图片key格式", zap.String("key", key))
		return nil
	}
	objIDWithExt := parts[1]

	// Delete thumbnails for all configured sizes
	for _, size := range s.cfg.Image.ThumbnailSizes {
		thumbKey := "thumb/" + size.Name + "/" + objIDWithExt
		if err := s.store.Delete(ctx, thumbKey); err != nil && !storage.IsNotFoundError(err) {
			s.logger.Warn("删除缩略图失败", zap.String("key", thumbKey), zap.Error(err))
		}
	}

	// Delete watermark variant
	wmKey := "watermark/" + objIDWithExt
	if err := s.store.Delete(ctx, wmKey); err != nil && !storage.IsNotFoundError(err) {
		s.logger.Warn("删除水印版本失败", zap.String("key", wmKey), zap.Error(err))
	}

	return nil
}

// GeneratePresignedURL creates a temporary pre-signed URL for an S3 object.
func (s *ImageService) GeneratePresignedURL(ctx context.Context, key string, expiration time.Duration) (string, error) {
	return s.store.GeneratePresignedURL(ctx, key, expiration)
}

// Ping checks the connection to the underlying storage.
func (s *ImageService) Ping(ctx context.Context) error {
	return s.store.Ping(ctx)
}

// buildURL constructs the full public URL for an S3 object key.
func (s *ImageService) buildURL(key string) string {
	// If a CDN/public URL is configured, use that
	if s.cfg.S3.PublicURL != "" {
		base := s.cfg.S3.PublicURL
		if base[len(base)-1] != '/' {
			base += "/"
		}
		return base + key
	}

	// Otherwise, construct URL pointing to our own server
	base := s.publicURL
	if base[len(base)-1] != '/' {
		base += "/"
	}
	return base + "api/images/" + key
}

// splitKey splits a key like "original/uuid.jpg" into parts.
func splitKey(key string) []string {
	parts := make([]string, 0, 2)
	slashIdx := -1
	for i, c := range key {
		if c == '/' {
			slashIdx = i
			break
		}
	}
	if slashIdx < 0 {
		return []string{key}
	}
	parts = append(parts, key[:slashIdx])
	parts = append(parts, key[slashIdx+1:])
	return parts
}
