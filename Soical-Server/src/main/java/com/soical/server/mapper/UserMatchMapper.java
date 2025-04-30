package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.entity.UserMatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户匹配Mapper接口
 */
@Mapper
public interface UserMatchMapper extends BaseMapper<UserMatch> {

    /**
     * 查询用户的匹配列表
     * 
     * @param userId 用户ID
     * @param status 状态筛选条件（可选）
     * @return 匹配列表
     */
    List<UserMatch> selectUserMatches(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 查询两个用户之间的匹配关系
     *
     * @param userAId 用户A ID
     * @param userBId 用户B ID
     * @return 匹配关系
     */
    UserMatch selectMatchBetweenUsers(@Param("userAId") Long userAId, 
                                  @Param("userBId") Long userBId);
    
    /**
     * 统计用户的匹配数量
     * 
     * @param userId 用户ID
     * @param status 状态筛选条件（可选）
     * @return 匹配数量
     */
    Integer countUserMatches(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 更新匹配状态
     *
     * @param matchId 匹配ID
     * @param status 新状态
     * @return 影响行数
     */
    int updateMatchStatus(@Param("matchId") Long matchId, @Param("status") Integer status);
    
    /**
     * 获取用户的推荐列表，根据条件筛选可能匹配的用户
     *
     * @param userId 当前用户ID
     * @param gender 性别筛选
     * @param ageMin 最小年龄
     * @param ageMax 最大年龄
     * @param location 位置筛选
     * @param interestsList 兴趣标签列表
     * @param limit 返回数量限制
     * @return 推荐用户ID列表
     */
    List<Long> selectRecommendedUserIds(
            @Param("userId") Long userId,
            @Param("gender") Integer gender,
            @Param("ageMin") Integer ageMin,
            @Param("ageMax") Integer ageMax,
            @Param("location") String location,
            @Param("interestsList") List<String> interestsList,
            @Param("limit") Integer limit);

    /**
     * 统计成功匹配的数量（状态为1）
     */
    @Select("SELECT COUNT(*) FROM t_user_match WHERE status = 1")
    Long countSuccessMatches();
    
    /**
     * 统计待确认的匹配数量（状态为0）
     */
    @Select("SELECT COUNT(*) FROM t_user_match WHERE status = 0")
    Long countPendingMatches();
    
    /**
     * 统计今日新增匹配数量
     */
    @Select("SELECT COUNT(*) FROM t_user_match WHERE DATE(create_time) = CURDATE()")
    Long countTodayMatches();
    
    /**
     * 统计昨日新增匹配数量
     */
    @Select("SELECT COUNT(*) FROM t_user_match WHERE DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long countYesterdayMatches();
    
    /**
     * 统计上周总匹配数量
     */
    @Select("SELECT COUNT(*) FROM t_user_match WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1) - 1")
    Long countLastWeekTotalMatches();
    
    /**
     * 统计上周成功匹配数量
     */
    @Select("SELECT COUNT(*) FROM t_user_match WHERE status = 1 AND YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1) - 1")
    Long countLastWeekSuccessMatches();
    
    /**
     * 统计本周总匹配数量
     */
    @Select("SELECT COUNT(*) FROM t_user_match WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1)")
    Long countThisWeekTotalMatches();
    
    /**
     * 统计本周成功匹配数量
     */
    @Select("SELECT COUNT(*) FROM t_user_match WHERE status = 1 AND YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1)")
    Long countThisWeekSuccessMatches();

    /**
     * 查询热门用户（被喜欢次数最多的用户）
     *
     * @param startDate 开始日期
     * @return 热门用户列表，包含用户ID和被喜欢次数
     */
    @Select("SELECT user_b_id as user_id, COUNT(*) as like_count " +
            "FROM t_user_match " +
            "WHERE create_time >= #{startDate} " +
            "AND status IN (0, 1) " + // 包含待确认和已匹配状态
            "GROUP BY user_b_id " +
            "ORDER BY like_count DESC " +
            "LIMIT 100")
    List<Map<String, Object>> selectPopularUsers(@Param("startDate") Date startDate);
} 