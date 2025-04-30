package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 城市实体类
 */
@Data
@TableName("t_city")
public class City {
    
    /**
     * 城市ID
     */
    @TableId(value = "city_id", type = IdType.AUTO)
    private Integer cityId;
    
    /**
     * 省份名称
     */
    private String provinceName;
    
    /**
     * 城市名称
     */
    private String cityName;
    
    /**
     * 城市编码
     */
    private String cityCode;
    
    /**
     * 省份编码
     */
    private String provinceCode;
    
    /**
     * 状态：0-禁用，1-正常
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