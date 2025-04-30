package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 职业实体类
 */
@Data
@TableName("t_occupation")
public class Occupation {
    
    /**
     * 职业ID
     */
    @TableId(value = "occupation_id", type = IdType.AUTO)
    private Integer occupationId;
    
    /**
     * 职业名称
     */
    private String name;
    
    /**
     * 职业编码
     */
    private String code;
    
    /**
     * 职业类别
     */
    private String category;
    
    /**
     * Dr状态：0-禁用，1-正常
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 