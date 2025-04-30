package com.soical.server.websocket;

import lombok.Data;

/**
 * WebSocket聊天消息DTO
 */
@Data
public class ChatMessageDTO {
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：1-文本，2-图片
     */
    private Integer messageType;
} 