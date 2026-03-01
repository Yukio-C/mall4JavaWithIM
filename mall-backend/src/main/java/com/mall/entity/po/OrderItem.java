package com.mall.entity.po;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "order_items", autoResultMap = true)
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 订单项实体类
     */
    private Long id;
    /**
     * 订单id
     */
    private Long orderId;
    /**
    * 商品id
     */
    private Long productId;

    /**
     * 商品标题
     */
    private String productTitle;

    /**
    * 商品封面
    */
    private String productCover;

    /**
    * 商品价格，单位元
    */
    private BigDecimal productPrice;

    /**
    * 商品数量
    */
    private Integer count;

    /**
     * 是否已评价：0否, 1是
     */
    private Integer isCommented;
}
