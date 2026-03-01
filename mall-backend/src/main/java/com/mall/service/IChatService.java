package com.mall.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.entity.po.ChatMessage;
import com.mall.entity.vo.ChatSessionVO;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.List;

public interface IChatService extends IService<ChatMessage> {
    /**
     * 处理用户发来的消息
     */
    void handleUserMessage(ChatMessage msg, SimpMessageHeaderAccessor accessor);

    /**
     * 获取指定订单的聊天历史
     */
    List<ChatMessage> getChatHistory(String orderNo);

    /**
     * 处理客服/管理员发出的消息
     */
    void handleAdminMessage(ChatMessage msg, SimpMessageHeaderAccessor accessor);

    /**
     * 获取客服当前活跃的会话列表 (带未读数)
     */
    List<ChatSessionVO> getMySessionsVO(String adminId);

    /**
     * 获取用户当前参与的会话列表 (带未读数)
     */
    List<ChatSessionVO> getUserSessionsVO(String userId);

    /**
     * 获取当前用户的未读消息统计
     */
    java.util.Map<String, Integer> getUserUnreadSummary(String userId);

    /**
     * 标记指定订单的消息为已读 (针对当前用户)
     * @param orderNo
     * @param currentId
     */
    void markAsRead(String orderNo, String currentId);
}
