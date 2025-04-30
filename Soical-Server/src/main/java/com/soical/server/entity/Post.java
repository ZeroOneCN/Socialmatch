package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 动态实体类
 */
@Data
@TableName("t_post")
public class Post {

    /**
     * 动态ID
     */
    @TableId(type = IdType.AUTO)
    private Long postId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 内容
     */
    private String content;

    /**
     * 图片列表（JSON格式）
     */
    private String images;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 是否为分享的动态
     */
    private Boolean isShared;

    /**
     * 原始动态ID（如果是分享的动态）
     */
    private Long originalPostId;

    /**
     * 发布城市
     */
    private String city;

    /**
     * 动态类型：0-普通，1-图文，2-视频，3-链接
     */
    private Integer postType;

    /**
     * 热度评分
     */
    private Double hotScore;

    /**
     * 状态：0-删除，1-正常，2-违规
     */
    private Integer status;

    /**
     * 审核原因
     */
    private String reviewReason;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 