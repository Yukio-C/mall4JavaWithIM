<template>
  <div class="after-sale-list">
    <el-card>
      <template #header>
        <div class="header">
          <span>售后审核管理</span>
          <el-radio-group v-model="statusFilter" size="small" @change="fetchList">
            <el-radio-button :label="null">全部</el-radio-button>
            <el-radio-button :label="0">待审核</el-radio-button>
            <el-radio-button :label="1">处理中</el-radio-button>
            <el-radio-button :label="2">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="商品信息" min-width="250">
          <template #default="{ row }">
            <div class="product-info">
              <el-image :src="row.productCover" class="p-img" fit="cover" />
              <span>{{ row.productTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ typeMap[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请原因" width="150" show-overflow-tooltip />
        <el-table-column prop="refundAmount" label="退款金额" width="120">
          <template #default="{ row }">¥{{ row.refundAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <template v-if="row.status === 0 || row.status === 1">
              <el-button link type="success" @click="handleReview(row, 2)">同意</el-button>
              <el-button link type="danger" @click="handleReview(row, 3)">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 审核详情弹窗 -->
    <el-dialog v-model="detailVisible" title="售后详情" width="700px">
      <div v-if="currentRow" class="detail-body">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentRow.userId }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentRow.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="订单编号">{{ currentRow.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ currentRow.refundAmount }}</el-descriptions-item>
          <el-descriptions-item label="售后类型">{{ typeMap[currentRow.type] }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="statusType(currentRow.status)" size="small">{{ statusMap[currentRow.status] }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-section-title">售后商品</div>
        <el-table :data="currentRow.items" border size="small" style="margin-top: 10px">
          <el-table-column label="图片" width="80">
            <template #default="{ row }">
              <el-image :src="row.productCover" class="mini-p-img" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="productTitle" label="商品名称" />
          <el-table-column prop="productPrice" label="单价" width="100">
            <template #default="{ row }">¥{{ row.productPrice }}</template>
          </el-table-column>
          <el-table-column prop="count" label="申请数量" width="100" />
        </el-table>

        <div class="detail-section-title">问题描述</div>
        <div class="desc-content">{{ currentRow.description }}</div>

        <div class="detail-section-title">凭证图片</div>
        <div class="image-list">
          <el-image 
            v-for="url in currentRow.images" 
            :key="url" 
            :src="url" 
            class="v-img"
            :preview-src-list="currentRow.images"
          />
          <div v-if="!currentRow.images || !currentRow.images.length" class="no-images">未上传凭证</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const statusFilter = ref<number | null>(0) // 默认看待审核

const typeMap = { 1: '仅退款', 2: '退货退款', 3: '换货' }
const statusMap = { 0: '待审核', 1: '处理中', 2: '已完成', 3: '已拒绝' }

const statusType = (s: number) => {
  if (s === 2) return 'success'
  if (s === 3) return 'danger'
  return 'warning'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await api.getAfterSales({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value ?? undefined
    })
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const detailVisible = ref(false)
const currentRow = ref<any>(null)
const showDetail = (row: any) => {
  currentRow.value = row
  detailVisible.value = true
}

const handleReview = (row: any, targetStatus: number) => {
  const action = targetStatus === 2 ? '通过' : '拒绝'
  ElMessageBox.confirm(`确定要${action}该售后申请吗？`, '提示', {
    type: targetStatus === 2 ? 'warning' : 'error'
  }).then(async () => {
    try {
      await api.handleAfterSale(row.id, targetStatus)
      ElMessage.success('处理成功')
      fetchList()
    } catch (e: any) {
      ElMessage.error(e.message)
    }
  })
}

onMounted(fetchList)
</script>

<style scoped>
.after-sale-list { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; }
.product-info { display: flex; align-items: center; gap: 10px; }
.p-img { width: 40px; height: 40px; border-radius: 4px; flex-shrink: 0; }
.pagination { margin-top: 20px; text-align: right; }
.info-row { margin-bottom: 15px; font-size: 14px; }
.image-list { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 10px; }
.v-img { width: 100px; height: 100px; border-radius: 4px; border: 1px solid #eee; }
.detail-section-title { font-size: 15px; font-weight: bold; border-left: 4px solid #409EFF; padding-left: 10px; margin: 20px 0 10px; }
.desc-content { background: #f8f9fa; padding: 12px; border-radius: 4px; font-size: 14px; color: #666; line-height: 1.6; }
.mini-p-img { width: 50px; height: 50px; border-radius: 4px; }
.no-images { color: #999; font-size: 13px; font-style: italic; }
</style>
