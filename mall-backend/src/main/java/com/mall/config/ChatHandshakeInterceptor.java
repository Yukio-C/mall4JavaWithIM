package com.mall.config;

import com.mall.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 工业级做法：WebSocket 握手拦截器
 * 确保在 HTTP 协议升级为 WebSocket 的瞬间，身份信息就进入了底层 Session 的存储区
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 尝试从 URL 参数中提取 token (?token=xxx)
            String token = servletRequest.getServletRequest().getParameter("token");
            
            // 兼容 Header 模式
            if (token == null) {
                String auth = servletRequest.getServletRequest().getHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                    token = auth.substring(7);
                }
            }

            if (token != null) {
                try {
                    Long userId = jwtUtils.extractUserId(token);
                    if (userId != null) {
                        // 物理锁死：将 userId 存入底层 WebSocket 属性区
                        // 这一步非常关键，它能跨越 STOMP 的所有消息类型
                        attributes.put("userId", String.valueOf(userId));
                        log.info("WebSocket 物理握手成功: UserID={}", userId);
                    }
                } catch (Exception e) {
                    log.warn("握手阶段身份解析失败: {}", e.getMessage());
                }
            }
        }
        return true; 
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
