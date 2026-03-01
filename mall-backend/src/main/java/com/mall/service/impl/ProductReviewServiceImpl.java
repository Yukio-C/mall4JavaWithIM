package com.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.entity.dto.ItemReviewDTO;
import com.mall.entity.dto.ProductReviewDTO;
import com.mall.entity.po.Product;
import com.mall.entity.po.ProductReview;
import com.mall.entity.vo.ProductReviewVO;
import com.mall.common.PageResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.mapper.ProductReviewMapper;
import com.mall.service.IOrderService;
import com.mall.service.IProductReviewService;
import com.mall.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements IProductReviewService {

    private final IOrderService orderService;
    private final IProductService productService;

    @Override
    public PageResult<ProductReviewVO> getMyReviews(int pageNum, int pageSize, Long userId) {
        PageHelper.startPage(pageNum, pageSize);
        List<ProductReview> list = lambdaQuery()
                .eq(ProductReview::getUserId, userId)
                .orderByDesc(ProductReview::getCreatedAt)
                .list();
        
        PageInfo<ProductReview> pageInfo = new PageInfo<>(list);

        return PageResult.convert(pageInfo, po -> {
            ProductReviewVO vo = new ProductReviewVO();
            BeanUtils.copyProperties(po, vo);
            
            Product product = productService.getById(po.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
                vo.setProductCover(product.getCover());
            }

            if (po.getImages() != null && !po.getImages().isEmpty()) {
                vo.setImages(Arrays.asList(po.getImages().split(",")));
            }
            
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(ProductReviewDTO reviewDTO, Long userId) {
        List<ProductReview> reviewsToSave = new ArrayList<>();
        
        for (ItemReviewDTO itemDTO : reviewDTO.getReviews()) {
            // 构造评价记录
            ProductReview review = new ProductReview();
            review.setOrderId(reviewDTO.getOrderId());
            review.setOrderItemId(itemDTO.getOrderItemId());
            review.setProductId(itemDTO.getProductId());
            review.setUserId(userId);
            review.setRating(itemDTO.getRating());
            review.setContent(itemDTO.getContent());
            review.setIsAnonymous(itemDTO.getIsAnonymous() ? 1 : 0);
            review.setCreatedAt(LocalDateTime.now());
            if (itemDTO.getImages() != null && !itemDTO.getImages().isEmpty()) {
                review.setImages(String.join(",", itemDTO.getImages()));
            }
            reviewsToSave.add(review);

            // 核心解耦点：调用 OrderService 维护状态
            orderService.markItemCommented(reviewDTO.getOrderId(), itemDTO.getOrderItemId());

            // 核心功能：同步累加商品主表的评分和人数
            productService.addRating(itemDTO.getProductId(), itemDTO.getRating());
        }

        if (!reviewsToSave.isEmpty()) {
            this.saveBatch(reviewsToSave);
            log.info("用户 {} 评价成功，已通过 OrderService 联动更新状态", userId);
        }
    }
}
