package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import com.soical.server.dto.PostDTO;
import com.soical.server.entity.Post;
import com.soical.server.entity.PostLike;
import com.soical.server.entity.User;
import com.soical.server.entity.UserProfile;
import com.soical.server.mapper.PostMapper;
import com.soical.server.service.PostLikeService;
import com.soical.server.service.PostService;
import com.soical.server.service.UserFollowService;
import com.soical.server.service.UserProfileService;
import com.soical.server.service.UserService;
import com.soical.server.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Map;

/**
 * 动态服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final PostLikeService postLikeService;
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;
    private final PostMapper postMapper;

    // 使用懒加载方式获取UserFollowService，避免循环依赖
    private UserFollowService getUserFollowService() {
        return applicationContext.getBean(UserFollowService.class);
    }

    @Override
    public Long createPost(Long userId, String content, List<MultipartFile> images) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 上传图片
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            imageUrls = images.stream()
                    .map(FileUtil::uploadImage)
                    .collect(Collectors.toList());
        }

        // 获取用户所在城市
        String city = "";
        UserProfile profile = userProfileService.getUserProfile(userId);
        if (profile != null && profile.getCity() != null) {
            city = profile.getCity();
        }

        // 创建动态
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setCity(city);
        try {
            post.setImages(objectMapper.writeValueAsString(imageUrls));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "图片处理失败");
        }
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        post.setIsShared(false);
        post.setStatus(1);
        post.setPostType(imageUrls.isEmpty() ? 0 : 1); // 判断动态类型：0-普通文本，1-图文
        post.setHotScore(10.0); // 初始热度评分

        // 保存动态
        boolean saved = save(post);
        if (!saved) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "动态发布失败");
        }

        return post.getPostId();
    }

    @Override
    public Long sharePost(Long userId, Long originalPostId, String content) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证原始动态是否存在
        Post originalPost = getById(originalPostId);
        if (originalPost == null || originalPost.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "原始动态不存在或已删除");
        }

        // 获取用户所在城市
        String city = "";
        UserProfile profile = userProfileService.getUserProfile(userId);
        if (profile != null && profile.getCity() != null) {
            city = profile.getCity();
        }

        // 创建分享动态
        Post sharedPost = new Post();
        sharedPost.setUserId(userId);
        sharedPost.setContent(content);
        sharedPost.setCity(city);
        sharedPost.setLikeCount(0);
        sharedPost.setCommentCount(0);
        sharedPost.setShareCount(0);
        sharedPost.setIsShared(true);
        sharedPost.setOriginalPostId(originalPostId);
        sharedPost.setStatus(1);
        sharedPost.setPostType(3); // 3-分享类型
        sharedPost.setHotScore(8.0); // 初始热度评分低于原创

        // 保存分享动态
        boolean saved = save(sharedPost);
        if (!saved) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "动态分享失败");
        }

        // 增加原始动态的分享数
        getBaseMapper().incrementShareCount(originalPostId);
        
        // 更新原始动态的热度
        updateHotScore(originalPost);

        return sharedPost.getPostId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getPostList(Integer page, Integer pageSize) {
        // 分页查询
        Page<Post> postPage = new Page<>(page, pageSize);
        Page<Post> postResult = baseMapper.selectPostsWithUser(postPage);

        // 转换为DTO
        return convertToPostDTOPage(postResult);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getUserPostList(Long userId, Integer page, Integer pageSize) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 分页查询
        Page<Post> postPage = new Page<>(page, pageSize);
        Page<Post> postResult = baseMapper.selectUserPosts(postPage, userId);

        // 转换为DTO
        return convertToPostDTOPage(postResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDTO getPostDetail(Long postId) {
        // 查询动态
        Post post = getById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "动态不存在或已删除");
        }

        // 转换为DTO
        return convertToPostDTO(post);
    }

    @Override
    public boolean deletePost(Long userId, Long postId) {
        // 查询动态
        Post post = getById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "动态不存在");
        }

        // 验证权限
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除该动态");
        }

        // 逻辑删除
        post.setStatus(0);
        return updateById(post);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getFollowedPostList(Long userId, Integer page, Integer pageSize) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 获取关注的用户ID列表
        List<Long> followingIds = getUserFollowService().getFollowingIds(userId);
        if (followingIds.isEmpty()) {
            return new Page<>(page, pageSize);
        }
        
        // 查询关注用户的动态
        Page<Post> postPage = new Page<>(page, pageSize);
        Page<Post> postResult = baseMapper.selectFollowedPosts(postPage, followingIds);
        
        // 转换为DTO
        Page<PostDTO> dtoPage = convertToPostDTOPage(postResult);
        
        // 填充点赞状态
        fillLikeStatus(dtoPage.getRecords(), userId);
        
        return dtoPage;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getRecommendedPostList(Long userId, Integer page, Integer pageSize) {
        // 查询推荐动态（按热度排序）
        Page<Post> postPage = new Page<>(page, pageSize);
        Page<Post> postResult = baseMapper.selectRecommendedPosts(postPage);
        
        // 转换为DTO
        Page<PostDTO> dtoPage = convertToPostDTOPage(postResult);
        
        // 如果用户已登录，填充是否已点赞的信息
        if (userId != null) {
            fillLikeStatus(dtoPage.getRecords(), userId);
        }
        
        return dtoPage;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getCityPostList(Long userId, String city, Integer page, Integer pageSize) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 如果未提供城市，从用户资料中获取
        if (city == null || city.isEmpty()) {
            UserProfile userProfile = userProfileService.getUserProfile(userId);
            if (userProfile != null && userProfile.getCity() != null && !userProfile.getCity().isEmpty()) {
                city = userProfile.getCity();
            } else {
                // 如果用户资料中也没有城市信息，则返回空结果
                return new Page<>(page, pageSize);
            }
        }
        
        // 查询同城动态
        Page<Post> postPage = new Page<>(page, pageSize);
        Page<Post> postResult = baseMapper.selectCityPosts(postPage, city);
        
        // 转换为DTO
        Page<PostDTO> dtoPage = convertToPostDTOPage(postResult);
        
        // 填充点赞状态
        fillLikeStatus(dtoPage.getRecords(), userId);
        
        return dtoPage;
    }
    
    /**
     * 计算并更新动态热度评分
     * 热度评分公式：(点赞数 * 2 + 评论数 * 3 + 分享数 * 5) / (当前时间 - 发布时间的小时数 + 2)^1.2
     *
     * @param post 动态对象
     */
    private void updateHotScore(Post post) {
        if (post == null) {
            return;
        }
        
        // 获取发布时间
        LocalDateTime createTime = post.getCreateTime();
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        
        // 计算发布时间到现在经过的小时数
        long currentTimeMillis = System.currentTimeMillis();
        long createTimeMillis = createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        double hoursElapsed = (double) (currentTimeMillis - createTimeMillis) / (1000 * 60 * 60) + 2; // 加2避免新发布的动态分母太小
        
        // 计算热度分数
        int likeWeight = 2;
        int commentWeight = 3;
        int shareWeight = 5;
        
        double score = (post.getLikeCount() * likeWeight + post.getCommentCount() * commentWeight + post.getShareCount() * shareWeight)
                     / Math.pow(hoursElapsed, 1.2);
        
        // 更新热度评分
        baseMapper.updateHotScore(post.getPostId(), score);
    }

    /**
     * 将Post对象转换为PostDTO
     */
    private PostDTO convertToPostDTO(Post post) {
        PostDTO postDTO = new PostDTO();
        BeanUtils.copyProperties(post, postDTO);

        // 获取用户信息
        User user = userService.getById(post.getUserId());
        if (user != null) {
            postDTO.setAvatar(user.getAvatar());
            
            // 获取用户资料
            UserProfile userProfile = userProfileService.getUserProfile(post.getUserId());
            if (userProfile != null) {
                postDTO.setNickname(userProfile.getNickname());
            }
        }

        // 处理图片列表
        try {
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                List<String> images = objectMapper.readValue(post.getImages(), new TypeReference<List<String>>() {});
                postDTO.setImages(images);
            }
        } catch (JsonProcessingException e) {
            postDTO.setImages(new ArrayList<>());
        }

        // 如果是分享的动态，获取原始动态信息
        if (post.getIsShared() && post.getOriginalPostId() != null) {
            Post originalPost = getById(post.getOriginalPostId());
            if (originalPost != null && originalPost.getStatus() == 1) {
                postDTO.setOriginalPost(convertToPostDTO(originalPost));
            }
        }
        
        // 设置地区信息
        postDTO.setCity(post.getCity());
        
        return postDTO;
    }

    /**
     * 将Post分页对象转换为PostDTO分页对象
     */
    private Page<PostDTO> convertToPostDTOPage(Page<Post> postPage) {
        Page<PostDTO> postDTOPage = new Page<>();
        BeanUtils.copyProperties(postPage, postDTOPage, "records");

        List<PostDTO> postDTOList = postPage.getRecords().stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());

        postDTOPage.setRecords(postDTOList);
        return postDTOPage;
    }
    
    /**
     * 填充动态点赞状态
     */
    private void fillLikeStatus(List<PostDTO> postDTOs, Long userId) {
        if (postDTOs.isEmpty() || userId == null) {
            return;
        }
        
        // 获取动态ID列表
        List<Long> postIds = postDTOs.stream()
                                     .map(PostDTO::getPostId)
                                     .collect(Collectors.toList());
        
        // 查询用户点赞的动态列表
        List<Long> likedPostIds = new ArrayList<>();
        for (Long postId : postIds) {
            if (postLikeService.hasLiked(userId, postId)) {
                likedPostIds.add(postId);
            }
        }
        
        // 创建一个不可变集合而不是使用Set.copyOf
        Set<Long> likedPostIdSet = new HashSet<>(likedPostIds);
        
        // 设置点赞状态
        for (PostDTO postDTO : postDTOs) {
            postDTO.setLiked(likedPostIdSet.contains(postDTO.getPostId()));
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getCommunityPosts(Long userId, Integer page, Integer pageSize) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 创建分页对象
        Page<Post> postPage = new Page<>(page, pageSize);
        
        // 查询社区动态 - 综合推荐和关注用户的动态
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getStatus, 1) // 只查询状态正常的动态
                   .orderByDesc(Post::getCreateTime); // 按创建时间降序
        
        Page<Post> postResult = page(postPage, queryWrapper);
        
        // 转换为DTO
        Page<PostDTO> dtoPage = convertToPostDTOPage(postResult);
        
        // 填充点赞状态
        fillLikeStatus(dtoPage.getRecords(), userId);
        
        return dtoPage;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getHotPosts(Long userId, Integer page, Integer pageSize) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 创建分页对象
        Page<Post> postPage = new Page<>(page, pageSize);
        
        // 查询热门动态 - 按热度评分降序
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getStatus, 1) // 只查询状态正常的动态
                   .orderByDesc(Post::getHotScore); // 按热度评分降序
        
        Page<Post> postResult = page(postPage, queryWrapper);
        
        // 转换为DTO
        Page<PostDTO> dtoPage = convertToPostDTOPage(postResult);
        
        // 填充点赞状态
        fillLikeStatus(dtoPage.getRecords(), userId);
        
        return dtoPage;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getNearbyPosts(String city, Integer page, Integer pageSize) {
        if (city == null || city.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "城市不能为空");
        }
        
        // 创建分页对象
        Page<Post> postPage = new Page<>(page, pageSize);
        
        // 查询同城动态
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getStatus, 1) // 只查询状态正常的动态
                   .eq(Post::getCity, city) // 按城市筛选
                   .orderByDesc(Post::getCreateTime); // 按创建时间降序
        
        Page<Post> postResult = page(postPage, queryWrapper);
        
        // 转换为DTO
        return convertToPostDTOPage(postResult);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getNearbyPosts(Long userId, Integer page, Integer pageSize, String city) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 如果未提供城市，从用户资料中获取
        if (city == null || city.isEmpty()) {
            UserProfile userProfile = userProfileService.getUserProfile(userId);
            if (userProfile != null && userProfile.getCity() != null && !userProfile.getCity().isEmpty()) {
                city = userProfile.getCity();
            } else {
                // 如果用户资料中也没有城市信息，则返回空结果
                return new Page<>(page, pageSize);
            }
        }
        
        // 创建分页对象
        Page<Post> postPage = new Page<>(page, pageSize);
        
        // 查询同城动态
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getStatus, 1) // 只查询状态正常的动态
                   .eq(Post::getCity, city) // 按城市筛选
                   .orderByDesc(Post::getCreateTime); // 按创建时间降序
        
        Page<Post> postResult = page(postPage, queryWrapper);
        
        // 转换为DTO
        Page<PostDTO> dtoPage = convertToPostDTOPage(postResult);
        
        // 填充点赞状态
        fillLikeStatus(dtoPage.getRecords(), userId);
        
        return dtoPage;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getFollowedPosts(Long userId, Integer page, Integer pageSize) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 获取关注的用户ID列表
        List<Long> followingIds = getUserFollowService().getFollowingIds(userId);
        if (followingIds.isEmpty()) {
            return new Page<>(page, pageSize);
        }
        
        // 创建分页对象
        Page<Post> postPage = new Page<>(page, pageSize);
        
        // 查询关注用户的动态
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getStatus, 1) // 只查询状态正常的动态
                   .in(Post::getUserId, followingIds) // 仅查询关注用户的动态
                   .orderByDesc(Post::getCreateTime); // 按创建时间降序
        
        Page<Post> postResult = page(postPage, queryWrapper);
        
        // 转换为DTO
        Page<PostDTO> dtoPage = convertToPostDTOPage(postResult);
        
        // 填充点赞状态
        fillLikeStatus(dtoPage.getRecords(), userId);
        
        return dtoPage;
    }
    
    @Override
    public boolean likePost(Long userId, Long postId) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 验证动态是否存在
        Post post = getById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "动态不存在或已删除");
        }
        
        // 检查是否已点赞
        if (postLikeService.hasLiked(userId, postId)) {
            return true; // 已点赞，不再重复操作
        }
        
        // 创建点赞记录
        PostLike postLike = new PostLike();
        postLike.setUserId(userId);
        postLike.setPostId(postId);
        postLike.setCreateTime(new Date());
        boolean saved = postLikeService.save(postLike);
        
        if (saved) {
            // 更新动态点赞数
            baseMapper.incrementLikeCount(postId);
            
            // 更新热度评分
            updateHotScore(post);
        }
        
        return saved;
    }
    
    @Override
    public boolean unlikePost(Long userId, Long postId) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 验证动态是否存在
        Post post = getById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "动态不存在或已删除");
        }
        
        // 检查是否已点赞
        if (!postLikeService.hasLiked(userId, postId)) {
            return true; // 未点赞，不需要操作
        }
        
        // 删除点赞记录
        boolean removed = postLikeService.remove(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getUserId, userId)
                        .eq(PostLike::getPostId, postId)
        );
        
        if (removed) {
            // 更新动态点赞数
            baseMapper.decrementLikeCount(postId);
            
            // 更新热度评分
            updateHotScore(post);
        }
        
        return removed;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countUserPosts(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getUserId, userId)
                   .eq(Post::getStatus, 1); // 只统计状态正常的动态
        
        return Math.toIntExact(count(queryWrapper));
    }

    @Override
    public Page<Post> getPostList(Integer page, Integer pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Post::getContent, keyword);
        }
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        wrapper.orderByDesc(Post::getCreateTime);
        return postMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    // 添加日期转换方法
    private Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // 重写父类方法，确保返回类型一致
    @Override
    public boolean save(Post entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        if (entity.getUpdateTime() == null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(Post entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return super.updateById(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getLikedPosts(Long userId, Integer page, Integer pageSize) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 创建分页对象
        Page<PostLike> likePage = new Page<>(page, pageSize);
        
        // 查询用户点赞记录
        LambdaQueryWrapper<PostLike> likeQueryWrapper = new LambdaQueryWrapper<>();
        likeQueryWrapper.eq(PostLike::getUserId, userId)
                        .orderByDesc(PostLike::getCreateTime);
        
        Page<PostLike> likeResult = postLikeService.page(likePage, likeQueryWrapper);
        
        // 获取点赞帖子的ID列表
        List<Long> postIds = likeResult.getRecords().stream()
                            .map(PostLike::getPostId)
                            .collect(Collectors.toList());
        
        if (postIds.isEmpty()) {
            // 如果没有点赞记录，直接返回空页
            return new Page<>(page, pageSize);
        }
        
        // 查询这些帖子的详细信息
        LambdaQueryWrapper<Post> postQueryWrapper = new LambdaQueryWrapper<>();
        postQueryWrapper.in(Post::getPostId, postIds)
                       .eq(Post::getStatus, 1) // 只查询状态正常的动态
                       .orderByDesc(Post::getCreateTime);
        
        List<Post> posts = list(postQueryWrapper);
        
        // 按照点赞记录的顺序排序
        Map<Long, Post> postMap = posts.stream()
                                      .collect(Collectors.toMap(Post::getPostId, post -> post));
        
        List<Post> orderedPosts = postIds.stream()
                                       .filter(postMap::containsKey)
                                       .map(postMap::get)
                                       .collect(Collectors.toList());
        
        // 创建结果页对象
        Page<Post> postResult = new Page<>(page, pageSize, likeResult.getTotal());
        postResult.setRecords(orderedPosts);
        
        // 转换为DTO
        Page<PostDTO> dtoPage = convertToPostDTOPage(postResult);
        
        // 所有帖子都是已点赞状态
        dtoPage.getRecords().forEach(dto -> dto.setLiked(true));
        
        return dtoPage;
    }
} 