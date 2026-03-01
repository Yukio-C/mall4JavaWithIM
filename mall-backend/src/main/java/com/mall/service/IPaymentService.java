package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.entity.dto.PaymentDTO;
import com.mall.entity.po.PaymentRecord;
import com.mall.entity.vo.PaymentVO;

/**
 * 支付模块接口
 */
public interface IPaymentService extends IService<PaymentRecord> {

    /**
     * 发起支付
     * @param paymentDTO 支付请求信息
     * @param userId 用户ID
     * @return 支付成功的 VO 结果
     */
    PaymentVO processPayment(PaymentDTO paymentDTO, Long userId);

    /**
     * 查询支付单
     * @param orderNo 订单号
     * @return 支付成功的 VO 结果
     */
    PaymentVO getPaymentStatus(String orderNo);
}
