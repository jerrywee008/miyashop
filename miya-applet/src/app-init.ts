/**
 * Capacitor 原生能力初始化
 * 仅在 Capacitor 环境中生效，浏览器中优雅降级
 */

// 检测是否在 Capacitor 原生环境中运行
const isNative = (): boolean => {
  try {
    return !!(window as any).Capacitor?.isNativePlatform()
  } catch {
    return false
  }
}

export function initCapacitor() {
  if (!isNative()) {
    console.log('[Applet] 浏览器模式 — Capacitor 插件未激活')
    return
  }
  console.log('[Applet] 原生模式 — 初始化 Capacitor 插件')

  // 动态导入 Capacitor 插件（仅在原生环境加载）
  import('./capacitor-bridge').then(({ initNativeBridge }) => {
    initNativeBridge()
  }).catch(() => {
    // Fallback: Capacitor 插件不可用时不阻塞
    console.warn('[Applet] Capacitor 插件加载失败，使用 Web 模式')
  })
}
