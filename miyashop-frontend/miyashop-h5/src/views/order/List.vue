<template>
  <div class="order-list-page">
    <van-nav-bar title="我的订单" left-arrow @click="goBack" fixed />

    <!-- 订单状态Tab -->
    <van-tabs v-model:active="activeTab" sticky color="#FF6B95" title-active-color="#FF6B95" :offset-top="46">
      <van-tab v-for="tab in tabs" :key="tab.value" :title="tab.label" />
    </van-tabs>

    <!-- 订单列表 -->
    <div class="order-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="— 没有更多订单了 —"
          @load="onLoad"
        >
          <div class="order-card" v-for="order in orderList" :key="order.id" @click="goDetail(order)">
            <!-- 订单头部 -->
            <div class="order-header">
              <span class="order-no">{{ order.orderNo }}</span>
              <van-tag :type="getStatusType(order.status)" size="medium">
                {{ getStatusText(order.status) }}
              </van-tag>
            </div>

            <!-- 订单商品 -->
            <div class="order-goods">
              <div class="goods-item" v-for="item in (order.orderItems || [])" :key="item.id">
                <img :src="item.skuImage" :alt="item.productName" class="goods-image" />
                <div class="goods-info">
                  <div class="goods-name">{{ item.productName }}</div>
                  <div class="goods-specs" v-if="item.skuSpecs">{{ item.skuSpecs }}</div>
                  <div class="goods-price-row">
                    <span class="goods-price">¥{{ item.price }}</span>
                    <span class="goods-quantity">x{{ item.quantity }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 订单总额 -->
            <div class="order-total">
              <span>共{{ totalItems(order) }}件</span>
              <span class="total-label">合计：</span>
              <span class="total-amount">¥{{ order.payAmount || order.totalAmount }}</span>
            </div>

            <!-- 订单操作 -->
            <div class="order-actions" v-if="getActions(order).length > 0">
              <van-button
                v-for="action in getActions(order)"
                :key="action.text"
                size="small"
                :type="action.type"
                :plain="action.plain"
                round
                @click.stop="handleAction(order, action.key)"
                custom-style="min-width: 72px"
              >
                {{ action.text }}
              </van-button>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="tabbarActive" route active-color="#FF6B95" fixed>
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/seckill" icon="fire-o">秒杀</van-tabbar-item>
      <van-tabbar-item to="/cart" icon="shopping-cart-o">购物车</van-tabbar-item>
      <van-tabbar-item to="/user" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
const goBack = () => { window.location.hash = '#/' }
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showSuccessToast } from 'vant'
import { getOrderList, cancelOrder, confirmOrder } from '@/api/order'

const router = useRouter()

const activeTab = ref(0)
const tabbarActive = ref(3)

const tabs = [
  { label: '全部', value: -1 },
  { label: '待支付', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 }
]

const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const orderList = ref<any[]>([])
let page = 1

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: '', 3: 'success', 4: 'danger', 5: 'danger' }
  return map[status] || ''
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '已退款' }
  return map[status] || '未知'
}

const totalItems = (order: any) => {
  return (order.orderItems || []).reduce((sum: number, i: any) => sum + i.quantity, 0)
}

const getActions = (order: any) => {
  const actions: { key: string; text: string; type: string; plain?: boolean }[] = []
  switch (order.status) {
    case 0: // 待支付
      actions.push({ key: 'cancel', text: '取消订单', type: 'default', plain: true })
      actions.push({ key: 'pay', text: '去支付', type: 'danger' })
      break
    case 1: // 待发货
      actions.push({ key: 'remind', text: '提醒发货', type: 'default', plain: true })
      break
    case 2: // 待收货
      actions.push({ key: 'logistics', text: '查看物流', type: 'default', plain: true })
      actions.push({ key: 'confirm', text: '确认收货', type: 'danger' })
      break
    case 3: // 已完成
      actions.push({ key: 'review', text: '去评价', type: 'primary' })
      actions.push({ key: 'rebuy', text: '再次购买', type: 'danger', plain: true })
      break
  }
  return actions
}

const goDetail = (order: any) => {
  window.location.hash = `#/order/${order.id}`
}

const handleAction = async (order: any, action: string) => {
  switch (action) {
    case 'cancel':
      try {
        await showConfirmDialog({ title: '提示', message: '确定取消该订单吗？' })
        await cancelOrder(order.id)
        order.status = 4
        showSuccessToast('订单已取消')
      } catch { /* canceled */ }
      break
    case 'pay':
      showToast('跳转支付...')
      break
    case 'remind':
      showSuccessToast('已提醒卖家发货')
      break
    case 'confirm':
      try {
        await showConfirmDialog({ title: '提示', message: '确定已收到商品吗？' })
        await confirmOrder(order.id)
        order.status = 3
        showSuccessToast('已确认收货')
      } catch { /* canceled */ }
      break
    case 'logistics':
      showToast('查看物流信息')
      break
    case 'review':
      window.location.hash = `#/order/${order.id}?action=review`
      break
    case 'rebuy':
      showToast('已加入购物车')
      break
  }
}

// ---------- 加载数据 ----------
const fetchOrderList = async () => {
  try {
    const params: any = { page, size: 10 }
    if (activeTab.value >= 0) {
      params.status = activeTab.value
    }
    const res: any = await getOrderList(params)
    if (res?.code === 200 && res.data) {
      const records = res.data.records || res.data
      if (page === 1) {
        orderList.value = records
      } else {
        orderList.value = [...orderList.value, ...records]
      }
      finished.value = !records || records.length < 10
      page++
    } else {
      finished.value = true
    }
  } catch {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onLoad = () => {
  loading.value = true
  fetchOrderList()
}

const onRefresh = () => {
  page = 1
  finished.value = false
  refreshing.value = true
  fetchOrderList()
}

// Watch tab change to reload
watch(activeTab, () => {
  page = 1
  finished.value = false
  orderList.value = []
  onLoad()
})

onMounted(() => {
  onLoad()
})
</script>

<style scoped>
.order-list-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 60px;
}

.order-list {
  padding: 12px;
}

.order-card {
  background: white;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 12px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.order-no {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.order-goods {
  padding: 12px 0;
}

.goods-item {
  display: flex;
  gap: 10px;
  padding: 4px 0;
}

.goods-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}

.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.goods-name {
  font-size: 14px;
  color: var(--text-primary);
}

.goods-specs {
  font-size: 12px;
  color: var(--text-secondary);
}

.goods-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goods-price {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.goods-quantity {
  font-size: 13px;
  color: var(--text-secondary);
}

.order-total {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 6px;
  padding: 10px 0;
  border-top: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--text-secondary);
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: var(--primary-color);
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 4px;
}

/* ========== PC 宽屏 ========== */
@media screen and (min-width: 768px) {
  .order-list-page {
    padding-top: 0;
    padding-bottom: 0 !important;
  }

  .order-list {
    max-width: 800px;
    margin: 0 auto;
  }
}
</style>
