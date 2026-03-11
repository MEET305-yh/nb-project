<template>
  <div class="products-page">
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品"
        clearable
        @keyup.enter="loadProducts"
        style="width: 300px; margin-right: 10px"
      />
      <el-select v-model="category" placeholder="选择分类" clearable style="width: 150px; margin-right: 10px">
        <el-option label="全部" value="" />
        <el-option
          v-for="cat in categoryList"
          :key="cat.id"
          :label="cat.name"
          :value="cat.name"
        />
      </el-select>
      <el-button type="primary" @click="loadProducts">搜索</el-button>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6" v-for="product in products" :key="product.id">
        <el-card class="product-card" @click="goToDetail(product.id)">
          <img :src="product.image || '/placeholder.png'" class="product-image" />
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-footer">
              <span class="price">¥{{ product.price }}</span>
              <span class="stock">库存: {{ product.stock }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[12, 24, 48]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadProducts"
      @current-change="loadProducts"
      style="margin-top: 20px; justify-content: center"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getProducts, getAllCategories } from '@/api/product'

const router = useRouter()
const route = useRoute()
const products = ref([])
const loading = ref(false)
const keyword = ref('')
const category = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const categoryList = ref([])

const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (keyword.value) params.keyword = keyword.value
    if (category.value) params.category = category.value

    const res = await getProducts(params)
    products.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载商品失败', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/products/${id}`)
}

const loadCategories = async () => {
  try {
    const res = await getAllCategories()
    if (res && res.code === 200 && res.data) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

watch([category], () => {
  currentPage.value = 1
  loadProducts()
})

onMounted(() => {
  // 从路由参数中获取分类
  if (route.query.category) {
    category.value = route.query.category
  }
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.products-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
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
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.stock {
  color: #909399;
  font-size: 14px;
}
</style>


