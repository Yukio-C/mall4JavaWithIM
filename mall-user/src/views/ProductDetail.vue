<template>
  <div class="product-detail" v-if="product">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product.category }}</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product.title }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="detail-container">
      <div class="gallery">
        <el-carousel trigger="click" height="400px" v-if="product.sliderImgs && product.sliderImgs.length">
          <el-carousel-item v-for="(img, index) in product.sliderImgs" :key="index">
            <img :src="img" class="carousel-img" />
          </el-carousel-item>
        </el-carousel>
        <img v-else :src="product.cover" class="main-img" />
      </div>
      <div class="info">
        <h1 class="title">{{ product.title }}</h1>
        <p class="desc">{{ product.description }}</p>
        
        <div class="price-box">
          <span class="label">价格</span>
          <span class="currency">¥</span>
          <span class="price">{{ product.price }}</span>
          <span class="original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
        </div>

        <div class="meta">
          <div class="meta-item">销量 {{ product.sales }}</div>
          <div class="meta-item">评价 {{ product.rating }}</div>
          <div class="meta-item">库存 {{ product.stock }}</div>
        </div>

        <div class="actions">
          <el-input-number v-model="count" :min="1" :max="product.stock" />
          <el-button type="warning" size="large" @click="addToCart" :loading="submitting">加入购物车</el-button>
          <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
        </div>
      </div>
    </div>

    <!-- 商品详情区域 -->
    <div class="detail-content" v-if="product.detailHtml">
      <el-tabs type="border-card">
        <el-tab-pane label="商品详情">
          <div class="rich-text" v-html="product.detailHtml"></div>
        </el-tab-pane>
        <el-tab-pane label="规格参数">
          <div class="specs">
             <el-descriptions :column="1" border>
                <el-descriptions-item v-for="(val, key) in product.specs" :key="key" :label="key">
                  {{ val }}
                </el-descriptions-item>
             </el-descriptions>
          </div>
        </el-tab-pane>
        <el-tab-pane label="售后保障">
          <p>{{ product.serviceInfo }}</p>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
  <div v-else class="loading-state">
    <el-skeleton :rows="5" animated />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'
import type { ProductDetail } from '../api/mock'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const product = ref<ProductDetail>()
const count = ref(1)
const submitting = ref(false)

onMounted(async () => {
  const id = Number(route.params.id)
  if (id) {
    product.value = await api.getProductDetail(id)
  }
})

const addToCart = async () => {
  if (!product.value) return
  submitting.value = true
  try {
    await api.addToCart(product.value, count.value)
    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error('加入失败')
  } finally {
    submitting.value = false
  }
}

const buyNow = async () => {
  await addToCart()
  router.push('/cart')
}
</script>

<style scoped>
.breadcrumb {
  margin: 20px 0;
}

.detail-container {
  display: flex;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.gallery {
  width: 400px;
  margin-right: 40px;
}

.carousel-img {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

.main-img {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border: 1px solid #eee;
}

.info {
  flex: 1;
}

.title {
  font-size: 24px;
  margin-bottom: 10px;
}

.desc {
  color: #666;
  margin-bottom: 20px;
}

.price-box {
  background: #fff2e8;
  padding: 15px;
  color: #ff5000;
  margin-bottom: 20px;
}

.currency {
  font-size: 18px;
}

.price {
  font-size: 32px;
  font-weight: bold;
}

.original {
  color: #999;
  text-decoration: line-through;
  margin-left: 10px;
}

.meta {
  display: flex;
  border-top: 1px dotted #ccc;
  border-bottom: 1px dotted #ccc;
  padding: 10px 0;
  margin-bottom: 30px;
}

.meta-item {
  flex: 1;
  text-align: center;
  color: #666;
  border-right: 1px solid #eee;
}

.meta-item:last-child {
  border: none;
}

.actions {
  display: flex;
  gap: 20px;
}

.loading-state {
  padding: 50px;
}

.detail-content {
  margin-top: 30px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.rich-text {
  padding: 20px;
  line-height: 1.6;
  word-break: break-all;
}

.rich-text :deep(img) {
  max-width: 100% !important;
  height: auto !important;
  display: block;
  margin: 10px auto;
}

.rich-text :deep(p) {
  margin: 1em 0;
}

.specs {
  padding: 20px;
}
</style>
