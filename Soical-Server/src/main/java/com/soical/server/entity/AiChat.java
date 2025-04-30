package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI助手会话记录实体类
 */
@Data
@TableName("t_ai_chat")
public class AiChat {

    /**
     * 会话ID
     */
    @TableId(value = "chat_id", type = IdType.AUTO)
    private Long chatId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 消息内容
     */
    @TableField("message")
    private String message;

    /**
     * 角色：user-用户, assistant-AI助手
     */
    @TableField("role")
    private String role;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 消息的token数量
     */
    @TableField("tokens")
    private Integer tokens;
} 