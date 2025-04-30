package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建会话DTO
 */
@Data
@ApiModel(description = "创建会话的数据传输对象")
public class CreateConversationDTO {
    
    @ApiModelProperty(value = "目标用户ID", required = true)
    private Long targetUserId;
    
} 