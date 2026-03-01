package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.enums.OrderStatus;
import com.mall.common.enums.ResultCode;
import com.mall.entity.dto.PaymentDTO;
import com.mall.entity.po.Order;
import com.mall.entity.po.OrderItem;
import com.mall.entity.po.PaymentRecord;
import com.mall.entity.vo.PaymentVO;
import com.mall.mapper.PaymentRecordMapper;
import com.mall.service.IOrderService;
import com.mall.service.IPaymentService;
import com.mall.service.IProductService;
import com.mall.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 支付模块实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements IPaymentService {

    private final IOrderService orderService;
    private final IUserService userService;
    private final IProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO processPayment(PaymentDTO paymentDTO, Long userId) {
        log.info("用户ID {} 发起支付: {}", userId, paymentDTO);
        String orderNo = paymentDTO.getOrderNo();

        // 1. 悲观锁锁定订单
        Order order = orderService.getOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId)
                        .last("FOR UPDATE")
        );
        Assert.notNull(order, ResultCode.ORDER_NOT_FOUND.getMessage());

        // 2. 状态校验
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            throw new RuntimeException("订单状态已变更，请刷新后重试");
        }

        // 3. 处理余额支付
        if (paymentDTO.getPayType() == 3) {
            return executeBalancePay(order, userId, orderNo);
        }

        throw new UnsupportedOperationException("暂不支持此支付方式");
    }

    private PaymentVO executeBalancePay(Order order, Long userId, String orderNo) {
        log.info("开始执行余额支付，订单：{}, 金额：{}", orderNo, order.getPayAmount());

        // 创建流水单
        PaymentRecord record = new PaymentRecord()
                .setOrderNo(orderNo)
                .setUserId(userId)
                .setTradeNo(UUID.randomUUID().toString().replace("-", ""))
                .setAmount(order.getPayAmount())
                .setPayType(3)
                .setStatus(0)
                .setCreatedAt(LocalDateTime.now());
        
        if (!this.save(record)) {
            throw new RuntimeException("支付流水保存失败");
        }

        // 1. 扣款逻辑 (解耦：调用 UserService)
        userService.decreaseBalance(userId, order.getPayAmount());

        // 2. 更新流水状态
        record.setStatus(1).setUpdatedAt(LocalDateTime.now());
        this.updateById(record);

        // 3. 使用状态机校验并更新订单状态
        OrderStatus currentStatus = OrderStatus.getByCode(order.getStatus());
        OrderStatus nextStatus = OrderStatus.PENDING_SHIPMENT;
        
        if (currentStatus == null || !currentStatus.canTransitionTo(nextStatus)) {
            throw new RuntimeException("订单状态非法流转");
        }

        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(nextStatus.getCode());
        updateOrder.setPayTime(LocalDateTime.now());
        updateOrder.setPayType(3);
        orderService.updateById(updateOrder);

        // 4. 核心加固：支付成功后，增加商品销量
        List<OrderItem> items = orderService.getOrderDetailByNo(orderNo, userId).getItems();
        if (items != null) {
            for (OrderItem item : items) {
                log.info("同步更新商品销量：productId={}, count={}", item.getProductId(), item.getCount());
                productService.addSales(item.getProductId(), item.getCount());
            }
        }

        log.info("支付流程成功完成，已保存支付流水：{}", record.getTradeNo());

        // 封装 VO
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(record, vo);
        vo.setPayTypeName("余额支付");
        vo.setPayTime(record.getUpdatedAt());
        return vo;
    }

    @Override
    public PaymentVO getPaymentStatus(String orderNo) {
        PaymentRecord record = this.lambdaQuery().eq(PaymentRecord::getOrderNo, orderNo).one();
        if (record == null) return null;

        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(record, vo);
        vo.setPayTypeName(record.getPayType() == 3 ? "余额支付" : "其他");
        vo.setPayTime(record.getUpdatedAt());
        return vo;
    }
}
