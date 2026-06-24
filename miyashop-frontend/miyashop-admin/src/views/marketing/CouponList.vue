<template>
  <div class="page-container">
    <!-- 搜索 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="优惠券名称">
          <el-input v-model="searchForm.name" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作 -->
    <div class="action-section">
      <el-button type="primary" @click="handleAdd">新增优惠券</el-button>
      <el-button @click="fetchData" :icon="'Refresh'">刷新</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="name" label="名称" min-width="150" show-overflow-tooltip />
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'danger' : 'warning'" size="small">
            {{ row.type === 1 ? '满减券' : '折扣券' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="面额/折扣" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.type === 1" class="price">¥{{ row.amount }}</span>
          <span v-else class="price">{{ row.discount }}折</span>
        </template>
      </el-table-column>
      <el-table-column label="最低消费" width="110" align="center">
        <template #default="{ row }">¥{{ row.minAmount || 0 }}</template>
      </el-table-column>
      <el-table-column label="发放/使用" width="110" align="center">
        <template #default="{ row }">{{ row.usedCount || 0 }}/{{ row.totalCount || 0 }}</template>
      </el-table-column>
      <el-table-column label="有效期" width="220" align="center">
        <template #default="{ row }">{{ row.startTime || '-' }} ~ {{ row.endTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            active-color="#FF6B95"
            @change="(val: boolean) => handleStatusChange(row, val ? 1 : 0)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该优惠券吗？" @confirm="handleDelete(row)">
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="formData.name" placeholder="例如：满300减50" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio :value="1">满减券</el-radio>
            <el-radio :value="2">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="formData.type === 1" label="面额" prop="amount">
          <el-input-number v-model="formData.amount" :min="1" :precision="2" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">元</span>
        </el-form-item>
        <el-form-item v-else label="折扣" prop="discount">
          <el-input-number v-model="formData.discount" :min="1" :max="99" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">折（如85表示8.5折）</span>
        </el-form-item>
        <el-form-item label="最低消费" prop="minAmount">
          <el-input-number v-model="formData.minAmount" :min="0" :precision="2" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">元，0表示无门槛</span>
        </el-form-item>
        <el-form-item label="发放数量" prop="totalCount">
          <el-input-number v-model="formData.totalCount" :min="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="每人限领" prop="perLimit">
          <el-input-number v-model="formData.perLimit" :min="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="有效期" prop="timeRange">
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
      </el-form>
      <template #footer>
        <div style="width: 100%">
          <div v-if="formError" style="color: #f56c6c; font-size: 13px; margin-bottom: 10px; text-align: center">{{ formError }}</div>
          <div style="display: flex; justify-content: flex-end; gap: 12px">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getCouponList, getCouponDetail, addCoupon, updateCoupon, deleteCoupon, updateCouponStatus } from '@/api/coupon'

// 搜索
const searchForm = reactive({ name: '', type: undefined as number | undefined, status: undefined as number | undefined })

// 表格
const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCouponList({
      page: pagination.page, size: pagination.size,
      ...(searchForm.name ? { name: searchForm.name } : {}),
      ...(searchForm.type != null ? { type: searchForm.type } : {}),
      ...(searchForm.status != null ? { status: searchForm.status } : {})
    })
    if (res && res.code === 200) {
      tableData.value = res.data?.records || []
      pagination.total = res.data?.total || 0
    } else if (res && res.code !== 200) {
      ElMessage.error(res.message || '获取优惠券列表失败')
    }
  } catch (e: any) {
    console.error('获取优惠券列表失败', e)
    ElMessage.error('获取优惠券列表失败')
  } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { searchForm.name = ''; searchForm.type = undefined; searchForm.status = undefined; pagination.page = 1; fetchData() }

// 状态切换
const handleStatusChange = async (row: any, status: number) => {
  try {
    const res = await updateCouponStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success(status === 1 ? '已启用' : '已禁用')
      fetchData()
    }
  } catch { ElMessage.error('操作失败') }
}

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增优惠券')
const submitting = ref(false)
const formError = ref('')
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const editId = ref<number>()

const formData = reactive({
  name: '', type: 1, amount: undefined as number | undefined,
  discount: undefined as number | undefined, minAmount: undefined as number | undefined,
  totalCount: 100, perLimit: 1, timeRange: [] as string[]
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  totalCount: [{ required: true, message: '请输入发放数量', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

const resetForm = () => {
  formData.name = ''; formData.type = 1; formData.amount = undefined
  formData.discount = undefined; formData.minAmount = undefined
  formData.totalCount = 100; formData.perLimit = 1; formData.timeRange = []
}

const handleAdd = () => { isEdit.value = false; dialogTitle.value = '新增优惠券'; resetForm(); formError.value = ''; dialogVisible.value = true }

const handleEdit = async (row: any) => {
  isEdit.value = true; editId.value = row.id; dialogTitle.value = '编辑优惠券'; formError.value = ''
  try {
    const res = await getCouponDetail(row.id)
    if (res.code === 200) {
      const d = res.data
      formData.name = d.name || ''; formData.type = d.type || 1
      formData.amount = d.amount; formData.discount = d.discount
      formData.minAmount = d.minAmount; formData.totalCount = d.totalCount || 100
      formData.perLimit = d.perLimit || 1
      formData.timeRange = [d.startTime, d.endTime]
    }
  } catch { ElMessage.error('获取优惠券详情失败'); return }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    const payload: any = {
      name: formData.name, type: formData.type,
      amount: formData.type === 1 ? formData.amount : null,
      discount: formData.type === 2 ? formData.discount : null,
      minAmount: formData.minAmount || 0,
      totalCount: formData.totalCount, perLimit: formData.perLimit,
      startTime: formData.timeRange[0], endTime: formData.timeRange[1],
      status: 1
    }
    try {
      const res = isEdit.value
        ? await updateCoupon(editId.value!, payload)
        : await addCoupon(payload)
      if (res && res.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '创建成功')
        dialogVisible.value = false; formError.value = ''; fetchData()
      }
    } catch (e: any) {
      let msg = ''
      if (e?.response?.data?.message) {
        msg = e.response.data.message
      } else if (e?.message && e.message !== 'Request failed with status code 400') {
        msg = e.message
      }
      formError.value = msg || '操作失败，请稍后重试'
    }
    finally { submitting.value = false }
  })
}

const handleDelete = async (row: any) => {
  try {
    const res = await deleteCoupon(row.id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功'); fetchData()
    }
  } catch { ElMessage.error('删除失败') }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.search-section { background: var(--bg-primary); padding: 20px; border-radius: 8px; }
.action-section { margin-top: 16px; display: flex; gap: 12px; }
.pagination-section { margin-top: 16px; display: flex; justify-content: flex-end; }
.price { color: #FF6B95; font-weight: 600; }
</style>
