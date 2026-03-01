package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.po.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Update("UPDATE `orders` SET status = #{targetStatus} WHERE id = #{orderId} AND status IN (${validStatuses})")
    int updateStatusWithLock(@Param("orderId") Long orderId, @Param("targetStatus") Integer targetStatus, @Param("validStatuses") String validStatuses);
}
