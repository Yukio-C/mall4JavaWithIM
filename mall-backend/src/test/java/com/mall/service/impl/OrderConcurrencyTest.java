package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.entity.po.Order;
import com.mall.entity.po.OrderItem;
import com.mall.entity.po.Product;
import com.mall.mapper.OrderItemMapper;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.ProductMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OrderConcurrencyTest {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("方案一：Java逻辑校验（不安全）- 演示并发导致库存超额回滚")
    void testUnsafeCancel() throws InterruptedException {
        prepareData(); // 初始库存 8，订单项 2
        System.out.println(">>> [不安全实验] 初始库存: 8, 预期回滚 2");

        runConcurrentTest(() -> {
            // 手动开启事务模拟 Service 层的 @Transactional
            TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                // 1. 先查状态 (此时两个线程都能查到 status=0)
                Order order = orderMapper.selectById(1L);

                // 强制制造并发漏洞：让先到的线程等一等，让后到的线程也进来查到 status=0
                Thread.sleep(200);

                // 2. Java 逻辑判断
                if (order.getStatus() == 0 || order.getStatus() == 1) {
                    System.out.println("线程 " + Thread.currentThread().getName() + " 进入回滚逻辑");
                    
                    // 3. 修改状态
                    order.setStatus(4);
                    orderMapper.updateById(order);

                    // 4. 回滚库存
                    List<OrderItem> items = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, 1L)
                    );
                    for (OrderItem item : items) {
                        productMapper.addStock(item.getProductId(), item.getCount());
                    }
                }
                transactionManager.commit(status);
            } catch (Exception e) {
                transactionManager.rollback(status);
            }
        });

        int finalStock = productMapper.selectById(1L).getStock();
        System.out.println(">>> [不安全实验] 最终库存: " + finalStock);
        // 如果库存是 12 (8 + 2 + 2)，说明发生了并发错误
        assertTrue(finalStock > 10, "并发漏洞未触发，库存应该是 12");
    }

    @Test
    @DisplayName("方案二：SQL原子更新（安全）- 演示并发下正确回滚")
    void testSafeCancel() throws InterruptedException {
        prepareData(); // 初始库存 8，订单项 2
        System.out.println("\n>>> [安全实验] 初始库存: 8, 预期回滚 2");

        runConcurrentTest(() -> {
            TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                // 1. 利用 SQL 原子更新拦截并发
                int affectedRows = orderMapper.updateStatusWithLock(1L, 4, "0, 1");

                // 2. 只有拿到锁并更新成功的人才能回滚库存
                if (affectedRows == 1) {
                    System.out.println("线程 " + Thread.currentThread().getName() + " 抢锁成功，执行回滚");
                    List<OrderItem> items = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, 1L)
                    );
                    for (OrderItem item : items) {
                        productMapper.addStock(item.getProductId(), item.getCount());
                    }
                } else {
                    System.out.println("线程 " + Thread.currentThread().getName() + " 抢锁失败，跳过回滚");
                }
                transactionManager.commit(status);
            } catch (Exception e) {
                transactionManager.rollback(status);
            }
        });

        int finalStock = productMapper.selectById(1L).getStock();
        System.out.println(">>> [安全实验] 最终库存: " + finalStock);
        // 库存必须是 10 (8 + 2)
        assertEquals(10, finalStock, "安全逻辑失效，库存不等于 10");
    }

    private void prepareData() {
        // 初始化商品 (ID=1, 库存=8)
        Product product = productMapper.selectById(1L);
        if (product == null) {
            product = new Product().setId(1L).setTitle("并发测试商品").setStock(8).setPrice(new BigDecimal("100")).setCategoryId(1).setStatus(1);
            productMapper.insert(product);
        } else {
            product.setStock(8);
            productMapper.updateById(product);
        }

        // 初始化订单 (ID=1, 状态=0)
        Order order = orderMapper.selectById(1L);
        if (order == null) {
            order = new Order().setId(1L).setOrderNo("CONC_TEST_001").setUserId(1L).setStatus(0).setTotalAmount(new BigDecimal("200")).setReceiverName("测试").setReceiverPhone("138").setReceiverAddress("测试");
            orderMapper.insert(order);
        } else {
            order.setStatus(0);
            orderMapper.updateById(order);
        }

        // 初始化订单明细 (数量=2)
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, 1L));
        OrderItem item = new OrderItem().setOrderId(1L).setProductId(1L).setCount(2).setProductTitle("测试商品").setProductPrice(new BigDecimal("100")).setProductCover("test.jpg");
        orderItemMapper.insert(item);
    }

    private void runConcurrentTest(Runnable task) throws InterruptedException {
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                try {
                    latch.await();
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
        }
        latch.countDown();
        done.await();
        executor.shutdown();
    }
}
