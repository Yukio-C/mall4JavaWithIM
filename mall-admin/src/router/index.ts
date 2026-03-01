import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layout/AdminLayout.vue'

const routes = [
  {
    path: '/',
    component: AdminLayout,
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: '看板首页',
        component: () => import('../views/Home.vue')
      },
      {
        path: '/product',
        name: 'ProductList',
        component: () => import('../views/product/ProductList.vue')
      },
      {
        path: '/category',
        name: 'CategoryList',
        component: () => import('../views/category/CategoryList.vue')
      },
      {
        path: '/order',
        name: 'OrderList',
        component: () => import('../views/order/OrderList.vue')
      },
      {
        path: '/after-sale',
        name: 'AfterSaleList',
        component: () => import('../views/order/AfterSaleList.vue')
      },
      {
        path: '/chat',
        name: 'ChatBoard',
        component: () => import('../views/ChatBoard.vue')
      },
      {
        path: '/servicer',
        name: 'ServicerList',
        component: () => import('../views/ServicerList.vue')
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

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin-token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
