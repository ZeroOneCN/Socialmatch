package com.soical.server.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soical.server.entity.ChatMessage;
import com.soical.server.service.ChatMessageQueueService;
import com.soical.server.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天消息队列服务实现类
 * 使用Redis实现消息的发布和订阅功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageQueueServiceImpl implements ChatMessageQueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ObjectMapper objectMapper;
    private final ChatWebSocketHandler chatWebSocketHandler;
    
    // Redis通道前缀
    private static final String CHAT_CHANNEL_PREFIX = "soical:chat:user:";
    
    @PostConstruct
    @Override
    public void initializeSubscriptions() {
        log.info("初始化Redis消息订阅...");
        startMessageSubscription();
        log.info("Redis消息订阅已初始化");
    }
    
    @Override
    public boolean publishMessage(ChatMessage message) {
        if (message == null || message.getReceiverId() == null) {
            log.error("消息或接收者ID为空，无法发布消息");
            return false;
        }
        
        String channel = createChannelForUser(message.getReceiverId());
        
        try {
            // 使用字符串形式发送消息，避免复杂序列化问题
            String messageJson = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(channel, messageJson);
            log.info("消息已发布到Redis通道: {}", channel);
            return true;
        } catch (Exception e) {
            log.error("发布消息到Redis失败", e);
            return false;
        }
    }
    
    @Override
    public void startMessageSubscription() {
        log.info("启动Redis消息订阅，订阅通道模式: {}", CHAT_CHANNEL_PREFIX + "*");
        
        // 创建消息监听器并添加到容器
        ChatMessageListener chatMessageListener = new ChatMessageListener(objectMapper, chatWebSocketHandler);
        redisMessageListenerContainer.addMessageListener(chatMessageListener, new PatternTopic(CHAT_CHANNEL_PREFIX + "*"));
        
        log.info("Redis消息订阅已成功启动");
    }
    
    @Override
    public void stopMessageSubscription() {
        log.info("停止Redis消息订阅");
        // 这里可以实现停止订阅的逻辑，如果需要的话
    }
    
    @Override
    public String getChannelPrefix() {
        return CHAT_CHANNEL_PREFIX;
    }
    
    @Override
    public String createChannelForUser(Long userId) {
        return CHAT_CHANNEL_PREFIX + userId;
    }
    
    /**
     * Redis消息监听器内部类
     * 负责接收Redis通道中的消息并转发到WebSocket
     */
    private static class ChatMessageListener implements org.springframework.data.redis.connection.MessageListener {
        
        private final ObjectMapper objectMapper;
        private final ChatWebSocketHandler chatWebSocketHandler;
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChatMessageListener.class);
        
        // 用于记录正在处理的消息，避免重复处理
        private final Set<String> processingMessages = ConcurrentHashMap.newKeySet();
        
        public ChatMessageListener(ObjectMapper objectMapper, ChatWebSocketHandler chatWebSocketHandler) {
            this.objectMapper = objectMapper;
            this.chatWebSocketHandler = chatWebSocketHandler;
        }
        
        @Override
        public void onMessage(Message message, byte[] pattern) {
            // 获取消息内容
            byte[] body = message.getBody();
            String channel = new String(message.getChannel());
            
            // 转换为字符串
            String messageString = new String(body);
            
            // 使用消息内容的哈希值作为消息ID，检查是否已在处理中
            String messageId = String.valueOf(messageString.hashCode());
            
            // 如果消息已在处理中，则忽略
            if (!processingMessages.add(messageId)) {
                log.info("消息已在处理中，跳过: {}", messageId);
                return;
            }
            
            try {
                log.info("收到Redis消息，通道: {}", channel);
                log.debug("消息内容: {}", messageString);
                
                ChatMessage chatMessage = null;
                Exception lastException = null;
                
                // 尝试JSON反序列化
                try {
                    chatMessage = objectMapper.readValue(messageString, ChatMessage.class);
                } catch (Exception e) {
                    lastException = e;
                    log.debug("JSON直接反序列化失败: {}", e.getMessage());
                    
                    // 如果是泛型JSON格式，可能需要特殊处理
                    try {
                        // 先解析为Map
                        Map<String, Object> map = objectMapper.readValue(messageString, 
                            new TypeReference<HashMap<String, Object>>() {});
                        
                        // 如果包含@class字段，说明是GenericJackson2JsonRedisSerializer格式
                        if (map.containsKey("@class") && map.get("@class").equals("com.soical.server.entity.ChatMessage")) {
                            chatMessage = convertMapToChatMessage(map);
                        }
                    } catch (Exception ex) {
                        log.debug("Map反序列化也失败: {}", ex.getMessage());
                    }
                }
                
                // 如果反序列化成功且有接收者ID
                if (chatMessage != null && chatMessage.getReceiverId() != null) {
                    try {
                        // 将ChatMessage对象转换为JSON字符串
                        String messageJson = objectMapper.writeValueAsString(chatMessage);
                        
                        // 添加同步锁，防止同时向同一WebSocket会话写入消息
                        synchronized (chatWebSocketHandler) {
                            chatWebSocketHandler.sendMessageToUser(chatMessage.getReceiverId(), messageJson);
                            log.info("消息已成功转发到用户: {}", chatMessage.getReceiverId());
                        }
                    } catch (JsonProcessingException e) {
                        log.error("消息序列化为JSON失败: {}", e.getMessage());
                    }
                } else {
                    log.error("消息反序列化失败或缺少接收者ID");
                    if (lastException != null) {
                        log.debug("异常详情: {}", lastException.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("处理Redis消息时出错: {}", e.getMessage());
                log.debug("异常详情", e);
            } finally {
                // 处理完成后，从集合中移除
                processingMessages.remove(messageId);
            }
        }
        
        /**
         * 将Map转换为ChatMessage对象
         */
        private ChatMessage convertMapToChatMessage(Map<String, Object> map) {
            ChatMessage chatMessage = new ChatMessage();
            
            try {
                // 处理ID字段
                processIdField(map, "messageId", chatMessage::setMessageId);
                processIdField(map, "conversationId", chatMessage::setConversationId);
                processIdField(map, "senderId", chatMessage::setSenderId);
                processIdField(map, "receiverId", chatMessage::setReceiverId);
                
                // 处理基本类型
                if (map.containsKey("content")) {
                    chatMessage.setContent(map.get("content").toString());
                }
                
                if (map.containsKey("messageType")) {
                    Object value = map.get("messageType");
                    chatMessage.setMessageType(value instanceof Number ? ((Number) value).intValue() : Integer.valueOf(value.toString()));
                }
                
                if (map.containsKey("isRead")) {
                    Object value = map.get("isRead");
                    chatMessage.setIsRead(value instanceof Boolean ? (Boolean) value : Boolean.valueOf(value.toString()));
                }
                
                // 处理日期字段
                if (map.containsKey("createTime")) {
                    Object timeValue = map.get("createTime");
                    if (timeValue instanceof Object[] && ((Object[])timeValue).length == 2 && 
                        "java.util.Date".equals(((Object[])timeValue)[0])) {
                        // 处理格式为 ["java.util.Date", timestamp] 的情况
                        Object timestamp = ((Object[])timeValue)[1];
                        if (timestamp instanceof Number) {
                            chatMessage.setCreateTime(new Date(((Number) timestamp).longValue()));
                        }
                    } else if (timeValue instanceof String) {
                        try {
                            chatMessage.setCreateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse((String) timeValue));
                        } catch (Exception e) {
                            chatMessage.setCreateTime(new Date());
                        }
                    } else if (timeValue instanceof Date) {
                        chatMessage.setCreateTime((Date) timeValue);
                    } else if (timeValue instanceof Number) {
                        chatMessage.setCreateTime(new Date(((Number) timeValue).longValue()));
                    } else {
                        chatMessage.setCreateTime(new Date());
                    }
                } else {
                    chatMessage.setCreateTime(new Date());
                }
            } catch (Exception e) {
                log.warn("转换Map到ChatMessage时出错: {}", e.getMessage());
            }
            
            return chatMessage;
        }
        
        /**
         * 处理ID字段，支持多种格式
         */
        private void processIdField(Map<String, Object> map, String fieldName, java.util.function.Consumer<Long> setter) {
            if (map.containsKey(fieldName)) {
                Object value = map.get(fieldName);
                if (value instanceof Number) {
                    setter.accept(((Number) value).longValue());
                } else if (value instanceof String) {
                    try {
                        setter.accept(Long.valueOf((String) value));
                    } catch (NumberFormatException e) {
                        log.warn("无法将字符串转换为Long: {}", value);
                    }
                } else if (value instanceof Object[] && ((Object[])value).length == 2 && 
                           "java.lang.Long".equals(((Object[])value)[0])) {
                    // 处理格式为 ["java.lang.Long", value] 的情况
                    Object idValue = ((Object[])value)[1];
                    if (idValue instanceof Number) {
                        setter.accept(((Number) idValue).longValue());
                    } else {
                        try {
                            setter.accept(Long.valueOf(idValue.toString()));
                        } catch (NumberFormatException e) {
                            log.warn("无法将值转换为Long: {}", idValue);
                        }
                    }
                }
            }
        }
    }
} 