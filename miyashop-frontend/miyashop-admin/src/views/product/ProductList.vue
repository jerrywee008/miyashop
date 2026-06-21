<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
            v-model="searchForm.categoryId"
            :options="categoryOptions"
            :props="{ value: 'id', label: 'name', checkStrictly: true, emitPath: false }"
            placeholder="请选择分类"
            clearable
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" :value="undefined" />
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="action-section">
      <el-button type="primary" @click="handleAdd">新增商品</el-button>
      <el-button @click="handleRefresh" :icon="'Refresh'">刷新</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      border
      stripe
      style="width: 100%; margin-top: 16px"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="商品图片" width="100" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.images"
            :src="row.images?.split(',')[0]"
            style="width: 60px; height: 60px; border-radius: 4px"
            fit="cover"
            preview-teleported
            :preview-src-list="row.images?.split(',')"
          />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ row.categoryName || '未分类' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120" align="center">
        <template #default="{ row }">
          <span style="color: #FF6B95; font-weight: 600">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" align="center" />
      <el-table-column prop="sales" label="销量" width="100" align="center" />
      <el-table-column label="评分" width="80" align="center">
        <template #default="{ row }">
          <el-rate :model-value="row.score || 5" disabled size="small" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            :active-value="1"
            :inactive-value="0"
            active-color="#FF6B95"
            @change="(val: boolean) => handleStatusChange(row, val ? 1 : 0)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180" align="center">
        <template #default="{ row }">
          {{ row.createdTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="primary" link @click="handleView(row)">详情</el-button>
          <el-popconfirm
            title="确定删除该商品吗？"
            confirm-button-text="确定"
            cancel-button-text="取消"
            @confirm="handleDelete(row)"
          >
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
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        background
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="formData.categoryId"
            :options="categoryOptions"
            :props="{ value: 'id', label: 'name', checkStrictly: true, emitPath: false }"
            placeholder="请选择分类"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <el-input v-model="formData.images" placeholder="图片URL，多张用逗号分隔" />
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" :step="1" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">元</span>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="formData.stock" :min="0" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="商品详情">
          <el-input
            v-model="formData.detail"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="商品详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="商品ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ detailData.price }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ detailData.stock }}</el-descriptions-item>
        <el-descriptions-item label="销量">{{ detailData.sales }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ detailData.score || 5 }}分</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'info'">
            {{ detailData.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createdTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商品详情" :span="2">{{ detailData.detail || '暂无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getProductList, getProductDetail, addProduct, updateProduct, deleteProduct, updateProductStatus } from '@/api/product'
import { getCategoryTree } from '@/api/category'

// ---------- 搜索 ----------
const searchForm = reactive({
  name: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined
})

// ---------- 分类选项 ----------
const categoryOptions = ref<any[]>([])

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    if (res.code === 200) {
      categoryOptions.value = res.data || []
    }
  } catch {
    ElMessage.error('加载分类选项失败')
  }
}

// ---------- 表格 ----------
const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProductList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name || undefined,
      categoryId: searchForm.categoryId,
      status: searchForm.status
    })
    if (res.code === 200) {
      const data = res.data
      tableData.value = data.records || []
      pagination.total = data.total || 0
    } else {
      ElMessage.error(res.message || '获取商品列表失败')
    }
  } catch {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.categoryId = undefined
  searchForm.status = undefined
  pagination.page = 1
  fetchData()
}

const handleRefresh = () => {
  fetchData()
}

// ---------- 上下架 ----------
const handleStatusChange = async (row: any, status: number) => {
  try {
    const res = await updateProductStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success(status === 1 ? '已上架' : '已下架')
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

// ---------- 新增/编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增商品')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const editId = ref<number>()

const formData = reactive({
  name: '',
  categoryId: undefined as number | undefined,
  images: '',
  price: undefined as number | undefined,
  stock: 0,
  sort: 0,
  detail: '',
  status: 1
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const resetForm = () => {
  formData.name = ''
  formData.categoryId = undefined
  formData.images = ''
  formData.price = undefined
  formData.stock = 0
  formData.sort = 0
  formData.detail = ''
  formData.status = 1
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增商品'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑商品'
  try {
    const res = await getProductDetail(row.id)
    if (res.code === 200) {
      const data = res.data
      formData.name = data.name || ''
      formData.categoryId = data.categoryId
      formData.images = data.images || ''
      formData.price = data.price
      formData.stock = data.stock || 0
      formData.sort = data.sort || 0
      formData.detail = data.detail || ''
      formData.status = data.status ?? 1
    }
  } catch {
    ElMessage.error('获取商品详情失败')
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      let res
      if (isEdit.value && editId.value) {
        res = await updateProduct(editId.value, formData)
      } else {
        res = await addProduct(formData)
      }
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        fetchData()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch {
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// ---------- 删除 ----------
const handleDelete = async (row: any) => {
  try {
    const res = await deleteProduct(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

// ---------- 详情 ----------
const detailVisible = ref(false)
const detailData = ref<any>(null)

const handleView = async (row: any) => {
  try {
    const res = await getProductDetail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
      detailVisible.value = true
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch {
    ElMessage.error('获取详情失败')
  }
}

onMounted(() => {
  loadCategories()
  fetchData()
})
</script>

<style scoped>
.search-section {
  background: var(--bg-primary);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 0;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.action-section {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.pagination-section {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
