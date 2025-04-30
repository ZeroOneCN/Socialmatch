package com.soical.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.common.Result;
import com.soical.server.dto.ContentStatsDTO;
import com.soical.server.dto.PostCommentDTO;
import com.soical.server.dto.PostDTO;
import com.soical.server.entity.Post;
import com.soical.server.entity.PostComment;
import com.soical.server.entity.Report;
import com.soical.server.service.AdminContentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/content")
@Api(tags = "管理员内容管理接口")
@RequiredArgsConstructor
@Slf4j
public class AdminContentController {

    private final AdminContentService adminContentService;

    @GetMapping("/posts")
    @ApiOperation("获取帖子列表")
    public Result<Page<Post>> getPostList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "createTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        return Result.success(adminContentService.getPostList(page, pageSize, keyword, status, sortBy, sortOrder));
    }

    @GetMapping("/posts/{postId}")
    @ApiOperation("获取帖子详情")
    public Result<Post> getPostDetail(@PathVariable Long postId) {
        return Result.success(adminContentService.getPostDetail(postId));
    }

    @PutMapping("/posts/{postId}/review")
    @ApiOperation("审核帖子")
    public Result<Void> reviewPost(
            @PathVariable Long postId,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        adminContentService.reviewPost(postId, status, reason);
        return Result.success();
    }

    @DeleteMapping("/posts/{postId}")
    @ApiOperation("删除帖子")
    public Result<Void> deletePost(@PathVariable Long postId) {
        adminContentService.deletePost(postId);
        return Result.success();
    }

    @GetMapping("/stats")
    @ApiOperation("获取内容统计数据")
    public Result<ContentStatsDTO> getContentStats() {
        return Result.success(adminContentService.getContentStats());
    }

    @GetMapping("/posts/{postId}/comments")
    @ApiOperation("获取帖子评论列表")
    public Result<Page<PostComment>> getCommentList(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminContentService.getCommentList(postId, page, pageSize));
    }

    @PutMapping("/comments/{commentId}/review")
    @ApiOperation("审核评论")
    public Result<Void> reviewComment(
            @PathVariable Long commentId,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        adminContentService.reviewComment(commentId, status, reason);
        return Result.success();
    }

    @DeleteMapping("/comments/{commentId}")
    @ApiOperation("删除评论")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        adminContentService.deleteComment(commentId);
        return Result.success();
    }
    
    @GetMapping("/reports")
    @ApiOperation("获取举报列表")
    public Result<Page<Report>> getReportList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "createTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        log.info("获取举报列表, 页码: {}, 每页数量: {}, 类型: {}, 状态: {}, 关键词: {}", page, pageSize, type, status, keyword);
        Page<Report> reports = adminContentService.getReportList(page, pageSize, type, status);
        log.info("查询到举报数量: {}", reports.getTotal());
        return Result.success(reports);
    }
    
    @PutMapping("/reports/{reportId}/process")
    @ApiOperation("处理举报")
    public Result<Void> processReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, Object> params) {
        Long targetId = null;
        Integer type = null;
        Integer status = null;
        String reason = null;
        
        if (params.containsKey("targetId")) {
            targetId = ((Number) params.get("targetId")).longValue();
        }
        if (params.containsKey("type")) {
            type = ((Number) params.get("type")).intValue();
        }
        if (params.containsKey("status")) {
            status = ((Number) params.get("status")).intValue();
        }
        if (params.containsKey("reason")) {
            reason = (String) params.get("reason");
        }
        
        adminContentService.handleReport(reportId, status, reason);
        return Result.success();
    }
    
    @GetMapping("/reports/stats")
    @ApiOperation("获取举报统计数据")
    public Result<Map<String, Object>> getReportStats() {
        ContentStatsDTO stats = adminContentService.getContentStats();
        ContentStatsDTO.ReportStats reportStats = stats.getReportStats();
        
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("totalReports", reportStats.getTotalReports());
        statsMap.put("pendingReports", reportStats.getPendingReports());
        statsMap.put("todayReports", reportStats.getTodayNewReports());
        statsMap.put("postReports", reportStats.getPostReports());
        statsMap.put("commentReports", reportStats.getCommentReports());
        statsMap.put("userReports", reportStats.getUserReports());
        statsMap.put("processedRate", adminContentService.getProcessedReportRate());
        
        return Result.success(statsMap);
    }
} 