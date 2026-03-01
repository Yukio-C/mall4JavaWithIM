package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.common.PageResult;
import com.mall.entity.dto.UserAddressDTO;
import com.mall.entity.po.Address;
import com.mall.entity.vo.UserAddressVO;
import com.mall.mapper.UserAddressMapper;
import com.mall.service.IUserAddressService;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, Address> implements IUserAddressService {


    @Override
    public PageResult<UserAddressVO> getAddressList(Long userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Address> addressList = this.list(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        
        // 使用 PageResult.convert 一步完成 PO -> VO 的转换和分页封装
        return PageResult.convert(new PageInfo<>(addressList), address -> {
            UserAddressVO addressVO = new UserAddressVO();
            BeanUtils.copyProperties(address, addressVO);
            return addressVO;
        });
    }

    @Override
    public UserAddressVO addAddress(UserAddressDTO addressDTO, Long userId) {
        log.info("添加用户地址，用户ID：{}，地址信息：{}", userId, addressDTO);
        Address address = new Address();
        BeanUtils.copyProperties(addressDTO, address);
        address.setUserId(userId);
        this.save(address);
        UserAddressVO addressVO = new UserAddressVO();
        BeanUtils.copyProperties(address, addressVO);
        log.info("添加用户地址成功，用户ID：{}，地址信息：{}", userId, addressVO);
        return addressVO;

    }

    @Override
    public UserAddressVO updateAddress(UserAddressDTO addressDTO, Long userId) {
        /**
         * TODO: 此处还需 handle 默认地址的逻辑
         */
        log.info("修改用户地址，用户ID：{}，地址信息：{}", userId, addressDTO);
        Address address = this.getById(addressDTO.getId());
        Assert.notNull(address, "地址不存在");
        Assert.isTrue(address.getUserId().equals(userId), "无权限修改该地址");
        BeanUtils.copyProperties(addressDTO, address);
        this.updateById(address);
        UserAddressVO addressVO = new UserAddressVO();
        BeanUtils.copyProperties(address, addressVO);
        log.info("修改用户地址成功，用户ID：{}，地址信息：{}", userId, addressVO);
        return addressVO;

    }

    @Override
    public void removeAddressById(Long id, Long userId) {
        Address address = this.getById(id);
        Assert.notNull(address, "地址不存在");
        Assert.isTrue(address.getUserId().equals(userId), "无权限删除该地址");
        this.removeById(id);
        log.info("删除用户地址成功，用户ID：{}，地址ID：{}", userId, id);

    }
}
