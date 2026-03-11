<template>
  <div class="admin-banners">
    <div class="page-header">
      <h2>轮播图管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加轮播图
      </el-button>
    </div>

    <el-card>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索分类名称"
          clearable
          style="width: 200px"
        />
        <el-select v-model="status" placeholder="选择状态" clearable style="width: 150px">
          <el-option label="全部" value="" />
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="loadBanners">搜索</el-button>
      </div>

      <el-table :data="banners" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="轮播图" width="200">
          <template #default="{ row }">
            <img :src="row.image || '/placeholder.png'" style="width: 150px; height: 80px; object-fit: cover; border-radius: 4px" />
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editBanner(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteBanner(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadBanners"
        @current-change="loadBanners"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="editingBanner ? '编辑轮播图' : '添加轮播图'"
      width="600px"
    >
      <el-form :model="bannerForm" label-width="100px">
        <el-form-item label="轮播图图片" required>
          <el-upload
            class="avatar-uploader"
            :http-request="handleImageUpload"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
          >
            <img v-if="bannerForm.image" :src="bannerForm.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="分类名称" required>
          <el-select v-model="bannerForm.categoryName" style="width: 100%" placeholder="请选择分类">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序顺序">
          <el-input-number v-model="bannerForm.sortOrder" :min="0" style="width: 100%" />
          <div style="color: #909399; font-size: 12px; margin-top: 5px">数字越小越靠前</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="bannerForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveBanner">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import {
  getAdminBanners,
  createAdminBanner,
  updateAdminBanner,
  deleteAdminBanner as deleteBannerApi,
  updateBannerStatus,
  getAllCategories,
  uploadImage
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const banners = ref([])
const loading = ref(false)
const keyword = ref('')
const status = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showDialog = ref(false)
const editingBanner = ref(null)
const categories = ref([])
const bannerForm = ref({
  image: '',
  categoryName: '',
  sortOrder: 0,
  status: 1
})

const loadBanners = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (keyword.value) params.keyword = keyword.value
    if (status.value !== '') params.status = status.value

    const res = await getAdminBanners(params)
    banners.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载轮播图列表失败', error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  editingBanner.value = null
  bannerForm.value = {
    image: '',
    categoryName: '',
    sortOrder: 0,
    status: 1
  }
  loadCategories()
  showDialog.value = true
}

const editBanner = (banner) => {
  editingBanner.value = banner
  bannerForm.value = {
    image: banner.image || '',
    categoryName: banner.categoryName || '',
    sortOrder: banner.sortOrder || 0,
    status: banner.status !== undefined ? banner.status : 1
  }
  loadCategories()
  showDialog.value = true
}

const saveBanner = async () => {
  if (!bannerForm.value.image) {
    ElMessage.warning('请上传轮播图图片')
    return
  }
  if (!bannerForm.value.categoryName) {
    ElMessage.warning('请选择分类名称')
    return
  }

  try {
    if (editingBanner.value) {
      await updateAdminBanner(editingBanner.value.id, bannerForm.value)
      ElMessage.success('更新成功')
    } else {
      await createAdminBanner(bannerForm.value)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    editingBanner.value = null
    bannerForm.value = {
      image: '',
      categoryName: '',
      sortOrder: 0,
      status: 1
    }
    loadBanners()
  } catch (error) {
    console.error('保存轮播图失败', error)
  }
}

const toggleStatus = async (banner) => {
  try {
    const newStatus = banner.status === 1 ? 0 : 1
    await updateBannerStatus(banner.id, newStatus)
    ElMessage.success('状态更新成功')
    loadBanners()
  } catch (error) {
    console.error('更新状态失败', error)
  }
}

const deleteBanner = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个轮播图吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await deleteBannerApi(id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      loadBanners()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除轮播图失败', error)
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

// 图片上传相关
const handleImageUpload = async (options) => {
  const { file } = options
  try {
    const res = await uploadImage(file)
    if (res && res.code === 200 && res.data) {
      bannerForm.value.image = res.data
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

watch([status], () => {
  currentPage.value = 1
  loadBanners()
})

onMounted(() => {
  loadBanners()
  loadCategories()
})
</script>

<style scoped>
.admin-banners {
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
  width: 400px;
  height: 250px;
  text-align: center;
  line-height: 250px;
}

.avatar {
  width: 400px;
  height: 250px;
  display: block;
  object-fit: cover;
}
</style>

