package com.soical.server.service;

import com.soical.server.entity.ChatMessage;

/**
 * 聊天消息队列服务接口
 * 用于通过Redis消息队列进行消息发布和订阅
 */
public interface ChatMessageQueueService {
    
    /**
     * 发布消息到Redis通道
     * @param message 要发布的聊天消息
     * @return 操作是否成功
     */
    boolean publishMessage(ChatMessage message);
    
    /**
     * 启动消息订阅
     * 订阅Redis中的消息通道，并处理接收到的消息
     */
    void startMessageSubscription();
    
    /**
     * 停止消息订阅
     */
    void stopMessageSubscription();
    
    /**
     * 初始化消息订阅
     */
    void initializeSubscriptions();
    
    /**
     * 获取消息通道前缀
     * @return 通道前缀
     */
    String getChannelPrefix();
    
    /**
     * 为指定用户创建通道名
     * @param userId 用户ID
     * @return 通道名
     */
    String createChannelForUser(Long userId);
} 