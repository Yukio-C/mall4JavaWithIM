package com.mall.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付结果 VO
 */
@Data
@Schema(description = "支付结果信息")
public class PaymentVO implements Serializable {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付平台流水号")
    private String tradeNo;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付状态: 0-进行中, 1-成功, 2-失败")
    private Integer status;

    @Schema(description = "支付方式名称")
    private String payTypeName;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;
}
