package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.po.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 原子扣减库存
     * @param id 商品ID
     * @param count 扣减数量
     * @return 成功扣减的行数 (1: 成功, 0: 失败/库存不足)
     */
    @Update("UPDATE products SET stock = stock - #{count} WHERE id = #{id} AND stock >= #{count}")
    int reduceStock(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 原子回滚库存 (加回库存)
     * @param id 商品ID
     * @param count 增加数量
     */
    @Update("UPDATE products SET stock = stock + #{count} WHERE id = #{id}")
    int addStock(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 原子累加评价分
     */
    @Update("UPDATE products SET total_rating = total_rating + #{rating}, " +
            "rating_count = rating_count + 1 WHERE id = #{id}")
    int addRating(@Param("id") Long id, @Param("rating") Integer rating);

    /**
     * 原子增加销量
     */
    @Update("UPDATE products SET sales = sales + #{count} WHERE id = #{id}")
    int addSales(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 查询销量最高的商品
     */
    @org.apache.ibatis.annotations.Select("SELECT * FROM products ORDER BY sales DESC LIMIT #{limit}")
    java.util.List<Product> selectSalesTop(@Param("limit") int limit);

    /**
     * 查询评分最高的商品
     */
    @org.apache.ibatis.annotations.Select("SELECT * FROM products ORDER BY total_rating DESC LIMIT #{limit}")
    java.util.List<Product> selectRatingTop(@Param("limit") int limit);

    /**
     * 查询售后最频繁的商品
     */
    @org.apache.ibatis.annotations.Select("SELECT p.* FROM products p " +
            "INNER JOIN after_sale_items t ON p.id = t.product_id " +
            "GROUP BY p.id ORDER BY COUNT(*) DESC LIMIT #{limit}")
    java.util.List<Product> selectAfterSaleTop(@Param("limit") int limit);
}
