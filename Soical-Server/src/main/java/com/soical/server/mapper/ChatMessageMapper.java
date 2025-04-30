package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天消息Mapper接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 分页查询会话消息
     *
     * @param page           分页对象
     * @param conversationId 会话ID
     * @return 消息列表
     */
    Page<ChatMessage> selectMessagesByConversation(Page<ChatMessage> page, @Param("conversationId") Long conversationId);
    
    /**
     * 统计未读消息数
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 未读消息数
     */
    Integer countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    /**
     * 将消息标记为已读
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 影响行数
     */
    int markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    /**
     * 获取会话中的消息列表（分页）
     * @param conversationId 会话ID
     * @param limit 限制条数
     * @param offset 偏移量
     * @return 消息列表
     */
    @Select("SELECT * FROM t_chat_message WHERE conversation_id = #{conversationId} " +
            "ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<ChatMessage> getMessagesByConversationId(@Param("conversationId") Long conversationId,
                                               @Param("limit") int limit,
                                               @Param("offset") int offset);
    
    /**
     * 获取两个用户之间的消息
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param limit 限制条数
     * @param offset 偏移量
     * @return 消息列表
     */
    @Select("SELECT * FROM t_chat_message WHERE " +
            "((sender_id = #{senderId} AND receiver_id = #{receiverId}) OR " +
            "(sender_id = #{receiverId} AND receiver_id = #{senderId})) " +
            "ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<ChatMessage> getMessagesBetweenUsers(@Param("senderId") Long senderId,
                                           @Param("receiverId") Long receiverId,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);
    
    /**
     * 将消息标记为已读
     * @param messageId 消息ID
     * @return 影响的行数
     */
    @Update("UPDATE t_chat_message SET is_read = true WHERE message_id = #{messageId}")
    int markAsRead(@Param("messageId") Long messageId);
    
    /**
     * 将会话中发送给指定用户的所有未读消息标记为已读
     * @param conversationId 会话ID
     * @param receiverId 接收者ID
     * @return 影响的行数
     */
    @Update("UPDATE t_chat_message SET is_read = true WHERE " +
            "conversation_id = #{conversationId} AND receiver_id = #{receiverId} AND is_read = false")
    int markConversationMessagesAsRead(@Param("conversationId") Long conversationId,
                                     @Param("receiverId") Long receiverId);
} 