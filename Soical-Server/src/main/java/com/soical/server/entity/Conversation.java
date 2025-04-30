package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 会话实体
 */
@Data
@TableName("t_conversation")
public class Conversation {
    
    /**
     * 会话ID
     */
    @TableId(value = "conversation_id", type = IdType.AUTO)
    private Long conversationId;
    
    /**
     * 用户A ID
     */
    @TableField("user_a_id")
    private Long userAId;
    
    /**
     * 用户B ID
     */
    @TableField("user_b_id")
    private Long userBId;
    
    /**
     * 最后一条消息
     */
    @TableField("last_message")
    private String lastMessage;
    
    /**
     * 最后消息时间
     */
    @TableField("last_message_time")
    private Date lastMessageTime;
    
    /**
     * 未读消息数量
     */
    @TableField("unread_count")
    private Integer unreadCount;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
    
    // 以下是非数据库字段，用于前端展示
    
    /**
     * 目标用户昵称（非数据库字段）
     */
    @TableField(exist = false)
    private String targetUserNickname;
    
    /**
     * 目标用户头像（非数据库字段）
     */
    @TableField(exist = false)
    private String targetUserAvatar;
    
    /**
     * 目标用户ID（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private Long targetUserId;
} 