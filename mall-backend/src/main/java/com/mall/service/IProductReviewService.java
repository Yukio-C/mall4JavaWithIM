package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.PageResult;
import com.mall.entity.dto.ProductReviewDTO;
import com.mall.entity.po.ProductReview;
import com.mall.entity.vo.ProductReviewVO;

public interface IProductReviewService extends IService<ProductReview> {
    /**
     * 提交订单评价
     */
    void submitReview(ProductReviewDTO reviewDTO, Long userId);

    /**
     * 获取我的评价列表 (分页)
     */
    PageResult<ProductReviewVO> getMyReviews(int pageNum, int pageSize, Long userId);
}
