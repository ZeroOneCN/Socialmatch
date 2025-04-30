package com.soical.server.service.impl;

import com.soical.server.service.UserOnlineStatusService;
import com.soical.server.websocket.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户在线状态服务实现类
 */
@Service
@Slf4j
public class UserOnlineStatusServiceImpl implements UserOnlineStatusService {

    // 用户在线状态缓存，true表示在线，false表示离线
    private static final Map<Long, Boolean> USER_ONLINE_STATUS = new ConcurrentHashMap<>();
    
    // 用户最后活跃时间缓存
    private static final Map<Long, Long> USER_LAST_ACTIVE_TIME = new ConcurrentHashMap<>();
    
    // 用户状态超时时间（毫秒）- 30秒没有活动则视为离线
    private static final long USER_STATUS_TIMEOUT = 30000;
    
    private final ApplicationContext applicationContext;
    
    @Autowired
    public UserOnlineStatusServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    // 使用延迟加载方式获取ChatWebSocketHandler，避免循环依赖
    private ChatWebSocketHandler getChatWebSocketHandler() {
        return applicationContext.getBean(ChatWebSocketHandler.class);
    }

    @Override
    public void userOnline(Long userId) {
        if (userId != null) {
            USER_ONLINE_STATUS.put(userId, true);
            // 更新用户最后活跃时间
            USER_LAST_ACTIVE_TIME.put(userId, System.currentTimeMillis());
            log.info("用户 {} 设置为在线状态", userId);
        }
    }

    @Override
    public void userOffline(Long userId) {
        if (userId != null) {
            USER_ONLINE_STATUS.put(userId, false);
            // 删除最后活跃时间记录
            USER_LAST_ACTIVE_TIME.remove(userId);
            
            // 尝试关闭WebSocket连接
            try {
                ChatWebSocketHandler handler = getChatWebSocketHandler();
                handler.closeUserSession(userId);
                log.info("已关闭用户 {} 的WebSocket连接", userId);
            } catch (Exception e) {
                log.error("关闭用户WebSocket连接失败", e);
            }
            
            log.info("用户 {} 设置为离线状态", userId);
        }
    }

    @Override
    public boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        
        // 首先检查本地缓存
        Boolean status = USER_ONLINE_STATUS.get(userId);
        
        // 检查最后活跃时间是否超时
        Long lastActiveTime = USER_LAST_ACTIVE_TIME.get(userId);
        if (lastActiveTime != null && status != null && status) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastActiveTime > USER_STATUS_TIMEOUT) {
                log.info("用户 {} 状态超时，标记为离线", userId);
                // 超时，设置为离线
                USER_ONLINE_STATUS.put(userId, false);
                status = false;
            }
        }
        
        // 如果缓存中没有，则从WebSocket会话中检查
        if (status == null) {
            try {
                ChatWebSocketHandler handler = getChatWebSocketHandler();
                status = handler.isUserConnected(userId);
                
                // 更新缓存
                if (status) {
                    USER_ONLINE_STATUS.put(userId, status);
                    // 如果在线，更新最后活跃时间
                    USER_LAST_ACTIVE_TIME.put(userId, System.currentTimeMillis());
                } else {
                    // 如果离线，从缓存中删除
                    USER_ONLINE_STATUS.remove(userId);
                    USER_LAST_ACTIVE_TIME.remove(userId);
                }
            } catch (Exception e) {
                log.error("检查用户在线状态失败", e);
                status = false;
                
                // 异常情况下，也从缓存中删除
                USER_ONLINE_STATUS.remove(userId);
                USER_LAST_ACTIVE_TIME.remove(userId);
            }
        }
        
        return status != null && status;
    }

    @Override
    public Map<Long, Boolean> batchGetUserOnlineStatus(List<Long> userIds) {
        Map<Long, Boolean> resultMap = new HashMap<>();
        
        if (userIds == null || userIds.isEmpty()) {
            return resultMap;
        }
        
        // 检查是否有过期的在线状态缓存
        cleanupStaleStatus();
        
        for (Long userId : userIds) {
            if (userId != null) {
                resultMap.put(userId, isUserOnline(userId));
            }
        }
        
        return resultMap;
    }
    
    /**
     * 清理过期的在线状态缓存
     */
    private void cleanupStaleStatus() {
        try {
            ChatWebSocketHandler handler = getChatWebSocketHandler();
            long currentTime = System.currentTimeMillis();
            
            // 遍历所有缓存的在线状态
            Iterator<Map.Entry<Long, Boolean>> iterator = USER_ONLINE_STATUS.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Boolean> entry = iterator.next();
                Long userId = entry.getKey();
                Boolean status = entry.getValue();
                
                // 检查WebSocket连接状态
                boolean isConnected = handler.isUserConnected(userId);
                
                // 获取最后活跃时间
                Long lastActiveTime = USER_LAST_ACTIVE_TIME.get(userId);
                boolean isTimeout = lastActiveTime != null && (currentTime - lastActiveTime > USER_STATUS_TIMEOUT);
                
                // 如果缓存中状态为在线，但实际WebSocket会话已断开，或者超时，则清除缓存
                if (status && (!isConnected || isTimeout)) {
                    log.info("用户 {} 会话已断开或状态超时，清除过期的在线状态", userId);
                    iterator.remove();
                    USER_LAST_ACTIVE_TIME.remove(userId);
                }
            }
        } catch (Exception e) {
            log.error("清理过期在线状态失败", e);
        }
    }
} 