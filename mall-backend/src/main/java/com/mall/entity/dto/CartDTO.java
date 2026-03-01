package com.mall.entity.dto;

import com.mall.entity.po.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "添加购物车商品请求参数")
public class CartDTO {
    @Schema(description = "购物车ID (修改时必填)")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "数量")
    @Min(1)
    private Integer count;
}