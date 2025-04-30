package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * AI聊天响应DTO
 */
@Data
@ApiModel("AI聊天响应")
public class AiChatResponseDTO {

    @ApiModelProperty("消息ID")
    private Long chatId;

    @ApiModelProperty("AI回复内容")
    private String message;
    
    @ApiModelProperty("角色（用户/AI）")
    private String role;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
    
    @ApiModelProperty("消耗的token数")
    private Integer tokens;
} 