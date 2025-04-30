package com.soical.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.dto.PostDTO;
import com.soical.server.entity.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 动态服务接口
 */
public interface PostService extends IService<Post> {
    
    /**
     * 发布动态
     *
     * @param userId  用户ID
     * @param content 内容
     * @param images  图片列表
     * @return 动态ID
     */
    Long createPost(Long userId, String content, List<MultipartFile> images);
    
    /**
     * 分享动态
     *
     * @param userId       分享者ID
     * @param originalPostId 原始动态ID
     * @param content      分享内容
     * @return 分享后的动态ID
     */
    Long sharePost(Long userId, Long originalPostId, String content);
    
    /**
     * 获取动态列表（分页）
     *
     * @param page     页码
     * @param pageSize 每页条数
     * @return 动态列表
     */
    Page<PostDTO> getPostList(Integer page, Integer pageSize);
    
    /**
     * 获取用户动态列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 动态列表
     */
    Page<PostDTO> getUserPostList(Long userId, Integer page, Integer pageSize);
    
    /**
     * 获取动态详情
     *
     * @param postId 动态ID
     * @return 动态详情
     */
    PostDTO getPostDetail(Long postId);
    
    /**
     * 删除动态
     *
     * @param userId 用户ID
     * @param postId 动态ID
     * @return 是否成功
     */
    boolean deletePost(Long userId, Long postId);
    
    /**
     * 获取关注的用户动态列表
     *
     * @param userId    当前用户ID
     * @param page      页码
     * @param pageSize  每页条数
     * @return 关注用户的动态列表
     */
    Page<PostDTO> getFollowedPostList(Long userId, Integer page, Integer pageSize);
    
    /**
     * 获取推荐动态列表
     * 根据热度、点赞数、评论数等因素推荐
     *
     * @param userId    当前用户ID (可选，用于个性化推荐)
     * @param page      页码
     * @param pageSize  每页条数
     * @return 推荐动态列表
     */
    Page<PostDTO> getRecommendedPostList(Long userId, Integer page, Integer pageSize);
    
    /**
     * 获取同城动态列表
     *
     * @param userId    当前用户ID
     * @param city      城市名称 (若为null，则使用用户资料中的城市)
     * @param page      页码
     * @param pageSize  每页条数
     * @return 同城动态列表
     */
    Page<PostDTO> getCityPostList(Long userId, String city, Integer page, Integer pageSize);
    
    /**
     * 获取社区动态列表
     * 包括推荐动态和关注用户的动态
     *
     * @param userId    当前用户ID
     * @param page      页码
     * @param pageSize  每页条数
     * @return 社区动态列表
     */
    Page<PostDTO> getCommunityPosts(Long userId, Integer page, Integer pageSize);
    
    /**
     * 获取热门动态
     * 按照热度评分排序
     *
     * @param userId    当前用户ID (用于检查点赞状态)
     * @param page      页码
     * @param pageSize  每页条数
     * @return 热门动态列表
     */
    Page<PostDTO> getHotPosts(Long userId, Integer page, Integer pageSize);
    
    /**
     * 获取同城动态
     *
     * @param city      城市名称
     * @param page      页码
     * @param pageSize  每页条数
     * @return 同城动态列表
     */
    Page<PostDTO> getNearbyPosts(String city, Integer page, Integer pageSize);
    
    /**
     * 获取附近动态
     *
     * @param userId    当前用户ID
     * @param page      页码
     * @param pageSize  每页条数
     * @param city      城市名称 (若为null，则使用用户资料中的城市)
     * @return 附近动态列表
     */
    Page<PostDTO> getNearbyPosts(Long userId, Integer page, Integer pageSize, String city);
    
    /**
     * 获取关注用户的动态
     *
     * @param userId    当前用户ID
     * @param page      页码
     * @param pageSize  每页条数
     * @return 关注用户的动态列表
     */
    Page<PostDTO> getFollowedPosts(Long userId, Integer page, Integer pageSize);
    
    /**
     * 点赞动态
     *
     * @param userId  用户ID
     * @param postId  动态ID
     * @return 操作是否成功
     */
    boolean likePost(Long userId, Long postId);
    
    /**
     * 取消点赞动态
     *
     * @param userId  用户ID
     * @param postId  动态ID
     * @return 操作是否成功
     */
    boolean unlikePost(Long userId, Long postId);
    
    /**
     * 计算用户的动态数量
     *
     * @param userId 用户ID
     * @return 动态数量
     */
    Integer countUserPosts(Long userId);

    /**
     * 获取帖子列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @param status   状态
     * @return 帖子分页列表
     */
    Page<Post> getPostList(Integer page, Integer pageSize, String keyword, Integer status);

    /**
     * 获取用户点赞的动态列表
     *
     * @param userId    用户ID
     * @param page      页码
     * @param pageSize  每页条数
     * @return 用户点赞的动态列表
     */
    Page<PostDTO> getLikedPosts(Long userId, Integer page, Integer pageSize);
} 