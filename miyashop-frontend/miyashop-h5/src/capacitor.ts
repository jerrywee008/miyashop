/**
 * Capacitor 原生桥接初始化
 * 在 main.ts 之后、app.mount 之前调用
 */
import { App } from '@capacitor/app';
import { StatusBar, Style } from '@capacitor/status-bar';
import { SplashScreen } from '@capacitor/splash-screen';
import { Keyboard } from '@capacitor/keyboard';

export function initCapacitor() {
  // 状态栏：浅色文字（粉红背景）
  StatusBar.setStyle({ style: Style.Light });
  StatusBar.setBackgroundColor({ color: '#FF6B95' });

  // 键盘事件：iOS 键盘弹出时调整布局
  Keyboard.addListener('keyboardWillShow', () => {
    document.body.classList.add('keyboard-open');
  });
  Keyboard.addListener('keyboardWillHide', () => {
    document.body.classList.remove('keyboard-open');
  });

  // 返回按钮：Android 硬件返回键处理
  App.addListener('backButton', ({ canGoBack }) => {
    if (canGoBack) {
      window.history.back();
    } else {
      App.exitApp();
    }
  });

  // 隐藏启动屏
  SplashScreen.hide();
}
