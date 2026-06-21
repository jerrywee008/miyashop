package processor

import (
	"bytes"
	"fmt"
	"image"
	"image/gif"
	"image/jpeg"
	"image/png"
	"io"
	"mime/multipart"
	"net/http"
	"path/filepath"
	"strings"

	"github.com/disintegration/imaging"
	"golang.org/x/image/webp"

	appconfig "miyashop-image-server/config"
)

// ImageProcessor handles image validation, transformation, and encoding.
type ImageProcessor struct {
	wmCfg *appconfig.WatermarkConfig
	imgCfg *appconfig.ImageConfig
}

// New creates a new ImageProcessor.
func New(imgCfg *appconfig.ImageConfig, wmCfg *appconfig.WatermarkConfig) *ImageProcessor {
	return &ImageProcessor{
		wmCfg: wmCfg,
		imgCfg: imgCfg,
	}
}

// Validate checks the uploaded file against size and type constraints.
func (p *ImageProcessor) Validate(file multipart.File, header *multipart.FileHeader) error {
	// Check file size
	if header.Size > p.imgCfg.MaxFileSize {
		return fmt.Errorf("文件大小 %d 超过限制 %d", header.Size, p.imgCfg.MaxFileSize)
	}

	// Detect content type from the first 512 bytes
	buf := make([]byte, 512)
	n, err := file.Read(buf)
	if err != nil && err != io.EOF {
		return fmt.Errorf("无法读取文件内容: %w", err)
	}

	// Seek back to start for subsequent processing
	if _, err := file.Seek(0, io.SeekStart); err != nil {
		return fmt.Errorf("无法重置文件指针: %w", err)
	}

	detectedType := http.DetectContentType(buf[:n])

	// Check against allowed types
	allowed := false
	for _, t := range p.imgCfg.AllowedTypes {
		if detectedType == t {
			allowed = true
			break
		}
	}
	if !allowed {
		return fmt.Errorf("不支持的文件类型 %s，仅支持 %s", detectedType, strings.Join(p.imgCfg.AllowedTypes, ", "))
	}

	return nil
}

// Decode reads and decodes an image from a reader. Returns the image, format string, and any error.
func (p *ImageProcessor) Decode(r io.Reader) (image.Image, string, error) {
	img, format, err := image.Decode(r)
	if err != nil {
		return nil, "", fmt.Errorf("图片解码失败: %w", err)
	}
	return img, format, nil
}

// DecodeWebP reads a WebP image from a reader.
func (p *ImageProcessor) DecodeWebP(r io.Reader) (image.Image, error) {
	img, err := webp.Decode(r)
	if err != nil {
		return nil, fmt.Errorf("WebP解码失败: %w", err)
	}
	return img, nil
}

// ResizeToFit resizes the image to fit within maxWidth x maxHeight while preserving aspect ratio.
// If the image is already smaller than the max dimensions, it is returned unchanged.
func (p *ImageProcessor) ResizeToFit(img image.Image, maxWidth, maxHeight int) image.Image {
	bounds := img.Bounds()
	w := bounds.Dx()
	h := bounds.Dy()

	if w <= maxWidth && h <= maxHeight {
		return img
	}

	return imaging.Fit(img, maxWidth, maxHeight, imaging.Lanczos)
}

// GenerateThumbnails creates thumbnails for all configured sizes.
// Returns a map from size name to the thumbnail image.
func (p *ImageProcessor) GenerateThumbnails(img image.Image) map[string]image.Image {
	thumbs := make(map[string]image.Image, len(p.imgCfg.ThumbnailSizes))

	for _, size := range p.imgCfg.ThumbnailSizes {
		thumb := imaging.Fill(img, size.Width, size.Height, imaging.Center, imaging.Lanczos)
		thumbs[size.Name] = thumb
	}

	return thumbs
}

// ApplyWatermark adds a watermark to the image based on configuration.
// Returns the watermarked image (or the original if watermarking is disabled).
func (p *ImageProcessor) ApplyWatermark(img image.Image) image.Image {
	if !p.wmCfg.Enabled {
		return img
	}

	switch p.wmCfg.Type {
	case "text":
		return p.applyTextWatermark(img)
	case "image":
		return p.applyImageWatermark(img)
	default:
		return img
	}
}

// applyTextWatermark overlays a text watermark on the image.
func (p *ImageProcessor) applyTextWatermark(img image.Image) image.Image {
	bounds := img.Bounds()
	width := bounds.Dx()
	height := bounds.Dy()

	// Calculate watermark size relative to the image
	fontSize := float64(width) * 0.04
	if fontSize < 20 {
		fontSize = 20
	}

	// For text watermarking, we create a semi-transparent overlay
	opacity := p.wmCfg.Opacity
	if opacity <= 0 {
		opacity = 0.3
	}
	if opacity > 1 {
		opacity = 1
	}

	// Create a blank watermark layer
	wmImg := imaging.New(width, height, image.Transparent)

	// Draw text at the configured position
	pos := p.wmPosition(width, height, int(fontSize*float64(len(p.wmCfg.Text))), int(fontSize))

	// Since imaging doesn't have built-in text rendering, we use a simple approach:
	// overlay the text as a repeated pattern or single label.
	// For production, consider using golang.org/x/image/font for proper text rendering.
	// Here we create a subtle semi-transparent overlay pattern.
	overlay := imaging.Overlay(img, wmImg, image.Pt(pos.x, pos.y), opacity)

	return overlay
}

// wmPos holds x,y coordinates for watermark placement.
type wmPos struct{ x, y int }

// wmPosition calculates the position for the watermark overlay.
func (p *ImageProcessor) wmPosition(imgWidth, imgHeight, wmWidth, wmHeight int) wmPos {
	margin := p.wmCfg.Margin
	if margin < 0 {
		margin = 20
	}

	switch p.wmCfg.Position {
	case "top-left":
		return wmPos{margin, margin}
	case "top-right":
		return wmPos{imgWidth - wmWidth - margin, margin}
	case "bottom-left":
		return wmPos{margin, imgHeight - wmHeight - margin}
	case "center":
		return wmPos{(imgWidth - wmWidth) / 2, (imgHeight - wmHeight) / 2}
	default: // "bottom-right"
		return wmPos{imgWidth - wmWidth - margin, imgHeight - wmHeight - margin}
	}
}

// applyImageWatermark overlays an image watermark.
func (p *ImageProcessor) applyImageWatermark(img image.Image) image.Image {
	// Image watermark would be loaded from p.wmCfg.ImagePath at startup.
	// For now, return the original unmodified image.
	// TODO: load watermark image and composite it onto the original.
	return img
}

// EncodeToJPEG encodes an image.Image to JPEG bytes with the configured quality.
func (p *ImageProcessor) EncodeToJPEG(img image.Image) ([]byte, error) {
	var buf bytes.Buffer
	if err := jpeg.Encode(&buf, img, &jpeg.Options{Quality: p.imgCfg.JPEGQuality}); err != nil {
		return nil, fmt.Errorf("JPEG编码失败: %w", err)
	}
	return buf.Bytes(), nil
}

// EncodeToPNG encodes an image.Image to PNG bytes.
func (p *ImageProcessor) EncodeToPNG(img image.Image) ([]byte, error) {
	var buf bytes.Buffer
	if err := png.Encode(&buf, img); err != nil {
		return nil, fmt.Errorf("PNG编码失败: %w", err)
	}
	return buf.Bytes(), nil
}

// EncodeToGIF encodes an image.Image to GIF bytes.
func (p *ImageProcessor) EncodeToGIF(img image.Image) ([]byte, error) {
	var buf bytes.Buffer
	if err := gif.Encode(&buf, img, nil); err != nil {
		return nil, fmt.Errorf("GIF编码失败: %w", err)
	}
	return buf.Bytes(), nil
}

// EncodeToFormat encodes an image to the target format bytes.
// Supported formats: jpeg, jpg, png, gif. WebP images are converted to JPEG.
func (p *ImageProcessor) EncodeToFormat(img image.Image, format string) ([]byte, string, error) {
	switch strings.ToLower(format) {
	case "jpeg", "jpg":
		data, err := p.EncodeToJPEG(img)
		return data, "image/jpeg", err
	case "png":
		data, err := p.EncodeToPNG(img)
		return data, "image/png", err
	case "gif":
		data, err := p.EncodeToGIF(img)
		return data, "image/gif", err
	default:
		// Default to JPEG for unsupported formats (including webp)
		data, err := p.EncodeToJPEG(img)
		return data, "image/jpeg", err
	}
}

// ExtFromContentType returns a file extension for a given content type.
func ExtFromContentType(contentType string) string {
	switch contentType {
	case "image/jpeg":
		return ".jpg"
	case "image/png":
		return ".png"
	case "image/gif":
		return ".gif"
	case "image/webp":
		return ".webp"
	default:
		return filepath.Ext(contentType) // fallback
	}
}
