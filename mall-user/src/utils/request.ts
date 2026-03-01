import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一走 /api 代理前缀，由 Vite 决定打向 Mock 还是后端
const baseURL = '/api'

const request = axios.create({
  baseURL, 
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 兼容部分非标准接口返回
    if (res.code === 200 || res.code === undefined) {
      return res.data !== undefined ? res.data : res
    }
    ElMessage.error(res.message || 'Error')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  error => {
    console.error('Request Error:', error)
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request
