import request from '../utils/request'
import type { Product, ProductDetail, User, Address, Order, CartItem, PageResult } from './mock'

// 导入 Mock 适配器 (仅在开发/演示模式下开启)
// import './mock-adapter' 

export const api = {
  // 用户
  login: (username: string, password: string) => request.post<any, { token: string, userId: number, username: string }>('/user/login', { username, password }),
  logout: () => request.post('/user/logout'),
  register: (username: string, password: string) => request.post('/user/register', { username, password }),
  getUserInfo: () => request.get<any, User>('/user/info'),
  updateProfile: (data: Partial<User>) => request.put('/user/update', data),
  
  // 地址
  getAddresses: (params: { pageNum?: number, pageSize?: number } = {}) => request.get<any, PageResult<Address>>('/user/address/list', { params }),
  addAddress: (data: any) => request.post('/user/address/add', data),
  updateAddress: (data: any) => request.put('/user/address/update', data),
  deleteAddress: (id: number) => request.delete(`/user/address/delete/${id}`),

  // 售后
  getAfterSales: (params?: { pageNum?: number, pageSize?: number }) => request.get<any, any>('/user/after-sales/list', { params }),
  apply4AfterSale: (data: any) => request.post('/user/after-sales/apply', data),

  // 商品
  getProductList: (params?: any) => request.get<any, PageResult<Product>>('/product/list', { params }),
  getProductDetail: (id: number) => request.get<any, ProductDetail>('/product/detail', { params: { id } }),
  getCategoryTree: () => request.get<any, any[]>('/common/category/tree'),

  // 购物车
  getCart: () => request.get<any, PageResult<CartItem>>('/cart/list'),
  addToCart: (product: Product, count: number) => request.post('/cart/add', { productId: product.id, count }),
  updateCart: (id: number, count: number) => request.post('/cart/update', { id, count }),
  deleteCart: (id: number) => request.delete(`/cart/delete/${id}`),

  // 订单
  getOrderToken: () => request.get<any, { token: string }>('/order/token'),
  createOrder: (data: { items: any[], addressId: number, orderToken: string }) => request.post<any, Order>('/order/create', data),
  cancelOrder: (orderId: number, reason: string) => request.post<any, string>('/order/cancel', { orderId, reason }),
  getOrders: (params?: { pageNum?: number, pageSize?: number, status?: number }) => request.get<any, PageResult<Order>>('/order/list', { params }),
  getOrderDetailByNo: (orderNo: string) => request.get<any, Order>('/order/detail-by-no', { params: { orderNo } }),
  confirmReceipt: (id: number) => request.post<any, string>(`/order/confirm/${id}`),
  submitReview: (data: any) => request.post<any, string>('/review/submit', data),
  getMyReviews: (params: { pageNum?: number, pageSize?: number } = {}) => request.get<any, PageResult<any>>('/review/my', { params }),
  // 支付
  payment: {
    pay: (orderNo: string, payType: number = 3) => 
      request.post<any, any>('/payment/pay', { orderNo, payType }),
    status: (orderNo: string) => 
      request.get<any, any>('/payment/status', { params: { orderNo } })
  },

  // 通用
  getRegions: () => request.get<any, any[]>('/common/region/tree'),
  getSupportStatus: () => request.get<any, boolean>('/common/chat/status'),
  getChatUnreadSummary: () => request.get<any, Record<string, number>>('/common/chat/unread-summary'),
  getUserChatSessions: () => request.get<any, any[]>('/common/chat/user-sessions'),
  markChatRead: (orderNo: string) => request.post('/common/chat/read', null, { params: { orderNo } }),
  uploadFile: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return request.post<any, string>('/common/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
};