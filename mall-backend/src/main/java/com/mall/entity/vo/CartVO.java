package com.mall.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "购物车信息展示对象")
public class CartVO {

    @Schema(description = "购物车记录ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品标题")
    private String productTitle;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品封面图")
    private String productCover;

    @Schema(description = "购买数量")
    private Integer count;
}
