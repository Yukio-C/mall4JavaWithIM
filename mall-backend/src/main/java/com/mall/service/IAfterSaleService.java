package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.PageResult;
import com.mall.entity.dto.AfterSaleDTO;
import com.mall.entity.dto.AfterSalePageDTO;
import com.mall.entity.dto.OrderPageDTO;
import com.mall.entity.po.AfterSale;
import com.mall.entity.vo.AfterSaleVO;
import com.mall.security.LoginUser;

public interface IAfterSaleService extends IService<AfterSale> {
    PageResult<AfterSaleVO> getAfterSaleList(AfterSalePageDTO afterSalePageDTO, LoginUser loginUser);

    void apply4AfterSale(AfterSaleDTO afterSaleDTO, LoginUser loginUser);

    /**
     * 管理员处理售后申请 (同意/拒绝)
     */
    void handleAfterSale(AfterSaleDTO afterSaleDTO);
}
