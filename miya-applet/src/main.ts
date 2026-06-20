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
  const errStr = err instanceof Error ? (err.message || String(err)) : JSON.stringify(err)
  const stack = err instanceof Error ? (err.stack || '') : ''
  let compPath = ''
  if (instance) {
    const type = instance.type || (instance.$ as any)?.type || {}
    compPath = (type.name || type.__name || instance.$options?.name || '?') +
               (type.__file ? ' @' + type.__file.split('/').slice(-2).join('/') : '')
  }
  console.error('[Vue Error]', err, '| info:', info, '| component:', compPath, '| stack:', stack.substring(0,300))
}

// 捕获未处理的 Promise rejection
window.addEventListener('unhandledrejection', (e) => {
  console.error('[Unhandled Rejection]', e.reason)
})

app.use(pinia)
app.use(router)

app.mount('#app')

// 初始化 Capacitor 原生桥接
initCapacitor()
