package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 管理员DTO
 */
@Data
@ApiModel("管理员信息")
public class AdminDTO {
    
    @ApiModelProperty("管理员ID")
    private Long adminId;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("昵称")
    private String nickname;
    
    @ApiModelProperty("头像")
    private String avatar;
    
    @ApiModelProperty("状态：0-禁用，1-正常")
    private Integer status;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
} 