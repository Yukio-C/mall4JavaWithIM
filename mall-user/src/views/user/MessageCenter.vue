<template>
  <div class="message-center">
    <div class="header">
      <h3>消息中心</h3>
    </div>

    <div class="chat-layout">
      <!-- 左侧列表 -->
      <div class="session-list">
        <div 
          v-for="s in sessions" 
          :key="s.orderNo" 
          :class="['session-item', { active: currentOrderNo === s.orderNo }]"
          @click="selectSession(s.orderNo)"
        >
          <el-badge :value="unreadSummary[s.orderNo]" :hidden="!unreadSummary[s.orderNo]" class="badge">
            <div class="info">
              <div class="title">订单: {{ s.orderNo }}</div>
              <div class="last">{{ s.lastContent || '咨询中...' }}</div>
            </div>
          </el-badge>
        </div>
        <el-empty v-if="sessions.length === 0" description="暂无咨询记录" :image-size="60" />
      </div>

      <!-- 右侧聊天 -->
      <div class="chat-window" v-if="currentOrderNo">
        <div class="window-header">
          <span>咨询订单：<strong>{{ currentOrderNo }}</strong></span>
          <el-button type="primary" link size="small" @click="showOrderDetail">查看订单详情</el-button>
        </div>
        
        <div class="message-box" ref="msgBox">
          <template v-for="(msg, index) in messages" :key="index">
            <div v-if="shouldShowTime(index)" class="time-tag">
              {{ formatSmartTime(msg.createdAt) }}
            </div>
            
            <div :class="['msg-row', msg.isMine ? 'mine' : 'others']">
              <div class="avatar">
                <el-icon v-if="msg.isMine"><UserFilled /></el-icon>
                <el-icon v-else><Service /></el-icon>
              </div>
              <div class="bubble">{{ msg.content }}</div>
            </div>
          </template>
        </div>

        <div class="input-bar">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            placeholder="请输入咨询内容..."
            @keyup.enter="handleSend"
          />
          <div class="btn-row">
            <el-button type="primary" @click="handleSend" :disabled="!inputText.trim()">发送消息</el-button>
          </div>
        </div>
      </div>

      <div class="empty-window" v-else>
        <el-result icon="info" title="请选择左侧会话" sub-title="实时查看客服回复" />
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <div v-if="orderDetail" class="order-detail-view">
        <div class="info-section">
          <p><strong>订单编号：</strong>{{ orderDetail.orderNo }}</p>
          <p><strong>当前状态：</strong><el-tag size="small" type="warning">{{ statusMap[orderDetail.status] }}</el-tag></p>
          <p><strong>下单时间：</strong>{{ formatDate(orderDetail.createdAt) }}</p>
          <p><strong>收货信息：</strong>{{ orderDetail.receiverName }} ({{ orderDetail.receiverPhone }})</p>
          <p><strong>地址：</strong>{{ orderDetail.receiverAddress }}</p>
        </div>
        <el-divider />
        <div class="item-list">
          <div class="item-row" v-for="item in orderDetail.items" :key="item.id">
            <el-image :src="item.productCover" class="item-img" fit="cover" />
            <div class="item-info">
              <div class="item-name">{{ item.productTitle }}</div>
              <div class="item-price">¥{{ item.productPrice }} x {{ item.count }}</div>
            </div>
            <div class="item-total">¥{{ (item.productPrice * item.count).toFixed(2) }}</div>
          </div>
        </div>
        <div class="total-bar">
          实付金额：<span class="final-price">¥{{ orderDetail.payAmount }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted, watch } from 'vue'
import { api } from '../../api'
import { useChat } from '../../hooks/useChat'
import { useUserStore } from '../../stores/user'
import { UserFilled, Service } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { unreadSummary } = storeToRefs(userStore)

const sessions = ref<any[]>([])
const currentOrderNo = ref('')
const inputText = ref('')
const msgBox = ref<HTMLElement | null>(null)

// 详情相关
const detailVisible = ref(false)
const orderDetail = ref<any>(null)
const statusMap: Record<number, string> = {
  0: '待付款', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已关闭', 5: '售后中'
}

// 核心功能：处理实时消息推送对列表的影响
const handleIncomingPush = (msg: any) => {
  const orderNo = String(msg.orderNo)
  const session = sessions.value.find(s => s.orderNo === orderNo)
  if (session) {
    session.lastContent = msg.content
    const idx = sessions.value.indexOf(session)
    if (idx > 0) {
      sessions.value.splice(idx, 1)
      sessions.value.unshift(session)
    }
  } else {
    sessions.value.unshift({ orderNo: orderNo, unreadCount: 1, lastContent: msg.content })
  }
}

// 初始化聊天 Hook
const { messages, connect, sendMessage, disconnect } = useChat(
  () => currentOrderNo.value,
  handleIncomingPush
)

watch(() => messages.value.length, () => {
  scrollToBottom()
})

const fetchSessions = async () => {
  try {
    const res = await api.getUserChatSessions()
    sessions.value = res
    
    // 关键修正：解析 URL 中的单号并强制执行加载
    const qOrderNo = route.query.orderNo as string
    if (qOrderNo) {
      await selectSession(qOrderNo, true)
    } else if (sessions.value.length > 0 && !currentOrderNo.value) {
      await selectSession(sessions.value[0].orderNo)
    }
  } catch (e) {
    console.error('加载会话失败', e)
  }
}

const selectSession = async (orderNo: string, force = false) => {
  // 关键修正：如果是跳转过来的 (force=true)，即便单号相同也必须重新连接拉取
  if (currentOrderNo.value === orderNo && !force) return
  
  currentOrderNo.value = orderNo
  
  // 1. 重启连接 (内部会自动触发 loadHistory)
  await disconnect()
  await connect()
  
  // 2. 同步状态
  userStore.clearUnread(orderNo)
  await api.markChatRead(orderNo).catch(() => {})
  
  scrollToBottom()
}

const handleSend = () => {
  if (!inputText.value.trim()) return
  sendMessage(inputText.value)
  inputText.value = ''
  scrollToBottom()
}

const showOrderDetail = async () => {
  try {
    const res = await api.getOrderDetailByNo(currentOrderNo.value)
    orderDetail.value = res
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取订单详情失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
  })
}

const shouldShowTime = (index: number) => {
  if (index === 0) return true
  const curr = new Date(messages.value[index].createdAt).getTime()
  const prev = new Date(messages.value[index - 1].createdAt).getTime()
  return curr - prev > 5 * 60 * 1000
}

const formatSmartTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const formatDate = (dateStr: string) => dateStr ? new Date(dateStr).toLocaleString() : '-'

onMounted(() => {
  fetchSessions()
})

onUnmounted(() => {
  disconnect()
})
</script>

<style scoped lang="scss">
.message-center { height: 600px; display: flex; flex-direction: column; }
.header { margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.chat-layout { flex: 1; display: flex; background: #fff; border: 1px solid #eee; border-radius: 8px; overflow: hidden; }

.session-list {
  width: 240px; border-right: 1px solid #eee; background: #fafafa; overflow-y: auto;
  .session-item {
    padding: 15px; cursor: pointer; border-bottom: 1px solid #f0f0f0; transition: all 0.3s;
    &:hover { background: #f0f0f0; }
    &.active { background: #fff; border-left: 4px solid #ff5000; }
    .title { font-size: 13px; font-weight: bold; color: #333; margin-bottom: 5px; }
    .last { font-size: 12px; color: #999; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  }
}

.chat-window {
  flex: 1; display: flex; flex-direction: column; background: #f5f5f5;
  .window-header { padding: 12px 20px; background: #fff; border-bottom: 1px solid #eee; font-size: 14px; display: flex; justify-content: space-between; align-items: center; }
  .message-box {
    flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 15px;
    .time-tag { align-self: center; font-size: 12px; color: #bbb; margin: 5px 0; }
    .msg-row {
      display: flex; gap: 10px; max-width: 80%;
      &.mine { align-self: flex-end; flex-direction: row-reverse; }
      &.others { align-self: flex-start; }
      .avatar { width: 32px; height: 32px; border-radius: 4px; background: #eee; display: flex; align-items: center; justify-content: center; color: #999; }
      &.mine .avatar { background: #ff5000; color: #fff; }
      .bubble { padding: 8px 12px; background: #fff; border-radius: 4px; font-size: 14px; line-height: 1.5; box-shadow: 0 1px 2px rgba(0,0,0,0.05); word-break: break-all; }
      &.mine .bubble { background: #95ec69; color: #000; }
    }
  }
  .input-bar { padding: 15px; background: #fff; border-top: 1px solid #eee; .btn-row { display: flex; justify-content: flex-end; margin-top: 10px; } }
}

.order-detail-view {
  .info-section p { margin: 8px 0; font-size: 14px; color: #666; }
  .info-section strong { color: #333; }
  .item-row { display: flex; align-items: center; gap: 15px; margin-bottom: 15px; }
  .item-img { width: 60px; height: 60px; border-radius: 4px; }
  .item-info { flex: 1; .item-name { font-size: 14px; margin-bottom: 5px; } .item-price { color: #999; font-size: 12px; } }
  .item-total { font-weight: bold; color: #333; }
  .total-bar { text-align: right; margin-top: 20px; font-size: 16px; .final-price { color: #ff5000; font-size: 20px; font-weight: bold; } }
}

.empty-window { flex: 1; display: flex; align-items: center; justify-content: center; }
.badge { width: 100%; }
</style>
