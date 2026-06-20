/**
 * Miya Applet 入口 — Capacitor + Vue 3
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from '@/App.vue'
import router from '@/router'
import '@/assets/styles/main.css'
import { initCapacitor } from './app-init'

const app = createApp(App)
const pinia = createPinia()

// 捕获 Vue 内部错误（不会触发 window.onerror）
app.config.errorHandler = (err, instance, info) => {
  const msg = String(err).substring(0, 100)
  const stack = err instanceof Error ? (err.stack || '').substring(0, 200) : ''
  const comp = instance?.$options?.name || instance?.$.type?.name || '?'
  ;(window as any)._dlog?.('VUE_ERR:' + msg + '|comp:' + comp + '|info:' + info + '|stack:' + stack)
  console.error('Vue error:', err, instance, info)
}

app.use(pinia)
app.use(router)

app.mount('#app')

// 初始化 Capacitor 原生桥接
initCapacitor()
