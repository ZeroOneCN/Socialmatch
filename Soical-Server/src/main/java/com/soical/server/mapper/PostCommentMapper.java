package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {
    
    /**
     * 获取今日新增评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE DATE(create_time) = CURDATE()")
    Long getTodayNewCommentsCount();
    
    /**
     * 获取昨日新增评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long getYesterdayNewCommentsCount();
    
    /**
     * 获取本周新增评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1)")
    Long getThisWeekCommentsCount();
    
    /**
     * 获取上周新增评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1) - 1")
    Long getLastWeekCommentsCount();
    
    /**
     * 获取本月新增评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE DATE_FORMAT(create_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')")
    Long getMonthNewCommentsCount();
    
    /**
     * 获取待审核评论数量
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE status = 0")
    Long getPendingReviewCommentsCount();
    
    /**
     * 获取已删除评论数量
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE status = 2")
    Long getDeletedCommentsCount();
    
    /**
     * 获取正常评论数量
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE status = 1")
    Long getNormalCommentsCount();
} 