package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 查询没有资料的用户
     * 
     * @return 用户列表
     */
    @Select("SELECT u.* FROM t_user u LEFT JOIN t_user_profile p ON u.user_id = p.user_id WHERE p.profile_id IS NULL")
    List<User> findUsersWithoutProfile();

    /**
     * 通过用户名查询用户
     */
    @Select("SELECT * FROM t_user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    /**
     * 通过手机号查询用户
     */
    @Select("SELECT * FROM t_user WHERE phone = #{phone}")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 获取今日新增用户数
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE DATE(create_time) = CURDATE()")
    Long getTodayNewUsersCount();

    /**
     * 获取昨日新增用户数
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long getYesterdayNewUsersCount();

    /**
     * 获取本周新增用户数
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1)")
    Long getWeekNewUsersCount();

    /**
     * 获取上周新增用户数
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1) - 1")
    Long getLastWeekNewUsersCount();

    /**
     * 获取本月新增用户数
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE DATE_FORMAT(create_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')")
    Long getMonthNewUsersCount();

    /**
     * 获取上月新增用户数
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE DATE_FORMAT(create_time, '%Y%m') = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m')")
    Long getLastMonthNewUsersCount();

    /**
     * 获取活跃用户数（状态为正常的用户）
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE status = 1")
    Long getActiveUsersCount();

    /**
     * 按性别统计用户数
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE gender = #{gender}")
    Long countUsersByGender(@Param("gender") Integer gender);

    /**
     * 获取用户增长率
     */
    @Select("SELECT IFNULL(ROUND(((SELECT COUNT(*) FROM t_user WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1)) / " +
            "NULLIF((SELECT COUNT(*) FROM t_user WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1) - 1), 0) - 1) * 100, 1), 0) AS growth_rate")
    Double getUserGrowthRate();
} 