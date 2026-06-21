<template>
  <div class="page-container">
    <!-- 操作按钮 -->
    <div class="action-section">
      <el-button type="primary" @click="handleAdd">创建秒杀活动</el-button>
      <el-button @click="fetchData" :icon="'Refresh'">刷新</el-button>
    </div>

    <!-- 活动状态统计 -->
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
      <el-table-column label="活动时间" width="320">
        <template #default="{ row }">
          <div>{{ row.startTime || '-' }} 至</div>
          <div>{{ row.endTime || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="参与商品数" width="110" align="center">
        <template #default="{ row }">
          {{ row.skuCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column label="创建时间" width="170" align="center">
        <template #default="{ row }">{{ row.createdTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="success" link @click="handleManageSkus(row)">管理商品</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入活动名称" />
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
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- SKU管理弹窗 -->
    <el-dialog v-model="skuVisible" title="管理秒杀商品" width="800px" :close-on-click-modal="false" destroy-on-close>
      <div class="sku-add-section">
        <el-select v-model="selectedSkuId" filterable placeholder="选择商品SKU" style="width: 300px">
          <el-option
            v-for="sku in availableSkus"
            :key="sku.id"
            :label="`${sku.name} - ¥${sku.price} (库存:${sku.stock})`"
            :value="sku.id"
          />
        </el-select>
        <el-input-number
          v-model="skuPrice"
          :min="0"
          :precision="2"
          placeholder="秒杀价格"
          style="width: 150px; margin: 0 12px"
        />
        <el-input-number v-model="skuStock" :min="1" placeholder="库存" style="width: 120px; margin-right: 12px" />
        <el-button type="primary" @click="addSkuToActivity">添加</el-button>
      </div>

      <el-table :data="activitySkus" border stripe style="width: 100%; margin-top: 16px">
        <el-table-column label="商品图片" width="80" align="center">
          <template #default="{ row }">
            <el-image :src="row.image" style="width: 50px; height: 50px; border-radius: 4px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="originalPrice" label="原价" width="100" align="center">
          <template #default="{ row }">
            <span style="text-decoration: line-through; color: #999">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="秒杀价" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #FF6B95; font-weight: 600">¥{{ row.seckillPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="100" align="center">
          <template #default="{ row }">{{ row.sold }}/{{ row.stock }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-popconfirm title="确定移除该商品吗？" @confirm="removeSku(row)">
              <template #reference>
                <el-button size="small" type="danger" link>移除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getSeckillActivities, getSeckillActivity, createSeckillActivity, updateSeckillActivity, deleteSeckillActivity, getSeckillSkus, addSeckillSku, deleteSeckillSku } from '@/api/seckill'
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

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSeckillActivities({ page: pagination.page, size: pagination.size })
    if (res.code === 200) {
      const data = res.data
      tableData.value = data.records || []
      pagination.total = data.total || 0
    }
  } catch {
    ElMessage.error('获取秒杀活动列表失败')
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

// ---------- 新增/编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('创建秒杀活动')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const editId = ref<number>()

const formData = reactive({
  name: '',
  timeRange: [] as string[],
  sort: 0
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '创建秒杀活动'
  formData.name = ''
  formData.timeRange = []
  formData.sort = 0
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑秒杀活动'
  try {
    const res = await getSeckillActivity(row.id)
    if (res.code === 200) {
      const data = res.data
      formData.name = data.name || ''
      formData.timeRange = [data.startTime, data.endTime]
      formData.sort = data.sort || 0
    }
  } catch {
    ElMessage.error('获取活动详情失败')
    return
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    const payload = {
      name: formData.name,
      startTime: formData.timeRange[0],
      endTime: formData.timeRange[1],
      sort: formData.sort
    }
    try {
      const res = isEdit.value
        ? await updateSeckillActivity(editId.value!, payload)
        : await createSeckillActivity(payload)
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
    const res = await deleteSeckillActivity(row.id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

// ---------- SKU管理 ----------
const skuVisible = ref(false)
const currentActivityId = ref<number>()
const activitySkus = ref<any[]>([])
const selectedSkuId = ref<number>()
const skuPrice = ref(0)
const skuStock = ref(100)

const availableSkus = ref<any[]>([])

const fetchAvailableSkus = async () => {
  try {
    const res = await getSkuList({ page: 1, size: 200, status: 1 })
    if (res.code === 200) {
      availableSkus.value = res.data?.records || []
    }
  } catch {
    ElMessage.error('获取SKU列表失败')
  }
}

const handleManageSkus = async (row: any) => {
  currentActivityId.value = row.id
  selectedSkuId.value = undefined
  skuPrice.value = 0
  skuStock.value = 100
  fetchAvailableSkus()
  try {
    const res = await getSeckillSkus(row.id)
    if (res.code === 200) {
      activitySkus.value = res.data || []
    }
  } catch {
    ElMessage.error('获取活动商品失败')
  }
  skuVisible.value = true
}

const addSkuToActivity = () => {
  if (!selectedSkuId.value) {
    ElMessage.warning('请选择商品SKU')
    return
  }
  if (!skuPrice.value || skuPrice.value <= 0) {
    ElMessage.warning('请输入秒杀价格')
    return
  }
  const sku = availableSkus.value.find(s => s.id === selectedSkuId.value)
  if (!sku) return
  if (activitySkus.value.some(s => s.skuId === sku.id)) {
    ElMessage.warning('该商品已添加')
    return
  }
  activitySkus.value.push({
    id: Date.now(),
    skuId: sku.id,
    name: sku.name,
    originalPrice: sku.price,
    seckillPrice: skuPrice.value,
    stock: skuStock.value,
    sold: 0,
    image: sku.image || ''
  })
  ElMessage.success('添加成功')
}

const removeSku = (row: any) => {
  activitySkus.value = activitySkus.value.filter(s => s.id !== row.id)
  ElMessage.success('已移除')
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

.sku-add-section { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
</style>
