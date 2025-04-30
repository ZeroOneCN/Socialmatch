package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.entity.UserPreference;
import com.soical.server.mapper.UserPreferenceMapper;
import com.soical.server.service.UserPreferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 用户同频偏好设置服务实现类
 */
@Slf4j
@Service
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference> implements UserPreferenceService {

    @Override
    public UserPreference getUserPreference(Long userId) {
        if (userId == null) {
            return createDefaultPreference(null);
        }
        
        // 查询用户偏好设置
        LambdaQueryWrapper<UserPreference> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPreference::getUserId, userId);
        UserPreference preference = getOne(queryWrapper);
        
        // 如果不存在，则返回默认设置
        if (preference == null) {
            return createDefaultPreference(userId);
        }
        
        return preference;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserPreference(Long userId, Map<String, Object> preferences) {
        if (userId == null || preferences == null) {
            return false;
        }
        
        try {
            // 查询用户偏好设置
            LambdaQueryWrapper<UserPreference> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserPreference::getUserId, userId);
            UserPreference userPreference = getOne(queryWrapper);
            
            boolean isNew = false;
            // 如果不存在，则创建新的偏好设置
            if (userPreference == null) {
                userPreference = new UserPreference();
                userPreference.setUserId(userId);
                userPreference.setCreateTime(new Date());
                isNew = true;
            }
            
            // 更新属性
            updateFromMap(userPreference, preferences);
            userPreference.setUpdateTime(new Date());
            
            // 保存或更新
            if (isNew) {
                return save(userPreference);
            } else {
                return updateById(userPreference);
            }
        } catch (Exception e) {
            log.error("更新用户偏好设置失败: userId={}", userId, e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> convertToMap(UserPreference preference) {
        if (preference == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 兴趣爱好
        result.put("interests", preference.getInterests());
        
        // 位置设置
        result.put("city", preference.getCity());
        result.put("cityCode", preference.getCityCode());
        result.put("nearbyOnly", preference.getNearbyOnly());
        result.put("maxDistance", preference.getMaxDistance());
        
        // 年龄范围
        List<Integer> ageRange = Arrays.asList(
            preference.getMinAge() != null ? preference.getMinAge() : 18,
            preference.getMaxAge() != null ? preference.getMaxAge() : 35
        );
        result.put("ageRange", ageRange);
        
        // 教育与职业
        result.put("education", preference.getEducation());
        result.put("occupation", preference.getOccupation());
        result.put("occupationCode", preference.getOccupationCode());
        
        // 其他设置
        result.put("verifiedOnly", preference.getVerifiedOnly());
        
        return result;
    }
    
    /**
     * 创建默认的偏好设置
     */
    private UserPreference createDefaultPreference(Long userId) {
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setInterests("");
        preference.setCity(null);
        preference.setCityCode(null);
        preference.setNearbyOnly(false);
        preference.setMaxDistance(50);
        preference.setMinAge(18);
        preference.setMaxAge(35);
        preference.setEducation(0);  // 不限
        preference.setOccupation(null);
        preference.setOccupationCode(null);
        preference.setVerifiedOnly(false);
        preference.setCreateTime(new Date());
        preference.setUpdateTime(new Date());
        return preference;
    }
    
    /**
     * 从Map更新UserPreference对象的属性
     */
    private void updateFromMap(UserPreference preference, Map<String, Object> map) {
        // 兴趣爱好
        if (map.containsKey("interests")) {
            preference.setInterests(parseStringValue(map.get("interests")));
        }
        
        // 位置设置
        if (map.containsKey("city")) {
            preference.setCity(String.valueOf(map.get("city")));
        }
        
        if (map.containsKey("cityCode")) {
            preference.setCityCode(String.valueOf(map.get("cityCode")));
        }
        
        if (map.containsKey("nearbyOnly")) {
            preference.setNearbyOnly(Boolean.valueOf(String.valueOf(map.get("nearbyOnly"))));
        }
        
        if (map.containsKey("maxDistance")) {
            Object maxDistanceObj = map.get("maxDistance");
            if (maxDistanceObj != null) {
                try {
                    preference.setMaxDistance(Integer.parseInt(String.valueOf(maxDistanceObj)));
                } catch (NumberFormatException e) {
                    log.warn("解析maxDistance失败: {}", maxDistanceObj);
                }
            }
        }
        
        // 年龄范围
        if (map.containsKey("ageRange")) {
            Object ageRangeObj = map.get("ageRange");
            if (ageRangeObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> ageRange = (List<Object>) ageRangeObj;
                if (ageRange.size() >= 2) {
                    try {
                        preference.setMinAge(Integer.parseInt(String.valueOf(ageRange.get(0))));
                        preference.setMaxAge(Integer.parseInt(String.valueOf(ageRange.get(1))));
                    } catch (NumberFormatException e) {
                        log.warn("解析ageRange失败: {}", ageRange);
                    }
                }
            }
        }
        
        // 教育与职业
        if (map.containsKey("education")) {
            Object educationObj = map.get("education");
            if (educationObj != null) {
                try {
                    preference.setEducation(Integer.parseInt(String.valueOf(educationObj)));
                } catch (NumberFormatException e) {
                    // 尝试解析字符串形式的教育程度
                    String educationStr = String.valueOf(educationObj);
                    switch (educationStr) {
                        case "不限":
                            preference.setEducation(0);
                            break;
                        case "高中及以上":
                            preference.setEducation(1);
                            break;
                        case "大专及以上":
                            preference.setEducation(2);
                            break;
                        case "本科及以上":
                            preference.setEducation(3);
                            break;
                        case "硕士及以上":
                            preference.setEducation(4);
                            break;
                        case "博士及以上":
                            preference.setEducation(5);
                            break;
                        default:
                            log.warn("无法解析的教育程度: {}", educationStr);
                    }
                }
            }
        }
        
        if (map.containsKey("occupation")) {
            preference.setOccupation(String.valueOf(map.get("occupation")));
        }
        
        if (map.containsKey("occupationCode")) {
            preference.setOccupationCode(String.valueOf(map.get("occupationCode")));
        }
        
        // 其他设置
        if (map.containsKey("verifiedOnly")) {
            preference.setVerifiedOnly(Boolean.valueOf(String.valueOf(map.get("verifiedOnly"))));
        }
    }
    
    /**
     * 解析兴趣爱好等字符串，支持字符串或List<String>格式
     */
    private String parseStringValue(Object value) {
        if (value == null) {
            return "";
        }
        
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) value;
            return String.join(",", list);
        }
        
        return String.valueOf(value);
    }
} 