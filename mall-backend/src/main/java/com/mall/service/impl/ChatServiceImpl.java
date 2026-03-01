package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.config.RabbitMQConfig;
import com.mall.config.WebSocketInterceptor;
import com.mall.entity.po.ChatMessage;
import com.mall.entity.po.Order;
import com.mall.entity.po.User;
import com.mall.entity.vo.ChatSessionVO;
import com.mall.mapper.ChatMessageMapper;
import com.mall.mapper.UserMapper;
import com.mall.service.IChatService;
import com.mall.service.IOrderService;
import com.mall.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatService {

    private final IOrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private static final String ROUTE_KEY_PREFIX = "chat:route:";
    private static final String ADMIN_SESSIONS_PREFIX = "chat:admin:sessions:";

    private final ExecutorService chatExecutor = new ThreadPoolExecutor(
        10, 20, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1000),
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Override
    public List<ChatMessage> getChatHistory(String orderNo) {
        return this.list(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getOrderNo, orderNo)
                .orderByAsc(ChatMessage::getCreatedAt));
    }

    @Override
    public void handleUserMessage(ChatMessage msg, SimpMessageHeaderAccessor accessor) {
        String userId = getUserId(accessor);
        chatExecutor.submit(() -> {
            try {
                processUserMessage(msg, userId);
            } catch (Exception e) {
                log.error("处理用户消息异常: ", e);
            }
        });
    }

    private String getUserId(SimpMessageHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal != null) return principal.getName();
        if (accessor.getSessionAttributes() != null) {
            return (String) accessor.getSessionAttributes().get("userId");
        }
        return null;
    }

    private void processUserMessage(ChatMessage msg, String resolvedUserId) {
        String userId = resolvedUserId;
        if (userId == null && msg.getOrderNo() != null) {
            Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, msg.getOrderNo()));
            if (order != null) userId = String.valueOf(order.getUserId());
        }

        if (userId == null) return;

        msg.setFromUserId(userId);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setIsRead(0);
        if (msg.getMsgType() == null) msg.setMsgType(1);

        boolean isRouted = routeMessage(msg);
        this.save(msg);

        if (isRouted) {
            redisTemplate.opsForSet().add(ADMIN_SESSIONS_PREFIX + msg.getToUserId(), msg.getOrderNo());
        } else {
            sendSystemPrompt(msg);
            rabbitTemplate.convertAndSend(RabbitMQConfig.SUPPORT_EXCHANGE, RabbitMQConfig.SUPPORT_WAIT_ROUTING_KEY, msg);
        }

        messagingTemplate.convertAndSendToUser(userId, "/queue/chat", msg);
    }

    private boolean routeMessage(ChatMessage msg) {
        String routeKey = ROUTE_KEY_PREFIX + msg.getOrderNo();
        String assignedAdminId = redisTemplate.opsForValue().get(routeKey);

        if (assignedAdminId != null) {
            Boolean isOnline = redisTemplate.hasKey(WebSocketInterceptor.ONLINE_STAFF_PREFIX + assignedAdminId);
            if (Boolean.TRUE.equals(isOnline)) {
                msg.setToUserId(assignedAdminId);
                messagingTemplate.convertAndSendToUser(assignedAdminId, "/queue/chat", msg);
                return true;
            } else {
                redisTemplate.delete(routeKey);
            }
        }

        Set<String> onlineAdminKeys = redisTemplate.keys(WebSocketInterceptor.ONLINE_STAFF_PREFIX + "*");
        if (onlineAdminKeys != null && !onlineAdminKeys.isEmpty()) {
            List<String> onlineAdmins = onlineAdminKeys.stream()
                    .map(key -> key.replace(WebSocketInterceptor.ONLINE_STAFF_PREFIX, ""))
                    .collect(Collectors.toList());
            
            String targetAdminId = onlineAdmins.get(new Random().nextInt(onlineAdmins.size()));
            redisTemplate.opsForValue().set(routeKey, targetAdminId, 2, TimeUnit.HOURS);
            
            msg.setToUserId(targetAdminId);
            log.info("会话重新路由: {} -> 客服ID {}", msg.getOrderNo(), targetAdminId);
            
            messagingTemplate.convertAndSendToUser(targetAdminId, "/queue/chat", msg);
            return true;
        }
        return false;
    }

    @Override
    public void handleAdminMessage(ChatMessage msg, SimpMessageHeaderAccessor accessor) {
        String adminId = getUserId(accessor);
        if (adminId == null) return;

        msg.setFromUserId(adminId);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setIsRead(0);
        if (msg.getMsgType() == null) msg.setMsgType(1);

        Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, msg.getOrderNo()));
        if (order != null) {
            String targetUserId = String.valueOf(order.getUserId());
            msg.setToUserId(targetUserId);
            this.save(msg);
            
            redisTemplate.opsForSet().add(ADMIN_SESSIONS_PREFIX + adminId, msg.getOrderNo());
            
            messagingTemplate.convertAndSendToUser(targetUserId, "/queue/chat", msg);
            messagingTemplate.convertAndSendToUser(adminId, "/queue/chat", msg);
        }
    }

    @Override
    public List<ChatSessionVO> getMySessionsVO(String adminId) {
        String cacheKey = ADMIN_SESSIONS_PREFIX + adminId;
        Boolean hasCache = redisTemplate.hasKey(cacheKey);
        List<String> orderNos;

        if (Boolean.TRUE.equals(hasCache)) {
            Set<String> cachedOrderNos = redisTemplate.opsForSet().members(cacheKey);
            orderNos = (cachedOrderNos != null) ? new ArrayList<>(cachedOrderNos) : new ArrayList<>();
        } else {
            orderNos = baseMapper.selectOrderNosByAdminId(adminId);
            if (!orderNos.isEmpty()) {
                redisTemplate.opsForSet().add(cacheKey, orderNos.toArray(new String[0]));
                redisTemplate.expire(cacheKey, 24, TimeUnit.HOURS);
            }
        }
        
        List<ChatSessionVO> result = new ArrayList<>();
        for (String no : orderNos) {
            int unread = baseMapper.countUnread(no, adminId);
            String lastMsg = baseMapper.selectLastContent(no);
            result.add(new ChatSessionVO(no, unread, lastMsg));
        }
        return result;
    }

    @Override
    public List<ChatSessionVO> getUserSessionsVO(String userId) {
        List<String> orderNos = baseMapper.selectOrderNosByUserId(userId);
        List<ChatSessionVO> result = new ArrayList<>();
        for (String no : orderNos) {
            int unread = baseMapper.countUserUnread(no, userId);
            String lastMsg = baseMapper.selectLastContent(no);
            result.add(new ChatSessionVO(no, unread, lastMsg));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(String orderNo, String identifier) {
        this.update(new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getOrderNo, orderNo)
                .eq(ChatMessage::getToUserId, identifier)
                .set(ChatMessage::getIsRead, 1));
    }


    @Override
    public Map<String, Integer> getUserUnreadSummary(String userId) {
        Map<String, Integer> summary = new HashMap<>();
        summary.put("total", baseMapper.countUserTotalUnread(userId));
        List<Map<String, Object>> list = baseMapper.selectUnreadCountsByOrder(userId);
        for (Map<String, Object> item : list) {
            String orderNo = (String) item.get("order_no");
            Number count = (Number) item.get("count");
            summary.put(orderNo, count.intValue());
        }
        return summary;
    }

    private void sendSystemPrompt(ChatMessage userMsg) {
        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setOrderNo(userMsg.getOrderNo());
        systemMsg.setFromUserId("SYSTEM");
        systemMsg.setToUserId(userMsg.getFromUserId());
        systemMsg.setContent("正在为您连接在线客服，请稍候...");
        systemMsg.setMsgType(1);
        systemMsg.setCreatedAt(LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(userMsg.getFromUserId(), "/queue/chat", systemMsg);
    }
}
