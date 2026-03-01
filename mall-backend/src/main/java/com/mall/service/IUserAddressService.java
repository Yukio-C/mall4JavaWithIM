package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.PageResult;
import com.mall.entity.dto.UserAddressDTO;
import com.mall.entity.po.Address;
import com.mall.entity.vo.UserAddressVO;
import jakarta.validation.Valid;

import java.util.List;

public interface IUserAddressService extends IService<Address> {
    /**
     * 获取用户地址列表
     */
    PageResult<UserAddressVO> getAddressList(Long userId, Integer pageNum, Integer pageSize);
    /**
     * 添加用户地址
     */
    UserAddressVO addAddress(@Valid UserAddressDTO addressDTO, Long userId);
    /**
     * 修改用户地址
     */
    UserAddressVO updateAddress(@Valid UserAddressDTO addressDTO, Long userId);

    /**
     * 删除用户地址
     * @param id
     */
    void removeAddressById(Long id, Long userId);
}
