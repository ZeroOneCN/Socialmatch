package com.soical.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.common.Result;
import com.soical.server.dto.AdminPostDTO;
import com.soical.server.service.AdminPostService;
import com.soical.server.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员动态管理控制器
 */
@RestController
@RequestMapping("/api/admin/post")
@RequiredArgsConstructor
@Api(tags = "管理员动态管理接口")
public class AdminPostController {

    private final AdminPostService adminPostService;

    @GetMapping("/list")
    @ApiOperation("获取动态列表")
    public Result<Page<AdminPostDTO>> getPostList(
            @ApiParam(value = "状态：0-删除，1-正常，2-违规，null-全部") @RequestParam(required = false) Integer status,
            @ApiParam(value = "关键词") @RequestParam(required = false) String keyword,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AdminPostDTO> postList = adminPostService.getPostList(status, keyword, page, pageSize);
        return Result.success(postList);
    }

    @GetMapping("/{postId}")
    @ApiOperation("获取动态详情")
    public Result<AdminPostDTO> getPostDetail(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId) {
        AdminPostDTO postDetail = adminPostService.getPostDetail(postId);
        return Result.success(postDetail);
    }

    @PutMapping("/review/{postId}")
    @ApiOperation("审核动态")
    public Result<Boolean> reviewPost(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId,
            @ApiParam(value = "状态：1-正常，2-违规", required = true) @RequestParam Integer status,
            @ApiParam(value = "拒绝原因（当status=2时提供）") @RequestParam(required = false) String rejectReason) {
        // 获取管理员ID
        Long adminId = SecurityUtil.getCurrentAdminId();
        boolean result = adminPostService.reviewPost(postId, status, adminId, rejectReason);
        return Result.success(result);
    }

    @DeleteMapping("/{postId}")
    @ApiOperation("删除动态")
    public Result<Boolean> deletePost(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId) {
        // 获取管理员ID
        Long adminId = SecurityUtil.getCurrentAdminId();
        boolean result = adminPostService.deletePost(postId, adminId);
        return Result.success(result);
    }

    @GetMapping("/statistics")
    @ApiOperation("获取动态统计数据")
    public Result<AdminPostService.AdminPostStatistics> getPostStatistics() {
        AdminPostService.AdminPostStatistics statistics = adminPostService.getPostStatistics();
        return Result.success(statistics);
    }
} 