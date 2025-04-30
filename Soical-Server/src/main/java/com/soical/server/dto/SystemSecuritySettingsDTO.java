package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 系统安全设置DTO
 */
@Data
@ApiModel(description = "系统安全设置")
public class SystemSecuritySettingsDTO {
    
    @ApiModelProperty("密码最小长度")
    private Integer passwordMinLength;
    
    @ApiModelProperty("密码复杂度要求")
    private List<String> passwordStrength;
    
    @ApiModelProperty("密码过期时间（天）")
    private Integer passwordExpireDays;
    
    @ApiModelProperty("最大登录失败次数")
    private Integer maxLoginAttempts;
    
    @ApiModelProperty("账户锁定时间（分钟）")
    private Integer accountLockTime;
    
    @ApiModelProperty("是否启用验证码")
    private Boolean enableCaptcha;
    
    @ApiModelProperty("会话超时时间（分钟）")
    private Integer sessionTimeout;
} 