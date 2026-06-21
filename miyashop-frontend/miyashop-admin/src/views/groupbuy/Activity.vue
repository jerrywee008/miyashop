<template>
  <div class="page-container">
    <!-- 操作按钮 -->
    <div class="action-section">
      <el-button type="primary" @click="handleAdd">创建团购活动</el-button>
      <el-button @click="fetchData" :icon="'Refresh'">刷新</el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="6" v-for="s in statsCards" :key="s.label">
        <el-card shadow="hover" class="stat-mini-card">
          <div class="stat-mini-value" :style="{ color: s.color }">{{ s.value }}</div>
          <div class="stat-mini-label">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活动列表 -->
    <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="商品" min-width="180">
        <template #default="{ row }">
          <div class="product-cell">
            <el-image :src="row.productImage" style="width: 50px; height: 50px; border-radius: 4px; margin-right: 8px" fit="cover" />
            <span>{{ row.productName || '-' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格信息" width="200" align="center">
        <template #default="{ row }">
          <div>
            <span style="text-decoration: line-through; color: #999; font-size: 12px">¥{{ row.originalPrice }}</span>
            <span style="color: #FF6B95; font-weight: 600; margin-left: 8px; font-size: 16px">¥{{ row.groupbuyPrice }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="成团条件" width="120" align="center">
        <template #default="{ row }">
          <span>{{ row.minPeople }}人成团</span>
          <span v-if="row.maxPeople">/ 上限{{ row.maxPeople }}人</span>
        </template>
      </el-table-column>
      <el-table-column label="活动时间" width="320">
        <template #default="{ row }">
          <div>{{ row.startTime }} 至</div>
          <div>{{ row.endTime }}</div>
        </template>
      </el-table-column>
      <el-table-column label="有效时长" width="100" align="center">
        <template #default="{ row }">{{ row.validHours || 24 }}小时</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="(getStatusType(row.status) as 'info' | 'success' | 'warning' | 'danger')" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="success" link @click="handleViewTeams(row)">查看队伍</el-button>
          <el-popconfirm title="确定删除该活动吗？" @confirm="handleDelete(row)">
            <template #reference>
              <el-button size="small" type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        background
      />
    </div>

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="关联商品" prop="skuId">
          <el-select v-model="formData.skuId" filterable placeholder="选择商品SKU" style="width: 100%">
            <el-option v-for="p in productOptions" :key="p.id" :label="`${p.name} - ¥${p.price}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="formData.originalPrice" :min="0" :precision="2" />
          <span style="margin-left: 8px; color: #999">元</span>
        </el-form-item>
        <el-form-item label="团购价" prop="groupbuyPrice">
          <el-input-number v-model="formData.groupbuyPrice" :min="0" :precision="2" />
          <span style="margin-left: 8px; color: #999">元</span>
        </el-form-item>
        <el-form-item label="成团人数" prop="minPeople">
          <el-input-number v-model="formData.minPeople" :min="2" :max="100" />
          <span style="margin-left: 8px; color: #999">人</span>
        </el-form-item>
        <el-form-item label="最大人数">
          <el-input-number v-model="formData.maxPeople" :min="0" :max="1000" />
          <span style="margin-left: 8px; color: #999">0表示不限制</span>
        </el-form-item>
        <el-form-item label="有效时长">
          <el-input-number v-model="formData.validHours" :min="1" :max="720" />
          <span style="margin-left: 8px; color: #999">小时</span>
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="formData.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 队伍列表弹窗 -->
    <el-dialog v-model="teamVisible" title="团购队伍列表" width="900px" destroy-on-close>
      <el-table :data="teamData" v-loading="teamLoading" border stripe>
        <el-table-column prop="id" label="队伍ID" width="80" align="center" />
        <el-table-column label="团长" width="100">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 6px">
              <el-avatar :size="24" :src="row.leaderAvatar" />
              <span>{{ row.leaderName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="人数" width="100" align="center">
          <template #default="{ row }">{{ row.currentPeople }}/{{ row.maxPeople }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="(getTeamStatusType(row.status) as 'info' | 'success' | 'warning' | 'danger')" size="small">{{ getTeamStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="170" align="center">
          <template #default="{ row }">{{ row.expireTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{ row }">{{ row.createdTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="viewTeamDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="teamPagination.page"
          v-model:page-size="teamPagination.size"
          :page-sizes="[10, 20, 50]"
          :total="teamPagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchTeams"
          @current-change="fetchTeams"
          small
          background
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getGroupBuyActivities, getGroupBuyActivity, createGroupBuyActivity, updateGroupBuyActivity, deleteGroupBuyActivity, getTeamList, getTeamDetail } from '@/api/groupbuy'
import { getSkuList } from '@/api/product'

// ---------- 统计 ----------
const statsCards = ref([
  { label: '未开始', value: 0, color: '#909399' },
  { label: '进行中', value: 0, color: '#67C23A' },
  { label: '已结束', value: 0, color: '#F56C6C' },
  { label: '活动总数', value: 0, color: '#409EFF' }
])

// ---------- 表格 ----------
const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status] || '未知'
}

const getTeamStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

const getTeamStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待成团', 1: '进行中', 2: '已成团', 3: '已失败' }
  return map[status] || '未知'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getGroupBuyActivities({ page: pagination.page, size: pagination.size })
    if (res.code === 200) {
      const data = res.data
      tableData.value = data.records || []
      pagination.total = data.total || 0
    }
  } catch {
    ElMessage.error('获取团购活动列表失败')
  } finally {
    loading.value = false
    updateStats()
  }
}

const updateStats = () => {
  let notStarted = 0, running = 0, ended = 0
  tableData.value.forEach(a => {
    if (a.status === 0) notStarted++
    else if (a.status === 1) running++
    else if (a.status === 2) ended++
  })
  statsCards.value[0].value = notStarted
  statsCards.value[1].value = running
  statsCards.value[2].value = ended
  statsCards.value[3].value = pagination.total
}

const productOptions = ref<any[]>([])

const fetchProductOptions = async () => {
  try {
    const res = await getSkuList({ page: 1, size: 1000 })
    if (res.code === 200) {
      productOptions.value = res.data?.records || []
    }
  } catch {
    ElMessage.error('获取商品SKU列表失败')
  }
}

// ---------- 新增/编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('创建团购活动')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const editId = ref<number>()

const formData = reactive({
  name: '',
  skuId: undefined as number | undefined,
  originalPrice: 0,
  groupbuyPrice: 0,
  minPeople: 3,
  maxPeople: 0,
  validHours: 24,
  timeRange: [] as string[],
  sort: 0
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  skuId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }],
  groupbuyPrice: [{ required: true, message: '请输入团购价', trigger: 'blur' }],
  minPeople: [{ required: true, message: '请输入成团人数', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '创建团购活动'
  resetForm()
  fetchProductOptions()
  dialogVisible.value = true
}

const resetForm = () => {
  formData.name = ''
  formData.skuId = undefined
  formData.originalPrice = 0
  formData.groupbuyPrice = 0
  formData.minPeople = 3
  formData.maxPeople = 0
  formData.validHours = 24
  formData.timeRange = []
  formData.sort = 0
}

const handleEdit = async (row: any) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑团购活动'
  fetchProductOptions()
  try {
    const res = await getGroupBuyActivity(row.id)
    if (res.code === 200) {
      const data = res.data
      fillForm(data)
    }
  } catch {
    ElMessage.error('获取团购活动详情失败')
    return
  }
  dialogVisible.value = true
}

const fillForm = (data: any) => {
  formData.name = data.name || ''
  formData.skuId = data.skuId
  formData.originalPrice = data.originalPrice || 0
  formData.groupbuyPrice = data.groupbuyPrice || 0
  formData.minPeople = data.minPeople || 3
  formData.maxPeople = data.maxPeople || 0
  formData.validHours = data.validHours || 24
  formData.timeRange = [data.startTime, data.endTime]
  formData.sort = data.sort || 0
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    const payload = {
      name: formData.name,
      skuId: formData.skuId,
      originalPrice: formData.originalPrice,
      groupbuyPrice: formData.groupbuyPrice,
      minPeople: formData.minPeople,
      maxPeople: formData.maxPeople,
      validHours: formData.validHours,
      startTime: formData.timeRange[0],
      endTime: formData.timeRange[1],
      sort: formData.sort
    }
    try {
      const res = isEdit.value
        ? await updateGroupBuyActivity(editId.value!, payload)
        : await createGroupBuyActivity(payload)
      if (res && res.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '创建成功')
        dialogVisible.value = false
        fetchData()
      }
    } catch {
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row: any) => {
  try {
    const res = await deleteGroupBuyActivity(row.id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

// ---------- 队伍列表 ----------
const teamVisible = ref(false)
const teamLoading = ref(false)
const teamData = ref<any[]>([])
const teamPagination = reactive({ page: 1, size: 10, total: 0 })
const currentActivityForTeams = ref<number>()

const handleViewTeams = async (row: any) => {
  currentActivityForTeams.value = row.id
  teamPagination.page = 1
  teamVisible.value = true
  await fetchTeams()
}

const fetchTeams = async () => {
  if (!currentActivityForTeams.value) return
  teamLoading.value = true
  try {
    const res = await getTeamList(currentActivityForTeams.value, {
      page: teamPagination.page,
      size: teamPagination.size
    })
    if (res.code === 200) {
      const data = res.data
      teamData.value = data.records || []
      teamPagination.total = data.total || 0
    }
  } catch {
    ElMessage.error('获取队伍列表失败')
  } finally {
    teamLoading.value = false
  }
}

const viewTeamDetail = async (row: any) => {
  try {
    const res = await getTeamDetail(row.id)
    if (res.code === 200) {
      ElMessage.info('队伍详情数据已加载')
    }
  } catch {
    ElMessage.error('获取队伍详情失败')
  }
}

// ---------- 页面加载 ----------
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.action-section { display: flex; gap: 12px; }

.stat-mini-card { text-align: center; border-radius: 8px; }
.stat-mini-value { font-size: 28px; font-weight: 700; margin-bottom: 8px; }
.stat-mini-label { font-size: 13px; color: var(--text-secondary); }

.pagination-section { margin-top: 16px; display: flex; justify-content: flex-end; }

.product-cell { display: flex; align-items: center; }
</style>
