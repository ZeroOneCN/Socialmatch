package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * AI聊天请求DTO
 */
@Data
@ApiModel("AI聊天请求")
public class AiChatRequestDTO {

    @ApiModelProperty("用户消息内容")
    private String message;
    
    @ApiModelProperty("是否新会话")
    private Boolean newSession;
} 