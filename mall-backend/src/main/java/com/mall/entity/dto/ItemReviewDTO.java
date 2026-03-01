package com.mall.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ItemReviewDTO {
    @NotNull(message = "订单明细ID不能为空")
    private Long orderItemId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Min(value = 1, message = "评分最小1星")
    @Max(value = 5, message = "评分最大5星")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    private String content;

    private List<String> images;

    private Boolean isAnonymous = false;
}