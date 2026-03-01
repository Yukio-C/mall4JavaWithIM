package com.mall.controller.admin;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.dto.ServicerPageDTO;
import com.mall.entity.dto.UserLoginDTO;
import com.mall.entity.vo.UserLoginVo;
import com.mall.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mall.entity.dto.ServicerDTO;
import com.mall.entity.vo.ProductInfoVO;
import com.mall.entity.vo.UserInfoVO;
import com.mall.service.IProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "商家管理接口")
@Validated
public class AdminController {

    private final IUserService userService;
    private final IProductService productService;

    /**
     * 首页统计：获取销售排行榜
     */
    @GetMapping("/stats/sales")
    @Operation(summary = "首页统计：销售排行榜")
    public Result<List<ProductInfoVO>> getSalesTop() {
        return Result.success(productService.getSalesTop(10));
    }

    /**
     * 首页统计：获取售后排行榜
     */
    @GetMapping("/stats/after-sale")
    @Operation(summary = "首页统计：售后排行榜")
    public Result<List<ProductInfoVO>> getAfterSaleTop() {
        return Result.success(productService.getAfterSaleTop(10));
    }

    /**
     * 首页统计：获取评分排行榜
     */
    @GetMapping("/stats/rating")
    @Operation(summary = "首页统计：评分排行榜")
    public Result<List<ProductInfoVO>> getRatingTop() {
        return Result.success(productService.getRatingTop(10));
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录接口")
    public Result<UserLoginVo> login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        log.info("管理员尝试登录: {}", userLoginDTO.getUsername());
        return Result.success(userService.adminLogin(userLoginDTO));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<String> logout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.logout(username);
        return Result.success("退出成功");
    }

    /**
     * 创建/修改客服账号 (仅限 ADMIN)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/servicer/save")
    @Operation(summary = "创建/修改客服账号", description = "仅限超级管理员操作")
    public Result<String> saveServicer(@RequestBody @Valid ServicerDTO servicerDTO) {
        userService.saveOrUpdateServicer(servicerDTO);
        return Result.success("操作成功");
    }

    /**
     * 获取客服列表 (仅限 ADMIN)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/servicer/list")
    @Operation(summary = "获取客服列表")
    public Result<PageResult<UserInfoVO>> getServicers(
            @ParameterObject ServicerPageDTO pageDTO
    ) {
        return Result.success(userService.getServicerList(pageDTO));
    }
}
