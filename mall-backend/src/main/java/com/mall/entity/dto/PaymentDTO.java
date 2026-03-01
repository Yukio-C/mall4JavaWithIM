package com.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 支付请求 DTO
 */
@Data
@Schema(description = "支付请求参数")
public class PaymentDTO implements Serializable {

    @NotBlank(message = "订单号不能为空")
    @Schema(description = "系统订单号", example = "202310240001")
    private String orderNo;

    @NotNull(message = "支付方式不能为空")
    @Schema(description = "支付方式: 1-支付宝, 2-微信, 3-余额", example = "3")
    private Integer payType;
}
