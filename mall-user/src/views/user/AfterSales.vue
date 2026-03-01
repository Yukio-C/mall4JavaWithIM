<template>
  <div class="after-sales-container">
    <div class="header">
      <h3>售后服务</h3>
    </div>
    
    <el-tabs v-model="activeTab" class="custom-tabs">
      <el-tab-pane label="申请记录" name="record">
        <el-table :data="records" v-loading="loading" stripe style="width: 100%">
           <el-table-column label="售后商品" width="280">
             <template #default="{ row }">
               <div class="product-info-brief">
                 <el-image :src="row.productCover" class="mini-img" fit="cover" />
                 <div class="meta">
                   <div class="title">{{ row.productTitle }}</div>
                   <div class="no">订单号: {{ row.orderNo }}</div>
                 </div>
               </div>
             </template>
           </el-table-column>
           <el-table-column prop="type" label="类型" width="100">
             <template #default="scope">{{ typeMap[scope.row.type] || '通用' }}</template>
           </el-table-column>
           <el-table-column prop="applyTime" label="申请时间" width="160">
             <template #default="scope">{{ formatDateTime(scope.row.applyTime) }}</template>
           </el-table-column>
           <el-table-column prop="status" label="状态" width="100">
             <template #default="scope">
               <el-tag :type="statusType(scope.row.status)">{{ statusMap[scope.row.status] }}</el-tag>
             </template>
           </el-table-column>
           <el-table-column label="操作">
             <template #default="{ row }">
               <el-button link type="primary" @click="showDetail(row)">详情</el-button>
               <el-button link type="primary" @click="goToChat(row.orderNo)">联系客服</el-button>
             </template>
           </el-table-column>
        </el-table>
        <el-empty v-if="records.length === 0" description="暂无售后记录" />
      </el-tab-pane>

      <!-- 详情弹窗 -->
      <el-dialog v-model="detailVisible" title="售后详情" width="650px" destroy-on-close>
        <div v-if="selectedRecord" class="as-detail-content">
          <div class="section">
            <h4 class="section-title">申请信息</h4>
            <div class="info-grid">
              <div class="info-item"><span>原因：</span>{{ selectedRecord.reason }}</div>
              <div class="info-item"><span>状态：</span>
                <el-tag :type="statusType(selectedRecord.status)" size="small">{{ statusMap[selectedRecord.status] }}</el-tag>
              </div>
            </div>
            <div class="desc-box">{{ selectedRecord.description || '无详细描述' }}</div>
          </div>
        </div>
      </el-dialog>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api'

const router = useRouter()

const activeTab = ref('record')
const records = ref<any[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const selectedRecord = ref<any>(null)

const typeMap = { 1: '仅退款', 2: '退货退款', 3: '换货' }
const statusMap = { 0: '待审核', 1: '处理中', 2: '已完成', 3: '已拒绝' }

const statusType = (status: number) => {
  if (status === 2) return 'success'
  if (status === 3) return 'danger'
  return 'warning'
}

const showDetail = (row: any) => {
  selectedRecord.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchRecords()
})

async function fetchRecords() {
  loading.value = true
  try {
    const res = await api.getAfterSales({ pageNum: 1, pageSize: 50 })
    records.value = res.list || []
  } catch (e) {
    records.value = []
  } finally {
    loading.value = false
  }
}

// 核心重定向逻辑：跳转到集中的消息中心
const goToChat = (orderNo: string) => {
  router.push({
    name: 'MessageCenter',
    query: { orderNo }
  })
}

const formatDateTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.after-sales-container { padding: 20px; background: #fff; border-radius: 8px; min-height: 500px; }
.header { margin-bottom: 20px; border-bottom: 1px solid #f0f0f0; padding-bottom: 10px; }
.product-info-brief { display: flex; align-items: center; gap: 10px; }
.meta .title { font-size: 14px; color: #333; margin-bottom: 4px; }
.meta .no { font-size: 12px; color: #999; }
.mini-img { width: 45px; height: 45px; border-radius: 4px; }
.as-detail-content { padding: 10px; }
.section-title { font-size: 15px; border-left: 4px solid #ff5000; padding-left: 10px; margin-bottom: 15px; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 15px; }
.info-item { font-size: 14px; color: #666; }
.info-item span { color: #333; font-weight: bold; }
.desc-box { background: #f8f9fa; padding: 12px; border-radius: 4px; font-size: 14px; color: #333; line-height: 1.5; white-space: pre-wrap; }
</style>
