package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户详情数据传输对象
 */
@Data
@ApiModel("用户详情")
public class UserDetailDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("手机号")
    private String phone;
    
    @ApiModelProperty("头像URL")
    private String avatar;
    
    @ApiModelProperty("性别：0-未知，1-男，2-女")
    private Integer gender;
    
    @ApiModelProperty("生日")
    private Date birthday;
    
    @ApiModelProperty("状态：0-禁用，1-正常")
    private Integer status;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
    
    @ApiModelProperty("更新时间")
    private Date updateTime;
    
    @ApiModelProperty("用户资料")
    private UserProfileDTO profile;
    
    @ApiModelProperty("统计信息")
    private UserStatsDTO stats;
    
    /**
     * 用户资料数据传输对象
     */
    @Data
    public static class UserProfileDTO implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty("资料ID")
        private Long profileId;
        
        @ApiModelProperty("用户ID")
        private Long userId;
        
        @ApiModelProperty("昵称")
        private String nickname;
        
        @ApiModelProperty("学历：0-未知，1-大专，2-本科，3-硕士，4-博士")
        private Integer education;
        
        @ApiModelProperty("职业")
        private String occupation;
        
        @ApiModelProperty("所在地")
        private String location;
        
        @ApiModelProperty("自我介绍")
        private String selfIntro;
        
        @ApiModelProperty("兴趣爱好")
        private String hobbies;
        
        @ApiModelProperty("个人相册")
        private Object photos;
        
        @ApiModelProperty("头像URL")
        private String avatar;
        
        @ApiModelProperty("所在城市")
        private String city;
        
        @ApiModelProperty("创建时间")
        private Date createTime;
        
        @ApiModelProperty("更新时间")
        private Date updateTime;
    }
    
    /**
     * 用户统计数据传输对象
     */
    @Data
    public static class UserStatsDTO implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty("动态数量")
        private Integer postCount = 0;
        
        @ApiModelProperty("评论数量")
        private Integer commentCount = 0;
        
        @ApiModelProperty("关注数量")
        private Integer followingCount = 0;
        
        @ApiModelProperty("粉丝数量")
        private Integer followerCount = 0;
        
        @ApiModelProperty("匹配数量")
        private Integer matchCount = 0;
    }
} 