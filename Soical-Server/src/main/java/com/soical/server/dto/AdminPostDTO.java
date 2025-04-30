package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 管理员查看动态DTO
 */
@Data
@ApiModel("管理员查看动态信息")
public class AdminPostDTO {
    
    @ApiModelProperty("动态ID")
    private Long postId;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("用户昵称")
    private String nickname;
    
    @ApiModelProperty("内容")
    private String content;
    
    @ApiModelProperty("图片列表")
    private List<String> images;
    
    @ApiModelProperty("点赞数")
    private Integer likeCount;
    
    @ApiModelProperty("评论数")
    private Integer commentCount;
    
    @ApiModelProperty("分享数")
    private Integer shareCount;
    
    @ApiModelProperty("是否为分享的动态")
    private Boolean isShared;
    
    @ApiModelProperty("原始动态ID")
    private Long originalPostId;
    
    @ApiModelProperty("状态：0-删除，1-正常，2-违规")
    private Integer status;
    
    @ApiModelProperty("状态文本")
    private String statusText;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
} 