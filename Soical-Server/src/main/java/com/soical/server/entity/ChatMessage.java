package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 聊天消息实体
 */
@Data
@TableName("t_chat_message")
public class ChatMessage {
    
    /**
     * 消息ID
     */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;
    
    /**
     * 会话ID
     */
    @TableField("conversation_id")
    private Long conversationId;
    
    /**
     * 发送者ID
     */
    @TableField("sender_id")
    private Long senderId;
    
    /**
     * 接收者ID
     */
    @TableField("receiver_id")
    private Long receiverId;
    
    /**
     * 消息内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 消息类型：1-文本，2-图片
     */
    @TableField("message_type")
    private Integer messageType;
    
    /**
     * 状态：0-未读，1-已读
     */
    @TableField("is_read")
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 发送者名称（非数据库字段）
     */
    @TableField(exist = false)
    private String senderName;
    
    /**
     * 发送者头像（非数据库字段）
     */
    @TableField(exist = false)
    private String senderAvatar;
} 