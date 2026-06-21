package handler

import (
	"context"
	"io"
	"net/http"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"

	"miyashop-image-server/internal/model"
	"miyashop-image-server/internal/service"
)

// ImageHandler handles HTTP requests for image operations.
type ImageHandler struct {
	svc    *service.ImageService
	logger *zap.Logger
}

// NewImageHandler creates a new ImageHandler.
func NewImageHandler(svc *service.ImageService, logger *zap.Logger) *ImageHandler {
	return &ImageHandler{
		svc:    svc,
		logger: logger,
	}
}

// Upload handles POST /api/images/upload
// Accepts multipart form data with "file" or "files" field.
func (h *ImageHandler) Upload(c *gin.Context) {
	// Parse multipart form (limit to max file size * number of concurrent files)
	if err := c.Request.ParseMultipartForm(50 << 20); err != nil { // 50MB max
		c.JSON(http.StatusBadRequest, model.Error(model.CodeFileTooLarge, "请求体过大"))
		return
	}

	form := c.Request.MultipartForm
	if form == nil {
		c.JSON(http.StatusBadRequest, model.Error(model.CodeBadRequest, "未找到上传文件"))
		return
	}

	results, err := h.svc.UploadMultiple(c.Request.Context(), form.File)
	if err != nil {
		h.logger.Error("批量上传失败", zap.Error(err))
		c.JSON(http.StatusInternalServerError, model.Error(model.CodeUploadFailed, "上传失败: "+err.Error()))
		return
	}

	if len(results) == 0 {
		c.JSON(http.StatusBadRequest, model.Error(model.CodeBadRequest, "未找到有效的上传文件"))
		return
	}

	// If only one file was uploaded, return single result; otherwise return array
	if len(results) == 1 {
		c.JSON(http.StatusOK, model.Success(results[0]))
	} else {
		c.JSON(http.StatusOK, model.Success(results))
	}
}

// Download handles GET /api/images/:prefix/*key
// Serves the image directly or redirects to a pre-signed URL.
func (h *ImageHandler) Download(c *gin.Context) {
	prefix := c.Param("prefix")
	key := c.Param("key")
	if prefix == "" || key == "" {
		c.JSON(http.StatusBadRequest, model.Error(model.CodeBadRequest, "缺少图片key"))
		return
	}
	// Reconstruct full key: "prefix/rest_of_key"
	key = prefix + "/" + strings.TrimPrefix(key, "/")

	// Check cache headers
	if h.handleCache(c, key) {
		return
	}

	body, contentType, err := h.svc.Download(c.Request.Context(), key)
	if err != nil {
		if resultErr, ok := err.(model.Result); ok {
			c.JSON(http.StatusNotFound, resultErr)
			return
		}
		h.logger.Error("下载图片失败", zap.String("key", key), zap.Error(err))
		c.JSON(http.StatusNotFound, model.Error(model.CodeImageNotFound, "图片不存在"))
		return
	}
	defer body.Close()

	// Set caching headers
	c.Header("Cache-Control", "public, max-age=31536000, immutable")
	c.Header("Content-Type", contentType)

	// Stream the response
	c.Status(http.StatusOK)
	io.Copy(c.Writer, body)
}

// GetThumbnail handles GET /api/images/thumb/:size/*key
func (h *ImageHandler) GetThumbnail(c *gin.Context) {
	size := c.Param("size")
	key := c.Param("key")
	key = strings.TrimPrefix(key, "/")

	if size == "" || key == "" {
		c.JSON(http.StatusBadRequest, model.Error(model.CodeBadRequest, "缺少参数"))
		return
	}

	// Validate size
	validSizes := map[string]bool{"small": true, "medium": true, "large": true}
	if !validSizes[size] {
		c.JSON(http.StatusBadRequest, model.Error(model.CodeBadRequest, "无效的缩略图尺寸，支持: small, medium, large"))
		return
	}

	// Parse key to extract object ID and extension
	// Key format: "original/{uuid}.{ext}" -> we need "{uuid}.{ext}"
	objIDWithExt := extractObjID(key)
	if objIDWithExt == "" {
		c.JSON(http.StatusBadRequest, model.Error(model.CodeBadRequest, "无效的图片key格式"))
		return
	}

	// Extract extension
	dotIdx := strings.LastIndex(objIDWithExt, ".")
	if dotIdx < 0 {
		c.JSON(http.StatusBadRequest, model.Error(model.CodeBadRequest, "无效的图片key格式"))
		return
	}
	objID := objIDWithExt[:dotIdx]
	ext := objIDWithExt[dotIdx:]

	// Retry with different extension if needed
	body, contentType, err := h.svc.GetThumbnail(c.Request.Context(), size, objID, ext)
	if err != nil {
		// Try .jpg extension if original format failed
		if ext != ".jpg" {
			body, contentType, err = h.svc.GetThumbnail(c.Request.Context(), size, objID, ".jpg")
		}
		if err != nil {
			c.JSON(http.StatusNotFound, model.Error(model.CodeImageNotFound, "缩略图不存在"))
			return
		}
	}
	defer body.Close()

	c.Header("Cache-Control", "public, max-age=31536000, immutable")
	c.Header("Content-Type", contentType)
	c.Status(http.StatusOK)
	io.Copy(c.Writer, body)
}

// Delete handles DELETE /api/images/*key
// Requires admin authentication.
func (h *ImageHandler) Delete(c *gin.Context) {
	key := c.Param("key")
	key = strings.TrimPrefix(key, "/")

	if key == "" {
		c.JSON(http.StatusBadRequest, model.Error(model.CodeBadRequest, "缺少图片key"))
		return
	}

	if err := h.svc.Delete(c.Request.Context(), key); err != nil {
		h.logger.Error("删除图片失败", zap.String("key", key), zap.Error(err))
		c.JSON(http.StatusInternalServerError, model.Error(model.CodeError, "删除失败"))
		return
	}

	c.JSON(http.StatusOK, model.SuccessMsg("删除成功", nil))
}

// Health handles GET /health
func (h *ImageHandler) Health(c *gin.Context) {
	ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
	defer cancel()

	storageStatus := "connected"
	if err := h.svc.Ping(ctx); err != nil {
		storageStatus = "disconnected: " + err.Error()
	}

	c.JSON(http.StatusOK, model.Success(model.HealthResponse{
		Status:  "ok",
		Storage: storageStatus,
	}))
}

// PreSignRedirect handles the pre-signed URL redirect pattern for S3 direct access.
func (h *ImageHandler) PreSignRedirect(c *gin.Context) {
	key := c.Param("key")
	key = strings.TrimPrefix(key, "/")

	url, err := h.svc.GeneratePresignedURL(c.Request.Context(), key, 1*time.Hour)
	if err != nil {
		h.logger.Error("生成预签名URL失败", zap.String("key", key), zap.Error(err))
		c.JSON(http.StatusInternalServerError, model.Error(model.CodeError, "生成访问链接失败"))
		return
	}

	c.Redirect(http.StatusFound, url)
}

// handleCache checks for cache-related headers and returns 304 if appropriate.
func (h *ImageHandler) handleCache(c *gin.Context, key string) bool {
	etag := c.GetHeader("If-None-Match")
	if etag != "" && etag == `"`+key+`"` {
		c.Status(http.StatusNotModified)
		return true
	}

	c.Header("ETag", `"`+key+`"`)
	return false
}

// extractObjID extracts the object ID (filename) from a full key path.
// "original/uuid.jpg" -> "uuid.jpg"
func extractObjID(key string) string {
	parts := splitBySlash(key)
	if len(parts) >= 2 {
		return parts[len(parts)-1]
	}
	return key
}

func splitBySlash(s string) []string {
	var parts []string
	start := 0
	for i, c := range s {
		if c == '/' {
			if i > start {
				parts = append(parts, s[start:i])
			}
			start = i + 1
		}
	}
	if start < len(s) {
		parts = append(parts, s[start:])
	}
	return parts
}
