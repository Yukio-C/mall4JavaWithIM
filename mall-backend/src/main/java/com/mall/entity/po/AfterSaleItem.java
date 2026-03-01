package com.mall.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.io.Serializable;

/**
 * 售后明细持久化对象 (PO)
 */
@Data
@TableName("after_sale_items")
public class AfterSaleItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联售后主表ID */
    private Long afterSaleId;

    /** 关联订单明细ID */
    private Long orderItemId;

    /** 商品ID */
    private Long productId;

    /** 商品标题快照 */
    private String productTitle;

    /** 商品封面快照 */
    private String productCover;

    /** 单价快照 */
    private BigDecimal productPrice;

    /** 售后数量 */
    private Integer count;
}
