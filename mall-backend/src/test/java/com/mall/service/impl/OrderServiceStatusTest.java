package com.mall.service.impl;

import com.mall.common.enums.OrderStatus;
import com.mall.entity.po.Order;
import com.mall.mapper.OrderMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * 验证 OrderService 的状态流转异常拦截
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceStatusTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("测试：取消一个【已完成】的订单，应当抛出异常")
    void testCancelCompletedOrderThrowsException() {
        Long orderId = 100L;
        Long userId = 1L;
        
        // 模拟一个已完成状态的订单 (Status = 3)
        Order completedOrder = new Order();
        completedOrder.setOrderNo("TEST_001");
        completedOrder.setId(orderId);
        completedOrder.setUserId(userId);
        completedOrder.setStatus(OrderStatus.COMPLETED.getCode());

        // 模拟 MyBatis Plus 的 getById
        when(orderMapper.selectById(orderId)).thenReturn(completedOrder);

        // 执行取消操作，断言必须抛出 RuntimeException
        assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder(orderId, userId);
        }, "已完成的订单不应被取消，但逻辑未拦截！");
    }

    @Test
    @DisplayName("测试：对【待支付】订单进行收货，应当抛出异常")
    void testConfirmUnpaidOrderThrowsException() {
        Long orderId = 101L;
        Long userId = 1L;

        // 模拟一个待支付订单 (Status = 0)
        Order unpaidOrder = new Order();
        unpaidOrder.setOrderNo("TEST_002");
        unpaidOrder.setId(orderId);
        unpaidOrder.setUserId(userId);
        unpaidOrder.setStatus(OrderStatus.PENDING_PAYMENT.getCode());

        when(orderMapper.selectById(orderId)).thenReturn(unpaidOrder);

        // 执行确认收货，断言抛出异常
        assertThrows(RuntimeException.class, () -> {
            //orderService.confirmReceipt(orderId, userId);
        }, "待支付的订单不应被确认收货，但逻辑未拦截！");
    }
}
