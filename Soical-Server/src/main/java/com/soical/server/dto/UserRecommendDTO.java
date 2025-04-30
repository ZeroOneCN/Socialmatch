package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 用户推荐DTO
 */
@Data
@ApiModel("用户推荐")
public class UserRecommendDTO {

    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("昵称")
    private String nickname;
    
    @ApiModelProperty("头像")
    private String avatar;
    
    @ApiModelProperty("性别")
    private Integer gender;
    
    @ApiModelProperty("年龄")
    private Integer age;
    
    @ApiModelProperty("职业")
    private String occupation;
    
    @ApiModelProperty("位置")
    private String location;
    
    @ApiModelProperty("自我介绍")
    private String selfIntro;
    
    @ApiModelProperty("个人相册")
    private List<String> photos;
    
    @ApiModelProperty("兴趣爱好")
    private String hobbies;
    
    @ApiModelProperty("相似度（百分比）")
    private Integer similarity;
    
    @ApiModelProperty("是否已喜欢")
    private Boolean isLiked;
    
    @ApiModelProperty("匹配状态：0-待确认，1-已匹配，2-已解除，3-拒绝")
    private Integer matchStatus;
    
    @ApiModelProperty("匹配ID")
    private Long matchId;

    @ApiModelProperty("推荐类型：collaborative-协同过滤，content-基于内容，popular-热门推荐，basic-基础推荐")
    private String recommendType;
} 