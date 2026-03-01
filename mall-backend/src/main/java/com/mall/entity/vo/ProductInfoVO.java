package com.mall.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;



import java.math.BigDecimal;


@Data

@Schema(description = "商品基础信息")

public class ProductInfoVO {



    @Schema(description = "商品ID")

    private Long id;



    @Schema(description = "商品标题")

    private String title;



    @Schema(description = "商品简述")
    private String description;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "封面图URL")

    private String cover;



    @Schema(description = "当前价格")

    private BigDecimal price;



    @Schema(description = "原价")

    private BigDecimal originalPrice;



    @Schema(description = "库存")

    private Integer stock;



    @Schema(description = "销量")

    private Integer sales;



        @Schema(description = "综合评分")
        private BigDecimal rating;
    
        @Schema(description = "商品状态 (0:下架, 1:上架)")
        private Integer status;
    }


