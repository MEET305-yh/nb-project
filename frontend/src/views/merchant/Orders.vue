<template>
  <div class="admin-orders">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <el-card>
      <div class="search-bar">
        <el-input
          v-model="orderNo"
          placeholder="搜索订单号"
          clearable
          style="width: 200px"
        />
        <el-select v-model="status" placeholder="选择状态" clearable style="width: 150px">
          <el-option label="全部" value="" />
          <el-option label="待支付" value="PENDING_PAYMENT" />
          <el-option label="已支付" value="PAID" />
          <el-option label="已发货" value="SHIPPED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button type="primary" @click="loadOrders">搜索</el-button>
      </div>

      <el-table :data="orders" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="totalAmount" label="总金额">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收货人" />
        <el-table-column prop="receiverPhone" label="收货电话" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewOrder(row)">查看</el-button>
            <el-button
              v-if="row.status === 'PAID'"
              size="small"
              type="success"
              @click="shipOrder(row.id)"
            >
              发货
            </el-button>
            <el-button
              v-if="row.status !== 'COMPLETED' && row.status !== 'CANCELLED'"
              size="small"
              type="danger"
              @click="cancelOrder(row.id)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadOrders"
        @current-change="loadOrders"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="订单详情" width="800px">
      <div v-if="orderDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ orderDetail.order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ orderDetail.order.userId }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ orderDetail.order.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(orderDetail.order.status)">
              {{ getStatusName(orderDetail.order.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ orderDetail.order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="收货电话">{{ orderDetail.order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ orderDetail.order.shippingAddress }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ orderDetail.order.createTime }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px">订单项</h3>
        <el-table :data="orderDetail.items" style="width: 100%">
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="quantity" label="数量" />
          <el-table-column prop="price" label="单价">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="subtotal" label="小计">
            <template #default="{ row }">¥{{ row.subtotal }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getAdminOrders, getAdminOrder, shipOrder as shipOrderApi, cancelOrder as cancelOrderApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const orderNo = ref('')
const status = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showDetailDialog = ref(false)
const orderDetail = ref(null)

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (orderNo.value) params.orderNo = orderNo.value
    if (status.value) params.status = status.value

    const res = await getAdminOrders(params)
    orders.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载订单列表失败', error)
  } finally {
    loading.value = false
  }
}

const viewOrder = async (order) => {
  try {
    const res = await getAdminOrder(order.id)
    orderDetail.value = res.data
    showDetailDialog.value = true
  } catch (error) {
    console.error('加载订单详情失败', error)
  }
}

const shipOrder = async (id) => {
  try {
    await ElMessageBox.confirm('确定要发货吗？', '提示', {
      type: 'warning'
    })
    await shipOrderApi(id)
    ElMessage.success('发货成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败', error)
    }
  }
}

const cancelOrder = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      type: 'warning'
    })
    await cancelOrderApi(id)
    ElMessage.success('取消成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败', error)
    }
  }
}

const getStatusName = (status) => {
  const statusMap = {
    PENDING_PAYMENT: '待支付',
    PAID: '已支付',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    PENDING_PAYMENT: 'warning',
    PAID: 'info',
    SHIPPED: 'success',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return typeMap[status] || ''
}

watch([status], () => {
  currentPage.value = 1
  loadOrders()
})

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.admin-orders {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  gap: 10px;
}
</style>




