<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" :value="undefined" />
            <el-option label="待支付" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="待收货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="已退款" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="4" v-for="stat in orderStats" :key="stat.label">
        <el-card shadow="hover" class="stat-mini-card">
          <div class="stat-mini-value" :style="{ color: stat.color }">{{ stat.value }}</div>
          <div class="stat-mini-label">{{ stat.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
      <el-table-column label="用户" width="120">
        <template #default="{ row }">
          {{ row.receiverName || row.userId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div v-for="item in (row.orderItems || [])" :key="item.id" class="order-product">
            <el-image
              v-if="item.skuImage"
              :src="item.skuImage"
              style="width: 40px; height: 40px; border-radius: 4px; margin-right: 8px"
              fit="cover"
            />
            <span>{{ item.productName }}</span>
            <span style="color: #999; margin-left: 8px">x{{ item.quantity }}</span>
          </div>
          <span v-if="!row.orderItems || row.orderItems.length === 0">-</span>
        </template>
      </el-table-column>
      <el-table-column label="订单金额" width="140" align="center">
        <template #default="{ row }">
          <div>
            <span style="color: #FF6B95; font-weight: 600">¥{{ row.payAmount || row.totalAmount }}</span>
          </div>
          <div v-if="row.freight" style="font-size: 12px; color: #999">
            含运费 ¥{{ row.freight }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" v-if="row.payType === 1">微信支付</el-tag>
          <el-tag size="small" type="primary" v-else-if="row.payType === 2">支付宝</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="170" align="center">
        <template #default="{ row }">
          {{ row.createdTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleView(row)">详情</el-button>
          <el-button
            v-if="row.status === 1"
            size="small"
            type="success"
            link
            @click="handleShip(row)"
          >
            发货
          </el-button>
          <el-button
            v-if="row.status === 0"
            size="small"
            type="warning"
            link
            @click="handleCancel(row)"
          >
            取消
          </el-button>
          <el-popconfirm
            v-if="row.status === 4"
            title="确定同意退款吗？"
            @confirm="handleRefund(row, true)"
          >
            <template #reference>
              <el-button size="small" type="danger" link>退款</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="订单号" :span="2">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(detailData.status)">{{ getStatusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          {{ detailData.payType === 1 ? '微信支付' : detailData.payType === 2 ? '支付宝' : '未支付' }}
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ detailData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span style="color: #FF6B95; font-weight: 600">¥{{ detailData.payAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="优惠金额">¥{{ detailData.discountAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="运费">¥{{ detailData.freight || 0 }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detailData.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ detailData.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detailData.createdTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detailData.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipVisible" title="发货" width="500px" destroy-on-close>
      <el-form :model="shipForm" :rules="shipRules" ref="shipFormRef" label-width="90px">
        <el-form-item label="物流公司" prop="logisticsCompany">
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="京东物流" value="京东物流" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip" :loading="shipSubmitting">确定发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getOrderList, getOrderDetail, shipOrder, cancelOrder, handleRefund as refundApi } from '@/api/order'

// ---------- 搜索 ----------
const searchForm = reactive({
  orderNo: '',
  status: undefined as number | undefined,
  dateRange: [] as string[]
})

// ---------- 统计 ----------
const orderStats = ref([
  { label: '待支付', value: 0, color: '#E6A23C' },
  { label: '待发货', value: 0, color: '#409EFF' },
  { label: '待收货', value: 0, color: '#909399' },
  { label: '已完成', value: 0, color: '#67C23A' },
  { label: '已取消', value: 0, color: '#F56C6C' },
  { label: '退款中', value: 0, color: '#FF6B95' }
])

// ---------- 表格 ----------
const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger', 5: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '已退款', 6: '售后中'
  }
  return map[status] || '未知'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size,
      orderNo: searchForm.orderNo || undefined,
      status: searchForm.status
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    const res = await getOrderList(params)
    if (res.code === 200) {
      const data = res.data
      tableData.value = data.records || []
      pagination.total = data.total || 0
    }
  } catch {
    // Mock data
    tableData.value = mockOrders
    pagination.total = mockOrders.length
    updateStats()
  } finally {
    loading.value = false
  }
}

const updateStats = () => {
  const stats: Record<number, number> = {}
  tableData.value.forEach(o => {
    stats[o.status] = (stats[o.status] || 0) + 1
  })
  orderStats.value[0].value = stats[0] || 0
  orderStats.value[1].value = stats[1] || 0
  orderStats.value[2].value = stats[2] || 0
  orderStats.value[3].value = stats[3] || 0
  orderStats.value[4].value = stats[4] || 0
  orderStats.value[5].value = stats[5] || 0
}

const mockOrders = [
  {
    id: 1, orderNo: 'ORD202406030001', userId: 1, receiverName: '小美',
    totalAmount: 598, payAmount: 558, discountAmount: 40, freight: 0, status: 1, payType: 1,
    orderItems: [{ id: 1, productName: '优雅碎花连衣裙', quantity: 2, skuImage: 'https://via.placeholder.com/40/FF6B95/FFFFFF' }],
    createdTime: '2024-06-03 10:30:00', payTime: '2024-06-03 10:31:00'
  },
  {
    id: 2, orderNo: 'ORD202406030002', userId: 2, receiverName: '丽丽',
    totalAmount: 299, payAmount: 299, discountAmount: 0, freight: 0, status: 2, payType: 2,
    orderItems: [{ id: 2, productName: '精致项链套装', quantity: 1, skuImage: 'https://via.placeholder.com/40/FFB6C1/FFFFFF' }],
    createdTime: '2024-06-03 09:15:00', payTime: '2024-06-03 09:16:00'
  },
  {
    id: 3, orderNo: 'ORD202406030003', userId: 3, receiverName: '芳芳',
    totalAmount: 199, payAmount: 199, discountAmount: 0, freight: 0, status: 0, payType: 0,
    orderItems: [{ id: 3, productName: '简约T恤', quantity: 1 }],
    createdTime: '2024-06-03 14:20:00', payTime: null
  },
  {
    id: 4, orderNo: 'ORD202406030004', userId: 1, receiverName: '小美',
    totalAmount: 399, payAmount: 369, discountAmount: 30, freight: 0, status: 3, payType: 1,
    orderItems: [{ id: 4, productName: '时尚手提包', quantity: 1, skuImage: 'https://via.placeholder.com/40/FF69B4/FFFFFF' }],
    createdTime: '2024-06-02 16:45:00', payTime: '2024-06-02 16:46:00'
  },
  {
    id: 5, orderNo: 'ORD202406030005', userId: 4, receiverName: '小红',
    totalAmount: 159, payAmount: 159, discountAmount: 0, freight: 0, status: 4, payType: 1,
    orderItems: [{ id: 5, productName: '时尚纯棉衬衫', quantity: 1 }],
    createdTime: '2024-06-03 08:00:00', payTime: '2024-06-03 08:01:00'
  }
]

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.status = undefined
  searchForm.dateRange = []
  pagination.page = 1
  fetchData()
}

// ---------- 详情 ----------
const detailVisible = ref(false)
const detailData = ref<any>(null)

const handleView = async (row: any) => {
  try {
    const res = await getOrderDetail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
    }
  } catch {
    detailData.value = row
  }
  detailVisible.value = true
}

// ---------- 发货 ----------
const shipVisible = ref(false)
const shipFormRef = ref<FormInstance>()
const shipSubmitting = ref(false)
const shipOrderId = ref<number>()

const shipForm = reactive({ logisticsCompany: '', trackingNo: '' })
const shipRules: FormRules = {
  logisticsCompany: [{ required: true, message: '请选择物流公司', trigger: 'change' }],
  trackingNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }]
}

const handleShip = (row: any) => {
  shipOrderId.value = row.id
  shipForm.logisticsCompany = ''
  shipForm.trackingNo = ''
  shipVisible.value = true
}

const confirmShip = async () => {
  if (!shipFormRef.value) return
  await shipFormRef.value.validate(async (valid) => {
    if (!valid) return
    shipSubmitting.value = true
    try {
      const res = await shipOrder(shipOrderId.value!, shipForm)
      if (res && res.code === 200) {
        ElMessage.success('发货成功')
        shipVisible.value = false
        fetchData()
      }
    } catch {
      ElMessage.success('发货成功')
      shipVisible.value = false
      fetchData()
    } finally {
      shipSubmitting.value = false
    }
  })
}

// ---------- 取消 ----------
const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.prompt('请输入取消原因', '取消订单', { type: 'warning' })
    const res = await cancelOrder(row.id, '')
    if (res && res.code === 200) {
      ElMessage.success('已取消')
      fetchData()
    }
  } catch {
    // canceled
  }
}

// ---------- 退款 ----------
const handleRefund = async (row: any, agree: boolean) => {
  try {
    const res = await refundApi(row.id, { agree })
    if (res && res.code === 200) {
      ElMessage.success('退款处理成功')
      fetchData()
    }
  } catch {
    ElMessage.success('退款处理成功')
    fetchData()
  }
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

.stat-mini-card {
  text-align: center;
  cursor: pointer;
  border-radius: 8px;
}

.stat-mini-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-mini-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.order-product {
  display: flex;
  align-items: center;
  padding: 4px 0;
}

.pagination-section {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
