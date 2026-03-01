package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.PageResult;
import com.mall.entity.dto.ProductDTO;
import com.mall.entity.dto.ProductPageDTO;
import com.mall.entity.po.Product;
import com.mall.entity.vo.ProductDetailVO;
import com.mall.entity.vo.ProductInfoVO;

import java.util.List;

public interface IProductService extends IService<Product> {
    /**
     * 获取商品列表，支持分页和搜索 (用户端：仅展示上架商品)
     */
    PageResult<ProductInfoVO> getProductList(ProductPageDTO productPageDTO);

    /**
     * 获取商品列表 (管理端：展示所有状态商品)
     */
    PageResult<ProductInfoVO> getAdminProductList(ProductPageDTO productPageDTO);

    /**
     * 获取商品详情
     */
    ProductDetailVO getProductDetail(Long id);

    /**
     * 管理员：更新商品信息 (含新增)
     */
    void updateProduct(ProductDTO productDTO);

    /**
     * 管理员：修改商品状态 (上/下架)
     */
    void updateStatus(Long id, Integer status);

    /**
     * 管理员：物理删除商品
     */
    void deleteProduct(Long id);

    /**
     * 业务核心：扣减库存 (SQL原子更新)
     */
    void reduceStock(Long productId, Integer count);

    /**
     * 业务核心：回滚库存 (SQL原子更新)
     */
    void addStock(Long productId, Integer count);

    /**
     * 业务核心：累加评价分数和人数
     */
    void addRating(Long productId, Integer rating);

    /**
     * 业务核心：增加销量
     */
    void addSales(Long productId, Integer count);

    /**
     * 商家端：获取销售排行榜
     */
    List<ProductInfoVO> getSalesTop(int limit);

    /**
     * 商家端：获取售后排行榜 (售后单数最多)
     */
    List<ProductInfoVO> getAfterSaleTop(int limit);

    /**
     * 商家端：获取好评排行榜
     */
    List<ProductInfoVO> getRatingTop(int limit);
}
