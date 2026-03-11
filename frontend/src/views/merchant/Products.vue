<template>
  <div class="admin-products">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>

    <el-card>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索商品名称"
          clearable
          style="width: 200px"
        />
        <el-select v-model="category" placeholder="选择分类" clearable style="width: 150px">
          <el-option label="全部" value="" />
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.name"
          />
        </el-select>
        <el-select v-model="status" placeholder="选择状态" clearable style="width: 150px">
          <el-option label="全部" value="" />
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="loadProducts">搜索</el-button>
      </div>

      <el-table :data="products" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <img :src="row.image || '/placeholder.png'" style="width: 60px; height: 60px; object-fit: cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="category" label="分类" />
        <el-table-column prop="price" label="价格">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editProduct(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteProduct(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadProducts"
        @current-change="loadProducts"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="editingProduct ? '编辑商品' : '添加商品'"
      width="800px"
    >
      <el-form :model="productForm" label-width="100px">
        <el-form-item label="商品名称" required>
          <el-input v-model="productForm.name" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="productForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="商品标签">
          <el-input
            v-model="productForm.tags"
            placeholder="例如：产地直供, 时令优选（多个标签用逗号隔开）"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            这些标签会显示在前台商品卡片上，用来突出特色卖点
          </div>
        </el-form-item>
        <el-form-item label="商品参数">
          <el-input 
            v-model="productForm.specifications" 
            type="textarea" 
            :rows="5"
            placeholder='请输入商品参数，格式：{"品牌":"xxx","产地":"xxx","规格":"xxx"} 或直接输入文本'
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            支持JSON格式或纯文本格式
          </div>
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="productForm.category" style="width: 100%" placeholder="请选择分类">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="productForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" required>
          <el-input-number v-model="productForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品主图">
          <el-upload
            class="avatar-uploader"
            :http-request="handleImageUpload"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
          >
            <img v-if="productForm.image" :src="productForm.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="详情图片">
          <el-upload
            class="detail-uploader"
            :http-request="handleDetailImageUpload"
            :show-file-list="true"
            :before-upload="beforeImageUpload"
            :file-list="detailImageList"
            list-type="picture-card"
            :on-remove="handleDetailImageRemove"
          >
            <el-icon class="detail-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            可上传多张图片，用于商品详情页展示
          </div>
        </el-form-item>
        <el-form-item label="图文详情">
          <el-input 
            v-model="productForm.detailText" 
            type="textarea" 
            :rows="8"
            placeholder="请输入商品图文详情，支持HTML格式或纯文本"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            支持HTML格式或纯文本，用于商品详情页展示
          </div>
        </el-form-item>
        <el-form-item label="生长环境图片">
          <el-upload
            class="detail-uploader"
            :http-request="handleEnvImageUpload"
            :show-file-list="true"
            :before-upload="beforeImageUpload"
            :file-list="environmentImageList"
            list-type="picture-card"
            :on-remove="handleEnvImageRemove"
          >
            <el-icon class="detail-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            展示农产品生长环境的实景照片，支持多张图片
          </div>
        </el-form-item>
        <el-form-item label="生长环境视频">
          <el-upload
            class="detail-uploader"
            :http-request="handleEnvVideoUpload"
            :show-file-list="false"
            :before-upload="beforeVideoUpload"
          >
            <template #trigger>
              <el-button type="primary" plain>
                {{ productForm.environmentVideo ? '重新上传视频' : '上传视频' }}
              </el-button>
            </template>
            <template #tip>
              <div style="color: #909399; font-size: 12px; margin-top: 5px">
                支持上传 mp4 等视频文件，大小不超过 200MB；上传后会自动生成播放地址
              </div>
            </template>
          </el-upload>
          <div v-if="productForm.environmentVideo" style="margin-top: 10px">
            <video
              :src="productForm.environmentVideo"
              controls
              style="width: 100%; border-radius: 8px"
            />
          </div>
        </el-form-item>
        <el-form-item label="商家ID">
          <el-input-number v-model="productForm.merchantId" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getAdminProducts, getAdminProduct, createAdminProduct, updateAdminProduct, deleteAdminProduct as deleteProductApi, updateProductStatus, getAllCategories, uploadImage, uploadVideo } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const products = ref([])
const loading = ref(false)
const keyword = ref('')
const category = ref('')
const status = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showDialog = ref(false)
const editingProduct = ref(null)
const categories = ref([])
const productForm = ref({
  name: '',
  description: '',
  tags: '',
  specifications: '',
  category: '',
  price: 0,
  stock: 0,
  image: '',
  detailImages: '',
  detailText: '',
  environmentImages: '',
  environmentVideo: '',
  merchantId: null
})

const detailImageList = ref([])
const environmentImageList = ref([])

const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (keyword.value) params.keyword = keyword.value
    if (category.value) params.category = category.value
    if (status.value !== '') params.status = status.value

    const res = await getAdminProducts(params)
    products.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载商品列表失败', error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  editingProduct.value = null
  productForm.value = {
    name: '',
    description: '',
    tags: '',
    specifications: '',
    category: '',
    price: 0,
    stock: 0,
    image: '',
    detailImages: '',
    detailText: '',
    environmentImages: '',
    environmentVideo: '',
    merchantId: null
  }
  detailImageList.value = []
  environmentImageList.value = []
  loadCategories()
  showDialog.value = true
}

const editProduct = async (product) => {
  editingProduct.value = product
  try {
    const res = await getAdminProduct(product.id)
    const detail = res?.data || product
    productForm.value = {
      name: detail.name,
      description: detail.description || '',
      tags: detail.tags || '',
      specifications: detail.specifications || '',
      category: detail.category || '',
      price: detail.price,
      stock: detail.stock,
      image: detail.image || '',
      detailImages: detail.detailImages || '',
      detailText: detail.detailText || '',
      environmentImages: detail.environmentImages || '',
      environmentVideo: detail.environmentVideo || '',
      merchantId: detail.merchantId
    }
    if (detail.detailImages) {
      const images = detail.detailImages.split(',').map(img => img.trim()).filter(Boolean)
      detailImageList.value = images.map((url, index) => ({
        uid: `${detail.id || 'detail'}-${index}`,
        name: url.split('/').pop() || `detail-${index}.jpg`,
        status: 'finished',
        url
      }))
    } else {
      detailImageList.value = []
    }
    if (detail.environmentImages) {
      const images = detail.environmentImages.split(',').map(img => img.trim()).filter(Boolean)
      environmentImageList.value = images.map((url, index) => ({
        uid: `${detail.id || 'env'}-${index}`,
        name: url.split('/').pop() || `env-${index}.jpg`,
        status: 'finished',
        url
      }))
    } else {
      environmentImageList.value = []
    }
  } catch (error) {
    console.error('加载商品详情失败', error)
    productForm.value = {
      name: product.name,
      description: product.description || '',
      tags: product.tags || '',
      specifications: product.specifications || '',
      category: product.category || '',
      price: product.price,
      stock: product.stock,
      image: product.image || '',
      detailImages: product.detailImages || '',
      detailText: product.detailText || '',
      environmentImages: product.environmentImages || '',
      environmentVideo: product.environmentVideo || '',
      merchantId: product.merchantId
    }
    if (product.detailImages) {
      const images = product.detailImages.split(',').map(img => img.trim()).filter(Boolean)
      detailImageList.value = images.map((url, index) => ({
        uid: `${product.id || 'detail'}-${index}`,
        name: url.split('/').pop() || `detail-${index}.jpg`,
        status: 'finished',
        url
      }))
    } else {
      detailImageList.value = []
    }
    if (product.environmentImages) {
      const images = product.environmentImages.split(',').map(img => img.trim()).filter(Boolean)
      environmentImageList.value = images.map((url, index) => ({
        uid: `${product.id || 'env'}-${index}`,
        name: url.split('/').pop() || `env-${index}.jpg`,
        status: 'finished',
        url
      }))
    } else {
      environmentImageList.value = []
    }
  }
  loadCategories()
  showDialog.value = true
}

const saveProduct = async () => {
  if (!productForm.value.name) {
    ElMessage.warning('请输入商品名称')
    return
  }

  const detailImageUrls = detailImageList.value.map(item => item.url).join(',')
  productForm.value.detailImages = detailImageUrls
  const envImageUrls = environmentImageList.value.map(item => item.url).join(',')
  productForm.value.environmentImages = envImageUrls

  try {
    if (editingProduct.value) {
      await updateAdminProduct(editingProduct.value.id, productForm.value)
      ElMessage.success('更新成功')
    } else {
      await createAdminProduct(productForm.value)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    editingProduct.value = null
    productForm.value = {
      name: '',
      description: '',
      tags: '',
      specifications: '',
      category: '',
      price: 0,
      stock: 0,
      image: '',
      detailImages: '',
      detailText: '',
      environmentImages: '',
      environmentVideo: '',
      merchantId: null
    }
    detailImageList.value = []
    environmentImageList.value = []
    loadProducts()
  } catch (error) {
    console.error('保存商品失败', error)
  }
}

const toggleStatus = async (product) => {
  try {
    const newStatus = product.status === 1 ? 0 : 1
    await updateProductStatus(product.id, newStatus)
    ElMessage.success('状态更新成功')
    loadProducts()
  } catch (error) {
    console.error('更新状态失败', error)
  }
}

const deleteProduct = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await deleteProductApi(id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      loadProducts()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败', error)
      ElMessage.error(error.response?.data?.message || error.message || '删除失败，请稍后重试')
    }
  }
}

const loadCategories = async () => {
  try {
    const res = await getAllCategories()
    if (res && res.code === 200 && res.data) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('加载分类列表失败', error)
  }
}

const handleImageUpload = async (options) => {
  const { file } = options
  try {
    const res = await uploadImage(file)
    if (res && res.code === 200 && res.data) {
      productForm.value.image = res.data
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res?.message || '图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败', error)
    ElMessage.error(error.response?.data?.message || '图片上传失败，请重试')
  }
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

const handleDetailImageUpload = async (options) => {
  const { file } = options
  try {
    const res = await uploadImage(file)
    if (res && res.code === 200 && res.data) {
      detailImageList.value.push({
        uid: Date.now(),
        name: file.name,
        url: res.data
      })
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res?.message || '图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败', error)
    ElMessage.error(error.response?.data?.message || '图片上传失败，请重试')
  }
}

const handleDetailImageRemove = (file) => {
  const index = detailImageList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    detailImageList.value.splice(index, 1)
  }
}

const handleEnvImageUpload = async (options) => {
  const { file } = options
  try {
    const res = await uploadImage(file)
    if (res && res.code === 200 && res.data) {
      environmentImageList.value.push({
        uid: Date.now(),
        name: file.name,
        url: res.data
      })
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res?.message || '图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败', error)
    ElMessage.error(error.response?.data?.message || '图片上传失败，请重试')
  }
}

const handleEnvImageRemove = (file) => {
  const index = environmentImageList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    environmentImageList.value.splice(index, 1)
  }
}

const beforeVideoUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isLt200M = file.size / 1024 / 1024 < 200

  if (!isVideo) {
    ElMessage.error('只能上传视频文件!')
    return false
  }
  if (!isLt200M) {
    ElMessage.error('视频大小不能超过 200MB!')
    return false
  }
  return true
}

const handleEnvVideoUpload = async (options) => {
  const { file } = options
  try {
    const res = await uploadVideo(file)
    if (res && res.code === 200 && res.data) {
      productForm.value.environmentVideo = res.data
      ElMessage.success('视频上传成功')
    } else {
      ElMessage.error(res?.message || '视频上传失败')
    }
  } catch (error) {
    console.error('视频上传失败', error)
    ElMessage.error(error.response?.data?.message || '视频上传失败，请重试')
  }
}

watch([category, status], () => {
  currentPage.value = 1
  loadProducts()
})

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<style scoped>
.admin-products {
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

.avatar-uploader {
  display: block;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 48px;
  color: #8c939d;
  width: 300px;
  height: 300px;
  text-align: center;
  line-height: 300px;
}

.avatar {
  width: 300px;
  height: 300px;
  display: block;
  object-fit: cover;
}

.detail-uploader {
  display: block;
}

.detail-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
</style>

