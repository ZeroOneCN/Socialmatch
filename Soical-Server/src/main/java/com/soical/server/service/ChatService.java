package com.soical.server.service;

/**
 * 聊天服务接口
 */
public interface ChatService {
    
    /**
     * 统计用户的对话数量
     *
     * @param userId 用户ID
     * @return 对话数量
     */
    Integer countUserConversations(Long userId);
} 