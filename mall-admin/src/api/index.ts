import request from '../utils/request'
import type { Product, Order } from './mock'
// import './mock-adapter' // 开发模式引入 Mock

export const api = {
  login: (username: string, password: string) => request.post('/admin/login', { username, password }),
  logout: () => request.post('/admin/logout'),
  
  // 获取个人信息
  getUserInfo: () => request.get('/user/info'),

  // 通用接口
  getCategories: () => request.get<any, Record<string, string>>('/common/category/list'),
  uploadFile: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<any, string>('/common/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 分类管理 (商家端专用)
  getCategoryTree: () => request.get<any, any[]>('/common/category/tree'),
  addCategory: (category: any) => request.post('/admin/category/add', category),
  updateCategory: (category: any) => request.put('/admin/category/update', category),
  deleteCategory: (id: number) => request.delete(`/admin/category/delete/${id}`),

  // 商品
  getProducts: (params?: { pageNum?: number, pageSize?: number, keyword?: string, categoryId?: number }) => 
    request.get<any, any>('/admin/product/list', { params }),
  getProductDetail: (id: number) => request.get<any, any>(`/admin/product/detail/${id}`),
  updateProduct: (product: any) => request.put('/admin/product/update', product),
  updateProductStatus: (id: number, status: number) => request.put(`/admin/product/status/${id}?status=${status}`),
  deleteProduct: (id: number) => request.delete(`/admin/product/delete/${id}`),

  // 订单
  getOrders: (params?: { pageNum?: number, pageSize?: number, status?: number, keyword?: string }) => 
    request.get<any, { list: any[], total: number }>('/admin/order/list', { params }),
  shipOrder: (id: number | string, logisticsNo: string, logisticsCompany?: string) => 
    request.post(`/admin/order/ship/${id}`, { logisticsNo, logisticsCompany }),
  cancelOrder: (orderId: number | string, reason: string) =>
    request.post('/admin/order/cancel', { orderId, reason }),

  // 售后管理
  getAfterSales: (params?: { pageNum?: number, pageSize?: number, status?: number }) => 
    request.get<any, { list: any[], total: number }>('/admin/after-sale/list', { params }),
  handleAfterSale: (id: number, status: number) => 
    request.post('/admin/after-sale/handle', { id, status }),

  // 客服管理
  getServicers: (params?: { pageNum?: number, pageSize?: number, username?: string }) => 
    request.get<any, { list: any[], total: number }>('/admin/servicer/list', { params }),
  saveServicer: (data: any) => request.post('/admin/servicer/save', data),

  // 统计报表
  getSalesStats: () => request.get<any, any[]>('/admin/stats/sales'),
  getAfterSaleStats: () => request.get<any, any[]>('/admin/stats/after-sale'),
  getRatingStats: () => request.get<any, any[]>('/admin/stats/rating'),

  // IM 聊天
  getChatSessions: () => request.get<any, any[]>('/common/chat/sessions'),
  getChatHistory: (orderNo: string) => request.get<any, any[]>(`/common/chat/history/${orderNo}`),
  markChatRead: (orderNo: string) => request.post('/common/chat/read', null, { params: { orderNo } })
};