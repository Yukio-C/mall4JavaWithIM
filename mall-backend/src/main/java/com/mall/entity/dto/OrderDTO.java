package com.mall.entity.dto;


import com.mall.entity.po.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "订单请求参数")
public class OrderDTO {

    private Integer addressId;
    private List<OrderItem> items;
    private String orderToken; // 幂等性 Token
}
