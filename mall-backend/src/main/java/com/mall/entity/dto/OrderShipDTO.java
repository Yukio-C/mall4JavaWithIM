package com.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "订单发货DTO")
public class OrderShipDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "物流单号不能为空")
    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "物流公司名称 (选填)")
    private String logisticsCompany;
}
