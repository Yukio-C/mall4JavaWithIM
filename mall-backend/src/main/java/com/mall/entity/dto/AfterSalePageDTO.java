package com.mall.entity.dto;

import com.mall.entity.po.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "售后请求参数")
public class AfterSalePageDTO extends Page {

    @Schema(description = "售后订单状态:0:待审核, 1:处理中, 2:已完成, 3:已拒绝", example = "0")
    private Integer status;
}
