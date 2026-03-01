package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.common.PageResult;
import com.mall.entity.dto.ProductDTO;
import com.mall.entity.dto.ProductPageDTO;
import com.mall.entity.po.Product;
import com.mall.entity.po.ProductDetail;
import com.mall.entity.vo.ProductDetailVO;
import com.mall.entity.vo.ProductInfoVO;
import com.mall.mapper.ProductDetailMapper;
import com.mall.mapper.ProductMapper;
import com.mall.service.ICategoryService;
import com.mall.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    private final ProductMapper productMapper;
    private final ProductDetailMapper productDetailMapper;
    private final ICategoryService categoryService;

    private static final Safelist DETAIL_HTML_WHITELIST = Safelist.relaxed()
            .addAttributes(":all", "style", "class")
            .addTags("span", "hr", "section");

    /**
     * 辅助方法：根据总分和人数推导平均分
     */
    private BigDecimal calculateAvgRating(Integer total, Integer count) {
        if (count == null || count == 0) return new BigDecimal("5.0");
        return BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
    }

    @Override
    public PageResult<ProductInfoVO> getProductList(ProductPageDTO productPageDTO) {
        PageHelper.startPage(productPageDTO.getPageNum(), productPageDTO.getPageSize());

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, 1); // 仅展示上架

        if (productPageDTO.getKeyword() != null && !productPageDTO.getKeyword().isBlank()) {
            queryWrapper.like(Product::getTitle, productPageDTO.getKeyword().trim());
        }
        if (productPageDTO.getCategoryId() != null) {
            queryWrapper.eq(Product::getCategoryId, productPageDTO.getCategoryId());
        }

        // 动态排序处理
        String field = productPageDTO.getSortField();
        boolean isDesc = "desc".equalsIgnoreCase(productPageDTO.getSortOrder());
        
        if ("sales".equals(field)) {
            queryWrapper.orderBy(true, isDesc, Product::getSales);
        } else if ("createdAt".equals(field)) {
            queryWrapper.orderBy(true, isDesc, Product::getCreatedAt);
        } else if ("rating".equals(field)) {
            // 综合评价排序：按总分排序
            queryWrapper.orderBy(true, isDesc, Product::getTotalRating);
        } else {
            // 默认排序
            queryWrapper.orderByDesc(Product::getSales);
        }

        List<Product> products = this.list(queryWrapper);
        PageInfo<Product> pageInfo = new PageInfo<>(products);

        return PageResult.convert(pageInfo, product -> {
            ProductInfoVO vo = new ProductInfoVO();
            BeanUtils.copyProperties(product, vo);
            vo.setCategoryId(product.getCategoryId());
            vo.setCategoryName(categoryService.getNameById(product.getCategoryId()));
            vo.setRating(calculateAvgRating(product.getTotalRating(), product.getRatingCount()));
            return vo;
        });
    }

    @Override
    public PageResult<ProductInfoVO> getAdminProductList(ProductPageDTO productPageDTO) {
        PageHelper.startPage(productPageDTO.getPageNum(), productPageDTO.getPageSize());

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        if (productPageDTO.getKeyword() != null && !productPageDTO.getKeyword().isBlank()) {
            queryWrapper.like(Product::getTitle, productPageDTO.getKeyword().trim());
        }
        if (productPageDTO.getCategoryId() != null) {
            queryWrapper.eq(Product::getCategoryId, productPageDTO.getCategoryId());
        }
        queryWrapper.orderByDesc(Product::getCreatedAt);

        List<Product> products = this.list(queryWrapper);
        PageInfo<Product> pageInfo = new PageInfo<>(products);

        return PageResult.convert(pageInfo, product -> {
            ProductInfoVO vo = new ProductInfoVO();
            BeanUtils.copyProperties(product, vo);
            vo.setCategoryId(product.getCategoryId());
            vo.setCategoryName(categoryService.getNameById(product.getCategoryId()));
            vo.setRating(calculateAvgRating(product.getTotalRating(), product.getRatingCount()));
            return vo;
        });
    }

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        Product product = this.getById(id);
        Assert.notNull(product, "商品不存在");

        ProductDetail detail = productDetailMapper.selectById(id);
        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(product, vo);
        vo.setCategoryName(categoryService.getNameById(product.getCategoryId()));
        vo.setRating(calculateAvgRating(product.getTotalRating(), product.getRatingCount()));

        if (detail != null) {
            BeanUtils.copyProperties(detail, vo);
        } else {
            vo.setSliderImgs(Collections.singletonList(product.getCover()));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductDTO productDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        this.saveOrUpdate(product);

        ProductDetail detail = new ProductDetail();
        BeanUtils.copyProperties(productDTO, detail);
        detail.setProductId(product.getId());
        
        boolean exists = productDetailMapper.selectById(product.getId()) != null;
        if (exists) {
            productDetailMapper.updateById(detail);
        } else {
            productDetailMapper.insert(detail);
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product p = new Product();
        p.setId(id);
        p.setStatus(status);
        this.updateById(p);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        productDetailMapper.deleteById(id);
        this.removeById(id);
    }

    @Override
    public void reduceStock(Long productId, Integer count) {
        log.info("执行扣减库存，商品ID: {}, 数量: {}", productId, count);
        int affected = productMapper.reduceStock(productId, count);
        Assert.isTrue(affected == 1, "库存扣减失败，库存不足或商品不存在");
    }

    @Override
    public void addStock(Long productId, Integer count) {
        log.info("执行回滚库存，商品ID: {}, 数量: {}", productId, count);
        int affected = productMapper.addStock(productId, count);
        Assert.isTrue(affected == 1, "库存回滚失败，商品不存在");
    }

    @Override
    public void addRating(Long productId, Integer rating) {
        log.info("商品 {} 收到新评价，分数: {}", productId, rating);
        productMapper.addRating(productId, rating);
    }

    @Override
    public void addSales(Long productId, Integer count) {
        log.info("商品 {} 销量增加: {}", productId, count);
        productMapper.addSales(productId, count);
    }

    @Override
    public List<ProductInfoVO> getSalesTop(int limit) {
        List<Product> products = productMapper.selectSalesTop(limit);
        return convertToVOList(products);
    }

    @Override
    public List<ProductInfoVO> getAfterSaleTop(int limit) {
        List<Product> products = productMapper.selectAfterSaleTop(limit);
        return convertToVOList(products);
    }

    @Override
    public List<ProductInfoVO> getRatingTop(int limit) {
        List<Product> products = productMapper.selectRatingTop(limit);
        return convertToVOList(products);
    }

    private List<ProductInfoVO> convertToVOList(List<Product> products) {
        return products.stream().map(product -> {
            ProductInfoVO vo = new ProductInfoVO();
            BeanUtils.copyProperties(product, vo);
            vo.setRating(calculateAvgRating(product.getTotalRating(), product.getRatingCount()));
            return vo;
        }).collect(Collectors.toList());
    }
}
