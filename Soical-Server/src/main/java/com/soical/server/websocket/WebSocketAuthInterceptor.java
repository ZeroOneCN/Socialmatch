package com.soical.server.websocket;

import com.soical.server.entity.User;
import com.soical.server.service.TokenService;
import com.soical.server.service.UserService;
import com.soical.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * WebSocket握手拦截器，用于验证用户身份
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final TokenService tokenService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            // 获取请求URL和相关信息
            String uri = request.getURI().toString();
            String path = request.getURI().getPath();
            log.info("WebSocket握手请求: URI={}, Path={}", uri, path);
            
            // 获取请求URL中的参数
            String query = request.getURI().getQuery();
            if (!StringUtils.hasText(query)) {
                log.error("WebSocket连接缺少认证参数");
                return false;
            }
            
            log.info("WebSocket连接参数: {}", query);
            
            // 解析参数
            Map<String, String> params = parseQueryString(query);
            // 调试日志：打印所有URL参数
            log.info("解析出的URL参数: {}", params);
            
            String token = params.get("token");
            String userIdStr = params.get("userId");
            
            log.info("从URL参数中获取: token={}, userId={}", 
                     token != null ? (token.substring(0, Math.min(token.length(), 20)) + "...") : "null", 
                     userIdStr);
            
            // 尝试URL解码token
            if (StringUtils.hasText(token)) {
                try {
                    token = URLDecoder.decode(token, StandardCharsets.UTF_8.name());
                    log.info("解码后的token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                } catch (Exception e) {
                    log.warn("Token URL解码失败，将使用原始值: {}", e.getMessage());
                }
            } else {
                log.error("WebSocket连接缺少token参数");
                return false;
            }
            
            // 尝试从userIdStr中获取userId
            Long userIdFromParam = null;
            if (StringUtils.hasText(userIdStr)) {
                try {
                    userIdFromParam = Long.parseLong(userIdStr);
                    log.info("从URL参数中提取的userId: {}", userIdFromParam);
                } catch (NumberFormatException e) {
                    log.warn("URL参数中的userId格式无效: {}", userIdStr);
                    // 继续执行，尝试从token中获取userId
                }
            }
            
            // 从token中获取userId (优先使用TokenService，回退到JwtUtil)
            Long userIdFromToken = null;
            try {
                userIdFromToken = tokenService.getUserIdFromToken(token);
                if (userIdFromToken == null) {
                    log.warn("TokenService无法从token中提取用户ID，尝试使用JwtUtil");
                    userIdFromToken = jwtUtil.getUserIdFromToken(token);
                }
            } catch (Exception e) {
                log.error("从token提取userId时发生异常，尝试使用JwtUtil", e);
                try {
                    userIdFromToken = jwtUtil.getUserIdFromToken(token);
                } catch (Exception ex) {
                    log.error("JwtUtil也无法从token中提取用户ID", ex);
                }
            }
            
            if (userIdFromToken != null) {
                log.info("从token中提取的用户ID: {}", userIdFromToken);
            } else {
                log.error("无法从token中提取用户ID");
                // 如果从token中无法获取userId，但URL参数中提供了userId，则使用URL参数中的userId
                if (userIdFromParam != null) {
                    log.info("使用URL参数中的userId: {}", userIdFromParam);
                    userIdFromToken = userIdFromParam;
                } else {
                    return false;
                }
            }
            
            // 确保有userId
            final Long userId = Optional.ofNullable(userIdFromParam).orElse(userIdFromToken);
            if (userId == null) {
                log.error("无法获取有效的用户ID");
                return false;
            }
            
            // 验证token
            boolean tokenValid = false;
            try {
                tokenValid = tokenService.verify(token, userId);
            } catch (Exception e) {
                log.error("使用TokenService验证token时出错", e);
            }
            
            if (!tokenValid) {
                log.warn("TokenService验证token失败，尝试使用JwtUtil验证");
                try {
                    tokenValid = jwtUtil.validateToken(token);
                } catch (Exception e) {
                    log.error("使用JwtUtil验证token时出错", e);
                }
            }
            
            if (!tokenValid) {
                log.error("验证失败: token无效");
                return false;
            }
            
            // 尝试获取用户信息以确认用户存在
            User user = userService.getById(userId);
            if (user == null) {
                log.error("用户不存在: {}", userId);
                return false;
            }
            
            // 将用户ID存储到WebSocket会话属性中
            attributes.put("userId", userId);
            log.info("WebSocket认证成功，用户ID: {}", userId);
            return true;
        } catch (Exception e) {
            log.error("WebSocket认证过程中出错", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理，通常不需要额外操作
    }
    
    /**
     * 解析查询字符串为参数Map
     */
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new java.util.HashMap<>();
        
        if (query == null || query.isEmpty()) {
            return params;
        }
        
        // 按&分割，处理每个参数对
        for (String part : query.split("&")) {
            try {
                // 仅处理有=号的参数对
                int equalsIndex = part.indexOf('=');
                if (equalsIndex > 0) { // 确保=号不是第一个字符
                    String key = part.substring(0, equalsIndex);
                    String value = part.substring(equalsIndex + 1);
                    
                    // 尝试URL解码
                    try {
                        key = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
                        value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                    } catch (Exception e) {
                        log.warn("URL解码参数失败: {} = {}, 错误: {}", key, value, e.getMessage());
                        // 继续使用原始值
                    }
                    
                    // 存储到Map中
                    params.put(key, value);
                    log.debug("解析查询参数: {} = {}", key, value);
                }
            } catch (Exception e) {
                log.warn("解析查询参数失败: {}, 错误: {}", part, e.getMessage());
            }
        }
        
        return params;
    }
} 