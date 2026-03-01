package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.common.PageResult;
import com.mall.entity.dto.*;
import com.mall.entity.po.User;
import com.mall.entity.vo.UserInfoVO;
import com.mall.entity.vo.UserRegisterVO;
import com.mall.mapper.UserMapper;
import com.mall.service.IUserService;
import com.mall.entity.vo.UserLoginVo;
import com.mall.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 用户登录 (mall-user 入口)
     */
    @Override
    public UserLoginVo login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        User user = lambdaQuery().eq(User::getUsername, username).one();
        Assert.notNull(user, "用户名错误");

        // 核心约束：mall-user 只能由 ROLE_USER 登录
        if (!"USER".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("该账号禁止登录用户端");
        }

        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        return createLoginVo(user);
    }

    /**
     * 管理员登录 (mall-admin 入口)
     */
    @Override
    public UserLoginVo adminLogin(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        User user = lambdaQuery().eq(User::getUsername, username).one();
        Assert.notNull(user, "用户名错误");

        // 核心约束：mall-admin 只能由 ADMIN 或 SERVICER 登录
        if (!"ADMIN".equalsIgnoreCase(user.getRole()) && !"SERVICER".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("权限不足，禁止访问管理端");
        }

        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        return createLoginVo(user);
    }

    private UserLoginVo createLoginVo(User user) {
        String redisKey = "login:status:" + user.getUsername();
        redisTemplate.opsForValue().set(redisKey, "1", 24, TimeUnit.HOURS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole()); // 额外存入角色
        String token = jwtUtils.generateToken(claims, user.getUsername());

        UserLoginVo vo = new UserLoginVo();
        vo.setUserId(user.getId());
        vo.setToken(token);
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        
        // 如果是客服，解析权限列表
        if (user.getPermissions() != null) {
            try {
                vo.setPermissions(objectMapper.readValue(user.getPermissions(), List.class));
            } catch (Exception e) {
                log.error("权限解析失败: {}", user.getPermissions());
            }
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateServicer(ServicerDTO dto) {
        User user;
        if (dto.getId() != null) {
            user = this.getById(dto.getId());
            Assert.notNull(user, "账号不存在");
        } else {
            // 新增检查唯一性
            Long count = lambdaQuery().eq(User::getUsername, dto.getUsername()).count();
            Assert.isTrue(count == 0, "用户名已存在");
            user = new User();
            user.setRole("SERVICER");
            user.setBalance(BigDecimal.ZERO);
            user.setCreatedAt(LocalDateTime.now());
            
            if (dto.getPassword() == null || dto.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode("123456")); // 默认密码
            } else {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        }

        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        
        // 处理权限 JSON 存储
        try {
            if (dto.getPermissions() != null) {
                user.setPermissions(objectMapper.writeValueAsString(dto.getPermissions()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("权限数据处理失败");
        }

        this.saveOrUpdate(user);
    }

    @Override
    public PageResult<UserInfoVO> getServicerList(ServicerPageDTO pageDTO) {
        PageHelper.startPage(pageDTO.getPageNum(), pageDTO.getPageSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "SERVICER");

        if (pageDTO.getUsername() != null && !pageDTO.getUsername().isBlank()) {
            wrapper.like(User::getUsername, pageDTO.getUsername().trim());
        }

        wrapper.orderByDesc(User::getCreatedAt);

        List<User> list = this.list(wrapper);
        PageInfo<User> pageInfo = new PageInfo<>(list);

        return PageResult.convert(pageInfo, user -> {
            UserInfoVO vo = new UserInfoVO();
            BeanUtils.copyProperties(user, vo);
            // 关键：将数据库的 JSON 权限解析回 List 供前端回显
            if (user.getPermissions() != null) {
                try {
                    vo.setPermissions(objectMapper.readValue(user.getPermissions(), List.class));
                } catch (Exception e) {
                    log.error("解析客服 {} 权限失败: {}", user.getUsername(), user.getPermissions());
                }
            }
            return vo;
        });
    }

    /**
     * 用户注册
     */
    @Override
    public UserRegisterVO register(UserRegisterDTO userRegisterDTO) {
        //1检查用户唯一性
        String username = userRegisterDTO.getUsername();
        String password = userRegisterDTO.getPassword();
        Boolean existingUser = lambdaQuery().eq(User::getUsername, username).exists();
        Assert.isTrue(!existingUser, "用户名已存在");

        //2密码加密
        String encodedPassword = passwordEncoder.encode(password);
        //3保存用户,写入数据库
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(encodedPassword);
        this.save(newUser);
        //4返回结果
        UserRegisterVO vo = new UserRegisterVO();
        vo.setUserId(newUser.getId());
        vo.setUsername(newUser.getUsername());
        return vo;
    }

    /**
     * 获取用户信息
     */
    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        Assert.notNull(user, "用户不存在/用户数据异常，请重新登录");
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        vo.setPhone(desensitizePhone(user.getPhone()));
        log.info( "获取用户信息，用户信息：{}", vo);
        return vo;
    }

    /**
     * 修改用户信息
     */
    @Override
    public void updateUserInfo(Long userId, UserUpdateDTO userUpdateDTO) {
        User user = this.getById(userId);
        Assert.notNull(user, "用户数据异常，请重新登录");
        // 更新用户信息
        user.setNickname(userUpdateDTO.getNickname());
        user.setAvatar(userUpdateDTO.getAvatar());
        
        this.updateById(user);
    }

    /**
     * 脱敏手机号
     */
    private String desensitizePhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 退出登录
     */
    @Override
    public void logout(String username) {
        String redisKey = "login:status:" + username;
        redisTemplate.delete(redisKey);
        log.info("用户 {} 已退出登录，清除 Redis 状态", username);
    }

    @Override
    public void decreaseBalance(Long userId, BigDecimal amount) {
        log.info("执行扣减余额，用户ID: {}, 数量: {}", userId, amount);
        int affected = baseMapper.decreaseBalance(userId, amount);
        Assert.isTrue(affected == 1, "余额不足或用户不存在");
    }
}
