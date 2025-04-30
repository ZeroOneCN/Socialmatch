package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.soical.server.dto.UserRecommendDTO;
import com.soical.server.entity.User;
import com.soical.server.entity.UserMatch;
import com.soical.server.entity.UserProfile;
import com.soical.server.mapper.UserMatchMapper;
import com.soical.server.service.UserProfileService;
import com.soical.server.service.UserService;
import com.soical.server.service.UserRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户推荐服务实现类
 */
@Slf4j
@Service
public class UserRecommendationServiceImpl implements UserRecommendationService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserMatchMapper userMatchMapper;

    @Override
    public List<UserRecommendDTO> getCollaborativeFilteringRecommendations(
            Long userId, Integer gender, Set<Long> excludeUserIds, Integer limit) {
        try {
            // 1. 获取用户的喜欢记录
            List<UserMatch> userLikes = userMatchMapper.selectUserMatches(userId, 1); // 状态1表示已匹配
            
            if (userLikes.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 2. 获取与当前用户有相似喜好的用户
            Set<Long> similarUserIds = new HashSet<>();
            for (UserMatch like : userLikes) {
                // 找到也喜欢这些用户的其他用户
                LambdaQueryWrapper<UserMatch> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.eq(UserMatch::getUserBId, like.getUserBId())
                          .eq(UserMatch::getStatus, 1)
                          .ne(UserMatch::getUserAId, userId);
                
                List<UserMatch> similarMatches = userMatchMapper.selectList(queryWrapper);
                similarUserIds.addAll(similarMatches.stream()
                        .map(UserMatch::getUserAId)
                        .collect(Collectors.toSet()));
            }
            
            if (similarUserIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 3. 获取相似用户喜欢的用户
            Set<Long> recommendedUserIds = new HashSet<>();
            for (Long similarUserId : similarUserIds) {
                LambdaQueryWrapper<UserMatch> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.eq(UserMatch::getUserAId, similarUserId)
                          .eq(UserMatch::getStatus, 1)
                          .notIn(UserMatch::getUserBId, excludeUserIds);
                
                List<UserMatch> recommendations = userMatchMapper.selectList(queryWrapper);
                recommendedUserIds.addAll(recommendations.stream()
                        .map(UserMatch::getUserBId)
                        .collect(Collectors.toSet()));
            }
            
            // 4. 转换为推荐DTO
            List<UserRecommendDTO> result = new ArrayList<>();
            for (Long recommendedUserId : recommendedUserIds) {
                if (result.size() >= limit) {
                    break;
                }
                
                User user = userService.getById(recommendedUserId);
                if (user == null || (gender != null && gender > 0 && !user.getGender().equals(gender))) {
                    continue;
                }
                
                UserProfile profile = userProfileService.getProfileByUserId(recommendedUserId);
                UserRecommendDTO dto = convertToUserRecommendDTO(user, profile);
                dto.setRecommendType("collaborative");
                result.add(dto);
            }
            
            return result;
        } catch (Exception e) {
            log.error("协同过滤推荐失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<UserRecommendDTO> getContentBasedRecommendations(
            Long userId, Integer gender, Integer ageMin, Integer ageMax,
            String location, String interests, Set<Long> excludeUserIds, Integer limit) {
        try {
            // 1. 获取当前用户的资料
            UserProfile userProfile = userProfileService.getProfileByUserId(userId);
            if (userProfile == null) {
                return Collections.emptyList();
            }
            
            // 2. 构建查询条件
            LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(User::getStatus, 1)
                       .notIn(User::getUserId, excludeUserIds);
            
            if (gender != null && gender > 0) {
                queryWrapper.eq(User::getGender, gender);
            }
            
            // 3. 获取候选用户列表
            List<User> candidates = userService.list(queryWrapper);
            if (candidates.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 4. 计算内容相似度并排序
            List<UserRecommendDTO> result = new ArrayList<>();
            for (User candidate : candidates) {
                UserProfile candidateProfile = userProfileService.getProfileByUserId(candidate.getUserId());
                if (candidateProfile == null) {
                    continue;
                }
                
                // 计算内容相似度
                int similarity = calculateContentSimilarity(userProfile, candidateProfile);
                
                // 转换为DTO
                UserRecommendDTO dto = convertToUserRecommendDTO(candidate, candidateProfile);
                dto.setSimilarity(similarity);
                dto.setRecommendType("content");
                result.add(dto);
            }
            
            // 5. 按相似度排序并返回指定数量
            return result.stream()
                    .sorted(Comparator.comparing(UserRecommendDTO::getSimilarity).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("基于内容的推荐失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<UserRecommendDTO> getPopularUserRecommendations(
            Long userId, Integer gender, Set<Long> excludeUserIds, Integer limit) {
        try {
            // 1. 获取最近一周内被喜欢次数最多的用户
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            Date oneWeekAgo = calendar.getTime();
            
            // 2. 统计被喜欢次数
            List<Map<String, Object>> popularUsers = userMatchMapper.selectPopularUsers(oneWeekAgo);
            
            // 3. 过滤并转换为推荐DTO
            List<UserRecommendDTO> result = new ArrayList<>();
            for (Map<String, Object> popularUser : popularUsers) {
                if (result.size() >= limit) {
                    break;
                }
                
                Long popularUserId = Long.valueOf(popularUser.get("user_id").toString());
                if (excludeUserIds.contains(popularUserId)) {
                    continue;
                }
                
                User user = userService.getById(popularUserId);
                if (user == null || (gender != null && gender > 0 && !user.getGender().equals(gender))) {
                    continue;
                }
                
                UserProfile profile = userProfileService.getProfileByUserId(popularUserId);
                UserRecommendDTO dto = convertToUserRecommendDTO(user, profile);
                dto.setRecommendType("popular");
                
                // 设置热度分数
                int likeCount = Integer.parseInt(popularUser.get("like_count").toString());
                dto.setSimilarity((int)(Math.min(likeCount * 10, 100))); // 将喜欢数转换为0-100的分数
                
                result.add(dto);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("热门用户推荐失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 计算内容相似度
     */
    private int calculateContentSimilarity(UserProfile profileA, UserProfile profileB) {
        int similarity = 0;
        int totalWeight = 0;
        
        // 1. 兴趣爱好匹配 (权重 40)
        if (StringUtils.hasText(profileA.getHobbies()) && StringUtils.hasText(profileB.getHobbies())) {
            totalWeight += 40;
            List<String> hobbiesA = Arrays.asList(profileA.getHobbies().split(",|、|;"));
            List<String> hobbiesB = Arrays.asList(profileB.getHobbies().split(",|、|;"));
            
            int commonHobbies = 0;
            for (String hobbyA : hobbiesA) {
                for (String hobbyB : hobbiesB) {
                    if (hobbyA.trim().equals(hobbyB.trim())) {
                        commonHobbies++;
                    }
                }
            }
            
            similarity += (commonHobbies * 40) / Math.max(hobbiesA.size(), hobbiesB.size());
        }
        
        // 2. 地理位置匹配 (权重 30)
        if (StringUtils.hasText(profileA.getLocation()) && StringUtils.hasText(profileB.getLocation())) {
            totalWeight += 30;
            if (profileA.getLocation().equals(profileB.getLocation())) {
                similarity += 30;
            } else if (extractProvince(profileA.getLocation()).equals(extractProvince(profileB.getLocation()))) {
                similarity += 15;
            }
        }
        
        // 3. 职业领域匹配 (权重 20)
        if (StringUtils.hasText(profileA.getOccupation()) && StringUtils.hasText(profileB.getOccupation())) {
            totalWeight += 20;
            if (profileA.getOccupation().equals(profileB.getOccupation())) {
                similarity += 20;
            } else if (profileA.getOccupation().contains(profileB.getOccupation()) || 
                      profileB.getOccupation().contains(profileA.getOccupation())) {
                similarity += 10;
            }
        }
        
        // 4. 教育程度匹配 (权重 10)
        if (profileA.getEducation() != null && profileB.getEducation() != null) {
            totalWeight += 10;
            if (profileA.getEducation().equals(profileB.getEducation())) {
                similarity += 10;
            } else if (Math.abs(profileA.getEducation() - profileB.getEducation()) == 1) {
                similarity += 5;
            }
        }
        
        return totalWeight > 0 ? (similarity * 100) / totalWeight : 50;
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
            Calendar birth = Calendar.getInstance();
            birth.setTime(user.getBirthday());
            Calendar today = Calendar.getInstance();
            
            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
                 today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
                age--;
            }
            dto.setAge(Math.max(0, age));
        }
        
        // 设置资料信息
        if (profile != null) {
            dto.setNickname(profile.getNickname());
            dto.setLocation(profile.getLocation());
            dto.setOccupation(profile.getOccupation());
            dto.setSelfIntro(profile.getSelfIntro());
            dto.setHobbies(profile.getHobbies());
        }
        
        return dto;
    }
} 