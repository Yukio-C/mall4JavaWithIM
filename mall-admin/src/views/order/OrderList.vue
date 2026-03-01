<template>
  <div class="order-list">
    <!-- 1. 搜索筛选栏 -->
    <el-card class="search-card" style="margin-bottom: 20px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="订单号 (前缀匹配)" 
            clearable 
            @keyup.enter="handleSearch"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="待付款" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已关闭" :value="4" />
            <el-option label="售后中" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
        </div>
      </template>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="包含商品" width="300">
          <template #default="scope">
            <div v-for="(item, idx) in scope.row.items" :key="idx" style="font-size: 12px">
              {{ item.productTitle }} x {{ item.count }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总价">
          <template #default="scope">¥{{ scope.row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
           <template #default="scope">
             <el-tag :type="getStatusType(scope.row.status)">
               {{ getStatusText(scope.row.status) }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column label="收货信息" width="200">
          <template #default="scope">
             <div>{{ scope.row.receiverName }} {{ scope.row.receiverPhone }}</div>
             <div style="font-size: 12px; color: #999">{{ scope.row.receiverAddress }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 1" 
              type="primary" 
              size="small" 
              @click="openShipDialog(scope.row)"
            >
              发货
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="danger"
              size="small"
              @click="openCancelDialog(scope.row)"
            >
              取消订单
            </el-button>
            <span v-else-if="scope.row.status === 2" style="color: #67c23a; font-size: 12px">
              物流号: {{ scope.row.logisticsNo }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; text-align: right">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <el-dialog v-model="shipDialogVisible" title="订单发货" width="400px">
      <el-form>
        <el-form-item label="物流公司">
          <el-select v-model="logisticsCompany" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="logisticsNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip" :loading="shipping">确定发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelDialogVisible" title="商家取消订单" width="400px">
      <el-form>
        <el-form-item label="取消原因" required>
          <el-input 
            v-model="cancelReason" 
            type="textarea" 
            placeholder="请输入取消原因，用户可见" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">返回</el-button>
        <el-button type="danger" @click="confirmCancel" :loading="cancelling">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const shipDialogVisible = ref(false)
const logisticsNo = ref('')
const logisticsCompany = ref('顺丰速运')
const currentOrderId = ref<number | string>('')
const shipping = ref(false)

// 取消相关
const cancelDialogVisible = ref(false)
const cancelReason = ref('库存不足/由于商家原因无法发货')
const cancelling = ref(false)

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = ref({
  keyword: '',
  status: undefined as number | undefined
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await api.getOrders({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.value.keyword || undefined,
      status: searchForm.value.status
    })
    tableData.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    status: undefined
  }
  handleSearch()
}

onMounted(fetchList)

const getStatusType = (status: number) => {
  const map: any = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger', 5: 'warning' }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: any = { 0: '待付款', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已关闭', 5: '售后中' }
  return map[status] || '未知'
}

const openShipDialog = (row: any) => {
  currentOrderId.value = row.id
  logisticsNo.value = 'SF' + Date.now()
  shipDialogVisible.value = true
}

const confirmShip = async () => {
  if (!logisticsNo.value) return ElMessage.warning('请输入物流单号')
  shipping.value = true
  try {
    await api.shipOrder(currentOrderId.value, logisticsNo.value, logisticsCompany.value)
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    fetchList()
  } finally {
    shipping.value = false
  }
}

const openCancelDialog = (row: any) => {
  currentOrderId.value = row.id
  cancelReason.value = '库存不足/由于商家原因无法发货'
  cancelDialogVisible.value = true
}

const confirmCancel = async () => {
  if (!cancelReason.value.trim()) return ElMessage.warning('请输入取消原因')
  
  cancelling.value = true
  try {
    await api.cancelOrder(currentOrderId.value, cancelReason.value)
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    cancelling.value = false
  }
}
</script>
