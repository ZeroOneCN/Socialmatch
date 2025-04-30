package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 会话Mapper接口
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    
    /**
     * 根据两个用户ID查询会话
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 会话对象
     */
    @Select("SELECT * FROM t_conversation WHERE " +
            "(user_a_id = #{userId1} AND user_b_id = #{userId2}) OR " +
            "(user_a_id = #{userId2} AND user_b_id = #{userId1})")
    Conversation findByUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * 重置未读消息计数
     * @param conversationId 会话ID
     * @return 影响的行数
     */
    @Update("UPDATE t_conversation SET unread_count = 0 WHERE conversation_id = #{conversationId}")
    int resetUnreadCount(@Param("conversationId") Long conversationId);
    
    /**
     * 获取用户的会话列表，包含目标用户的昵称和头像
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Conversation> getConversationsWithUserInfo(@Param("userId") Long userId);
} 