<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="(stat, index) in stats" :key="index">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>销售趋势</span>
            </div>
          </template>
          <div ref="chartRef" style="width: 100%; height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>商品分类占比</span>
            </div>
          </template>
          <div ref="pieRef" style="width: 100%; height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新订单</span>
              <el-button type="primary" text @click="$router.push('/order/list')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="150" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">
                <span style="color: #FF6B95; font-weight: 600">¥{{ row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>热门商品</span>
              <el-button type="primary" text @click="$router.push('/product/list')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="hotProducts" style="width: 100%">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                <span style="color: #FF6B95; font-weight: 600">¥{{ row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sales" label="销量" width="100" />
            <el-table-column prop="stock" label="库存" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref<HTMLElement>()
const pieRef = ref<HTMLElement>()

const stats = [
  { label: '今日订单', value: 128, icon: 'Document', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { label: '今日销售额', value: '¥12,580', icon: 'Money', color: 'linear-gradient(135deg, #FF6B95 0%, #FF5580 100%)' },
  { label: '新增用户', value: 36, icon: 'User', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { label: '待处理订单', value: 15, icon: 'Bell', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
]

const recentOrders = [
  { orderNo: 'ORD202406010001', productName: '优雅碎花连衣裙', amount: 299, status: 1 },
  { orderNo: 'ORD202406010002', productName: '时尚纯棉衬衫', amount: 199, status: 0 },
  { orderNo: 'ORD202406010003', productName: '精致项链', amount: 159, status: 2 },
  { orderNo: 'ORD202406010004', productName: '口红礼盒', amount: 299, status: 3 },
  { orderNo: 'ORD202406010005', productName: '时尚手镯', amount: 199, status: 1 }
]

const hotProducts = [
  { name: '优雅碎花连衣裙', price: 299, sales: 156, stock: 85 },
  { name: '时尚纯棉衬衫', price: 199, sales: 123, stock: 120 },
  { name: '精致项链', price: 159, sales: 89, stock: 45 },
  { name: '口红礼盒', price: 299, sales: 145, stock: 60 },
  { name: '时尚手提包', price: 399, sales: 67, stock: 30 }
]

const getStatusType = (status: number) => {
  const map: Record<number, any> = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success' }
  return map[status] || ''
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成' }
  return map[status] || ''
}

const initLineChart = () => {
  if (!chartRef.value) return
  const chart = echarts.init(chartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '销售额'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: [82, 93, 90, 104, 126, 130, 128],
        itemStyle: { color: '#FF6B95' }
      },
      {
        name: '销售额',
        type: 'bar',
        data: [8200, 9320, 9010, 9340, 12900, 13300, 12580],
        itemStyle: { color: '#667eea' }
      }
    ]
  }
  chart.setOption(option)
}

const initPieChart = () => {
  if (!pieRef.value) return
  const chart = echarts.init(pieRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '分类占比',
        type: 'pie',
        radius: '70%',
        data: [
          { value: 35, name: '连衣裙' },
          { value: 25, name: '衬衫' },
          { value: 18, name: 'T恤' },
          { value: 12, name: '首饰' },
          { value: 10, name: '彩妆' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  chart.setOption(option)
}

onMounted(() => {
  initLineChart()
  initPieChart()
})
</script>

<style scoped>
.stat-card {
  border-radius: 12px;
  overflow: hidden;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>