<template>
  <div id="app">
    <PcNav />
    <div class="app-shell">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import PcNav from '@/components/PcNav.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  background-color: #f5f5f5;
}

.app-shell {
  width: 100%;
  min-height: 100vh;
}

/* PC / Mac 浏览器：自适应宽屏布局 */
@media screen and (min-width: 768px) {
  body {
    background-color: #f0f0f0;
  }

  #app {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .app-shell {
    width: 100%;
    max-width: 1200px;
    min-height: calc(100vh - 56px);
    background: #f5f5f5;
    box-shadow: 0 0 32px rgba(0, 0, 0, 0.08);
    flex: 1;
  }
}
</style>
