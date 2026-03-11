<template>
  <div class="statistics-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据统计</span>
          <div class="time-selector">
            <el-button
              :type="selectedDays === 1 ? 'primary' : ''"
              @click="changeTimeRange(1)"
            >
              昨日
            </el-button>
            <el-button
              :type="selectedDays === 7 ? 'primary' : ''"
              @click="changeTimeRange(7)"
            >
              近7日
            </el-button>
            <el-button
              :type="selectedDays === 30 ? 'primary' : ''"
              @click="changeTimeRange(30)"
            >
              近30日
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-card-content">
              <div class="stat-label">总营业额</div>
              <div class="stat-value">¥{{ formatNumber(totalRevenue) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-card-content">
              <div class="stat-label">总订单数</div>
              <div class="stat-value">{{ totalOrders }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-card-content">
              <div class="stat-label">平均每日营业额</div>
              <div class="stat-value">¥{{ formatNumber(avgDailyRevenue) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-card-content">
              <div class="stat-label">平均每日订单数</div>
              <div class="stat-value">{{ avgDailyOrders }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 营业额统计图 -->
      <el-card style="margin-bottom: 20px">
        <template #header>
          <span>营业额统计</span>
        </template>
        <div ref="revenueChartRef" style="width: 100%; height: 400px"></div>
      </el-card>

      <!-- 订单数统计图 -->
      <el-card style="margin-bottom: 20px">
        <template #header>
          <span>订单数统计</span>
        </template>
        <div ref="orderCountChartRef" style="width: 100%; height: 400px"></div>
      </el-card>

      <!-- 综合统计图 -->
      <el-card style="margin-bottom: 20px">
        <template #header>
          <span>综合统计</span>
        </template>
        <div ref="combinedChartRef" style="width: 100%; height: 400px"></div>
      </el-card>

      <!-- 商品销量统计图 -->
      <el-card>
        <template #header>
          <span>商品销量统计（TOP 10）</span>
        </template>
        <div ref="productSalesChartRef" style="width: 100%; height: 400px"></div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'
import { getStatistics, getProductSales } from '@/api/admin'
import { ElMessage } from 'element-plus'

const selectedDays = ref(7)
const revenueChartRef = ref(null)
const orderCountChartRef = ref(null)
const combinedChartRef = ref(null)
const productSalesChartRef = ref(null)

let revenueChart = null
let orderCountChart = null
let combinedChart = null
let productSalesChart = null

const statisticsData = ref({
  dateLabels: [],
  revenue: [],
  orderCount: [],
  totalRevenue: 0,
  totalOrders: 0
})

const productSalesData = ref({
  productNames: [],
  sales: [],
  totalSales: 0
})

const totalRevenue = computed(() => {
  return statisticsData.value.totalRevenue || 0
})

const totalOrders = computed(() => {
  return statisticsData.value.totalOrders || 0
})

const avgDailyRevenue = computed(() => {
  if (selectedDays.value === 0) return 0
  return (totalRevenue.value / selectedDays.value).toFixed(2)
})

const avgDailyOrders = computed(() => {
  if (selectedDays.value === 0) return 0
  return Math.round(totalOrders.value / selectedDays.value)
})

const formatNumber = (num) => {
  if (typeof num === 'string') {
    num = parseFloat(num)
  }
  return num.toFixed(2)
}

const changeTimeRange = (days) => {
  selectedDays.value = days
  loadStatistics()
  loadProductSales()
}

const loadStatistics = async () => {
  try {
    const res = await getStatistics(selectedDays.value)
    if (res.code === 200) {
      // 确保数据格式正确
      const data = res.data
      statisticsData.value = {
        dateLabels: data.dateLabels || [],
        revenue: data.revenue || [],
        orderCount: data.orderCount || [],
        totalRevenue: parseFloat(data.totalRevenue || 0),
        totalOrders: parseInt(data.totalOrders || 0)
      }
      updateCharts()
    } else {
      ElMessage.error(res.msg || '加载统计数据失败')
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
    ElMessage.error('加载统计数据失败')
  }
}

const loadProductSales = async () => {
  try {
    const res = await getProductSales(selectedDays.value)
    if (res.code === 200) {
      const data = res.data
      productSalesData.value = {
        productNames: data.productNames || [],
        sales: data.sales || [],
        totalSales: parseInt(data.totalSales || 0)
      }
      updateProductSalesChart()
    } else {
      ElMessage.error(res.msg || '加载商品销量数据失败')
    }
  } catch (error) {
    console.error('加载商品销量数据失败', error)
    ElMessage.error('加载商品销量数据失败')
  }
}

const initCharts = () => {
  // 初始化营业额图表
  if (revenueChartRef.value) {
    revenueChart = echarts.init(revenueChartRef.value)
  }

  // 初始化订单数图表
  if (orderCountChartRef.value) {
    orderCountChart = echarts.init(orderCountChartRef.value)
  }

  // 初始化综合图表
  if (combinedChartRef.value) {
    combinedChart = echarts.init(combinedChartRef.value)
  }

  // 初始化商品销量图表
  if (productSalesChartRef.value) {
    productSalesChart = echarts.init(productSalesChartRef.value)
  }
}

const updateCharts = () => {
  const { dateLabels, revenue, orderCount } = statisticsData.value

  // 营业额折线图
  if (revenueChart) {
    const revenueValues = revenue.map(r => parseFloat(r))
    revenueChart.setOption({
      title: {
        text: '营业额趋势',
        left: 'center',
        textStyle: {
          fontSize: 16
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params) => {
          return `${params[0].name}<br/>营业额: ¥${formatNumber(params[0].value)}`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dateLabels
      },
      yAxis: {
        type: 'value',
        name: '营业额(元)',
        axisLabel: {
          formatter: (value) => {
            return '¥' + value
          }
        }
      },
      series: [
        {
          name: '营业额',
          type: 'line',
          smooth: true,
          data: revenueValues,
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                {
                  offset: 0,
                  color: 'rgba(64, 158, 255, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(64, 158, 255, 0.1)'
                }
              ]
            }
          }
        }
      ]
    })
  }

  // 订单数柱状图
  if (orderCountChart) {
    orderCountChart.setOption({
      title: {
        text: '订单数趋势',
        left: 'center',
        textStyle: {
          fontSize: 16
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params) => {
          return `${params[0].name}<br/>订单数: ${params[0].value}`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: dateLabels
      },
      yAxis: {
        type: 'value',
        name: '订单数'
      },
      series: [
        {
          name: '订单数',
          type: 'bar',
          data: orderCount,
          itemStyle: {
            color: '#67C23A'
          }
        }
      ]
    })
  }

  // 综合统计图（双Y轴）
  if (combinedChart) {
    const revenueValues = revenue.map(r => parseFloat(r))
    combinedChart.setOption({
      title: {
        text: '营业额与订单数对比',
        left: 'center',
        textStyle: {
          fontSize: 16
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        },
        formatter: (params) => {
          let result = params[0].name + '<br/>'
          params.forEach(param => {
            if (param.seriesName === '营业额') {
              result += `${param.seriesName}: ¥${formatNumber(param.value)}<br/>`
            } else {
              result += `${param.seriesName}: ${param.value}<br/>`
            }
          })
          return result
        }
      },
      legend: {
        data: ['营业额', '订单数'],
        top: 30
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dateLabels
      },
      yAxis: [
        {
          type: 'value',
          name: '营业额(元)',
          position: 'left',
          axisLabel: {
            formatter: (value) => {
              return '¥' + value
            }
          }
        },
        {
          type: 'value',
          name: '订单数',
          position: 'right'
        }
      ],
      series: [
        {
          name: '营业额',
          type: 'line',
          smooth: true,
          data: revenueValues,
          itemStyle: {
            color: '#409EFF'
          },
          yAxisIndex: 0
        },
        {
          name: '订单数',
          type: 'bar',
          data: orderCount,
          itemStyle: {
            color: '#67C23A'
          },
          yAxisIndex: 1
        }
      ]
    })
  }
}

const updateProductSalesChart = () => {
  const { productNames, sales } = productSalesData.value

  if (productSalesChart) {
    productSalesChart.setOption({
      title: {
        text: '商品销量排行',
        left: 'center',
        textStyle: {
          fontSize: 16
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: (params) => {
          return `${params[0].name}<br/>销量: ${params[0].value}`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        name: '销量'
      },
      yAxis: {
        type: 'category',
        data: productNames,
        axisLabel: {
          interval: 0,
          formatter: (value) => {
            // 如果商品名称太长，截断并显示省略号
            if (value.length > 10) {
              return value.substring(0, 10) + '...'
            }
            return value
          }
        }
      },
      series: [
        {
          name: '销量',
          type: 'bar',
          data: sales,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#2378f7' },
                { offset: 0.7, color: '#2378f7' },
                { offset: 1, color: '#83bff6' }
              ])
            }
          },
          label: {
            show: true,
            position: 'right',
            formatter: '{c}'
          }
        }
      ]
    })
  }
}

const resizeCharts = () => {
  if (revenueChart) revenueChart.resize()
  if (orderCountChart) orderCountChart.resize()
  if (combinedChart) combinedChart.resize()
  if (productSalesChart) productSalesChart.resize()
}

onMounted(() => {
  initCharts()
  loadStatistics()
  loadProductSales()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  if (revenueChart) revenueChart.dispose()
  if (orderCountChart) orderCountChart.dispose()
  if (combinedChart) combinedChart.dispose()
  if (productSalesChart) productSalesChart.dispose()
})
</script>

<style scoped>
.statistics-page {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-selector {
  display: flex;
  gap: 10px;
}

.stat-card {
  text-align: center;
}

.stat-card-content {
  padding: 10px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
</style>

