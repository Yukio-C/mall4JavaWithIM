package com.mall.controller.admin;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.dto.ProductDTO;
import com.mall.entity.dto.ProductPageDTO;
import com.mall.entity.vo.ProductInfoVO;
import com.mall.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mall.entity.vo.ProductDetailVO;

@Slf4j
@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
@Tag(name = "商家商品管理")
@PreAuthorize("@ss.hasPerm('product')")
public class AdminProductController {

    private final IProductService productService;

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取商品详情")
    public Result<ProductDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    /**
     * 商品分页列表 (支持搜索)
     */
    @GetMapping("/list")
    @Operation(summary = "商品分页列表", description = "支持关键字搜索和分类筛选，返回完整的商品信息列表 (含下架商品)")
    public Result<PageResult<ProductInfoVO>> getProductList(
            @ParameterObject ProductPageDTO productPageDTO
    ) {
        return Result.success(productService.getAdminProductList(productPageDTO));
    }

    /**
     * 更新或新增商品
     */
    @PutMapping("/update")
    @Operation(summary = "更新商品信息")
    public Result<String> update(@RequestBody @Valid ProductDTO productDTO) {
        productService.updateProduct(productDTO);
        return Result.success("操作成功");
    }

    /**
     * 修改商品状态 (上/下架)
     */
    @PutMapping("/status/{id}")
    @Operation(summary = "修改商品状态")
    public Result<String> updateStatus(
            @PathVariable @Parameter(description = "商品ID") Long id,
            @RequestParam @Parameter(description = "状态 (0:下架, 1:上架)") Integer status
    ) {
        productService.updateStatus(id, status);
        return Result.success("操作成功");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "物理删除商品")
    public Result<String> delete(@PathVariable @Parameter(description = "商品ID") Long id) {
        productService.deleteProduct(id);
        return Result.success("删除成功");
    }
}
