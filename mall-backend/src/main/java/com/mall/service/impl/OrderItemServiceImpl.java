package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.entity.po.OrderItem;
import com.mall.mapper.OrderItemMapper;
import com.mall.service.IOrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveItems(List<OrderItem> items) {
        log.info("保存订单明细，数量: {}", items.size());
        this.saveBatch(items);
    }

    @Override
    public List<OrderItem> getByOrderId(Long orderId) {
        log.info("查询订单明细，订单ID: {}", orderId);
        return this.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }
}
