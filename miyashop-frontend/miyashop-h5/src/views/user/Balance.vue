<template>
  <div class="balance-page">
    <van-nav-bar title="我的余额" left-arrow @click="goBack" fixed />

    <!-- 余额卡片 -->
    <div class="balance-card">
      <div class="balance-label">可用余额 (元)</div>
      <div class="balance-amount">{{ userInfo?.balance || 0 }}</div>
      <div class="balance-actions">
        <van-button size="small" type="danger" round plain @click="showToast('充值')">充值</van-button>
        <van-button size="small" type="default" round plain @click="showToast('提现')">提现</van-button>
      </div>
    </div>

    <!-- 积分 -->
    <div class="points-card">
      <div class="points-header">
        <span class="points-label">我的积分</span>
        <span class="points-value">{{ userInfo?.points || 0 }}</span>
      </div>
      <div class="points-hint">积分可用于兑换优惠券和礼品</div>
    </div>

    <!-- 交易记录 -->
    <div class="records-section">
      <div class="section-title">余额变动记录</div>
      <van-cell-group inset>
        <van-cell
          v-for="record in records"
          :key="record.id"
          :title="record.desc"
          :label="record.time"
        >
          <template #value>
            <span :class="record.type === 'income' ? 'amount-in' : 'amount-out'">
              {{ record.type === 'income' ? '+' : '-' }}¥{{ record.amount }}
            </span>
          </template>
        </van-cell>
      </van-cell-group>
      <van-empty v-if="records.length === 0" description="暂无交易记录" />
    </div>
  </div>
</template>

<script setup lang="ts">
const goBack = () => { window.location.hash = '#/' }
import { ref, computed } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const records = ref<any[]>([])
</script>

<style scoped>
.balance-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-top: 46px;
}

.balance-card {
  background: linear-gradient(135deg, #FF6B95, #FF5580);
  margin: 16px;
  border-radius: 16px;
  padding: 28px 24px;
  color: white;
  text-align: center;
}

.balance-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 12px;
}

.balance-amount {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 20px;
}

.balance-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.points-card {
  background: white;
  margin: 0 16px 12px;
  border-radius: 12px;
  padding: 20px;
}

.points-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.points-label {
  font-size: 15px;
  color: var(--text-primary);
}

.points-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--gold-color, #C9A96E);
}

.points-hint {
  font-size: 12px;
  color: var(--text-secondary);
}

.records-section {
  margin: 16px 0;
}

.section-title {
  font-size: 13px;
  color: var(--text-secondary);
  padding: 0 16px 8px;
}

.amount-in {
  color: var(--success-color);
  font-weight: 600;
}

.amount-out {
  color: var(--text-primary);
}
</style>
