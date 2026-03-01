import { ref, onUnmounted } from 'vue';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs.js'; 
import { useUserStore } from '../stores/user';
import type { ChatMessage } from '../api/chat';
import request from '../utils/request';

// 增加 onMessage 外部回调，支持页面组件实时同步
export function useChat(orderNoFn: () => string | number, onMessage?: (msg: ChatMessage) => void) {
    const messages = ref<ChatMessage[]>([]);
    const isConnected = ref(false);
    let stompClient: Client | null = null;
    const userStore = useUserStore();

    const getMyId = () => {
        return userStore.user?.id ? String(userStore.user.id) : '';
    };

    // 拉取历史消息
    const loadHistory = async (orderNo: string | number) => {
        if (!orderNo) return;
        try {
            const history = await request.get<any, ChatMessage[]>(`/common/chat/history/${orderNo}`);
            const myId = getMyId();
            
            messages.value = history.map(msg => {
                const senderId = msg.fromUserId ? String(msg.fromUserId) : '';
                return { ...msg, isMine: (myId !== '' && senderId === myId) };
            });

            // 进入即已读，更新内存状态
            await request.post('/common/chat/read', null, { params: { orderNo } });
            userStore.clearUnread(String(orderNo));
        } catch (e) {
            console.error('加载历史失败:', e);
        }
    };

    // 连接 WebSocket
    const connect = async () => {
        const currentOrderNo = orderNoFn();
        
        if (!userStore.user) {
            await userStore.fetchUserInfo().catch(() => {});
        }

        if (currentOrderNo) {
            await loadHistory(currentOrderNo);
        }

        const socket = new SockJS('/api/ws'); 
        
        stompClient = new Client({
            webSocketFactory: () => socket,
            connectHeaders: {
                Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        stompClient.onConnect = (frame) => {
            isConnected.value = true;
            console.log('[ChatHook] 连接成功');
            
            stompClient?.subscribe('/user/queue/chat', (tick) => {
                const msg = JSON.parse(tick.body);
                const myId = getMyId();
                const activeOrderNo = String(orderNoFn());

                // 1. 无论当前在看哪个订单，都先同步给外部(用于列表预览更新)
                if (onMessage) onMessage(msg);

                // 2. 只有属于当前活跃订单的消息才推入屏幕
                if (String(msg.orderNo) === activeOrderNo) {
                    msg.isMine = (myId !== '' && String(msg.fromUserId) === myId);
                    messages.value.push(msg);
                    
                    // 已读处理
                    request.post('/common/chat/read', null, { params: { orderNo: activeOrderNo } });
                } else {
                    // 3. 如果是其他订单的消息，直接内存级增加红点
                    userStore.addUnread(msg.orderNo);
                }
            });
        };

        stompClient.onDisconnect = () => {
            isConnected.value = false;
        };

        stompClient.activate();
    };

    const sendMessage = (content: string, type = 1) => {
        const orderNo = orderNoFn();
        if (stompClient && isConnected.value && orderNo) {
            const chatMsg = {
                orderNo: String(orderNo),
                toUserId: '', 
                content: content,
                msgType: type,
                isRead: 0
            };
            stompClient.publish({
                destination: '/app/chat.send',
                body: JSON.stringify(chatMsg)
            });
        }
    };

    const disconnect = () => {
        if (stompClient) {
            stompClient.deactivate();
            stompClient = null;
        }
        isConnected.value = false;
    };

    onUnmounted(() => disconnect());

    return {
        messages,
        isConnected,
        connect,
        sendMessage,
        disconnect
    };
}
