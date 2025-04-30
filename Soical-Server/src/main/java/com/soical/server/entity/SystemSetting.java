package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统设置实体类
 */
@Data
@TableName("t_system_settings")
@ApiModel(description = "系统设置")
public class SystemSetting {
    
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("设置组")
    @TableField(value = "`group`")
    private String group;
    
    @ApiModelProperty("设置键")
    private String settingKey;
    
    @ApiModelProperty("设置值")
    private String settingValue;
    
    @ApiModelProperty("设置描述")
    private String description;
    
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
} 