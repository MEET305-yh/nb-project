<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="info">
          <el-form :model="userInfo" label-width="100px" style="max-width: 500px">
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="userInfo.nickname" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userInfo.phone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userInfo.email" />
            </el-form-item>
            <el-form-item label="头像">
              <el-input v-model="userInfo.avatar" placeholder="头像URL" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateInfo">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="收货地址" name="address">
          <el-button type="primary" @click="showAddressDialog = true" style="margin-bottom: 20px">
            添加地址
          </el-button>
          <el-table :data="addresses" style="width: 100%">
            <el-table-column prop="receiverName" label="收货人" />
            <el-table-column prop="receiverPhone" label="手机号" />
            <el-table-column label="地址">
              <template #default="{ row }">
                {{ row.province }}{{ row.city }}{{ row.district }}{{ row.detail }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="editAddress(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteAddress(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="showAddressDialog" title="添加地址" width="500px">
      <el-form :model="addressForm" label-width="100px">
        <el-form-item label="收货人">
          <el-input v-model="addressForm.receiverName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addressForm.receiverPhone" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="addressForm.province" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="addressForm.city" />
        </el-form-item>
        <el-form-item label="区县">
          <el-input v-model="addressForm.district" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="addressForm.detail" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserInfo, updateUserInfo } from '@/api/user'
import { getAddresses, addAddress, updateAddress, deleteAddress as deleteAddressApi } from '@/api/address'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('info')
const userInfo = ref({})
const addresses = ref([])
const showAddressDialog = ref(false)
const addressForm = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: ''
})
const editingAddressId = ref(null)

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

const updateInfo = async () => {
  try {
    await updateUserInfo(userInfo.value)
    ElMessage.success('更新成功')
    userStore.fetchUserInfo()
  } catch (error) {
    console.error('更新失败', error)
  }
}

const loadAddresses = async () => {
  try {
    const res = await getAddresses()
    addresses.value = res.data
  } catch (error) {
    console.error('加载地址失败', error)
  }
}

const editAddress = (address) => {
  editingAddressId.value = address.id
  addressForm.value = { ...address }
  showAddressDialog.value = true
}

const saveAddress = async () => {
  try {
    if (editingAddressId.value) {
      await updateAddress(editingAddressId.value, addressForm.value)
      ElMessage.success('更新成功')
    } else {
      await addAddress(addressForm.value)
      ElMessage.success('添加成功')
    }
    showAddressDialog.value = false
    editingAddressId.value = null
    addressForm.value = {
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detail: ''
    }
    loadAddresses()
  } catch (error) {
    console.error('保存地址失败', error)
  }
}

const deleteAddress = async (id) => {
  try {
    await deleteAddressApi(id)
    ElMessage.success('删除成功')
    await loadAddresses()
  } catch (error) {
    console.error('删除地址失败', error)
    ElMessage.error(error.response?.data?.message || error.message || '删除失败')
  }
}

onMounted(() => {
  loadUserInfo()
  loadAddresses()
})
</script>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}
</style>

















