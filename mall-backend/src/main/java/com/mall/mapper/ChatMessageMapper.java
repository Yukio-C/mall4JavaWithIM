package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.po.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 获取与该客服有关的所有订单号 (去重，且按最新消息排序)
     */
    @Select("SELECT order_no FROM chat_messages " +
            "WHERE from_user_id = #{adminId} OR to_user_id = #{adminId} " +
            "GROUP BY order_no " +
            "ORDER BY MAX(id) DESC")
    List<String> selectOrderNosByAdminId(@Param("adminId") String adminId);

    /**
     * 获取指定订单中，该客服未读的消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_messages " +
            "WHERE order_no = #{orderNo} AND to_user_id = #{adminId} AND is_read = 0")
    int countUnread(@Param("orderNo") String orderNo, @Param("adminId") String adminId);

    /**
     * 获取指定订单最后一条消息内容
     */
    @Select("SELECT content FROM chat_messages WHERE order_no = #{orderNo} ORDER BY id DESC LIMIT 1")
    String selectLastContent(@Param("orderNo") String orderNo);

    /**
     * 获取用户总的未读消息数 (针对所有订单)
     */
    @Select("SELECT COUNT(*) FROM chat_messages WHERE to_user_id = #{userId} AND is_read = 0")
    int countUserTotalUnread(@Param("userId") String userId);

    /**
     * 获取用户在各订单中的未读数映射
     */
    @Select("SELECT order_no, COUNT(*) as count FROM chat_messages " +
            "WHERE to_user_id = #{userId} AND is_read = 0 GROUP BY order_no")
    java.util.List<java.util.Map<String, Object>> selectUnreadCountsByOrder(@Param("userId") String userId);

    /**
     * 获取用户所有咨询过的订单号
     */
    @Select("SELECT order_no FROM chat_messages " +
            "WHERE from_user_id = #{userId} OR to_user_id = #{userId} " +
            "GROUP BY order_no " +
            "ORDER BY MAX(id) DESC")
    List<String> selectOrderNosByUserId(@Param("userId") String userId);

    /**
     * 获取指定订单中，用户未读的消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_messages " +
            "WHERE order_no = #{orderNo} AND to_user_id = #{userId} AND is_read = 0")
    int countUserUnread(@Param("orderNo") String orderNo, @Param("userId") String userId);
}
