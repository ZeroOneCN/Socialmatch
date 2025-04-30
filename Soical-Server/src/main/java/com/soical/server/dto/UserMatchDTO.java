package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 用户匹配DTO
 */
@Data
@ApiModel("用户匹配")
public class UserMatchDTO {

    @ApiModelProperty("匹配ID")
    private Long matchId;
    
    @ApiModelProperty("匹配用户ID")
    private Long userId;
    
    @ApiModelProperty("匹配用户名")
    private String username;
    
    @ApiModelProperty("匹配用户昵称")
    private String nickname;
    
    @ApiModelProperty("匹配用户头像")
    private String avatar;
    
    @ApiModelProperty("匹配用户性别")
    private Integer gender;
    
    @ApiModelProperty("匹配用户年龄")
    private Integer age;
    
    @ApiModelProperty("匹配用户职业")
    private String occupation;
    
    @ApiModelProperty("匹配用户位置")
    private String location;
    
    @ApiModelProperty("匹配用户自我介绍")
    private String selfIntro;
    
    @ApiModelProperty("匹配状态：0-待确认，1-已匹配，2-已解除，3-拒绝")
    private Integer status;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
    
    @ApiModelProperty("是否有未读消息")
    private Boolean hasUnreadMessage;
    
    @ApiModelProperty("匹配时间")
    private Date matchTime;
    
    @ApiModelProperty("相似度（百分比）")
    private Integer similarity;
} 