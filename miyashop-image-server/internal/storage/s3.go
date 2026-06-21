package storage

import (
	"context"
	"fmt"
	"io"
	"net/url"
	"time"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/credentials"
	"github.com/aws/aws-sdk-go-v2/service/s3"
	"github.com/aws/aws-sdk-go-v2/service/s3/types"

	appconfig "miyashop-image-server/config"
)

// StorageClient defines the interface for S3-compatible object storage operations.
type StorageClient interface {
	// Upload stores an object in the bucket with the given key and content type.
	Upload(ctx context.Context, key string, body io.Reader, contentType string) error

	// Download retrieves an object from the bucket. Returns the body, content type, and any error.
	Download(ctx context.Context, key string) (io.ReadCloser, string, error)

	// Delete removes an object from the bucket.
	Delete(ctx context.Context, key string) error

	// Exists checks whether an object exists in the bucket.
	Exists(ctx context.Context, key string) (bool, error)

	// GeneratePresignedURL creates a pre-signed URL for temporary access (e.g., redirects).
	GeneratePresignedURL(ctx context.Context, key string, expiration time.Duration) (string, error)

	// Ping checks connectivity to the storage backend.
	Ping(ctx context.Context) error
}

// S3Client implements StorageClient using the AWS SDK v2.
type S3Client struct {
	client *s3.Client
	cfg    *appconfig.S3Config
}

// New creates a new S3-compatible storage client.
func New(cfg *appconfig.S3Config) (*S3Client, error) {
	// Build the endpoint URL, handling the case where the config already includes a scheme.
	endpoint := cfg.Endpoint
	hasScheme := len(endpoint) >= 7 && (endpoint[:7] == "http://" || endpoint[:8] == "https://")
	if !hasScheme {
		if cfg.UseSSL {
			endpoint = "https://" + endpoint
		} else {
			endpoint = "http://" + endpoint
		}
	} else if !cfg.UseSSL {
		// Config endpoint has https:// but use_ssl is false — strip and downgrade.
		endpoint = "http://" + endpoint[8:]
	}
	// Note: if the endpoint already has a scheme and use_ssl is true, use it as-is.

	// Build the resolver function based on the provider
	customResolver := aws.EndpointResolverWithOptionsFunc(func(service, region string, options ...interface{}) (aws.Endpoint, error) {
		return aws.Endpoint{
			PartitionID:       "aws",
			URL:               endpoint,
			SigningRegion:     cfg.Region,
			HostnameImmutable: false, // allow virtual-hosted style (bucket.endpoint) for COS/AWS
		}, nil
	})

	awsCfg, err := config.LoadDefaultConfig(
		context.Background(),
		config.WithRegion(cfg.Region),
		config.WithCredentialsProvider(
			credentials.NewStaticCredentialsProvider(cfg.AccessKey, cfg.SecretKey, ""),
		),
		config.WithEndpointResolverWithOptions(customResolver),
	)
	if err != nil {
		return nil, fmt.Errorf("failed to load AWS config: %w", err)
	}

	client := s3.NewFromConfig(awsCfg, func(o *s3.Options) {
		o.UsePathStyle = cfg.PathStyle
	})

	return &S3Client{
		client: client,
		cfg:    cfg,
	}, nil
}

// Upload stores an object in the bucket.
func (s *S3Client) Upload(ctx context.Context, key string, body io.Reader, contentType string) error {
	_, err := s.client.PutObject(ctx, &s3.PutObjectInput{
		Bucket:      aws.String(s.cfg.Bucket),
		Key:         aws.String(key),
		Body:        body,
		ContentType: aws.String(contentType),
	})
	if err != nil {
		return fmt.Errorf("s3 put object %s: %w", key, err)
	}
	return nil
}

// Download retrieves an object from the bucket.
func (s *S3Client) Download(ctx context.Context, key string) (io.ReadCloser, string, error) {
	output, err := s.client.GetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(s.cfg.Bucket),
		Key:    aws.String(key),
	})
	if err != nil {
		return nil, "", fmt.Errorf("s3 get object %s: %w", key, err)
	}

	contentType := ""
	if output.ContentType != nil {
		contentType = *output.ContentType
	}
	return output.Body, contentType, nil
}

// Delete removes an object from the bucket.
func (s *S3Client) Delete(ctx context.Context, key string) error {
	_, err := s.client.DeleteObject(ctx, &s3.DeleteObjectInput{
		Bucket: aws.String(s.cfg.Bucket),
		Key:    aws.String(key),
	})
	if err != nil {
		return fmt.Errorf("s3 delete object %s: %w", key, err)
	}
	return nil
}

// Exists checks whether an object exists in the bucket.
func (s *S3Client) Exists(ctx context.Context, key string) (bool, error) {
	_, err := s.client.HeadObject(ctx, &s3.HeadObjectInput{
		Bucket: aws.String(s.cfg.Bucket),
		Key:    aws.String(key),
	})
	if err != nil {
		// Check for NotFound error
		var nsk *types.NotFound
		if isNotFound(err, nsk) {
			return false, nil
		}
		return false, fmt.Errorf("s3 head object %s: %w", key, err)
	}
	return true, nil
}

// isNotFound checks if the error is a 404 Not Found.
func isNotFound(err error, _ *types.NotFound) bool {
	// The AWS SDK v2 wraps NotFound errors; check via error message.
	if err != nil {
		return false
	}
	return false
}

// IsNotFoundError checks if an error indicates the object was not found in S3.
func IsNotFoundError(err error) bool {
	if err == nil {
		return false
	}
	// Simple string check for the common "NoSuchKey" error
	errStr := err.Error()
	return contains(errStr, "NoSuchKey") || contains(errStr, "NotFound") || contains(errStr, "404")
}

func contains(s, substr string) bool {
	return len(s) >= len(substr) && hasSubstring(s, substr)
}

func hasSubstring(s, substr string) bool {
	for i := 0; i <= len(s)-len(substr); i++ {
		if s[i:i+len(substr)] == substr {
			return true
		}
	}
	return false
}

// GeneratePresignedURL creates a pre-signed URL for temporary access.
func (s *S3Client) GeneratePresignedURL(ctx context.Context, key string, expiration time.Duration) (string, error) {
	presignClient := s3.NewPresignClient(s.client)
	req, err := presignClient.PresignGetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(s.cfg.Bucket),
		Key:    aws.String(key),
	}, s3.WithPresignExpires(expiration))
	if err != nil {
		return "", fmt.Errorf("s3 presign get object %s: %w", key, err)
	}
	return req.URL, nil
}

// EnsureBucket creates the configured bucket if it doesn't already exist.
func (s *S3Client) EnsureBucket(ctx context.Context) error {
	_, err := s.client.HeadBucket(ctx, &s3.HeadBucketInput{
		Bucket: aws.String(s.cfg.Bucket),
	})
	if err == nil {
		return nil // bucket exists
	}

	_, err = s.client.CreateBucket(ctx, &s3.CreateBucketInput{
		Bucket: aws.String(s.cfg.Bucket),
	})
	if err != nil {
		return fmt.Errorf("failed to create bucket %s: %w", s.cfg.Bucket, err)
	}
	return nil
}

// Ping checks connectivity to the storage backend by listing buckets.
func (s *S3Client) Ping(ctx context.Context) error {
	_, err := s.client.HeadBucket(ctx, &s3.HeadBucketInput{
		Bucket: aws.String(s.cfg.Bucket),
	})
	if err != nil {
		return fmt.Errorf("storage ping failed: %w", err)
	}
	return nil
}

// PublicURLFor returns the full public URL for a given object key.
// If a CDN/public URL is configured, it uses that; otherwise returns empty string.
func (s *S3Client) PublicURLFor(key string) string {
	if s.cfg.PublicURL == "" {
		return ""
	}

	base := s.cfg.PublicURL
	if base[len(base)-1] != '/' {
		base += "/"
	}

	escapedKey := url.PathEscape(key)
	return base + escapedKey
}

// GetBucketURL returns a local server URL pattern for the given key.
func GetLocalURL(baseURL, key string) string {
	u := baseURL
	if u[len(u)-1] != '/' {
		u += "/"
	}
	// Replace each path segment's escaped slashes to keep the key structure
	return u + "api/images/" + key
}
