<template>
  <div class="orders-page">
    <h2>我的订单</h2>
    <el-tabs v-model="activeTab" @tab-change="loadOrders">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待支付" name="PENDING_PAYMENT" />
      <el-tab-pane label="已支付" name="PAID" />
      <el-tab-pane label="已发货" name="SHIPPED" />
      <el-tab-pane label="已完成" name="COMPLETED" />
    </el-tabs>

    <el-card v-for="order in filteredOrders" :key="order.id" class="order-card">
      <div class="order-header">
        <div>
          <span>订单号: {{ order.orderNo }}</span>
          <el-tag :type="getStatusType(order.status)" style="margin-left: 10px">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>
        <div class="order-time">{{ formatTime(order.createTime) }}</div>
      </div>
      <div class="order-content">
        <p>收货地址: {{ order.shippingAddress }}</p>
        <p>收货人: {{ order.receiverName }} {{ order.receiverPhone }}</p>
        <p class="total-amount">总金额: ¥{{ order.totalAmount }}</p>
      </div>
      <div class="order-items" v-if="order.items && order.items.length">
        <div class="order-item" v-for="item in order.items" :key="item.id">
          <div class="item-info">
            <img :src="item.productImage || '/placeholder.png'" class="item-thumb" />
            <div class="item-text">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-meta">
                <span>单价: ¥{{ item.price }}</span>
                <span style="margin-left: 10px">数量: {{ item.quantity }}</span>
              </div>
            </div>
          </div>
          <div class="item-right">
            <div class="item-subtotal">小计: ¥{{ item.subtotal }}</div>
            <el-button 
              v-if="order.status === 'COMPLETED' && !item.evaluated" 
              type="primary" 
              size="small"
              @click="openEvaluationDialog(order.id, item.productId, item.id)"
            >
              评价
            </el-button>
            <el-tag v-if="item.evaluated" type="success" size="small">已评价</el-tag>
          </div>
        </div>
      </div>
      <div class="order-actions">
        <el-button v-if="order.status === 'PENDING_PAYMENT'" type="primary" @click="payOrder(order.id)">
          支付
        </el-button>
        <el-button v-if="order.status === 'SHIPPED'" type="success" @click="completeOrder(order.id)">
          确认收货
        </el-button>
      </div>
    </el-card>

    <el-empty v-if="filteredOrders.length === 0" description="暂无订单" />

    <!-- 评价对话框 -->
    <el-dialog v-model="showEvaluationDialog" title="商品评价" width="600px">
      <el-form :model="evaluationForm" label-width="100px">
        <el-form-item label="评分" required>
          <el-rate v-model="evaluationForm.rating" :max="5" show-score />
        </el-form-item>
        <el-form-item label="评价内容" required>
          <el-input
            v-model="evaluationForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入您的评价..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            class="evaluation-uploader"
            list-type="picture-card"
            :file-list="evaluationImageList"
            :http-request="handleEvaluationImageUpload"
            :before-upload="beforeEvaluationImageUpload"
            :on-remove="handleEvaluationImageRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="uploader-tip">最多上传 6 张图片，每张不超过 5MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEvaluationDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEvaluation" :loading="evaluating">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getOrders, payOrder as payOrderApi, completeOrder as completeOrderApi } from '@/api/order'
import { createEvaluation, getProductEvaluations } from '@/api/evaluation'
import { uploadImage } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const orders = ref([])
const loading = ref(false)
const activeTab = ref('all')
const showEvaluationDialog = ref(false)
const evaluating = ref(false)
const evaluationForm = ref({
  orderId: null,
  productId: null,
  orderItemId: null,
  rating: 5,
  content: '',
  images: []
})
const evaluationImageList = ref([])

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') {
    return orders.value
  }
  return orders.value.filter(order => order.status === activeTab.value)
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getOrders()
    orders.value = res.data
    // 检查每个订单项是否已评价
    await checkEvaluations()
  } catch (error) {
    console.error('加载订单失败', error)
  } finally {
    loading.value = false
  }
}

// 检查订单项是否已评价
const checkEvaluations = async () => {
  // 只检查已完成的订单
  const completedOrders = orders.value.filter(order => order.status === 'COMPLETED' && order.items)
  if (completedOrders.length === 0) return

  // 收集所有需要检查的商品ID
  const productIds = new Set()
  completedOrders.forEach(order => {
    order.items.forEach(item => {
      if (item.productId) {
        productIds.add(item.productId)
      }
    })
  })

  // 并行获取所有商品的评价
  const evaluationPromises = Array.from(productIds).map(productId => 
    getProductEvaluations(productId).catch(() => ({ data: [] }))
  )

  try {
    const evaluationResults = await Promise.all(evaluationPromises)
    const evaluationsMap = new Map()
    
    // 构建商品ID到评价列表的映射
    Array.from(productIds).forEach((productId, index) => {
      evaluationsMap.set(productId, evaluationResults[index].data || [])
    })

    // 更新订单项的评价状态
    completedOrders.forEach(order => {
      order.items.forEach(item => {
        if (item.productId) {
          const evaluations = evaluationsMap.get(item.productId) || []
          // 检查当前订单是否已有评价
          item.evaluated = evaluations.some(ev => ev.orderId === order.id)
        } else {
          item.evaluated = false
        }
      })
    })
  } catch (error) {
    console.error('检查评价状态失败', error)
  }
}

// 打开评价对话框
const openEvaluationDialog = (orderId, productId, orderItemId) => {
  evaluationForm.value = {
    orderId,
    productId,
    orderItemId,
    rating: 5,
    content: '',
    images: []
  }
  evaluationImageList.value = []
  showEvaluationDialog.value = true
}

// 提交评价
const submitEvaluation = async () => {
  if (!evaluationForm.value.rating || evaluationForm.value.rating < 1) {
    ElMessage.warning('请选择评分')
    return
  }
  if (!evaluationForm.value.content || evaluationForm.value.content.trim() === '') {
    ElMessage.warning('请输入评价内容')
    return
  }

  evaluating.value = true
  const imageUrls = evaluationImageList.value.map(item => item.url).filter(Boolean)
  try {
    await createEvaluation({
      orderId: evaluationForm.value.orderId,
      productId: evaluationForm.value.productId,
      rating: evaluationForm.value.rating,
      content: evaluationForm.value.content.trim(),
      images: imageUrls
    })
    ElMessage.success('评价成功')
    showEvaluationDialog.value = false
    evaluationImageList.value = []
    // 重新加载订单以更新评价状态
    await loadOrders()
  } catch (error) {
    console.error('提交评价失败', error)
    ElMessage.error(error.response?.data?.message || '评价失败，请稍后再试')
  } finally {
    evaluating.value = false
  }
}

const beforeEvaluationImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  if (evaluationImageList.value.length >= 6) {
    ElMessage.warning('最多上传 6 张图片')
    return false
  }
  return true
}

const handleEvaluationImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const res = await uploadImage(file)
    if (res && res.code === 200 && res.data) {
      evaluationImageList.value.push({
        uid: Date.now(),
        name: file.name,
        url: res.data
      })
      ElMessage.success('图片上传成功')
      onSuccess && onSuccess(res.data)
    } else {
      throw new Error(res?.message || '图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败', error)
    ElMessage.error('图片上传失败，请重试')
    onError && onError(error)
  }
}

const handleEvaluationImageRemove = (file, fileList) => {
  evaluationImageList.value = fileList
}

const payOrder = async (orderId) => {
  try {
    await payOrderApi(orderId)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (error) {
    console.error('支付失败', error)
  }
}

const completeOrder = async (orderId) => {
  try {
    await completeOrderApi(orderId)
    ElMessage.success('订单已完成')
    loadOrders()
  } catch (error) {
    console.error('完成订单失败', error)
  }
}

const getStatusText = (status) => {
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

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.order-time {
  color: #909399;
  font-size: 14px;
}

.order-content {
  margin-bottom: 15px;
}

.order-content p {
  margin: 8px 0;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.order-items {
  border-top: 1px solid #f2f2f2;
  padding-top: 15px;
  margin-top: 10px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-thumb {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.item-name {
  font-weight: bold;
}

.item-meta {
  color: #909399;
  font-size: 13px;
  margin-top: 5px;
}

.item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.item-subtotal {
  font-weight: bold;
  color: #303133;
}

.order-actions {
  text-align: right;
}

.evaluation-uploader :deep(.el-upload) {
  width: 100px;
  height: 100px;
}

.uploader-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}
</style>




