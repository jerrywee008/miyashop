<template>
  <div class="seckill-page">
    <div class="nav-bar"><span @click="goBack">← 返回</span><span class="nav-title">秒杀专区</span><span></span></div>

    <div class="seckill-header" v-if="activityName">
      <div class="seckill-time">
        <span class="time-label">距结束还剩</span>
        <van-count-down :time="countdownTime" format="HH:mm:ss" class="countdown">
          <template #default="timeData">
            <span class="time-block">{{ timeData.hours.toString().padStart(2, '0') }}</span>
            <span class="time-colon">:</span>
            <span class="time-block">{{ timeData.minutes.toString().padStart(2, '0') }}</span>
            <span class="time-colon">:</span>
            <span class="time-block">{{ timeData.seconds.toString().padStart(2, '0') }}</span>
          </template>
        </van-count-down>
      </div>
    </div>

    <van-loading v-if="loading" class="loading-center" />
    <div v-else-if="products.length === 0 && !loading" class="empty-tip">暂无秒杀活动</div>

    <div class="seckill-products" v-else>
      <div class="seckill-product-item" v-for="item in products" :key="item.id" @click="goProduct(item.productId || item.id)">
        <img :src="item.image || defaultImg" :alt="item.name" class="product-image" />
        <div class="product-info">
          <div class="product-name">{{ item.name }}</div>
          <div class="product-price-row">
            <div class="seckill-price"><span class="price-symbol">¥</span><span class="price-value">{{ item.seckillPrice || item.price }}</span></div>
            <div class="original-price">¥{{ item.originalPrice }}</div>
          </div>
          <div class="progress-row">
            <van-progress :percentage="item.percent || 0" color="#FF6B95" :show-pivot="false" stroke-width="6" />
            <span class="progress-text" v-if="item.stock != null">库存: {{ item.stock }}</span>
          </div>
        </div>
      </div>
    </div>

    <TabBar :tab="1" />
  </div>
</template>

<script setup lang="ts">
import TabBar from '@/components/TabBar.vue'
import { ref, onMounted } from 'vue'
import { getSeckillActivities, getSeckillSkus } from '@/api/seckill'
import { getSkuList } from '@/api/product'

const loading = ref(false)
const countdownTime = ref(2 * 60 * 60 * 1000)
const activityName = ref('')
const products = ref<any[]>([])
const defaultImg = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" fill="%23f0f0f0"><rect width="200" height="200"/><text x="100" y="110" text-anchor="middle" fill="%23ccc" font-size="14">暂无图片</text></svg>'

const fetchData = async () => {
  loading.value = true
  try {
    // 1. 获取秒杀活动列表
    const res = await getSeckillActivities()
    if (res.code !== 200) return
    const list = res.data?.records || res.data || []
    if (list.length === 0) return

    const first = list[0]
    activityName.value = first.name || ''
    if (first.endTime) {
      const diff = new Date(first.endTime).getTime() - Date.now()
      if (diff > 0) countdownTime.value = diff
    }

    // 2. 获取该活动的SKU列表
    const skuRes = await getSeckillSkus(first.id)
    if (skuRes.code !== 200 || !skuRes.data?.length) return
    const seckillSkus = skuRes.data

    // 3. 获取所有上架SKU（用于补充商品名称/图片/原价）
    const allSkuRes = await getSkuList({ page: 1, size: 200, status: 1 })
    const allSkus = allSkuRes.code === 200 ? (allSkuRes.data?.records || []) : []

    // 4. 组合数据
    products.value = seckillSkus.slice(0, 12).map((ss: any) => {
      const info = allSkus.find((s: any) => s.id === ss.skuId) || {}
      return {
        id: ss.skuId,
        activityId: ss.activityId,
        name: info.name || `SKU #${ss.skuId}`,
        image: info.image || defaultImg,
        seckillPrice: ss.seckillPrice,
        originalPrice: info.price || 0,
        stock: ss.stock,
        sold: ss.sold || 0,
        percent: ss.stock ? Math.round((ss.sold || 0) / ((ss.sold || 0) + ss.stock) * 100) : 0
      }
    })
  } catch { /* ignore */ }
  finally { loading.value = false }
}

const goBack = () => { window.location.hash = '#/' }
const goProduct = (id: number) => {
  window.location.hash = '#/product/' + id + '?type=seckill'
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.nav-bar { display: flex; align-items: center; justify-content: space-between; height: 46px; padding: 0 16px; background: #fff; border-bottom: 1px solid #eee; position: sticky; top: 0; z-index: 100; }
.nav-title { font-size: 16px; font-weight: 600; }
.seckill-page { min-height: 100vh; background: var(--bg-secondary); padding-bottom: 60px; }
.seckill-header { background: linear-gradient(135deg, #FF6B95, #FF5580); padding: 20px; display: flex; justify-content: center; align-items: center; }
.seckill-time { display: flex; align-items: center; gap: 12px; color: white; }
.time-label { font-size: 14px; }
.countdown { display: flex; align-items: center; gap: 4px; }
.time-block { display: inline-block; width: 28px; height: 28px; line-height: 28px; text-align: center; background: rgba(0,0,0,0.3); border-radius: 4px; font-size: 16px; font-weight: 600; }
.time-colon { font-size: 16px; }
.seckill-products { padding: 12px; }
.seckill-product-item { display: flex; gap: 12px; padding: 12px; background: var(--bg-primary); border-radius: 8px; margin-bottom: 12px; }
.product-image { width: 100px; height: 100px; border-radius: 8px; object-fit: cover; }
.product-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.product-name { font-size: 14px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price-row { display: flex; align-items: baseline; gap: 12px; }
.seckill-price { display: flex; align-items: baseline; }
.price-symbol { font-size: 14px; color: var(--primary-color); }
.price-value { font-size: 24px; font-weight: 600; color: var(--primary-color); }
.original-price { font-size: 12px; color: var(--text-secondary); text-decoration: line-through; }
.progress-row { display: flex; align-items: center; gap: 8px; }
.progress-text { font-size: 12px; color: var(--primary-color); white-space: nowrap; }
.loading-center { display: flex; justify-content: center; padding: 40px; }
.empty-tip { text-align: center; padding: 60px 0; color: #999; font-size: 14px; }
@media screen and (min-width: 768px) {
  .seckill-page { padding-bottom: 0; }
  .seckill-products { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; padding: 24px 32px; max-width: 1200px; margin: 0 auto; }
  .seckill-product-item { flex-direction: column; padding: 16px; }
  .seckill-product-item .product-image { width: 100%; height: 220px; }
}
</style>
