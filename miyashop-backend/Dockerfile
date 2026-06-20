# ============================================
# MiyaShop Backend — Dockerfile
# ============================================

# --- Stage 1: Build with Maven ---
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 先复制 pom 文件，利用 Docker 缓存层加速
COPY pom.xml .
COPY miyashop-common/pom.xml miyashop-common/
COPY miyashop-service/pom.xml miyashop-service/
COPY miyashop-web/pom.xml miyashop-web/

# 下载依赖（缓存友好）
RUN mvn dependency:go-offline -B -q

# 复制源码并构建
COPY . .
RUN mvn clean package -pl miyashop-web -am -DskipTests -B -q

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制构建产物
COPY --from=builder /build/miyashop-web/target/*.jar app.jar

# 时区设置
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
