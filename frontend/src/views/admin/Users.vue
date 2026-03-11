<template>
  <div class="admin-users">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showDialog = true; editingUser = null">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>

    <el-card>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名、昵称、手机号"
          clearable
          style="width: 300px"
          @keyup.enter="loadUsers"
        />
        <el-button type="primary" @click="loadUsers">搜索</el-button>
      </div>

      <el-table :data="users" v-loading="loading" style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editUser(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteUser(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadUsers"
        @current-change="loadUsers"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="editingUser ? '编辑用户' : '添加用户'"
      width="500px"
    >
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="userForm.username" :disabled="!!editingUser" />
        </el-form-item>
        <el-form-item label="密码" :required="!editingUser">
          <el-input v-model="userForm.password" type="password" :placeholder="editingUser ? '留空则不修改密码' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminUsers, createAdminUser, updateAdminUser, deleteAdminUser as deleteUserApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const users = ref([])
const loading = ref(false)
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showDialog = ref(false)
const editingUser = ref(null)
const userForm = ref({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  role: 'USER'
})

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAdminUsers({
      current: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    })
    users.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载用户列表失败', error)
  } finally {
    loading.value = false
  }
}

const editUser = (user) => {
  editingUser.value = user
  userForm.value = {
    username: user.username,
    password: '',
    nickname: user.nickname || '',
    phone: user.phone || '',
    email: user.email || '',
    role: user.role
  }
  showDialog.value = true
}

const saveUser = async () => {
  if (!userForm.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!editingUser.value && !userForm.value.password) {
    ElMessage.warning('请输入密码')
    return
  }

  try {
    let res
    if (editingUser.value) {
      res = await updateAdminUser(editingUser.value.id, userForm.value)
      if (res && res.code === 200) {
        ElMessage.success('更新成功')
      } else {
        ElMessage.error(res?.message || '更新失败')
        return
      }
    } else {
      res = await createAdminUser(userForm.value)
      if (res && res.code === 200) {
        ElMessage.success('创建成功')
      } else {
        ElMessage.error(res?.message || '创建失败')
        return
      }
    }
    showDialog.value = false
    editingUser.value = null
    userForm.value = {
      username: '',
      password: '',
      nickname: '',
      phone: '',
      email: '',
      role: 'USER'
    }
    loadUsers()
  } catch (error) {
    console.error('保存用户失败', error)
    const errorMessage = error.response?.data?.message || error.message || '操作失败，请稍后重试'
    ElMessage.error(errorMessage)
  }
}

const deleteUser = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await deleteUserApi(id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败', error)
      ElMessage.error(error.response?.data?.message || error.message || '删除失败，请稍后重试')
    }
  }
}

const getRoleName = (role) => {
  const roleMap = {
    USER: '普通用户',
    MERCHANT: '商家',
    ADMIN: '管理员'
  }
  return roleMap[role] || role
}

const getRoleType = (role) => {
  const typeMap = {
    USER: '',
    MERCHANT: 'warning',
    ADMIN: 'danger'
  }
  return typeMap[role] || ''
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-users {
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

