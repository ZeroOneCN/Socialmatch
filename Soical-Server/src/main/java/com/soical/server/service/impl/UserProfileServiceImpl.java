package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import com.soical.server.dto.UserProfileDTO;
import com.soical.server.entity.User;
import com.soical.server.entity.UserProfile;
import com.soical.server.mapper.UserMapper;
import com.soical.server.mapper.UserProfileMapper;
import com.soical.server.service.UserProfileService;
import com.soical.server.service.UserService;
import com.soical.server.util.FileUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户资料服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService, CommandLineRunner {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(UserProfileServiceImpl.class);
    
    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ApplicationContext applicationContext;
    
    // 使用懒加载方式获取UserService，避免循环依赖
    private UserService getUserService() {
        return applicationContext.getBean(UserService.class);
    }
    
    @Autowired
    public UserProfileServiceImpl(UserMapper userMapper, 
                                JdbcTemplate jdbcTemplate,
                                ApplicationContext applicationContext) {
        this.userMapper = userMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.applicationContext = applicationContext;
    }

    /**
     * 根据用户ID获取用户资料
     * 如果不存在则返回null
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public UserProfile getProfileByUserId(Long userId) {
        UserProfile userProfile = getOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId), false);
        if (userProfile == null) {
            log.warn("用户资料不存在: userId={}", userId);
            return null;
        }
        return userProfile;
    }

    /**
     * 更新或创建用户资料
     */
    @Override
    @Transactional
    public boolean updateUserProfile(Long userId, UserProfileDTO userProfileDTO) {
        try {
            // 获取用户资料或创建新的
            UserProfile userProfile = null;
            try {
                userProfile = getProfileByUserId(userId);
                log.info("更新现有用户资料: userId={}", userId);
            } catch (BusinessException e) {
                // 如果用户资料不存在，创建新资料
                if (e.getCode().equals(ResultCode.USER_PROFILE_NOT_EXIST.getCode())) {
                    userProfile = new UserProfile();
                    userProfile.setUserId(userId);
                    log.info("创建新用户资料: userId={}", userId);
                } else {
                    throw e;
                }
            }
            
            // 记录更新前后数据
            log.debug("更新前资料: {}", userProfile);
            log.debug("传入DTO: {}", userProfileDTO);
            
            // 复制属性
            BeanUtils.copyProperties(userProfileDTO, userProfile);
            
            // 保存更新
            boolean result = saveOrUpdateUserProfile(userProfile);
            log.info("用户资料保存结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("更新用户资料失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FAILED.getCode(), "更新资料失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加照片到用户相册
     */
    @Override
    public String addPhoto(Long userId, MultipartFile file) {
        // 获取用户资料
        UserProfile userProfile = getProfileByUserId(userId);
        
        // 上传照片
        String photoUrl = FileUtil.uploadImage(file);
        
        // 更新相册
        try {
            List<String> photos = getPhotosList(userProfile);
            photos.add(photoUrl);
            userProfile.setPhotos(objectMapper.writeValueAsString(photos));
            updateById(userProfile);
            return photoUrl;
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "照片添加失败");
        }
    }
    
    /**
     * 获取用户相册照片列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getPhotos(Long userId) {
        UserProfile userProfile = getProfileByUserId(userId);
        return getPhotosList(userProfile);
    }
    
    /**
     * 从用户相册中删除照片
     */
    @Override
    public boolean deletePhoto(Long userId, String photoUrl) {
        // 获取用户资料
        UserProfile userProfile = getProfileByUserId(userId);
        
        try {
            // 获取照片列表
            List<String> photos = getPhotosList(userProfile);
            
            // 删除指定照片
            boolean removed = photos.remove(photoUrl);
            if (!removed) {
                return false;
            }
            
            // 更新照片列表
            userProfile.setPhotos(objectMapper.writeValueAsString(photos));
            return updateById(userProfile);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "照片删除失败");
        }
    }
    
    /**
     * 获取用户照片列表
     */
    private List<String> getPhotosList(UserProfile userProfile) {
        if (userProfile.getPhotos() == null || userProfile.getPhotos().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(userProfile.getPhotos(), new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 应用启动时，为没有资料的用户创建默认资料
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("开始检查并创建缺失的用户资料...");
        createMissingUserProfiles();
        log.info("用户资料检查完成");
    }
    
    /**
     * 为没有资料的用户创建默认资料
     */
    @Transactional
    public void createMissingUserProfiles() {
        // 查询所有没有资料的用户
        List<User> usersWithoutProfile = userMapper.findUsersWithoutProfile();
        
        if (usersWithoutProfile.isEmpty()) {
            log.info("所有用户都已有资料");
            return;
        }
        
        log.info("发现 {} 个没有资料的用户，开始创建默认资料", usersWithoutProfile.size());
        
        for (User user : usersWithoutProfile) {
            try {
                UserProfile profile = new UserProfile();
                profile.setUserId(user.getUserId());
                profile.setNickname(user.getUsername());
                save(profile);
                log.info("为用户 {} 创建了默认资料", user.getUserId());
            } catch (Exception e) {
                log.error("为用户 {} 创建资料失败: {}", user.getUserId(), e.getMessage());
            }
        }
    }

    /**
     * 获取单个用户的资料
     * 如果资料不存在，从用户表获取基本信息创建临时资料
     */
    @Override
    public UserProfile getUserProfile(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<UserProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProfile::getUserId, userId);
        
        UserProfile userProfile = this.getOne(queryWrapper);
        
        // 如果找不到用户资料，尝试从用户表获取基本信息
        if (userProfile == null) {
            User user = getUserService().getById(userId);
            if (user != null) {
                userProfile = new UserProfile();
                userProfile.setUserId(userId);
                userProfile.setNickname(user.getUsername());
                userProfile.setAvatar(user.getAvatar());
            }
        }
        
        return userProfile;
    }
    
    /**
     * 批量获取多个用户资料
     */
    @Override
    public Map<Long, UserProfile> getUserProfiles(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 查询用户资料
        LambdaQueryWrapper<UserProfile> profileQueryWrapper = new LambdaQueryWrapper<>();
        profileQueryWrapper.in(UserProfile::getUserId, userIds);
        List<UserProfile> userProfiles = this.list(profileQueryWrapper);
        
        // 转换为Map，处理可能的重复用户ID
        Map<Long, UserProfile> profileMap = new HashMap<>();
        for (UserProfile profile : userProfiles) {
            if (profile != null && profile.getUserId() != null) {
                // 避免重复键，只保留第一个遇到的同ID记录
                if (!profileMap.containsKey(profile.getUserId())) {
                    profileMap.put(profile.getUserId(), profile);
                } else {
                    log.warn("发现重复的用户资料ID: {}, 忽略此条记录", profile.getUserId());
                }
            }
        }
        
        // 对于没有资料的用户，从用户表查询基本信息
        Set<Long> missingUserIds = new HashSet<>(userIds);
        missingUserIds.removeAll(profileMap.keySet());
        
        if (!missingUserIds.isEmpty()) {
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.in(User::getUserId, missingUserIds);
            List<User> users = userMapper.selectList(userQueryWrapper);
            
            for (User user : users) {
                UserProfile profile = new UserProfile();
                profile.setUserId(user.getUserId());
                profile.setNickname(user.getUsername());
                profile.setAvatar(user.getAvatar());
                
                profileMap.put(user.getUserId(), profile);
            }
        }
        
        return profileMap;
    }
    
    /**
     * 保存或更新用户资料
     */
    @Override
    public boolean saveOrUpdateUserProfile(UserProfile userProfile) {
        if (userProfile == null || userProfile.getUserId() == null) {
            log.error("用户资料保存失败：userProfile为null或userId为null");
            return false;
        }
        
        // 检查是否已存在
        LambdaQueryWrapper<UserProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProfile::getUserId, userProfile.getUserId());
        
        UserProfile existingProfile = this.getOne(queryWrapper);
        
        if (existingProfile == null) {
            // 不存在，新增
            log.info("用户资料不存在，执行新增操作: userId={}", userProfile.getUserId());
            return this.save(userProfile);
        } else {
            // 已存在，更新
            userProfile.setProfileId(existingProfile.getProfileId());
            log.info("用户资料已存在，执行更新操作: userId={}, profileId={}", 
                userProfile.getUserId(), userProfile.getProfileId());
            return this.updateById(userProfile);
        }
    }
    
    /**
     * 检查是否存在重复的用户资料记录
     * @return 重复记录列表
     */
    @Override
    public List<Object> checkForDuplicateProfiles() {
        // 创建结果集
        List<Object> result = new ArrayList<>();
        
        try {
            // 查询重复的用户资料
            String sql = "SELECT user_id, COUNT(*) as count FROM t_user_profile GROUP BY user_id HAVING COUNT(*) > 1";
            List<Map<String, Object>> duplicates = jdbcTemplate.queryForList(sql);
            
            if (duplicates.isEmpty()) {
                Map<String, Object> info = new HashMap<>();
                info.put("message", "未发现重复用户资料");
                result.add(info);
                return result;
            }
            
            // 添加重复记录摘要
            Map<String, Object> summary = new HashMap<>();
            summary.put("duplicateCount", duplicates.size());
            summary.put("message", "发现 " + duplicates.size() + " 个用户有重复资料记录");
            result.add(summary);
            
            // 添加每个重复用户的详细记录
            for (Map<String, Object> dup : duplicates) {
                Long userId = ((Number) dup.get("user_id")).longValue();
                
                // 查询该用户的所有资料记录
                String detailSql = "SELECT * FROM t_user_profile WHERE user_id = ? ORDER BY profile_id";
                List<Map<String, Object>> userProfiles = jdbcTemplate.queryForList(detailSql, userId);
                
                Map<String, Object> userDetail = new HashMap<>();
                userDetail.put("userId", userId);
                userDetail.put("duplicateCount", dup.get("count"));
                userDetail.put("profiles", userProfiles);
                
                result.add(userDetail);
            }
        } catch (Exception e) {
            log.error("检查重复用户资料失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            result.add(error);
        }
        
        return result;
    }
} 