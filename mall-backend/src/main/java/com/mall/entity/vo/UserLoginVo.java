package com.mall.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserLoginVo {
    // Token 校验码
    private String token;
    // UserId 主键
    private Long userId;
    // Username 用户名
    private String username;
    // Balance 余额
    private BigDecimal balance;
    // Role 角色
    private String role;
    // Permissions 权限列表
    private java.util.List<String> permissions;
}
