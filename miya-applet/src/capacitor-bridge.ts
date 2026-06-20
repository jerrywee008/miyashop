/**
 * Capacitor 原生桥接 — 状态栏、返回键、启动屏等
 */
import { Capacitor } from '@capacitor/core'
import { App } from '@capacitor/app'
import { StatusBar, Style } from '@capacitor/status-bar'
import { SplashScreen } from '@capacitor/splash-screen'
import { Keyboard } from '@capacitor/keyboard'
import { Share } from '@capacitor/share'

export async function initNativeBridge() {
  try {
    const platform = Capacitor.getPlatform()

    // 1. 状态栏 — 粉红色背景 + 白色文字
    await StatusBar.setStyle({ style: Style.Light })
    // setBackgroundColor 仅 Android 支持，iOS 返回 UNIMPLEMENTED
    if (platform === 'android') {
      await StatusBar.setBackgroundColor({ color: '#FF6B95' })
    }

    // 2. 键盘事件监听
    Keyboard.addListener('keyboardWillShow', () => {
      document.body.classList.add('keyboard-open')
    })
    Keyboard.addListener('keyboardWillHide', () => {
      document.body.classList.remove('keyboard-open')
    })

    // 3. Android 返回键处理
    App.addListener('backButton', ({ canGoBack }) => {
      if (canGoBack) {
        window.history.back()
      } else {
        // 双击退出提示由 App 层处理
      }
    })

    // 4. 隐藏启动屏
    await SplashScreen.hide()

    console.log('[Applet] 原生桥接初始化完成')
  } catch (err) {
    console.error('[Applet] 原生桥接初始化失败:', err)
  }
}

/**
 * 原生分享
 */
export async function nativeShare(title: string, text: string, url?: string) {
  try {
    await Share.share({ title, text, url })
  } catch {
    // 用户取消或不可用
  }
}
