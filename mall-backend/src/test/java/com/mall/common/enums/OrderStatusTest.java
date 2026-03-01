package com.mall.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单状态机流转单元测试
 */
class OrderStatusTest {

    @Test
    @DisplayName("验证核心业务流转：待支付 -> 待发货 -> 已发货 -> 已完成")
    void testMainLifecycle() {
        assertTrue(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.PENDING_SHIPMENT));
        assertTrue(OrderStatus.PENDING_SHIPMENT.canTransitionTo(OrderStatus.SHIPPED));
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.COMPLETED));
    }

    @Test
    @DisplayName("验证非法流转：禁止从未支付状态直接跨越到完成状态")
    void testInvalidTransition() {
        assertFalse(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.COMPLETED));
        assertFalse(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.SHIPPED));
        assertFalse(OrderStatus.PENDING_SHIPMENT.canTransitionTo(OrderStatus.COMPLETED));
    }

    @Test
    @DisplayName("验证取消/关闭逻辑：待支付和待发货均可关闭订单")
    void testCancelLogic() {
        // 1. 待支付可以关闭（超时或手动）
        assertTrue(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.CLOSED));
        // 2. 待发货可以关闭（申请退款）
        assertTrue(OrderStatus.PENDING_SHIPMENT.canTransitionTo(OrderStatus.CLOSED));
        // 3. 已发货不可直接关闭，需走售后
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CLOSED));
        // 4. 已完成不可关闭
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.CLOSED));
    }

    @Test
    @DisplayName("验证售后流转：支持售后作为中间态进行流转")
    void testAfterSalesFlow() {
        // 几乎所有环节（除未支付外）理论上都可能进入售后
        assertTrue(OrderStatus.PENDING_SHIPMENT.canTransitionTo(OrderStatus.AFTER_SALES));
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.AFTER_SALES));
        assertTrue(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.AFTER_SALES));

        // 售后处理完后可以回到已发货或直接关闭/完成
        assertTrue(OrderStatus.AFTER_SALES.canTransitionTo(OrderStatus.SHIPPED));
        assertTrue(OrderStatus.AFTER_SALES.canTransitionTo(OrderStatus.COMPLETED));
        assertTrue(OrderStatus.AFTER_SALES.canTransitionTo(OrderStatus.CLOSED));
    }

    @Test
    @DisplayName("验证终态：CLOSED是终态，不可再流转")
    void testClosedIsFinalState() {
        for (OrderStatus status : OrderStatus.values()) {
            if (status == OrderStatus.CLOSED) continue;
            assertFalse(OrderStatus.CLOSED.canTransitionTo(status), 
                "CLOSED状态不应再流转到: " + status.getDescription());
        }
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    @DisplayName("验证通过Code获取枚举的准确性")
    void testGetByCode(OrderStatus status) {
        assertEquals(status, OrderStatus.getByCode(status.getCode()));
    }

    @Test
    @DisplayName("验证非法Code返回null")
    void testGetByInvalidCode() {
        assertNull(OrderStatus.getByCode(-1));
        assertNull(OrderStatus.getByCode(99));
    }
}
