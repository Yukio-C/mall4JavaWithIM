package com.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "售后申请/状态更新请求参数")
public class AfterSaleDTO {

    @Schema(description = "售后表ID (更新状态时必填)")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "关联订单号")
    private String orderNo;

    @Schema(description = "状态: 0-待审核, 1-处理中, 2-已完成, 3-已拒绝")
    private Integer status;

    @NotNull
    @Schema(description = "售后类型: 1-仅退款, 2-退货退款, 3-换货",example = "1")
    private Integer type;

    @Schema(description = "申请简要原因")
    @NotNull
    private String reason;

    @NotNull
    @Schema(description = "详细问题描述")
    private String description;

    @Schema(description = "凭证图片URL列表")
    private List<String> images;

    @Schema(description = "售后商品列表")
    private List<AfterSaleItemDTO> items;
}
