<template>
  <div class="chat-container">
    <div class="chat-main">
      <!-- 左侧会话列表 -->
      <div class="session-list">
        <div class="list-header">咨询会话 ({{ sessions.length }})</div>
        <div 
          v-for="s in sessions" 
          :key="s.orderNo" 
          class="session-item" 
          :class="{ active: currentOrderNo === s.orderNo }"
          @click="selectSession(s.orderNo)"
        >
          <el-badge :value="s.unreadCount" :hidden="s.unreadCount <= 0 && !hasNew[s.orderNo]" class="item-badge">
            <div class="session-info">
              <div class="order-no">订单号: {{ s.orderNo }}</div>
              <div class="last-msg">{{ s.lastContent || '暂无新消息' }}</div>
            </div>
          </el-badge>
        </div>
        <el-empty v-if="sessions.length === 0" description="暂无咨询" />
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-area" v-if="currentOrderNo">
        <div class="chat-header">
          正在处理订单: <strong>{{ currentOrderNo }}</strong>
        </div>
        
        <div class="message-box" ref="msgBox">
          <template v-for="(msg, index) in messages" :key="index">
            <!-- 智能时间轴 -->
            <div v-if="shouldShowTime(index)" class="time-divider">
              {{ formatSmartTime(msg.createdAt) }}
            </div>
            
            <!-- 消息项 -->
            <div :class="['msg-item', isMyMsg(msg) ? 'mine' : 'others']">
              <div class="avatar">
                <el-icon v-if="isMyMsg(msg)"><Service /></el-icon>
                <el-icon v-else><UserFilled /></el-icon>
              </div>
              <div class="bubble">
                <div class="content">{{ msg.content }}</div>
              </div>
            </div>
          </template>
          
          <div v-if="messages.length === 0" class="chat-empty">
            暂无聊天记录，客服接入后可直接沟通。
          </div>
        </div>

        <div class="input-area">
          <el-input
            v-model="inputMsg"
            type="textarea"
            :rows="3"
            placeholder="请输入回复内容 (Enter 发送)"
            @keyup.enter="handleSend"
          />
          <div class="actions">
            <el-button type="primary" @click="handleSend" :disabled="!inputMsg.trim()">发送回复</el-button>
          </div>
        </div>
      </div>

      <div class="empty-board" v-else>
        <el-result icon="info" title="请在左侧选择会话" sub-title="实时接收来自用户的售后咨询" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, reactive } from 'vue'
import { api } from '../api'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import { UserFilled, Service, ChatDotRound } from '@element-plus/icons-vue'

const userStore = useUserStore()
const sessions = ref<any[]>([])
const currentOrderNo = ref('')
const messages = ref<any[]>([])
const inputMsg = ref('')
const msgBox = ref<HTMLElement | null>(null)

// 关键逻辑：判断消息是否由当前登录客服发出
const isMyMsg = (msg: any) => {
  // 统一转为 String 进行比对
  return String(msg.fromUserId) === String(userStore.user?.id)
}

// 辅助状态
const lastMsgs = reactive<Record<string, string>>({})
const hasNew = reactive<Record<string, boolean>>({})

let stompClient: any = null

const fetchSessions = async () => {
  try {
    const res = await api.getChatSessions()
    // 确保 res 是数组
    sessions.value = res || []
    
    if (sessions.value.length > 0 && !currentOrderNo.value) {
      selectSession(sessions.value[0].orderNo)
    }
  } catch (e) {
    console.error('获取会话失败', e)
  }
}

const selectSession = async (orderNo: string) => {
  if (!orderNo) return
  currentOrderNo.value = orderNo
  
  // 1. 通知后端并清除本地未读状态
  try {
    await api.markChatRead(orderNo)
    const session = sessions.value.find(s => s.orderNo === orderNo)
    if (session) {
      session.unreadCount = 0
    }
  } catch (e) {
    console.error('标记已读失败', e)
  }

  // 2. 加载历史记录
  try {
    const res = await api.getChatHistory(orderNo)
    messages.value = res || []
    scrollToBottom()
  } catch (e) {
    ElMessage.error('加载历史记录失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (msgBox.value) {
      msgBox.value.scrollTop = msgBox.value.scrollHeight
    }
  })
}

// 智能时间轴逻辑
const shouldShowTime = (index: number) => {
  if (index === 0) return true
  const currTime = messages.value[index].createdAt
  const prevTime = messages.value[index - 1].createdAt
  if (!currTime || !prevTime) return false
  const curr = new Date(currTime).getTime()
  const prev = new Date(prevTime).getTime()
  return curr - prev > 10 * 60 * 1000 // 10分钟间隔
}

const formatSmartTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  if (isToday) return `${h}:${m}`
  return `${date.getMonth() + 1}月${date.getDate()}日 ${h}:${m}`
}

// WebSocket 连接
const initWebSocket = () => {
  const socket = new SockJS('http://localhost:9090/ws')
  stompClient = Stomp.over(socket)
  stompClient.debug = () => {}

  const headers = { Authorization: userStore.token || '' }

  stompClient.connect(headers, () => {
    console.log('[ChatBoard] WebSocket 连接成功')
    stompClient.subscribe('/user/queue/chat', (res: any) => {
      const msg = JSON.parse(res.body)
      handleIncomingMessage(msg)
    })
  }, (err: any) => {
    console.error('WS 连接失败', err)
    setTimeout(initWebSocket, 5000)
  })
}

const handleIncomingMessage = (msg: any) => {
  // 1. 安全拦截：缺失订单号的消息不予处理
  if (!msg || !msg.orderNo) return

  const orderNo = String(msg.orderNo)

  // 2. 检查会话是否存在
  const existingSession = sessions.value.find(s => String(s.orderNo) === orderNo)
  
  if (!existingSession) {
    // 首次咨询：创建新会话对象并置顶
    sessions.value.unshift({
      orderNo: orderNo,
      unreadCount: currentOrderNo.value === orderNo ? 0 : 1,
      lastContent: msg.content
    })
  } else {
    // 已有会话：更新数据
    existingSession.lastContent = msg.content
    if (currentOrderNo.value !== orderNo) {
      existingSession.unreadCount++
    }
    // 置顶活跃会话
    const idx = sessions.value.indexOf(existingSession)
    if (idx > 0) {
      sessions.value.splice(idx, 1)
      sessions.value.unshift(existingSession)
    }
  }

  // 3. 如果当前正在看这个订单，直接追加消息
  if (currentOrderNo.value === orderNo) {
    messages.value.push(msg)
    scrollToBottom()
  }
}

const handleSend = () => {
  if (!inputMsg.value.trim() || !currentOrderNo.value) return

  const payload = {
    orderNo: currentOrderNo.value,
    content: inputMsg.value.trim(),
    msgType: 1
  }

  if (stompClient && stompClient.connected) {
    // 发送到客服专用回复端点
    stompClient.send('/app/chat.admin.send', {}, JSON.stringify(payload))
    inputMsg.value = ''
  } else {
    ElMessage.error('聊天服务器未连接，请刷新页面')
  }
}

onMounted(() => {
  fetchSessions()
  initWebSocket()
})

onUnmounted(() => {
  if (stompClient) stompClient.disconnect()
})
</script>

<style scoped lang="scss">
.chat-container {
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  border: 1px solid #ebeef5;
}

.chat-main {
  display: flex;
  height: 100%;
}

.session-list {
  width: 280px;
  border-right: 1px solid #ebeef5;
  background: #f8f9fb;
  display: flex;
  flex-direction: column;

  .list-header {
    padding: 15px;
    font-weight: bold;
    border-bottom: 1px solid #ebeef5;
    background: #fff;
    font-size: 14px;
  }

  .session-item {
    padding: 15px;
    cursor: pointer;
    transition: all 0.3s;
    border-bottom: 1px solid #f0f2f5;

    &:hover { background: #f0f5ff; }
    &.active { 
      background: #e6f1ff;
      border-left: 4px solid #409eff;
    }

    .session-info {
      .order-no { font-size: 13px; color: #333; margin-bottom: 5px; font-weight: 500; }
      .last-msg { font-size: 12px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    }
  }
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f4f4f4;

  .chat-header {
    padding: 12px 20px;
    border-bottom: 1px solid #eee;
    background: #fff;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .message-box {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 12px;

    /* 智能时间轴线 */
    .time-divider { 
      align-self: center; 
      font-size: 12px; 
      color: #999; 
      background: rgba(0,0,0,0.06); 
      padding: 2px 10px; 
      border-radius: 12px; 
      margin: 10px 0; 
    }

    .msg-item {
      display: flex;
      gap: 10px;
      max-width: 85%;
      align-self: flex-start;

      &.mine {
        align-self: flex-end;
        flex-direction: row-reverse;
      }

      .avatar {
        width: 34px;
        height: 34px;
        border-radius: 4px;
        background: #fff;
        color: #666;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        flex-shrink: 0;
        border: 1px solid #eee;
      }

      &.mine .avatar {
        background: #1890ff;
        color: #fff;
        border: none;
      }

      .bubble {
        padding: 10px 12px;
        border-radius: 4px;
        background: #fff;
        position: relative;
        font-size: 14px;
        line-height: 1.5;
        box-shadow: 0 1px 2px rgba(0,0,0,0.1);
        word-break: break-all;

        &::before {
          content: "";
          position: absolute;
          width: 0;
          height: 0;
          border: 6px solid transparent;
          top: 10px;
        }
      }

      &.others .bubble::before {
        border-right-color: #fff;
        left: -12px;
      }

      &.mine .bubble {
        background: #95ec69;
        &::before {
          border-left-color: #95ec69;
          right: -12px;
        }
      }
    }
  }

  .input-area {
    padding: 15px;
    border-top: 1px solid #eee;
    background: #fff;

    .actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 10px;
    }
  }
}

.empty-board {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fb;
}

.item-badge {
  width: 100%;
}
</style>
