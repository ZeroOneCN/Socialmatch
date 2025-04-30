package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.entity.UserProfile;
import com.soical.server.service.UserProfileService;
import com.soical.server.util.UserUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "用户资料接口")
@Slf4j
@RestController
@RequestMapping("/api/user-profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @ApiOperation("获取指定用户资料")
    @GetMapping("/{userId}")
    public Result<UserProfile> getUserProfile(@PathVariable Long userId) {
        log.info("查询用户资料：{}", userId);
        UserProfile profile = userProfileService.getUserProfile(userId);
        if (profile == null) {
            return Result.fail("用户资料不存在");
        }
        return Result.ok(profile);
    }
    
    @ApiOperation("获取当前用户资料")
    @GetMapping("/current")
    public Result<UserProfile> getCurrentUserProfile() {
        Long userId = UserUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        log.info("查询当前用户资料：{}", userId);
        UserProfile profile = userProfileService.getUserProfile(userId);
        if (profile == null) {
            return Result.fail("用户资料不存在");
        }
        return Result.ok(profile);
    }
    
    @ApiOperation("批量获取用户资料")
    @GetMapping("/batch")
    public Result<Map<Long, UserProfile>> batchGetUserProfiles(@RequestParam("userIds") String userIdsStr) {
        try {
            List<Long> userIds = Arrays.stream(userIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            
            log.info("批量查询用户资料：{}", userIds);
            Map<Long, UserProfile> profiles = userProfileService.getUserProfiles(userIds);
            return Result.ok(profiles);
        } catch (Exception e) {
            log.error("批量查询用户资料异常", e);
            return Result.fail("参数格式错误，应为逗号分隔的用户ID列表");
        }
    }
    
    @ApiOperation("查询重复用户资料")
    @GetMapping("/check-duplicates")
    public Result<List<Object>> checkDuplicateProfiles() {
        List<Object> result = new ArrayList<>();
        try {
            result = userProfileService.checkForDuplicateProfiles();
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询重复用户资料异常", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }
} 