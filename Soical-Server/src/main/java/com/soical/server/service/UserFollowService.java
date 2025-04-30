package com.soical.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.dto.UserProfileDTO;
import com.soical.server.entity.UserFollow;

import java.util.List;

/**
 * 用户关注服务接口
 */
public interface UserFollowService extends IService<UserFollow> {
    
    /**
     * 关注用户
     *
     * @param followerId 关注者ID（粉丝）
     * @param followedId 被关注者ID
     * @return 是否成功
     */
    boolean followUser(Long followerId, Long followedId);
    
    /**
     * 取消关注
     *
     * @param followerId 关注者ID（粉丝）
     * @param followedId 被关注者ID
     * @return 是否成功
     */
    boolean unfollowUser(Long followerId, Long followedId);
    
    /**
     * 获取用户关注列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 关注的用户列表
     */
    Page<UserProfileDTO> getFollowingList(Long userId, Integer page, Integer pageSize);
    
    /**
     * 获取用户粉丝列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 粉丝用户列表
     */
    Page<UserProfileDTO> getFollowerList(Long userId, Integer page, Integer pageSize);
    
    /**
     * 判断是否已关注
     *
     * @param followerId 关注者ID（粉丝）
     * @param followedId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followedId);
    
    /**
     * 获取用户关注的用户ID列表
     *
     * @param userId 用户ID
     * @return 关注的用户ID列表
     */
    List<Long> getFollowingIds(Long userId);
    
    /**
     * 获取用户的粉丝数量
     *
     * @param userId 用户ID
     * @return 粉丝数量
     */
    Integer getFollowerCount(Long userId);
    
    /**
     * 获取用户的关注数量
     *
     * @param userId 用户ID
     * @return 关注数量
     */
    Integer getFollowingCount(Long userId);
} 