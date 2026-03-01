package com.mall.controller.user;


import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.dto.CartDTO;
import com.mall.entity.vo.CartVO;
import com.mall.service.ICartService;
import com.mall.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/cart")
@RestController
@RequiredArgsConstructor
@Tag(name = "用户购物车模块", description = "提供购物车列表展示及加入功能")
@Validated
public class CartController {
    /**
     * TOOD:后续改造成redis缓存
     */
    private final ICartService cartService;

    @GetMapping("/list")
    @Operation(summary = "购物车列表", description = "返回处理过的购物车列表")
    public Result<PageResult<CartVO>> getCartList(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cartService.getMyCart(loginUser.getUserId()));
    }

    @PostMapping("/add")
    @Operation(summary = "加入购物车", description = "将指定商品加入购物车，若已存在则增加数量")
    public Result<String> addToCart(
            @RequestBody CartDTO cartDTO,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        cartService.addToCart(cartDTO, loginUser.getUserId());
        return Result.success("商品加入购物车");
    }

    @PostMapping("/update")
    @Operation(summary = "更新购物车数量", description = "更新指定购物车条目的数量")
    public Result<String> updateCount(@RequestBody CartDTO cartDTO) {
        cartService.updateCount(cartDTO.getId(), cartDTO.getCount());
        return Result.success("数量已更新");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除购物车商品", description = "从购物车中移除指定商品")
    public Result<String> deleteCart(@PathVariable Long id) {
        cartService.deleteCartItem(id);
        return Result.success("商品已移除");
    }

}
