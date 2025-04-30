package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 动态数据访问接口
 */
@Repository
public interface PostMapper extends BaseMapper<Post> {
    
    /**
     * 查询动态列表（带用户信息）
     *
     * @param page 分页对象
     * @return 分页结果
     */
    Page<Post> selectPostsWithUser(Page<Post> page);
    
    /**
     * 查询用户的动态列表
     *
     * @param page   分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<Post> selectUserPosts(Page<Post> page, @Param("userId") Long userId);
    
    /**
     * 增加点赞数
     */
    @Update("UPDATE t_post SET like_count = like_count + 1 WHERE post_id = #{postId}")
    int incrementLikeCount(@Param("postId") Long postId);
    
    /**
     * 减少点赞数
     */
    @Update("UPDATE t_post SET like_count = GREATEST(like_count - 1, 0) WHERE post_id = #{postId}")
    int decrementLikeCount(@Param("postId") Long postId);
    
    /**
     * 增加评论数
     */
    @Update("UPDATE t_post SET comment_count = comment_count + 1 WHERE post_id = #{postId}")
    int incrementCommentCount(@Param("postId") Long postId);
    
    /**
     * 减少评论数
     */
    @Update("UPDATE t_post SET comment_count = GREATEST(comment_count - 1, 0) WHERE post_id = #{postId}")
    int decrementCommentCount(@Param("postId") Long postId);
    
    /**
     * 增加分享数
     */
    @Update("UPDATE t_post SET share_count = share_count + 1 WHERE post_id = #{postId}")
    int incrementShareCount(@Param("postId") Long postId);
    
    /**
     * 更新热度评分
     */
    @Update("UPDATE t_post SET hot_score = #{hotScore} WHERE post_id = #{postId}")
    int updateHotScore(@Param("postId") Long postId, @Param("hotScore") Double hotScore);
    
    /**
     * 查询关注用户的动态列表
     *
     * @param page          分页对象
     * @param followingIds  关注的用户ID列表
     * @return 分页结果
     */
    Page<Post> selectFollowedPosts(Page<Post> page, @Param("followingIds") List<Long> followingIds);
    
    /**
     * 查询推荐动态列表（按热度排序）
     *
     * @param page 分页对象
     * @return 分页结果
     */
    Page<Post> selectRecommendedPosts(Page<Post> page);
    
    /**
     * 查询同城动态列表
     *
     * @param page 分页对象
     * @param city 城市名称
     * @return 分页结果
     */
    Page<Post> selectCityPosts(Page<Post> page, @Param("city") String city);
    
    /**
     * 获取今天新增帖子数
     */
    @Select("SELECT COUNT(*) FROM t_post WHERE DATE(create_time) = CURDATE()")
    Long getTodayNewPostsCount();
    
    /**
     * 获取本周新增帖子数
     */
    @Select("SELECT COUNT(*) FROM t_post WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1)")
    Long getThisWeekPostsCount();
    
    /**
     * 获取上周新增帖子数
     */
    @Select("SELECT COUNT(*) FROM t_post WHERE YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1) - 1")
    Long getLastWeekPostsCount();
    
    /**
     * 获取待审核帖子数量
     */
    @Select("SELECT COUNT(*) FROM t_post WHERE status = 0")
    Long getPendingReviewPostsCount();
    
    /**
     * 获取已删除帖子数量
     */
    @Select("SELECT COUNT(*) FROM t_post WHERE status = 2")
    Long getDeletedPostsCount();
    
    /**
     * 获取正常帖子数量
     */
    @Select("SELECT COUNT(*) FROM t_post WHERE status = 1")
    Long getNormalPostsCount();

    /**
     * 获取昨日新增帖子数
     */
    @Select("SELECT COUNT(*) FROM t_post WHERE TO_DAYS(NOW()) - TO_DAYS(create_time) = 1")
    Long getYesterdayNewPostsCount();

    /**
     * 获取指定状态的帖子数量
     * @param status 帖子状态
     */
    @Select("SELECT COUNT(*) FROM t_post WHERE status = #{status}")
    Long getPostCountByStatus(Integer status);
} 