<template>
  <div class="product-detail-page">
    <!-- 顶部导航 -->
    <van-nav-bar
      title="商品详情"
      left-arrow
      @click="goBack"
      fixed
    >
      <template #right>
        <van-icon name="ellipsis" size="20" @click="showShare" />
      </template>
    </van-nav-bar>

    <!-- 商品图片轮播 -->
    <div class="image-swiper">
      <van-swipe :autoplay="3000" indicator-color="white" @change="onSwipeChange">
        <van-swipe-item v-for="(img, index) in productImages" :key="index">
          <img :src="img" class="detail-image" />
        </van-swipe-item>
      </van-swipe>
      <div class="image-indicator">{{ currentImage + 1 }}/{{ productImages.length }}</div>
    </div>

    <!-- 价格区域 -->
    <div class="price-section">
      <div class="price-row">
        <div class="current-price">
          <span class="price-symbol">¥</span>
          <span class="price-value">{{ currentPrice }}</span>
        </div>
        <div class="original-price" v-if="product.originalPrice && product.originalPrice > product.price">
          ¥{{ product.originalPrice }}
        </div>
        <div class="discount-tag" v-if="discount">
          {{ discount }}折
        </div>
      </div>
      <div class="product-title">{{ product.name }}</div>
      <div class="product-subtitle">
        <span class="sales">已售{{ product.sales || 0 }}件</span>
        <span class="score">评分 {{ product.score || 5.0 }}分</span>
        <span class="review-count">{{ product.reviewCount || 0 }}条评价</span>
      </div>
    </div>

    <!-- 优惠信息 -->
    <div class="promotion-section" v-if="promotions.length > 0">
      <div class="promotion-item" v-for="(promo, index) in promotions" :key="index">
        <span class="promo-tag">{{ promo.tag }}</span>
        <span class="promo-text">{{ promo.text }}</span>
      </div>
    </div>

    <!-- SKU规格选择 -->
    <div class="spec-section">
      <div class="spec-title">规格选择</div>
      <div class="spec-list">
        <div
          v-for="sku in product.skus || []"
          :key="sku.id"
          class="spec-item"
          :class="{ active: selectedSkuId === sku.id, disabled: sku.stock <= 0 }"
          @click="selectSku(sku)"
        >
          {{ sku.specs }}
        </div>
      </div>
    </div>

    <!-- 数量选择 -->
    <div class="quantity-section">
      <span class="quantity-label">数量</span>
      <van-stepper
        v-model="quantity"
        :min="1"
        :max="maxBuyQuantity"
        integer
        theme="round"
      />
      <span class="stock-text" v-if="currentStock > 0">
        库存{{ currentStock }}件
      </span>
    </div>

    <!-- 配送信息 -->
    <div class="delivery-section">
      <div class="delivery-item">
        <span class="delivery-label">配送</span>
        <span class="delivery-value">{{ deliveryInfo }}</span>
      </div>
      <div class="delivery-item">
        <span class="delivery-label">运费</span>
        <span class="delivery-value">{{ freight > 0 ? `¥${freight}` : '免运费' }}</span>
      </div>
      <div class="delivery-item">
        <span class="delivery-label">服务</span>
        <span class="delivery-value">{{ serviceTags.join(' · ') }}</span>
      </div>
    </div>

    <!-- 商品评价 -->
    <div class="review-section">
      <div class="section-header">
        <span class="section-title">商品评价 ({{ reviews.length }})</span>
        <span class="section-more" @click="viewAllReviews">查看全部 ></span>
      </div>
      <div class="review-item" v-for="review in reviews.slice(0, 2)" :key="review.id">
        <div class="review-user">
          <van-image round width="32" height="32" :src="review.avatar" />
          <span class="review-name">{{ review.nickname }}</span>
          <van-rate v-model="review.score" readonly size="12" />
        </div>
        <div class="review-content">{{ review.content }}</div>
        <div class="review-images" v-if="review.images">
          <img
            v-for="(img, i) in review.images?.split(',')?.slice(0, 3)"
            :key="i"
            :src="img"
            class="review-img"
          />
        </div>
        <div class="review-sku">{{ review.skuSpecs }}</div>
        <div class="review-time">{{ review.createdTime }}</div>
      </div>
    </div>

    <!-- 商品详情 -->
    <div class="detail-section">
      <div class="section-header">
        <span class="section-title">商品详情</span>
      </div>
      <div class="detail-content" v-html="product.detail || '暂无详情'"></div>
      <div class="detail-images" v-if="detailImages.length > 0">
        <img
          v-for="(img, index) in detailImages"
          :key="index"
          :src="img"
          class="detail-desc-image"
        />
      </div>
    </div>

    <!-- 底部操作栏 -->
    <van-action-bar>
      <van-action-bar-icon icon="home-o" text="首页" to="/" />
      <van-action-bar-icon
        icon="shopping-cart-o"
        text="购物车"
        :badge="cartCount > 0 ? cartCount : undefined"
        to="/cart"
      />
      <van-action-bar-icon icon="star-o" text="收藏" @click="toggleFavorite" />
      <van-action-bar-button
        type="warning"
        text="加入购物车"
        @click="addToCart"
      />
      <van-action-bar-button
        type="danger"
        text="立即购买"
        @click="buyNow"
      />
    </van-action-bar>

    <!-- SKU选择弹窗 -->
    <van-action-sheet
      v-model:show="skuSheetVisible"
      title="选择规格"
      :close-on-click-action="false"
    >
      <div class="sku-sheet-content">
        <div class="sku-sheet-header">
          <img :src="productImages[0]" class="sku-sheet-image" />
          <div class="sku-sheet-info">
            <div class="sku-sheet-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ currentPrice }}</span>
            </div>
            <div class="sku-sheet-stock">
              库存{{ currentStock }}件
            </div>
            <div class="sku-sheet-selected" v-if="selectedSku">
              已选: {{ selectedSku.specs }}
            </div>
          </div>
        </div>
        <div class="sku-sheet-specs">
          <div class="spec-title">规格</div>
          <div class="spec-list">
            <div
              v-for="sku in product.skus || []"
              :key="sku.id"
              class="spec-item"
              :class="{ active: selectedSkuId === sku.id, disabled: sku.stock <= 0 }"
              @click="selectSku(sku)"
            >
              {{ sku.specs }}
            </div>
          </div>
        </div>
        <div class="sku-sheet-quantity">
          <span>数量</span>
          <van-stepper v-model="skuSheetQuantity" :min="1" :max="maxBuyQuantity" integer />
        </div>
        <van-button
          type="danger"
          block
          round
          custom-style="margin: 16px; background: #FF6B95; border-color: #FF6B95"
          @click="confirmSkuSheet"
        >
          {{ skuSheetMode === 'cart' ? '加入购物车' : '立即购买' }}
        </van-button>
      </div>
    </van-action-sheet>

    <!-- 分享面板 -->
    <van-share-sheet
      v-model:show="showShareVisible"
      title="分享商品"
      :options="shareOptions"
      @select="onShareSelect"
    />
  </div>
</template>

<script setup lang="ts">
const goBack = () => { window.location.hash = '#/' }
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/store/user'
import { getProductDetail, getProductReviews, getSkuList } from '@/api/product'
import { addToCart as addCartApi } from '@/api/cart'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const productId = computed(() => route.params.id as string)
const cartCount = computed(() => userStore.cartCount)

// ---------- 商品数据 ----------
const product = ref<any>({})
const productImages = ref<string[]>([])
const detailImages = ref<string[]>([])
const loading = ref(false)

const currentImage = ref(0)

const onSwipeChange = (index: number) => {
  currentImage.value = index
}

// ---------- SKU选择 ----------
const selectedSkuId = ref<number>()
const selectedSku = computed(() => {
  return (product.value.skus || []).find((s: any) => s.id === selectedSkuId.value)
})

const currentPrice = computed(() => {
  return selectedSku.value?.price ?? product.value.price ?? 0
})

const currentStock = computed(() => {
  return selectedSku.value?.stock ?? product.value.stock ?? 0
})

const maxBuyQuantity = computed(() => {
  return Math.min(currentStock.value, 99)
})

const discount = computed(() => {
  if (selectedSku.value?.originalPrice && selectedSku.value?.price) {
    return (selectedSku.value.price / selectedSku.value.originalPrice * 10).toFixed(1)
  }
  if (product.value.originalPrice && product.value.price) {
    return (product.value.price / product.value.originalPrice * 10).toFixed(1)
  }
  return ''
})

const quantity = ref(1)
const freight = ref(0)
const deliveryInfo = ref('全国包邮（港澳台及偏远地区除外）')
const serviceTags = ref(['7天无理由退换', '正品保证', '极速退款'])

const selectSku = (sku: any) => {
  if (sku.stock <= 0 && sku.id > 0) return  // 兜底规格（id=-1）允许无库存时选中
  selectedSkuId.value = sku.id
  if (quantity.value > sku.stock) {
    quantity.value = sku.stock
  }
}

// ---------- 促销 ----------
const promotions = ref<any[]>([])
// ---------- 评价 ----------
const reviews = ref<any[]>([])

const viewAllReviews = () => {
  showToast('查看全部评价')
}

// ---------- SKU弹窗 ----------
const skuSheetVisible = ref(false)
const skuSheetMode = ref<'cart' | 'buy'>('cart')
const skuSheetQuantity = ref(1)

const addToCart = () => {
  if (selectedSkuId.value == null) {
    skuSheetMode.value = 'cart'
    skuSheetQuantity.value = quantity.value
    skuSheetVisible.value = true
    return
  }
  doAddToCart()
}

const buyNow = () => {
  if (selectedSkuId.value == null) {
    skuSheetMode.value = 'buy'
    skuSheetQuantity.value = quantity.value
    skuSheetVisible.value = true
    return
  }
  doBuyNow()
}

const confirmSkuSheet = () => {
  quantity.value = skuSheetQuantity.value
  if (skuSheetMode.value === 'cart') {
    doAddToCart()
  } else {
    doBuyNow()
  }
}

const doAddToCart = async () => {
  if (selectedSkuId.value == null) {
    showToast('请先选择商品规格')
    return
  }
  if (!product.value.id) {
    showToast('商品数据加载中，请稍后')
    return
  }
  try {
    const res = await addCartApi({
      productId: product.value.id,
      skuId: selectedSkuId.value,
      quantity: quantity.value
    })
    if (res.code === 200) {
      showToast('已加入购物车')
      userStore.setCartCount((userStore.cartCount || 0) + quantity.value)
    }
  } catch (e: any) {
    console.error('加入购物车失败', e)
    showToast('加入购物车失败')
  }
}

const doBuyNow = () => {
  if (!selectedSkuId.value) {
    showToast('请选择商品规格')
    return
  }
  const items = [{
    skuId: selectedSkuId.value,
    productId: product.value.id,
    name: product.value.name,
    specs: selectedSku.value?.specs,
    price: currentPrice.value,
    image: selectedSku.value?.image || productImages.value[0],
    quantity: quantity.value
  }]
  localStorage.setItem('checkoutItems', JSON.stringify(items))
  window.location.hash = '#/checkout'
}

// ---------- 收藏 ----------
const toggleFavorite = () => {
  showToast('已收藏')
}

// ---------- 分享 ----------
const showShareVisible = ref(false)
const shareOptions = [
  { name: '微信', icon: 'wechat' },
  { name: '朋友圈', icon: 'wechat-moments' },
  { name: '微博', icon: 'weibo' },
  { name: '复制链接', icon: 'link' }
]
const showShare = () => {
  showShareVisible.value = true
}
const onShareSelect = () => {
  showShareVisible.value = false
  showToast('分享功能')
}

// ---------- 加载数据 ----------
onMounted(async () => {
  loading.value = true
  try {
    const res = await getProductDetail(productId.value)
    if (res.code === 200 && res.data) {
      product.value = res.data
      // Parse images
      if (res.data.images) {
        productImages.value = res.data.images.split(',').filter(Boolean)
      }
      // 从 SKU API 获取该商品的所有 SKU
      try {
        const skuRes = await getSkuList({ productId: res.data.id, page: 1, size: 200 })
        if (skuRes.code === 200 && skuRes.data?.records?.length) {
          product.value.skus = skuRes.data.records.map((s: any) => ({
            id: s.id,
            specs: s.specs || s.name || '默认',
            price: s.price,
            originalPrice: s.price,
            stock: s.stock || 0,
            image: s.image
          }))
        }
      } catch { /* ignore */ }
      // 如果没有 SKU 记录，用商品自身数据构造一个默认规格
      if (!product.value.skus || product.value.skus.length === 0) {
        product.value.skus = [{
          id: -1,
          specs: '默认',
          price: product.value.price || 0,
          originalPrice: product.value.price || 0,
          stock: Math.max(product.value.stock || 0, 1),
          image: ''
        }]
      }
      // Try to load reviews
      try {
        const reviewRes = await getProductReviews(res.data.id, { page: 1, size: 5 })
        if (reviewRes.code === 200) {
          reviews.value = reviewRes.data?.records || reviewRes.data || []
        }
      } catch { /* ignore */ }
    }
  } catch (e) {
    console.error('获取商品详情失败', e)
  } finally { loading.value = false }
  // 默认选中第一个有库存的SKU
  const firstAvailable = (product.value.skus || []).find((s: any) => s.stock > 0)
  if (firstAvailable) {
    selectedSkuId.value = firstAvailable.id
  }
})
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 60px;
  padding-top: 46px;
}

.image-swiper {
  position: relative;
  background: white;
}

.detail-image {
  width: 100%;
  height: 375px;
  object-fit: cover;
}

.image-indicator {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  z-index: 1;
}

.price-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}

.current-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 14px;
  color: var(--primary-color);
}

.price-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--primary-color);
  line-height: 1;
}

.original-price {
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.discount-tag {
  background: linear-gradient(135deg, #FF6B95, #FF5580);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.product-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.5;
}

.product-subtitle {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-secondary);
}

.promotion-section {
  background: white;
  padding: 12px 16px;
  margin-bottom: 8px;
}

.promotion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  font-size: 13px;
}

.promo-tag {
  background: linear-gradient(135deg, #FF6B95, #FF5580);
  color: white;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 600;
}

.promo-text {
  color: var(--text-secondary);
}

.spec-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.spec-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.spec-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-item {
  padding: 8px 16px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 13px;
  color: var(--text-primary);
  background: var(--bg-secondary);
  transition: all 0.2s;
}

.spec-item.active {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: rgba(255, 107, 149, 0.05);
}

.spec-item.disabled {
  color: var(--text-secondary);
  opacity: 0.5;
}

.quantity-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.quantity-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.stock-text {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: auto;
}

.delivery-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.delivery-item {
  display: flex;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
}

.delivery-label {
  color: var(--text-secondary);
  width: 60px;
}

.delivery-value {
  color: var(--text-primary);
  flex: 1;
}

.review-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.section-more {
  font-size: 13px;
  color: var(--text-secondary);
}

.review-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.review-item:last-child {
  border-bottom: none;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.review-name {
  font-size: 13px;
  color: var(--text-primary);
  flex: 1;
}

.review-content {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.review-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
}

.review-sku {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.review-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.detail-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.detail-content {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.8;
}

.detail-desc-image {
  width: 100%;
  margin-top: 8px;
  display: block;
}

/* SKU弹窗 */
.sku-sheet-content {
  padding-bottom: 16px;
}

.sku-sheet-header {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.sku-sheet-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
}

.sku-sheet-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.sku-sheet-price .price-value {
  font-size: 24px;
}

.sku-sheet-stock {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 4px 0;
}

.sku-sheet-selected {
  font-size: 13px;
  color: var(--primary-color);
}

.sku-sheet-specs {
  padding: 16px;
}

.sku-sheet-quantity {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  font-size: 14px;
}

/* ========== PC 宽屏 ========== */
@media screen and (min-width: 768px) {
  .product-detail-page {
    padding-top: 0;
    padding-bottom: 0 !important;
  }

  .image-swiper {
    max-width: 520px;
    margin: 0 auto;
    border-radius: 0;
  }

  .detail-image {
    height: 520px;
    object-fit: contain;
  }

  .price-section,
  .promotion-section,
  .spec-section,
  .quantity-section,
  .delivery-section,
  .review-section,
  .detail-section {
    max-width: 800px;
    margin-left: auto;
    margin-right: auto;
  }

  .spec-list {
    gap: 14px;
  }

  .spec-item {
    padding: 10px 24px;
    font-size: 14px;
  }

  .review-images {
    gap: 12px;
  }

  .review-img {
    width: 100px;
    height: 100px;
  }
}
</style>
