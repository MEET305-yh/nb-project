<template>
  <div class="home">
    <el-carousel v-if="banners.length > 0" height="400px" indicator-position="outside">
      <el-carousel-item v-for="item in banners" :key="item.id">
        <div class="banner-item" style="margin: 0 auto; cursor: pointer;"
             :style="{height: '500px', width: '800px',  backgroundImage: `url(${item.image})` }"
             @click="handleBannerClick(item.categoryName)">
<!--          <div class="banner-content">-->
<!--            <h1>{{ item.title }}</h1>-->
<!--            <p>{{ item.description }}</p>-->
<!--          </div>-->
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="content">
      <!-- 分类专区 -->
      <div class="category-section">
        <h2>商品分类</h2>
        <el-row :gutter="20" v-loading="categoryLoading">
          <el-col :span="6" v-for="cat in categories" :key="cat.id">
            <el-card class="category-card" @click="goToCategory(cat.name)">
              <div 
                class="category-content"
                :style="cat.image ? { backgroundImage: `url(${cat.image})` } : {}"
              >
                <div class="category-overlay">
                  <div class="category-icon" v-if="!cat.image">
                    <el-icon :size="40"><Shop /></el-icon>
                  </div>
                  <h3>{{ cat.name }}</h3>
                  <p v-if="cat.description" class="category-desc">{{ cat.description }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <h2 style="margin-top: 40px">热门商品</h2>
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="6" v-for="product in products" :key="product.id">
          <el-card class="product-card" @click="goToDetail(product.id)">
            <img :src="product.image || '/placeholder.png'" class="product-image" />
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-footer">
                <span class="price">¥{{ product.price }}</span>
                <el-button type="primary" size="small" @click.stop="addToCart(product.id)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProducts, getAllCategories, getBanners } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Shop } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const products = ref([])
const loading = ref(false)
const categories = ref([])
const categoryLoading = ref(false)

const banners = ref([])

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getProducts({ current: 1, size: 8 })
    products.value = res.data.records
  } catch (error) {
    console.error('加载商品失败', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/products/${id}`)
}

const goToCategory = (categoryName) => {
  if (categoryName) {
    router.push({
      path: '/products',
      query: { category: categoryName }
    })
  }
}

const handleBannerClick = (categoryName) => {
  if (categoryName) {
    goToCategory(categoryName)
  }
}

const loadCategories = async () => {
  categoryLoading.value = true
  try {
    const res = await getAllCategories()
    if (res && res.code === 200 && res.data) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('加载分类失败', error)
  } finally {
    categoryLoading.value = false
  }
}

const loadBanners = async () => {
  try {
    const res = await getBanners()
    if (res && res.code === 200 && res.data && res.data.length > 0) {
      banners.value = res.data
    } else {
      // 如果没有数据，使用空数组（不显示轮播图）
      banners.value = []
    }
  } catch (error) {
    console.error('加载轮播图失败', error)
    banners.value = []
  }
}

const addToCart = async (productId) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await addToCartApi({ productId, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error('加入购物车失败', error)
  }
}

onMounted(() => {
  loadBanners()
  loadProducts()
  loadCategories()
})
</script>

<style scoped>
.home {
  min-height: calc(100vh - 60px);
}

.banner-item {
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-content {
  text-align: center;
  color: white;
  background: rgba(0, 0, 0, 0.5);
  padding: 40px;
  border-radius: 10px;
}

.banner-content h1 {
  font-size: 48px;
  margin-bottom: 20px;
}

.content {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}

.content h2 {
  margin-bottom: 20px;
  font-size: 28px;
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
  border-radius: 4px;
}

.product-info h3 {
  margin: 10px 0;
  font-size: 18px;
}

.product-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.category-section {
  margin-bottom: 40px;
}

.category-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  min-height: 180px;
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.category-content {
  text-align: center;
  padding: 20px 10px;
  min-height: 180px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  border-radius: 4px;
}

.category-overlay {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.9);
  padding: 20px 10px;
  border-radius: 4px;
  transition: background 0.3s;
}

.category-card:hover .category-overlay {
  background: rgba(255, 255, 255, 0.95);
}

.category-icon {
  margin-bottom: 15px;
  color: #409eff;
}

.category-content h3 {
  margin: 10px 0;
  font-size: 18px;
  color: #303133;
}

.category-desc {
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>


