<template>
  <el-container>
    <el-header>
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <h2>特色农产品销售平台</h2>
        </div>
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          router
          class="header-menu"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/products">商品</el-menu-item>
          <el-menu-item index="/cart" v-if="userStore.isAuthenticated">
            购物车
          </el-menu-item>
          <el-menu-item index="/orders" v-if="userStore.isAuthenticated">
            我的订单
          </el-menu-item>
        </el-menu>
        <div class="user-info">
          <template v-if="userStore.isAuthenticated">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar :src="userStore.avatar" :size="40">
                  {{ userStore.nickname?.charAt(0) }}
                </el-avatar>
                <span style="margin-left: 8px">{{ userStore.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isMerchant || userStore.isAdmin" command="merchant">
                    前往商家后台
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin">
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'merchant') {
    router.push('/merchant')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  }
}
</script>

<style scoped>
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.logo {
  cursor: pointer;
  color: #409eff;
}

.logo h2 {
  margin: 0;
  font-size: 20px;
}

.header-menu {
  flex: 1;
  justify-content: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}
</style>

