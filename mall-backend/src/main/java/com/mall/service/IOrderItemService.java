package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.entity.po.OrderItem;
import java.util.List;

public interface IOrderItemService extends IService<OrderItem> {
    /**
     * 保存订单明细列表
     */
    void saveItems(List<OrderItem> items);

    /**
     * 根据订单ID获取明细列表
     */
    List<OrderItem> getByOrderId(Long orderId);
}
