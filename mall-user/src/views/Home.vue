<template>
  <div class="home-container">
    <!-- Carousel -->
    <el-carousel height="400px" style="margin-bottom: 20px; border-radius: 8px;">
      <el-carousel-item v-for="item in 3" :key="item">
        <div class="banner-placeholder" :style="{ backgroundColor: ['#ff9a9e', '#fad0c4', '#a18cd1'][item-1] }">
          <h1>年货节 - 超级大促 {{ item }}</h1>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- Product List -->
    <div class="section-title">
      <h2>热销商品</h2>
    </div>

    <div class="product-grid-container">
      <el-row :gutter="20">
        <el-col :span="6" v-for="product in products" :key="product.id">
          <el-card :body-style="{ padding: '0px' }" class="product-card" @click="goDetail(product.id)">
            <div class="image-wrapper">
               <img :src="product.cover" class="image" />
            </div>
            <div style="padding: 14px">
              <div class="product-title">{{ product.title }}</div>
              <div class="product-bottom">
                <span class="price">¥{{ product.price }}</span>
                <span class="sales">{{ product.sales }}人付款</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div class="load-status">
        <div v-if="loading" class="loading-more">
           <el-icon class="is-loading"><Loading /></el-icon> 正在努力加载更多商品...
        </div>
        <el-divider v-if="noMore">主人，已经到底啦</el-divider>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from '../api'
import type { Product } from '../api/mock'
import { Loading } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const products = ref<Product[]>([])
const pageNum = ref(1)
const loading = ref(false)
const noMore = ref(false)

const loadMore = async () => {
  if (loading.value || noMore.value) return
  
  loading.value = true
  try {
    console.log(`[Frontend] Requesting Page ${pageNum.value}`);
    
    const res = await api.getProductList({ 
      pageNum: pageNum.value, 
      pageSize: 12 
    })
    
    // 适配后端返回的 PageResult 结构
    const newList = res.list || []
    if (newList.length === 0 || (res.total > 0 && products.value.length + newList.length >= res.total)) {
      noMore.value = true
    }
    
    products.value.push(...newList)
    pageNum.value++
  } catch (error) {
    console.error('Failed to load products:', error)
  } finally {
    loading.value = false
  }
}

// 手动监听滚动
const handleScroll = () => {
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const scrollHeight = document.documentElement.scrollHeight

  // 距离底部 100px 时加载
  if (scrollTop + windowHeight >= scrollHeight - 100) {
    loadMore()
  }
}

const resetAndLoad = () => {
  products.value = []
  pageNum.value = 1
  noMore.value = false
  loadMore()
}

onMounted(() => {
  resetAndLoad()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const goDetail = (id: number) => {
  router.push(`/product/${id}`)
}
</script>

<style scoped>
.banner-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.section-title {
  margin: 20px 0;
  border-left: 4px solid #ff5000;
  padding-left: 10px;
}

.product-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.image-wrapper {
  height: 200px;
  overflow: hidden;
  background: #f9f9f9;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 10px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.price {
  color: #ff5000;
  font-size: 18px;
  font-weight: bold;
}

.sales {
  color: #999;
  font-size: 12px;
}

.load-status {
  padding: 20px 0;
  text-align: center;
  color: #999;
}
</style>
