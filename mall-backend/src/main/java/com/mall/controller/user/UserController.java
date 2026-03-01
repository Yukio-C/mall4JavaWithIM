package com.mall.controller.user;

import com.mall.common.Result;
import com.mall.entity.dto.UserLoginDTO;
import com.mall.entity.dto.UserRegisterDTO;
import com.mall.entity.dto.UserUpdateDTO;
import com.mall.entity.vo.UserInfoVO;
import com.mall.entity.vo.UserRegisterVO;
import com.mall.service.IUserService;
import com.mall.security.LoginUser;
import com.mall.entity.vo.UserLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
@Tag(name = "用户管理接口")
@Validated  // 类级别启用校验
public class UserController {

    private final IUserService userService;

    @Operation(summary = "登录接口")
    @PostMapping("/login")
    public Result<UserLoginVo> login(
            @RequestBody @Valid UserLoginDTO userLoginDTO
    ) {
        log.info("用户登录，登录信息：{}", userLoginDTO);

        return Result.success(userService.login(userLoginDTO));
    }

    @Operation(summary = "注册接口")
    @PostMapping("/register")
    public Result<UserRegisterVO> register(
            @RequestBody @Valid UserRegisterDTO userRegisterDTO
    ) {
        log.info("用户注册，注册信息：{}", userRegisterDTO);

        return Result.success(userService.register(userRegisterDTO));
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息接口")
    public Result<UserInfoVO> getUserInfo(
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(userService.getUserInfo(loginUser.getUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "修改用户信息接口")
    public Result updateUserInfo(
            @RequestBody UserUpdateDTO userUpdateDTO,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        userService.updateUserInfo(loginUser.getUserId(), userUpdateDTO);
        return Result.success("修改成功");
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录接口")
    public Result<String> logout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.logout(username);
        return Result.success("退出成功");
    }

}
