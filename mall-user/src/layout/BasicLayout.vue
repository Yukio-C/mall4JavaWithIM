<template>
  <div class="common-layout">
    <el-container>
      <el-header class="mall-header">
        <div class="header-content">
          <div class="logo" @click="$router.push('/')">Mall Demo</div>
          <div class="search-bar">
            <el-input placeholder="搜索商品..." v-model="keyword" @keyup.enter="handleSearch">
              <template #append>
                <el-button :icon="Search" @click="handleSearch"/>
              </template>
            </el-input>
          </div>
          <div class="user-actions">
            <el-button type="text" @click="$router.push('/')">首页</el-button>
            <el-button type="text" @click="$router.push('/cart')">购物车</el-button>
            <el-badge 
              :value="unreadSummary?.total || 0" 
              :hidden="!(unreadSummary?.total)" 
              class="msg-badge"
            >
              <el-button type="text" @click="$router.push('/user/orders')">个人中心</el-button>
            </el-badge>
            <el-dropdown v-if="user">
              <span class="el-dropdown-link">
                {{ user.nickname }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-else type="primary" size="small" @click="$router.push('/login')">登录 / 注册</el-button>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
      <el-footer class="mall-footer">
        <p>© 2026 Mall System Demo. All rights reserved.</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.js'

const router = useRouter()
const keyword = ref('')
const userStore = useUserStore()
const { user, unreadSummary } = storeToRefs(userStore)

let stompClient: Client | null = null

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value } })
  }
}

const initGlobalChat = () => {
  if (!userStore.token || stompClient) return

  const socket = new SockJS('/api/ws')
  stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: { Authorization: `Bearer ${userStore.token}` },
    onConnect: () => {
      console.log('[GlobalChat] 已建立全局实时监听')
      // 订阅私有消息通道
      stompClient?.subscribe('/user/queue/chat', () => {
        console.log('[GlobalChat] 收到新回复，刷新未读数')
        // 收到消息，立即刷新 Pinia 中的未读数
        userStore.fetchUnreadSummary()
      })
    }
  })
  stompClient.activate()
}

const logout = async () => {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
  await userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (token) {
    try {
      await userStore.fetchUserInfo()
      // 加载未读数并启动监听
      userStore.fetchUnreadSummary()
      initGlobalChat()
    } catch (e) {
      userStore.clearUser()
    }
  }
})

onUnmounted(() => {
  if (stompClient) stompClient.deactivate()
})
</script>

<style scoped>
.mall-header {
  background-color: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: center;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  width: 1200px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #ff5000;
  cursor: pointer;
}

.search-bar {
  width: 500px;
}

:deep(.el-input-group__append) {
  background-color: #ff5000;
  color: white;
  border-color: #ff5000;
}

.user-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.msg-badge {
  display: flex;
  align-items: center;
}

:deep(.el-badge__content.is-fixed) {
  top: 10px;
  right: 5px;
}

.mall-footer {
  text-align: center;
  padding: 20px;
  color: #999;
  background: #f5f5f5;
  margin-top: 40px;
}

.el-main {
  width: 1200px;
  margin: 0 auto;
  min-height: 80vh;
  padding-top: 20px;
}
</style>
