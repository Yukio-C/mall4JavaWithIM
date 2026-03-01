<template>
  <div class="user-center">
    <div class="sidebar">
      <div class="menu-title">个人中心</div>
      <ul class="menu-list">
        <li :class="{ active: route.path.includes('orders') }" @click="router.push('/user/orders')">我的订单</li>
        <li :class="{ active: route.path.includes('messages') }" @click="router.push('/user/messages')">
          消息中心
          <el-badge v-if="userStore.unreadSummary?.total" is-dot class="menu-badge" />
        </li>
        <li :class="{ active: route.path.includes('reviews') }" @click="router.push('/user/reviews')">我的评价</li>
        <li :class="{ active: route.path.includes('address') }" @click="router.push('/user/address')">地址管理</li>
        <li :class="{ active: route.path.includes('after-sales') }" @click="router.push('/user/after-sales')">售后服务</li>
        <li :class="{ active: route.path.includes('profile') }" @click="router.push('/user/profile')">个人资料</li>
      </ul>
    </div>
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
</script>

<style scoped>
.user-center {
  display: flex;
  gap: 20px;
}

.sidebar {
  width: 200px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 15px 0;
  height: fit-content;
}

.menu-title {
  padding: 0 20px 15px;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-list li {
  padding: 12px 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.menu-list li:hover {
  background: #fff5f0;
  color: #ff5000;
}

.menu-list li.active {
  background: #ff5000;
  color: #fff;
}

.menu-badge {
  margin-left: 5px;
  vertical-align: middle;
}

.content {
  flex: 1;
  background: #fff;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 4px;
  min-height: 500px;
}
</style>
