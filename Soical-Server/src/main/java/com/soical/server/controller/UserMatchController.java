package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.common.ResultCode;
import com.soical.server.dto.UserRecommendDTO;
import com.soical.server.entity.UserMatch;
import com.soical.server.service.UserMatchService;
import com.soical.server.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户匹配控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/match")
@Api(tags = "用户匹配接口")
public class UserMatchController {

    @Autowired
    private UserMatchService userMatchService;

    @ApiOperation("获取推荐用户列表")
    @GetMapping("/recommend")
    public Result<List<UserRecommendDTO>> getRecommendedUsers(
            @ApiParam(value = "性别筛选：1-男，2-女") @RequestParam(required = false) Integer gender,
            @ApiParam(value = "最小年龄") @RequestParam(required = false) Integer ageMin,
            @ApiParam(value = "最大年龄") @RequestParam(required = false) Integer ageMax,
            @ApiParam(value = "位置筛选") @RequestParam(required = false) String location,
            @ApiParam(value = "兴趣标签，多个以逗号分隔") @RequestParam(required = false) String interests,
            @ApiParam(value = "推荐数量，默认10") @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        log.info("用户 {} 请求推荐列表，条件: gender={}, ageMin={}, ageMax={}, location={}, interests={}, limit={}",
                currentUserId, gender, ageMin, ageMax, location, interests, limit);
        
        List<UserRecommendDTO> recommendedUsers = userMatchService.getRecommendedUsers(
                currentUserId, gender, ageMin, ageMax, location, interests, limit);
        
        return Result.ok(recommendedUsers);
    }

    @ApiOperation("喜欢用户")
    @PostMapping("/like/{targetUserId}")
    public Result<Map<String, Boolean>> likeUser(
            @ApiParam(value = "目标用户ID", required = true) @PathVariable Long targetUserId) {
        
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        log.info("用户 {} 喜欢用户 {}", currentUserId, targetUserId);
        
        boolean matched = userMatchService.likeUser(currentUserId, targetUserId);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("matched", matched);
        return Result.ok(result);
    }

    @ApiOperation("不喜欢用户")
    @PostMapping("/dislike/{targetUserId}")
    public Result<Boolean> dislikeUser(
            @ApiParam(value = "目标用户ID", required = true) @PathVariable Long targetUserId) {
        
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        log.info("用户 {} 不喜欢用户 {}", currentUserId, targetUserId);
        
        boolean result = userMatchService.dislikeUser(currentUserId, targetUserId);
        
        return Result.ok(result);
    }

    @ApiOperation("获取匹配列表")
    @GetMapping("/list")
    public Result<List<UserMatch>> getMatches(
            @ApiParam(value = "匹配状态：0-待确认，1-已匹配，2-已解除，3-拒绝") 
            @RequestParam(required = false) Integer status) {
        
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        List<UserMatch> matches = userMatchService.getUserMatches(currentUserId, status);
        
        return Result.ok(matches);
    }

    @ApiOperation("获取用户喜欢的人列表")
    @GetMapping("/likes-given")
    public Result<List<UserRecommendDTO>> getLikesGiven() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("用户 {} 请求获取已喜欢的用户列表", currentUserId);
        List<UserRecommendDTO> likedUsers = userMatchService.getLikesGiven(currentUserId);
        return Result.ok(likedUsers);
    }

    @ApiOperation("获取喜欢当前用户的人列表")
    @GetMapping("/likes-received")
    public Result<List<UserRecommendDTO>> getLikesReceived() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("用户 {} 请求获取喜欢当前用户的用户列表", currentUserId);
        List<UserRecommendDTO> likedByUsers = userMatchService.getLikesReceived(currentUserId);
        return Result.ok(likedByUsers);
    }

    @ApiOperation("获取匹配详情")
    @GetMapping("/{matchId}")
    public Result<UserMatch> getMatchDetail(
            @ApiParam(value = "匹配ID", required = true) @PathVariable Long matchId) {
        
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        UserMatch match = userMatchService.getMatchById(matchId);
        
        // 检查当前用户是否有权限查看这个匹配
        if (match == null || (!match.getUserAId().equals(currentUserId) && !match.getUserBId().equals(currentUserId))) {
            return Result.fail(ResultCode.FORBIDDEN);
        }
        
        return Result.ok(match);
    }

    @ApiOperation("检查是否匹配")
    @GetMapping("/check/{targetUserId}")
    public Result<Map<String, Object>> checkIfMatched(
            @ApiParam(value = "目标用户ID", required = true) @PathVariable Long targetUserId) {
        
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        UserMatch match = userMatchService.getMatchBetweenUsers(currentUserId, targetUserId);
        
        boolean matched = match != null && match.getStatus() == 1; // 1表示已匹配状态
        
        Map<String, Object> result = new HashMap<>();
        result.put("matched", matched);
        result.put("matchId", match != null ? match.getMatchId() : null);
        result.put("status", match != null ? match.getStatus() : null);
        return Result.ok(result);
    }

    @ApiOperation("取消匹配")
    @PostMapping("/cancel/{matchId}")
    public Result<Boolean> cancelMatch(
            @ApiParam(value = "匹配ID", required = true) @PathVariable Long matchId) {
        
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        UserMatch match = userMatchService.getMatchById(matchId);
        
        // 检查当前用户是否有权限操作这个匹配
        if (match == null || (!match.getUserAId().equals(currentUserId) && !match.getUserBId().equals(currentUserId))) {
            return Result.fail(ResultCode.FORBIDDEN);
        }
        
        boolean result = userMatchService.updateMatchStatus(matchId, 2); // 2表示已解除状态
        
        return Result.ok(result);
    }
} 