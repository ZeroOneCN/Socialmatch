package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评论数据访问接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 查询带用户信息的评论列表
     *
     * @param page   分页对象
     * @param postId 动态ID
     * @return 分页结果
     */
    Page<Comment> selectCommentsWithUser(Page<Comment> page, @Param("postId") Long postId);
    
    /**
     * 获取昨日新增评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long getYesterdayNewCommentsCount();
    
    /**
     * 获取今日新增评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE DATE(create_time) = CURDATE()")
    Long getTodayNewCommentsCount();
    
    /**
     * 获取本周新增评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1)")
    Long getWeekNewCommentsCount();
    
    /**
     * 获取上周新增评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE YEARWEEK(create_time, 1) = YEARWEEK(DATE_SUB(CURDATE(), INTERVAL 1 WEEK), 1)")
    Long getLastWeekNewCommentsCount();
    
    /**
     * 获取本月新增评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE DATE_FORMAT(create_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')")
    Long getMonthNewCommentsCount();
    
    /**
     * 获取待审核评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE status = 0")
    Long getPendingReviewCommentsCount();
    
    /**
     * 获取已删除评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE status = 2")
    Long getDeletedCommentsCount();
    
    /**
     * 获取正常评论数
     */
    @Select("SELECT COUNT(*) FROM t_comment WHERE status = 1")
    Long getNormalCommentsCount();
} 