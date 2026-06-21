<template>
  <div class="page">
    <div class="nav">
      <span @click="goBack" class="back">← 返回</span>
      <span class="title">{{ catName }}</span>
      <span></span>
    </div>
    <van-loading v-if="loading" class="loading-center" />
    <div class="list" v-else>
      <div class="card" v-for="p in items" :key="p.id" @click="goProduct(p.id)">
        <img :src="getImg(p)" class="img" />
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
import { ref, onMounted } from 'vue'
import { getProductList } from '@/api/product'

const catName = ref('')
const loading = ref(false)
const items = ref<any[]>([])
const defaultImg = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" fill="%23f0f0f0"><rect width="300" height="300"/><text x="150" y="160" text-anchor="middle" fill="%23ccc" font-size="16">暂无图片</text></svg>'

// 从 hash 解析分类ID或名称
const hash = window.location.hash
const match = hash.match(/#\/category\/(.+)/)
const raw = match ? decodeURIComponent(match[1]) : ''
catName.value = raw || '分类'

const getImg = (p: any) => {
  if (p.images) return p.images.split(',')[0]
  return defaultImg
}

const fetchData = async () => {
  loading.value = true
  try {
    const catId = Number(raw)
    const params: any = { page: 1, size: 20 }
    if (!isNaN(catId)) {
      params.categoryId = catId
    } else if (raw) {
      params.name = raw
    }
    const res = await getProductList(params)
    if (res.code === 200) {
      items.value = res.data?.records || []
    }
    // Try to get category name from first product
    if (catName.value === '分类' && items.value.length > 0) {
      catName.value = items.value[0].categoryName || '分类'
    }
  } catch { /* ignore */ }
  finally { loading.value = false }
}

const goProduct = (id: number) => { window.location.hash = '#/product/' + id }
const goBack = () => { window.location.hash = '#/' }

onMounted(() => { fetchData() })
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
.loading-center { display: flex; justify-content: center; padding: 40px; }
</style>
