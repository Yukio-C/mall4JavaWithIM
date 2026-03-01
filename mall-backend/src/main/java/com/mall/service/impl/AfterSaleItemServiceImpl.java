package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.entity.po.AfterSaleItem;
import com.mall.mapper.AfterSaleItemMapper;
import com.mall.service.IAfterSaleItemService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AfterSaleItemServiceImpl extends ServiceImpl<AfterSaleItemMapper, AfterSaleItem> implements IAfterSaleItemService {
    @Override
    public List<AfterSaleItem> getByAfterSaleId(Long afterSaleId) {
        return this.list(new LambdaQueryWrapper<AfterSaleItem>().eq(AfterSaleItem::getAfterSaleId, afterSaleId));
    }
}
