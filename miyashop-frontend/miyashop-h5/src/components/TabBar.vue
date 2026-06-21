<template>
  <div class="tabbar" :class="{ fixed: !inline, inline: inline }">
    <div class="t-item" :class="{ on: active === 0 }" @click="go('/')">
      <span class="t-icon">🏠</span>
      <span class="t-label">首页</span>
    </div>
    <div class="t-item" :class="{ on: active === 1 }" @click="go('/seckill')">
      <span class="t-icon">⚡</span>
      <span class="t-label">秒杀</span>
    </div>
    <div class="t-item" :class="{ on: active === 2 }" @click="go('/groupbuy')">
      <span class="t-icon">👥</span>
      <span class="t-label">团购</span>
    </div>
    <div class="t-item" :class="{ on: active === 3 }" @click="go('/cart')">
      <span class="t-icon-wrap">
        <span class="t-icon">🛒</span>
        <span v-if="badge > 0" class="t-badge">{{ badge > 99 ? '99+' : badge }}</span>
      </span>
      <span class="t-label">购物车</span>
    </div>
    <div class="t-item" :class="{ on: active === 4 }" @click="go('/user')">
      <span class="t-icon">👤</span>
      <span class="t-label">我的</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ tab?: number; badge?: number; inline?: boolean }>()

const active = computed(() => props.tab ?? 0)

const go = (path: string) => {
  window.location.hash = '#' + path
}
</script>

<style scoped>
.tabbar { display: flex; align-items: center; justify-content: space-around; height: 50px; background: #fff; border-top: 1px solid #eee; }
.tabbar.fixed { position: fixed; bottom: 0; left: 0; right: 0; z-index: 999; padding-bottom: env(safe-area-inset-bottom); }
.tabbar.inline { position: relative; }
.t-item { display: flex; flex-direction: column; align-items: center; justify-content: center; flex: 1; height: 100%; cursor: pointer; gap: 1px; min-width: 0; }
.t-icon { font-size: 20px; line-height: 1; }
.t-label { font-size: 10px; color: #666; white-space: nowrap; }
.t-item.on .t-label { color: #FF6B95; }
.t-icon-wrap { position: relative; display: inline-flex; }
.t-badge {
  position: absolute; top: -6px; right: -10px; background: #FF6B95; color: #fff;
  font-size: 9px; min-width: 14px; height: 14px; border-radius: 7px;
  display: flex; align-items: center; justify-content: center; padding: 0 3px;
}
</style>
