package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.PageResult;
import com.mall.entity.dto.ServicerDTO;
import com.mall.entity.dto.ServicerPageDTO;
import com.mall.entity.dto.UserLoginDTO;
import com.mall.entity.dto.UserRegisterDTO;
import com.mall.entity.dto.UserUpdateDTO;
import com.mall.entity.po.User;
import com.mall.entity.vo.UserInfoVO;
import com.mall.entity.vo.UserLoginVo;
import com.mall.entity.vo.UserRegisterVO;

import java.math.BigDecimal;
import java.util.List;

public interface IUserService extends IService<User> {
    /**
     * 用户登录 (mall-user 入口)
     */
    UserLoginVo login(UserLoginDTO userLoginDTO);

    /**
     * 用户注册
     */
    UserRegisterVO register(UserRegisterDTO userRegisterDTO);

    /**
     * 管理员登录 (mall-admin 入口)
     */
    UserLoginVo adminLogin(UserLoginDTO userLoginDTO);

    /**
     * 获取用户信息
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 修改用户信息
     */
    void updateUserInfo(Long userId, UserUpdateDTO userUpdateDTO);

    /**
     * 退出登录
     */
    void logout(String username);

    /**
     * 扣减余额 (原子更新)
     */
    void decreaseBalance(Long userId, BigDecimal amount);

    /**
     * 管理员：创建/更新客服账号
     */
    void saveOrUpdateServicer(ServicerDTO servicerDTO);

    /**
     * 管理员：获取所有客服账号 (分页)
     */
    PageResult<UserInfoVO> getServicerList(ServicerPageDTO pageDTO);
}
