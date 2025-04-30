package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import com.soical.server.dto.UserProfileDTO;
import com.soical.server.entity.User;
import com.soical.server.entity.UserFollow;
import com.soical.server.entity.UserProfile;
import com.soical.server.mapper.UserFollowMapper;
import com.soical.server.service.UserFollowService;
import com.soical.server.service.UserProfileService;
import com.soical.server.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户关注服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {

    private final UserService userService;
    private ApplicationContext applicationContext;
    
    // 使用懒加载方式获取UserProfileService，避免循环依赖
    private UserProfileService getUserProfileService() {
        return applicationContext.getBean(UserProfileService.class);
    }
    
    @Autowired
    public UserFollowServiceImpl(UserService userService, ApplicationContext applicationContext) {
        this.userService = userService;
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean followUser(Long followerId, Long followedId) {
        // 检查参数
        if (followerId == null || followedId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "参数错误");
        }
        
        // 不能关注自己
        if (followerId.equals(followedId)) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "不能关注自己");
        }
        
        // 检查用户是否存在
        User follower = userService.getById(followerId);
        User followed = userService.getById(followedId);
        if (follower == null || followed == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST.getCode(), "用户不存在");
        }
        
        // 检查是否已经关注
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                   .eq(UserFollow::getFollowedId, followedId);
        UserFollow existFollow = getOne(queryWrapper);
        
        if (existFollow != null) {
            // 如果已关注，但状态为取消，则重新关注
            if (existFollow.getStatus() == 0) {
                existFollow.setStatus(1);
                existFollow.setCreateTime(new Date());
                return updateById(existFollow);
            } else {
                // 已关注
                return true;
            }
        }
        
        // 创建新关注关系
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFollowedId(followedId);
        userFollow.setStatus(1);
        userFollow.setCreateTime(new Date());
        
        return save(userFollow);
    }

    @Override
    public boolean unfollowUser(Long followerId, Long followedId) {
        // 检查参数
        if (followerId == null || followedId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "参数错误");
        }
        
        // 检查是否已关注
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                   .eq(UserFollow::getFollowedId, followedId)
                   .eq(UserFollow::getStatus, 1);
        UserFollow userFollow = getOne(queryWrapper);
        
        if (userFollow == null) {
            // 未关注
            return true;
        }
        
        // 更新状态为取消关注
        userFollow.setStatus(0);
        return updateById(userFollow);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDTO> getFollowingList(Long userId, Integer page, Integer pageSize) {
        // 检查参数
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "参数错误");
        }
        
        // 分页查询关注的用户
        Page<User> userPage = new Page<>(page, pageSize);
        Page<User> usersResult = baseMapper.selectFollowingUsersWithInfo(userPage, userId);
        
        // 转换为DTO
        Page<UserProfileDTO> resultPage = new Page<>();
        BeanUtils.copyProperties(usersResult, resultPage, "records");
        
        List<UserProfileDTO> userProfileDTOs = usersResult.getRecords().stream().map(user -> {
            UserProfileDTO dto = new UserProfileDTO();
            BeanUtils.copyProperties(user, dto);
            
            // 获取用户资料
            UserProfile profile = getUserProfileService().getUserProfile(user.getUserId());
            if (profile != null) {
                dto.setNickname(profile.getNickname());
                dto.setAvatar(profile.getAvatar());
                dto.setSelfIntro(profile.getSelfIntro());
                dto.setLocation(profile.getLocation());
                dto.setCity(profile.getCity());
            }
            
            // 获取关注信息
            dto.setFollowerCount(getFollowerCount(user.getUserId()));
            dto.setFollowingCount(getFollowingCount(user.getUserId()));
            dto.setIsFollowing(true); // 必然是已关注的
            
            return dto;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(userProfileDTOs);
        return resultPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDTO> getFollowerList(Long userId, Integer page, Integer pageSize) {
        // 检查参数
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "参数错误");
        }
        
        // 分页查询粉丝列表
        Page<User> userPage = new Page<>(page, pageSize);
        Page<User> usersResult = baseMapper.selectFollowerUsersWithInfo(userPage, userId);
        
        // 转换为DTO
        Page<UserProfileDTO> resultPage = new Page<>();
        BeanUtils.copyProperties(usersResult, resultPage, "records");
        
        // 获取用户关注的ID列表，用于判断是否互关
        List<Long> followingIds = getFollowingIds(userId);
        Set<Long> followingSet = new HashSet<>(followingIds);
        
        List<UserProfileDTO> userProfileDTOs = usersResult.getRecords().stream().map(user -> {
            UserProfileDTO dto = new UserProfileDTO();
            BeanUtils.copyProperties(user, dto);
            
            // 获取用户资料
            UserProfile profile = getUserProfileService().getUserProfile(user.getUserId());
            if (profile != null) {
                dto.setNickname(profile.getNickname());
                dto.setAvatar(profile.getAvatar());
                dto.setSelfIntro(profile.getSelfIntro());
                dto.setLocation(profile.getLocation());
                dto.setCity(profile.getCity());
            }
            
            // 获取关注信息
            dto.setFollowerCount(getFollowerCount(user.getUserId()));
            dto.setFollowingCount(getFollowingCount(user.getUserId()));
            dto.setIsFollowing(followingSet.contains(user.getUserId())); // 判断是否互关
            
            return dto;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(userProfileDTOs);
        return resultPage;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followedId) {
        // 检查参数
        if (followerId == null || followedId == null) {
            return false;
        }
        
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                   .eq(UserFollow::getFollowedId, followedId)
                   .eq(UserFollow::getStatus, 1);
        
        return count(queryWrapper) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getFollowingIds(Long userId) {
        // 检查参数
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return baseMapper.selectFollowingIds(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getFollowerCount(Long userId) {
        // 检查参数
        if (userId == null) {
            return 0;
        }
        
        Integer count = baseMapper.countFollowers(userId);
        return count != null ? count : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getFollowingCount(Long userId) {
        // 检查参数
        if (userId == null) {
            return 0;
        }
        
        Integer count = baseMapper.countFollowing(userId);
        return count != null ? count : 0;
    }
} 