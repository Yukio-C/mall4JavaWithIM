<template>
  <div class="admin-layout">
    <el-container class="h-100">
      <el-aside width="200px" class="aside">
        <div class="logo">商家管理后台</div>
        <el-menu
          router
          :default-active="route.path"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          class="menu"
        >
          <el-menu-item v-for="menu in displayMenus" :key="menu.path" :index="menu.path">
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
               <el-breadcrumb-item>首页</el-breadcrumb-item>
               <el-breadcrumb-item>{{ route.name }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="user">
             <span class="username">欢迎，{{ userStore.user?.nickname || userStore.user?.username || '管理员' }}</span>
             <el-button link type="primary" @click="handleLogout">退出登录</el-button>
          </div>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Goods, List, Menu, UserFilled, ChatDotRound, Ticket, HomeFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 所有可能的菜单定义
const allMenus = [
  { path: '/home', name: '看板首页', icon: HomeFilled, key: 'home' },
  { path: '/product', name: '商品管理', icon: Goods, key: 'product' },
  { path: '/category', name: '分类管理', icon: Menu, key: 'category' },
  { path: '/order', name: '订单管理', icon: List, key: 'order' },
  { path: '/after-sale', name: '售后管理', icon: Ticket, key: 'order' }, 
  { path: '/chat', name: '在线客服', icon: ChatDotRound, key: 'chat' },
  { path: '/servicer', name: '客服管理', icon: UserFilled, key: 'servicer', adminOnly: true }
]

// 根据权限过滤后的菜单
const displayMenus = computed(() => {
  if (userStore.role === 'ADMIN') {
    return allMenus
  }
  // 客服角色：过滤掉 adminOnly 的，且必须在 permissions 列表里的
  return allMenus.filter(m => !m.adminOnly && userStore.permissions.includes(m.key))
})

onMounted(async () => {
  // 如果进入了布局但 Store 里没用户信息（比如刷新了页面），自动拉取
  if (!userStore.user && userStore.token) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      console.error('用户信息加载失败', e)
    }
  }
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出管理系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await userStore.logout()
    ElMessage.success('已安全退出')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.admin-layout, .h-100 {
  height: 100vh;
}

.aside {
  background-color: #304156;
  color: white;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3a4d;
}

.menu {
  border-right: none;
  flex: 1;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.user {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  font-size: 14px;
  color: #666;
}

.main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
