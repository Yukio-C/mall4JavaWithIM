package com.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "售后申请商品明细")
public class AfterSaleItemDTO {

    @Schema(description = "关联订单项ID")
    private Long orderItemId;

    @Schema(description = "售后数量")
    private Integer count;
}
