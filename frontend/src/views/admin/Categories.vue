<template>
  <div class="admin-categories">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="showDialog = true; editingCategory = null">
        <el-icon><Plus /></el-icon>
        添加分类
      </el-button>
    </div>

    <el-card>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索分类名称"
          clearable
          style="width: 300px"
          @keyup.enter="loadCategories"
        />
        <el-button type="primary" @click="loadCategories">搜索</el-button>
      </div>

      <el-table :data="categories" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="背景图片" width="150">
          <template #default="{ row }">
            <img v-if="row.image" :src="row.image" style="width: 120px; height: 80px; object-fit: cover; border-radius: 4px" />
            <span v-else style="color: #909399">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="description" label="分类描述" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editCategory(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleCategoryStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteCategory(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadCategories"
        @current-change="loadCategories"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="editingCategory ? '编辑分类' : '添加分类'"
      width="500px"
    >
      <el-form :model="categoryForm" label-width="100px">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="背景图片">
          <el-upload
            class="avatar-uploader"
            :http-request="handleImageUpload"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
          >
            <img v-if="categoryForm.image" :src="categoryForm.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="排序顺序">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" style="width: 100%" />
          <div style="color: #909399; font-size: 12px; margin-top: 5px">数字越小越靠前</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getAdminCategories,
  createAdminCategory,
  updateAdminCategory,
  deleteAdminCategory as deleteCategoryApi,
  updateCategoryStatus,
  uploadImage
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const categories = ref([])
const loading = ref(false)
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showDialog = ref(false)
const editingCategory = ref(null)
const categoryForm = ref({
  name: '',
  description: '',
  image: '',
  sortOrder: 0
})

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await getAdminCategories({
      current: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    })
    categories.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载分类列表失败', error)
  } finally {
    loading.value = false
  }
}

const editCategory = (category) => {
  editingCategory.value = category
  categoryForm.value = {
    name: category.name,
    description: category.description || '',
    image: category.image || '',
    sortOrder: category.sortOrder || 0
  }
  showDialog.value = true
}

const saveCategory = async () => {
  if (!categoryForm.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    if (editingCategory.value) {
      await updateAdminCategory(editingCategory.value.id, categoryForm.value)
      ElMessage.success('更新成功')
    } else {
      await createAdminCategory(categoryForm.value)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    editingCategory.value = null
    categoryForm.value = {
      name: '',
      description: '',
      image: '',
      sortOrder: 0
    }
    loadCategories()
  } catch (error) {
    console.error('保存分类失败', error)
  }
}

const deleteCategory = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个分类吗？删除后相关商品的分类信息不会改变。', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await deleteCategoryApi(id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      loadCategories()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败', error)
      ElMessage.error(error.response?.data?.message || error.message || '删除失败，请稍后重试')
    }
  }
}

const toggleCategoryStatus = async (category) => {
  const nextStatus = category.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '上架' : '下架'
  try {
    await updateCategoryStatus(category.id, nextStatus)
    ElMessage.success(`${actionText}成功`)
    loadCategories()
  } catch (error) {
    console.error(`${actionText}分类失败`, error)
    ElMessage.error(error.response?.data?.message || error.message || `${actionText}失败，请稍后重试`)
  }
}

// 图片上传相关
const handleImageUpload = async (options) => {
  const { file } = options
  try {
    const res = await uploadImage(file)
    if (res && res.code === 200 && res.data) {
      categoryForm.value.image = res.data
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

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.admin-categories {
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

