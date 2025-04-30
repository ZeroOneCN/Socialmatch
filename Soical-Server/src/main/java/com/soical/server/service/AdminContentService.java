package com.soical.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.dto.ContentStatsDTO;
import com.soical.server.entity.Comment;
import com.soical.server.entity.Post;
import com.soical.server.entity.PostComment;
import com.soical.server.entity.Report;

public interface AdminContentService {
    /**
     * 获取帖子列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 关键词
     * @param status 状态
     * @param sortBy 排序字段
     * @param sortOrder 排序方式 asc/desc
     */
    Page<Post> getPostList(Integer page, Integer pageSize, String keyword, Integer status, String sortBy, String sortOrder);

    /**
     * 获取帖子详情
     */
    Post getPostDetail(Long postId);

    /**
     * 审核帖子
     */
    void reviewPost(Long postId, Integer status, String reason);

    /**
     * 删除帖子
     */
    void deletePost(Long postId);

    /**
     * 获取内容统计数据
     */
    ContentStatsDTO getContentStats();

    /**
     * 获取帖子评论列表
     */
    Page<PostComment> getCommentList(Long postId, Integer page, Integer pageSize);

    /**
     * 获取评论详情
     */
    Comment getCommentDetail(Long commentId);

    /**
     * 审核评论
     */
    void reviewComment(Long commentId, Integer status, String reason);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId);
    
    /**
     * 获取举报列表
     */
    Page<Report> getReportList(Integer page, Integer pageSize, Integer reportType, Integer status);
    
    /**
     * 获取举报详情
     */
    Report getReportDetail(Long reportId);
    
    /**
     * 处理举报
     */
    void handleReport(Long reportId, Integer status, String result);
    
    /**
     * 获取举报总数
     */
    Long getReportCount();
    
    /**
     * 获取待处理举报数量
     */
    Long getPendingReportCount();
    
    /**
     * 获取已处理举报比例
     */
    Double getProcessedReportRate();
} 