<template>
  <div class="groupbuy-page">
    <van-nav-bar title="拼团专区" left-arrow @click="goBack" fixed />

    <!-- 拼团商品列表 -->
    <div class="groupbuy-products">
      <div class="groupbuy-product-item" v-for="item in products" :key="item.id" @click="goTeam(item)">
        <div class="groupbuy-badge" :class="{ 'soon': item.progress < 30 }">
          {{ item.currentPeople }}人拼团
        </div>
        <img :src="item.image" :alt="item.name" class="product-image" />
        <div class="product-info">
          <div class="product-name">{{ item.name }}</div>
          <div class="product-price-row">
            <div class="groupbuy-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ item.groupbuyPrice }}</span>
            </div>
            <div class="original-price">¥{{ item.originalPrice }}</div>
            <div class="discount-tag">
              {{ Math.round((1 - item.groupbuyPrice / item.originalPrice) * 100) }}%off
            </div>
          </div>
          <div class="progress-row">
            <van-progress
              :percentage="item.progress"
              color="#FF6B95"
              :show-pivot="false"
              stroke-width="6"
            />
            <span class="progress-text">还差{{ item.minPeople - item.currentPeople }}人成团</span>
          </div>
          <div class="team-info">
            <div class="team-leader">
              <van-image
                round
                width="24"
                height="24"
                :src="item.leaderAvatar"
              />
              <span class="leader-name">{{ item.leaderName }}发起</span>
            </div>
            <div class="team-time">
              <van-count-down :time="item.expireTime" format="HH:mm:ss" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" route active-color="#FF6B95" fixed>
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/groupbuy" icon="friends-o">拼团</van-tabbar-item>
      <van-tabbar-item to="/cart" icon="shopping-cart-o" :badge="cartCount > 0 ? cartCount : null">
        购物车
      </van-tabbar-item>
      <van-tabbar-item to="/user" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
const goBack = () => { window.location.hash = '#/' }
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref(1)

const cartCount = computed(() => userStore.cartCount)

const products = ref([
  {
    id: 1,
    teamId: 'GB001',
    image: 'https://via.placeholder.com/300/FF6B95/FFFFFF?text=G1',
    name: '优雅碎花连衣裙',
    groupbuyPrice: 199,
    originalPrice: 399,
    progress: 66,
    currentPeople: 2,
    minPeople: 3,
    maxPeople: 5,
    leaderAvatar: 'https://via.placeholder.com/50/FF6B95/FFFFFF?text=L1',
    leaderName: '小美',
    expireTime: 5 * 60 * 60 * 1000 // 5小时
  },
  {
    id: 2,
    teamId: 'GB002',
    image: 'https://via.placeholder.com/300/FFB6C1/FFFFFF?text=G2',
    name: '时尚纯棉衬衫',
    groupbuyPrice: 159,
    originalPrice: 299,
    progress: 50,
    currentPeople: 1,
    minPeople: 2,
    maxPeople: 10,
    leaderAvatar: 'https://via.placeholder.com/50/FFB6C1/FFFFFF?text=L2',
    leaderName: '丽丽',
    expireTime: 8 * 60 * 60 * 1000 // 8小时
  },
  {
    id: 3,
    teamId: 'GB003',
    image: 'https://via.placeholder.com/300/FFC0CB/FFFFFF?text=G3',
    name: '精致项链套装',
    groupbuyPrice: 299,
    originalPrice: 599,
    progress: 75,
    currentPeople: 3,
    minPeople: 4,
    maxPeople: 8,
    leaderAvatar: 'https://via.placeholder.com/50/FFC0CB/FFFFFF?text=L3',
    leaderName: '芳芳',
    expireTime: 3 * 60 * 60 * 1000 // 3小时
  },
  {
    id: 4,
    teamId: 'GB004',
    image: 'https://via.placeholder.com/300/FF69B4/FFFFFF?text=G4',
    name: '时尚手提包',
    groupbuyPrice: 399,
    originalPrice: 699,
    progress: 25,
    currentPeople: 1,
    minPeople: 4,
    maxPeople: 10,
    leaderAvatar: 'https://via.placeholder.com/50/FF69B4/FFFFFF?text=L4',
    leaderName: '小红',
    expireTime: 12 * 60 * 60 * 1000 // 12小时
  }
])

const goTeam = (item: any) => {
  window.location.hash = `#/groupbuy/team/${item.teamId}`
}
</script>

<style scoped>
.groupbuy-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 60px;
  padding-top: 46px;
}

.groupbuy-products {
  padding: 12px;
}

.groupbuy-product-item {
  padding: 12px;
  background: var(--bg-primary);
  border-radius: 8px;
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
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
  font-weight: 600;
  z-index: 1;
}

.groupbuy-badge.soon {
  background: linear-gradient(135deg, #E6A23C, #F59E0B);
}

.product-image {
  width: 100%;
  height: 180px;
  border-radius: 8px;
  object-fit: cover;
  margin-bottom: 12px;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
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

.groupbuy-price {
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

.discount-tag {
  margin-left: auto;
  background: linear-gradient(135deg, #FF6B95, #FF5580);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
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

.team-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid var(--border-color);
}

.team-leader {
  display: flex;
  align-items: center;
  gap: 8px;
}

.leader-name {
  font-size: 12px;
  color: var(--text-secondary);
}

.team-time {
  font-size: 12px;
  color: var(--primary-color);
}

/* ========== PC 宽屏 ========== */
@media screen and (min-width: 768px) {
  .groupbuy-page {
    padding-bottom: 0;
    padding-top: 56px;
  }

  .groupbuy-products {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    padding: 24px 32px;
    max-width: 1200px;
    margin: 0 auto;
  }

  .groupbuy-product-item {
    margin-bottom: 0;
  }

  .groupbuy-product-item .product-image {
    height: 240px;
  }
}
</style>