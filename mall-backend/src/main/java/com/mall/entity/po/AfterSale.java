package com.mall.entity.po;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("after_sales")
public class AfterSale implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 售后订单ID
     */
    private Long orderId;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 售后支付单号
     */
    private String orderNo;

    /**
     * 申请售后简要原因
     */
    private String reason;

    /**
     * 申请售后详细描述
     */
    private String description;

    /**
     * 售后类型：1:仅退款, 2:退货退款, 3:换货
     */
    private Integer type;

    /**
     * 申请售后图片凭证，逗号分隔
     */
    private String images;

    /**
     * 商家处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 商家处理备注/拒绝原因
     */
    private String handleRemark;

    /**
     * 售后状态：0:待审核, 1:处理中, 2:已完成, 3:已拒绝
     */
    private Integer status;

    /**
     * 退款金额，单位元
     */
    private BigDecimal refundAmount;

    /**
     * 申请售后时间
     */
    private LocalDateTime applyTime;

}
