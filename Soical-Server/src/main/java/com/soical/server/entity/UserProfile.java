package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户资料实体类
 */
@Data
@TableName("t_user_profile")
public class UserProfile {

    /**
     * 资料ID
     */
    @TableId(value = "profile_id", type = IdType.AUTO)
    private Long profileId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 学历：0-未知，1-大专，2-本科，3-硕士，4-博士
     */
    @TableField("education")
    private Integer education;

    /**
     * 职业
     */
    @TableField("occupation")
    private String occupation;
    
    /**
     * 职业代码
     */
    @TableField("occupation_code")
    private String occupationCode;

    /**
     * 所在地
     */
    @TableField("location")
    private String location;

    /**
     * 城市
     */
    @TableField("city")
    private String city;
    
    /**
     * 城市代码
     */
    @TableField("city_code")
    private String cityCode;
    
    /**
     * 省份代码
     */
    @TableField("province_code")
    private String provinceCode;
    
    /**
     * 性别：0-未知，1-男，2-女，3-保密
     */
    @TableField("gender")
    private Integer gender;
    
    /**
     * 生日
     */
    @TableField("birthday")
    private String birthday;

    /**
     * 自我介绍
     */
    @TableField("self_intro")
    private String selfIntro;

    /**
     * 兴趣爱好
     */
    @TableField("hobbies")
    private String hobbies;

    /**
     * 个人相册（JSON格式）
     */
    @TableField("photos")
    private String photos;

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