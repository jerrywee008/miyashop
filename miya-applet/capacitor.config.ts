import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.miyashop.app',
  appName: 'MiyaShop',
  webDir: 'dist',
  server: {
    cleartext: true,
    // 开发阶段：从 vite dev server 加载（API 请求通过 vite proxy → backend:8080）
    // 上线前删除 url 这行，改回 webDir 本地加载
    url: 'http://localhost:3002',
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
