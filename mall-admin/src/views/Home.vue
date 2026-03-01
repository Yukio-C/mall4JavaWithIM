<template>
  <div class="home-container">
    <!-- 1. 数据概览卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="item in overview" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-title">{{ item.title }}</div>
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-footer">
              较昨日 <span :class="item.trend > 0 ? 'up' : 'down'">{{ Math.abs(item.trend) }}%</span>
            </div>
          </div>
          <el-icon class="stat-icon" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 2. 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never" header="热销商品排行 (Top 10)">
          <div id="salesChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="售后分布排行 (售后频次最高)">
          <div id="afterSaleChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="never" header="商品口碑排行 (综合评价得分)">
          <div id="ratingChart" class="chart-box rating-chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { api } from '../api'
import * as echarts from 'echarts'
import { ShoppingCart, Money, User, ChatDotSquare } from '@element-plus/icons-vue'

const overview = ref([
  { title: '今日订单', value: '128', trend: 12.5, icon: ShoppingCart, color: '#409EFF' },
  { title: '今日销售额', value: '¥12,840', trend: 8.2, icon: Money, color: '#67C23A' },
  { title: '活跃用户', value: '1,024', trend: -2.4, icon: User, color: '#E6A23C' },
  { title: '待处理咨询', value: '12', trend: 5.0, icon: ChatDotSquare, color: '#F56C6C' }
])

let charts: echarts.ECharts[] = []

const initSalesChart = (data: any[]) => {
  const chart = echarts.init(document.getElementById('salesChart')!)
  chart.setOption({
    tooltip: { 
      trigger: 'axis', 
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const item = params[0]
        return `${item.name}<br/>销量: ${item.value}`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', boundaryGap: [0, 0.01] },
    yAxis: { 
      type: 'category', 
      data: data.map(i => i.title).reverse(),
      axisLabel: {
        formatter: (value: string) => value.length > 8 ? value.substring(0, 8) + '...' : value
      }
    },
    series: [
      {
        name: '销量',
        type: 'bar',
        data: data.map(i => i.sales).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#1890ff' },
            { offset: 1, color: '#36cfc9' }
          ])
        }
      }
    ]
  })
  charts.push(chart)
}

const initAfterSaleChart = (data: any[]) => {
  const chart = echarts.init(document.getElementById('afterSaleChart')!)
  chart.setOption({
    tooltip: { 
      trigger: 'item',
      formatter: '{b}: {c} 件 ({d}%)' 
    },
    series: [
      {
        name: '售后件数',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { 
          show: false, 
          position: 'center',
          formatter: '{b}'
        },
        emphasis: { 
          label: { 
            show: true, 
            fontSize: '14', 
            fontWeight: 'bold',
            formatter: (params: any) => {
              const name = params.name
              return name.length > 10 ? name.substring(0, 10) + '\n' + name.substring(10, 20) + '...' : name
            }
          } 
        },
        data: data.map(i => ({ value: 1, name: i.title }))
      }
    ]
  })
  charts.push(chart)
}

const initRatingChart = (data: any[]) => {
  const chart = echarts.init(document.getElementById('ratingChart')!)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { 
      type: 'category', 
      data: data.map(i => i.title),
      axisLabel: {
        formatter: (value: string) => value.length > 6 ? value.substring(0, 6) + '...' : value
      }
    },
    yAxis: { type: 'value', min: 0, max: 5 },
    series: [{
      data: data.map(i => i.rating),
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255, 144, 0, 0.5)' },
          { offset: 1, color: 'rgba(255, 144, 0, 0)' }
        ])
      },
      itemStyle: { color: '#ff9000' }
    }]
  })
  charts.push(chart)
}

const fetchData = async () => {
  try {
    const [sales, afterSales, ratings] = await Promise.all([
      api.getSalesStats(),
      api.getAfterSaleStats(),
      api.getRatingStats()
    ])
    initSalesChart(sales)
    initAfterSaleChart(afterSales)
    initRatingChart(ratings)
  } catch (e) {
    console.error('统计数据加载失败', e)
  }
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

const handleResize = () => charts.forEach(c => c.resize())

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped lang="scss">
.home-container {
  padding: 10px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  .stat-content {
    .stat-title { font-size: 14px; color: #999; margin-bottom: 10px; }
    .stat-value { font-size: 28px; font-weight: bold; color: #333; margin-bottom: 10px; }
    .stat-footer { font-size: 12px; color: #999;
      .up { color: #67C23A; font-weight: bold; }
      .down { color: #F56C6C; font-weight: bold; }
    }
  }
  .stat-icon {
    position: absolute;
    right: 20px;
    top: 25px;
    font-size: 48px;
    opacity: 0.15;
  }
}

.chart-box {
  height: 350px;
  width: 100%;
}

.rating-chart {
  height: 300px;
}
</style>
