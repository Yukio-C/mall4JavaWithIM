<template>
  <div class="search-page">
    <div class="search-layout">
      <!-- 左侧：嵌套分类树 -->
      <div class="sidebar">
        <div class="sidebar-title">全部分类</div>
        <div class="category-tree-container">
          <div 
            :class="['all-cate-btn', { active: !queryParams.categoryId }]"
            @click="handleCategoryChange(null)"
          >
            全部商品
          </div>
          <el-tree
            ref="treeRef"
            :data="categoryTree"
            :props="defaultProps"
            node-key="id"
            highlight-current
            :default-expand-all="false"
            @node-click="handleNodeClick"
            class="custom-tree"
          />
        </div>
      </div>

      <!-- 右侧搜索主体 -->
      <div class="search-content">
        <!-- 筛选工具条 -->
        <div class="filter-bar">
          <div class="sort-options">
            <div 
              v-for="opt in sortOptions" 
              :key="opt.value"
              :class="['sort-item', { active: queryParams.sortField === opt.value }]"
              @click="handleSortChange(opt.value)"
            >
              {{ opt.label }}
              <span class="sort-icon" v-if="queryParams.sortField === opt.value">
                {{ queryParams.sortOrder === 'desc' ? '↓' : '↑' }}
              </span>
            </div>
          </div>
          <div class="result-tip">
            共找到 <strong>{{ total }}</strong> 件相关商品
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="product-grid" v-loading="loading">
          <el-row :gutter="20">
            <el-col :span="6" v-for="product in products" :key="product.id">
              <el-card :body-style="{ padding: '0px' }" class="product-card" @click="goToDetail(product.id)">
                <div class="image-wrapper">
                  <el-image :src="product.cover" fit="cover" class="image" />
                </div>
                <div style="padding: 14px">
                  <div class="product-title">{{ product.title }}</div>
                  <div class="product-bottom">
                    <div class="price-row">
                      <span class="currency">¥</span>
                      <span class="price">{{ product.price }}</span>
                    </div>
                    <div class="meta-row">
                      <span>{{ product.sales }}人付款</span>
                      <span class="rating">★ {{ product.rating }}</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <el-empty v-if="products.length === 0" description="换个词试试，或者换个分类？" />
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            layout="prev, pager, next"
            :total="total"
            @current-change="fetchProducts"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'

const route = useRoute()
const router = useRouter()
const treeRef = ref()

const loading = ref(false)
const products = ref<any[]>([])
const total = ref(0)
const categoryTree = ref<any[]>([])

const defaultProps = {
  children: 'children',
  label: 'name'
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  keyword: (route.query.keyword as string) || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  sortField: 'sales',
  sortOrder: 'desc'
})

const sortOptions = [
  { label: '销量', value: 'sales' },
  { label: '评分', value: 'rating' },
  { label: '最新', value: 'createdAt' }
]

const fetchCategories = async () => {
  try {
    const res = await api.getCategoryTree()
    categoryTree.value = res
    
    // 如果 URL 里带了分类，初始化时自动高亮
    if (queryParams.categoryId) {
      nextTick(() => {
        treeRef.value?.setCurrentKey(queryParams.categoryId)
      })
    }
  } catch (e) {
    console.error('加载分类树失败', e)
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await api.getProductList(queryParams)
    products.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleNodeClick = (data: any) => {
  queryParams.categoryId = data.id
  queryParams.pageNum = 1
  fetchProducts()
  // 同步 URL 参数（可选，让刷新不丢失）
  router.replace({ query: { ...route.query, categoryId: data.id } })
}

const handleCategoryChange = (id: number | null) => {
  queryParams.categoryId = id || undefined
  queryParams.pageNum = 1
  if (id === null) {
    treeRef.value?.setCurrentKey(null)
  }
  fetchProducts()
  router.replace({ query: { ...route.query, categoryId: id || undefined } })
}

const handleSortChange = (field: string) => {
  if (queryParams.sortField === field) {
    queryParams.sortOrder = queryParams.sortOrder === 'desc' ? 'asc' : 'desc'
  } else {
    queryParams.sortField = field
    queryParams.sortOrder = 'desc'
  }
  queryParams.pageNum = 1
  fetchProducts()
}

// 监听关键词变化
watch(() => route.query.keyword, (newVal) => {
  queryParams.keyword = (newVal as string) || ''
  queryParams.pageNum = 1
  fetchProducts()
})

// 监听分类参数变化 (处理外部跳转)
watch(() => route.query.categoryId, (newId) => {
  if (newId) {
    queryParams.categoryId = Number(newId)
    treeRef.value?.setCurrentKey(Number(newId))
  } else {
    queryParams.categoryId = undefined
    treeRef.value?.setCurrentKey(null)
  }
  fetchProducts()
})

const goToDetail = (id: number) => router.push(`/product/${id}`)

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped lang="scss">
.search-page {
  width: 1200px;
  margin: 0 auto;
  padding: 20px 0;
}

.search-layout {
  display: flex;
  gap: 20px;
}

.sidebar {
  width: 240px;
  background: #fff;
  border-radius: 8px;
  padding: 15px 0;
  height: fit-content;
  border: 1px solid #eee;

  .sidebar-title {
    padding: 0 20px 15px;
    font-weight: bold;
    border-bottom: 1px solid #f5f5f5;
    margin-bottom: 10px;
  }

  .all-cate-btn {
    padding: 10px 20px;
    margin: 0 10px 10px;
    cursor: pointer;
    font-size: 14px;
    color: #666;
    border-radius: 4px;
    transition: all 0.3s;
    
    &:hover { color: #ff5000; background: #fff5f0; }
    &.active { color: #fff; background: #ff5000; font-weight: bold; }
  }

  .custom-tree {
    padding: 0 10px;
    :deep(.el-tree-node__content) {
      height: 36px;
      border-radius: 4px;
      margin-bottom: 2px;
      &:hover { background-color: #fff5f0; color: #ff5000; }
    }
    :deep(.is-current > .el-tree-node__content) {
      background-color: #fff5f0 !important;
      color: #ff5000 !important;
      font-weight: bold;
    }
  }
}

.search-content { flex: 1; }

.filter-bar {
  background: #fff; padding: 15px 20px; border-radius: 8px; border: 1px solid #eee; margin-bottom: 20px;
  display: flex; justify-content: space-between; align-items: center;
  .sort-options { display: flex; gap: 30px;
    .sort-item { font-size: 14px; color: #666; cursor: pointer; display: flex; align-items: center; gap: 4px;
      &.active { color: #ff5000; font-weight: bold; }
    }
  }
  .result-tip { font-size: 13px; color: #999; }
}

.product-card {
  margin-bottom: 20px; cursor: pointer; transition: all 0.3s;
  &:hover { transform: translateY(-5px); box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
  .image-wrapper { height: 220px; }
  .product-title { font-size: 14px; color: #333; height: 40px; overflow: hidden; margin-bottom: 10px; line-height: 1.4; padding: 0 5px; }
  .product-bottom {
    padding: 0 5px;
    .price-row { color: #ff5000; margin-bottom: 8px; .price { font-size: 20px; font-weight: bold; } }
    .meta-row { display: flex; justify-content: space-between; font-size: 12px; color: #999; }
  }
}

.pagination-container { display: flex; justify-content: center; margin-top: 30px; }
</style>
