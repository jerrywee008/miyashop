import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from 'unplugin-vue-components/resolvers'

// 共享 H5 源码路径
const H5_SRC = resolve(__dirname, '../miyashop-frontend/miyashop-h5/src')

// Capacitor 的 capacitor:// 自定义 scheme 不支持 ES module，
// Vite 默认会输出 type="module" + crossorigin，导致 WKWebView 白屏
function fixCapacitorHtml() {
  return {
    name: 'fix-capacitor-html',
    enforce: 'post' as const,
    transformIndexHtml(html: string) {
      // 只移除 crossorigin，保留 type="module"
      return html.replace(/crossorigin(="[^"]*")?\s*/g, '')
    }
  }
}

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [VantResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      resolvers: [VantResolver()],
      dts: 'src/components.d.ts'
    }),
    fixCapacitorHtml()
  ],
  resolve: {
    alias: {
      // Capacitor 安全版 store（reactive 替代 Pinia）
      '@/store/user': resolve(__dirname, 'src/store-capacitor.ts'),
      // Capacitor 专用路由（Hash History），覆盖 H5 的 Web History 路由
      '@/router': resolve(__dirname, 'src/router.ts'),
      // 指向 H5 源码目录，共享所有页面/组件/路由
      '@': H5_SRC,
    }
  },
  // PostCSS 配置由 postcss.config.cjs 自动加载
  server: {
    port: 3002,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    // Capacitor WKWebView 中动态 import() 无法从 capacitor:// scheme 加载
    // 内联所有动态导入，避免代码分割导致的路由空白
    rollupOptions: {
      output: {
        inlineDynamicImports: true
      }
    }
  }
})
