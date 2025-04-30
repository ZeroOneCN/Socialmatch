package com.soical.server.service;

import com.soical.server.dto.UserRecommendDTO;
import java.util.List;
import java.util.Set;

/**
 * 用户推荐服务接口
 */
public interface UserRecommendationService {

    /**
     * 基于协同过滤的推荐
     *
     * @param userId 用户ID
     * @param gender 性别偏好
     * @param excludeUserIds 需要排除的用户ID集合
     * @param limit 返回数量限制
     * @return 推荐用户列表
     */
    List<UserRecommendDTO> getCollaborativeFilteringRecommendations(
            Long userId, Integer gender, Set<Long> excludeUserIds, Integer limit);

    /**
     * 基于内容的推荐
     *
     * @param userId 用户ID
     * @param gender 性别偏好
     * @param ageMin 最小年龄
     * @param ageMax 最大年龄
     * @param location 位置偏好
     * @param interests 兴趣爱好
     * @param excludeUserIds 需要排除的用户ID集合
     * @param limit 返回数量限制
     * @return 推荐用户列表
     */
    List<UserRecommendDTO> getContentBasedRecommendations(
            Long userId, Integer gender, Integer ageMin, Integer ageMax,
            String location, String interests, Set<Long> excludeUserIds, Integer limit);

    /**
     * 获取热门用户推荐
     *
     * @param userId 用户ID
     * @param gender 性别偏好
     * @param excludeUserIds 需要排除的用户ID集合
     * @param limit 返回数量限制
     * @return 推荐用户列表
     */
    List<UserRecommendDTO> getPopularUserRecommendations(
            Long userId, Integer gender, Set<Long> excludeUserIds, Integer limit);
} 