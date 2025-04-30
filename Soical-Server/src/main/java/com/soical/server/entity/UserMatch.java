package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户匹配实体类
 */
@Data
@TableName("t_user_match")
public class UserMatch {
    
    /**
     * 匹配ID
     */
    @TableId(value = "match_id", type = IdType.AUTO)
    private Long matchId;
    
    /**
     * 用户A ID
     */
    @TableField("user_a_id")
    private Long userAId;
    
    /**
     * 用户B ID
     */
    @TableField("user_b_id")
    private Long userBId;
    
    /**
     * 状态：0-待确认，1-已匹配，2-已解除
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
} 