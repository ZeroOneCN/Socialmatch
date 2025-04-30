package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 评论数据传输对象
 */
@Data
@ApiModel("评论信息")
public class CommentDTO {

    @ApiModelProperty("评论ID")
    private Long commentId;
    
    @ApiModelProperty("动态ID")
    private Long postId;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty(value = "评论内容", required = true)
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容长度不能超过500字符")
    private String content;
    
    @ApiModelProperty("用户昵称")
    private String nickname;
    
    @ApiModelProperty("用户头像")
    private String avatar;
    
    @ApiModelProperty("父评论ID")
    private Long parentId;
    
    @ApiModelProperty("被回复用户ID")
    private Long replyUserId;
    
    @ApiModelProperty("被回复用户昵称")
    private String replyNickname;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
} 