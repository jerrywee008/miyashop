<template>
  <div class="home-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <van-search
        v-model="searchText"
        placeholder="搜索商品"
        shape="round"
        background="transparent"
      />
    </div>

    <!-- 轮播图 -->
    <div class="banner-swiper" v-if="banners.length">
      <van-swipe :autoplay="3000" indicator-color="white">
        <van-swipe-item v-for="(item, index) in banners" :key="index">
          <img :src="item.image" :alt="item.title" class="banner-image" />
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <div class="category-item" v-for="(item, index) in categories" :key="index" @click="goCategory(item)">
        <img :src="item.icon || defaultCategoryIcon" :alt="item.name" class="category-icon" />
        <span class="category-name">{{ item.name }}</span>
      </div>
    </div>

    <!-- 秒杀专区 -->
    <div class="seckill-section" v-if="seckillProducts.length">
      <div class="section-header">
        <div class="section-title"><span class="seckill-icon">⚡</span>限时秒杀</div>
        <div class="section-more" @click="window.location.hash='#/seckill'">更多 ></div>
      </div>
      <div class="seckill-list">
        <div class="seckill-item" v-for="item in seckillProducts" :key="item.id" @click="goProduct(item.productId || item.id)">
          <img :src="item.image || defaultProductImg" :alt="item.name" class="seckill-image" />
          <div class="seckill-price">¥{{ item.seckillPrice || item.price }}</div>
          <div class="seckill-original">¥{{ item.originalPrice }}</div>
        </div>
      </div>
    </div>

    <!-- 团购专区 -->
    <div class="groupbuy-section" v-if="groupbuyProducts.length">
      <div class="section-header">
        <div class="section-title"><span class="groupbuy-icon">👥</span>拼团优惠</div>
        <div class="section-more" @click="window.location.hash='#/groupbuy'">更多 ></div>
      </div>
      <div class="groupbuy-list">
        <div class="groupbuy-item" v-for="item in groupbuyProducts" :key="item.id" @click="goProduct(item.productId || item.id)">
          <div class="groupbuy-badge" v-if="item.currentPeople">{{ item.currentPeople }}人拼团</div>
          <img :src="item.productImage || item.image || defaultProductImg" :alt="item.productName || item.name" class="groupbuy-image" />
          <div class="groupbuy-info">
            <div class="groupbuy-name">{{ item.name || item.productName }}</div>
            <div class="groupbuy-price-row">
              <span class="groupbuy-price">¥{{ item.groupbuyPrice }}</span>
              <span class="groupbuy-original">¥{{ item.originalPrice }}</span>
            </div>
            <div class="groupbuy-progress" v-if="item.minPeople">
              <van-progress :percentage="item.currentPeople ? Math.round(item.currentPeople / item.maxPeople * 100) : 0" color="#FF6B95" :show-pivot="false" />
              <span class="groupbuy-people">{{ item.currentPeople || 0 }}/{{ item.minPeople }}人</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐商品 -->
    <div class="recommend-section">
      <div class="section-header"><div class="section-title">🔥 热门推荐</div></div>
      <div class="product-grid">
        <div class="product-card" v-for="item in recommendProducts" :key="item.id" @click="goProduct(item.id)">
          <img :src="getProductImage(item)" :alt="item.name" class="product-image" />
          <div class="product-info">
            <div class="product-name">{{ item.name }}</div>
            <div class="product-price">¥{{ item.price }}</div>
            <div class="product-sales">已售{{ item.sales || 0 }}件</div>
          </div>
        </div>
      </div>
      <van-loading v-if="recommendLoading" class="loading-center" />
    </div>

    <!-- 底部导航 -->
    <TabBar :tab="0" :badge="cartCount" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'HomePage' })
import { ref, computed, onMounted } from 'vue'
import TabBar from '@/components/TabBar.vue'
import { useUserStore } from '@/store/user'
import { getCategoryList, getProductList, getSkuList } from '@/api/product'
import { getSeckillActivities, getSeckillSkus } from '@/api/seckill'
import { getGroupBuyActivities } from '@/api/groupbuy'
import { getCartCount } from '@/api/cart'

const userStore = useUserStore()
const searchText = ref('')
const cartCount = computed(() => userStore.cartCount)

const defaultProductImg = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" fill="%23f0f0f0"><rect width="200" height="200"/><text x="100" y="110" text-anchor="middle" fill="%23ccc" font-size="14">暂无图片</text></svg>'
const defaultCategoryIcon = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" fill="%23f0f0f0"><circle cx="50" cy="50" r="50"/><text x="50" y="56" text-anchor="middle" fill="%23ccc" font-size="12">分类</text></svg>'

const banners = ref<any[]>([])
const categories = ref<any[]>([])
const seckillProducts = ref<any[]>([])
const groupbuyProducts = ref<any[]>([])
const recommendProducts = ref<any[]>([])
const recommendLoading = ref(false)

const getProductImage = (item: any) => {
  if (item.images) {
    return item.images.split(',')[0]
  }
  return item.image || defaultProductImg
}

const goProduct = (id: number) => {
  if (id) window.location.hash = `#/product/${id}`
}

const goCategory = (item: any) => {
  if (item.id) {
    window.location.hash = `#/category/${item.id}`
  } else if (item.name) {
    window.location.hash = `#/category/${encodeURIComponent(item.name)}`
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 200) {
      categories.value = (res.data || []).slice(0, 8)
    }
  } catch { /* ignore */ }
}

const loadBanners = async () => {
  try {
    const res = await getProductList({ page: 1, size: 3, sort: 'sales_desc' })
    if (res.code === 200) {
      banners.value = (res.data?.records || []).map((p: any) => ({
        image: p.images ? p.images.split(',')[0] : '',
        title: p.name,
        id: p.id
      }))
    }
  } catch { /* ignore */ }
}

const loadSeckill = async () => {
  try {
    const res = await getSeckillActivities()
    if (res.code !== 200) return
    const activities = res.data?.records || res.data || []
    if (activities.length === 0) return

    // 获取所有上架SKU，用于匹配图片
    const skuRes = await getSkuList({ page: 1, size: 200, status: 1 })
    const allSkus = skuRes.code === 200 ? (skuRes.data?.records || []) : []

    // 获取第一个活动下的SKU列表
    const firstActivity = activities[0]
    let activitySkus: any[] = []
    try {
      const skuListRes = await getSeckillSkus(firstActivity.id)
      if (skuListRes.code === 200) {
        activitySkus = skuListRes.data || []
      }
    } catch { /* ignore */ }

    seckillProducts.value = activitySkus.slice(0, 4).map((ss: any) => {
      const info = allSkus.find((s: any) => s.id === ss.skuId) || {}
      return {
        id: ss.skuId,
        productId: info.productId,
        name: info.name || `秒杀商品`,
        image: info.image || defaultProductImg,
        seckillPrice: ss.seckillPrice,
        originalPrice: info.price || 0
      }
    })
  } catch { /* ignore */ }
}

const loadGroupbuy = async () => {
  try {
    const res = await getGroupBuyActivities()
    if (res.code !== 200) return
    const activities = res.data?.records || res.data || []
    if (activities.length === 0) return

    // 获取所有上架SKU，用于匹配图片
    const skuRes = await getSkuList({ page: 1, size: 200, status: 1 })
    const allSkus = skuRes.code === 200 ? (skuRes.data?.records || []) : []

    groupbuyProducts.value = activities.slice(0, 4).map((a: any) => {
      const info = allSkus.find((s: any) => s.id === a.skuId) || {}
      return {
        ...a,
        productName: info.name || a.name,
        image: info.image || defaultProductImg,
        productImage: info.image || defaultProductImg,
        currentPeople: a.currentPeople || 0,
        progress: a.currentPeople && a.minPeople ? Math.round(a.currentPeople / a.minPeople * 100) : 0
      }
    })
  } catch { /* ignore */ }
}

const loadRecommend = async () => {
  recommendLoading.value = true
  try {
    const res = await getProductList({ page: 1, size: 6, sort: 'sales_desc' })
    if (res.code === 200) {
      recommendProducts.value = res.data?.records || []
    }
  } catch { /* ignore */ }
  finally { recommendLoading.value = false }
}

const loadCartCount = async () => {
  try {
    const res = await getCartCount()
    if (res.code === 200) {
      userStore.setCartCount(res.data || 0)
    }
  } catch { /* ignore */ }
}

onMounted(() => {
  loadCategories()
  loadBanners()
  loadSeckill()
  loadGroupbuy()
  loadRecommend()
  loadCartCount()
})
</script>

<style scoped>
/* ... styles unchanged ... */
.home-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 60px;
}

.search-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #FF6B95, #FFB6C1);
  padding: 8px 12px;
}

.banner-swiper { margin-bottom: 12px; }
.banner-image { width: 100%; height: 180px; object-fit: cover; }

.category-nav {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px;
  padding: 16px; background: var(--bg-primary); margin-bottom: 12px;
}
.category-item { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.category-icon { width: 48px; height: 48px; border-radius: 50%; object-fit: cover; }
.category-name { font-size: 12px; color: var(--text-primary); }

.seckill-section, .groupbuy-section, .recommend-section {
  background: var(--bg-primary); padding: 16px; margin-bottom: 12px;
}
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.section-title { font-size: 18px; font-weight: 600; color: var(--text-primary); display: flex; align-items: center; gap: 8px; }
.seckill-icon, .groupbuy-icon { font-size: 20px; }
.section-more { font-size: 14px; color: var(--text-secondary); }
.seckill-list { display: flex; gap: 12px; overflow-x: auto; padding-bottom: 8px; }
.seckill-item { flex-shrink: 0; width: 100px; text-align: center; }
.seckill-image { width: 100px; height: 100px; border-radius: 8px; object-fit: cover; margin-bottom: 8px; }
.seckill-price { font-size: 18px; font-weight: 600; color: var(--primary-color); }
.seckill-original { font-size: 12px; color: var(--text-secondary); text-decoration: line-through; }

.groupbuy-list { display: grid; gap: 12px; }
.groupbuy-item { display: flex; gap: 12px; padding: 12px; background: var(--bg-secondary); border-radius: 8px; position: relative; }
.groupbuy-badge { position: absolute; top: 0; right: 0; background: linear-gradient(135deg, #FF6B95, #FF5580); color: white; padding: 4px 12px; border-radius: 0 8px 0 8px; font-size: 12px; }
.groupbuy-image { width: 100px; height: 100px; border-radius: 8px; object-fit: cover; }
.groupbuy-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.groupbuy-name { font-size: 14px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.groupbuy-price-row { display: flex; align-items: baseline; gap: 8px; }
.groupbuy-price { font-size: 20px; font-weight: 600; color: var(--primary-color); }
.groupbuy-original { font-size: 12px; color: var(--text-secondary); text-decoration: line-through; }
.groupbuy-progress { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
.groupbuy-people { font-size: 12px; color: var(--text-secondary); }

.product-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.product-card { background: var(--bg-secondary); border-radius: 8px; overflow: hidden; }
.product-image { width: 100%; height: 160px; object-fit: cover; }
.product-info { padding: 12px; }
.product-name { font-size: 14px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 8px; }
.product-price { font-size: 18px; font-weight: 600; color: var(--primary-color); margin-bottom: 4px; }
.product-sales { font-size: 12px; color: var(--text-secondary); }
.loading-center { display: flex; justify-content: center; padding: 20px; }

@media screen and (min-width: 768px) {
  .home-page { padding-bottom: 0; }
  .search-bar { max-width: 1200px; margin: 0 auto; border-radius: 0 0 16px 16px; }
  .banner-image { height: 320px; }
  .category-nav { grid-template-columns: repeat(8, 1fr); padding: 24px 32px; gap: 20px; }
  .category-icon { width: 64px; height: 64px; }
  .category-name { font-size: 14px; }
  .seckill-section, .groupbuy-section, .recommend-section { padding: 24px 32px; }
  .section-header { margin-bottom: 20px; }
  .section-title { font-size: 20px; }
  .seckill-item { width: 140px; }
  .seckill-image { width: 140px; height: 140px; }
  .groupbuy-list { grid-template-columns: repeat(2, 1fr); }
  .product-grid { grid-template-columns: repeat(4, 1fr); gap: 16px; }
  .product-image { height: 220px; }
  .product-name { font-size: 15px; }
  .product-price { font-size: 20px; }
}
</style>
