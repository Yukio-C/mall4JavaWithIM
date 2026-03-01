package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.PageResult;
import com.mall.entity.dto.CartDTO;
import com.mall.entity.po.Cart;
import com.mall.entity.vo.CartVO;

public interface ICartService extends IService<Cart> {
    PageResult<CartVO> getMyCart(Long userId);

    void addToCart(CartDTO cartDTO, Long userId);

    void updateCount(Long cartId, Integer count);

    void deleteCartItem(Long cartId);
}
