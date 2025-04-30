package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态数据传输对象
 */
@Data
@ApiModel("动态信息")
public class PostDTO {
    
    @ApiModelProperty("动态ID")
    private Long postId;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("用户昵称")
    private String nickname;
    
    @ApiModelProperty("用户头像")
    private String avatar;
    
    @ApiModelProperty(value = "内容", required = true)
    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容长度不能超过1000字符")
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
    
    @ApiModelProperty("原始动态ID（如果是分享的动态）")
    private Long originalPostId;
    
    @ApiModelProperty("原始动态信息（如果是分享的动态）")
    private PostDTO originalPost;
    
    @ApiModelProperty("状态：0-审核中，1-审核通过，2-审核不通过")
    private Integer status;
    
    @ApiModelProperty("审核原因（如果审核不通过）")
    private String reviewReason;
    
    @ApiModelProperty("审核时间")
    private LocalDateTime reviewTime;
    
    @ApiModelProperty("城市")
    private String city;
    
    @ApiModelProperty("动态类型：0-普通，1-图文，2-视频，3-链接")
    private Integer postType;
    
    @ApiModelProperty("热度评分")
    private Double hotScore;
    
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
    
    @ApiModelProperty("当前用户是否已点赞")
    private Boolean liked;
} 