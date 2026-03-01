package com.mall.config;

import com.mall.security.LoginUser;
import com.mall.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 终极 WebSocket 拦截器
 * 已加固：支持上线实时写入、心跳实时续期、离线实时销毁
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final StringRedisTemplate redisTemplate;

    public static final String ONLINE_USER_PREFIX = "online:user:";
    public static final String ONLINE_STAFF_PREFIX = "online:staff:";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();

        // 1. 处理连接认证
        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        }
        
        // 2. 处理断开连接 (核心修复：实时清理)
        else if (StompCommand.DISCONNECT.equals(command)) {
            cleanOnlineStatus(accessor);
        }

        // 3. 状态续期 (只要有数据交互就延长寿命)
        refreshOnlineStatus(accessor);

        // 4. 身份补救
        if (StompCommand.SEND.equals(command) && accessor.getUser() == null) {
            Map<String, Object> attrs = accessor.getSessionAttributes();
            if (attrs != null && attrs.containsKey("userId")) {
                String uid = (String) attrs.get("userId");
                accessor.setUser(new UsernamePasswordAuthenticationToken(uid, null, null));
            }
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authToken = accessor.getFirstNativeHeader("Authorization");
        if (authToken == null) authToken = accessor.getFirstNativeHeader("token");

        if (authToken != null) {
            String jwt = authToken.startsWith("Bearer ") ? authToken.substring(7) : authToken;
            try {
                String username = jwtUtils.extractUsername(jwt);
                if (username != null) {
                    LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(username);
                    if (jwtUtils.isTokenValid(jwt, loginUser.getUsername())) {
                        String role = loginUser.getUser().getRole();
                        String userId = String.valueOf(loginUser.getUser().getId());

                        Map<String, Object> attrs = accessor.getSessionAttributes();
                        if (attrs != null) {
                            attrs.put("userId", userId);
                            attrs.put("role", role);
                        }

                        accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, loginUser.getAuthorities()));

                        // 上线：写入 Redis
                        String key = "USER".equalsIgnoreCase(role) ? ONLINE_USER_PREFIX + userId : ONLINE_STAFF_PREFIX + userId;
                        redisTemplate.opsForValue().set(key, "1", 30, TimeUnit.MINUTES);
                        log.info("WebSocket 上线: Role={}, ID={}", role, userId);
                    }
                }
            } catch (Exception e) {
                log.error("WebSocket 认证异常: {}", e.getMessage());
            }
        }
    }

    private void refreshOnlineStatus(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) return;

        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs != null && attrs.containsKey("role")) {
            String role = (String) attrs.get("role");
            String key = "USER".equalsIgnoreCase(role) ? ONLINE_USER_PREFIX + principal.getName() : ONLINE_STAFF_PREFIX + principal.getName();
            redisTemplate.expire(key, 30, TimeUnit.MINUTES);
        }
    }

    private void cleanOnlineStatus(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) return;

        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs != null && attrs.containsKey("role")) {
            String role = (String) attrs.get("role");
            String key = "USER".equalsIgnoreCase(role) ? ONLINE_USER_PREFIX + principal.getName() : ONLINE_STAFF_PREFIX + principal.getName();
            
            // 下线：立即删除键
            redisTemplate.delete(key);
            log.info("WebSocket 下线清理: Role={}, ID={}", role, principal.getName());
        }
    }
}
