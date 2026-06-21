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
    <div class="banner-swiper">
      <van-swipe :autoplay="3000" indicator-color="white">
        <van-swipe-item v-for="(item, index) in banners" :key="index">
          <img :src="item.image" :alt="item.title" class="banner-image" />
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <div class="category-item" v-for="(item, index) in categories" :key="index" @click="goCategory(item)">
        <img :src="item.icon" :alt="item.name" class="category-icon" />
        <span class="category-name">{{ item.name }}</span>
      </div>
    </div>

    <!-- 秒杀专区 -->
    <div class="seckill-section">
      <div class="section-header">
        <div class="section-title">
          <span class="seckill-icon">⚡</span>
          限时秒杀
        </div>
        <div class="countdown">
          <van-count-down :time="countdownTime" format="HH:mm:ss" />
        </div>
        <div class="section-more" @click="window.location.hash='#/seckill'">更多 ></div>
      </div>
      <div class="seckill-list">
        <div class="seckill-item" v-for="item in seckillProducts" :key="item.id" @click="goProduct(item.id)">
          <img :src="item.image" :alt="item.name" class="seckill-image" />
          <div class="seckill-price">¥{{ item.seckillPrice }}</div>
          <div class="seckill-original">¥{{ item.originalPrice }}</div>
        </div>
      </div>
    </div>

    <!-- 团购专区 -->
    <div class="groupbuy-section">
      <div class="section-header">
        <div class="section-title">
          <span class="groupbuy-icon">👥</span>
          拼团优惠
        </div>
        <div class="section-more" @click="window.location.hash='#/groupbuy'">更多 ></div>
      </div>
      <div class="groupbuy-list">
        <div class="groupbuy-item" v-for="item in groupbuyProducts" :key="item.id" @click="goProduct(item.id)">
          <div class="groupbuy-badge">{{ item.currentPeople }}人拼团</div>
          <img :src="item.image" :alt="item.name" class="groupbuy-image" />
          <div class="groupbuy-info">
            <div class="groupbuy-name">{{ item.name }}</div>
            <div class="groupbuy-price-row">
              <span class="groupbuy-price">¥{{ item.groupbuyPrice }}</span>
              <span class="groupbuy-original">¥{{ item.originalPrice }}</span>
            </div>
            <div class="groupbuy-progress">
              <van-progress :percentage="item.progress" color="#FF6B95" :show-pivot="false" />
              <span class="groupbuy-people">{{ item.currentPeople }}/{{ item.minPeople }}人</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐商品 -->
    <div class="recommend-section">
      <div class="section-header">
        <div class="section-title">🔥 热门推荐</div>
      </div>
      <div class="product-grid">
        <div class="product-card" v-for="item in recommendProducts" :key="item.id" @click="goProduct(item.id)">
          <img :src="item.image" :alt="item.name" class="product-image" />
          <div class="product-info">
            <div class="product-name">{{ item.name }}</div>
            <div class="product-price">¥{{ item.price }}</div>
            <div class="product-sales">已售{{ item.sales }}件</div>
          </div>
        </div>
      </div>
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

const userStore = useUserStore()

const searchText = ref('')
const countdownTime = ref(2 * 60 * 60 * 1000) // 2小时

const cartCount = computed(() => userStore.cartCount)

const banners = ref([
  { image: 'https://via.placeholder.com/750x350/FF6B95/FFFFFF?text=Banner+1', title: '夏季新品' },
  { image: 'https://via.placeholder.com/750x350/FFB6C1/FFFFFF?text=Banner+2', title: '限时优惠' },
  { image: 'https://via.placeholder.com/750x350/FFC0CB/FFFFFF?text=Banner+3', title: '品牌特惠' }
])

const categories = ref([
  { name: '连衣裙', icon: 'https://via.placeholder.com/100/FF6B95/FFFFFF?text=1' },
  { name: '衬衫', icon: 'https://via.placeholder.com/100/FFB6C1/FFFFFF?text=2' },
  { name: 'T恤', icon: 'https://via.placeholder.com/100/FFC0CB/FFFFFF?text=3' },
  { name: '裤子', icon: 'https://via.placeholder.com/100/FF69B4/FFFFFF?text=4' },
  { name: '首饰', icon: 'https://via.placeholder.com/100/DA70D6/FFFFFF?text=5' },
  { name: '彩妆', icon: 'https://via.placeholder.com/100/FF6B95/FFFFFF?text=6' },
  { name: '香水', icon: 'https://via.placeholder.com/100/FFB6C1/FFFFFF?text=7' },
  { name: '更多', icon: 'https://via.placeholder.com/100/FFC0CB/FFFFFF?text=8' }
])

const seckillProducts = ref([
  { id: 1, image: 'https://via.placeholder.com/200/FF6B95/FFFFFF?text=S1', seckillPrice: 99, originalPrice: 299 },
  { id: 2, image: 'https://via.placeholder.com/200/FFB6C1/FFFFFF?text=S2', seckillPrice: 159, originalPrice: 399 },
  { id: 3, image: 'https://via.placeholder.com/200/FFC0CB/FFFFFF?text=S3', seckillPrice: 199, originalPrice: 499 }
])

const groupbuyProducts = ref([
  { id: 3, image: 'https://via.placeholder.com/300x300/FF6B95/FFFFFF?text=G1', name: '优雅连衣裙', groupbuyPrice: 199, originalPrice: 399, currentPeople: 2, minPeople: 3, progress: 66 },
  { id: 4, image: 'https://via.placeholder.com/300x300/FFB6C1/FFFFFF?text=G2', name: '时尚衬衫', groupbuyPrice: 159, originalPrice: 299, currentPeople: 1, minPeople: 2, progress: 50 }
])

const recommendProducts = ref([
  { id: 1, image: 'https://via.placeholder.com/300x300/FF6B95/FFFFFF?text=P1', name: '优雅碎花连衣裙', price: 299, sales: 56 },
  { id: 2, image: 'https://via.placeholder.com/300x300/FFB6C1/FFFFFF?text=P2', name: '时尚纯棉衬衫', price: 199, sales: 123 },
  { id: 3, image: 'https://via.placeholder.com/300x300/FFC0CB/FFFFFF?text=P3', name: '简约T恤', price: 99, sales: 234 },
  { id: 4, image: 'https://via.placeholder.com/300x300/FF69B4/FFFFFF?text=P4', name: '精致项链', price: 159, sales: 89 },
  { id: 5, image: 'https://via.placeholder.com/300x300/DA70D6/FFFFFF?text=P5', name: '时尚手镯', price: 199, sales: 67 },
  { id: 6, image: 'https://via.placeholder.com/300x300/FF6B95/FFFFFF?text=P6', name: '口红礼盒', price: 299, sales: 145 }
])

const goProduct = (id: number) => {
  window.location.hash = `#/product/${id}`
}

const goCategory = (item: any) => {
  window.location.hash = `#/category/${encodeURIComponent(item.name)}`
}

onMounted(() => {
  // TODO: 加载购物车数量
  userStore.setCartCount(3)
})
</script>

<style scoped>
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

.banner-swiper {
  margin-bottom: 12px;
}

.banner-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.category-nav {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 16px;
  background: var(--bg-primary);
  margin-bottom: 12px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.category-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.category-name {
  font-size: 12px;
  color: var(--text-primary);
}

.seckill-section,
.groupbuy-section,
.recommend-section {
  background: var(--bg-primary);
  padding: 16px;
  margin-bottom: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.seckill-icon,
.groupbuy-icon {
  font-size: 20px;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: var(--primary-color);
  font-weight: 600;
}

.section-more {
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
}

.seckill-list {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.seckill-item {
  flex-shrink: 0;
  width: 100px;
  text-align: center;
}

.seckill-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  margin-bottom: 8px;
}

.seckill-price {
  font-size: 18px;
  font-weight: 600;
  color: var(--primary-color);
}

.seckill-original {
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.groupbuy-list {
  display: grid;
  gap: 12px;
}

.groupbuy-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
  position: relative;
}

.groupbuy-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, #FF6B95, #FF5580);
  color: white;
  padding: 4px 12px;
  border-radius: 0 8px 0 8px;
  font-size: 12px;
}

.groupbuy-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
}

.groupbuy-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.groupbuy-name {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.groupbuy-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.groupbuy-price {
  font-size: 20px;
  font-weight: 600;
  color: var(--primary-color);
}

.groupbuy-original {
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.groupbuy-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.groupbuy-people {
  font-size: 12px;
  color: var(--text-secondary);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.product-card {
  background: var(--bg-secondary);
  border-radius: 8px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.product-sales {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ========== PC / Mac 宽屏适配 ========== */
@media screen and (min-width: 768px) {
  .home-page {
    padding-bottom: 0;
  }

  .search-bar {
    max-width: 1200px;
    margin: 0 auto;
    border-radius: 0 0 16px 16px;
  }

  .banner-image {
    height: 320px;
  }

  .category-nav {
    grid-template-columns: repeat(8, 1fr);
    padding: 24px 32px;
    gap: 20px;
  }

  .category-icon {
    width: 64px;
    height: 64px;
  }

  .category-name {
    font-size: 14px;
  }

  .seckill-section,
  .groupbuy-section,
  .recommend-section {
    padding: 24px 32px;
    margin-bottom: 12px;
  }

  .section-header {
    margin-bottom: 20px;
  }

  .section-title {
    font-size: 20px;
  }

  .seckill-item {
    width: 140px;
  }

  .seckill-image {
    width: 140px;
    height: 140px;
  }

  .groupbuy-list {
    grid-template-columns: repeat(2, 1fr);
  }

  .product-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
  }

  .product-image {
    height: 220px;
  }

  .product-name {
    font-size: 15px;
  }

  .product-price {
    font-size: 20px;
  }
}
</style>