<template>
  <div class="checkout-page">
    <van-nav-bar title="确认订单" left-arrow @click="goBack" fixed />

    <!-- 收货地址 -->
    <div class="address-section" @click="goSelectAddress">
      <div class="address-card" v-if="selectedAddress">
        <div class="address-header">
          <span class="address-name">{{ selectedAddress.name }}</span>
          <span class="address-phone">{{ selectedAddress.phone }}</span>
          <van-tag v-if="selectedAddress.isDefault" type="danger" size="mini" round>默认</van-tag>
        </div>
        <div class="address-detail">
          <van-icon name="location-o" />
          {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }} {{ selectedAddress.detail }}
        </div>
      </div>
      <div class="no-address" v-else>
        <span>请添加收货地址</span>
        <van-icon name="arrow" />
      </div>
      <div class="address-arrow">
        <van-icon name="arrow" />
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="goods-section">
      <div class="goods-item" v-for="item in orderItems" :key="item.skuId">
        <img :src="item.image" :alt="item.name" class="goods-image" />
        <div class="goods-info">
          <div class="goods-name">{{ item.name }}</div>
          <div class="goods-specs" v-if="item.specs">{{ item.specs }}</div>
          <div class="goods-price-row">
            <span class="goods-price">¥{{ item.price }}</span>
            <span class="goods-original" v-if="item.originalPrice && item.originalPrice > item.price">
              ¥{{ item.originalPrice }}
            </span>
            <span class="goods-quantity">x{{ item.quantity }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 优惠券 -->
    <div class="coupon-section">
      <div class="section-item" @click="showCouponSheet = true">
        <span class="item-label">优惠券</span>
        <span class="item-value">
          <span v-if="selectedCoupon" class="coupon-selected">
            -¥{{ selectedCoupon.amount }}
          </span>
          <span v-else class="item-hint">{{ availableCoupons.length }}张可用</span>
          <van-icon name="arrow" />
        </span>
      </div>
    </div>

    <!-- 订单信息 -->
    <div class="order-info-section">
      <div class="section-item">
        <span class="item-label">商品总额</span>
        <span class="item-value">¥{{ goodsTotal.toFixed(2) }}</span>
      </div>
      <div class="section-item">
        <span class="item-label">运费</span>
        <span class="item-value" :class="{ free: freight === 0 }">
          {{ freight === 0 ? '免运费' : `¥${freight.toFixed(2)}` }}
        </span>
      </div>
      <div class="section-item" v-if="couponDiscount > 0">
        <span class="item-label">优惠券</span>
        <span class="item-value discount">-¥{{ couponDiscount.toFixed(2) }}</span>
      </div>
      <div class="section-item" v-if="promotionDiscount > 0">
        <span class="item-label">满减</span>
        <span class="item-value discount">-¥{{ promotionDiscount.toFixed(2) }}</span>
      </div>
    </div>

    <!-- 支付方式 -->
    <div class="payment-section">
      <div class="section-title">支付方式</div>
      <van-radio-group v-model="payType">
        <div class="payment-item" :class="{ active: payType === 1 }">
          <van-radio :name="1" checked-color="#FF6B95">
            <template #icon>
              <span class="pay-icon wx">微信</span>
            </template>
            微信支付
          </van-radio>
        </div>
        <div class="payment-item" :class="{ active: payType === 2 }">
          <van-radio :name="2" checked-color="#FF6B95">
            <template #icon>
              <span class="pay-icon ali">支付宝</span>
            </template>
            支付宝
          </van-radio>
        </div>
      </van-radio-group>
    </div>

    <!-- 订单备注 -->
    <div class="remark-section">
      <div class="section-item">
        <span class="item-label">订单备注</span>
        <van-field
          v-model="remark"
          placeholder="选填，请填写备注信息"
          border
          class="remark-input"
        />
      </div>
    </div>

    <!-- 底部提交栏 -->
    <van-submit-bar
      :price="finalTotal * 100"
      button-text="提交订单"
      @submit="handleSubmitOrder"
      :loading="submitting"
    >
      <template #default>
        <span class="total-label">合计：</span>
      </template>
    </van-submit-bar>

    <!-- 优惠券选择弹窗 -->
    <van-action-sheet
      v-model:show="showCouponSheet"
      title="选择优惠券"
      :close-on-click-action="false"
    >
      <div class="coupon-list">
        <div
          class="coupon-item"
          v-for="coupon in availableCoupons"
          :key="coupon.id"
          :class="{ selected: selectedCouponId === coupon.id }"
          @click="selectCoupon(coupon)"
        >
          <div class="coupon-left">
            <div class="coupon-amount">
              <span class="amount-symbol">¥</span>
              <span class="amount-value">{{ coupon.amount }}</span>
            </div>
            <div class="coupon-condition">满{{ coupon.minAmount }}可用</div>
          </div>
          <div class="coupon-right">
            <div class="coupon-name">{{ coupon.name }}</div>
            <div class="coupon-expire">有效期至 {{ coupon.expireTime }}</div>
          </div>
          <van-icon v-if="selectedCouponId === coupon.id" name="success" color="#FF6B95" size="20" />
        </div>
        <div class="no-coupon" v-if="availableCoupons.length === 0">
          暂无可用优惠券
        </div>
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup lang="ts">
const goBack = () => { window.location.hash = '#/' }
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { getAddressList } from '@/api/member'
import { getCouponList } from '@/api/member'
import { createOrder } from '@/api/order'

const router = useRouter()

// ---------- 地址 ----------
const selectedAddress = ref<any>(null)

const goSelectAddress = () => {
  window.location.hash = '#/user/address?select=1'
}

// ---------- 订单商品 ----------
const orderItems = ref<any[]>([])

// ---------- 价格计算 ----------
const payType = ref(1)
const remark = ref('')
const freight = ref(0)
const submitting = ref(false)

const goodsTotal = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const couponDiscount = ref(0)
const promotionDiscount = computed(() => {
  // 满300减50
  return goodsTotal.value >= 300 ? 50 : 0
})

const finalTotal = computed(() => {
  return Math.max(0, goodsTotal.value + freight.value - couponDiscount.value - promotionDiscount.value)
})

// ---------- 优惠券 ----------
const showCouponSheet = ref(false)
const selectedCouponId = ref<number>()
const selectedCoupon = computed(() => {
  return availableCoupons.value.find(c => c.id === selectedCouponId.value)
})

const availableCoupons = ref<any[]>([])

const selectCoupon = (coupon: any) => {
  if (goodsTotal.value < coupon.minAmount) {
    showToast('未达使用门槛')
    return
  }
  if (selectedCouponId.value === coupon.id) {
    selectedCouponId.value = undefined
    couponDiscount.value = 0
  } else {
    selectedCouponId.value = coupon.id
    couponDiscount.value = coupon.amount
  }
  showCouponSheet.value = false
}

// ---------- 提交订单 ----------
const handleSubmitOrder = async () => {
  if (!selectedAddress.value) {
    showToast('请选择收货地址')
    return
  }
  if (orderItems.value.length === 0) {
    showToast('没有商品')
    return
  }
  submitting.value = true
  try {
    const addr = selectedAddress.value
    const orderData = {
      addressId: addr.id,
      items: orderItems.value.map(i => ({
        skuId: i.skuId,
        productId: i.productId,
        quantity: i.quantity
      })),
      payType: payType.value,
      couponId: selectedCouponId.value,
      remark: remark.value
    }

    const res: any = await createOrder(orderData)

    if (res?.code === 200) {
      showSuccessToast('下单成功')
      localStorage.removeItem('checkoutItems')
      window.location.hash = `#/order/${res.data.id || res.data.orderNo}`
    } else {
      showToast(res?.message || '下单失败')
    }
  } catch {
    showToast('下单失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  // 加载结算商品
  const checkoutData = localStorage.getItem('checkoutItems')
  if (checkoutData) {
    try {
      orderItems.value = JSON.parse(checkoutData)
    } catch { /* ignore */ }
  }

  // 加载地址列表
  try {
    const addrRes: any = await getAddressList()
    if (addrRes?.code === 200 && addrRes.data) {
      const addresses = addrRes.data.records || addrRes.data
      // 优先选择默认地址
      selectedAddress.value = addresses.find((a: any) => a.isDefault) || addresses[0] || null
    }
  } catch { /* ignore */ }

  // 加载优惠券列表
  try {
    const couponRes: any = await getCouponList({ status: 0 })
    if (couponRes?.code === 200 && couponRes.data) {
      availableCoupons.value = couponRes.data.records || couponRes.data
    }
  } catch { /* ignore */ }
})
</script>

<style scoped>
.checkout-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-top: 46px;
  padding-bottom: 60px;
}

.address-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.address-card {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.address-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.address-phone {
  font-size: 14px;
  color: var(--text-secondary);
}

.address-detail {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.no-address {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  color: var(--text-secondary);
}

.address-arrow {
  margin-left: 8px;
  color: var(--text-secondary);
}

.goods-section {
  background: white;
  margin-bottom: 8px;
}

.goods-item {
  display: flex;
  padding: 12px 16px;
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
  margin-left: 12px;
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
  align-items: baseline;
  gap: 8px;
}

.goods-price {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color);
}

.goods-original {
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.goods-quantity {
  margin-left: auto;
  font-size: 13px;
  color: var(--text-secondary);
}

.coupon-section,
.order-info-section,
.payment-section,
.remark-section {
  background: white;
  padding: 0 16px;
  margin-bottom: 8px;
}

.section-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-color);
}

.section-item:last-child {
  border-bottom: none;
}

.item-label {
  font-size: 14px;
  color: var(--text-primary);
}

.item-value {
  font-size: 14px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-value.discount {
  color: var(--primary-color);
}

.item-hint {
  font-size: 13px;
  color: var(--text-secondary);
}

.coupon-selected {
  color: var(--primary-color);
  font-weight: 600;
}

.free {
  color: var(--success-color);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  padding: 14px 0 10px;
}

.payment-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.payment-item:last-child {
  border-bottom: none;
}

.pay-icon {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
  color: white;
  margin-right: 6px;
}

.pay-icon.wx {
  background: #07C160;
}

.pay-icon.ali {
  background: #1677FF;
}

.remark-input {
  margin-top: 8px;
  border-radius: 8px;
}

.total-label {
  font-size: 14px;
  color: var(--text-primary);
}

/* 优惠券弹窗 */
.coupon-list {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.coupon-item {
  display: flex;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  border-radius: 8px;
  background: linear-gradient(135deg, #FFF5F7, #FFF0F3);
  border: 2px solid transparent;
  position: relative;
}

.coupon-item.selected {
  border-color: var(--primary-color);
}

.coupon-left {
  text-align: center;
  min-width: 80px;
  border-right: 1px dashed var(--primary-color);
  padding-right: 12px;
  margin-right: 12px;
}

.amount-symbol {
  font-size: 14px;
  color: var(--primary-color);
}

.amount-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--primary-color);
}

.coupon-condition {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.coupon-right {
  flex: 1;
}

.coupon-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.coupon-expire {
  font-size: 12px;
  color: var(--text-secondary);
}

.no-coupon {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}

/* ========== PC 宽屏 ========== */
@media screen and (min-width: 768px) {
  .checkout-page {
    padding-top: 0;
    padding-bottom: 0 !important;
  }

  .address-section,
  .goods-section,
  .coupon-section,
  .order-info-section,
  .payment-section,
  .remark-section {
    max-width: 800px;
    margin-left: auto;
    margin-right: auto;
  }
}
</style>
