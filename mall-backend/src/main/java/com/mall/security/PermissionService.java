package com.mall.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 动态权限校验服务 (在 @PreAuthorize 中通过 @ss.hasPerm('xxx') 调用)
 */
@Service("ss")
@Slf4j
@RequiredArgsConstructor
public class PermissionService {

    private final ObjectMapper objectMapper;

    /**
     * 判断当前登录用户是否有权访问指定模块
     * @param permission 模块标识 (如 product, order, category)
     */
    public boolean hasPerm(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof LoginUser)) {
            return false;
        }

        LoginUser loginUser = (LoginUser) principal;
        String role = loginUser.getUser().getRole();

        // 1. 如果是超级管理员，拥有上帝视角，直接放行
        if ("ADMIN".equalsIgnoreCase(role)) {
            return true;
        }

        // 2. 如果是普通用户，禁止访问管理端模块
        if ("USER".equalsIgnoreCase(role)) {
            return false;
        }

        // 3. 如果是客服 (SERVICER)，校验具体的权限 JSON
        if ("SERVICER".equalsIgnoreCase(role)) {
            String permsJson = loginUser.getUser().getPermissions();
            if (permsJson == null || permsJson.isBlank()) {
                return false;
            }

            try {
                // 解析 JSON 列表并判断是否包含该标识符
                List<String> permsList = objectMapper.readValue(permsJson, List.class);
                return permsList != null && permsList.contains(permission);
            } catch (Exception e) {
                log.error("权限解析异常，用户: {}, 权限数据: {}", loginUser.getUsername(), permsJson);
                return false;
            }
        }

        return false;
    }
}
