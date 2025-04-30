package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("用户登录响应")
public class UserLoginResponseDTO {
    
    @ApiModelProperty("JWT Token")
    private String token;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("昵称")
    private String nickname;
    
    @ApiModelProperty("头像")
    private String avatar;
} 