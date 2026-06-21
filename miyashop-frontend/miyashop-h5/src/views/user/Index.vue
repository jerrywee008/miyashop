<template>
  <div class="user-page">
    <!-- 用户头部 -->
    <div class="user-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="user-avatar" @click="goLogin" v-if="!isLoggedIn">
          <van-icon name="user-o" size="40" color="#FF6B95" />
        </div>
        <div class="user-avatar" v-else @click="window.location.hash = '#/user/profile'">
          <img :src="userInfo?.avatar || defaultAvatar" />
        </div>
        <div class="user-info" v-if="isLoggedIn">
          <div class="user-name" @click="window.location.hash = '#/user/profile'">{{ userInfo?.nickname || '用户' }}</div>
          <div class="user-level">
            <van-tag type="danger" size="mini" round>Lv.{{ userInfo?.level || 1 }}</van-tag>
          </div>
        </div>
        <div class="user-info" v-else @click="goLogin">
          <div class="user-name">登录/注册</div>
          <div class="user-hint">登录享受更多优惠</div>
        </div>
        <div class="header-right" v-if="isLoggedIn">
          <van-icon name="setting-o" size="20" color="white" @click="goSettings" />
        </div>
      </div>
    </div>

    <!-- 资产信息 -->
    <div class="asset-section" v-if="isLoggedIn">
      <div class="asset-item" @click="goOrderList(0)">
        <div class="asset-value">{{ userInfo?.points || 0 }}</div>
        <div class="asset-label">积分</div>
      </div>
      <div class="asset-item" @click="showBalance">
        <div class="asset-value">¥{{ userInfo?.balance || 0 }}</div>
        <div class="asset-label">余额</div>
      </div>
      <div class="asset-item" @click="goCoupons">
        <div class="asset-value">{{ couponCount }}</div>
        <div class="asset-label">优惠券</div>
      </div>
      <div class="asset-item" @click="goFavorites">
        <div class="asset-value">{{ favoriteCount }}</div>
        <div class="asset-label">收藏</div>
      </div>
    </div>

    <!-- 订单 -->
    <div class="order-section">
      <div class="section-header" @click="window.location.hash = '#/order/list'">
        <span class="section-title">我的订单</span>
        <span class="section-more">查看全部 <van-icon name="arrow" /></span>
      </div>
      <van-grid :column-num="5" :border="false">
        <van-grid-item
          v-for="item in orderNav"
          :key="item.key"
          :icon="item.icon"
          :text="item.text"
          :badge="item.badge"
          @click="goOrderList(item.key)"
        />
      </van-grid>
    </div>

    <!-- 常用功能 -->
    <div class="menu-section">
      <van-cell-group inset>
        <van-cell title="收货地址" icon="location-o" is-link to="/user/address" />
        <van-cell title="我的优惠券" icon="coupon-o" is-link :value="`${couponCount}张`" @click="goCoupons" />
        <van-cell title="我的收藏" icon="star-o" is-link :value="`${favoriteCount}件`" @click="goFavorites" />
        <van-cell title="浏览记录" icon="browsing-history-o" is-link to="/user/history" />
      </van-cell-group>
    </div>

    <!-- 客服 & 帮助 -->
    <div class="menu-section">
      <van-cell-group inset>
        <van-cell title="客服中心" icon="service-o" is-link @click="showToast('客服中心')" />
        <van-cell title="帮助中心" icon="question-o" is-link @click="showToast('帮助中心')" />
        <van-cell title="关于我们" icon="info-o" is-link @click="showToast('关于MiyaShop')" />
      </van-cell-group>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section" v-if="isLoggedIn">
      <van-button
        type="default"
        block
        round
        @click="handleLogout"
        custom-style="color: #F56C6C; border-color: #F56C6C"
      >
        退出登录
      </van-button>
    </div>

    <!-- 底部导航 -->
    <TabBar :tab="3" />
  </div>
</template>

<script setup lang="ts">
import TabBar from '@/components/TabBar.vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showSuccessToast } from 'vant'
import { useUserStore } from '@/store/user'
import { getCouponList, getFavoriteList } from '@/api/member'
import { getOrderList } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref(3)
const cartCount = computed(() => userStore.cartCount)
const isLoggedIn = computed(() => !!userStore.token)
const userInfo = computed(() => userStore.userInfo)

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const couponCount = ref(0)
const favoriteCount = ref(0)

const orderNav = ref([
  { key: 0, icon: 'balance-pay', text: '待支付', badge: 0 },
  { key: 1, icon: 'logistics', text: '待发货', badge: 0 },
  { key: 2, icon: 'send-gift-o', text: '待收货', badge: 0 },
  { key: 3, icon: 'comment-o', text: '待评价', badge: 0 },
  { key: -1, icon: 'after-sale', text: '售后', badge: 0 }
])

const goLogin = () => {
  window.location.hash = '#/login'
}

const goOrderList = (status?: number) => {
  if (status !== undefined && status >= 0) {
    window.location.hash = '#/order/list?status=' + status
  } else {
    window.location.hash = '#/order/list'
  }
}

const goCoupons = () => window.location.hash = '#/user/coupons'
const goFavorites = () => window.location.hash = '#/user/favorites'
const showBalance = () => window.location.hash = '#/user/balance'
const goSettings = () => window.location.hash = '#/user/settings'

const handleLogout = async () => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定退出登录吗？' })
    userStore.logout()
    showSuccessToast('已退出登录')
  } catch { /* canceled */ }
}

const fetchCounts = async () => {
  // 获取优惠券数量
  try {
    const couponRes: any = await getCouponList({ status: 0 })
    if (couponRes?.code === 200 && couponRes.data) {
      couponCount.value = (couponRes.data.records || couponRes.data).length
    }
  } catch { /* ignore */ }

  // 获取收藏数量
  try {
    const favRes: any = await getFavoriteList()
    if (favRes?.code === 200 && favRes.data) {
      favoriteCount.value = (favRes.data.records || favRes.data).length
    }
  } catch { /* ignore */ }

  // 获取各状态订单数量
  if (isLoggedIn.value) {
    const statuses = [0, 1, 2, 3]
    for (const status of statuses) {
      try {
        const orderRes: any = await getOrderList({ page: 1, size: 1, status })
        if (orderRes?.code === 200 && orderRes.data) {
          const total = orderRes.data.total ?? 0
          const nav = orderNav.value.find(n => n.key === status)
          if (nav) nav.badge = total || 0
        }
      } catch { /* ignore */ }
    }
  }
}

onMounted(() => {
  fetchCounts()
})
</script>

<style scoped>
.user-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 60px;
}

.user-header {
  position: relative;
  height: 180px;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 140px;
  background: linear-gradient(135deg, #FF6B95, #FFB6C1);
  border-radius: 0 0 50% 50% / 0 0 30px 30px;
}

.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  padding: 40px 20px 0;
  gap: 16px;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid white;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: white;
  margin-bottom: 6px;
}

.user-hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.user-level {
  margin-top: 4px;
}

.header-right {
  padding: 8px;
}

/* 资产 */
.asset-section {
  display: flex;
  background: white;
  margin: -30px 16px 12px;
  border-radius: 12px;
  padding: 16px 0;
  box-shadow: 0 4px 12px rgba(255, 107, 149, 0.1);
  position: relative;
  z-index: 2;
}

.asset-item {
  flex: 1;
  text-align: center;
  border-right: 1px solid var(--border-color);
}

.asset-item:last-child {
  border-right: none;
}

.asset-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.asset-label {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 订单 */
.order-section {
  background: white;
  margin: 12px;
  border-radius: 12px;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.section-more {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
}

/* 功能菜单 */
.menu-section {
  margin: 12px;
}

.logout-section {
  padding: 20px 16px;
}
</style>
