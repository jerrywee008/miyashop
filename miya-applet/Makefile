# ========================================
# Miya Applet — iOS & Android 构建
# ========================================

.PHONY: install build ios android sync

# 安装依赖
install:
	npm install --legacy-peer-deps

# 构建 Web 产物
build:
	npm run build

# 同步 Capacitor（生成 ios/android 目录）
sync:
	npx cap sync

# 添加 iOS 平台
ios-add:
	npx cap add ios

# 添加 Android 平台
android-add:
	npx cap add android

# 构建 + 同步 + 打开 Xcode
ios: build
	npx cap copy
	npx cap sync ios
	npx cap open ios

# 构建 + 同步 + 打开 Android Studio
android: build
	npx cap copy
	npx cap sync android
	npx cap open android

# 清理
clean:
	rm -rf dist node_modules ios android
