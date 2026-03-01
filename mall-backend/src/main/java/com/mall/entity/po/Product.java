package com.mall.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "products", autoResultMap = true)
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;


    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品分类
     */
    private Integer categoryId;

    /**
     * 商品封面
     */
    private String cover;

    /**
     * 当前价格，单位元
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 商品评价
     */
    private BigDecimal rating;

    /**
     * 商品状态 (0: 下架, 1: 上架)
     */
    private Integer status;

    /**
     * 评价总分
     */
    private Integer totalRating;

    /**
     * 评价总人数
     */
    private Integer ratingCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
