package com.mall.entity.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "售后信息展示对象")
public class AfterSaleVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "关联订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "退款总金额")
    private BigDecimal refundAmount;

    @Schema(description = "商品标题")
    private String productTitle;

    @Schema(description = "商品封面图URL")
    private String productCover;

    @Schema(description = "售后类型: 1-仅退款, 2-退货退款, 3-换货")
    private Integer type;

    @Schema(description = "申请原因简述")
    private String reason;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "凭证图片URL列表")
    private List<String> images;

    @Schema(description = "进度: 1-待审核, 2-处理中, 3-已完成, 4-已拒绝")
    private Integer status;

    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    @Schema(description = "商家处理备注")
    private String handleRemark;

    @Schema(description = "售后商品明细列表")
    private List<AfterSaleItemVO> items;
}
