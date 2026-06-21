<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
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

    <!-- 图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card v-loading="loading">
          <template #header>
            <div class="card-header"><span>销售趋势（近7天）</span></div>
          </template>
          <div ref="chartRef" style="width: 100%; height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card v-loading="loading">
          <template #header>
            <div class="card-header"><span>商品分类占比</span></div>
          </template>
          <div ref="pieRef" style="width: 100%; height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新订单</span>
              <el-button type="primary" text @click="$router.push('/order/list')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="productName" label="商品名称" show-overflow-tooltip />
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
          <el-table :data="hotProducts" style="width: 100%" v-loading="loading">
            <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
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
import { ref, reactive, watch, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getDashboard } from '@/api/dashboard'

const loading = ref(false)
const chartRef = ref<HTMLElement>()
const pieRef = ref<HTMLElement>()
let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// ── 数据 ──
const stats = ref([
  { label: '今日订单', value: '0', icon: 'Document', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { label: '今日销售额', value: '¥0', icon: 'Money', color: 'linear-gradient(135deg, #FF6B95 0%, #FF5580 100%)' },
  { label: '新增用户', value: '0', icon: 'User', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { label: '待处理订单', value: '0', icon: 'Bell', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
])

const recentOrders = ref<any[]>([])
const hotProducts = ref<any[]>([])

// 图表数据缓存
const trendDates = ref<string[]>([])
const trendOrders = ref<number[]>([])
const trendSales = ref<number[]>([])
const pieData = ref<{ value: number; name: string }[]>([])

// ── 状态映射 ──
const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger', 5: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '已退款' }
  return map[status] || '未知'
}

// ── 数据加载 ──
const fetchDashboard = async () => {
  loading.value = true
  try {
    const res = await getDashboard()
    if (res.code === 200) {
      const d = res.data

      // 统计卡片
      const s = d.stats || {}
      stats.value[0].value = String(s.todayOrders ?? 0)
      stats.value[1].value = '¥' + (s.todaySales ?? 0).toLocaleString()
      stats.value[2].value = String(s.newUsers ?? 0)
      stats.value[3].value = String(s.pendingOrders ?? 0)

      // 最新订单
      recentOrders.value = d.recentOrders || []

      // 热门商品
      hotProducts.value = d.hotProducts || []

      // 图表数据
      const trend = d.salesTrend || []
      trendDates.value = trend.map((t: any) => t.date)
      trendOrders.value = trend.map((t: any) => t.orders)
      trendSales.value = trend.map((t: any) => t.sales)
      pieData.value = d.categoryDistribution || []

      // 渲染图表
      await nextTick()
      renderLineChart()
      renderPieChart()
    }
  } catch {
    ElMessage.error('获取首页数据失败')
  } finally {
    loading.value = false
  }
}

// ── 图表渲染 ──
const renderLineChart = () => {
  if (!chartRef.value) return
  if (!lineChart) lineChart = echarts.init(chartRef.value)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '销售额'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: trendDates.value },
    yAxis: { type: 'value' },
    series: [
      {
        name: '订单数', type: 'line', smooth: true, data: trendOrders.value,
        itemStyle: { color: '#FF6B95' }
      },
      {
        name: '销售额', type: 'bar', data: trendSales.value,
        itemStyle: { color: '#667eea' }
      }
    ]
  })
}

const renderPieChart = () => {
  if (!pieRef.value) return
  if (!pieChart) pieChart = echarts.init(pieRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '分类占比', type: 'pie', radius: '70%', data: pieData.value,
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
}

onMounted(() => {
  fetchDashboard()
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
