package com.mall.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "售后商品明细展示对象")
public class AfterSaleItemVO {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品标题")
    private String productTitle;

    @Schema(description = "商品封面图URL")
    private String productCover;

    @Schema(description = "购买单价")
    private BigDecimal productPrice;

    @Schema(description = "售后数量")
    private Integer count;
}
