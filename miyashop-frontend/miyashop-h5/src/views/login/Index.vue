<template>
  <div class="page">
    <div class="nav">
      <span @click="goBack">← 返回</span>
      <span>登录</span>
      <span></span>
    </div>
    <div class="body">
      <div class="logo">🌸 MiyaShop</div>
      <div class="info">当前路由: {{ path }}</div>
      <button class="btn" @click="doLogin">登录（Mock）</button>
      <div v-if="msg" class="msg">{{ msg }}</div>
      <button class="btn" style="background:#333;margin-top:12px" @click="goBack">返回首页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'LoginPage' })
import { ref } from 'vue'

const path = ref(window.location.hash || '?')
const msg = ref('')

const doLogin = () => {
  msg.value = '登录成功'
  setTimeout(() => { window.location.hash = '#/' }, 1000)
}
const goBack = () => { window.location.hash = '#/' }
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }
.nav { display: flex; justify-content: space-between; align-items: center; height: 46px; padding: 0 16px; background: #fff; }
.body { padding: 40px 16px; text-align: center; }
.logo { font-size: 28px; color: #FF6B95; margin-bottom: 20px; }
.info { font-size: 14px; color: #999; margin-bottom: 20px; }
.btn { display: block; width: 100%; height: 46px; background: #FF6B95; color: #fff; border: none; border-radius: 23px; font-size: 16px; margin-top: 20px; cursor: pointer; }
.msg { font-size: 14px; color: #FF6B95; margin-top: 16px; }
</style>
