package com.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "用户注册DTO")
public class UserRegisterDTO {
    // Username 用户名
    @NotNull(message = "用户名不能为空")
    private String username;

    // Password 密码
    @NotNull(message = "密码不能为空")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!*()]).{8,20}$",
            message = "密码必须包含数字、字母和特殊符号(@#$%^&+=!*())，长度8-20位"
    )
    private String password;
}
