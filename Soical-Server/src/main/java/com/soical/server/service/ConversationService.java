package com.soical.server.service;

import com.soical.server.entity.Conversation;

import java.util.List;

/**
 * 会话服务接口
 */
public interface ConversationService {
    
    /**
     * 查询用户的会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Conversation> getConversationsByUserId(Long userId);
    
    /**
     * 根据会话ID获取会话
     * @param conversationId 会话ID
     * @return 会话对象
     */
    Conversation getById(Long conversationId);
    
    /**
     * 根据两个用户ID查找会话
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 会话对象，如果不存在则返回null
     */
    Conversation findByUsers(Long userId1, Long userId2);
    
    /**
     * 创建新会话
     * @param conversation 会话对象
     * @return 创建后的会话ID
     */
    Long createConversation(Conversation conversation);
    
    /**
     * 创建两个用户之间的会话
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 创建的会话对象
     */
    Conversation createConversation(Long userId1, Long userId2);
    
    /**
     * 获取或创建两个用户之间的会话
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 会话对象
     */
    Conversation getConversationBetweenUsers(Long userId1, Long userId2);
    
    /**
     * 更新会话信息
     * @param conversation 会话对象
     * @return 是否更新成功
     */
    boolean updateConversation(Conversation conversation);
    
    /**
     * 标记会话为已读
     * @param conversationId 会话ID
     * @param userId 当前用户ID
     * @return 是否标记成功
     */
    boolean markAsRead(Long conversationId, Long userId);
    
    /**
     * 删除会话
     * @param conversationId 会话ID
     * @return 是否删除成功
     */
    boolean deleteConversation(Long conversationId);
} 