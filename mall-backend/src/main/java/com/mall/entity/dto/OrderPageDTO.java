package com.mall.entity.dto;

import com.mall.entity.po.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单分页查询参数")
public class OrderPageDTO extends Page {

    @Schema(description = "订单状态: 0待付款, 1待发货, 2已发货, 3已完成, 4已关闭, 5售后中", example = "0")
    private Integer status;

    @Schema(description = "查询关键字 (订单号/收货人/手机号)")
    private String keyword;

    @Schema(description = "用户ID过滤 (管理员可用)")
    private Long userId;
}
