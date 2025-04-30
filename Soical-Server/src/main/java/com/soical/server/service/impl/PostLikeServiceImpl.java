package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import com.soical.server.entity.Post;
import com.soical.server.entity.PostLike;
import com.soical.server.entity.User;
import com.soical.server.mapper.PostLikeMapper;
import com.soical.server.service.PostLikeService;
import com.soical.server.service.PostService;
import com.soical.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 动态点赞服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements PostLikeService {

    private final UserService userService;
    private final ApplicationContext applicationContext;
    
    // 使用懒加载方式获取PostService，避免循环依赖
    private PostService getPostService() {
        return applicationContext.getBean(PostService.class);
    }
    
    @Autowired
    public PostLikeServiceImpl(UserService userService, 
                             ApplicationContext applicationContext) {
        this.userService = userService;
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean likePost(Long userId, Long postId) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证动态是否存在
        Post post = getPostService().getById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "动态不存在或已删除");
        }

        // 判断是否已点赞
        if (hasLiked(userId, postId)) {
            return true;
        }

        // 创建点赞记录
        PostLike postLike = new PostLike();
        postLike.setUserId(userId);
        postLike.setPostId(postId);

        // 保存点赞记录
        boolean saved = save(postLike);
        if (saved) {
            // 更新动态点赞数
            post.setLikeCount(post.getLikeCount() + 1);
            getPostService().updateById(post);
        }

        return saved;
    }

    @Override
    public boolean unlikePost(Long userId, Long postId) {
        // 查询点赞记录
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId, postId);
        PostLike postLike = getOne(wrapper);
        
        if (postLike == null) {
            return true;
        }

        // 删除点赞记录
        boolean removed = removeById(postLike.getLikeId());
        if (removed) {
            // 更新动态点赞数
            Post post = getPostService().getById(postId);
            if (post != null) {
                post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                getPostService().updateById(post);
            }
        }

        return removed;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasLiked(Long userId, Long postId) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId, postId);
        return count(wrapper) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getLikeUserIds(Long postId) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getPostId, postId)
                .select(PostLike::getUserId);
        
        return list(wrapper).stream()
                .map(PostLike::getUserId)
                .collect(Collectors.toList());
    }
} 