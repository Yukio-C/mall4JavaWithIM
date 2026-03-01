package com.mall.common.enums;

import lombok.Getter;

/**
 * 通用返回码枚举
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统内部异常"),
    BAD_REQUEST(400, "错误的请求"),
    UNAUTHORIZED(401, "未授权或登录失效"),
    FORBIDDEN(403, "无权限操作"),
    
    // 业务相关
    USER_NOT_FOUND(1001, "用户不存在"),
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    STOCK_INSUFFICIENT(2002, "商品库存不足"),
    ADDRESS_NOT_FOUND(3001, "收货地址不存在"),
    ORDER_NOT_FOUND(4011, "订单不存在"),
    ORDER_STATUS_ERROR(4012, "订单状态异常，无法支付"),
    AUTH_ERROR(1002, "用户名或密码错误"),
    BALANCE_INSUFFICIENT(1003, "账户余额不足");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
