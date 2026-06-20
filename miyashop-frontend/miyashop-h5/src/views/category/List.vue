<template>
  <div class="page">
    <div class="nav">
      <span @click="goBack" class="back">← 返回</span>
      <span class="title">{{ catName }}</span>
      <span></span>
    </div>
    <div class="list">
      <div class="card" v-for="p in items" :key="p.id" @click="goProduct(p.id)">
        <img :src="p.image" class="img" />
        <div class="info">
          <div class="name">{{ p.name }}</div>
          <div class="price">¥{{ p.price }}</div>
        </div>
      </div>
      <div v-if="items.length===0" class="empty">暂无商品</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const catName = ref('')

// 从 hash 中手动解析分类名
const hash = window.location.hash
const match = hash.match(/#\/category\/(.+)/)
catName.value = match ? decodeURIComponent(match[1]) : '分类'

const products: Record<string, any[]> = {
  '连衣裙': [
    { id: 1, image: 'https://via.placeholder.com/300/FF6B95/fff?text=D1', name: '法式复古连衣裙', price: 299 },
    { id: 2, image: 'https://via.placeholder.com/300/FFB6C1/fff?text=D2', name: '夏季碎花连衣裙', price: 259 },
  ],
  '衬衫': [
    { id: 3, image: 'https://via.placeholder.com/300/DA70D6/fff?text=S1', name: '缎面飘带衬衫', price: 199 },
    { id: 4, image: 'https://via.placeholder.com/300/FF69B4/fff?text=S2', name: '基础款白衬衫', price: 159 },
  ],
  'T恤': [{ id: 5, image: 'https://via.placeholder.com/300/FFC0CB/fff?text=T1', name: '纯棉印花T恤', price: 99 }],
  '裤子': [{ id: 6, image: 'https://via.placeholder.com/300/FFB6C1/fff?text=P1', name: '高腰直筒西裤', price: 269 }],
  '首饰': [{ id: 7, image: 'https://via.placeholder.com/300/DA70D6/fff?text=J1', name: '珍珠锁骨链', price: 159 }],
  '彩妆': [{ id: 8, image: 'https://via.placeholder.com/300/FF6B95/fff?text=C1', name: '丝绒口红', price: 129 }],
  '香水': [{ id: 9, image: 'https://via.placeholder.com/300/FFB6C1/fff?text=PF1', name: '花香淡香水', price: 299 }],
  '更多': [{ id: 10, image: 'https://via.placeholder.com/300/FFC0CB/fff?text=M1', name: '精选好物', price: 199 }],
}

const items = ref<any[]>([])
items.value = products[catName.value] || products['更多'] || []

const goProduct = (id: number) => { window.location.hash = '#/product/' + id }
const goBack = () => { window.location.hash = '#/' }
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }
.nav { display: flex; align-items: center; justify-content: space-between; height: 46px; padding: 0 16px; background: #fff; border-bottom: 1px solid #eee; }
.back { color: #FF6B95; font-size: 14px; }
.title { font-size: 16px; font-weight: 600; }
.list { display: flex; flex-wrap: wrap; padding: 12px; gap: 8px; }
.card { width: calc(50% - 4px); background: #fff; border-radius: 8px; overflow: hidden; }
.img { width: 100%; aspect-ratio: 1; object-fit: cover; }
.info { padding: 8px 10px 12px; }
.name { font-size: 14px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price { color: #FF6B95; font-weight: 700; font-size: 16px; margin-top: 4px; }
.empty { width: 100%; text-align: center; padding: 60px 0; color: #999; font-size: 14px; }
</style>
