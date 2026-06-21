<template>
  <div class="cart-page">
    <!-- 顶部导航 -->
    <van-nav-bar title="购物车" fixed>
      <template #right>
        <span class="edit-btn" @click="editMode = !editMode">
          {{ editMode ? '完成' : '管理' }}
        </span>
      </template>
    </van-nav-bar>

    <!-- 空状态 -->
    <van-empty
      v-if="cartItems.length === 0"
      image="shopping-cart-o"
      description="购物车还是空的"
    >
      <van-button type="danger" round custom-style="background: #FF6B95; border-color: #FF6B95" @click="goHome()">
        去逛逛
      </van-button>
    </van-empty>

    <!-- 购物车列表 -->
    <div class="cart-list" v-else>
      <!-- 按店铺分组 -->
      <div class="cart-group" v-for="(group, shopName) in groupedItems" :key="shopName">
        <div class="shop-header">
          <van-checkbox
            v-model="group.checked"
            @change="onGroupCheckChange(group)"
            icon-size="18"
            checked-color="#FF6B95"
          />
          <span class="shop-name">{{ shopName }}</span>
          <span class="shop-promo" v-if="group.promo">满{{ group.promo.minAmount }}减{{ group.promo.discount }}</span>
        </div>

        <div
          class="cart-item"
          v-for="item in group.items"
          :key="item.id"
          :class="{ editing: editMode }"
        >
          <van-checkbox
            v-model="item.checked"
            @change="onItemCheckChange"
            icon-size="18"
            checked-color="#FF6B95"
          />
          <div class="item-image" @click="goProduct(item)">
            <img :src="item.image" :alt="item.name" />
            <!-- SKU标签 -->
            <div class="sku-badge" v-if="item.type === 'seckill'">秒杀</div>
            <div class="sku-badge groupbuy" v-if="item.type === 'groupbuy'">拼团</div>
          </div>
          <div class="item-info" @click="goProduct(item)">
            <div class="item-name">{{ item.name }}</div>
            <div class="item-specs" v-if="item.specs">{{ item.specs }}</div>
            <div class="item-price-row">
              <div class="item-price">¥{{ item.price }}</div>
              <div class="item-original" v-if="item.originalPrice && item.originalPrice > item.price">
                ¥{{ item.originalPrice }}
              </div>
            </div>
            <div class="item-bottom">
              <van-stepper
                v-model="item.quantity"
                :min="1"
                :max="item.maxStock || 99"
                integer
                theme="round"
                @change="onQuantityChange(item)"
              />
            </div>
          </div>
          <!-- 编辑模式下的删除按钮 -->
          <div class="delete-btn" v-if="editMode" @click="removeItem(item)">
            <van-icon name="delete-o" size="20" color="#F56C6C" />
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐商品 -->
    <div class="recommend-section" v-if="cartItems.length > 0">
      <div class="section-title">— 猜你喜欢 —</div>
      <div class="product-grid">
        <div class="product-card" v-for="item in recommendProducts" :key="item.id" @click="goProduct(item)">
          <img :src="item.image" :alt="item.name" />
          <div class="card-info">
            <div class="card-name">{{ item.name }}</div>
            <div class="card-price">¥{{ item.price }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部固定块：结算栏 + TabBar -->
    <div class="bottom-bar" v-if="cartItems.length > 0">
      <div class="settle-row">
        <div class="settle-left">
          <div class="check-all" @click="onAllCheckChange(!allChecked)">
            <span class="check-box" :class="{ on: allChecked }">✓</span>
            <span class="check-text">全选</span>
          </div>
          <div class="settle-info">
            <div class="settle-total" v-if="!editMode">
              <span class="total-label">合计:</span>
              <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="settle-discount" v-if="!editMode && totalDiscount > 0">已优惠 ¥{{ totalDiscount }}</div>
          </div>
        </div>
        <button class="settle-btn" :class="{ danger: editMode }" @click="handleSubmit">
          {{ editMode ? `删除(${deleteCount})` : `去结算(${checkedCount})` }}
        </button>
      </div>
      <TabBar :tab="2" :badge="cartCount" inline />
    </div>
    <div class="bottom-bar bottom-bar-empty" v-else>
      <div class="settle-row">
        <div class="settle-left">
          <div class="check-all">
            <span class="check-box" :class="{ on: allChecked }">✓</span>
            <span class="check-text">全选</span>
          </div>
          <div class="settle-info">
            <span class="total-label">合计:</span>
            <span class="total-price">¥0.00</span>
          </div>
        </div>
        <button class="settle-btn" disabled>去结算(0)</button>
      </div>
      <TabBar :tab="2" :badge="cartCount" inline />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import TabBar from '@/components/TabBar.vue'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const editMode = ref(false)
const cartCount = computed(() => userStore.cartCount)

// ---------- 购物车数据 ----------
const cartItems = ref<any[]>([
  {
    id: 1, productId: 1, skuId: 1, shopName: 'MiyaShop官方旗舰店',
    name: '优雅碎花连衣裙 夏季新款', specs: '红色 S',
    image: 'https://via.placeholder.com/100/FF6B95/FFFFFF?text=C1',
    price: 299, originalPrice: 599, quantity: 1, maxStock: 50, checked: true, type: 'normal'
  },
  {
    id: 2, productId: 2, skuId: 103, shopName: 'MiyaShop官方旗舰店',
    name: '时尚纯棉衬衫', specs: '白色 M',
    image: 'https://via.placeholder.com/100/FFB6C1/FFFFFF?text=C2',
    price: 199, originalPrice: 399, quantity: 2, maxStock: 100, checked: true, type: 'normal'
  },
  {
    id: 3, productId: 3, skuId: 201, shopName: 'MiyaShop首饰店',
    name: '精致项链套装', specs: '金色',
    image: 'https://via.placeholder.com/100/FFC0CB/FFFFFF?text=C3',
    price: 159, originalPrice: 299, quantity: 1, maxStock: 30, checked: false, type: 'normal'
  }
])

// ---------- 分组 ----------
const groupedItems = computed(() => {
  const groups: Record<string, any> = {}
  cartItems.value.forEach(item => {
    const key = item.shopName || '默认店铺'
    if (!groups[key]) {
      groups[key] = { items: [], checked: false, promo: { minAmount: 300, discount: 50 } }
    }
    groups[key].items.push(item)
  })
  // 计算分组选中状态
  Object.values(groups).forEach(group => {
    group.checked = group.items.every((i: any) => i.checked)
  })
  return groups
})

// ---------- 全选 ----------
const allChecked = computed({
  get: () => cartItems.value.length > 0 && cartItems.value.every(i => i.checked),
  set: (val: boolean) => {
    cartItems.value.forEach(i => i.checked = val)
  }
})

const onAllCheckChange = (val: boolean) => {
  cartItems.value.forEach(i => i.checked = val)
}

const onGroupCheckChange = (group: any) => {
  group.items.forEach((i: any) => i.checked = group.checked)
}

const onItemCheckChange = () => {
  // triggered reactively
}

// ---------- 计算 ----------
const checkedItems = computed(() => cartItems.value.filter(i => i.checked))
const checkedCount = computed(() => checkedItems.value.reduce((sum, i) => sum + i.quantity, 0))

const totalPrice = computed(() => {
  return checkedItems.value.reduce((sum, i) => sum + i.price * i.quantity, 0)
})

const totalDiscount = computed(() => {
  return checkedItems.value.reduce((sum, i) => {
    if (i.originalPrice && i.originalPrice > i.price) {
      return sum + (i.originalPrice - i.price) * i.quantity
    }
    return sum
  }, 0)
})

const deleteCount = computed(() => cartItems.value.filter(i => i.checked).length)

// ---------- 操作 ----------
const onQuantityChange = (item: any) => {
  // Sync changes
  userStore.setCartCount(cartItems.value.reduce((sum, i) => sum + i.quantity, 0))
}

const removeItem = async (item: any) => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定删除该商品吗？' })
    cartItems.value = cartItems.value.filter(i => i.id !== item.id)
    userStore.setCartCount(cartItems.value.reduce((sum, i) => sum + i.quantity, 0))
    showToast('已删除')
  } catch {
    // canceled
  }
}

const handleSubmit = () => {
  if (editMode.value) {
    // 批量删除
    if (checkedItems.value.length === 0) {
      showToast('请选择商品')
      return
    }
    showConfirmDialog({ title: '提示', message: `确定删除选中的${checkedItems.value.length}件商品吗？` })
      .then(() => {
        cartItems.value = cartItems.value.filter(i => !i.checked)
        userStore.setCartCount(cartItems.value.reduce((sum, i) => sum + i.quantity, 0))
        editMode.value = false
        showToast('已删除')
      })
      .catch(() => {})
  } else {
    // 去结算
    if (checkedItems.value.length === 0) {
      showToast('请选择商品')
      return
    }
    const checkoutItems = checkedItems.value.map(i => ({
      skuId: i.skuId,
      productId: i.productId,
      name: i.name,
      specs: i.specs,
      price: i.price,
      originalPrice: i.originalPrice,
      image: i.image,
      quantity: i.quantity,
      type: i.type
    }))
    localStorage.setItem('checkoutItems', JSON.stringify(checkoutItems))
    window.location.hash = '#/checkout'
  }
}

const goHome = () => { window.location.hash = '#/' }

const goProduct = (item: any) => {
  if (editMode.value) return
  window.location.hash = `#/product/${item.productId}`
}

const goTab = (path: string) => {
  window.location.hash = '#' + path
}

// ---------- 推荐商品 ----------
const recommendProducts = ref([
  { id: 4, image: 'https://via.placeholder.com/300/FF6B95/FFFFFF?text=R1', name: '时尚手镯 气质款', price: 199 },
  { id: 5, image: 'https://via.placeholder.com/300/FFB6C1/FFFFFF?text=R2', name: '口红礼盒 6支装', price: 299 },
  { id: 6, image: 'https://via.placeholder.com/300/FFC0CB/FFFFFF?text=R3', name: '简约T恤 百搭款', price: 99 },
  { id: 7, image: 'https://via.placeholder.com/300/FF69B4/FFFFFF?text=R4', name: '香水套装 小样', price: 159 }
])

onMounted(() => {
  // Load cart from localStorage or API
  const savedCart = localStorage.getItem('cartItems')
  if (savedCart) {
    try { cartItems.value = JSON.parse(savedCart) } catch { /* ignore */ }
  }
})
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-top: 46px;
  padding-bottom: 110px;
}

/* ========== 底部固定块 ========== */
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; z-index: 1000; background: #fff; }
.bottom-bar-empty { opacity: 0.7; }
.settle-row { display: flex; align-items: center; padding: 0 16px; height: 50px; border-top: 1px solid #f0f0f0; }
.settle-left { flex: 1; display: flex; align-items: center; gap: 12px; }
.check-all { display: flex; align-items: center; gap: 6px; cursor: pointer; flex-shrink: 0; }
.check-box { width: 18px; height: 18px; border: 2px solid #ddd; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; color: transparent; }
.check-box.on { background: #FF6B95; border-color: #FF6B95; color: #fff; }
.check-text { font-size: 13px; color: #333; }
.settle-info { flex: 1; }
.settle-total { font-size: 14px; }
.total-label { color: #333; }
.total-price { color: #FF6B95; font-weight: 700; font-size: 16px; }
.settle-discount { font-size: 11px; color: #FF6B95; }
.settle-btn { height: 36px; padding: 0 20px; background: #FF6B95; color: #fff; border: none; border-radius: 18px; font-size: 14px; font-weight: 600; cursor: pointer; flex-shrink: 0; }
.settle-btn.danger { background: #F56C6C; }
.settle-btn:disabled { background: #ccc; }

.edit-btn {
  font-size: 14px;
  color: var(--primary-color);
}

.cart-list {
  padding-bottom: 16px;
}

.cart-group {
  background: white;
  margin-bottom: 8px;
}

.shop-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}

.shop-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-left: 8px;
  flex: 1;
}

.shop-promo {
  font-size: 12px;
  color: var(--primary-color);
  background: rgba(255, 107, 149, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  position: relative;
  transition: transform 0.3s;
}

.cart-item.editing {
  transform: translateX(-40px);
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  margin: 0 10px;
  position: relative;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sku-badge {
  position: absolute;
  top: 0;
  left: 0;
  background: linear-gradient(135deg, #FF6B95, #FF5580);
  color: white;
  padding: 1px 6px;
  font-size: 10px;
  border-radius: 8px 0 8px 0;
}

.sku-badge.groupbuy {
  background: linear-gradient(135deg, #E6A23C, #F59E0B);
}

.item-info {
  flex: 1;
  min-width: 0;
  margin-left: 8px;
}

.item-name {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 6px;
}

.item-specs {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
  background: var(--bg-secondary);
  display: inline-block;
  padding: 1px 8px;
  border-radius: 3px;
}

.item-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.item-price {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color);
}

.item-original {
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.item-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.delete-btn {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recommend-section {
  padding: 16px;
}

.section-title {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.product-card img {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.card-info {
  padding: 10px;
}

.card-name {
  font-size: 13px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.card-price {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color);
}

.check-all-text {
  font-size: 14px;
  color: var(--text-primary);
}

.discount-amount {
  color: var(--primary-color);
  font-weight: 600;
}

/* ========== PC 宽屏 ========== */
@media screen and (min-width: 768px) {
  .cart-page {
    padding-top: 0;
    padding-bottom: 0 !important;
  }

  .cart-list {
    max-width: 800px;
    margin: 0 auto;
  }

  .recommend-section {
    max-width: 1200px;
    margin: 0 auto;
  }

  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
