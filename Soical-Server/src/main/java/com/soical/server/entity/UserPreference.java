package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户同频偏好设置实体类
 */
@Data
@TableName("t_user_preference")
public class UserPreference {

    /**
     * 偏好设置ID
     */
    @TableId(value = "preference_id", type = IdType.AUTO)
    private Long preferenceId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 性别偏好(0-不限，1-男，2-女)
     */
    @TableField("gender_preference")
    private Integer genderPreference;

    /**
     * 兴趣偏好，多个以逗号分隔
     */
    @TableField("interests")
    private String interests;

    /**
     * 意向城市
     */
    @TableField("city")
    private String city;

    /**
     * 城市编码
     */
    @TableField("city_code")
    private String cityCode;

    /**
     * 是否仅查看附近
     */
    @TableField("nearby_only")
    private Boolean nearbyOnly;

    /**
     * 最大匹配距离(公里)
     */
    @TableField("max_distance")
    private Integer maxDistance;

    /**
     * 最小年龄
     */
    @TableField("min_age")
    private Integer minAge;

    /**
     * 最大年龄
     */
    @TableField("max_age")
    private Integer maxAge;

    /**
     * 教育程度要求(0-不限，1-高中及以上，2-大专及以上，3-本科及以上，4-硕士及以上，5-博士及以上)
     */
    @TableField("education")
    private Integer education;

    /**
     * 职业意向
     */
    @TableField("occupation")
    private String occupation;

    /**
     * 职业编码
     */
    @TableField("occupation_code")
    private String occupationCode;

    /**
     * 是否仅显示已认证用户
     */
    @TableField("verified_only")
    private Boolean verifiedOnly;

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

    public Integer getGenderPreference() {
        return genderPreference;
    }

    public void setGenderPreference(Integer genderPreference) {
        this.genderPreference = genderPreference;
    }

    public Integer getAgeMin() {
        return minAge;
    }

    public void setAgeMin(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getAgeMax() {
        return maxAge;
    }

    public void setAgeMax(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getLocationPreference() {
        return city;
    }

    public void setLocationPreference(String city) {
        this.city = city;
    }
} 