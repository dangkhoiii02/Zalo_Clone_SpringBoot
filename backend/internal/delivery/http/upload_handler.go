package http

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

// UploadHandler handles file upload requests
type UploadHandler struct {
	dataDir string
}

func NewUploadHandler(dataDir string) *UploadHandler {
	return &UploadHandler{dataDir: dataDir}
}

// allowedImageExts defines permitted image file extensions
var allowedImageExts = map[string]bool{
	".jpg": true, ".jpeg": true, ".png": true, ".gif": true,
	".webp": true, ".svg": true, ".bmp": true, ".ico": true,
}

// maxUploadSize is 10MB
const maxUploadSize = 10 * 1024 * 1024

// @Summary Upload a file
// @Description Upload an image or file attachment for chat messages
// @Tags Upload
// @Accept multipart/form-data
// @Produce json
// @Security BearerAuth
// @Param file formance file true "File to upload"
// @Success 200 {object} map[string]interface{}
// @Failure 400 {object} map[string]string
// @Failure 413 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /upload [post]
func (h *UploadHandler) Upload(c *gin.Context) {
	// Limit request body size
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, maxUploadSize)

	file, header, err := c.Request.FormFile("file")
	if err != nil {
		if err.Error() == "http: request body too large" {
			c.JSON(http.StatusRequestEntityTooLarge, gin.H{"error": "File quá lớn (tối đa 10MB)"})
			return
		}
		c.JSON(http.StatusBadRequest, gin.H{"error": "Không tìm thấy file trong request"})
		return
	}
	defer file.Close()

	// Get original filename and extension
	originalName := header.Filename
	ext := strings.ToLower(filepath.Ext(originalName))
	if ext == "" {
		ext = ".bin"
	}

	// Determine subdirectory based on file type
	subDir := "files"
	if allowedImageExts[ext] {
		subDir = "images"
	}

	// Generate unique filename
	newFileName := uuid.New().String() + ext
	targetDir := filepath.Join(h.dataDir, subDir)
	targetPath := filepath.Join(targetDir, newFileName)

	// Ensure directory exists
	if err := os.MkdirAll(targetDir, 0755); err != nil {
		log.Printf("❌ Failed to create upload dir: %v", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Không thể tạo thư mục lưu trữ"})
		return
	}

	// Create destination file
	dst, err := os.Create(targetPath)
	if err != nil {
		log.Printf("❌ Failed to create file: %v", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Không thể lưu file"})
		return
	}
	defer dst.Close()

	// Copy file content
	written, err := io.Copy(dst, file)
	if err != nil {
		log.Printf("❌ Failed to write file: %v", err)
		os.Remove(targetPath) // Clean up on error
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Lỗi khi ghi file"})
		return
	}

	// Build URL path (served as static files)
	url := fmt.Sprintf("/data/%s/%s", subDir, newFileName)

	log.Printf("📎 File uploaded: %s → %s (%d bytes)", originalName, url, written)

	c.JSON(http.StatusOK, gin.H{
		"url":       url,
		"filename":  originalName,
		"size":      written,
		"type":      subDir, // "images" or "files"
	})
}
