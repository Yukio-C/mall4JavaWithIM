<template>
  <div class="order-list">
    <div class="header">
      <h3>我的订单</h3>
    </div>
    
    <div class="tabs">
      <span 
        v-for="tab in tabs" 
        :key="tab.value"
        :class="{ active: currentStatus === tab.value }" 
        @click="handleTabChange(tab.value)"
      >{{ tab.label }}</span>
    </div>

    <div class="orders-container" v-loading="loading">
      <div class="order-card" v-for="order in orders" :key="order.id">
        <!-- 订单头 -->
        <div class="card-header">
          <div class="header-left">
            <span class="time">{{ formatDate(order.createdAt) }}</span>
            <span class="no">订单号：{{ order.orderNo }}</span>
          </div>
          <div class="header-right">
            <span :class="['status-text', 'status-' + order.status]">{{ statusMap[order.status] }}</span>
          </div>
        </div>

        <!-- 订单内容区 -->
        <div class="card-content">
          <table class="order-table">
            <tbody>
              <tr v-for="(item, index) in order.items" :key="item.id">
                <td class="col-product" width="400">
                  <div class="product-box">
                    <el-image :src="item.productCover" class="p-img" fit="cover" @click="goToDetail(item.productId)" />
                    <div class="p-info">
                      <div class="p-title" @click="goToDetail(item.productId)">{{ item.productTitle }}</div>
                      <div class="p-spec">规格：默认</div>
                    </div>
                  </div>
                </td>
                <td class="col-price" width="120">
                  <div class="price">¥{{ item.productPrice }}</div>
                  <div class="count">x {{ item.count }}</div>
                </td>
                <td class="col-item-action" width="150">
                  <div class="item-btns">
                    <el-button 
                      v-if="order.status === 3 && item.isCommented === 0" 
                      type="primary" link size="small" @click="openItemReviewDialog(order, item)"
                    >评价商品</el-button>
                    <span v-else-if="item.isCommented === 1" class="done-tag">已评价</span>
                    
                    <el-button 
                      v-if="order.status === 2 || order.status === 3" 
                      link size="small" @click="openAfterSaleApply(order, item)"
                    >申请售后</el-button>
                  </div>
                </td>
                <td v-if="index === 0" :rowspan="order.items.length" class="col-order-summary">
                  <div class="total-box">
                    <div class="pay-amount">实付：<strong>¥{{ order.payAmount }}</strong></div>
                  </div>
                  <div class="order-btns">
                    <el-button v-if="order.status === 0" type="primary" size="small" @click="handlePay(order.orderNo)">立即支付</el-button>
                    <el-button v-if="order.status === 2" type="success" size="small" @click="handleConfirmReceipt(order)">确认收货</el-button>
                    <el-button v-if="order.status === 0" size="small" plain @click="openCancelDialog(order)">取消订单</el-button>
                    <el-badge v-if="order.status === 5" :value="unreadSummary[order.orderNo]" :hidden="!unreadSummary[order.orderNo]">
                      <el-button type="warning" size="small" @click="goToChat(order.orderNo)">联系售后</el-button>
                    </el-badge>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <el-empty v-if="orders.length === 0" description="暂时没有相关的订单哦" />
    </div>

    <!-- 分页 -->
    <div class="pagination-box" v-if="total > 0">
      <el-pagination v-model:current-page="queryParams.pageNum" :total="total" @current-change="fetchOrders" />
    </div>

    <!-- 售后申请弹窗 -->
    <el-dialog v-model="afterSaleVisible" title="申请售后" width="500px" destroy-on-close>
      <div v-if="activeAfterSaleItem" class="after-sale-body">
        <div class="target-item-brief">
          <el-image :src="activeAfterSaleItem.productCover" class="mini-p-img" />
          <span>{{ activeAfterSaleItem.productTitle }}</span>
        </div>
        <el-form :model="afterSaleForm" label-position="top" style="margin-top: 20px">
          <el-form-item label="售后类型" required>
            <el-radio-group v-model="afterSaleForm.type">
              <el-radio :label="1">仅退款</el-radio>
              <el-radio :label="2">退货退款</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="申请原因" required>
            <el-select v-model="afterSaleForm.reason" placeholder="请选择退款原因" style="width: 100%">
              <el-option label="商品质量问题" value="商品质量问题" />
              <el-option label="收到商品少件/破损" value="收到商品少件/破损" />
              <el-option label="7天无理由退换" value="7天无理由退换" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="问题描述" required>
            <el-input v-model="afterSaleForm.description" type="textarea" :rows="3" placeholder="请详细描述您遇到的问题" />
          </el-form-item>
          <el-form-item label="上传凭证">
            <el-upload action="#" list-type="picture-card" :limit="3" :http-request="handleAfterSaleUpload" :on-remove="handleAfterSaleRemove">
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="afterSaleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAfterSale" :loading="submittingAfterSale">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗保持不变... -->
    <el-dialog v-model="reviewVisible" title="发表评价" width="500px">
      <div v-if="activeReviewItem" class="review-dialog-body">
        <div class="review-target">
          <el-image :src="activeReviewItem.productCover" class="target-img" />
          <div class="target-name">{{ activeReviewItem.productTitle }}</div>
        </div>
        <el-form label-position="top">
          <el-form-item label="描述相符">
            <el-rate v-model="reviewForm.rating" show-text />
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input v-model="reviewForm.content" type="textarea" :rows="4" />
          </el-form-item>
          <el-checkbox v-model="reviewForm.isAnonymous">匿名评价</el-checkbox>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSingleReview" :loading="submittingReview">发布评价</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelVisible" title="取消订单" width="400px">
      <el-form label-position="top">
        <el-form-item label="请选择取消原因" required>
          <el-select v-model="cancelForm.reason" style="width: 100%">
            <el-option label="不想买了" value="不想买了" />
            <el-option label="信息填错" value="信息填错" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">返回</el-button>
        <el-button type="primary" @click="submitCancel" :loading="cancelling">确定取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { api } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { storeToRefs } from 'pinia'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const { unreadSummary } = storeToRefs(userStore)

const loading = ref(false)
const orders = ref<any[]>([])
const total = ref(0)
const currentStatus = ref<number | null>(null)
const queryParams = reactive({ pageNum: 1, pageSize: 5 })

const statusMap: Record<number, string> = { 0: '待付款', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已关闭', 5: '售后中' }
const tabs = [{ label: '全部', value: null }, { label: '待付款', value: 0 }, { label: '待发货', value: 1 }, { label: '待收货', value: 2 }, { label: '已完成', value: 3 }, { label: '售后中', value: 5 }]

// 售后逻辑
const afterSaleVisible = ref(false)
const submittingAfterSale = ref(false)
const activeAfterSaleItem = ref<any>(null)
const afterSaleForm = reactive({
  orderId: 0,
  orderNo: '',
  type: 1,
  reason: '',
  description: '',
  images: [] as string[],
  items: [] as any[]
})

// 评价逻辑
const reviewVisible = ref(false)
const submittingReview = ref(false)
const activeReviewItem = ref<any>(null)
const reviewForm = reactive({ orderId: 0, orderItemId: 0, productId: 0, rating: 5, content: '', images: [], isAnonymous: false })

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await api.getOrders({ ...queryParams, status: currentStatus.value ?? undefined })
    orders.value = res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

const handleTabChange = (val: number | null) => {
  currentStatus.value = val
  queryParams.pageNum = 1
  fetchOrders()
}

// 打开售后申请弹窗 (不再直接跳转)
const openAfterSaleApply = (order: any, item: any) => {
  activeAfterSaleItem.value = item
  Object.assign(afterSaleForm, {
    orderId: order.id,
    orderNo: order.orderNo,
    type: 1,
    reason: '',
    description: '',
    images: [],
    items: [{ orderItemId: item.id, count: item.count }] // 单品售后
  })
  afterSaleVisible.value = true
}

const handleAfterSaleUpload = async (options: any) => {
  try {
    const res: any = await api.uploadFile(options.file)
    afterSaleForm.images.push(res.data || res)
    options.onSuccess()
  } catch { ElMessage.error('上传失败'); options.onError() }
}

const handleAfterSaleRemove = (file: any) => {
  const url = file.response || file.url
  afterSaleForm.images = afterSaleForm.images.filter(i => i !== url)
}

const submitAfterSale = async () => {
  if (!afterSaleForm.reason || !afterSaleForm.description) return ElMessage.warning('请补全申请信息')
  submittingAfterSale.value = true
  try {
    await api.apply4AfterSale(afterSaleForm)
    ElMessage.success('提交申请成功，请等待审核')
    afterSaleVisible.value = false
    fetchOrders()
  } catch (e: any) { ElMessage.error(e.message || '提交失败') }
  finally { submittingAfterSale.value = false }
}

// 评价逻辑
const openItemReviewDialog = (order: any, item: any) => {
  activeReviewItem.value = item
  Object.assign(reviewForm, { orderId: order.id, orderItemId: item.id, productId: item.productId, rating: 5, content: '', images: [], isAnonymous: false })
  reviewVisible.value = true
}

const submitSingleReview = async () => {
  if (!reviewForm.content.trim()) return ElMessage.warning('请输入评价内容')
  submittingReview.value = true
  try {
    await api.submitReview({ orderId: reviewForm.orderId, reviews: [reviewForm] })
    ElMessage.success('评价成功'); reviewVisible.value = false; fetchOrders()
  } catch (e: any) { ElMessage.error(e.message) }
  finally { submittingReview.value = false }
}

// 全局操作
const handleConfirmReceipt = (order: any) => {
  ElMessageBox.confirm('确认已收到商品吗？', '提示', { type: 'warning' }).then(async () => {
    try { await api.confirmReceipt(order.id); ElMessage.success('收货成功'); fetchOrders() } catch (e: any) { ElMessage.error(e.message) }
  })
}

const handlePay = async (no: string) => {
  try { await api.payment.pay(no); ElMessage.success('支付成功'); fetchOrders() } catch { ElMessage.error('支付失败') }
}

const cancelVisible = ref(false)
const cancelling = ref(false)
const cancelForm = reactive({ orderId: 0, reason: '不想买了' })
const openCancelDialog = (o: any) => { cancelForm.orderId = o.id; cancelVisible.value = true }
const submitCancel = async () => {
  cancelling.value = true
  try { await api.cancelOrder(cancelForm.orderId, cancelForm.reason); ElMessage.success('订单已取消'); cancelVisible.value = false; fetchOrders() } finally { cancelling.value = false }
}

const goToChat = (no: string) => router.push({ name: 'MessageCenter', query: { orderNo: no } })
const goToDetail = (id: any) => router.push(`/product/${id}`)
const formatDate = (d: string) => d ? new Date(d).toLocaleString() : '-'

onMounted(fetchOrders)
</script>

<style scoped lang="scss">
.order-list { padding: 20px; background: #fff; border-radius: 8px; min-height: 600px; }
.tabs { display: flex; gap: 30px; margin-bottom: 25px; border-bottom: 1px solid #f0f0f0; 
  span { padding-bottom: 10px; cursor: pointer; font-size: 15px; color: #666; transition: all 0.3s;
    &:hover { color: #ff5000; }
    &.active { color: #ff5000; font-weight: bold; border-bottom: 3px solid #ff5000; }
  }
}

.order-card { border: 1px solid #e8e8e8; margin-bottom: 25px; border-radius: 4px; overflow: hidden;
  &:hover { border-color: #ff5000; }
  .card-header { background: #f5f5f5; padding: 12px 20px; display: flex; justify-content: space-between; align-items: center; font-size: 13px;
    .time { color: #333; font-weight: bold; margin-right: 20px; }
    .no { color: #999; }
    .status-text { font-weight: bold; }
    .status-0 { color: #ff5000; }
    .status-3 { color: #67c23a; }
    .status-5 { color: #e6a23c; }
  }
  .order-table { width: 100%; border-collapse: collapse;
    td { border: 1px solid #f0f0f0; padding: 15px; vertical-align: middle; }
  }
  .product-box { display: flex; gap: 15px;
    .p-img { width: 80px; height: 80px; border-radius: 4px; cursor: pointer; border: 1px solid #f0f0f0; }
    .p-info { flex: 1; .p-title { font-size: 14px; color: #333; line-height: 1.4; cursor: pointer; &:hover { color: #ff5000; } }
      .p-spec { font-size: 12px; color: #999; margin-top: 8px; }
    }
  }
  .col-price { text-align: center; .price { font-size: 14px; color: #333; } .count { color: #999; margin-top: 5px; } }
  .col-item-action { text-align: center; .item-btns { display: flex; flex-direction: column; align-items: center; gap: 8px; 
    .el-button { margin: 0 !important; font-size: 13px; }
  } .done-tag { font-size: 12px; color: #999; } }
  .col-order-summary { text-align: center; background: #fffcfb;
    .total-box { margin-bottom: 15px; .pay-amount { font-size: 16px; color: #333; strong { color: #ff5000; font-size: 20px; } } }
    .order-btns { display: flex; flex-direction: column; align-items: center; gap: 10px; }
  }
}

.after-sale-body {
  .target-item-brief { display: flex; align-items: center; gap: 15px; padding: 10px; background: #f9f9f9; border-radius: 4px; 
    .mini-p-img { width: 50px; height: 50px; }
    span { font-size: 14px; font-weight: bold; }
  }
}

.review-target { display: flex; align-items: center; gap: 15px; background: #fafafa; padding: 10px; border-radius: 4px; margin-bottom: 20px;
  .target-img { width: 60px; height: 60px; }
  .target-name { font-size: 14px; color: #333; font-weight: bold; }
}

.pagination-box { display: flex; justify-content: flex-end; margin-top: 30px; }
</style>
