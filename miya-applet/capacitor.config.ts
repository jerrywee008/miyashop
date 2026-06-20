import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.miyashop.app',
  appName: 'MiyaShop',
  webDir: 'dist',
  server: {
    // 开发阶段允许 HTTP 请求，生产环境应移除
    cleartext: true,
    // iOS/Android 默认加载本地 web assets，也可配置远程 URL
  },
  ios: {
    contentInset: 'always',
    scheme: 'MiyaShop',
  },
  android: {
    allowMixedContent: true,
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 2000,
      backgroundColor: '#FF6B95',
      showSpinner: false,
    },
    StatusBar: {
      style: 'light',
      backgroundColor: '#FF6B95',
    },
    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert'],
    },
  },
};

export default config;
