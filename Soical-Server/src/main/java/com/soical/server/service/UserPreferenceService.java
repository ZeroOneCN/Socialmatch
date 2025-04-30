package com.soical.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.entity.UserPreference;

import java.util.Map;

/**
 * 用户同频偏好设置服务接口
 */
public interface UserPreferenceService extends IService<UserPreference> {
    
    /**
     * 获取用户的偏好设置
     * @param userId 用户ID
     * @return 用户偏好设置，如果不存在则返回默认设置
     */
    UserPreference getUserPreference(Long userId);
    
    /**
     * 更新用户偏好设置
     * @param userId 用户ID
     * @param preferences 偏好设置数据
     * @return 是否更新成功
     */
    boolean updateUserPreference(Long userId, Map<String, Object> preferences);
    
    /**
     * 将用户偏好设置转换为前端需要的Map格式
     * @param preference 用户偏好设置
     * @return 前端格式的偏好设置数据
     */
    Map<String, Object> convertToMap(UserPreference preference);
} 