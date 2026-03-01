package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.entity.po.AfterSaleItem;
import java.util.List;

public interface IAfterSaleItemService extends IService<AfterSaleItem> {
    /**
     * 根据售后ID获取明细列表
     */
    List<AfterSaleItem> getByAfterSaleId(Long afterSaleId);
}
