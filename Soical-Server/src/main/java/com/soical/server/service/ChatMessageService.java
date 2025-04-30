package com.soical.server.service;

import com.soical.server.entity.ChatMessage;

import java.util.List;

/**
 * 聊天消息服务接口
 */
public interface ChatMessageService {
    
    /**
     * 保存消息
     * @param message 消息对象
     * @return 消息ID
     */
    Long saveMessage(ChatMessage message);
    
    /**
     * 根据ID获取消息
     * @param messageId 消息ID
     * @return 消息对象，如果不存在返回null
     */
    ChatMessage getById(Long messageId);
    
    /**
     * 获取会话的消息列表
     * @param conversationId 会话ID
     * @return 消息列表
     */
    List<ChatMessage> getMessagesByConversationId(Long conversationId);
    
    /**
     * 获取两个用户之间的消息
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param page 页码
     * @param size 每页大小
     * @return 消息列表
     */
    List<ChatMessage> getMessagesBetweenUsers(Long senderId, Long receiverId, int page, int size);
    
    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @return 是否标记成功
     */
    boolean markMessageAsRead(Long messageId);
    
    /**
     * 批量标记消息为已读
     * @param conversationId 会话ID
     * @param receiverId 接收者ID
     * @return 标记的消息数量
     */
    int markMessagesAsRead(Long conversationId, Long receiverId);
} 