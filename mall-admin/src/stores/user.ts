import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../api'

export const useUserStore = defineStore('user', () => {
  const user = ref<any>(null)
  const token = ref<string | null>(localStorage.getItem('admin-token'))
  const role = ref<string | null>(localStorage.getItem('admin-role'))
  const permissions = ref<string[]>(JSON.parse(localStorage.getItem('admin-perms') || '[]'))

  // 设置用户信息
  const setInfo = (data: any) => {
    user.value = data
    if (data.role) {
      role.value = data.role
      localStorage.setItem('admin-role', data.role)
    }
    if (data.permissions) {
      permissions.value = data.permissions
      localStorage.setItem('admin-perms', JSON.stringify(data.permissions))
    }
  }

  // 获取管理员信息
  const fetchUserInfo = async (force = false) => {
    if (user.value && !force) return user.value
    
    try {
      const res = await api.getUserInfo() 
      setInfo(res)
      return res
    } catch (error) {
      clearLocal()
      throw error
    }
  }

  const clearLocal = () => {
    user.value = null
    token.value = null
    role.value = null
    permissions.value = []
    localStorage.removeItem('admin-token')
    localStorage.removeItem('admin-role')
    localStorage.removeItem('admin-perms')
  }

  const logout = async () => {
    try {
      await api.logout()
    } catch (error) {
      console.error('后端注销失败', error)
    } finally {
      clearLocal()
    }
  }

  return {
    user,
    token,
    role,
    permissions,
    setInfo,
    fetchUserInfo,
    logout,
    clearUser: logout
  }
})
