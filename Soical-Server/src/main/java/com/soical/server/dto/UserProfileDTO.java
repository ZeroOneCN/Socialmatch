package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户资料DTO
 */
@Data
@ApiModel("用户资料")
public class UserProfileDTO {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("昵称")
    private String nickname;
    
    @ApiModelProperty("学历：0-未知，1-大专，2-本科，3-硕士，4-博士")
    private Integer education;
    
    @ApiModelProperty("职业")
    private String occupation;
    
    @ApiModelProperty("职业代码")
    private String occupationCode;
    
    @ApiModelProperty("所在地")
    private String location;
    
    @ApiModelProperty("城市")
    private String city;
    
    @ApiModelProperty("城市代码")
    private String cityCode;
    
    @ApiModelProperty("省份代码")
    private String provinceCode;
    
    @ApiModelProperty("性别：0-未知，1-男，2-女，3-保密")
    private Integer gender;
    
    @ApiModelProperty("生日")
    private String birthday;
    
    @ApiModelProperty("自我介绍")
    private String selfIntro;
    
    @ApiModelProperty("兴趣爱好")
    private String hobbies;
    
    @ApiModelProperty("头像URL")
    private String avatar;
    
    @ApiModelProperty("粉丝数量")
    private Integer followerCount;
    
    @ApiModelProperty("关注数量")
    private Integer followingCount;
    
    @ApiModelProperty("当前用户是否已关注此用户")
    private Boolean isFollowing;
} 