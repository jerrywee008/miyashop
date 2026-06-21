# ========================================
# MiyaShop Image Server — Multi-stage Docker build
# ========================================
#
# Build with Docker Hub (default):
#   docker build -t image-server .
#
# Build with China mirror:
#   docker build \
#     --build-arg GOLANG_IMAGE=docker.m.daocloud.io/library/golang:1.22-alpine \
#     --build-arg ALPINE_IMAGE=docker.m.daocloud.io/library/alpine:3.20 \
#     -t image-server .
#
# Or set defaults via docker-compose build args or environment.

ARG GOLANG_IMAGE=golang:1.22-alpine
ARG ALPINE_IMAGE=alpine:3.20

# ========================================
# Stage 1: Build the Go binary
# ========================================
FROM ${GOLANG_IMAGE} AS builder

# Use China Go module proxy for faster downloads within China
ARG GOPROXY=https://goproxy.cn,direct
ENV GOPROXY=${GOPROXY}

# CGO is required for the imaging library
RUN apk add --no-cache gcc musl-dev

WORKDIR /build

# Cache dependency downloads
COPY go.mod go.sum ./
RUN go mod download

# Copy source code and build
COPY . .
RUN CGO_ENABLED=1 GOOS=linux go build -a -installsuffix cgo -o image-server .

# ========================================
# Stage 2: Minimal runtime image
# ========================================
FROM ${ALPINE_IMAGE}

# Use China mirrors for apk packages when in China
ARG APK_MIRROR=""
RUN if [ -n "${APK_MIRROR}" ]; then \
      sed -i "s|dl-cdn.alpinelinux.org|${APK_MIRROR}|g" /etc/apk/repositories; \
    fi

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
