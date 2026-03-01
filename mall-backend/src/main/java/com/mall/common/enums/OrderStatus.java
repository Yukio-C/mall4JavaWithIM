package com.mall.common.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 订单状态机枚举
 */
@Getter
public enum OrderStatus {
    PENDING_PAYMENT(0, "待付款"),
    PENDING_SHIPMENT(1, "待发货"),
    SHIPPED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CLOSED(4, "已关闭"),
    AFTER_SALES(5, "售后中");

    private final Integer code;
    private final String description;

    OrderStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 定义合法的后继状态集
     */
    public List<OrderStatus> nextStates() {
        switch (this) {
            case PENDING_PAYMENT:
                return Arrays.asList(PENDING_SHIPMENT, CLOSED);
            case PENDING_SHIPMENT:
                return Arrays.asList(SHIPPED, CLOSED, AFTER_SALES);
            case SHIPPED:
                return Arrays.asList(COMPLETED, AFTER_SALES);
            case COMPLETED:
                return Arrays.asList(AFTER_SALES);
            case AFTER_SALES:
                // 售后是一个中间态，可根据处理结果流转
                return Arrays.asList(PENDING_SHIPMENT, SHIPPED, COMPLETED, CLOSED);
            case CLOSED:
                return Collections.emptyList(); // 终态
            default:
                return Collections.emptyList();
        }
    }

    /**
     * 校验流转是否合法
     */
    public boolean canTransitionTo(OrderStatus next) {
        return nextStates().contains(next);
    }

    public static OrderStatus getByCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
