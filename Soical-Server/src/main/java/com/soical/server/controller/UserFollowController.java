package com.soical.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.common.Result;
import com.soical.server.dto.UserProfileDTO;
import com.soical.server.service.UserFollowService;
import com.soical.server.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关注控制器
 */
@Api(tags = "用户关注接口")
@Slf4j
@RestController
@RequestMapping("/api/user/follow")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;

    /**
     * 关注用户
     *
     * @param followedId 被关注用户ID
     * @return 操作结果
     */
    @ApiOperation("关注用户")
    @PostMapping("/{followedId}")
    public Result<Boolean> followUser(
            @ApiParam("被关注用户ID") @PathVariable Long followedId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean result = userFollowService.followUser(currentUserId, followedId);
        return Result.success(result);
    }

    /**
     * 取消关注
     *
     * @param followedId 被关注用户ID
     * @return 操作结果
     */
    @ApiOperation("取消关注")
    @DeleteMapping("/{followedId}")
    public Result<Boolean> unfollowUser(
            @ApiParam("被关注用户ID") @PathVariable Long followedId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean result = userFollowService.unfollowUser(currentUserId, followedId);
        return Result.success(result);
    }

    /**
     * 获取关注状态
     *
     * @param followedId 被关注用户ID
     * @return 是否已关注
     */
    @ApiOperation("获取关注状态")
    @GetMapping("/status/{followedId}")
    public Result<Boolean> getFollowStatus(
            @ApiParam("被关注用户ID") @PathVariable Long followedId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean isFollowing = userFollowService.isFollowing(currentUserId, followedId);
        return Result.success(isFollowing);
    }

    /**
     * 获取用户关注列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 关注用户列表
     */
    @ApiOperation("获取用户关注列表")
    @GetMapping("/following/{userId}")
    public Result<Page<UserProfileDTO>> getFollowingList(
            @ApiParam("用户ID") @PathVariable Long userId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<UserProfileDTO> followingList = userFollowService.getFollowingList(userId, page, pageSize);
        return Result.success(followingList);
    }

    /**
     * 获取用户粉丝列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 粉丝用户列表
     */
    @ApiOperation("获取用户粉丝列表")
    @GetMapping("/followers/{userId}")
    public Result<Page<UserProfileDTO>> getFollowerList(
            @ApiParam("用户ID") @PathVariable Long userId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<UserProfileDTO> followerList = userFollowService.getFollowerList(userId, page, pageSize);
        return Result.success(followerList);
    }
} 