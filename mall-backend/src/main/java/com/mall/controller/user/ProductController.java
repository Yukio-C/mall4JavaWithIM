package com.mall.controller.user;


import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.dto.ProductPageDTO;
import com.mall.entity.vo.ProductDetailVO;
import com.mall.entity.vo.ProductInfoVO;
import com.mall.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/product")
@RestController
@RequiredArgsConstructor
@Tag(name = "用户商品模块", description = "提供商品搜索、列表展示及详情查询功能")
@Validated
public class ProductController {

    private final IProductService productService;

    /**
     * 商品分页列表接口，支持关键字搜索和分类筛选
     * @param productPageDTO
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "商品分页列表", description = "支持关键字搜索和分类筛选，返回简短的商品信息列表")
    public Result<PageResult<ProductInfoVO>> getProductList(
            @ParameterObject ProductPageDTO productPageDTO
    ) {
        return Result.success(productService.getProductList(productPageDTO));
    }

    @GetMapping("/detail")
    @Operation(summary = "商品详情查询", description = "根据商品ID查询完整的商品详情，包括轮播图、图文详情和规格参数")
    public Result<ProductDetailVO> getProductDetail(
            @Parameter(description = "商品ID", required = true, example = "1") 
            @RequestParam("id") Long id
    ) {
        return Result.success(productService.getProductDetail(id));
    }

}
