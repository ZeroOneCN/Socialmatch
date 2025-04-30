package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import com.soical.server.dto.UserDetailDTO;
import com.soical.server.entity.User;
import com.soical.server.entity.UserProfile;
import com.soical.server.mapper.CommentMapper;
import com.soical.server.mapper.PostMapper;
import com.soical.server.mapper.UserFollowMapper;
import com.soical.server.mapper.UserMapper;
import com.soical.server.mapper.UserMatchMapper;
import com.soical.server.mapper.UserProfileMapper;
import com.soical.server.service.AdminUserService;
import com.soical.server.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserFollowMapper userFollowMapper;
    private final UserMatchMapper userMatchMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public Page<UserDetailDTO> getUserList(Integer page, Integer pageSize, String keyword, Integer status, String sortBy, String sortOrder) {
        // 创建分页对象
        Page<User> userPage = new Page<>(page, pageSize);
        
        // 创建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getPhone, keyword);
            // 查询用户资料表中昵称匹配的用户ID
            LambdaQueryWrapper<UserProfile> profileWrapper = new LambdaQueryWrapper<>();
            profileWrapper.like(UserProfile::getNickname, keyword);
            List<UserProfile> profiles = userProfileMapper.selectList(profileWrapper);
            
            // 如果有匹配的昵称
            if (!profiles.isEmpty()) {
                List<Long> userIds = profiles.stream().map(UserProfile::getUserId).collect(Collectors.toList());
                queryWrapper.or().in(User::getUserId, userIds);
            }
        }
        
        // 状态过滤
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        
        // 排序
        if (StringUtils.isNotBlank(sortBy)) {
            boolean isAsc = !"desc".equalsIgnoreCase(sortOrder);
            
            switch (sortBy) {
                case "userId":
                    queryWrapper.orderBy(true, isAsc, User::getUserId);
                    break;
                case "createTime":
                    queryWrapper.orderBy(true, isAsc, User::getCreateTime);
                    break;
                case "updateTime":
                    queryWrapper.orderBy(true, isAsc, User::getUpdateTime);
                    break;
                default:
                    queryWrapper.orderBy(true, false, User::getCreateTime);
            }
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(User::getCreateTime);
        }
        
        // 查询用户列表
        userPage = userMapper.selectPage(userPage, queryWrapper);
        
        // 转换为DTO
        Page<UserDetailDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(userPage, dtoPage, "records");
        
        if (userPage.getRecords().isEmpty()) {
            dtoPage.setRecords(new ArrayList<>());
            return dtoPage;
        }
        
        // 获取所有用户ID
        List<Long> userIds = userPage.getRecords().stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        
        // 批量查询用户资料
        LambdaQueryWrapper<UserProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.in(UserProfile::getUserId, userIds);
        List<UserProfile> profiles = userProfileMapper.selectList(profileWrapper);
        Map<Long, UserProfile> profileMap = profiles.stream()
                .collect(Collectors.toMap(UserProfile::getUserId, profile -> profile));
        
        // 转换用户列表
        List<UserDetailDTO> userDetailDTOs = userPage.getRecords().stream().map(user -> {
            UserDetailDTO detailDTO = new UserDetailDTO();
            BeanUtils.copyProperties(user, detailDTO);
            
            // 设置用户资料
            UserProfile profile = profileMap.get(user.getUserId());
            if (profile != null) {
                UserDetailDTO.UserProfileDTO profileDTO = new UserDetailDTO.UserProfileDTO();
                BeanUtils.copyProperties(profile, profileDTO);
                detailDTO.setProfile(profileDTO);
            }
            
            return detailDTO;
        }).collect(Collectors.toList());
        
        dtoPage.setRecords(userDetailDTOs);
        return dtoPage;
    }
    
    @Override
    public UserDetailDTO getUserDetail(Long userId) {
        // 检查用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 查询用户资料
        LambdaQueryWrapper<UserProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(profileWrapper);
        
        // 转换为DTO
        UserDetailDTO detailDTO = new UserDetailDTO();
        BeanUtils.copyProperties(user, detailDTO);
        
        // 设置用户资料
        if (profile != null) {
            UserDetailDTO.UserProfileDTO profileDTO = new UserDetailDTO.UserProfileDTO();
            BeanUtils.copyProperties(profile, profileDTO);
            detailDTO.setProfile(profileDTO);
        }
        
        // 查询统计数据
        UserDetailDTO.UserStatsDTO statsDTO = new UserDetailDTO.UserStatsDTO();
        
        // 动态数量
        LambdaQueryWrapper<com.soical.server.entity.Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(com.soical.server.entity.Post::getUserId, userId);
        postWrapper.eq(com.soical.server.entity.Post::getStatus, 1);
        statsDTO.setPostCount(postMapper.selectCount(postWrapper).intValue());
        
        // 评论数量
        LambdaQueryWrapper<com.soical.server.entity.Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(com.soical.server.entity.Comment::getUserId, userId);
        commentWrapper.eq(com.soical.server.entity.Comment::getStatus, 1);
        statsDTO.setCommentCount(commentMapper.selectCount(commentWrapper).intValue());
        
        // 关注数量
        LambdaQueryWrapper<com.soical.server.entity.UserFollow> followingWrapper = new LambdaQueryWrapper<>();
        followingWrapper.eq(com.soical.server.entity.UserFollow::getFollowerId, userId);
        followingWrapper.eq(com.soical.server.entity.UserFollow::getStatus, 1);
        statsDTO.setFollowingCount(userFollowMapper.selectCount(followingWrapper).intValue());
        
        // 粉丝数量
        LambdaQueryWrapper<com.soical.server.entity.UserFollow> followerWrapper = new LambdaQueryWrapper<>();
        followerWrapper.eq(com.soical.server.entity.UserFollow::getFollowedId, userId);
        followerWrapper.eq(com.soical.server.entity.UserFollow::getStatus, 1);
        statsDTO.setFollowerCount(userFollowMapper.selectCount(followerWrapper).intValue());
        
        // 匹配数量
        LambdaQueryWrapper<com.soical.server.entity.UserMatch> matchWrapper = new LambdaQueryWrapper<>();
        matchWrapper.and(wrapper -> wrapper
                .eq(com.soical.server.entity.UserMatch::getUserAId, userId)
                .or()
                .eq(com.soical.server.entity.UserMatch::getUserBId, userId));
        matchWrapper.eq(com.soical.server.entity.UserMatch::getStatus, 1);
        statsDTO.setMatchCount(userMatchMapper.selectCount(matchWrapper).intValue());
        
        detailDTO.setStats(statsDTO);
        
        return detailDTO;
    }
    
    @Override
    @Transactional
    public boolean updateUserStatus(Long userId, Integer status) {
        // 检查是否为管理员
        SecurityUtil.getCurrentAdminId();
        
        // 检查用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 检查状态值
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值无效");
        }
        
        // 更新用户状态
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, userId)
                .set(User::getStatus, status);
        
        return userMapper.update(null, updateWrapper) > 0;
    }
    
    @Override
    @Transactional
    public String resetUserPassword(Long userId) {
        // 检查是否为管理员
        Long adminId = SecurityUtil.getCurrentAdminId();
        
        // 检查用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 生成随机密码，包含字母和数字
        String newPassword = RandomStringUtils.randomAlphanumeric(12);
        
        // 加密密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        // 更新用户密码
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, userId)
                .set(User::getPassword, encodedPassword);
        
        if (userMapper.update(null, updateWrapper) <= 0) {
            throw new BusinessException("重置密码失败");
        }
        
        // TODO: 记录操作日志
        
        return newPassword;
    }
    
    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        stats.put("totalUsers", userMapper.selectCount(null));
        
        // 今日注册用户数
        stats.put("todayNewUsers", userMapper.getTodayNewUsersCount());
        
        // 昨日注册用户数
        stats.put("yesterdayNewUsers", userMapper.getYesterdayNewUsersCount());
        
        // 本周注册用户数
        stats.put("weekNewUsers", userMapper.getWeekNewUsersCount());
        
        // 本月注册用户数
        stats.put("monthNewUsers", userMapper.getMonthNewUsersCount());
        
        // 活跃用户数（有登录记录的）
        stats.put("activeUsers", userMapper.getActiveUsersCount());
        
        // 禁用用户数
        LambdaQueryWrapper<User> disabledWrapper = new LambdaQueryWrapper<>();
        disabledWrapper.eq(User::getStatus, 0);
        stats.put("disabledUsers", userMapper.selectCount(disabledWrapper));
        
        // 男性用户数
        LambdaQueryWrapper<User> maleWrapper = new LambdaQueryWrapper<>();
        maleWrapper.eq(User::getGender, 1);
        stats.put("maleUsers", userMapper.selectCount(maleWrapper));
        
        // 女性用户数
        LambdaQueryWrapper<User> femaleWrapper = new LambdaQueryWrapper<>();
        femaleWrapper.eq(User::getGender, 2);
        stats.put("femaleUsers", userMapper.selectCount(femaleWrapper));
        
        // 用户增长率（与上周比较）
        stats.put("userGrowthRate", userMapper.getUserGrowthRate());
        
        return stats;
    }
} 