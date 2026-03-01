package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.common.PageResult;
import com.mall.common.exception.MallException;
import com.mall.entity.dto.AfterSaleDTO;
import com.mall.entity.dto.AfterSaleItemDTO;
import com.mall.entity.dto.AfterSalePageDTO;
import com.mall.entity.po.AfterSale;
import com.mall.entity.po.AfterSaleItem;
import com.mall.entity.po.Order;
import com.mall.entity.po.OrderItem;
import com.mall.entity.vo.AfterSaleItemVO;
import com.mall.entity.vo.AfterSaleVO;
import com.mall.mapper.AfterSaleMapper;
import com.mall.security.LoginUser;
import com.mall.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AfterSaleServiceImpl extends ServiceImpl<AfterSaleMapper, AfterSale> implements IAfterSaleService {

    private final IAfterSaleItemService afterSaleItemService;
    private final IOrderService orderService;
    private final IOrderItemService orderItemService;
    private final IProductService productService;

    @Override
    public PageResult<AfterSaleVO> getAfterSaleList(AfterSalePageDTO afterSalePageDTO, LoginUser loginUser) {
        int pageNum = afterSalePageDTO.getPageNum() != null ? afterSalePageDTO.getPageNum() : 1;
        int pageSize = afterSalePageDTO.getPageSize() != null ? afterSalePageDTO.getPageSize() : 10;

        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<AfterSale> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键逻辑：如果是用户端请求，过滤 ID；如果是管理端请求（loginUser 为 null），查全量
        if (loginUser != null) {
            queryWrapper.eq(AfterSale::getUserId, loginUser.getUserId());
        }
        
        queryWrapper.eq(afterSalePageDTO.getStatus() != null, AfterSale::getStatus, afterSalePageDTO.getStatus())
                    .orderByDesc(AfterSale::getApplyTime);

        List<AfterSale> afterSales = baseMapper.selectList(queryWrapper);
        PageInfo<AfterSale> pageInfo = new PageInfo<>(afterSales);


        return PageResult.convert(pageInfo, afterSale -> {
            AfterSaleVO vo = new AfterSaleVO();
            BeanUtils.copyProperties(afterSale, vo);

            if (afterSale.getImages() != null && !afterSale.getImages().isEmpty()) {
                vo.setImages(Arrays.asList(afterSale.getImages().split(",")));
            }

            List<AfterSaleItem> items = afterSaleItemService.getByAfterSaleId(afterSale.getId());
            if (items != null && !items.isEmpty()) {
                AfterSaleItem firstItem = items.get(0);
                vo.setProductTitle(firstItem.getProductTitle());
                vo.setProductCover(firstItem.getProductCover());
                if (items.size() > 1) {
                    vo.setProductTitle(firstItem.getProductTitle() + " 等" + items.size() + "件商品");
                }
                
                List<AfterSaleItemVO> itemVOs = items.stream().map(item -> {
                    AfterSaleItemVO itemVO = new AfterSaleItemVO();
                    BeanUtils.copyProperties(item, itemVO);
                    return itemVO;
                }).collect(Collectors.toList());
                vo.setItems(itemVOs);
            }
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply4AfterSale(AfterSaleDTO afterSaleDTO, LoginUser loginUser) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, afterSaleDTO.getOrderId())
                .eq(Order::getUserId, loginUser.getUserId()));
        if (order == null) throw new MallException("订单不存在或无权操作");

        Long existingCount = baseMapper.selectCount(new LambdaQueryWrapper<AfterSale>()
                .eq(AfterSale::getOrderId, order.getId())
                .in(AfterSale::getStatus, 0, 1));
        if (existingCount > 0) throw new MallException("已有进行中的售后申请");

        if (order.getStatus() < 1 || order.getStatus() > 3) throw new MallException("当前订单状态不可申请售后");

        AfterSale afterSale = new AfterSale();
        BeanUtils.copyProperties(afterSaleDTO, afterSale);
        afterSale.setUserId(loginUser.getUserId());
        afterSale.setOrderNo(order.getOrderNo());
        afterSale.setStatus(0); // 待审核
        afterSale.setApplyTime(LocalDateTime.now());

        if (afterSaleDTO.getImages() != null && !afterSaleDTO.getImages().isEmpty()) {
            afterSale.setImages(String.join(",", afterSaleDTO.getImages()));
        }

        this.save(afterSale);

        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<AfterSaleItem> afterSaleItems = new ArrayList<>();
        for (AfterSaleItemDTO itemDTO : afterSaleDTO.getItems()) {
            OrderItem orderItem = orderItemService.getById(itemDTO.getOrderItemId());
            if (orderItem == null || !orderItem.getOrderId().equals(order.getId())) throw new MallException("订单项异常");

            AfterSaleItem afterSaleItem = new AfterSaleItem();
            afterSaleItem.setAfterSaleId(afterSale.getId());
            afterSaleItem.setOrderItemId(orderItem.getId());
            afterSaleItem.setProductId(orderItem.getProductId());
            afterSaleItem.setProductTitle(orderItem.getProductTitle());
            afterSaleItem.setProductCover(orderItem.getProductCover());
            afterSaleItem.setProductPrice(orderItem.getProductPrice());
            afterSaleItem.setCount(itemDTO.getCount());
            afterSaleItems.add(afterSaleItem);

            totalRefundAmount = totalRefundAmount.add(orderItem.getProductPrice().multiply(new BigDecimal(itemDTO.getCount())));
        }
        
        afterSaleItemService.saveBatch(afterSaleItems);

        if (totalRefundAmount.compareTo(order.getPayAmount()) > 0) totalRefundAmount = order.getPayAmount();
        afterSale.setRefundAmount(totalRefundAmount);
        this.updateById(afterSale);

        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(5); // 售后中
        orderService.updateById(updateOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAfterSale(AfterSaleDTO dto) {
        AfterSale record = this.getById(dto.getId());
        if (record == null) throw new MallException("记录不存在");
        
        // 关键加固：防止重复处理
        if (record.getStatus() != 0) {
            throw new MallException("该申请已处理过，请勿重复操作");
        }

        // 1. 更新售后单状态
        record.setStatus(dto.getStatus()); // 2:已完成, 3:已拒绝
        this.updateById(record);

        // 2. 如果是退货完成，执行资产回流
        if (record.getStatus() == 2) {
            log.info("售后单 {} 处理完成，开始资产回流", record.getId());
            
            // A. 退货退款/换货 -> 回滚库存
            if (record.getType() == 2 || record.getType() == 3) {
                List<AfterSaleItem> items = afterSaleItemService.getByAfterSaleId(record.getId());
                for (AfterSaleItem item : items) {
                    log.info("售后回滚库存: productId={}, count={}", item.getProductId(), item.getCount());
                    productService.addStock(item.getProductId(), item.getCount());
                }
            }

            // B. 标记原订单为已关闭 (或者是根据业务定为已完成)
            Order order = new Order();
            order.setId(record.getOrderId());
            order.setStatus(4); // 售后成功后关闭订单
            orderService.updateById(order);
        }
    }
}
