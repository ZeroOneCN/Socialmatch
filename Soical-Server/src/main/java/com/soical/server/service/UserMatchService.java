package com.soical.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.dto.UserRecommendDTO;
import com.soical.server.entity.UserMatch;

import java.util.List;

/**
 * 用户匹配服务接口
 */
public interface UserMatchService extends IService<UserMatch> {

    /**
     * 获取推荐的用户
     * @param userId 当前用户ID
     * @param gender 性别偏好
     * @param ageMin 最小年龄
     * @param ageMax 最大年龄
     * @param location 位置
     * @param interests 兴趣标签，多个以逗号分隔
     * @param limit 限制返回数量
     * @return 推荐用户列表
     */
    List<UserRecommendDTO> getRecommendedUsers(Long userId, Integer gender, Integer ageMin, Integer ageMax, 
                                              String location, String interests, Integer limit);

    /**
     * 喜欢用户
     * 
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 是否匹配成功（两人互相喜欢）
     */
    boolean likeUser(Long userId, Long targetUserId);

    /**
     * 不喜欢用户
     * 
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 操作是否成功
     */
    boolean dislikeUser(Long userId, Long targetUserId);

    /**
     * 获取用户的匹配列表
     * 
     * @param userId 用户ID
     * @param status 状态筛选条件（可选）
     * @return 匹配列表
     */
    List<UserMatch> getUserMatches(Long userId, Integer status);

    /**
     * 根据ID获取匹配记录
     * 
     * @param matchId 匹配ID
     * @return 匹配记录
     */
    UserMatch getMatchById(Long matchId);

    /**
     * 获取两个用户之间的匹配关系
     * 
     * @param userAId 用户A ID
     * @param userBId 用户B ID
     * @return 匹配关系，如果不存在则返回null
     */
    UserMatch getMatchBetweenUsers(Long userAId, Long userBId);

    /**
     * 更新匹配状态
     * 
     * @param matchId 匹配ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateMatchStatus(Long matchId, Integer status);

    /**
     * 计算用户之间的相似度（基于兴趣标签等）
     * 
     * @param userAId 用户A ID
     * @param userBId 用户B ID
     * @return 相似度百分比（0-100）
     */
    int calculateSimilarity(Long userAId, Long userBId);

    /**
     * 获取当前用户喜欢的用户列表
     */
    List<UserRecommendDTO> getLikesGiven(Long userId);
    
    /**
     * 获取喜欢当前用户的用户列表
     */
    List<UserRecommendDTO> getLikesReceived(Long userId);

    /**
     * 统计用户的总喜欢数
     * 
     * @param userId 用户ID
     * @return 喜欢的用户数量
     */
    Integer countUserLikes(Long userId);

    /**
     * 统计用户的匹配数量
     * 
     * @param userId 用户ID
     * @param status 匹配状态（可选）
     * @return 匹配数量
     */
    Integer countUserMatches(Long userId, Integer status);

    /**
     * 统计用户已接触过的潜在匹配对象总数
     * 包括：推荐给用户的、用户已喜欢的、已匹配的等所有接触过的用户
     * 
     * @param userId 用户ID
     * @return 潜在匹配对象总数
     */
    Integer countPotentialMatches(Long userId);
} 