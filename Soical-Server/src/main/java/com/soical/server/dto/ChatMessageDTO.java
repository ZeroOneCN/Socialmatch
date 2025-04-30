package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 聊天消息DTO
 */
@Data
@ApiModel("聊天消息")
public class ChatMessageDTO {
    
    @ApiModelProperty("消息ID")
    private Long messageId;
    
    @ApiModelProperty("会话ID")
    private Long conversationId;
    
    @ApiModelProperty("发送者ID")
    private Long senderId;
    
    @ApiModelProperty("发送者昵称")
    private String senderNickname;
    
    @ApiModelProperty("发送者头像")
    private String senderAvatar;
    
    @ApiModelProperty("消息内容")
    private String content;
    
    @ApiModelProperty("消息类型：1-文本，2-图片")
    private Integer type;
    
    @ApiModelProperty("状态：0-未读，1-已读")
    private Integer status;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
} 