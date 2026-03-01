package com.mall.controller.user;

import com.mall.common.Result;
import com.mall.entity.dto.ProductReviewDTO;
import com.mall.security.LoginUser;
import com.mall.service.IProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mall.common.PageResult;
import com.mall.entity.vo.ProductReviewVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
@Tag(name = "商品评价接口")
@Validated
public class ProductReviewController {

    private final IProductReviewService reviewService;

    @PostMapping("/submit")
    @Operation(summary = "提交评价", description = "仅限已完成且未评价的订单")
    public Result<String> submit(@RequestBody @Validated ProductReviewDTO reviewDTO, 
                                @AuthenticationPrincipal LoginUser loginUser) {
        reviewService.submitReview(reviewDTO, loginUser.getUserId());
        return Result.success("评价成功");
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的评价列表")
    public Result<PageResult<ProductReviewVO>> getMyReviews(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(reviewService.getMyReviews(pageNum, pageSize, loginUser.getUserId()));
    }
}
