package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 聊天会话DTO
 */
@Data
@ApiModel("聊天会话")
public class ConversationDTO {
    
    @ApiModelProperty("会话ID")
    private Long conversationId;
    
    @ApiModelProperty("对方用户ID")
    private Long targetUserId;
    
    @ApiModelProperty("对方用户昵称")
    private String targetNickname;
    
    @ApiModelProperty("对方用户头像")
    private String targetAvatar;
    
    @ApiModelProperty("最后一条消息")
    private String lastMessage;
    
    @ApiModelProperty("最后消息时间")
    private Date lastMessageTime;
    
    @ApiModelProperty("未读消息数")
    private Integer unreadCount;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
} 