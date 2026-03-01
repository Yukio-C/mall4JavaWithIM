# 商城系统：IM 实时通讯系统全技术栈深度实现指南 (V2.0 终极版)

## 0. 核心定位
本 IM 系统旨在解决“售前咨询”与“售后纠纷”中的高实时性、高可靠性沟通需求。它不仅是一个聊天工具，更是基于 **业务上下文 (OrderNo)** 的工单级通讯系统。

---

## 1. 通信架构演进 (Architecture)

### 1.1 协议栈 (Protocol Stack)
*   **L1 - TCP**: 建立稳定的数据通道。
*   **L2 - HTTP**: 进行初始握手 (WebSocket Handshake)，校验身份。
*   **L3 - WebSocket**: 升级为双全工长连接。
*   **L4 - SockJS**: 提供回退机制（若浏览器不支持 WebSocket，则自动降级为 XHR-Polling 或 Iframe）。
*   **L5 - STOMP (Simple Text Oriented Messaging Protocol)**: 应用层子协议，提供发布/订阅 (Pub/Sub) 语义。

### 1.2 核心拓扑
*   **用户端**: 订阅私有队列 `/user/queue/chat`。
*   **客服端**: 订阅管理队列 `/topic/admin.chat` 或私有队列。
*   **服务端中继 (Message Broker)**: 采用内存级 `SimpleBroker` 进行消息路由，支持高并发转发。

---

## 2. 后端核心组件解析 (Backend Deep Dive)

### 2.1 终极认证拦截器: `WebSocketInterceptor`
这是系统最核心的“守门员”，解决了 **WebSocket 状态化与 Security 无状态化** 的冲突。

#### 2.1.1 身份剥离 (STOMP Header Parsing)
不同于普通的 HTTP Filter，该拦截器直接从 STOMP 的 `CONNECT` 帧中解析 `nativeHeaders`。
```java
// 核心逻辑：从 STOMP 帧中剥离 Authorization
String authToken = accessor.getFirstNativeHeader("Authorization");
// ... 进行 JWT 校验并注入 Principal
accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, authorities));
```

#### 2.1.2 在线状态管理 (Redis Sync)
系统通过 `ONLINE_USER_PREFIX` 和 `ONLINE_STAFF_PREFIX` 在 Redis 中实时记录“谁在线”。
*   **上线**: `CONNECT` 帧到达时写入 Redis，设置 30 分钟过期。
*   **续期**: 只要有任何 `SEND` 或 `SUBSCRIBE` 动作，自动 `expire` 续期。
*   **下线**: 监听 `DISCONNECT` 帧，主动 `delete` Redis 键，实现秒级状态同步。

### 2.2 消息分发控制器: `ChatController`
采用分权控制模式：
1.  **用户上行 (`/app/chat.send`)**: 路由至 `chatService.handleUserMessage`。
2.  **客服上行 (`/app/chat.admin.send`)**: 路由至 `chatService.handleAdminMessage`。
3.  **在线探测 (`/status`)**: 直接扫描 Redis 键，反馈是否有可用客服。

### 2.3 业务逻辑链路 (`ChatService`)
消息处理遵循 **“存 -> 找 -> 发”** 三部曲：
*   **存**: 写入 `after_sales_chat` 表，设置 `is_read = 0`。
*   **找**: 根据消息流向（如用户发给客服），查找目标用户的当前 Session。
*   **发**: 调用 `messagingTemplate.convertAndSendToUser(toUserId, "/queue/chat", msg)`。

---

## 3. 前端通讯 Hook 深度封装 (`useChat.ts`)

### 3.1 生命周期管理
前端不仅是发送消息，更要管理“连接的艺术”：
*   **OnMounted**: 激活 STOMP 客户端，建立 SockJS 链路。
*   **Heartbeat**: 配置 4000ms 的双向心跳，防止被防火墙或代理服务器断开。
*   **Automatic Reconnect**: 配置 `reconnectDelay: 5000`，实现断线后静默重连。

### 3.2 消息智能分发 (Client-Side Routing)
收到消息后，Hook 会进行多级过滤：
1.  **全局监听**: 用于全局未读数（红点）累加。
2.  **上下文过滤**: 如果 `msg.orderNo === currentOrderNo`，则推入对话流视图。
3.  **已读闭环**: 只要消息落入当前活跃视图，立即反向调用后端 `/common/chat/read` 接口，完成闭环。

---

## 4. 关键场景：已读/未读同步机制

这是最体现系统细节的地方：
*   **未读累加**: 用户不在聊天页，后台 WebSocket 收到消息 -> 存入 Redis `unread:{userId}:{orderNo}` -> 前端 Store 计数 +1。
*   **进入页面**: 请求历史消息接口 -> 后端标记该订单下所有消息为 `is_read=1` -> 前端清除红点。
*   **实时阅读**: 用户在聊天页，对方发来消息 -> 前端推入视图 -> 自动触发 API 回调 -> 标记已读。

---

## 5. 性能与安全优化

### 5.1 数据库索引优化
为了承载百万级聊天记录，`after_sales_chat` 表必须建立复合索引：
*   `INDEX idx_order_time (order_no, created_at)`: 用于快速拉取某一订单的历史记录（由于是正序加载，时间戳作为第二分量极佳）。

### 5.2 安全防护
*   **非法投递拦截**: 在 `ChatController` 中校验 `fromUserId` 是否为当前 `Principal`。防止用户伪造他人 ID 发送消息。
*   **内容脱敏**: 在消息存库前，可以集成脏词库或正则表达式进行过滤。

---

## 6. 部署运维建议 (Deployment)

### 6.1 Nginx 反向代理配置
这是 WebSocket 部署最常见的“坑”，必须确保 Header 透传：
```nginx
location /api/ws {
    proxy_pass http://backend:9090;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "Upgrade";
    proxy_set_header Host $host;
}
```

### 6.2 集群扩展建议
如果未来后端需要多机部署，`SimpleBroker`（内存级）将不再适用。建议：
1.  **引入外部 Broker**: 如 RabbitMQ 的 STOMP 插件。
2.  **订阅共享通道**: 所有后端节点订阅同一个消息队列，确保无论用户连接在哪个节点，都能收到消息。

---

## 7. 数据结构参考 (MySQL)
```sql
CREATE TABLE `after_sales_chat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `from_user_id` bigint NOT NULL COMMENT '发送人ID',
  `to_user_id` bigint NOT NULL COMMENT '接收人ID',
  `content` text NOT NULL COMMENT '消息内容',
  `msg_type` tinyint DEFAULT '1' COMMENT '消息类型: 1文本 2图片',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_from_to` (`from_user_id`,`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
