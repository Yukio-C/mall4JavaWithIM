package com.mall.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.enums.OrderStatus;
import com.mall.config.RabbitMQConfig;
import com.mall.entity.po.Order;
import com.mall.mapper.OrderMapper;
import com.mall.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单消息队列监听器
 */
@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = RabbitMQConfig.ORDER_DEAD_LETTER_QUEUE)
public class OrderMQListener {

    private final IOrderService orderService;
    private final OrderMapper orderMapper;

    @RabbitHandler
    public void handleOrderTimeout(String orderNo) {
        log.info("收到订单超时取消消息，订单号: {}", orderNo);

        try {
            Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
            );
            
            if (order == null) {
                log.warn("订单 {} 不存在，取消超时任务", orderNo);
                return;
            }

            // 只有处于“待支付”状态的订单才执行自动取消
            if (OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
                log.info("订单 {} 超时未支付，执行自动取消", orderNo);
                orderService.cancelOrder(order.getId(), order.getUserId());
            } else {
                log.info("订单 {} 状态为 {}，无需执行超时取消", orderNo, order.getStatus());
            }
        } catch (Exception e) {
            log.error("处理订单超时消息失败，订单号: " + orderNo, e);
        }
    }
}
