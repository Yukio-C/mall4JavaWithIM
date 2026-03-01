package com.mall.controller.common;

import com.mall.common.Result;
import com.mall.config.WebSocketInterceptor;
import com.mall.entity.po.ChatMessage;
import com.mall.entity.vo.ChatSessionVO;
import com.mall.service.IChatService;
import com.mall.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/common/chat")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "IM 聊天管理")
public class ChatController {

    private final IChatService chatService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 获取指定订单的聊天历史
     */
    @GetMapping("/history/{orderNo}")
    @Operation(summary = "获取指定订单的聊天历史")
    public Result<List<ChatMessage>> getChatHistory(@PathVariable("orderNo") String orderNo) {
        log.info("查询历史消息: orderNo={}", orderNo);
        return Result.success(chatService.getChatHistory(orderNo));
    }

    /**
     * 查询是否有客服在线
     */
    @GetMapping("/status")
    @Operation(summary = "查询是否有客服在线")
    public Result<Boolean> getSupportStatus() {
        Set<String> keys = redisTemplate.keys(WebSocketInterceptor.ONLINE_STAFF_PREFIX + "*");
        boolean isOnline = keys != null && !keys.isEmpty();
        return Result.success(isOnline);
    }

    /**
     * 用户发送消息
     */
    @MessageMapping("/chat.send")
    public void handleChatMessage(ChatMessage msg, SimpMessageHeaderAccessor accessor) {
        chatService.handleUserMessage(msg, accessor);
    }

    /**
     * 客服发送消息
     */
    @MessageMapping("/chat.admin.send")
    public void handleAdminChatMessage(ChatMessage msg, SimpMessageHeaderAccessor accessor) {
        chatService.handleAdminMessage(msg, accessor);
    }

    /**
     * 客服端：获取当前咨询列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取客服当前活跃会话")
    public Result<List<ChatSessionVO>> getMySessions(@AuthenticationPrincipal LoginUser loginUser) {
        String staffId = String.valueOf(loginUser.getUser().getId());
        return Result.success(chatService.getMySessionsVO(staffId));
    }

    /**
     * 用户端：获取自己的咨询列表
     */
    @GetMapping("/user-sessions")
    @Operation(summary = "获取用户当前咨询记录")
    public Result<List<ChatSessionVO>> getUserSessions(@AuthenticationPrincipal LoginUser loginUser) {
        String userId = String.valueOf(loginUser.getUser().getId());
        return Result.success(chatService.getUserSessionsVO(userId));
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/read")
    @Operation(summary = "标记订单消息为已读")
    public Result<String> markAsRead(@RequestParam String orderNo, @AuthenticationPrincipal LoginUser loginUser) {
        String currentId = String.valueOf(loginUser.getUser().getId());
        chatService.markAsRead(orderNo, currentId);
        return Result.success("ok");
    }

    /**
     * 获取未读摘要
     */
    @GetMapping("/unread-summary")
    @Operation(summary = "获取未读消息摘要")
    public Result<Map<String, Integer>> getUnreadSummary(@AuthenticationPrincipal LoginUser loginUser) {
        String userId = String.valueOf(loginUser.getUser().getId());
        return Result.success(chatService.getUserUnreadSummary(userId));
    }
}
