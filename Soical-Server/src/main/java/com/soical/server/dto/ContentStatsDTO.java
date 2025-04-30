package com.soical.server.dto;

import lombok.Data;

@Data
public class ContentStatsDTO {
    /**
     * 帖子统计
     */
    private PostStats postStats;
    
    /**
     * 评论统计
     */
    private CommentStats commentStats;
    
    /**
     * 举报统计
     */
    private ReportStats reportStats;
    
    /**
     * 总帖子数 - 兼容前端字段
     */
    private Long totalPosts;
    
    /**
     * 正常帖子数 - 兼容前端字段
     */
    private Long normalPosts;
    
    /**
     * 违规帖子数 - 兼容前端字段
     */
    private Long violationPosts;
    
    /**
     * 已删除帖子数 - 兼容前端字段
     */
    private Long deletedPosts;

    @Data
    public static class PostStats {
        /**
         * 总帖子数
         */
        private Long totalPosts;
        
        /**
         * 今日新增帖子数
         */
        private Long todayNewPosts;
        
        /**
         * 昨日新增帖子数
         */
        private Long yesterdayNewPosts;
        
        /**
         * 待审核帖子数
         */
        private Long pendingReviewPosts;
        
        /**
         * 已删除帖子数
         */
        private Long deletedPosts;
    }

    @Data
    public static class CommentStats {
        /**
         * 总评论数
         */
        private Long totalComments;
        
        /**
         * 今日新增评论数
         */
        private Long todayNewComments;
        
        /**
         * 昨日新增评论数
         */
        private Long yesterdayNewComments;
        
        /**
         * 待审核评论数
         */
        private Long pendingReviewComments;
        
        /**
         * 已删除评论数
         */
        private Long deletedComments;
    }

    @Data
    public static class ReportStats {
        /**
         * 总举报数
         */
        private Long totalReports;
        
        /**
         * 今日新增举报数
         */
        private Long todayNewReports;
        
        /**
         * 昨日新增举报数
         */
        private Long yesterdayNewReports;
        
        /**
         * 待处理举报数
         */
        private Long pendingReports;
        
        /**
         * 已处理举报数
         */
        private Long processedReports;
        
        /**
         * 帖子举报数
         */
        private Long postReports;
        
        /**
         * 评论举报数
         */
        private Long commentReports;
        
        /**
         * 用户举报数
         */
        private Long userReports;
    }
} 