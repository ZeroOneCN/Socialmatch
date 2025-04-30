package com.soical.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.dto.UserRecommendDTO;
import com.soical.server.entity.User;
import com.soical.server.entity.UserMatch;
import com.soical.server.entity.UserProfile;
import com.soical.server.entity.UserPreference;
import com.soical.server.mapper.UserMatchMapper;
import com.soical.server.service.UserMatchService;
import com.soical.server.service.UserProfileService;
import com.soical.server.service.UserService;
import com.soical.server.service.UserPreferenceService;
import com.soical.server.service.UserRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户匹配服务实现类
 */
@Slf4j
@Service
public class UserMatchServiceImpl extends ServiceImpl<UserMatchMapper, UserMatch> implements UserMatchService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private UserRecommendationService recommendationService;

    @Override
    public List<UserRecommendDTO> getRecommendedUsers(Long userId, Integer gender, Integer ageMin, Integer ageMax, 
                                                     String location, String interests, Integer limit) {
        try {
            // 1. 获取用户偏好
                UserPreference preference = userPreferenceService.getUserPreference(userId);
            if (preference == null) {
                preference = new UserPreference();
                preference.setUserId(userId);
            }

            // 2. 获取已匹配和已拒绝的用户ID
            Set<Long> excludeUserIds = new HashSet<>();
            excludeUserIds.add(userId); // 排除自己
            
            List<UserMatch> existingMatches = baseMapper.selectList(
                Wrappers.lambdaQuery(UserMatch.class)
                        .eq(UserMatch::getUserAId, userId)
            );
            
            for (UserMatch match : existingMatches) {
                excludeUserIds.add(match.getUserBId());
            }

            // 3. 使用传入的参数覆盖用户偏好设置
            if (gender != null) {
                preference.setGenderPreference(gender);
            }
            if (ageMin != null) {
                preference.setAgeMin(ageMin);
            }
            if (ageMax != null) {
                preference.setAgeMax(ageMax);
            }
            if (location != null) {
                preference.setLocationPreference(location);
            }

            // 4. 获取不同类型的推荐结果
            List<UserRecommendDTO> collaborativeResults = recommendationService
                .getCollaborativeFilteringRecommendations(userId, preference.getGenderPreference(), excludeUserIds, limit);
                
            List<UserRecommendDTO> contentResults = recommendationService
                .getContentBasedRecommendations(
                    userId, 
                    preference.getGenderPreference(),
                    preference.getAgeMin(),
                    preference.getAgeMax(),
                    preference.getLocationPreference(),
                    interests, // 使用传入的兴趣参数
                    excludeUserIds,
                    limit
                );
                
            List<UserRecommendDTO> popularResults = recommendationService
                .getPopularUserRecommendations(userId, preference.getGenderPreference(), excludeUserIds, limit);

            // 5. 合并结果并计算最终得分
            Map<Long, UserRecommendDTO> finalResults = new HashMap<>();
            
            // 协同过滤结果权重 0.4
            for (UserRecommendDTO dto : collaborativeResults) {
                dto.setSimilarity((int)(dto.getSimilarity() * 0.4));
                finalResults.put(dto.getUserId(), dto);
            }
            
            // 基于内容的推荐权重 0.4
            for (UserRecommendDTO dto : contentResults) {
                if (finalResults.containsKey(dto.getUserId())) {
                    UserRecommendDTO existing = finalResults.get(dto.getUserId());
                    existing.setSimilarity(existing.getSimilarity() + (int)(dto.getSimilarity() * 0.4));
                } else {
                    dto.setSimilarity((int)(dto.getSimilarity() * 0.4));
                    finalResults.put(dto.getUserId(), dto);
                }
            }
            
            // 热门用户推荐权重 0.2
            for (UserRecommendDTO dto : popularResults) {
                if (finalResults.containsKey(dto.getUserId())) {
                    UserRecommendDTO existing = finalResults.get(dto.getUserId());
                    existing.setSimilarity(existing.getSimilarity() + (int)(dto.getSimilarity() * 0.2));
                } else {
                    dto.setSimilarity((int)(dto.getSimilarity() * 0.2));
                    finalResults.put(dto.getUserId(), dto);
                }
            }

            // 6. 如果没有推荐结果，使用基础推荐
            if (finalResults.isEmpty()) {
                return getBasicRecommendations(userId, gender, ageMin, ageMax, location, interests, limit);
            }

            // 7. 排序并返回结果
            return finalResults.values().stream()
                .sorted(Comparator.comparing(UserRecommendDTO::getSimilarity).reversed())
                .limit(limit)
                .collect(Collectors.toList());
        
        } catch (Exception e) {
            log.error("获取推荐用户失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 计算最终推荐得分
     */
    private void calculateFinalScores(List<UserRecommendDTO> recommendations) {
        for (UserRecommendDTO dto : recommendations) {
            // 基础相似度得分 (60%)
            int similarity = dto.getSimilarity() != null ? dto.getSimilarity() : 50;
            
            // 活跃度得分 (20%)
            int activityScore = getUserActivityScore(dto.getUserId());
            
            // 时间衰减因子
            double timeDecay = calculateTimeDecay(dto.getUserId());
            
            // 推荐类型权重
            double typeWeight = getRecommendTypeWeight(dto.getRecommendType());
            
            // 最终得分 = (相似度 * 0.6 + 活跃度 * 0.2) * 时间衰减 * 推荐类型权重
            int finalScore = (int) ((similarity * 0.6 + activityScore * 2) * timeDecay * typeWeight);
            dto.setSimilarity(Math.min(100, finalScore));
        }
    }

    /**
     * 计算时间衰减因子
     */
    private double calculateTimeDecay(Long userId) {
        try {
            User user = userService.getById(userId);
            if (user == null || user.getUpdateTime() == null) {
                return 0.7; // 默认衰减
            }
            
            // 计算最后活跃距今天数
            long daysSinceLastActive = Duration.between(
                user.getUpdateTime().toInstant(),
                new Date().toInstant()
            ).toDays();
            
            // 使用指数衰减函数: decay = e^(-λt)
            // λ = ln(2)/半衰期(这里设置为7天)
            double lambda = Math.log(2) / 7;
            double decay = Math.exp(-lambda * daysSinceLastActive);
            
            // 限制衰减范围在[0.7, 1.0]之间
            return Math.max(0.7, Math.min(1.0, decay));
            
        } catch (Exception e) {
            log.warn("计算时间衰减因子失败: userId={}", userId, e);
            return 0.7; // 发生错误时返回默认值
        }
    }

    /**
     * 获取推荐类型权重
     */
    private double getRecommendTypeWeight(String recommendType) {
        if (recommendType == null) {
            return 0.9;
        }
        switch (recommendType) {
            case "collaborative":
                return 1.2; // 协同过滤推荐权重最高
            case "content":
                return 1.1; // 基于内容的推荐次之
            case "popular":
                return 1.0; // 热门推荐权重适中
            default:
                return 0.9; // 基础推荐权重最低
        }
    }

    /**
     * 获取用户已交互过的用户ID（喜欢或不喜欢）
     */
    private Set<Long> getUserInteractedIds(Long userId) {
        Set<Long> interactedIds = new HashSet<>();
        
        // 查询用户已经喜欢的用户
        LambdaQueryWrapper<UserMatch> likedQuery = Wrappers.lambdaQuery();
        likedQuery.eq(UserMatch::getUserAId, userId);
        List<UserMatch> likedMatches = baseMapper.selectList(likedQuery);
        
        // 查询喜欢过此用户的用户
        LambdaQueryWrapper<UserMatch> beingLikedQuery = Wrappers.lambdaQuery();
        beingLikedQuery.eq(UserMatch::getUserBId, userId);
        List<UserMatch> beingLikedMatches = baseMapper.selectList(beingLikedQuery);
        
        // 合并交互过的用户ID
        likedMatches.forEach(match -> interactedIds.add(match.getUserBId()));
        beingLikedMatches.forEach(match -> interactedIds.add(match.getUserAId()));
        
        return interactedIds;
    }

    /**
     * 无资料情况下的基础推荐
     */
    private List<UserRecommendDTO> getBasicRecommendations(Long userId, Integer gender, Integer ageMin, Integer ageMax, String location, String interests, Integer limit) {
        Set<Long> interactedUserIds = getUserInteractedIds(userId);
        interactedUserIds.add(userId); // 排除自己
        
        // 获取未交互的用户，使用基础过滤条件
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.notIn(interactedUserIds.isEmpty() ? false : true, User::getUserId, interactedUserIds);
        queryWrapper.eq(User::getStatus, 1); // 只推荐正常状态的用户
        
        if (gender != null && gender > 0) {
            // 如果指定了性别偏好，则按偏好过滤
            queryWrapper.eq(User::getGender, gender);
        } else {
            // 否则基于用户自身性别智能推荐（异性优先）
            User currentUser = userService.getById(userId);
            if (currentUser != null && currentUser.getGender() != null && currentUser.getGender() > 0) {
                if (currentUser.getGender() == 1) {
                    queryWrapper.eq(User::getGender, 2); // 男性用户推荐女性
                } else if (currentUser.getGender() == 2) {
                    queryWrapper.eq(User::getGender, 1); // 女性用户推荐男性
                }
            }
        }
        
        // 随机获取用户
        List<User> randomUsers = userService.list(queryWrapper);
        
        // 如果没有符合条件的用户，放宽条件再查询
        if (randomUsers.isEmpty()) {
            log.info("使用宽松条件查询推荐用户: userId={}", userId);
            queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.notIn(interactedUserIds.isEmpty() ? false : true, User::getUserId, interactedUserIds);
            queryWrapper.eq(User::getStatus, 1);
            randomUsers = userService.list(queryWrapper);
        }
        
        if (randomUsers.isEmpty()) {
            log.warn("系统中没有其他可推荐用户: userId={}", userId);
            return new ArrayList<>();
        }
        
        // 随机打乱顺序
        Collections.shuffle(randomUsers);
        
        // 返回截取的用户列表
        List<UserRecommendDTO> result = randomUsers.stream()
                .limit(limit)
                .map(this::convertToRecommendDTO)
                .collect(Collectors.toList());
        
        log.info("生成基础推荐用户列表: userId={}, count={}", userId, result.size());
        return result;
    }

    /**
     * 检查当前用户是否已经喜欢过目标用户
     */
    private boolean checkIfUserLiked(Long currentUserId, Long targetUserId) {
        LambdaQueryWrapper<UserMatch> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(UserMatch::getUserAId, currentUserId);
        queryWrapper.eq(UserMatch::getUserBId, targetUserId);
        return baseMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 将User实体转换为推荐DTO
     */
    private UserRecommendDTO convertToRecommendDTO(User user) {
        UserRecommendDTO dto = new UserRecommendDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setGender(user.getGender());
        dto.setAvatar(user.getAvatar());
        
        // 计算年龄
        if (user.getBirthday() != null) {
            int age = calculateAge(user.getBirthday());
            dto.setAge(age);
        }
        
        // 获取其他资料信息
        UserProfile profile = userProfileService.getProfileByUserId(user.getUserId());
        if (profile != null) {
            dto.setNickname(profile.getNickname());
            dto.setLocation(profile.getLocation());
            dto.setOccupation(profile.getOccupation());
            dto.setSelfIntro(profile.getSelfIntro());
            dto.setHobbies(profile.getHobbies());
            
            // 如果有相册，解析JSON为List<String>
            if (profile.getPhotos() != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<String> photoList = objectMapper.readValue(profile.getPhotos().toString(), new TypeReference<List<String>>() {});
                    dto.setPhotos(photoList);
                } catch (JsonProcessingException e) {
                    log.error("解析相册JSON失败", e);
                }
            }
        }
        
        // 相似度初始设置为50%
        dto.setSimilarity(50);
        dto.setIsLiked(false);
        
        return dto;
    }

    /**
     * 将User和UserProfile转换为推荐DTO
     */
    private UserRecommendDTO convertToUserRecommendDTO(User user, UserProfile profile) {
        UserRecommendDTO dto = new UserRecommendDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setGender(user.getGender());
        dto.setAvatar(user.getAvatar());
        
        // 计算年龄
        if (user.getBirthday() != null) {
            int age = calculateAge(user.getBirthday());
            dto.setAge(age);
        }
        
        // 设置资料信息
        if (profile != null) {
            dto.setNickname(profile.getNickname());
            dto.setLocation(profile.getLocation());
            dto.setOccupation(profile.getOccupation());
            dto.setSelfIntro(profile.getSelfIntro());
            dto.setHobbies(profile.getHobbies());
            
            // 如果有相册，解析JSON为List<String>
            if (profile.getPhotos() != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<String> photoList = objectMapper.readValue(profile.getPhotos().toString(), new TypeReference<List<String>>() {});
                    dto.setPhotos(photoList);
                } catch (JsonProcessingException e) {
                    log.error("解析相册JSON失败", e);
                }
            }
        }
        
        // 相似度初始设置为50%
        dto.setSimilarity(50);
        dto.setIsLiked(false);
        
        return dto;
    }

    /**
     * 计算年龄
     */
    private int calculateAge(Date birthday) {
        if (birthday == null) {
            return 0;
        }
        
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthday);
        Calendar today = Calendar.getInstance();
        
        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        
        // 如果今年的生日还没到，年龄减1
        if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
            (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
             today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }
        
        return Math.max(0, age);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeUser(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return false;
        }

        // 检查是否已存在匹配关系
        UserMatch existingMatch = getMatchBetweenUsers(userId, targetUserId);

        if (existingMatch == null) {
            // 创建新的匹配记录（用户A喜欢用户B，等待B确认）
            UserMatch newMatch = new UserMatch();
            newMatch.setUserAId(userId);
            newMatch.setUserBId(targetUserId);
            newMatch.setStatus(0); // 待确认
            newMatch.setCreateTime(new Date());
            save(newMatch);
            return false; // 不是匹配成功，只是单方面喜欢
            
        } else {
            // 已存在匹配记录
            
            // 如果当前用户是接收方，且对方已经喜欢了自己
            if (existingMatch.getUserBId().equals(userId) && existingMatch.getStatus() == 0) {
                // 双方互相喜欢，更新为已匹配状态
                existingMatch.setStatus(1); // 已匹配
                updateById(existingMatch);
                return true; // 匹配成功
                
            } else if (existingMatch.getUserAId().equals(userId)) {
                // 当前用户已经喜欢过对方，不做操作
                return existingMatch.getStatus() == 1; // 如果状态是1，表示已经匹配
                
            } else if (existingMatch.getStatus() == 3) {
                // 如果之前被拒绝过，重新创建匹配记录
                existingMatch.setStatus(0); // 改为待确认
                updateById(existingMatch);
                return false;
            }
        }
        
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dislikeUser(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return false;
        }

        // 检查是否已存在匹配关系
        UserMatch existingMatch = getMatchBetweenUsers(userId, targetUserId);

        if (existingMatch == null) {
            // 创建拒绝记录，防止重复推荐
            UserMatch newMatch = new UserMatch();
            newMatch.setUserAId(targetUserId); // 这里反过来，因为被拒绝的逻辑是：targetUser喜欢currentUser，但currentUser不喜欢targetUser
            newMatch.setUserBId(userId);
            newMatch.setStatus(3); // 拒绝
            newMatch.setCreateTime(new Date());
            save(newMatch);
            
        } else {
            if (existingMatch.getUserAId().equals(targetUserId) && existingMatch.getUserBId().equals(userId)) {
                // 对方喜欢自己，自己不喜欢对方
                existingMatch.setStatus(3); // 拒绝
                updateById(existingMatch);
                
            } else if (existingMatch.getStatus() == 1) {
                // 之前已匹配，现在取消匹配
                existingMatch.setStatus(2); // 已解除
                updateById(existingMatch);
                
            } else {
                // 其他情况，也设置为拒绝
                existingMatch.setStatus(3); // 拒绝
                updateById(existingMatch);
            }
        }
        
        return true;
    }

    @Override
    public List<UserMatch> getUserMatches(Long userId, Integer status) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectUserMatches(userId, status);
    }

    @Override
    public UserMatch getMatchById(Long matchId) {
        if (matchId == null) {
            return null;
        }
        return getById(matchId);
    }

    @Override
    public UserMatch getMatchBetweenUsers(Long userAId, Long userBId) {
        if (userAId == null || userBId == null) {
            return null;
        }
        return baseMapper.selectMatchBetweenUsers(userAId, userBId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMatchStatus(Long matchId, Integer status) {
        if (matchId == null || status == null) {
            return false;
        }
        return baseMapper.updateMatchStatus(matchId, status) > 0;
    }

    @Override
    public int calculateSimilarity(Long userAId, Long userBId) {
        if (userAId == null || userBId == null) {
            return 0;
        }

        // 获取两个用户的资料
        UserProfile profileA = userProfileService.getProfileByUserId(userAId);
        UserProfile profileB = userProfileService.getProfileByUserId(userBId);
        
        // 获取用户基本信息
        User userA = userService.getById(userAId);
        User userB = userService.getById(userBId);

        if (profileA == null || profileB == null || userA == null || userB == null) {
            return 0;
        }

        int matchScore = 0;
        int totalFactors = 0;

        // 1. 位置匹配 (25分)
        if (StringUtils.hasText(profileA.getLocation()) && StringUtils.hasText(profileB.getLocation())) {
            totalFactors += 25;
            if (profileA.getLocation().equals(profileB.getLocation())) {
                matchScore += 25;
            } else if (profileA.getLocation().contains(profileB.getLocation()) || 
                     profileB.getLocation().contains(profileA.getLocation())) {
                matchScore += 15;
            }
            // 只匹配到省份也给分
            else if (extractProvince(profileA.getLocation()).equals(extractProvince(profileB.getLocation()))) {
                matchScore += 8;
            }
        }

        // 2. 兴趣爱好匹配 (50分)
        if (StringUtils.hasText(profileA.getHobbies()) && StringUtils.hasText(profileB.getHobbies())) {
            totalFactors += 50;
            
            // 解析兴趣爱好
            List<String> hobbiesA = Arrays.stream(profileA.getHobbies().split(",|、|;"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            
            List<String> hobbiesB = Arrays.stream(profileB.getHobbies().split(",|、|;"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            
            // 计算共同兴趣数量
            int commonHobbies = 0;
            for (String hobbyA : hobbiesA) {
                for (String hobbyB : hobbiesB) {
                    if (hobbyA.equals(hobbyB) || hobbyA.contains(hobbyB) || hobbyB.contains(hobbyA)) {
                        commonHobbies++;
                        break;
                    }
                }
            }
            
            // 计算兴趣匹配得分
            if (!hobbiesA.isEmpty() && !hobbiesB.isEmpty()) {
                int maxHobbies = Math.max(hobbiesA.size(), hobbiesB.size());
                matchScore += (int) (((double) commonHobbies / maxHobbies) * 50);
            }
        }

        // 3. 职业匹配 (15分)
        if (StringUtils.hasText(profileA.getOccupation()) && StringUtils.hasText(profileB.getOccupation())) {
            totalFactors += 15;
            if (profileA.getOccupation().equals(profileB.getOccupation())) {
                matchScore += 15;
            } else if (profileA.getOccupation().contains(profileB.getOccupation()) || 
                     profileB.getOccupation().contains(profileA.getOccupation())) {
                matchScore += 8;
            }
        }

        // 4. 教育程度匹配 (10分)
        if (profileA.getEducation() != null && profileB.getEducation() != null) {
            totalFactors += 10;
            if (profileA.getEducation().equals(profileB.getEducation())) {
                matchScore += 10;
            } else if (Math.abs(profileA.getEducation() - profileB.getEducation()) == 1) {
                matchScore += 5;
            }
        }
        
        // 5. 年龄匹配 (15分)
        if (userA.getBirthday() != null && userB.getBirthday() != null) {
            totalFactors += 15;
            Calendar calA = Calendar.getInstance();
            Calendar calB = Calendar.getInstance();
            calA.setTime(userA.getBirthday());
            calB.setTime(userB.getBirthday());
            int yearA = calA.get(Calendar.YEAR);
            int yearB = calB.get(Calendar.YEAR);
            int ageDiff = Math.abs(yearA - yearB);
            
            if (ageDiff <= 1) {
                matchScore += 15; // 同龄或相差一岁
            } else if (ageDiff <= 3) {
                matchScore += 10; // 相差2-3岁
            } else if (ageDiff <= 5) {
                matchScore += 5;  // 相差4-5岁
            }
        }
        
        // 6. 综合因素 - 活跃度匹配 (10分)
        // 这里可以根据用户的活跃程度（登录频率、发帖数等）来计算匹配度
        // 活跃的用户和活跃的用户更匹配，不活跃的和不活跃的更匹配
        try {
            int userAActivityScore = getUserActivityScore(userAId);
            int userBActivityScore = getUserActivityScore(userBId);
            int activityDiff = Math.abs(userAActivityScore - userBActivityScore);
            
            totalFactors += 10;
            if (activityDiff <= 2) {
                matchScore += 10;
            } else if (activityDiff <= 4) {
                matchScore += 5;
            } else if (activityDiff <= 6) {
                matchScore += 2;
            }
        } catch (Exception e) {
            log.warn("计算活跃度匹配失败", e);
            // 出错不影响其他匹配计算
        }

        // 计算最终相似度百分比
        if (totalFactors > 0) {
            return (matchScore * 100) / totalFactors;
        } else {
            // 如果没有足够的匹配因素，返回默认50%的相似度
            return 50;
        }
    }

    /**
     * 从地址中提取省份
     */
    private String extractProvince(String location) {
        if (!StringUtils.hasText(location)) {
            return "";
        }
        
        // 简单处理，取第一个省/市/自治区
        String[] provinces = {"北京", "天津", "上海", "重庆", "河北", "山西", "辽宁", "吉林", "黑龙江", 
                             "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南", 
                             "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "台湾", 
                             "内蒙古", "广西", "西藏", "宁夏", "新疆", "香港", "澳门"};
        
        for (String province : provinces) {
            if (location.contains(province)) {
                return province;
            }
        }
        
        return location.length() > 2 ? location.substring(0, 2) : location;
    }

    /**
     * 计算用户活跃度分数 (0-10)
     */
    private int getUserActivityScore(Long userId) {
        // 可以根据用户的登录频率、发帖数、评论数等计算活跃度
        // 这里简单实现，实际项目中可以更复杂
        int score = 0;
        
        // 检查最近登录时间
        User user = userService.getById(userId);
        if (user != null && user.getUpdateTime() != null) {
            // 一周内活跃
            long daysDiff = (System.currentTimeMillis() - user.getUpdateTime().getTime()) / (1000 * 60 * 60 * 24);
            if (daysDiff <= 1) {
                score += 4; // 今天活跃
            } else if (daysDiff <= 3) {
                score += 3; // 近3天活跃
            } else if (daysDiff <= 7) {
                score += 2; // 近一周活跃
            } else if (daysDiff <= 30) {
                score += 1; // 近一月活跃
            }
        }
        
        // 尝试查询发帖数和评论数（假设有相关服务）
        try {
            // 这里应该是调用实际存在的服务，例如：
            // int postCount = postService.countByUserId(userId);
            // int commentCount = commentService.countByUserId(userId);
            
            // 临时模拟，随机生成1-5分
            int contentScore = new Random().nextInt(5) + 1;
            score += contentScore;
        } catch (Exception e) {
            log.warn("计算内容活跃度失败", e);
        }
        
        return Math.min(10, score); // 最高10分
    }

    @Override
    public List<UserRecommendDTO> getLikesGiven(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 查询当前用户喜欢的用户记录
        LambdaQueryWrapper<UserMatch> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(UserMatch::getUserAId, userId)  // 当前用户是A，表示用户A喜欢了用户B
                    .in(UserMatch::getStatus, Arrays.asList(0, 1))  // 0-待确认，1-已匹配
                    .orderByDesc(UserMatch::getCreateTime);  // 按时间倒序
        
        List<UserMatch> matches = baseMapper.selectList(queryWrapper);
        if (matches.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 提取被喜欢的用户ID列表
        List<Long> likedUserIds = matches.stream()
                .map(UserMatch::getUserBId)
                .collect(Collectors.toList());
        
        // 获取用户信息
        List<UserRecommendDTO> result = new ArrayList<>();
        for (Long likedUserId : likedUserIds) {
            User user = userService.getById(likedUserId);
            if (user == null) {
                continue;
            }
            
            UserProfile profile = userProfileService.getProfileByUserId(likedUserId);
            UserRecommendDTO dto = convertToUserRecommendDTO(user, profile);
            
            // 设置匹配状态
            UserMatch match = matches.stream()
                    .filter(m -> m.getUserBId().equals(likedUserId))
                    .findFirst().orElse(null);
            
            if (match != null) {
                dto.setMatchStatus(match.getStatus());
                dto.setMatchId(match.getMatchId());
                dto.setIsLiked(true);
            }
            
            result.add(dto);
        }
        
        return result;
    }
    
    @Override
    public List<UserRecommendDTO> getLikesReceived(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 查询喜欢当前用户的记录
        LambdaQueryWrapper<UserMatch> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(UserMatch::getUserBId, userId)  // 当前用户是B，表示用户A喜欢了当前用户
                    .in(UserMatch::getStatus, Arrays.asList(0, 1))  // 0-待确认，1-已匹配
                    .orderByDesc(UserMatch::getCreateTime);  // 按时间倒序
        
        List<UserMatch> matches = baseMapper.selectList(queryWrapper);
        if (matches.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 提取喜欢当前用户的用户ID列表
        List<Long> likedByUserIds = matches.stream()
                .map(UserMatch::getUserAId)
                .collect(Collectors.toList());
        
        // 获取用户信息
        List<UserRecommendDTO> result = new ArrayList<>();
        for (Long likedByUserId : likedByUserIds) {
            User user = userService.getById(likedByUserId);
            if (user == null) {
                continue;
            }
            
            UserProfile profile = userProfileService.getProfileByUserId(likedByUserId);
            UserRecommendDTO dto = convertToUserRecommendDTO(user, profile);
            
            // 设置匹配状态
            UserMatch match = matches.stream()
                    .filter(m -> m.getUserAId().equals(likedByUserId))
                    .findFirst().orElse(null);
            
            if (match != null) {
                dto.setMatchStatus(match.getStatus());
                dto.setMatchId(match.getMatchId());
                
                // 检查当前用户是否也喜欢了这个用户
                boolean userLikedBack = checkIfUserLiked(userId, likedByUserId);
                dto.setIsLiked(userLikedBack);
            }
            
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public Integer countUserLikes(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        try {
            // 查询当前用户作为用户A的所有匹配记录数
            // 用户A是主动喜欢的用户
            LambdaQueryWrapper<UserMatch> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserMatch::getUserAId, userId);
            
            return Math.toIntExact(count(queryWrapper));
        } catch (Exception e) {
            log.error("统计用户喜欢数失败: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public Integer countUserMatches(Long userId, Integer status) {
        if (userId == null) {
            return 0;
        }
        
        try {
            return baseMapper.countUserMatches(userId, status);
        } catch (Exception e) {
            log.error("统计用户匹配数量失败: userId={}, status={}", userId, status, e);
            return 0;
        }
    }

    @Override
    public Integer countPotentialMatches(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        try {
            // 1. 用户发起的喜欢（userA是当前用户）
            LambdaQueryWrapper<UserMatch> likedByUserQuery = new LambdaQueryWrapper<>();
            likedByUserQuery.eq(UserMatch::getUserAId, userId);
            List<UserMatch> likedByUser = list(likedByUserQuery);
            
            // 获取用户喜欢的用户ID集合
            Set<Long> likedUserIds = likedByUser.stream()
                .map(UserMatch::getUserBId)
                .collect(Collectors.toSet());
                
            // 2. 喜欢当前用户的（userB是当前用户）
            LambdaQueryWrapper<UserMatch> likesUserQuery = new LambdaQueryWrapper<>();
            likesUserQuery.eq(UserMatch::getUserBId, userId);
            List<UserMatch> likesUser = list(likesUserQuery);
            
            // 获取喜欢当前用户的用户ID集合
            Set<Long> likeUserIds = likesUser.stream()
                .map(UserMatch::getUserAId)
                .collect(Collectors.toSet());
                
            // 3. 合并两个集合，获取唯一用户ID数量
            Set<Long> allInteractedUserIds = new HashSet<>();
            allInteractedUserIds.addAll(likedUserIds);
            allInteractedUserIds.addAll(likeUserIds);
            
            // 4. 获取推荐过给用户的用户ID集合
            // 这里需要根据实际情况获取，可能需要从推荐历史表或缓存中查询
            // 简化处理，这里假设只从Redis中获取当天推荐的用户
            String recommendCacheKey = "user:recommend:" + userId + ":" + LocalDate.now();
            try {
                @SuppressWarnings("unchecked")
                List<UserRecommendDTO> cachedRecommends = (List<UserRecommendDTO>) redisTemplate.opsForValue().get(recommendCacheKey);
                if (cachedRecommends != null && !cachedRecommends.isEmpty()) {
                    Set<Long> recommendedUserIds = cachedRecommends.stream()
                        .map(UserRecommendDTO::getUserId)
                        .collect(Collectors.toSet());
                    allInteractedUserIds.addAll(recommendedUserIds);
                }
            } catch (Exception e) {
                log.warn("获取推荐用户缓存失败", e);
                // 忽略缓存获取异常，不影响统计
            }
            
            // 返回总的接触用户数
            return allInteractedUserIds.size();
        } catch (Exception e) {
            log.error("统计用户潜在匹配对象失败: userId={}", userId, e);
            return 0;
        }
    }
} 