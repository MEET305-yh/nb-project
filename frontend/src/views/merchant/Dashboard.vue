<template>
  <div class="merchant-dashboard">
    <el-card class="welcome-card">
      <h2>商家欢迎您！</h2>
      <p>在这里可以管理商品、查看订单并及时处理发货。</p>
      <el-alert
        v-if="orderStats.pendingPayment > 0"
        :title="`您有 ${orderStats.pendingPayment} 个待支付订单，请及时关注。`"
        type="warning"
        show-icon
        style="margin-top: 15px"
      />
      <el-alert
        v-if="orderStats.paid > 0"
        :title="`您有 ${orderStats.paid} 个已支付待发货订单，请尽快安排发货！`"
        type="success"
        show-icon
        style="margin-top: 15px"
      />
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card>
          <div class="stat-title">商品数量</div>
          <div class="stat-value">{{ stats.productCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="stat-title">待发货订单</div>
          <div class="stat-value">{{ orderStats.paid || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="stat-title">待支付订单</div>
          <div class="stat-value">{{ orderStats.pendingPayment || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>今日订单提醒</span>
      </template>
      <el-timeline>
        <el-timeline-item v-for="item in reminders" :key="item.id" :timestamp="item.time" :type="item.type">
          {{ item.message }}
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { getAdminProducts, getOrderStats } from '@/api/admin'

const stats = reactive({
  productCount: 0
})

const orderStats = reactive({
  pendingPayment: 0,
  paid: 0
})

const reminders = ref([])

const loadProductCount = async () => {
  try {
    const res = await getAdminProducts({ current: 1, size: 1 })
    stats.productCount = res.data.total
  } catch (error) {
    console.error('加载商品数失败', error)
  }
}

const loadOrderStats = async () => {
  try {
    const res = await getOrderStats()
    Object.assign(orderStats, res.data || {})
    reminders.value = []
    if (orderStats.paid > 0) {
      reminders.value.push({
        id: 'paid',
        time: '刚刚',
        type: 'success',
        message: `您有 ${orderStats.paid} 个已支付待发货订单，请及时处理。`
      })
    }
    if (orderStats.pendingPayment > 0) {
      reminders.value.push({
        id: 'pending',
        time: '刚刚',
        type: 'warning',
        message: `还有 ${orderStats.pendingPayment} 个待支付订单，建议关注客户进度。`
      })
    }
    if (reminders.value.length === 0) {
      reminders.value.push({
        id: 'empty',
        time: '今天',
        type: 'info',
        message: '当前暂无新的订单动态。'
      })
    }
  } catch (error) {
    console.error('加载订单统计失败', error)
  }
}

onMounted(() => {
  loadProductCount()
  loadOrderStats()
})
</script>

<style scoped>
.merchant-dashboard {
  padding: 20px;
}

.welcome-card {
  text-align: left;
}

.stat-title {
  color: #909399;
  font-size: 14px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-top: 10px;
  color: #409eff;
}
</style>

