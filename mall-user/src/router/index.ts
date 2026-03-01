import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '../layout/BasicLayout.vue'

const routes = [
  {
    path: '/',
    component: BasicLayout,
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue') },
      { path: '/search', name: 'Search', component: () => import('../views/Search.vue') },
      { path: '/product/:id', name: 'ProductDetail', component: () => import('../views/ProductDetail.vue') },
      { path: '/cart', name: 'Cart', component: () => import('../views/Cart.vue') },
      { path: '/checkout', name: 'Checkout', component: () => import('../views/Checkout.vue') },
      { 
        path: '/user', 
        name: 'UserCenter', 
        component: () => import('../views/UserCenter.vue'),
        children: [
          { path: 'orders', name: 'OrderList', component: () => import('../views/user/OrderList.vue') },
          { path: 'profile', name: 'UserProfile', component: () => import('../views/user/Profile.vue') },
          { path: 'address', name: 'UserAddress', component: () => import('../views/user/AddressList.vue') },
          { path: 'messages', name: 'MessageCenter', component: () => import('../views/user/MessageCenter.vue') },
          { path: 'reviews', name: 'ReviewList', component: () => import('../views/user/ReviewList.vue') },
          { path: 'after-sales', name: 'AfterSales', component: () => import('../views/user/AfterSales.vue') }
        ]
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
