package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 系统设置实体类
 */
@Data
@TableName("t_system_settings")
public class SystemSettings {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设置组（如security, basic, notification等）
     */
    @TableField(value = "`group`")
    private String group;

    /**
     * 设置键
     */
    @TableField("setting_key")
    private String settingKey;

    /**
     * 设置值（可能是字符串、数字、JSON等）
     */
    @TableField("setting_value")
    private String settingValue;

    /**
     * 设置描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
} 