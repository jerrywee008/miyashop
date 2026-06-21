<template>
  <div class="seckill-page">
    <div class="nav-bar"><span @click="goBack">← 返回</span><span class="nav-title">秒杀专区</span><span></span></div>

    <!-- 倒计时 -->
    <div class="seckill-header">
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

    <!-- 秒杀商品列表 -->
    <div class="seckill-products">
      <div class="seckill-product-item" v-for="item in products" :key="item.id" @click="goProduct(item.id)">
        <img :src="item.image" :alt="item.name" class="product-image" />
        <div class="product-info">
          <div class="product-name">{{ item.name }}</div>
          <div class="product-price-row">
            <div class="seckill-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ item.seckillPrice }}</span>
            </div>
            <div class="original-price">¥{{ item.originalPrice }}</div>
          </div>
          <div class="progress-row">
            <van-progress :percentage="item.progress" color="#FF6B95" :show-pivot="false" stroke-width="6" />
            <span class="progress-text">已抢{{ item.soldPercent }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" route active-color="#FF6B95" fixed>
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/seckill" icon="fire-o">秒杀</van-tabbar-item>
      <van-tabbar-item to="/cart" icon="shopping-cart-o" :badge="cartCount > 0 ? cartCount : null">
        购物车
      </van-tabbar-item>
      <van-tabbar-item to="/user" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref(1)
const countdownTime = ref(2 * 60 * 60 * 1000) // 2小时

const cartCount = computed(() => userStore.cartCount)

const products = ref([
  {
    id: 1,
    image: 'https://via.placeholder.com/200/FF6B95/FFFFFF?text=S1',
    name: '优雅碎花连衣裙',
    seckillPrice: 99,
    originalPrice: 299,
    progress: 85,
    soldPercent: 85,
    stock: 15
  },
  {
    id: 2,
    image: 'https://via.placeholder.com/200/FFB6C1/FFFFFF?text=S2',
    name: '时尚纯棉衬衫',
    seckillPrice: 159,
    originalPrice: 399,
    progress: 60,
    soldPercent: 60,
    stock: 40
  },
  {
    id: 3,
    image: 'https://via.placeholder.com/200/FFC0CB/FFFFFF?text=S3',
    name: '简约T恤',
    seckillPrice: 59,
    originalPrice: 199,
    progress: 92,
    soldPercent: 92,
    stock: 8
  },
  {
    id: 4,
    image: 'https://via.placeholder.com/200/FF69B4/FFFFFF?text=S4',
    name: '精致项链',
    seckillPrice: 199,
    originalPrice: 599,
    progress: 45,
    soldPercent: 45,
    stock: 55
  },
  {
    id: 5,
    image: 'https://via.placeholder.com/200/DA70D6/FFFFFF?text=S5',
    name: '时尚手镯',
    seckillPrice: 129,
    originalPrice: 399,
    progress: 70,
    soldPercent: 70,
    stock: 30
  },
  {
    id: 6,
    image: 'https://via.placeholder.com/200/FF6B95/FFFFFF?text=S6',
    name: '口红礼盒',
    seckillPrice: 199,
    originalPrice: 499,
    progress: 55,
    soldPercent: 55,
    stock: 45
  }
])

const goBack = () => { window.location.hash = '#/' }

const goProduct = (id: number) => {
  window.location.hash = '#/product/' + id + '?type=seckill'
}
</script>

<style scoped>
.nav-bar { display: flex; align-items: center; justify-content: space-between; height: 46px; padding: 0 16px; background: #fff; border-bottom: 1px solid #eee; position: sticky; top: 0; z-index: 100; }
.nav-title { font-size: 16px; font-weight: 600; }
.seckill-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 60px;
  padding-top: 46px;
}

.seckill-header {
  background: linear-gradient(135deg, #FF6B95, #FF5580);
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.seckill-time {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
}

.time-label {
  font-size: 14px;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 4px;
}

.time-block {
  display: inline-block;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
}

.time-colon {
  font-size: 16px;
}

.seckill-products {
  padding: 12px;
}

.seckill-product-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: var(--bg-primary);
  border-radius: 8px;
  margin-bottom: 12px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.seckill-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 14px;
  color: var(--primary-color);
}

.price-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-color);
}

.original-price {
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.progress-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-text {
  font-size: 12px;
  color: var(--primary-color);
  white-space: nowrap;
}

/* ========== PC 宽屏 ========== */
@media screen and (min-width: 768px) {
  .seckill-page {
    padding-bottom: 0;
    padding-top: 56px;
  }

  .seckill-header {
    border-radius: 0;
  }

  .seckill-products {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    padding: 24px 32px;
    max-width: 1200px;
    margin: 0 auto;
  }

  .seckill-product-item {
    flex-direction: column;
    padding: 16px;
  }

  .seckill-product-item .product-image {
    width: 100%;
    height: 220px;
    border-radius: 8px;
  }

  .seckill-product-item .product-name {
    font-size: 15px;
    margin-top: 8px;
  }
}
</style>