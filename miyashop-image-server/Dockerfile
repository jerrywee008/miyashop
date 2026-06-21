# ========================================
# MiyaShop Image Server — Multi-stage Docker build
# ========================================

# Stage 1: Build the Go binary
FROM golang:1.22-alpine AS builder

# CGO is required for the imaging library
RUN apk add --no-cache gcc musl-dev

WORKDIR /build

# Cache dependency downloads
COPY go.mod go.sum ./
RUN go mod download

# Copy source code and build
COPY . .
RUN CGO_ENABLED=1 GOOS=linux go build -a -installsuffix cgo -o image-server .

# Stage 2: Minimal runtime image
FROM alpine:3.20

RUN apk add --no-cache ca-certificates tzdata
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

WORKDIR /app

# Copy the binary and default config from builder
COPY --from=builder /build/image-server .
COPY --from=builder /build/config.yaml .

# Create a non-root user
RUN adduser -D -g '' miyashop
USER miyashop

EXPOSE 8081

ENTRYPOINT ["./image-server"]
CMD ["-config", "config.yaml"]
