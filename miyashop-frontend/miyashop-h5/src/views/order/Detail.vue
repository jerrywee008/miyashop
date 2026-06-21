<template>
  <div class="order-detail-page">
    <van-nav-bar title="订单详情" left-arrow @click="goBack" fixed />

    <!-- 订单状态 -->
    <div class="status-section" :class="`status-${order.status}`">
      <div class="status-icon">
        <van-icon
          :name="getStatusIcon(order.status)"
          size="32"
        />
      </div>
      <div class="status-text">{{ getStatusText(order.status) }}</div>
      <div class="status-desc" v-if="order.status === 0">
        请尽快支付，超时订单将自动取消
      </div>
      <div class="status-desc" v-else-if="order.status === 1">
        卖家正在准备发货中...
      </div>
      <div class="status-desc" v-else-if="order.status === 2">
        商品运输中，请耐心等待
      </div>
      <van-count-down
        v-if="order.status === 0 && order.expireTime"
        :time="order.expireTime"
        format="剩余 HH 时 mm 分 ss 秒"
        class="countdown"
      />
    </div>

    <!-- 收货地址 -->
    <div class="address-section">
      <div class="address-detail">
        <van-icon name="location-o" size="18" color="#FF6B95" />
        <div class="address-info">
          <div class="address-user">
            <span>{{ order.receiverName }}</span>
            <span class="address-phone">{{ order.receiverPhone }}</span>
          </div>
          <div class="address-text">{{ order.receiverAddress }}</div>
        </div>
      </div>
    </div>

    <!-- 商品信息 -->
    <div class="goods-section">
      <div class="goods-item" v-for="item in order.orderItems" :key="item.id">
        <img :src="item.skuImage" :alt="item.productName" class="goods-image" @click="goProduct(item.productId)" />
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

    <!-- 订单信息 -->
    <div class="info-section">
      <div class="section-title">订单信息</div>
      <div class="info-item">
        <span class="info-label">订单编号</span>
        <span class="info-value">
          {{ order.orderNo }}
          <van-icon name="records-o" size="14" style="margin-left: 4px" @click="copyOrderNo" />
        </span>
      </div>
      <div class="info-item">
        <span class="info-label">下单时间</span>
        <span class="info-value">{{ order.createdTime }}</span>
      </div>
      <div class="info-item" v-if="order.payTime">
        <span class="info-label">支付时间</span>
        <span class="info-value">{{ order.payTime }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">支付方式</span>
        <span class="info-value">{{ order.payType === 1 ? '微信支付' : order.payType === 2 ? '支付宝' : '未支付' }}</span>
      </div>
      <div class="info-item" v-if="order.remark">
        <span class="info-label">订单备注</span>
        <span class="info-value">{{ order.remark }}</span>
      </div>
    </div>

    <!-- 价格明细 -->
    <div class="price-section">
      <div class="section-title">价格明细</div>
      <div class="price-item">
        <span>商品总额</span>
        <span>¥{{ order.totalAmount?.toFixed(2) }}</span>
      </div>
      <div class="price-item">
        <span>运费</span>
        <span class="free" v-if="!order.freight || order.freight === 0">免运费</span>
        <span v-else>¥{{ order.freight?.toFixed(2) }}</span>
      </div>
      <div class="price-item" v-if="order.discountAmount > 0">
        <span>优惠</span>
        <span class="discount">-¥{{ order.discountAmount?.toFixed(2) }}</span>
      </div>
      <div class="price-item total">
        <span>实付款</span>
        <span class="total-price">¥{{ order.payAmount?.toFixed(2) }}</span>
      </div>
    </div>

    <!-- 物流信息 -->
    <div class="logistics-section" v-if="order.status >= 2 && order.status < 4">
      <div class="section-title">物流信息</div>
      <van-steps direction="vertical" :active="logisticsStep" active-color="#FF6B95">
        <van-step v-for="(log, index) in logistics" :key="index">
          <div>{{ log.text }}</div>
          <div class="log-time">{{ log.time }}</div>
        </van-step>
      </van-steps>
    </div>

    <!-- 底部操作 -->
    <div class="bottom-actions" v-if="actionButtons.length > 0">
      <van-button
        v-for="btn in actionButtons"
        :key="btn.text"
        :type="btn.type"
        :plain="btn.plain"
        round
        size="small"
        @click="btn.action"
        custom-style="margin-left: 8px"
      >
        {{ btn.text }}
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
const goBack = () => { window.location.hash = '#/' }
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showSuccessToast, showConfirmDialog } from 'vant'

const router = useRouter()
const route = useRoute()

const order = ref<any>({
  id: 1,
  orderNo: 'ORD202406030001',
  status: 1,
  totalAmount: 598,
  payAmount: 548,
  discountAmount: 50,
  freight: 0,
  payType: 1,
  receiverName: '小美',
  receiverPhone: '138****0001',
  receiverAddress: '广东省广州市天河区珠江新城街道100号',
  remark: '请尽快发货',
  expireTime: 2 * 60 * 60 * 1000,
  createdTime: '2024-06-03 10:30:00',
  payTime: '2024-06-03 10:31:00',
  orderItems: [
    { id: 1, productId: 1, productName: '优雅碎花连衣裙', skuSpecs: '红色 S', price: 299, quantity: 2, skuImage: 'https://via.placeholder.com/100/FF6B95/FFFFFF' }
  ]
})

const logistics = ref([
  { text: '包裹已签收', time: '2024-06-05 15:30:00' },
  { text: '派送中，快递员正在为您派送', time: '2024-06-05 09:00:00' },
  { text: '已到达【天河区】营业点', time: '2024-06-05 06:00:00' },
  { text: '已从【广州分拣中心】发出', time: '2024-06-04 22:00:00' },
  { text: '卖家已发货，等待揽收', time: '2024-06-04 18:00:00' }
])

const logisticsStep = ref(2)

const getStatusIcon = (status: number) => {
  const map: Record<number, string> = { 0: 'clock-o', 1: 'logistics', 2: 'logistics', 3: 'checked', 4: 'cross', 5: 'refund-o' }
  return map[status] || 'info-o'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '已退款' }
  return map[status] || '未知'
}

const actionButtons = computed(() => {
  const btns: { text: string; type: string; plain?: boolean; action: () => void }[] = []
  switch (order.value.status) {
    case 0:
      btns.push(
        { text: '取消订单', type: 'default', plain: true, action: handleCancel },
        { text: '去支付', type: 'danger', action: () => showToast('跳转支付') }
      )
      break
    case 1:
      btns.push({ text: '提醒发货', type: 'primary', plain: true, action: () => showSuccessToast('已提醒发货') })
      break
    case 2:
      btns.push(
        { text: '查看物流', type: 'default', plain: true, action: () => showToast('查看物流') },
        { text: '确认收货', type: 'danger', action: handleConfirm }
      )
      break
    case 3:
      btns.push(
        { text: '去评价', type: 'primary', action: () => showToast('去评价') },
        { text: '再次购买', type: 'danger', plain: true, action: () => showToast('已加入购物车') }
      )
      break
  }
  return btns
})

const goProduct = (id: number) => {
  window.location.hash = `#/product/${id}`
}

const copyOrderNo = () => {
  // Simplified copy
  try {
    navigator.clipboard.writeText(order.value.orderNo)
    showSuccessToast('已复制')
  } catch {
    showToast('复制失败')
  }
}

const handleCancel = async () => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定取消该订单吗？' })
    order.value.status = 4
    showSuccessToast('订单已取消')
  } catch { /* canceled */ }
}

const handleConfirm = async () => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定已收到商品吗？' })
    order.value.status = 3
    showSuccessToast('已确认收货')
  } catch { /* canceled */ }
}

onMounted(async () => {
  const orderId = route.params.id
  try {
    const res = await fetch(`/api/order/${orderId}`).then(r => r.json())
    if (res.code === 200 && res.data) {
      order.value = res.data
    }
  } catch { /* use mock */ }
})
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-top: 46px;
  padding-bottom: 80px;
}

.status-section {
  background: linear-gradient(135deg, #FF6B95, #FF5580);
  color: white;
  padding: 24px;
  text-align: center;
}

.status-section.status-0 {
  background: linear-gradient(135deg, #E6A23C, #F59E0B);
}

.status-section.status-3 {
  background: linear-gradient(135deg, #67C23A, #85CE61);
}

.status-icon {
  margin-bottom: 8px;
}

.status-text {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.status-desc {
  font-size: 13px;
  opacity: 0.9;
}

.countdown {
  margin-top: 12px;
  font-size: 14px;
  font-weight: 600;
}

/* 地址 */
.address-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.address-detail {
  display: flex;
  gap: 10px;
}

.address-info {
  flex: 1;
}

.address-user {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.address-phone {
  font-weight: 400;
  margin-left: 12px;
  color: var(--text-secondary);
}

.address-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 商品 */
.goods-section {
  background: white;
  padding: 12px 16px;
  margin-bottom: 8px;
}

.goods-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
}

.goods-item:last-child {
  border-bottom: none;
}

.goods-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
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
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.goods-quantity {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 订单信息 & 价格 */
.info-section,
.price-section {
  background: white;
  padding: 12px 16px;
  margin-bottom: 8px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 13px;
}

.info-label {
  color: var(--text-secondary);
}

.info-value {
  color: var(--text-primary);
  display: flex;
  align-items: center;
}

.price-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.price-item.total {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  padding-top: 10px;
  border-top: 1px solid var(--border-color);
}

.price-item .free {
  color: var(--success-color);
}

.price-item .discount {
  color: var(--primary-color);
}

.total-price {
  color: var(--primary-color);
  font-size: 18px;
}

/* 物流 */
.logistics-section {
  background: white;
  padding: 12px 16px;
  margin-bottom: 8px;
}

.log-time {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* 底部操作 */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 10px 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

/* ========== PC 宽屏 ========== */
@media screen and (min-width: 768px) {
  .order-detail-page {
    padding-top: 0;
    padding-bottom: 0 !important;
  }

  .status-section {
    border-radius: 0;
  }

  .address-section,
  .goods-section,
  .info-section,
  .price-section,
  .logistics-section {
    max-width: 800px;
    margin-left: auto;
    margin-right: auto;
  }

  .bottom-actions {
    max-width: 800px;
    left: 50% !important;
    transform: translateX(-50%);
  }
}
</style>
