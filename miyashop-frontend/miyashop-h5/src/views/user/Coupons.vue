<template>
  <div class="coupons-page">
    <van-nav-bar title="我的优惠券" left-arrow @click-left="$router.back()" fixed />

    <van-tabs v-model:active="activeTab" sticky color="#FF6B95" title-active-color="#FF6B95" :offset-top="46">
      <van-tab v-for="tab in tabs" :key="tab.key" :title="tab.title">
        <div class="coupon-list">
          <van-empty v-if="filteredCoupons.length === 0" description="暂无优惠券" />
          <div
            v-for="coupon in filteredCoupons"
            :key="coupon.id"
            class="coupon-card"
            :class="{ used: coupon.status === 1, expired: coupon.status === 2 }"
          >
            <div class="coupon-left">
              <div class="coupon-value">
                <template v-if="coupon.type === 1">
                  <span class="amount-symbol">¥</span>
                  <span class="amount-value">{{ coupon.amount }}</span>
                </template>
                <template v-else>
                  <span class="amount-value">{{ coupon.discount }}</span>
                  <span class="amount-symbol">折</span>
                </template>
              </div>
              <div class="coupon-condition">满{{ coupon.minAmount }}元可用</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ coupon.name }}</div>
              <div class="coupon-scope">{{ coupon.scope || '全场通用' }}</div>
              <div class="coupon-time">{{ coupon.startTime }} - {{ coupon.endTime }}</div>
              <div class="coupon-status" v-if="coupon.status === 1">已使用</div>
              <div class="coupon-status" v-if="coupon.status === 2">已过期</div>
              <van-button
                v-if="coupon.status === 0"
                size="small"
                type="danger"
                round
                plain
                @click="useCoupon(coupon)"
                custom-style="border-color: #FF6B95; color: #FF6B95"
              >
                去使用
              </van-button>
            </div>
          </div>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()

const activeTab = ref(0)
const tabs = [
  { key: 0, title: '未使用' },
  { key: 1, title: '已使用' },
  { key: 2, title: '已过期' }
]

const coupons = ref<any[]>([])
const filteredCoupons = computed(() => {
  if (activeTab.value === 0) return coupons.value.filter(c => c.status === 0)
  return coupons.value.filter(c => c.status === activeTab.value)
})

const useCoupon = (coupon: any) => {
  router.push('/')
  showToast(`满${coupon.minAmount}元可用`)
}

onMounted(() => {
  // Mock data
  coupons.value = [
    {
      id: 1, name: '新人专享券', type: 1, amount: 20, minAmount: 200,
      scope: '全场通用', startTime: '2024-06-01', endTime: '2024-12-31', status: 0
    },
    {
      id: 2, name: '会员日满减券', type: 1, amount: 50, minAmount: 500,
      scope: '女装、首饰可用', startTime: '2024-06-15', endTime: '2024-07-15', status: 0
    },
    {
      id: 3, name: '618折扣券', type: 2, discount: 85, minAmount: 300,
      scope: '全场通用', startTime: '2024-06-18', endTime: '2024-06-25', status: 0
    },
    {
      id: 4, name: '满200减30', type: 1, amount: 30, minAmount: 200,
      scope: '彩妆专场', startTime: '2024-05-01', endTime: '2024-05-31', status: 1
    },
    {
      id: 5, name: '限时满减券', type: 1, amount: 100, minAmount: 800,
      scope: '全场通用', startTime: '2024-03-01', endTime: '2024-04-30', status: 2
    }
  ]
})
</script>

<style scoped>
.coupons-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 20px;
}

.coupon-list {
  padding: 12px 16px;
}

.coupon-card {
  display: flex;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 14px;
  background: linear-gradient(135deg, #FFF5F7, #FFF0F3);
  box-shadow: 0 2px 8px rgba(255, 107, 149, 0.1);
}

.coupon-card.used,
.coupon-card.expired {
  filter: grayscale(0.6);
  opacity: 0.7;
}

.coupon-left {
  width: 110px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 8px;
  border-right: 1px dashed #FFD5E0;
  flex-shrink: 0;
}

.amount-symbol {
  font-size: 16px;
  color: var(--primary-color);
}

.amount-value {
  font-size: 36px;
  font-weight: 800;
  color: var(--primary-color);
  line-height: 1;
}

.coupon-condition {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 6px;
}

.coupon-right {
  flex: 1;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coupon-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.coupon-scope {
  font-size: 12px;
  color: var(--text-secondary);
}

.coupon-time {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.coupon-status {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>
