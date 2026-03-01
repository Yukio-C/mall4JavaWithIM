package com.mall.controller.user;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.dto.UserAddressDTO;
import com.mall.entity.po.Page;
import com.mall.entity.vo.UserAddressVO;
import com.mall.service.IUserAddressService;
import com.mall.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/user/address")
@RestController
@RequiredArgsConstructor
@Tag(name = "用户地址管理接口")
@Validated  // 类级别启用校验
public class AddressController {

    private final IUserAddressService userAddressService;

    @Operation(summary = "获取用户地址列表接口")
    @GetMapping("/list")
    public Result<PageResult<UserAddressVO>> getAddressList(
            @AuthenticationPrincipal LoginUser loginUser,
            Page page
    ) {
        return Result.success(userAddressService.getAddressList(loginUser.getUserId(), page.getPageNum(), page.getPageSize()));
    }

    @PostMapping("/add")
    @Operation(summary = "添加用户地址接口")
    public Result<UserAddressVO> addAddress(
            @RequestBody @Valid UserAddressDTO addressDTO,
            @AuthenticationPrincipal LoginUser loginUser
    ) {

        return Result.success(userAddressService.addAddress(addressDTO, loginUser.getUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "修改用户地址接口")
    public Result<UserAddressVO> updateAddress(
            @RequestBody @Valid UserAddressDTO addressDTO,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(userAddressService.updateAddress(addressDTO, loginUser.getUserId()));
    }

    /**
     * TODO: 完善批量删除功能,地址管理列表前端格式有问题
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除用户地址接口")
    public Result<String> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        log.info("删除用户地址，用户ID：{}，地址ID：{}", loginUser.getUserId(), id);
        userAddressService.removeAddressById(id, loginUser.getUserId());
        return Result.success("删除成功");
    }

}