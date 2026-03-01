package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.common.PageResult;
import com.mall.config.RabbitMQConfig;
import com.mall.entity.dto.OrderDTO;
import com.mall.entity.dto.OrderPageDTO;
import com.mall.entity.dto.OrderShipDTO;
import com.mall.entity.po.Address;
import com.mall.entity.po.Order;
import com.mall.entity.po.OrderItem;
import com.mall.entity.po.Product;
import com.mall.entity.vo.OrderVO;
import com.mall.mapper.OrderMapper;
import com.mall.service.IOrderItemService;
import com.mall.service.IOrderService;
import com.mall.service.IProductService;
import com.mall.service.IUserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final OrderMapper orderMapper;
    private final IOrderItemService orderItemService;
    private final IProductService productService;
    private final IUserAddressService userAddressService;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        cancelOrder(orderId, userId, "系统自动取消（超时未支付）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            log.warn("取消订单失败：订单不存在或无权操作，ID: {}", orderId);
            return;
        }
        doCancel(orderId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderAdmin(Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        Assert.notNull(order, "订单不存在");
        
        log.info("管理员发起取消订单，ID: {}, 原因: {}", orderId, reason);
        doCancel(orderId, "[管理员操作] " + reason);
    }

    private void doCancel(Long orderId, String reason) {
        int affectedRows = orderMapper.updateStatusWithLock(orderId, 4, "0");

        if (affectedRows == 1) {
            Order updateOrder = new Order();
            updateOrder.setId(orderId);
            updateOrder.setCancelReason(reason);
            updateOrder.setCancelTime(LocalDateTime.now());
            orderMapper.updateById(updateOrder);

            List<OrderItem> items = orderItemService.getByOrderId(orderId);
            for (OrderItem item : items) {
                productService.addStock(item.getProductId(), item.getCount());
            }
            log.info("订单 {} 取消成功，最终状态：已关闭，库存已回滚", orderId);
        } else {
            Order current = orderMapper.selectById(orderId);
            log.warn("订单 {} 状态已变更，取消失败。当前状态: {}", orderId, current.getStatus());
            throw new RuntimeException("订单当前状态不支持取消（可能已支付或已关闭）");
        }
    }

    @Override
    public PageResult<OrderVO> getOrderList(OrderPageDTO orderPageDTO, Long userId) {
        int pageNum = orderPageDTO.getPageNum() != null ? orderPageDTO.getPageNum() : 1;
        int pageSize = orderPageDTO.getPageSize() != null ? orderPageDTO.getPageSize() : 10;

        PageHelper.startPage(pageNum, pageSize);
        List<Order> orders = orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(orderPageDTO.getStatus() != null, Order::getStatus, orderPageDTO.getStatus())
                .orderByDesc(Order::getCreatedAt)
        );

        PageInfo<Order> pageInfo = new PageInfo<>(orders);

        return PageResult.convert(pageInfo , order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            vo.setItems(orderItemService.getByOrderId(order.getId()));
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long orderId, Long userId) {
        int affected = orderMapper.updateStatusWithLock(orderId, 3, "2");
        
        if (affected == 0) {
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                throw new RuntimeException("订单不存在或无权操作");
            }
            throw new RuntimeException("操作失败，当前订单状态不可确认收货");
        }
        log.info("用户 {} 确认收货，订单 ID: {}", userId, orderId);
    }

    @Override
    public OrderVO getOrderDetailByNo(String orderNo, Long userId) {
        Order order = this.lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId)
                .one();
        
        if (order == null) return null;

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setItems(orderItemService.getByOrderId(order.getId()));
        return vo;
    }

    @Override
    public PageResult<OrderVO> getAdminOrderList(OrderPageDTO orderPageDTO) {
        int pageNum = orderPageDTO.getPageNum() != null ? orderPageDTO.getPageNum() : 1;
        int pageSize = orderPageDTO.getPageSize() != null ? orderPageDTO.getPageSize() : 10;

        PageHelper.startPage(pageNum, pageSize);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(orderPageDTO.getStatus() != null, Order::getStatus, orderPageDTO.getStatus())
                    .eq(orderPageDTO.getUserId() != null, Order::getUserId, orderPageDTO.getUserId());

        if (orderPageDTO.getKeyword() != null && !orderPageDTO.getKeyword().isBlank()) {
            queryWrapper.likeRight(Order::getOrderNo, orderPageDTO.getKeyword().trim());
        }

        queryWrapper.orderByDesc(Order::getCreatedAt);

        List<Order> orders = orderMapper.selectList(queryWrapper);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);

        return PageResult.convert(pageInfo, order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            vo.setItems(orderItemService.getByOrderId(order.getId()));
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, OrderShipDTO shipDTO) {
        Order order = this.getById(orderId);
        Assert.notNull(order, "订单不存在");
        Assert.isTrue(order.getStatus() == 1, "只有待发货状态的订单可以执行发货操作");

        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setStatus(2);
        updateOrder.setLogisticsNo(shipDTO.getLogisticsNo());
        updateOrder.setLogisticsCompany(shipDTO.getLogisticsCompany());
        updateOrder.setDeliveryTime(LocalDateTime.now());

        this.updateById(updateOrder);
        log.info("订单 {} 已发货，物流单号: {}", orderId, shipDTO.getLogisticsNo());
    }

    @Override
    public String createOrderToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("order:token:" + userId, token, 15, TimeUnit.MINUTES);
        return token;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(OrderDTO orderDTO, Long userId) {
        String key = "order:token:" + userId;
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        
        Long result = redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(key),
            orderDTO.getOrderToken()
        );

        Assert.isTrue(result != null && result > 0, "请勿重复下单或令牌已失效");

        Address address = userAddressService.getById(orderDTO.getAddressId());
        Assert.notNull(address, "收货地址不存在");
        Assert.isTrue(address.getUserId().equals(userId), "非法收货地址");

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItem itemReq : orderDTO.getItems()) {
            Product product = productService.getById(itemReq.getProductId());
            Assert.notNull(product, "商品不存在: " + itemReq.getProductId());
            Assert.isTrue(product.getStatus() == 1, "商品已下架: " + product.getTitle());
            
            productService.reduceStock(product.getId(), itemReq.getCount());

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductTitle(product.getTitle());
            item.setProductCover(product.getCover());
            item.setProductPrice(product.getPrice());
            item.setCount(itemReq.getCount());
            orderItems.add(item);

            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(itemReq.getCount())));
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo(userId));
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(0);
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        order.setCreatedAt(LocalDateTime.now());
        
        orderMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItemService.saveItems(orderItems);

        final String finalOrderNo = order.getOrderNo();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("订单事务已提交，发送延时取消消息: {}", finalOrderNo);
                rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_DELAY_ROUTING_KEY, finalOrderNo);
            }
        });

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markItemCommented(Long orderId, Long orderItemId) {
        OrderItem item = orderItemService.getById(orderItemId);
        if (item != null && item.getOrderId().equals(orderId)) {
            item.setIsCommented(1);
            orderItemService.updateById(item);
        }

        Long unCommentedCount = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .eq(OrderItem::getIsCommented, 0)
                .count();
        
        if (unCommentedCount == 0) {
            Order order = new Order();
            order.setId(orderId);
            order.setIsCommented(1);
            this.updateById(order);
            log.info("订单 {} 评价全部完成，更新主表状态", orderId);
        }
    }

    private String generateOrderNo(Long userId) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(1000);
    }
}
