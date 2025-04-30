package com.soical.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.common.Result;
import com.soical.server.dto.UserMatchDTO;
import com.soical.server.dto.UserProfileDTO;
import com.soical.server.entity.User;
import com.soical.server.entity.UserMatch;
import com.soical.server.entity.UserPreference;
import com.soical.server.entity.UserProfile;
import com.soical.server.service.UserFollowService;
import com.soical.server.service.UserProfileService;
import com.soical.server.service.UserService;
import com.soical.server.service.PostService;
import com.soical.server.service.UserOnlineStatusService;
import com.soical.server.service.UserMatchService;
import com.soical.server.service.ChatService;
import com.soical.server.service.UserPreferenceService;
import com.soical.server.util.FileUtil;
import com.soical.server.util.SecurityUtil;
import com.soical.server.util.TokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 */
@Api(tags = "用户接口")
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final UserFollowService userFollowService;
    private final PostService postService;
    private final UserOnlineStatusService userOnlineStatusService;
    private final UserMatchService userMatchService;
    private final ChatService chatService;
    private final TokenUtil tokenService;
    private final UserPreferenceService userPreferenceService;

    /**
     * 获取用户信息
     */
    @ApiOperation("获取用户信息")
    @GetMapping("/{userId}")
    public Result<User> getUserInfo(@ApiParam("用户ID") @PathVariable Long userId) {
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    /**
     * 获取当前用户信息
     */
    @ApiOperation("获取当前用户信息")
    @GetMapping("/current")
    public Result<User> getCurrentUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    /**
     * 获取用户资料
     */
    @ApiOperation("获取用户资料")
    @GetMapping("/profile/{userId}")
    public Result<UserProfile> getUserProfile(@ApiParam("用户ID") @PathVariable Long userId) {
        UserProfile userProfile = userProfileService.getProfileByUserId(userId);
        return Result.success(userProfile);
    }

    /**
     * 获取当前用户资料
     */
    @ApiOperation("获取当前用户资料")
    @GetMapping("/profile")
    public Result<UserProfile> getCurrentUserProfile2() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserProfile userProfile = userProfileService.getProfileByUserId(userId);
        return Result.success(userProfile);
    }

    /**
     * 获取当前用户资料 (兼容旧API)
     */
    @ApiOperation("获取当前用户资料 (兼容旧API)")
    @GetMapping("/profile/current")
    public Result<UserProfile> getCurrentUserProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserProfile userProfile = userProfileService.getProfileByUserId(userId);
        return Result.success(userProfile);
    }

    /**
     * 更新用户资料
     */
    @ApiOperation("更新用户资料")
    @PutMapping("/profile")
    public Result<Boolean> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO) {
        // 确保是当前用户的资料
        Long userId = SecurityUtil.getCurrentUserId();
        boolean result = userProfileService.updateUserProfile(userId, userProfileDTO);
        return Result.success(result);
    }
    
    /**
     * 更新用户基本信息
     */
    @ApiOperation("更新用户基本信息")
    @PutMapping("/basic")
    public Result<Boolean> updateUserBasicInfo(@RequestBody Map<String, String> basicInfo) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 同时更新用户表和用户资料表
        boolean result = userService.updateUserBasicInfo(userId, basicInfo);
        return Result.success(result);
    }
    
    /**
     * 同步用户基本信息到用户资料表
     */
    @ApiOperation("同步用户基本信息到用户资料表")
    @PostMapping("/sync-profile")
    public Result<Boolean> syncUserProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean result = userService.syncUserToProfile(userId);
        return Result.success(result);
    }
    
    /**
     * 上传头像
     */
    @ApiOperation("上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserId();
        String avatarUrl = userService.updateAvatar(userId, file);
        return Result.success(avatarUrl);
    }
    
    /**
     * 上传照片到个人相册
     */
    @ApiOperation("上传照片到个人相册")
    @PostMapping("/photos")
    public Result<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserId();
        String photoUrl = userProfileService.addPhoto(userId, file);
        return Result.success(photoUrl);
    }
    
    /**
     * 获取个人相册
     */
    @ApiOperation("获取个人相册")
    @GetMapping("/photos")
    public Result<List<String>> getPhotos() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<String> photos = userProfileService.getPhotos(userId);
        return Result.success(photos);
    }
    
    /**
     * 删除相册照片
     */
    @ApiOperation("删除相册照片")
    @DeleteMapping("/photos")
    public Result<Boolean> deletePhoto(@RequestBody Map<String, String> request) {
        Long userId = SecurityUtil.getCurrentUserId();
        String photoUrl = request.get("photoUrl");
        boolean result = userProfileService.deletePhoto(userId, photoUrl);
        return Result.success(result);
    }

    /**
     * 获取用户统计数据（动态数、粉丝数、关注数）
     * @return 用户统计数据
     */
    @ApiOperation("获取用户统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats() {
        Long userId = SecurityUtil.getCurrentUserId();
        return getUserStatsById(userId);
    }
    
    /**
     * 获取指定用户的统计数据（动态数、粉丝数、关注数）
     * @param userId 用户ID
     * @return 用户统计数据
     */
    @ApiOperation("获取指定用户的统计数据")
    @GetMapping("/{userId}/stats")
    public Result<Map<String, Object>> getUserStatsById(@PathVariable Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取动态数量
        int postCount = postService.countUserPosts(userId);
        
        // 获取粉丝数量
        int followerCount = userFollowService.getFollowerCount(userId);
        
        // 获取关注数量
        int followingCount = userFollowService.getFollowingCount(userId);
        
        stats.put("posts", postCount);
        stats.put("followers", followerCount);
        stats.put("following", followingCount);
        
        return Result.success(stats);
    }

    /**
     * 获取用户同频统计数据
     * @return 同频统计数据
     */
    @ApiOperation("获取用户同频统计数据")
    @GetMapping("/frequency/stats")
    public Result<Map<String, Object>> getFrequencyStats() {
        Long userId = SecurityUtil.getCurrentUserId();
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 通过UserMatchService获取用户的匹配数据
            // 获取已匹配用户数量（状态为1的匹配记录）
            Integer matchedCount = userMatchService.countUserMatches(userId, 1);
            
            // 获取待确认匹配数量（状态为0的匹配记录）
            Integer pendingCount = userMatchService.countUserMatches(userId, 0);
            
            // 获取用户已接触过的潜在匹配对象总数
            Integer potentialMatchesCount = userMatchService.countPotentialMatches(userId);
            
            // 获取聊天记录数量
            Integer conversationCount = chatService.countUserConversations(userId);
            
            // 计算匹配率 (如果潜在匹配对象数为0，设置为0避免除以0)
            int matchRate = 0;
            if (potentialMatchesCount != null && potentialMatchesCount > 0) {
                matchRate = (int) Math.round((double) matchedCount / potentialMatchesCount * 100);
            }
            
            // 获取今日推荐数量
            Integer todayRecommends = 30; // 可以根据实际情况从配置或其他地方获取
            
            // 填充统计数据
            stats.put("matches", matchedCount);
            stats.put("pendingMatches", pendingCount);
            stats.put("conversationCount", conversationCount);
            stats.put("potentialMatches", potentialMatchesCount);
            stats.put("rate", matchRate);
            stats.put("todayRecommends", todayRecommends);
            stats.put("newMatches", pendingCount);
            stats.put("conversations", conversationCount);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户同频统计数据失败", e);
            // 出错时返回默认数据
            stats.put("matches", 0);
            stats.put("rate", 0);
            stats.put("potentialMatches", 0);
            stats.put("todayRecommends", 30);
            stats.put("newMatches", 0);
            stats.put("conversations", 0);
            return Result.success(stats);
        }
    }
    
    /**
     * 获取用户同频匹配列表
     * @param page 页码
     * @param pageSize 每页数量
     * @return 匹配用户列表
     */
    @ApiOperation("获取用户同频匹配列表")
    @GetMapping("/frequency/matches")
    public Result<Page<UserMatchDTO>> getFrequencyMatches(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        try {
            log.info("获取用户同频匹配列表: userId={}, page={}, pageSize={}", userId, page, pageSize);
            
            // 获取用户的匹配列表
            List<UserMatch> matches = userMatchService.getUserMatches(userId, null);
            log.info("查询到匹配记录数: {}", matches.size());
            
            // 转换为DTO并分页
            List<UserMatchDTO> matchDTOs = new ArrayList<>();
            
            for (UserMatch match : matches) {
                // 获取匹配的另一方用户
                Long matchedUserId = match.getUserAId().equals(userId) 
                    ? match.getUserBId() : match.getUserAId();
                
                User matchedUser = userService.getById(matchedUserId);
                if (matchedUser == null) {
                    log.warn("匹配用户不存在: {}", matchedUserId);
                    continue;
                }
                
                // 获取用户资料
                UserProfile profile = userProfileService.getProfileByUserId(matchedUserId);
                
                // 转换为DTO
                UserMatchDTO dto = new UserMatchDTO();
                dto.setMatchId(match.getMatchId());
                dto.setUserId(matchedUserId);
                dto.setUsername(matchedUser.getUsername());
                // 从用户资料中获取昵称，如果没有则使用用户名
                dto.setNickname(profile != null && profile.getNickname() != null ? 
                    profile.getNickname() : matchedUser.getUsername());
                dto.setAvatar(matchedUser.getAvatar());
                dto.setGender(matchedUser.getGender());
                
                // 计算年龄
                if (matchedUser.getBirthday() != null) {
                    Calendar birthDay = Calendar.getInstance();
                    birthDay.setTime(matchedUser.getBirthday());
                    Calendar now = Calendar.getInstance();
                    int age = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
                    dto.setAge(age);
                }
                
                // 设置资料信息
                if (profile != null) {
                    dto.setOccupation(profile.getOccupation());
                    dto.setLocation(profile.getLocation());
                    dto.setSelfIntro(profile.getSelfIntro());
                }
                
                // 设置匹配状态
                dto.setStatus(match.getStatus());
                dto.setCreateTime(match.getCreateTime());
                
                // 计算相似度
                int similarity = userMatchService.calculateSimilarity(userId, matchedUserId);
                dto.setSimilarity(similarity);
                
                // 判断是否有未读消息 (假设目前没有)
                dto.setHasUnreadMessage(false);
                
                matchDTOs.add(dto);
            }
            
            // 分页处理
            List<UserMatchDTO> pageResult = matchDTOs.stream()
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
                
            // 创建分页对象
            Page<UserMatchDTO> resultPage = new Page<>();
            resultPage.setCurrent(page);
            resultPage.setSize(pageSize);
            resultPage.setTotal(matchDTOs.size());
            resultPage.setRecords(pageResult);
            
            log.info("返回匹配结果: 总数={}, 当前页数据={}", matchDTOs.size(), pageResult.size());
            return Result.success(resultPage);
        } catch (Exception e) {
            log.error("获取用户同频匹配列表异常", e);
            return Result.fail("获取匹配列表失败");
        }
    }
    
    /**
     * 更新用户同频偏好设置
     * @param preferences 偏好设置数据
     * @return 操作结果
     */
    @ApiOperation("更新用户同频偏好设置")
    @PutMapping("/frequency/preferences")
    public Result<Boolean> updateFrequencyPreferences(@RequestBody Map<String, Object> preferences) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        
        boolean success = userPreferenceService.updateUserPreference(userId, preferences);
        if (success) {
            return Result.success(true);
        } else {
            return Result.fail("更新偏好设置失败");
        }
    }
    
    /**
     * 获取用户同频偏好设置
     * @return 偏好设置数据
     */
    @ApiOperation("获取用户同频偏好设置")
    @GetMapping("/frequency/preferences")
    public Result<Map<String, Object>> getFrequencyPreferences() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        
        try {
            // 获取用户偏好设置
            UserPreference preference = userPreferenceService.getUserPreference(userId);
            
            // 转换为前端需要的格式
            Map<String, Object> result = userPreferenceService.convertToMap(preference);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户偏好设置失败: userId={}", userId, e);
            return Result.fail("获取偏好设置失败");
        }
    }

    /**
     * 获取用户在线状态
     * @param userIdsStr 用户ID字符串，多个ID用逗号分隔
     * @return 用户在线状态映射
     */
    @ApiOperation("获取用户在线状态")
    @GetMapping("/online-status")
    public Result<Map<Long, Boolean>> getUserOnlineStatus(
            @ApiParam("用户ID列表，多个ID用逗号分隔") @RequestParam String userIdsStr) {
        
        // 解析用户ID列表
        List<Long> userIds = new ArrayList<>();
        try {
            String[] idStrArr = userIdsStr.split(",");
            for (String idStr : idStrArr) {
                if (idStr != null && !idStr.trim().isEmpty()) {
                    userIds.add(Long.parseLong(idStr.trim()));
                }
            }
        } catch (Exception e) {
            log.error("解析用户ID列表失败", e);
            return Result.fail("无效的用户ID格式");
        }
        
        if (userIds.isEmpty()) {
            return Result.fail("用户ID列表为空");
        }
        
        try {
            // 获取在线状态
            Map<Long, Boolean> onlineStatusMap;
            
            // 优先使用在线状态服务
            if (userOnlineStatusService != null) {
                onlineStatusMap = userOnlineStatusService.batchGetUserOnlineStatus(userIds);
            } else {
                // 如果服务不可用，生成随机在线状态
                log.warn("用户在线状态服务不可用，使用随机状态");
                onlineStatusMap = new HashMap<>();
                for (Long userId : userIds) {
                    // 随机约50%的用户显示为在线
                    onlineStatusMap.put(userId, Math.random() > 0.5);
                }
            }
            
            return Result.success(onlineStatusMap);
        } catch (Exception e) {
            log.error("获取用户在线状态失败", e);
            return Result.fail("获取用户在线状态失败");
        }
    }
    
    /**
     * 获取单个用户的在线状态
     */
    @ApiOperation("获取单个用户的在线状态")
    @GetMapping("/status/{userId}")
    public Result<Boolean> getSingleUserOnlineStatus(
            @ApiParam("用户ID") @PathVariable Long userId) {
        
        if (userId == null) {
            return Result.fail("用户ID不能为空");
        }
        
        try {
            boolean isOnline = false;
            
            // 优先使用在线状态服务
            if (userOnlineStatusService != null) {
                isOnline = userOnlineStatusService.isUserOnline(userId);
                log.info("用户 {} 在线状态: {}", userId, isOnline ? "在线" : "离线");
            } else {
                // 如果服务不可用，生成随机在线状态
                log.warn("用户在线状态服务不可用，使用随机状态");
                isOnline = Math.random() > 0.5;
            }
            
            return Result.success(isOnline);
        } catch (Exception e) {
            log.error("获取用户在线状态失败", e);
            return Result.fail("获取用户在线状态失败");
        }
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request) {
        // 获取当前登录用户
        String token = TokenUtil.getRequestToken(request);
        try {
            Long userId = tokenService.getUserIdFromToken(token);
            if (userId != null) {
                log.info("用户 {} 退出登录", userId);
                
                // 设置用户为离线状态
                try {
                    userOnlineStatusService.userOffline(userId);
                    log.info("已将用户 {} 设置为离线状态", userId);
                } catch (Exception e) {
                    log.error("设置用户离线状态失败", e);
                    // 失败不影响登出流程
                }
                
                // 移除token
                tokenService.removeToken(token);
                
                return Result.ok(true);
            }
        } catch (Exception e) {
            log.error("处理退出请求时出错", e);
        }
        
        return Result.ok(false);
    }
} 