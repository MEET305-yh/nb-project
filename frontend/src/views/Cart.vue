<template>
  <div class="cart-page">
    <h2>购物车</h2>
    <el-table
      ref="tableRef"
      :data="cartItems"
      v-loading="loading"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="商品" width="300">
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
      <el-table-column label="数量" width="180">
        <template #default="{ row }">
          <el-input-number
            v-model="row.quantity"
            :min="1"
            :max="row.product?.stock || 9999"
            :disabled="(row.product?.stock || 0) === 0"
            @change="updateQuantity(row.id, row.quantity)"
            style="width: 120px"
          />
          <div v-if="(row.product?.stock || 0) === 0" class="stock-warning">
            库存不足
          </div>
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template #default="{ row }">
          ¥{{ (row.product?.price * row.quantity).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="removeItem(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="cart-footer">
      <div class="total">
        <span>总计: ¥{{ totalPrice.toFixed(2) }}</span>
      </div>
      <el-button type="primary" size="large" @click="checkout" :disabled="selectedItems.length === 0">
        去结算
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, updateCartQuantity, removeFromCart } from '@/api/cart'
import { getProduct } from '@/api/product'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartItems = ref([])
const loading = ref(false)
const selectedItems = ref([])
const tableRef = ref(null)

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    if (item?.product) {
      return sum + Number(item.product.price || 0) * item.quantity
    }
    return sum
  }, 0)
})

const loadCart = async () => {
  loading.value = true
  try {
    const res = await getCart()
    cartItems.value = res.data

    // 加载商品详情
    for (const item of cartItems.value) {
      try {
        const productRes = await getProduct(item.productId)
        item.product = productRes.data
      } catch (error) {
        console.error('加载商品失败', error)
      }
    }

    nextTick(() => {
      if (tableRef.value) {
        tableRef.value.clearSelection()
        cartItems.value.forEach(row => {
          tableRef.value.toggleRowSelection(row, true)
        })
      }
    })
  } catch (error) {
    console.error('加载购物车失败', error)
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (rows) => {
  selectedItems.value = rows
}

const updateQuantity = async (cartId, quantity) => {
  try {
    await updateCartQuantity(cartId, { quantity })
    ElMessage.success({
      message: '更新成功',
      duration: 1000
    })
    loadCart()
  } catch (error) {
    console.error('更新数量失败', error)
  }
}

const removeItem = async (cartId) => {
  try {
    await removeFromCart(cartId)
    ElMessage.success({
      message: '删除成功',
      duration: 1000
    })
    loadCart()
  } catch (error) {
    console.error('删除失败', error)
  }
}

const checkout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning({
      message: '请先选择需要结算的商品',
      duration: 1000
    })
    return
  }
  const payload = selectedItems.value.map(item => ({
    cartId: item.id,
    productId: item.productId,
    quantity: item.quantity,
    product: item.product
  }))
  sessionStorage.setItem('checkoutItems', JSON.stringify(payload))
  router.push('/checkout')
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
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

.cart-footer {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.stock-warning {
  margin-top: 8px;
  font-size: 12px;
  color: #f56c6c;
}
</style>




