package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.service.PostLikeService;
import com.soical.server.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 动态点赞控制器
 */
@RestController
@RequestMapping("/api/post/like")
@RequiredArgsConstructor
@Api(tags = "动态点赞接口")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}")
    @ApiOperation("点赞动态")
    public Result<Boolean> likePost(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean result = postLikeService.likePost(userId, postId);
        return Result.success(result);
    }

    @DeleteMapping("/{postId}")
    @ApiOperation("取消点赞")
    public Result<Boolean> unlikePost(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean result = postLikeService.unlikePost(userId, postId);
        return Result.success(result);
    }

    @GetMapping("/status/{postId}")
    @ApiOperation("获取点赞状态")
    public Result<Boolean> getLikeStatus(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean hasLiked = postLikeService.hasLiked(userId, postId);
        return Result.success(hasLiked);
    }

    @GetMapping("/users/{postId}")
    @ApiOperation("获取点赞用户列表")
    public Result<List<Long>> getLikeUsers(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId) {
        List<Long> userIds = postLikeService.getLikeUserIds(postId);
        return Result.success(userIds);
    }
} 