package com.soical.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.dto.UserProfileDTO;
import com.soical.server.entity.UserProfile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 用户资料服务接口
 */
public interface UserProfileService extends IService<UserProfile> {

    /**
     * 获取用户资料
     *
     * @param userId 用户ID
     * @return 用户资料
     */
    UserProfile getProfileByUserId(Long userId);

    /**
     * 更新用户资料
     *
     * @param userId 用户ID
     * @param userProfileDTO 用户资料DTO
     * @return 是否成功
     */
    boolean updateUserProfile(Long userId, UserProfileDTO userProfileDTO);
    
    /**
     * 添加照片到个人相册
     *
     * @param userId 用户ID
     * @param file 照片文件
     * @return 照片URL
     */
    String addPhoto(Long userId, MultipartFile file);
    
    /**
     * 获取个人相册照片列表
     *
     * @param userId 用户ID
     * @return 照片URL列表
     */
    List<String> getPhotos(Long userId);
    
    /**
     * 删除相册照片
     *
     * @param userId 用户ID
     * @param photoUrl 照片URL
     * @return 是否成功
     */
    boolean deletePhoto(Long userId, String photoUrl);

    /**
     * 根据用户ID获取用户资料
     * @param userId 用户ID
     * @return 用户资料
     */
    UserProfile getUserProfile(Long userId);
    
    /**
     * 批量获取用户资料
     * @param userIds 用户ID集合
     * @return 用户ID到用户资料的映射
     */
    Map<Long, UserProfile> getUserProfiles(Collection<Long> userIds);
    
    /**
     * 保存或更新用户资料
     * @param userProfile 用户资料
     * @return 是否成功
     */
    boolean saveOrUpdateUserProfile(UserProfile userProfile);

    /**
     * 检查是否存在重复的用户资料记录
     * @return 重复记录列表
     */
    List<Object> checkForDuplicateProfiles();
} 