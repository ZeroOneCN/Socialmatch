package com.soical.server.dto;

import lombok.Data;

/**
 * WebSocket消息DTO
 */
@Data
public class WebSocketMessageDTO {

    /**
     * 消息类型
     * 1: 连接成功
     * 2: 聊天消息
     * 3: 已读通知
     * 4: 错误消息
     */
    private Integer type;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 会话ID
     */
    private Long conversationId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：1-文本，2-图片
     */
    private Integer contentType;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 扩展数据（JSON字符串）
     */
    private String extraData;
} 