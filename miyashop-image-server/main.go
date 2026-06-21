package main

import (
	"context"
	"flag"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"

	"miyashop-image-server/config"
	"miyashop-image-server/internal/handler"
	"miyashop-image-server/internal/middleware"
	"miyashop-image-server/internal/processor"
	"miyashop-image-server/internal/service"
	"miyashop-image-server/internal/storage"
)

func main() {
	// Parse command-line flags
	configPath := flag.String("config", "config.yaml", "Path to configuration file")
	flag.Parse()

	// Load configuration
	cfg, err := config.LoadConfig(*configPath)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to load config: %v\n", err)
		os.Exit(1)
	}

	// Initialize logger
	logger := initLogger(cfg)
	defer logger.Sync()

	logger.Info("Starting MiyaShop Image Server",
		zap.Int("port", cfg.Server.Port),
		zap.String("mode", cfg.Server.Mode),
		zap.String("s3_provider", cfg.S3.Provider),
		zap.String("s3_bucket", cfg.S3.Bucket),
	)

	// Set Gin mode
	gin.SetMode(cfg.Server.Mode)

	// Initialize storage client
	store, err := storage.New(&cfg.S3)
	if err != nil {
		logger.Fatal("Failed to initialize storage client", zap.Error(err))
	}

	// Ensure the bucket exists
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	s3Client := store // store is *storage.S3Client
	_ = s3Client       // We'll use the concrete type's EnsureBucket if needed
	// Type-assert to check for EnsureBucket method
	type bucketEnsurer interface {
		EnsureBucket(ctx context.Context) error
	}
	if ensurer, ok := interface{}(store).(bucketEnsurer); ok {
		if err := ensurer.EnsureBucket(ctx); err != nil {
			logger.Warn("Failed to ensure bucket exists", zap.Error(err))
		}
	}
	cancel()

	// Initialize image processor
	proc := processor.New(&cfg.Image, &cfg.Watermark)

	// Determine public URL base
	publicURL := fmt.Sprintf("http://localhost:%d", cfg.Server.Port)
	if envPort := os.Getenv("MIYASHOP_SERVER_PORT"); envPort != "" {
		publicURL = fmt.Sprintf("http://localhost:%s", envPort)
	}
	// Override with public URL if configured
	if cfg.S3.PublicURL != "" {
		publicURL = cfg.S3.PublicURL
	}

	// Initialize service layer
	svc := service.NewImageService(store, proc, cfg, logger, publicURL)

	// Initialize HTTP handlers
	h := handler.NewImageHandler(svc, logger)

	// Setup router
	router := gin.New()

	// --- Global middleware ---
	router.Use(middleware.CORSMiddleware())
	router.Use(ginZapLogger(logger))
	router.Use(gin.Recovery())

	// --- Public routes ---
	router.GET("/health", h.Health)

	// Image serving routes (public - no auth required for viewing)
	// NOTE: Order matters! Specific routes before wildcard routes.
	thumbnailGroup := router.Group("/api/images/thumb")
	{
		thumbnailGroup.GET("/:size/*key", h.GetThumbnail)
	}
	router.GET("/api/images/*key", h.Download)

	// --- Authenticated routes (admin only) ---
	authGroup := router.Group("/api/images")
	authGroup.Use(middleware.AuthMiddleware(cfg.JWT.Secret))
	{
		// Batch upload
		authGroup.POST("/upload", middleware.AdminMiddleware(), h.Upload)

		// Delete
		authGroup.DELETE("/*key", middleware.AdminMiddleware(), h.Delete)
	}

	// Create HTTP server
	srv := &http.Server{
		Addr:         fmt.Sprintf(":%d", cfg.Server.Port),
		Handler:      router,
		ReadTimeout:  30 * time.Second,
		WriteTimeout: 60 * time.Second,
		IdleTimeout:  120 * time.Second,
	}

	// Start server in a goroutine
	go func() {
		logger.Info("HTTP server listening", zap.Int("port", cfg.Server.Port))
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logger.Fatal("Server failed to start", zap.Error(err))
		}
	}()

	// Wait for interrupt signal for graceful shutdown
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	sig := <-quit
	logger.Info("Received signal, shutting down", zap.String("signal", sig.String()))

	// Graceful shutdown with timeout
	shutdownCtx, shutdownCancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer shutdownCancel()

	if err := srv.Shutdown(shutdownCtx); err != nil {
		logger.Fatal("Server forced to shutdown", zap.Error(err))
	}

	logger.Info("Server exited gracefully")
}

// initLogger creates a configured zap logger.
func initLogger(cfg *config.Config) *zap.Logger {
	var level zapcore.Level
	switch cfg.Log.Level {
	case "debug":
		level = zapcore.DebugLevel
	case "warn":
		level = zapcore.WarnLevel
	case "error":
		level = zapcore.ErrorLevel
	default:
		level = zapcore.InfoLevel
	}

	encoderCfg := zap.NewProductionEncoderConfig()
	encoderCfg.TimeKey = "timestamp"
	encoderCfg.EncodeTime = zapcore.ISO8601TimeEncoder

	var encoder zapcore.Encoder
	if cfg.Log.Format == "json" {
		encoder = zapcore.NewJSONEncoder(encoderCfg)
	} else {
		encoderCfg.EncodeLevel = zapcore.CapitalColorLevelEncoder
		encoder = zapcore.NewConsoleEncoder(encoderCfg)
	}

	core := zapcore.NewCore(encoder, zapcore.AddSync(os.Stdout), level)
	return zap.New(core, zap.AddCaller(), zap.AddStacktrace(zapcore.ErrorLevel))
}

// ginZapLogger returns a Gin middleware that logs requests using zap.
func ginZapLogger(logger *zap.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		path := c.Request.URL.Path
		query := c.Request.URL.RawQuery

		c.Next()

		latency := time.Since(start)
		status := c.Writer.Status()

		fields := []zap.Field{
			zap.Int("status", status),
			zap.String("method", c.Request.Method),
			zap.String("path", path),
			zap.String("query", query),
			zap.String("ip", c.ClientIP()),
			zap.Duration("latency", latency),
			zap.Int("size", c.Writer.Size()),
		}

		if len(c.Errors) > 0 {
			for _, e := range c.Errors {
				logger.Error("Request error", append(fields, zap.String("error", e.Err.Error()))...)
			}
		} else if status >= 500 {
			logger.Error("Server error", fields...)
		} else if status >= 400 {
			logger.Warn("Client error", fields...)
		} else {
			logger.Info("Request", fields...)
		}
	}
}
