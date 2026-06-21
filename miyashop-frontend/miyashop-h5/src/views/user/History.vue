<template>
  <div class="history-page">
    <van-nav-bar title="浏览记录" left-arrow @click="goBack" fixed>
      <template #right>
        <span class="clear-btn" @click="clearAll" v-if="list.length > 0">清空</span>
      </template>
    </van-nav-bar>

    <van-empty v-if="list.length === 0" description="暂无浏览记录" />

    <div class="history-list" v-else>
      <div class="history-date-group" v-for="(group, date) in groupedList" :key="date">
        <div class="date-header">{{ date }}</div>
        <div class="history-grid">
          <div
            class="history-card"
            v-for="item in group"
            :key="item.id"
            @click="goProduct(item)"
          >
            <img :src="item.image" :alt="item.name" />
            <div class="card-info">
              <div class="card-name">{{ item.name }}</div>
              <div class="card-price">¥{{ item.price }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const goBack = () => { window.location.hash = '#/' }
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'

const router = useRouter()

const list = ref<any[]>([])

const groupedList = computed(() => {
  const groups: Record<string, any[]> = {}
  list.value.forEach(item => {
    if (!groups[item.date]) groups[item.date] = []
    groups[item.date].push(item)
  })
  return groups
})

const goProduct = (item: any) => {
  window.location.hash = `#/product/${item.productId}`
}

const clearAll = async () => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定清空所有浏览记录吗？' })
    list.value = []
    showSuccessToast('已清空')
  } catch { /* canceled */ }
}

onMounted(() => {
  // No backend API for browsing history — leave empty
})
</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-top: 46px;
}

.clear-btn {
  font-size: 14px;
  color: var(--text-secondary);
}

.history-list {
  padding: 12px;
}

.history-date-group {
  margin-bottom: 16px;
}

.date-header {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  padding: 8px 4px;
}

.history-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

@media screen and (min-width: 768px) {
  .history-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.history-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.history-card img {
  width: 100%;
  aspect-ratio: 1;
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
  margin-bottom: 4px;
}

.card-price {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color);
}
</style>
