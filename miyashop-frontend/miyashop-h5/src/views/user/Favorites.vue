<template>
  <div class="favorites-page">
    <van-nav-bar title="我的收藏" left-arrow @click="goBack" fixed>
      <template #right>
        <span class="edit-btn" @click="editMode = !editMode">
          {{ editMode ? '完成' : '管理' }}
        </span>
      </template>
    </van-nav-bar>

    <van-empty v-if="favorites.length === 0" description="暂无收藏">
      <van-button type="danger" round to="/" custom-style="background: #FF6B95; border-color: #FF6B95">
        去逛逛
      </van-button>
    </van-empty>

    <div class="favorites-grid" v-else>
      <div
        class="favorite-card"
        v-for="item in favorites"
        :key="item.id"
        :class="{ editing: editMode }"
        @click="editMode ? toggleSelect(item) : goProduct(item)"
      >
        <div class="card-image">
          <img :src="item.image" :alt="item.name" />
          <van-icon
            v-if="editMode"
            :name="item.selected ? 'checked' : 'passed'"
            size="20"
            :color="item.selected ? '#FF6B95' : '#ccc'"
            class="select-icon"
          />
        </div>
        <div class="card-info">
          <div class="card-name">{{ item.name }}</div>
          <div class="card-price-row">
            <span class="card-price">¥{{ item.price }}</span>
            <span class="card-original" v-if="item.originalPrice">¥{{ item.originalPrice }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑模式批量操作 -->
    <div class="batch-bar" v-if="editMode && favorites.length > 0">
      <van-checkbox
        v-model="selectAll"
        @change="toggleSelectAll"
        checked-color="#FF6B95"
      >
        全选
      </van-checkbox>
      <van-button
        type="danger"
        size="small"
        round
        @click="batchDelete"
        :disabled="selectedCount === 0"
        custom-style="background: #FF6B95; border-color: #FF6B95"
      >
        删除({{ selectedCount }})
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
const goBack = () => { window.location.hash = '#/user' }
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showSuccessToast } from 'vant'

const router = useRouter()

const editMode = ref(false)
const favorites = ref<any[]>([])

const allSelected = computed({
  get: () => favorites.value.length > 0 && favorites.value.every(f => f.selected),
  set: (val: boolean) => favorites.value.forEach(f => f.selected = val)
})

const selectAll = ref(false)
const selectedCount = computed(() => favorites.value.filter(f => f.selected).length)

const toggleSelect = (item: any) => {
  item.selected = !item.selected
  selectAll.value = favorites.value.every(f => f.selected)
}

const toggleSelectAll = (val: boolean) => {
  favorites.value.forEach(f => f.selected = val)
  selectAll.value = val
}

const goProduct = (item: any) => {
  window.location.hash = `#/product/${item.productId}`
}

const batchDelete = async () => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: `确定删除选中的${selectedCount.value}件商品吗？`
    })
    favorites.value = favorites.value.filter(f => !f.selected)
    showSuccessToast('已删除')
    if (favorites.value.length === 0) {
      editMode.value = false
    }
  } catch { /* canceled */ }
}

onMounted(() => {
  // Mock data
  favorites.value = [
    {
      id: 1, productId: 1, name: '优雅碎花连衣裙 夏季新款',
      image: 'https://via.placeholder.com/300/FF6B95/FFFFFF?text=F1',
      price: 299, originalPrice: 599, selected: false
    },
    {
      id: 2, productId: 2, name: '时尚纯棉衬衫 百搭款',
      image: 'https://via.placeholder.com/300/FFB6C1/FFFFFF?text=F2',
      price: 199, originalPrice: 399, selected: false
    },
    {
      id: 3, productId: 3, name: '精致项链套装 气质款',
      image: 'https://via.placeholder.com/300/FFC0CB/FFFFFF?text=F3',
      price: 159, originalPrice: 299, selected: false
    },
    {
      id: 4, productId: 4, name: '简约T恤 基础百搭',
      image: 'https://via.placeholder.com/300/FF69B4/FFFFFF?text=F4',
      price: 99, selected: false
    },
    {
      id: 5, productId: 5, name: '时尚手镯 轻奢款',
      image: 'https://via.placeholder.com/300/DA70D6/FFFFFF?text=F5',
      price: 199, originalPrice: 399, selected: false
    },
    {
      id: 6, productId: 6, name: '口红礼盒 6支装',
      image: 'https://via.placeholder.com/300/FF6B95/FFFFFF?text=F6',
      price: 299, originalPrice: 499, selected: false
    }
  ]
})
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-top: 46px;
  padding-bottom: 60px;
}

.edit-btn {
  font-size: 14px;
  color: var(--primary-color);
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 12px;
}

@media screen and (min-width: 768px) {
  .favorites-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.favorite-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.favorite-card.editing {
  opacity: 0.9;
}

.card-image {
  width: 100%;
  aspect-ratio: 1;
  position: relative;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.select-icon {
  position: absolute;
  top: 8px;
  right: 8px;
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

.card-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.card-price {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color);
}

.card-original {
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.batch-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
</style>
