<template>
  <el-container class="merchant-layout">
    <el-aside width="200px" class="merchant-sidebar">
      <div class="logo">
        <h2>商家中心</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="merchant-menu"
      >
        <el-menu-item index="/merchant/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>概览</span>
        </el-menu-item>
        <el-menu-item index="/merchant/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/merchant/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/merchant/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="merchant-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/merchant' }">商家中心</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :src="userStore.avatar" :size="32">
                {{ userStore.nickname?.charAt(0) }}
              </el-avatar>
              <span style="margin-left: 8px">{{ userStore.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">返回前台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="merchant-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { DataBoard, Goods, List, DataAnalysis } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const currentPageName = computed(() => {
  const nameMap = {
    '/merchant/dashboard': '概览',
    '/merchant/products': '商品管理',
    '/merchant/orders': '订单管理',
    '/merchant/statistics': '数据信息'
  }
  return nameMap[route.path] || '商家中心'
})

const handleCommand = (command) => {
  if (command === 'home') {
    router.push('/')
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.merchant-layout {
  height: 100vh;
}

.merchant-sidebar {
  background-color: #1f2d3d;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #2f3a48;
}

.logo h2 {
  margin: 0;
  color: #fff;
  font-size: 18px;
}

.merchant-menu {
  border: none;
  background-color: #1f2d3d;
}

.merchant-menu .el-menu-item {
  color: #bfcbd9;
}

.merchant-menu .el-menu-item:hover {
  background-color: #263445;
  color: #67c23a;
}

.merchant-menu .el-menu-item.is-active {
  background-color: #67c23a;
  color: #fff;
}

.merchant-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.merchant-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>




