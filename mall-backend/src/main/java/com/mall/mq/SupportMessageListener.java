package com.mall.mq;

import com.mall.config.RabbitMQConfig;
import com.mall.config.WebSocketInterceptor;
import com.mall.entity.po.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 客服抢单与消息分发监听器 (模拟多个客服同时竞争)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SupportMessageListener {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String ROUTE_KEY_PREFIX = "chat:route:";
    
    // 这里模拟当前系统的客服 ID。如果是真实分布式系统，这里应该是当前登录管理员的标识
    private static final String CURRENT_ADMIN_ID = "admin"; 

    /**
     * 监听客服等待队列
     * RabbitMQ 默认的 prefetch 会保证消息在多个 Consumer 之间公平分发
     */
    @RabbitListener(queues = RabbitMQConfig.SUPPORT_WAIT_QUEUE)
    public void handleSupportMessage(ChatMessage msg) {
        String orderNo = msg.getOrderNo();
        String routeKey = ROUTE_KEY_PREFIX + orderNo;

        log.info("客服 {} 从排队队列中抢到订单 {} 的消息", CURRENT_ADMIN_ID, orderNo);

        // 1. 将该订单会话绑定到当前客服，有效期设为 2 小时
        // (如果是高并发抢单，可以用 SETNX (setIfAbsent) 确保同一时间只有一个客服抢到第一个包)
        Boolean success = redisTemplate.opsForValue().setIfAbsent(routeKey, CURRENT_ADMIN_ID, 2, TimeUnit.HOURS);
        
        if (Boolean.TRUE.equals(success) || CURRENT_ADMIN_ID.equals(redisTemplate.opsForValue().get(routeKey))) {
            // 抢单成功，或者该订单本来就归我管
            
            // 2. 将排队的消息推送给当前客服前端
            msg.setToUserId(CURRENT_ADMIN_ID);
            messagingTemplate.convertAndSendToUser(CURRENT_ADMIN_ID, "/queue/chat", msg);
            
            // 3. 告诉用户：客服是否真正接入
            ChatMessage systemReply = new ChatMessage();
            systemReply.setOrderNo(orderNo);
            systemReply.setFromUserId("SYSTEM");
            systemReply.setToUserId(msg.getFromUserId());
            
            // 检查 admin 是否在线
            Boolean isAdminOnline = redisTemplate.hasKey(WebSocketInterceptor.ONLINE_STAFF_PREFIX + CURRENT_ADMIN_ID);
            if (Boolean.TRUE.equals(isAdminOnline)) {
                systemReply.setContent("客服专员 [" + CURRENT_ADMIN_ID + "] 已为您接入，请描述您的详细问题。");
            } else {
                systemReply.setContent("当前客服暂不在线，您的咨询已转入离线模式。请详细留言，客服上线后会第一时间回复您。");
            }
            
            systemReply.setMsgType(1);
            systemReply.setCreatedAt(LocalDateTime.now());
            
            messagingTemplate.convertAndSendToUser(msg.getFromUserId(), "/queue/chat", systemReply);
            
        } else {
            // 订单已经被其他客服抢走（这里通常只在多个不同的 admin 实例下发生）
            String owner = redisTemplate.opsForValue().get(routeKey);
            log.info("订单 {} 已被客服 {} 接管，放弃处理", orderNo, owner);
            // 理论上如果绑定成功，后续消息不再进队列，所以这里是极小概率事件
        }
    }
}
