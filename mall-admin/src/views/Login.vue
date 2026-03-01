<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>商家后台登录</h2>
      <el-form :model="form" label-width="60px">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="admin" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ username: 'admin', password: 'admin' })
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) return ElMessage.warning('请输入账号和密码')
  
  loading.value = true
  try {
    const res = await api.login(form.value.username, form.value.password)
    localStorage.setItem('admin-token', res.token)
    userStore.token = res.token
    
    // 立即保存角色和权限 (adminLogin 接口返回的对象包含这些)
    userStore.setInfo(res)
    
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e: any) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2d3a4b;
}

.login-card {
  width: 350px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
}
</style>
