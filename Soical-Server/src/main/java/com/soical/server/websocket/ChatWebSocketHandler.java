package com.soical.server.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.HashMap;

/**
 * WebSocket聊天消息处理器
 */
@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    // 用户ID到会话的映射
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    // 会话ID到用户ID的映射
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();
    
    // 添加applicationContext
    private final org.springframework.context.ApplicationContext applicationContext;
    
    // 构造函数注入applicationContext
    @org.springframework.beans.factory.annotation.Autowired
    public ChatWebSocketHandler(org.springframework.context.ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    /**
     * 检查用户是否已连接
     * @param userId 用户ID
     * @return 是否连接
     */
    public boolean isUserConnected(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
    
    /**
     * 连接建立时
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket连接已建立: {}, 查询参数: {}", session.getId(), session.getUri().getQuery());
        
        // 打印会话中的所有属性
        session.getAttributes().forEach((key, value) -> {
            log.info("会话属性: {} = {}", key, value);
        });
        
        // 从会话中获取用户ID
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            log.info("用户 {} 的WebSocket连接已建立", userId);
            registerUserSession(userId, session);
        } else {
            log.warn("WebSocket连接已建立，但未找到用户ID");
        }
    }
    
    /**
     * 接收消息时
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("接收到WebSocket消息: {}", message.getPayload());
        
        try {
            // 尝试解析消息为JSON
            String messageText = message.getPayload();
            if (messageText != null && !messageText.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> messageMap = mapper.readValue(messageText, Map.class);
                
                // 判断消息类型
                String type = (String) messageMap.get("type");
                if ("HEARTBEAT".equals(type)) {
                    // 处理心跳消息
                    handleHeartbeat(session);
                } else if ("GET_USER_STATUS".equals(type)) {
                    // 处理在线状态请求
                    handleStatusRequest(session, messageMap);
                }
                
                // 用户活动时更新状态
                updateUserStatus(session);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }
    
    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(WebSocketSession session) {
        try {
            // 返回pong响应
            session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
            
            // 更新用户状态
            updateUserStatus(session);
        } catch (Exception e) {
            log.error("发送心跳响应失败", e);
        }
    }
    
    /**
     * 处理状态请求
     */
    private void handleStatusRequest(WebSocketSession session, Map<String, Object> message) {
        try {
            // 获取请求的用户ID列表
            Object userIdsObj = message.get("userIds");
            if (userIdsObj instanceof List) {
                List<Object> userIdsRaw = (List<Object>) userIdsObj;
                Map<String, Object> response = new HashMap<>();
                response.put("type", "USER_STATUS_RESPONSE");
                Map<String, Boolean> statusMap = new HashMap<>();
                
                // 检查每个用户的状态
                for (Object userIdObj : userIdsRaw) {
                    Long userId = null;
                    if (userIdObj instanceof Integer) {
                        userId = ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        userId = (Long) userIdObj;
                    } else if (userIdObj instanceof String) {
                        try {
                            userId = Long.parseLong((String) userIdObj);
                        } catch (NumberFormatException e) {
                            log.warn("无效的用户ID格式: {}", userIdObj);
                        }
                    }
                    
                    if (userId != null) {
                        boolean isOnline = isUserConnected(userId);
                        statusMap.put(String.valueOf(userId), isOnline);
                    }
                }
                
                response.put("statusMap", statusMap);
                
                // 发送响应
                session.sendMessage(new TextMessage(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(response)));
            }
        } catch (Exception e) {
            log.error("处理状态请求失败", e);
        }
    }
    
    /**
     * 更新用户活动状态
     */
    private void updateUserStatus(WebSocketSession session) {
        try {
            // 从会话获取用户ID
            Long userId = (Long) session.getAttributes().get("userId");
            if (userId != null) {
                // 更新用户状态为在线
                com.soical.server.service.UserOnlineStatusService statusService = 
                    applicationContext.getBean(com.soical.server.service.UserOnlineStatusService.class);
                statusService.userOnline(userId);
                log.debug("已更新用户{}活动状态", userId);
            }
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
        }
    }
    
    /**
     * 连接关闭时
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = sessionUsers.remove(session.getId());
        if (userId != null) {
            userSessions.remove(userId);
            log.info("用户 {} 的WebSocket连接已关闭: {}", userId, status);
        } else {
            log.info("未知用户的WebSocket连接已关闭: {}, {}", session.getId(), status);
        }
    }
    
    /**
     * 注册用户会话
     * @param userId 用户ID
     * @param session WebSocket会话
     */
    public void registerUserSession(Long userId, WebSocketSession session) {
        if (userId == null || session == null) {
            return;
        }
        
        // 关闭已有会话
        WebSocketSession existingSession = userSessions.get(userId);
        if (existingSession != null && existingSession.isOpen()) {
            try {
                existingSession.close();
            } catch (Exception e) {
                log.error("关闭旧会话失败", e);
            }
        }
        
        // 注册新会话
        userSessions.put(userId, session);
        sessionUsers.put(session.getId(), userId);
        log.info("用户 {} 已注册WebSocket会话: {}", userId, session.getId());
    }
    
    /**
     * 发送消息给指定用户
     * @param userId 用户ID
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessageToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (Exception e) {
                log.error("发送WebSocket消息失败", e);
            }
        }
        return false;
    }
    
    /**
     * 获取连接用户数量
     * @return 连接用户数量
     */
    public int getConnectedUserCount() {
        return userSessions.size();
    }
    
    /**
     * 发送消息给客户端
     * @param session WebSocket会话
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessageToClient(WebSocketSession session, String message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (Exception e) {
                log.error("发送WebSocket消息到客户端失败", e);
                return false;
            }
        }
        return false;
    }
    
    /**
     * 关闭指定用户的WebSocket会话
     * @param userId 用户ID
     */
    public void closeUserSession(Long userId) {
        if (userId == null) {
            return;
        }
        
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                // 发送关闭消息通知客户端
                try {
                    session.sendMessage(new TextMessage("{\"type\":\"SESSION_CLOSED\",\"reason\":\"User logged out\"}"));
                } catch (Exception e) {
                    log.warn("发送会话关闭消息失败: {}", e.getMessage());
                }
                
                // 关闭会话
                session.close(CloseStatus.NORMAL);
                log.info("已关闭用户 {} 的WebSocket会话", userId);
                
                // 从映射中移除
                userSessions.remove(userId);
                sessionUsers.remove(session.getId());
            } catch (Exception e) {
                log.error("关闭用户会话失败", e);
            }
        }
    }
} 