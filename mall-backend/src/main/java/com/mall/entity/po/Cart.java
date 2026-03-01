package com.mall.entity.po;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "carts", autoResultMap = true)
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品标题冗余
     */
    private String productTitle;

    /**
     * 商品封面图冗余
     */
    private String productCover;

    /**
     * 商品价格冗余
     */
    private java.math.BigDecimal productPrice;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 更新时间
     */
    private LocalDateTime createdAt;

}
