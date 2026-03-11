<template>
  <div class="checkout-page" v-loading="loading">
    <h2>订单结算</h2>

    <el-row :gutter="20">
      <el-col :span="14">
        <el-card>
          <template #header>
            <span>收货地址</span>
          </template>
          <div v-if="addresses.length === 0">
            <el-empty description="暂无地址，请先在个人中心添加地址" />
            <el-button type="primary" @click="goToProfile">前往个人中心</el-button>
          </div>
          <el-radio-group v-else v-model="selectedAddressId" class="address-group">
            <el-radio
              v-for="address in addresses"
              :key="address.id"
              :label="address.id"
              class="address-radio"
            >
              <div class="address-item">
                <div class="address-header">
                  <span class="receiver">{{ address.receiverName }}</span>
                  <span class="phone">{{ address.receiverPhone }}</span>
                  <el-tag v-if="address.isDefault === 1" size="small" type="success">默认</el-tag>
                </div>
                <div class="address-detail">{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}</div>
              </div>
            </el-radio>
          </el-radio-group>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>
            <span>商品信息</span>
          </template>
          <el-table :data="checkoutItems" border>
            <el-table-column label="商品" min-width="200">
              <template #default="{ row }">
                <div class="product-cell">
                  <img :src="row.product?.image || '/placeholder.png'" class="product-thumb" />
                  <span>{{ row.product?.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="{ row }">¥{{ row.product?.price }}</template>
            </el-table-column>
            <el-table-column label="数量" width="100">
              <template #default="{ row }">{{ row.quantity }}</template>
            </el-table-column>
            <el-table-column label="小计" width="140">
              <template #default="{ row }">¥{{ (row.product?.price * row.quantity).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card class="summary-card">
          <template #header>
            <span>结算信息</span>
          </template>
          <div class="summary-row">
            <span>商品件数</span>
            <span>{{ itemCount }} 件</span>
          </div>
          <div class="summary-row">
            <span>商品总额</span>
            <span>¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <el-divider />
          <div class="summary-total">
            <span>应付金额</span>
            <span class="pay-amount">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            style="width: 100%; margin-top: 20px"
            :disabled="!canPay"
            :loading="paying"
            @click="handlePay"
          >
            {{ step === 1 ? '确认支付' : '已支付' }}
          </el-button>
          <el-alert
            v-if="paySuccess"
            title="支付成功！我们已经通知商家尽快发货～"
            type="success"
            :closable="false"
            show-icon
            style="margin-top: 20px"
          />
        </el-card>
        <el-card v-if="paySuccess" style="margin-top: 20px">
          <el-result
            icon="success"
            title="支付完成"
            :sub-title="`订单号：${orderInfo?.orderNo || '-'}，可在订单列表查看进度`"
          >
            <template #extra>
              <el-button type="primary" @click="goToOrders">查看订单</el-button>
            </template>
          </el-result>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAddresses } from '@/api/address'
import { createOrder, payOrder, getOrder } from '@/api/order'

const router = useRouter()
const checkoutItems = ref([])
const addresses = ref([])
const selectedAddressId = ref(null)
const loading = ref(false)
const paying = ref(false)
const paySuccess = ref(false)
const orderInfo = ref(null)
const step = ref(0)
const cartIds = ref([])
const pendingOrderId = ref(null) // 待支付订单ID

const totalAmount = computed(() =>
  checkoutItems.value.reduce((sum, item) => {
    if (item.product) {
      return sum + Number(item.product.price || 0) * item.quantity
    }
    return sum
  }, 0)
)

const itemCount = computed(() =>
  checkoutItems.value.reduce((sum, item) => sum + item.quantity, 0)
)

const canPay = computed(() =>
  checkoutItems.value.length > 0 && !!selectedAddressId.value && step.value === 1
)

const loadCheckoutItems = () => {
  const raw = sessionStorage.getItem('checkoutItems')
  if (!raw) {
    ElMessage.warning({
      message: '请先选择商品再结算',
      duration: 1000
    })
    router.push('/cart')
    return
  }
  try {
    checkoutItems.value = JSON.parse(raw)
    if (!checkoutItems.value || checkoutItems.value.length === 0) {
      ElMessage.warning({
        message: '商品信息无效',
        duration: 1000
      })
      router.push('/cart')
      return
    }
    // 检查是否是立即购买模式
    const isBuyNow = checkoutItems.value.some(item => item.isBuyNow)
    if (isBuyNow) {
      // 立即购买模式，不需要 cartIds
      cartIds.value = []
    } else {
      // 从购物车结算模式
      cartIds.value = checkoutItems.value.map(item => item.cartId || item.id).filter(id => id !== undefined && id !== null)
    }
    step.value = 1
    
    // 检查是否有待支付订单
    const savedOrderId = sessionStorage.getItem('pendingOrderId')
    if (savedOrderId) {
      pendingOrderId.value = Number(savedOrderId)
    }
  } catch (error) {
    console.error('解析商品信息失败', error)
    ElMessage.error({
      message: '商品信息格式错误',
      duration: 1000
    })
    router.push('/cart')
  }
}

const loadAddresses = async () => {
  try {
    const res = await getAddresses()
    addresses.value = res.data || []
    if (addresses.value.length > 0) {
      const defaultAddr = addresses.value.find(addr => addr.isDefault === 1)
      selectedAddressId.value = defaultAddr ? defaultAddr.id : addresses.value[0].id
      // 地址加载完成后，如果有商品信息，创建待支付订单
      if (checkoutItems.value.length > 0 && selectedAddressId.value) {
        createPendingOrder()
      }
    }
  } catch (error) {
    console.error('加载地址失败', error)
  }
}

// 创建待支付订单
const createPendingOrder = async () => {
  // 检查是否已有待支付订单
  const savedOrderId = sessionStorage.getItem('pendingOrderId')
  if (savedOrderId) {
    pendingOrderId.value = Number(savedOrderId)
    try {
      // 验证订单是否还存在且是待支付状态
      const orderRes = await getOrder(pendingOrderId.value)
      if (orderRes.data && orderRes.data.status === 'PENDING_PAYMENT') {
        orderInfo.value = orderRes.data
        return
      } else {
        // 订单已支付或不存在，清除
        sessionStorage.removeItem('pendingOrderId')
        pendingOrderId.value = null
      }
    } catch (error) {
      // 订单不存在，清除
      sessionStorage.removeItem('pendingOrderId')
      pendingOrderId.value = null
    }
  }

  // 如果没有待支付订单，创建新订单（订单状态默认为 PENDING_PAYMENT）
  if (!pendingOrderId.value && checkoutItems.value.length > 0 && selectedAddressId.value) {
    try {
      const isBuyNow = checkoutItems.value.some(item => item.isBuyNow)
      const orderData = {
        addressId: selectedAddressId.value
      }
      
      if (isBuyNow) {
        orderData.items = checkoutItems.value.map(item => ({
          productId: Number(item.productId || item.product?.id),
          quantity: Number(item.quantity || 1)
        }))
      } else {
        orderData.cartIds = cartIds.value.map(id => Number(id))
      }
      
      const createRes = await createOrder(orderData)
      if (createRes.data && createRes.data.id) {
        pendingOrderId.value = createRes.data.id
        orderInfo.value = createRes.data
        sessionStorage.setItem('pendingOrderId', createRes.data.id.toString())
      }
    } catch (error) {
      console.error('创建待支付订单失败', error)
      // 创建失败不影响页面显示，用户仍可以手动支付
    }
  }
}

const handlePay = async () => {
  if (!canPay.value) {
    ElMessage.warning({
      message: '请先选择地址和商品',
      duration: 1000
    })
    return
  }
  
  if (!selectedAddressId.value) {
    ElMessage.warning({
      message: '请选择收货地址',
      duration: 1000
    })
    return
  }
  
  if (checkoutItems.value.length === 0) {
    ElMessage.warning({
      message: '没有可结算的商品',
      duration: 1000
    })
    return
  }
  
  paying.value = true
  try {
    let orderId = pendingOrderId.value
    
    // 如果没有待支付订单，先创建订单
    if (!orderId) {
      // 检查是否是立即购买模式
      const isBuyNow = checkoutItems.value.some(item => item.isBuyNow)
      const orderData = {
        addressId: selectedAddressId.value
      }
      
      if (isBuyNow) {
        // 立即购买模式：传递商品信息
        orderData.items = checkoutItems.value.map(item => {
          const productId = item.productId || item.product?.id || item.productId
          const quantity = item.quantity || 1
          
          if (!productId) {
            throw new Error('商品ID缺失')
          }
          if (!quantity || quantity < 1) {
            throw new Error('商品数量无效')
          }
          
          return {
            productId: Number(productId),
            quantity: Number(quantity)
          }
        })
        
        if (orderData.items.length === 0) {
          throw new Error('没有有效的商品信息')
        }
      } else {
        // 从购物车结算模式：传递购物车ID
        if (cartIds.value.length === 0) {
          throw new Error('购物车ID缺失')
        }
        orderData.cartIds = cartIds.value.map(id => Number(id))
      }
      
      const createRes = await createOrder(orderData)
      if (!createRes.data || !createRes.data.id) {
        throw new Error('订单创建失败')
      }
      
      orderId = createRes.data.id
      orderInfo.value = createRes.data
    } else {
      // 使用已有的待支付订单
      const orderRes = await getOrder(orderId)
      orderInfo.value = orderRes.data
    }
    
    // 支付订单
    await payOrder(orderId)
    paySuccess.value = true
    step.value = 2
    sessionStorage.removeItem('checkoutItems')
    sessionStorage.removeItem('pendingOrderId')
    pendingOrderId.value = null
    ElMessage.success({
      message: '支付成功',
      duration: 1000
    })
  } catch (error) {
    console.error('支付失败', error)
    const errorMsg = error.response?.data?.message || error.message || '支付失败，请稍后再试'
    ElMessage.error({
      message: errorMsg,
      duration: 1000
    })
  } finally {
    paying.value = false
  }
}

const goToOrders = () => {
  router.push('/orders')
}

const goToProfile = () => {
  router.push('/profile')
}

// 页面离开时提示
onBeforeRouteLeave((to, from, next) => {
  // 如果有待支付订单且未支付成功，显示提示
  if (pendingOrderId.value && !paySuccess.value) {
    ElMessage.info({
      message: '订单已加入待支付列表，可在"我的订单"中查看并继续支付',
      duration: 1000
    })
  }
  next()
})

onMounted(async () => {
  await loadCheckoutItems()
  await loadAddresses()
})
</script>

<style scoped>
.checkout-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.address-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-radio {
  width: 100%;
  margin: 0;
}

.address-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
  width: 100%;
}

.address-header {
  display: flex;
  gap: 10px;
  align-items: center;
  font-weight: bold;
}

.address-detail {
  color: #606266;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.summary-card {
  position: sticky;
  top: 80px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #606266;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  font-size: 18px;
  font-weight: bold;
}

.pay-amount {
  color: #f56c6c;
}
</style>




