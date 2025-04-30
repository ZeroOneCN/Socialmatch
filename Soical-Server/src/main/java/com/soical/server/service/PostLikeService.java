package com.soical.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.entity.PostLike;

import java.util.List;

/**
 * 动态点赞服务接口
 */
public interface PostLikeService extends IService<PostLike> {
    
    /**
     * 点赞动态
     *
     * @param userId 用户ID
     * @param postId 动态ID
     * @return 是否成功
     */
    boolean likePost(Long userId, Long postId);
    
    /**
     * 取消点赞
     *
     * @param userId 用户ID
     * @param postId 动态ID
     * @return 是否成功
     */
    boolean unlikePost(Long userId, Long postId);
    
    /**
     * 判断用户是否点赞
     *
     * @param userId 用户ID
     * @param postId 动态ID
     * @return 是否已点赞
     */
    boolean hasLiked(Long userId, Long postId);
    
    /**
     * 获取动态点赞用户ID列表
     *
     * @param postId 动态ID
     * @return 用户ID列表
     */
    List<Long> getLikeUserIds(Long postId);
} 