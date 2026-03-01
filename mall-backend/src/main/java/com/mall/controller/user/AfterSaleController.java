package com.mall.controller.user;


import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.dto.AfterSaleDTO;
import com.mall.entity.dto.AfterSalePageDTO;
import com.mall.entity.dto.OrderPageDTO;
import com.mall.entity.vo.AfterSaleVO;
import com.mall.security.LoginUser;
import com.mall.service.IAfterSaleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/user/after-sales")
@RestController
@RequiredArgsConstructor
@Tag(name = "用户售后模块")
@Validated
public class AfterSaleController {
    private final IAfterSaleService afterSaleService;

    @GetMapping("/list")
    public Result<PageResult<AfterSaleVO>> getAfterSaleList(
            @ParameterObject AfterSalePageDTO afterSalePageDTO,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(afterSaleService.getAfterSaleList(afterSalePageDTO,loginUser));
    }

    @PostMapping("/apply")
    public Result<String> Apply4AfterSale(
            @RequestBody AfterSaleDTO afterSaleDTO,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        afterSaleService.apply4AfterSale(afterSaleDTO,loginUser);
        return Result.success("申请售后成功");
    }

}
