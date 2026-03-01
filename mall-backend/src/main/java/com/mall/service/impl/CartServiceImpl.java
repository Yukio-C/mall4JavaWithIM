package com.mall.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.mall.common.PageResult;
import com.mall.entity.dto.CartDTO;
import com.mall.entity.po.Cart;
import com.mall.entity.po.Product;
import com.mall.entity.vo.CartVO;
import com.mall.mapper.CartMapper;
import com.mall.service.ICartService;
import com.mall.service.IProductService;
import com.mall.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private final IUserService userService;
    private final IProductService productService;

    @Override
    public PageResult<CartVO> getMyCart(Long userId) {
        log.info("用户ID {} 的购物车查询", userId);
        // 2. 单表查询，避免联表
        List<Cart> carts = this.lambdaQuery()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreatedAt)
                .list();

        // 3. 转换为 VO
        PageInfo<Cart> pageInfo = new PageInfo<>(carts);
        log.info("用户ID {} 的购物车查询结果，Total: {}, Current List Size: {}", userId, pageInfo.getTotal(), carts.size());
        return PageResult.convert(pageInfo, cart -> {
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setProductId(cart.getProductId());
            vo.setProductTitle(cart.getProductTitle());
            vo.setProductPrice(cart.getProductPrice());
            vo.setProductCover(cart.getProductCover());
            vo.setCount(cart.getCount());
            return vo;
        });
    }

    @Override
    public void addToCart(CartDTO cartDTO, Long userId) {
        // 1. 直接查询购物车中是否已有该商品
        Cart existingCart = this.lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, cartDTO.getProductId())
                .one();
        
        // 2. 查询商品基础信息 (解耦：调用 ProductService)
        Product product = productService.getById(cartDTO.getProductId());
        Assert.notNull(product, "商品已下架，请刷新页面");
        
        if (existingCart != null) {
            // 已存在则增加数量
            existingCart.setCount(existingCart.getCount() + cartDTO.getCount());
            this.updateById(existingCart);
        } else {
            // 不存在则新增记录
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setProductId(cartDTO.getProductId());
            newCart.setProductTitle(product.getTitle());
            newCart.setProductPrice(product.getPrice());
            newCart.setProductCover(product.getCover());
            newCart.setCount(cartDTO.getCount());
            this.save(newCart);
        }

    }

    @Override
    public void updateCount(Long cartId, Integer count) {
        Cart cart = this.getById(cartId);
        if (cart != null) {
            cart.setCount(count);
            this.updateById(cart);
        }
    }

    @Override
    public void deleteCartItem(Long cartId) {
        this.removeById(cartId);
    }
}
