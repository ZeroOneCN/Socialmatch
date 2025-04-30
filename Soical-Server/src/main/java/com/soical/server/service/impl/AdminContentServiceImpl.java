package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.dto.ContentStatsDTO;
import com.soical.server.entity.Comment;
import com.soical.server.entity.Post;
import com.soical.server.entity.PostComment;
import com.soical.server.entity.Report;
import com.soical.server.mapper.CommentMapper;
import com.soical.server.mapper.PostCommentMapper;
import com.soical.server.mapper.PostMapper;
import com.soical.server.mapper.ReportMapper;
import com.soical.server.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 内容管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminContentServiceImpl implements AdminContentService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final PostCommentMapper postCommentMapper;
    private final ReportMapper reportMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<Post> getPostList(Integer page, Integer pageSize, String keyword, Integer status, String sortBy, String sortOrder) {
        Page<Post> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加关键词搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Post::getContent, keyword);
        }
        
        // 添加状态筛选条件
        if (status != null) {
            queryWrapper.eq(Post::getStatus, status);
        }
        
        // 处理排序
        if (sortBy != null && !sortBy.isEmpty()) {
            if ("postId".equals(sortBy)) {
                if ("desc".equalsIgnoreCase(sortOrder)) {
                    queryWrapper.orderByDesc(Post::getPostId);
                } else {
                    queryWrapper.orderByAsc(Post::getPostId);
                }
            } else if ("createTime".equals(sortBy)) {
                if ("desc".equalsIgnoreCase(sortOrder)) {
                    queryWrapper.orderByDesc(Post::getCreateTime);
                } else {
                    queryWrapper.orderByAsc(Post::getCreateTime);
                }
            } else if ("likeCount".equals(sortBy)) {
                if ("desc".equalsIgnoreCase(sortOrder)) {
                    queryWrapper.orderByDesc(Post::getLikeCount);
                } else {
                    queryWrapper.orderByAsc(Post::getLikeCount);
                }
            } else {
                // 默认按创建时间降序
                queryWrapper.orderByDesc(Post::getCreateTime);
            }
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc(Post::getCreateTime);
        }
        
        return postMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public Post getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        
        if (post == null) {
            return null;
        }
        
        // 如果图片字段是JSON字符串，确保它能被正确处理
        // 在此不做转换，由前端进行解析
        
        return post;
    }

    @Override
    @Transactional
    public void reviewPost(Long postId, Integer status, String reason) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setStatus(status);
            post.setReviewReason(reason);
            post.setReviewTime(LocalDateTime.now());
            postMapper.updateById(post);
        }
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        // 先删除相关评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getPostId, postId);
        commentMapper.delete(commentWrapper);
        
        // 删除帖子相关的点赞记录
        try {
            // 使用JdbcTemplate执行原生SQL
            log.info("删除帖子点赞记录，帖子ID: {}", postId);
            String deleteLikesSql = "DELETE FROM t_post_like WHERE post_id = ?";
            jdbcTemplate.update(deleteLikesSql, postId);
            log.info("成功删除帖子点赞记录");
        } catch (Exception e) {
            log.warn("删除帖子点赞记录失败，帖子ID: {}, 错误: {}", postId, e.getMessage());
            // 异常不影响主流程，继续执行
        }
        
        // 最后删除帖子
        postMapper.deleteById(postId);
    }

    @Override
    public Page<PostComment> getCommentList(Long postId, Integer page, Integer pageSize) {
        Page<PostComment> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加帖子ID筛选条件
        if (postId != null) {
            queryWrapper.eq(PostComment::getPostId, postId);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(PostComment::getCreateTime);
        
        try {
            Page<PostComment> result = postCommentMapper.selectPage(pageParam, queryWrapper);
            if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
                log.info("查询到评论数量: {}", result.getRecords().size());
            } else {
                log.info("未查询到评论数据");
            }
            return result;
        } catch (Exception e) {
            log.error("查询评论列表出错: {}", e.getMessage(), e);
            return new Page<>();
        }
    }

    @Override
    public Comment getCommentDetail(Long commentId) {
        return commentMapper.selectById(commentId);
    }

    @Override
    @Transactional
    public void reviewComment(Long commentId, Integer status, String reason) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setStatus(status);
            comment.setReviewReason(reason);
            comment.setReviewTime(new Date());
            commentMapper.updateById(comment);
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        // 获取评论信息以获取关联的帖子ID
        Comment comment = commentMapper.selectById(commentId);
        
        if (comment != null) {
            Long postId = comment.getPostId();
            
            // 删除评论
            commentMapper.deleteById(commentId);
            
            // 更新帖子评论数
            if (postId != null) {
                try {
                    log.info("更新帖子评论数，帖子ID: {}", postId);
                    // 使用JdbcTemplate直接执行SQL，确保更新成功
                    String updateSql = "UPDATE t_post SET comment_count = GREATEST(comment_count - 1, 0) WHERE post_id = ?";
                    jdbcTemplate.update(updateSql, postId);
                    log.info("成功更新帖子评论数");
                } catch (Exception e) {
                    log.error("更新帖子评论数失败，帖子ID: {}, 错误: {}", postId, e.getMessage(), e);
                }
            }
        } else {
            log.warn("未找到评论, 评论ID: {}", commentId);
        }
    }

    @Override
    public Page<Report> getReportList(Integer page, Integer pageSize, Integer reportType, Integer status) {
        Page<Report> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加举报类型筛选条件
        if (reportType != null) {
            queryWrapper.eq(Report::getReportType, reportType);
        }
        
        // 添加状态筛选条件
        if (status != null) {
            queryWrapper.eq(Report::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Report::getCreateTime);
        
        return reportMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public Report getReportDetail(Long reportId) {
        return reportMapper.selectById(reportId);
    }

    @Override
    @Transactional
    public void handleReport(Long reportId, Integer status, String result) {
        Report report = reportMapper.selectById(reportId);
        if (report != null) {
            report.setStatus(status);
            report.setResult(result);
            // 设置处理时间为当前时间
            report.setHandleTime(LocalDateTime.now());
            reportMapper.updateById(report);
            
            // 如果是举报帖子或评论，并且处理结果为违规，则更新对应内容的状态
            if (status == 1) { // 假设状态1表示举报处理为违规
                if (report.getReportType() == 1) { // 帖子
                    Post post = postMapper.selectById(report.getTargetId());
                    if (post != null) {
                        post.setStatus(2); // 假设状态2表示违规被下架
                        postMapper.updateById(post);
                    }
                } else if (report.getReportType() == 2) { // 评论
                    Comment comment = commentMapper.selectById(report.getTargetId());
                    if (comment != null) {
                        comment.setStatus(2); // 假设状态2表示违规被删除
                        commentMapper.updateById(comment);
                    }
                }
            }
        }
    }

    @Override
    public ContentStatsDTO getContentStats() {
        ContentStatsDTO statsDTO = new ContentStatsDTO();
        
        // 填充帖子统计数据
        ContentStatsDTO.PostStats postStats = new ContentStatsDTO.PostStats();
        Long totalPosts = postMapper.selectCount(null);
        Long normalPosts = postMapper.getPostCountByStatus(1);
        Long violationPosts = postMapper.getPostCountByStatus(2);
        Long deletedPosts = postMapper.getPostCountByStatus(0);
        
        postStats.setTotalPosts(totalPosts);
        postStats.setTodayNewPosts(postMapper.getTodayNewPostsCount());
        postStats.setYesterdayNewPosts(postMapper.getYesterdayNewPostsCount());
        postStats.setPendingReviewPosts(postMapper.getPostCountByStatus(0)); // 假设状态0表示待审核
        statsDTO.setPostStats(postStats);
        
        // 兼容前端字段
        statsDTO.setTotalPosts(totalPosts);
        statsDTO.setNormalPosts(normalPosts);
        statsDTO.setViolationPosts(violationPosts);
        statsDTO.setDeletedPosts(deletedPosts);
        
        // 填充评论统计数据
        ContentStatsDTO.CommentStats commentStats = new ContentStatsDTO.CommentStats();
        commentStats.setTotalComments(commentMapper.selectCount(null));
        commentStats.setTodayNewComments(commentMapper.getTodayNewCommentsCount());
        commentStats.setYesterdayNewComments(commentMapper.getYesterdayNewCommentsCount());
        commentStats.setPendingReviewComments(commentMapper.getPendingReviewCommentsCount());
        statsDTO.setCommentStats(commentStats);
        
        // 填充举报统计数据
        ContentStatsDTO.ReportStats reportStats = new ContentStatsDTO.ReportStats();
        reportStats.setTotalReports(reportMapper.selectCount(null));
        
        // Integer值的安全转换，如果为null则返回0L
        Integer todayReports = reportMapper.getTodayNewReportsCount();
        Integer yesterdayReports = reportMapper.getYesterdayNewReportsCount();
        Integer pendingReports = reportMapper.getPendingReportsCount();
        Integer postReports = reportMapper.getReportCountByType(1);
        Integer commentReports = reportMapper.getReportCountByType(2);
        Integer userReports = reportMapper.getReportCountByType(3);
        
        reportStats.setTodayNewReports(todayReports == null ? 0L : Long.valueOf(todayReports));
        reportStats.setYesterdayNewReports(yesterdayReports == null ? 0L : Long.valueOf(yesterdayReports));
        reportStats.setPendingReports(pendingReports == null ? 0L : Long.valueOf(pendingReports));
        reportStats.setPostReports(postReports == null ? 0L : Long.valueOf(postReports));
        reportStats.setCommentReports(commentReports == null ? 0L : Long.valueOf(commentReports));
        reportStats.setUserReports(userReports == null ? 0L : Long.valueOf(userReports));
        
        statsDTO.setReportStats(reportStats);
        
        return statsDTO;
    }
    
    @Override
    public Long getReportCount() {
        return reportMapper.selectCount(null);
    }
    
    @Override
    public Long getPendingReportCount() {
        Integer pendingReports = reportMapper.getPendingReportsCount();
        return pendingReports == null ? 0L : Long.valueOf(pendingReports);
    }
    
    @Override
    public Double getProcessedReportRate() {
        Long totalReports = getReportCount();
        
        if (totalReports == 0) {
            return 0.0;
        }
        
        Long pendingReports = getPendingReportCount();
        Long processedReports = totalReports - pendingReports;
        
        return processedReports.doubleValue() / totalReports.doubleValue() * 100;
    }
} 