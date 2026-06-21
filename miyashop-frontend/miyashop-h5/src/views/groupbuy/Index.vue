<template>
  <div class="groupbuy-page">
    <van-nav-bar title="拼团专区" left-arrow @click="goBack" fixed />
    <van-loading v-if="loading" class="loading-center" />
    <div v-else-if="products.length === 0 && !loading" class="empty-tip">暂无拼团活动</div>
    <div class="groupbuy-products" v-else>
      <div class="groupbuy-product-item" v-for="item in products" :key="item.id" @click="goTeam(item)">
        <div class="groupbuy-badge" :class="{ 'soon': item.progress < 30 }" v-if="item.currentPeople != null">
          {{ item.currentPeople }}人拼团
        </div>
        <img :src="item.productImage || item.image || defaultImg" :alt="item.productName || item.name" class="product-image" />
        <div class="product-info">
          <div class="product-name">{{ item.name || item.productName }}</div>
          <div class="product-price-row">
            <div class="groupbuy-price"><span class="price-symbol">¥</span><span class="price-value">{{ item.groupbuyPrice }}</span></div>
            <div class="original-price">¥{{ item.originalPrice }}</div>
            <div class="discount-tag" v-if="item.originalPrice && item.groupbuyPrice">
              {{ Math.round((1 - item.groupbuyPrice / item.originalPrice) * 100) }}%off
            </div>
          </div>
          <div class="progress-row" v-if="item.minPeople">
            <van-progress :percentage="item.currentPeople ? Math.round(item.currentPeople / item.minPeople * 100) : 0" color="#FF6B95" :show-pivot="false" stroke-width="6" />
            <span class="progress-text">还差{{ (item.minPeople - (item.currentPeople || 0)) }}人成团</span>
          </div>
          <div class="team-info" v-if="item.leaderName">
            <div class="team-leader">
              <van-image round width="24" height="24" :src="item.leaderAvatar || defaultImg" />
              <span class="leader-name">{{ item.leaderName }}发起</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <van-tabbar v-model="activeTab" route active-color="#FF6B95" fixed>
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/groupbuy" icon="friends-o">拼团</van-tabbar-item>
      <van-tabbar-item to="/cart" icon="shopping-cart-o" :badge="cartCount > 0 ? cartCount : null">购物车</van-tabbar-item>
      <van-tabbar-item to="/user" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
const goBack = () => { window.location.hash = '#/' }
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getGroupBuyActivities } from '@/api/groupbuy'
import { getSkuList } from '@/api/product'

const userStore = useUserStore()
const activeTab = ref(1)
const loading = ref(false)
const cartCount = computed(() => userStore.cartCount)
const products = ref<any[]>([])
const defaultImg = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" fill="%23f0f0f0"><rect width="300" height="300"/><text x="150" y="160" text-anchor="middle" fill="%23ccc" font-size="16">暂无图片</text></svg>'

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getGroupBuyActivities()
    if (res.code !== 200) return
    const activities = res.data?.records || res.data || []
    if (activities.length === 0) return

    // 获取所有上架SKU（补充商品名称/图片）
    const skuRes = await getSkuList({ page: 1, size: 200, status: 1 })
    const allSkus = skuRes.code === 200 ? (skuRes.data?.records || []) : []

    products.value = activities.map((a: any) => {
      const info = allSkus.find((s: any) => s.id === a.skuId) || {}
      return {
        ...a,
        teamId: a.id,
        productName: info.name || a.name,
        productImage: info.image || defaultImg,
        leaderName: '-',
        leaderAvatar: defaultImg,
        currentPeople: 0,
        progress: 0
      }
    })
  } catch { /* ignore */ }
  finally { loading.value = false }
}

const goTeam = (item: any) => {
  window.location.hash = `#/groupbuy/team/${item.teamId || item.id}`
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.groupbuy-page { min-height: 100vh; background: var(--bg-secondary); padding-bottom: 60px; padding-top: 46px; }
.groupbuy-products { padding: 12px; }
.groupbuy-product-item { padding: 12px; background: var(--bg-primary); border-radius: 8px; margin-bottom: 12px; position: relative; overflow: hidden; }
.groupbuy-badge { position: absolute; top: 0; right: 0; background: linear-gradient(135deg, #FF6B95, #FF5580); color: white; padding: 4px 12px; border-radius: 0 8px 0 8px; font-size: 12px; font-weight: 600; z-index: 1; }
.groupbuy-badge.soon { background: linear-gradient(135deg, #E6A23C, #F59E0B); }
.product-image { width: 100%; height: 180px; border-radius: 8px; object-fit: cover; margin-bottom: 12px; }
.product-info { display: flex; flex-direction: column; gap: 8px; }
.product-name { font-size: 14px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price-row { display: flex; align-items: baseline; gap: 12px; }
.groupbuy-price { display: flex; align-items: baseline; }
.price-symbol { font-size: 14px; color: var(--primary-color); }
.price-value { font-size: 24px; font-weight: 600; color: var(--primary-color); }
.original-price { font-size: 12px; color: var(--text-secondary); text-decoration: line-through; }
.discount-tag { margin-left: auto; background: linear-gradient(135deg, #FF6B95, #FF5580); color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
.progress-row { display: flex; align-items: center; gap: 8px; }
.progress-text { font-size: 12px; color: var(--primary-color); white-space: nowrap; }
.team-info { display: flex; justify-content: space-between; align-items: center; padding-top: 8px; border-top: 1px solid var(--border-color); }
.team-leader { display: flex; align-items: center; gap: 8px; }
.leader-name { font-size: 12px; color: var(--text-secondary); }
.loading-center { display: flex; justify-content: center; padding: 40px; }
.empty-tip { text-align: center; padding: 60px 0; color: #999; }
@media screen and (min-width: 768px) {
  .groupbuy-page { padding-bottom: 0; padding-top: 56px; }
  .groupbuy-products { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; padding: 24px 32px; max-width: 1200px; margin: 0 auto; }
  .groupbuy-product-item { margin-bottom: 0; }
  .groupbuy-product-item .product-image { height: 240px; }
}
</style>
