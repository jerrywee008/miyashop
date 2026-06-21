package config

import (
	"fmt"
	"os"
	"strings"

	"github.com/spf13/viper"
)

// Config is the top-level configuration struct.
type Config struct {
	Server    ServerConfig    `mapstructure:"server"`
	S3        S3Config        `mapstructure:"s3"`
	JWT       JWTConfig       `mapstructure:"jwt"`
	Image     ImageConfig     `mapstructure:"image"`
	Watermark WatermarkConfig `mapstructure:"watermark"`
	Log       LogConfig       `mapstructure:"log"`
}

// ServerConfig holds HTTP server settings.
type ServerConfig struct {
	Port int    `mapstructure:"port"`
	Mode string `mapstructure:"mode"`
}

// S3Config holds S3-compatible storage settings.
type S3Config struct {
	Provider  string `mapstructure:"provider"`
	Endpoint  string `mapstructure:"endpoint"`
	Region    string `mapstructure:"region"`
	Bucket    string `mapstructure:"bucket"`
	AccessKey string `mapstructure:"access_key"`
	SecretKey string `mapstructure:"secret_key"`
	UseSSL    bool   `mapstructure:"use_ssl"`
	PublicURL string `mapstructure:"public_url"`
	PathStyle bool   `mapstructure:"path_style"`
}

// JWTConfig holds JWT validation settings.
type JWTConfig struct {
	Secret     string `mapstructure:"secret"`
	Expiration int64  `mapstructure:"expiration"`
}

// ImageConfig holds image processing constraints.
type ImageConfig struct {
	MaxFileSize    int64           `mapstructure:"max_file_size"`
	AllowedTypes   []string        `mapstructure:"allowed_types"`
	MaxWidth       int             `mapstructure:"max_width"`
	MaxHeight      int             `mapstructure:"max_height"`
	JPEGQuality    int             `mapstructure:"jpeg_quality"`
	ThumbnailSizes []ThumbnailSize `mapstructure:"thumbnail_sizes"`
}

// ThumbnailSize defines a single thumbnail dimension preset.
type ThumbnailSize struct {
	Name   string `mapstructure:"name"`
	Width  int    `mapstructure:"width"`
	Height int    `mapstructure:"height"`
}

// WatermarkConfig holds watermark settings.
type WatermarkConfig struct {
	Enabled   bool    `mapstructure:"enabled"`
	Type      string  `mapstructure:"type"`
	Text      string  `mapstructure:"text"`
	ImagePath string  `mapstructure:"image_path"`
	Position  string  `mapstructure:"position"`
	Opacity   float64 `mapstructure:"opacity"`
	Margin    int     `mapstructure:"margin"`
}

// LogConfig holds logging settings.
type LogConfig struct {
	Level  string `mapstructure:"level"`
	Format string `mapstructure:"format"`
}

// LoadConfig reads config.yaml and merges environment variables.
// Environment variables use the prefix MIYASHOP_ and replace dots/separators with underscores.
// Example: MIYASHOP_SERVER_PORT overrides server.port, MIYASHOP_S3_BUCKET overrides s3.bucket.
//
// S3 credentials (access_key / secret_key) are resolved from environment variables:
//  1. MIYASHOP_S3_ACCESS_KEY / MIYASHOP_S3_SECRET_KEY (project-specific, highest priority)
//  2. Provider-specific fallback env vars (see resolveS3Credentials)
func LoadConfig(configPath string) (*Config, error) {
	v := viper.New()

	// File defaults
	v.SetConfigFile(configPath)
	v.SetConfigType("yaml")

	if err := v.ReadInConfig(); err != nil {
		return nil, fmt.Errorf("failed to read config file %s: %w", configPath, err)
	}

	// Environment variable overrides
	v.SetEnvPrefix("MIYASHOP")
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))
	v.AutomaticEnv()

	var cfg Config
	if err := v.Unmarshal(&cfg); err != nil {
		return nil, fmt.Errorf("failed to unmarshal config: %w", err)
	}

	// Set defaults for optional fields
	cfg.setDefaults()

	// Resolve S3 credentials from environment variables.
	// If access_key / secret_key are still empty after Viper's AutomaticEnv,
	// try provider-specific env vars as fallback.
	resolveS3Credentials(&cfg)

	return &cfg, nil
}

// setDefaults fills in sensible default values for optional config fields.
func (c *Config) setDefaults() {
	if c.Server.Port == 0 {
		c.Server.Port = 8081
	}
	if c.Server.Mode == "" {
		c.Server.Mode = "release"
	}
	if c.Image.JPEGQuality == 0 {
		c.Image.JPEGQuality = 85
	}
	if c.Image.MaxFileSize == 0 {
		c.Image.MaxFileSize = 10 * 1024 * 1024 // 10 MB
	}
	if c.Watermark.Margin == 0 {
		c.Watermark.Margin = 20
	}
	if c.Log.Level == "" {
		c.Log.Level = "info"
	}
	if c.Log.Format == "" {
		c.Log.Format = "console"
	}
	if len(c.Image.AllowedTypes) == 0 {
		c.Image.AllowedTypes = []string{"image/jpeg", "image/png", "image/webp", "image/gif"}
	}
	if len(c.Image.ThumbnailSizes) == 0 {
		c.Image.ThumbnailSizes = []ThumbnailSize{
			{Name: "small", Width: 200, Height: 200},
			{Name: "medium", Width: 400, Height: 400},
			{Name: "large", Width: 800, Height: 800},
		}
	}
}

// resolveS3Credentials fills in S3 AccessKey and SecretKey from environment variables
// if they are not already set (via config file or MIYASHOP_* vars).
//
// Priority for each field (access_key / secret_key independently):
//  1. MIYASHOP_S3_ACCESS_KEY / MIYASHOP_S3_SECRET_KEY (handled by Viper's AutomaticEnv)
//  2. Provider-specific env vars based on s3.provider:
//     - "aws":      AWS_ACCESS_KEY_ID     / AWS_SECRET_ACCESS_KEY
//     - "alibaba":  ALIBABA_CLOUD_ACCESS_KEY_ID / ALIBABA_CLOUD_ACCESS_KEY_SECRET
//     - "minio":    MINIO_ROOT_USER       / MINIO_ROOT_PASSWORD
//     - "tc" (腾讯): COS_SECRET_ID         / COS_SECRET_KEY (or TENCENTCLOUD_SECRET_ID / TENCENTCLOUD_SECRET_KEY)
func resolveS3Credentials(cfg *Config) {
	// AccessKey
	if cfg.S3.AccessKey == "" {
		cfg.S3.AccessKey = lookupAK(cfg.S3.Provider)
	}
	// SecretKey
	if cfg.S3.SecretKey == "" {
		cfg.S3.SecretKey = lookupSK(cfg.S3.Provider)
	}
}

// lookupAK returns the Access Key from provider-specific environment variables.
func lookupAK(provider string) string {
	switch strings.ToLower(provider) {
	case "aws":
		return os.Getenv("AWS_ACCESS_KEY_ID")
	case "alibaba", "aliyun", "oss":
		return os.Getenv("ALIBABA_CLOUD_ACCESS_KEY_ID")
	case "minio":
		return os.Getenv("MINIO_ROOT_USER")
	case "tc", "tencent", "cos":
		if v := os.Getenv("COS_SECRET_ID"); v != "" {
			return v
		}
		return os.Getenv("TENCENTCLOUD_SECRET_ID")
	default:
		return ""
	}
}

// lookupSK returns the Secret Key from provider-specific environment variables.
func lookupSK(provider string) string {
	switch strings.ToLower(provider) {
	case "aws":
		return os.Getenv("AWS_SECRET_ACCESS_KEY")
	case "alibaba", "aliyun", "oss":
		return os.Getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET")
	case "minio":
		return os.Getenv("MINIO_ROOT_PASSWORD")
	case "tc", "tencent", "cos":
		if v := os.Getenv("COS_SECRET_KEY"); v != "" {
			return v
		}
		return os.Getenv("TENCENTCLOUD_SECRET_KEY")
	default:
		return ""
	}
}
