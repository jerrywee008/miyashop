package model

import "time"

// Result is the unified API response, matching the Java backend's Result<T> format.
type Result struct {
	Code      int         `json:"code"`
	Message   string      `json:"message"`
	Data      interface{} `json:"data"`
	Timestamp int64       `json:"timestamp"`
}

// Predefined result codes, matching the Java backend's ResultCode.
const (
	CodeSuccess       = 200
	CodeError         = 500
	CodeBadRequest    = 400
	CodeUnauthorized  = 401
	CodeForbidden     = 403
	CodeNotFound      = 404
	CodeTokenExpired  = 1004
	CodeTokenInvalid  = 1005
	CodeFileTooLarge  = 7001
	CodeInvalidType   = 7002
	CodeUploadFailed  = 7003
	CodeImageNotFound = 7004
)

// Success returns a successful response with data.
func Success(data interface{}) Result {
	return Result{
		Code:      CodeSuccess,
		Message:   "操作成功",
		Data:      data,
		Timestamp: time.Now().UnixMilli(),
	}
}

// SuccessMsg returns a successful response with a custom message.
func SuccessMsg(message string, data interface{}) Result {
	return Result{
		Code:      CodeSuccess,
		Message:   message,
		Data:      data,
		Timestamp: time.Now().UnixMilli(),
	}
}

// Error returns an error response with a specific code and message.
func Error(code int, message string) Result {
	return Result{
		Code:      code,
		Message:   message,
		Data:      nil,
		Timestamp: time.Now().UnixMilli(),
	}
}

// ErrorMsg returns a generic error response (code 500).
func ErrorMsg(message string) Result {
	return Error(CodeError, message)
}

// --- Upload / Download specific types ---

// UploadResponse is returned for single-file uploads.
type UploadResponse struct {
	URL        string            `json:"url"`
	Key        string            `json:"key"`
	Thumbnails map[string]string `json:"thumbnails"` // size name -> URL
	Size       int64             `json:"size"`       // file size in bytes
}

// HealthResponse is returned by the health check endpoint.
type HealthResponse struct {
	Status  string `json:"status"`
	Storage string `json:"storage"`
}
