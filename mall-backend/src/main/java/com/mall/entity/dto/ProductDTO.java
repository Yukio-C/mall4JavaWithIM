package com.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "商品请求DTO")
public class ProductDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID (修改时必填)")
    private Long id;

    @NotBlank(message = "商品标题不能为空")
    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @NotNull(message = "商品分类不能为空")
    @Schema(description = "商品分类ID")
    private Integer categoryId;

    @Schema(description = "商品封面图URL")
    private String cover;

    @NotNull(message = "当前价格不能为空")
    @Schema(description = "当前售价")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @NotNull(message = "库存不能为空")
    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "商品状态 (0: 下架, 1: 上架)")
    private Integer status;

    @Schema(description = "轮播图列表")
    private java.util.List<String> sliderImgs;

    @Schema(description = "商品图文详情")
    private String detailHtml;

    @Schema(description = "规格参数")
    private java.util.Map<String, String> specs;

    @Schema(description = "售后服务信息")
    private String serviceInfo;
}
