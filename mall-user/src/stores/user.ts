import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../api'
import type { User } from '../api/mock'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const unreadSummary = ref<Record<string, number>>({ total: 0 })

  // 内存级增加未读数 (由 WebSocket 驱动)
  const addUnread = (orderNo: string) => {
    const key = String(orderNo)
    // 增加单订单未读
    unreadSummary.value[key] = (unreadSummary.value[key] || 0) + 1
    // 增加全局总未读
    unreadSummary.value.total = (unreadSummary.value.total || 0) + 1
  }

  // 内存级重置未读数 (进入聊天框时触发)
  const clearUnread = (orderNo: string) => {
    const key = String(orderNo)
    if (unreadSummary.value[key]) {
      unreadSummary.value.total -= unreadSummary.value[key]
      unreadSummary.value[key] = 0
    }
  }

  // 获取未读消息统计 (定时或初始化用)
  const fetchUnreadSummary = async () => {
    if (!token.value) return
    try {
      const res = await api.getChatUnreadSummary()
      unreadSummary.value = res
    } catch (e) {
      console.error('获取未读数失败', e)
    }
  }

  // 获取用户信息
  // force: 是否强制从后端拉取最新数据
  const fetchUserInfo = async (force = false) => {
    // 如果已经有数据，且不强制刷新，直接返回现有数据
    if (user.value && !force) {
      return user.value
    }
    
    try {
      const res = await api.getUserInfo()
      user.value = res
      return res
    } catch (error) {
      // 获取失败（如 Token 过期），清空状态
      user.value = null
      token.value = null
      localStorage.removeItem('token')
      throw error
    }
  }

  const logout = async () => {
    try {
      await api.logout()
    } catch (error) {
      console.error('后端注销失败', error)
    } finally {
      // 无论后端是否注销成功，本地都要清除
      user.value = null
      token.value = null
      localStorage.removeItem('token')
    }
  }

  return {
    user,
    token,
    unreadSummary,
    addUnread,
    clearUnread,
    fetchUnreadSummary,
    fetchUserInfo,
    logout,
    clearUser: logout
  }
})
