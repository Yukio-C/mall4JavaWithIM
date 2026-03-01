package com.mall.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水记录
 */
@Data
@Accessors(chain = true)
@TableName("payment_records")
public class PaymentRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联系统订单号
     */
    private String orderNo;

    /**
     * 支付平台流水号 (目前内部支付可存 UUID)
     */
    private String tradeNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式 (1:支付宝 2:微信 3:余额)
     */
    private Integer payType;

    /**
     * 支付状态 (0:进行中 1:支付成功 2:支付失败)
     */
    private Integer status;

    /**
     * 第三方支付平台返回的原始信息 (JSON)
     */
    private String rawResponse;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
