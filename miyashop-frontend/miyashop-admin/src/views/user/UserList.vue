<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID/昵称">
          <el-input v-model="searchForm.keyword" placeholder="请输入用户ID或昵称" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.mobile" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" :value="('' as any)" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计数据 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="6" v-for="s in statsCards" :key="s.label">
        <el-card shadow="hover" class="stat-mini-card">
          <div class="stat-mini-value" :style="{ color: s.color }">{{ s.value }}</div>
          <div class="stat-mini-label">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="用户信息" min-width="200">
        <template #default="{ row }">
          <div class="user-info-cell">
            <el-avatar :size="40" :src="row.avatar">
              <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            </el-avatar>
            <div class="user-text">
              <div class="user-nickname">{{ row.nickname || row.userId }}</div>
              <div class="user-id">ID: {{ row.userId || row.id }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="mobile" label="手机号" width="140" align="center" />
      <el-table-column label="性别" width="70" align="center">
        <template #default="{ row }">
          <span v-if="row.gender === 1">男</span>
          <span v-else-if="row.gender === 2">女</span>
          <span v-else>未知</span>
        </template>
      </el-table-column>
      <el-table-column label="会员等级" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="(getLevelType(row.level) as 'info' | 'success' | 'warning' | 'danger')"
            size="small"
            effect="dark"
          >
            Lv.{{ row.level || 1 }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="积分" width="80" align="center">
        <template #default="{ row }">{{ row.points || 0 }}</template>
      </el-table-column>
      <el-table-column label="余额" width="120" align="center">
        <template #default="{ row }">
          <span style="color: #FF6B95; font-weight: 500">¥{{ row.balance || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="累计消费" width="120" align="center">
        <template #default="{ row }">
          <span style="font-weight: 500">¥{{ row.totalSpent || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            active-color="#FF6B95"
            @change="(val: any) => handleStatusChange(row, val ? 1 : 0)"
          />
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="170" align="center">
        <template #default="{ row }">{{ row.createdTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleView(row)">详情</el-button>
          <el-button size="small" type="success" link @click="handleViewOrders(row)">订单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        background
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="600px" destroy-on-close>
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="用户ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="用户编号">{{ detailData.userId || detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailData.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.mobile || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ detailData.gender === 1 ? '男' : detailData.gender === 2 ? '女' : '未知' }}
        </el-descriptions-item>
        <el-descriptions-item label="生日">{{ detailData.birthday || '-' }}</el-descriptions-item>
        <el-descriptions-item label="会员等级">Lv.{{ detailData.level || 1 }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ detailData.points || 0 }}</el-descriptions-item>
        <el-descriptions-item label="余额">¥{{ detailData.balance || 0 }}</el-descriptions-item>
        <el-descriptions-item label="累计消费">¥{{ detailData.totalSpent || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
            {{ detailData.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ detailData.lastLoginTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detailData.createdTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getUserList } from '@/api/user'

const router = useRouter()

// ---------- 搜索 ----------
const searchForm = reactive({
  keyword: '',
  mobile: '',
  status: undefined as number | undefined
})

// ---------- 统计 ----------
const statsCards = ref([
  { label: '总用户数', value: 0, color: '#409EFF' },
  { label: '今日新增', value: 0, color: '#67C23A' },
  { label: '活跃用户', value: 0, color: '#E6A23C' },
  { label: '禁用用户', value: 0, color: '#F56C6C' }
])

// ---------- 表格 ----------
const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const getLevelType = (level: number) => {
  const map: Record<number, string> = { 1: 'info', 2: 'success', 3: 'warning', 4: 'danger' }
  return map[level] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      mobile: searchForm.mobile || undefined,
      status: searchForm.status
    })
    if (res.code === 200) {
      const data = res.data
      tableData.value = data.records || []
      pagination.total = data.total || 0
    }
  } catch {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
    updateStats()
  }
}

const updateStats = () => {
  statsCards.value[0].value = pagination.total
  statsCards.value[3].value = tableData.value.filter(u => u.status === 0).length
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.mobile = ''
  searchForm.status = undefined
  pagination.page = 1
  fetchData()
}

// ---------- 状态切换 ----------
const handleStatusChange = (row: any, status: number) => {
  ElMessage.success(status === 1 ? '已启用' : '已禁用')
  row.status = status
}

// ---------- 详情 ----------
const detailVisible = ref(false)
const detailData = ref<any>(null)

const handleView = (row: any) => {
  detailData.value = row
  detailVisible.value = true
}

// ---------- 查看订单 ----------
const handleViewOrders = (row: any) => {
  router.push({ path: '/order/list', query: { userId: row.id } })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.search-section {
  background: var(--bg-primary);
  padding: 20px;
  border-radius: 8px;
}

.stat-mini-card { text-align: center; border-radius: 8px; }
.stat-mini-value { font-size: 28px; font-weight: 700; margin-bottom: 8px; }
.stat-mini-label { font-size: 13px; color: var(--text-secondary); }

.user-info-cell { display: flex; align-items: center; gap: 12px; }
.user-nickname { font-size: 14px; color: var(--text-primary); font-weight: 500; }
.user-id { font-size: 12px; color: var(--text-secondary); }

.pagination-section { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
