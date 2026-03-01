package com.mall.service;

import com.mall.entity.dto.OrderShipDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.entity.po.Order;

import com.mall.entity.dto.OrderDTO;
import com.mall.entity.dto.OrderPageDTO;
import com.mall.common.PageResult;
import com.mall.entity.vo.OrderVO;

public interface IOrderService extends IService<Order> {
    /**
     * 获取下单幂等性令牌
     */
    String createOrderToken(Long userId);

    OrderVO createOrder(OrderDTO orderDTO, Long userId);

    /**
     * 取消订单 (自动取消，无原因)
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 取消订单 (用户主动取消，带原因)
     */
    void cancelOrder(Long orderId, Long userId, String reason);

    /**
     * 用户端：分页查询订单列表
     */
    PageResult<OrderVO> getOrderList(OrderPageDTO orderPageDTO, Long userId);

    /**
     * 用户端：确认收货
     */
    void confirmReceipt(Long orderId, Long userId);

    /**
     * 根据订单号查询详情
     */
    OrderVO getOrderDetailByNo(String orderNo, Long userId);

    /**
     * 将订单下的某个商品项标记为已评价，并自动检查整单是否评价完成
     */
    void markItemCommented(Long orderId, Long orderItemId);

    /**
     * 管理端：分页查询全量订单列表
     */
    PageResult<OrderVO> getAdminOrderList(OrderPageDTO orderPageDTO);

    /**
     * 管理端：订单发货
     */
    void shipOrder(Long orderId, OrderShipDTO shipDTO);

    /**
     * 管理端：取消订单
     */
    void cancelOrderAdmin(Long orderId, String reason);
}
