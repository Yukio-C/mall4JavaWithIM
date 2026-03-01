package com.mall.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserInfoVO {
    // UserId 主键
    private Long Id;
    // Username 用户名
    private String username;
    // Nickname 昵称
    private String nickname;
    // Avatar 头像
    private String avatar;
    // Phone 注册手机号
    private String phone;
    // Balance 余额
    private BigDecimal balance;
    // Role 角色
    private String role;
    // CreatedAt 创建时间
    private LocalDateTime createdAt;
    // Permissions 权限列表
    private java.util.List<String> permissions;
}
