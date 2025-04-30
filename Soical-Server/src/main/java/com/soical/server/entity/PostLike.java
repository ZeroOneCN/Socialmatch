package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 动态点赞实体类
 */
@Data
@TableName("t_post_like")
public class PostLike {
    
    /**
     * 点赞ID
     */
    @TableId(value = "like_id", type = IdType.AUTO)
    private Long likeId;
    
    /**
     * 动态ID
     */
    private Long postId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 