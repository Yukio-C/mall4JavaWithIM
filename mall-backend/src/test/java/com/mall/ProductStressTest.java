package com.mall;

import com.mall.entity.po.Product;
import com.mall.service.IProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class ProductStressTest {

    @Autowired
    private IProductService productService;

    @Test
    public void generateMassiveData() {
        System.out.println("========== 开始生成测试数据 ==========");
        long startTime = System.currentTimeMillis();

        int totalAmount = 100000; // 目标：10万条
        int batchSize = 1000;     // 每批次：1000条
        List<Product> batchList = new ArrayList<>(batchSize);
        Random random = new Random();

        for (int i = 1; i <= totalAmount; i++) {
            Product p = new Product();
            p.setTitle("高并发测试商品_" + i);
            p.setDescription("这是一个用于测试数据库性能的虚拟商品，编号：" + i);
            p.setCategoryId(random.nextInt(10) + 1); // 随机分类 1-10
            p.setCover("https://via.placeholder.com/150"); // 占位图
            p.setPrice(new BigDecimal(random.nextInt(1000) + 10)); // 随机价格
            p.setOriginalPrice(p.getPrice().add(new BigDecimal(100)));
            p.setStock(9999);
            p.setSales(0);
            p.setRating(new BigDecimal("5.0"));
            p.setStatus(1); // 上架

            batchList.add(p);

            // 每凑够 batchSize 条，或者到了最后一条，就写入数据库
            if (batchList.size() >= batchSize || i == totalAmount) {
                productService.saveBatch(batchList);
                batchList.clear();
                System.out.println("已插入数据: " + i + " 条");
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("========== 数据生成完毕 ==========");
        System.out.println("总耗时: " + (endTime - startTime) / 1000.0 + " 秒");
    }
}
