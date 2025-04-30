package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户关注实体类
 */
@Data
@TableName("t_user_follow")
public class UserFollow {
    
    /**
     * 关注ID
     */
    @TableId(value = "follow_id", type = IdType.AUTO)
    private Long followId;
    
    /**
     * 关注者ID（粉丝）
     */
    @TableField("follower_id")
    private Long followerId;
    
    /**
     * 被关注者ID
     */
    @TableField("followed_id")
    private Long followedId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
    
    /**
     * 状态：0-已取消，1-已关注
     */
    @TableField("status")
    private Integer status;
} 