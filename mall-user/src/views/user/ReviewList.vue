<template>
  <div class="review-list-container">
    <div class="header">
      <h3>我的评价</h3>
    </div>

    <div class="review-list" v-loading="loading">
      <div class="review-card" v-for="review in reviews" :key="review.id">
        <div class="product-info" @click="goToDetail(review.productId)">
          <el-image :src="review.productCover" class="p-img" fit="cover" />
          <div class="p-name">{{ review.productTitle }}</div>
        </div>
        
        <div class="review-body">
          <div class="review-top">
            <el-rate v-model="review.rating" disabled show-score text-color="#ff9900" />
            <span class="time">{{ formatDate(review.createdAt) }}</span>
          </div>
          
          <div class="content">{{ review.content }}</div>
          
          <div class="images" v-if="review.images && review.images.length">
            <el-image 
              v-for="(img, idx) in review.images" 
              :key="idx" 
              :src="img" 
              class="review-img" 
              :preview-src-list="review.images"
              fit="cover"
            />
          </div>

          <!-- 商家回复 -->
          <div class="reply-box" v-if="review.reply">
            <div class="reply-label">商家回复：</div>
            <div class="reply-content">{{ review.reply }}</div>
          </div>
        </div>
      </div>

      <el-empty v-if="reviews.length === 0" description="您还没有发表过评价哦~" />
    </div>

    <!-- 分页 -->
    <div class="pagination-box" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="fetchReviews"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api'
import { useRouter } from 'vue-router'

const router = useRouter()
const reviews = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const fetchReviews = async () => {
  loading.value = true
  try {
    const res = await api.getMyReviews({ pageNum: pageNum.value, pageSize: pageSize.value })
    reviews.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const goToDetail = (id: number) => {
  router.push(`/product/${id}`)
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

onMounted(fetchReviews)
</script>

<style scoped lang="scss">
.review-list-container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: 600px;
}

.header {
  margin-bottom: 25px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 15px;
}

.review-card {
  display: flex;
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 20px;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    border-color: #ff5000;
  }
}

.product-info {
  width: 120px;
  margin-right: 20px;
  cursor: pointer;
  text-align: center;

  .p-img {
    width: 80px;
    height: 80px;
    border-radius: 4px;
    margin-bottom: 8px;
  }

  .p-name {
    font-size: 12px;
    color: #666;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

.review-body {
  flex: 1;

  .review-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .time {
      font-size: 12px;
      color: #999;
    }
  }

  .content {
    font-size: 14px;
    color: #333;
    line-height: 1.6;
    margin-bottom: 15px;
    white-space: pre-wrap;
  }

  .images {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;

    .review-img {
      width: 80px;
      height: 80px;
      border-radius: 4px;
      cursor: zoom-in;
    }
  }

  .reply-box {
    background: #f9f9f9;
    padding: 12px 15px;
    border-radius: 4px;
    border-left: 3px solid #ff5000;
    margin-top: 10px;

    .reply-label {
      font-size: 12px;
      font-weight: bold;
      color: #ff5000;
      margin-bottom: 5px;
    }

    .reply-content {
      font-size: 13px;
      color: #666;
      line-height: 1.5;
    }
  }
}

.pagination-box {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}
</style>
