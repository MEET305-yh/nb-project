<template>
  <div class="product-detail" v-loading="loading">
    <div v-if="product" class="product-main">
      <el-row :gutter="40">
        <el-col :span="12">
          <div class="product-image-container">
            <img :src="product.image || '/placeholder.png'" class="product-image-large" />
          </div>
          <!-- 详情图片预览 -->
          <div v-if="detailImages.length > 0" class="detail-images-preview">
            <div class="detail-images-title">详情图片</div>
            <el-row :gutter="10">
              <el-col :span="6" v-for="(img, index) in detailImages" :key="index">
                <img 
                  :src="img" 
                  class="detail-thumbnail"
                  @click="previewImage(img)"
                />
              </el-col>
            </el-row>
          </div>
        </el-col>
        <el-col :span="12">
          <h1 class="product-name">{{ product.name }}</h1>
          <div class="product-price">¥{{ product.price }}</div>
          <div class="product-info">
            <p class="stock">库存: {{ product.stock }}</p>
            <p class="category">分类: {{ product.category }}</p>
          </div>
          <p class="description">{{ product.description }}</p>
          <div class="quantity-selector">
            <span>数量：</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" />
          </div>
          <div class="actions">
            <el-button type="primary" size="large" @click="addToCart">加入购物车</el-button>
            <el-button type="success" size="large" @click="buyNow">立即购买</el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-divider />

    <!-- 标签页内容 -->
    <el-tabs v-model="activeTab" class="product-tabs">
      <el-tab-pane label="用户评价" name="evaluations">
          <div class="evaluations-section">
          <div class="section-header">
            <h2>用户评价 · {{ evaluations.length }}</h2>
          </div>
          <div v-if="evaluations.length === 0" class="empty-state">
            <el-empty description="暂无评价" />
          </div>
            <el-card v-for="evaluation in evaluations" :key="evaluation.id" class="eval-card">
            <div class="eval-header">
                <el-avatar>{{ evaluation.userId }}</el-avatar>
              <div class="eval-info">
                <div class="eval-rating">
                    <el-rate v-model="evaluation.rating" disabled />
                </div>
                  <span class="eval-time">{{ formatTime(evaluation.createTime) }}</span>
              </div>
            </div>
              <p class="eval-content">{{ evaluation.content }}</p>
            <div class="eval-images" v-if="evaluation.imageList && evaluation.imageList.length">
              <el-image
                v-for="(img, idx) in evaluation.imageList"
                :key="idx"
                :src="img"
                :preview-src-list="evaluation.imageList"
                fit="cover"
                class="eval-image"
              />
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="参数信息" name="specifications">
        <div class="specifications-section">
          <div class="section-header">
            <h2>参数信息</h2>
          </div>
          <div v-if="!product?.specifications" class="empty-state">
            <el-empty description="暂无参数信息" />
          </div>
          <div v-else class="specifications-content">
            <template v-if="parsedSpecEntries.length">
              <el-descriptions :column="2" border>
                <el-descriptions-item
                  v-for="entry in parsedSpecEntries"
                  :key="entry.key"
                  :label="entry.key"
                >
                  {{ entry.value }}
                </el-descriptions-item>
              </el-descriptions>
            </template>
            <template v-else>
              <div class="spec-text" v-html="formatSpecText(product.specifications)"></div>
            </template>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="图文详情" name="details">
        <div class="details-section">
          <div class="section-header">
            <h2>图文详情</h2>
          </div>
          <div v-if="!product?.detailText && detailImages.length === 0" class="empty-state">
            <el-empty description="暂无图文详情" />
          </div>
          <div v-else class="details-content">
            <!-- 详情图片 -->
            <div v-if="detailImages.length > 0" class="detail-images-section">
              <div v-for="(img, index) in detailImages" :key="index" class="detail-image-item">
                <img :src="img" class="detail-image" />
              </div>
            </div>
            <!-- 详情文本 -->
            <div v-if="product?.detailText" class="detail-text" v-html="product.detailText"></div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="生长环境" name="environment">
        <div class="details-section">
          <div class="section-header">
            <h2>生长环境</h2>
          </div>
          <div
            v-if="!environmentImages.length && !product?.environmentVideo"
            class="empty-state"
          >
            <el-empty description="商家暂未上传生长环境信息" />
          </div>
          <div v-else class="details-content">
            <div v-if="environmentImages.length" class="detail-images-section">
              <div
                v-for="(img, index) in environmentImages"
                :key="index"
                class="detail-image-item"
              >
                <img :src="img" class="detail-image" />
              </div>
            </div>
            <div v-if="product?.environmentVideo" class="env-video-wrapper">
              <video
                :src="product.environmentVideo"
                controls
                controlslist="nodownload"
                style="width: 100%; border-radius: 8px; outline: none"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="本店推荐" name="recommended">
        <div class="recommended-section">
          <div class="section-header">
            <h2>本店推荐</h2>
          </div>
          <div v-if="recommendedProducts.length === 0" class="empty-state">
            <el-empty description="暂无推荐商品" />
          </div>
          <el-row :gutter="20" v-else>
            <el-col :span="6" v-for="item in recommendedProducts" :key="item.id">
              <el-card class="product-card" @click="goToDetail(item.id)">
                <img :src="item.image || '/placeholder.png'" class="product-image" />
                <div class="product-info">
                  <h3>{{ item.name }}</h3>
                  <div class="product-footer">
                    <span class="price">¥{{ item.price }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>

      <el-tab-pane label="看了又看" name="viewed">
        <div class="viewed-section">
          <div class="section-header">
            <h2>看了又看</h2>
          </div>
          <div v-if="viewedProducts.length === 0" class="empty-state">
            <el-empty description="暂无相关商品" />
          </div>
          <el-row :gutter="20" v-else>
            <el-col :span="6" v-for="item in viewedProducts" :key="item.id">
              <el-card class="product-card" @click="goToDetail(item.id)">
                <img :src="item.image || '/placeholder.png'" class="product-image" />
                <div class="product-info">
                  <h3>{{ item.name }}</h3>
                  <div class="product-footer">
                    <span class="price">¥{{ item.price }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="showImagePreview" width="80%">
      <img :src="previewImageUrl" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProduct, getProducts } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'
import { getProductEvaluations } from '@/api/evaluation'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref(null)
const evaluations = ref([])
const recommendedProducts = ref([])
const viewedProducts = ref([])
const loading = ref(false)
const quantity = ref(1)
const activeTab = ref('evaluations')
const showImagePreview = ref(false)
const previewImageUrl = ref('')

// 详情图片列表
const detailImages = computed(() => {
  if (!product.value?.detailImages) return []
  return product.value.detailImages.split(',').filter(img => img.trim())
})

// 生长环境图片列表
const environmentImages = computed(() => {
  if (!product.value?.environmentImages) return []
  return product.value.environmentImages.split(',').filter(img => img.trim())
})

// 解析参数信息
const isJsonSpecs = computed(() => {
  if (!product.value?.specifications) return false
  try {
    JSON.parse(product.value.specifications)
    return true
  } catch {
    return false
  }
})

const parsedSpecEntries = computed(() => {
  const specs = product.value?.specifications
  if (!specs) return []

  if (isJsonSpecs.value) {
    try {
      const obj = JSON.parse(specs)
      return Object.entries(obj)
        .filter(([key, value]) => key && value !== undefined && value !== null)
        .map(([key, value]) => ({
          key,
          value: Array.isArray(value) ? value.join(', ') : value
        }))
    } catch {
      return []
    }
  }

  // 处理“键：值”格式的多行文本
  const lines = specs.split(/\n+/)
  const entries = []
  lines.forEach(line => {
    const trimmed = line.trim()
    if (!trimmed) return
    const match = trimmed.split(/[:：]/)
    if (match.length >= 2) {
      const key = match.shift()?.trim()
      const value = match.join(':').trim()
      if (key && value) {
        entries.push({ key, value })
      }
    }
  })
  return entries
})

const loadProduct = async () => {
  loading.value = true
  try {
    const res = await getProduct(route.params.id)
    product.value = res.data
    loadEvaluations()
    loadRecommendedProducts()
    loadViewedProducts()
  } catch (error) {
    console.error('加载商品失败', error)
    ElMessage.error({
      message: '商品不存在或已下架',
      duration: 1000
    })
  } finally {
    loading.value = false
  }
}

const loadEvaluations = async () => {
  try {
    const needLoginForRequest = !userStore.isAuthenticated
    const res = await getProductEvaluations(route.params.id, {
      // When the user is not logged in, keep evaluation viewing public:
      // - don't attach Authorization token
      // - don't force redirect to login on 401
      skipAuthToken: needLoginForRequest,
      skipAuthRedirect: needLoginForRequest
    })
    evaluations.value = (res.data || []).map(ev => ({
      ...ev,
      imageList: ev.images
        ? ev.images.split(',').map(img => img.trim()).filter(Boolean)
        : []
    }))
  } catch (error) {
    console.error('加载评价失败', error)
    evaluations.value = []
  }
}

// 加载本店推荐（同分类的其他商品）
const loadRecommendedProducts = async () => {
  if (!product.value?.merchantId) {
    recommendedProducts.value = []
    return
  }
  try {
    const res = await getProducts({
      current: 1,
      size: 50 // 拉取更多数据以便筛选
    })
    // 仅保留同一商家且排除当前商品
    recommendedProducts.value = (res.data.records || [])
      .filter(item => item.id !== product.value.id && item.merchantId === product.value.merchantId)
      .slice(0, 4)
  } catch (error) {
    console.error('加载推荐商品失败', error)
    recommendedProducts.value = []
  }
}

// 加载看了又看（其他分类的商品）
const loadViewedProducts = async () => {
  try {
    const res = await getProducts({ 
      current: 1, 
      size: 4
    })
    // 排除当前商品和同分类商品
    viewedProducts.value = (res.data.records || []).filter(
      item => item.id !== product.value.id && item.category !== product.value.category
    ).slice(0, 4)
  } catch (error) {
    console.error('加载相关商品失败', error)
    viewedProducts.value = []
  }
}

const addToCart = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    router.push({
      path: '/login',
      query: { redirect: router.currentRoute.value.fullPath }
    })
    return
  }

  try {
    await addToCartApi({ productId: product.value.id, quantity: quantity.value })
    ElMessage.success({
      message: '已加入购物车',
      duration: 1000
    })
  } catch (error) {
    console.error('加入购物车失败', error)
  }
}

const buyNow = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning({
      message: '请先登录',
      duration: 1000
    })
    router.push({
      path: '/login',
      query: { redirect: '/checkout' }
    })
    return
  }
  
  // 检查库存
  if (product.value.stock < quantity.value) {
    ElMessage.warning({
      message: '库存不足',
      duration: 1000
    })
    return
  }
  
  // 将商品信息保存到 sessionStorage，标记为立即购买
  const buyNowItem = {
    productId: product.value.id,
    product: {
      id: product.value.id,
      name: product.value.name,
      image: product.value.image,
      price: product.value.price
    },
    quantity: quantity.value,
    isBuyNow: true // 标记为立即购买
  }
  
  sessionStorage.setItem('checkoutItems', JSON.stringify([buyNowItem]))
  // 跳转到结算页面
  router.push('/checkout')
}

const goToDetail = (id) => {
  router.push(`/products/${id}`)
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

const formatSpecText = (text) => {
  if (!text) return ''
  // 如果是纯文本，保持换行
  return text.replace(/\n/g, '<br>')
}

const previewImage = (url) => {
  previewImageUrl.value = url
  showImagePreview.value = true
}

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
.product-detail {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.product-main {
  margin-bottom: 40px;
}

.product-image-container {
  margin-bottom: 20px;
}

.product-image-large {
  width: 100%;
  height: 500px;
  object-fit: cover;
  border-radius: 8px;
}

.detail-images-preview {
  margin-top: 20px;
}

.detail-images-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.detail-thumbnail {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s;
}

.detail-thumbnail:hover {
  transform: scale(1.05);
}

.product-name {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
}

.product-price {
  font-size: 36px;
  color: #f56c6c;
  font-weight: bold;
  margin: 20px 0;
}

.product-info {
  margin: 20px 0;
}

.stock, .category {
  color: #909399;
  margin: 10px 0;
  font-size: 14px;
}

.description {
  line-height: 1.8;
  margin: 30px 0;
  color: #606266;
}

.quantity-selector {
  margin: 30px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.actions {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}

.product-tabs {
  margin-top: 40px;
}

.section-header {
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.evaluations-section {
  margin-top: 20px;
}

.eval-card {
  margin-bottom: 20px;
}

.eval-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.eval-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.eval-time {
  color: #909399;
  font-size: 14px;
}

.eval-content {
  margin-top: 10px;
  line-height: 1.6;
  color: #606266;
}

.eval-images {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.eval-image {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  object-fit: cover;
}

.specifications-content {
  margin-top: 20px;
}

.spec-text {
  line-height: 1.8;
  color: #606266;
}

.details-content {
  margin-top: 20px;
}

.detail-images-section {
  margin-bottom: 30px;
}

.detail-image-item {
  margin-bottom: 20px;
}

.detail-image {
  width: 100%;
  border-radius: 8px;
}

.env-video-wrapper {
  margin-top: 20px;
}

.detail-text {
  line-height: 1.8;
  color: #606266;
  margin-top: 20px;
}

.detail-text :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 10px 0;
}

.recommended-section,
.viewed-section {
  margin-top: 20px;
}

.product-card {
  cursor: pointer;
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info h3 {
  margin: 10px 0;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}
</style>
