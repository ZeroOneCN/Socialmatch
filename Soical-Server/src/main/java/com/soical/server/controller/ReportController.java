package com.soical.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.common.Result;
import com.soical.server.dto.ContentStatsDTO;
import com.soical.server.entity.Report;
import com.soical.server.service.AdminContentService;
import com.soical.server.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Api(tags = "举报管理接口")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final AdminContentService adminContentService;

    @PostMapping("/reports")
    @ApiOperation("提交举报")
    public Result<Void> submitReport(@RequestBody Report report) {
        reportService.submitReport(report);
        return Result.success();
    }

    @GetMapping("/reports/reasons")
    @ApiOperation("获取举报原因列表")
    public Result<List<Map<String, Object>>> getReportReasons(@RequestParam Integer type) {
        List<Map<String, Object>> reasons;
        
        // 根据举报类型返回不同的原因列表
        if (type == 1) {
            // 动态举报原因
            reasons = Arrays.asList(
                createReasonMap(101, "色情低俗"),
                createReasonMap(102, "政治敏感"),
                createReasonMap(103, "商业广告"),
                createReasonMap(104, "人身攻击"),
                createReasonMap(105, "违法违规"),
                createReasonMap(106, "虚假信息"),
                createReasonMap(107, "其他")
            );
        } else if (type == 2) {
            // 评论举报原因
            reasons = Arrays.asList(
                createReasonMap(201, "色情低俗"),
                createReasonMap(202, "政治敏感"),
                createReasonMap(203, "商业广告"),
                createReasonMap(204, "人身攻击"),
                createReasonMap(205, "违法违规"),
                createReasonMap(206, "其他")
            );
        } else if (type == 3) {
            // 用户举报原因
            reasons = Arrays.asList(
                createReasonMap(301, "冒充他人"),
                createReasonMap(302, "发布不良信息"),
                createReasonMap(303, "诈骗行为"),
                createReasonMap(304, "骚扰他人"),
                createReasonMap(305, "违法违规"),
                createReasonMap(306, "其他")
            );
        } else {
            reasons = Arrays.asList(createReasonMap(999, "其他"));
        }
        
        return Result.success(reasons);
    }

    private Map<String, Object> createReasonMap(int id, String reason) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("reason", reason);
        return map;
    }

    @GetMapping("/user/reports")
    @ApiOperation("获取用户举报历史")
    public Result<Page<Report>> getUserReportHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 从当前登录用户获取用户ID
        Long userId = 0L; // TODO: 从当前登录用户获取
        
        return Result.success(reportService.getUserReportHistory(userId, page, pageSize));
    }

    @PutMapping("/reports/{reportId}/cancel")
    @ApiOperation("取消举报")
    public Result<Void> cancelReport(@PathVariable Long reportId) {
        // 从当前登录用户获取用户ID
        Long userId = 0L; // TODO: 从当前登录用户获取
        
        reportService.cancelReport(reportId, userId);
        return Result.success();
    }

    @GetMapping("/stats")
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