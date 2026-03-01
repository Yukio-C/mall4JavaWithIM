<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>{{ isRegister ? '注册新账号' : '欢迎登录 Mall System' }}</span>
          <el-button type="text" @click="toggleMode">{{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}</el-button>
        </div>
      </template>
      <el-form :model="form" label-width="0">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item v-if="isRegister">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleSubmit" :loading="loading">
            {{ isRegister ? '注 册' : '登 录' }}
          </el-button>
        </el-form-item>
        <div class="tips" v-if="!isRegister">
          <p>测试账号: user / 123456</p>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const isRegister = ref(false)
const form = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const toggleMode = () => {
  isRegister.value = !isRegister.value
  form.value = { username: '', password: '', confirmPassword: '' }
}

const handleSubmit = async () => {
  if (!form.value.username || !form.value.password) return ElMessage.warning('请输入用户名 and 密码')
  
  loading.value = true
  try {
    if (isRegister.value) {
      if (form.value.password !== form.value.confirmPassword) {
        ElMessage.warning('两次密码输入不一致')
        loading.value = false
        return
      }
      await api.register(form.value.username, form.value.password)
      ElMessage.success('注册成功，请登录')
      toggleMode()
    } else {
      const res = await api.login(form.value.username, form.value.password)
      localStorage.setItem('token', res.token)
      userStore.token = res.token // 核心修复：更新全局响应式 Token
      ElMessage.success('登录成功')
      router.push('/')
    }
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
  background-color: #f0f2f5;
  background-image: url('https://via.placeholder.com/1920x1080/ff5000/ffffff?text=Mall+Background');
  background-size: cover;
}

.login-card {
  width: 400px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.95);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.login-btn {
  width: 100%;
  background-color: #ff5000;
  border-color: #ff5000;
}

.login-btn:hover {
  background-color: #ff6a00;
  border-color: #ff6a00;
}

.tips {
  text-align: center;
  font-size: 12px;
  color: #666;
  margin-top: 10px;
}
</style>
