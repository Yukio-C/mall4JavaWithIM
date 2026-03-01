import axios from 'axios'
import { ElMessage } from 'element-plus'

const baseURL = '/api'

const request = axios.create({
  baseURL,
  timeout: 5000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('admin-token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

request.interceptors.response.use(
  response => {
    const res = response.data
    // 兼容 Result 结构：只有 code=200 才算成功
    if (res.code === 200 || res.code === undefined) {
      return res.data !== undefined ? res.data : res
    }
    ElMessage.error(res.message || 'Error')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  error => {
    console.error('Request Error:', error)
    ElMessage.error(error.response?.data?.message || error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request
