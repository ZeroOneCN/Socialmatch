package com.soical.server.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soical.server.entity.SystemSetting;
import com.soical.server.mapper.SystemSettingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全设置管理器
 * 用于集中管理安全相关设置，并在需要时提供给安全组件
 */
@Component
public class SecuritySettingsManager {
    private static final Logger log = LoggerFactory.getLogger(SecuritySettingsManager.class);
    
    private static final String SECURITY_GROUP = "security";
    
    // 安全设置缓存
    private final Map<String, Object> securitySettings = new ConcurrentHashMap<>();
    
    @Autowired
    private SystemSettingMapper systemSettingMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void init() {
        // 从数据库加载设置
        loadSettingsFromDatabase();
        
        // 如果数据库中没有设置，初始化默认设置
        if (securitySettings.isEmpty()) {
            initDefaultSettings();
            saveAllSettingsToDatabase();
        }
        
        log.info("安全设置管理器初始化完成，设置：{}", securitySettings);
    }
    
    /**
     * 从数据库加载设置
     */
    private void loadSettingsFromDatabase() {
        try {
            List<SystemSetting> settings = systemSettingMapper.getSettingsByGroup(SECURITY_GROUP);
            if (settings != null && !settings.isEmpty()) {
                for (SystemSetting setting : settings) {
                    String key = setting.getSettingKey();
                    String value = setting.getSettingValue();
                    if (key != null && value != null) {
                        // 根据值类型转换
                        if ("passwordStrength".equals(key)) {
                            try {
                                // 尝试解析JSON数组
                                if (value.contains("[") && value.contains("]")) {
                                    // 清理格式
                                    value = value.replace(" ", "")  // 移除空格
                                                 .replace("[", "")  // 移除方括号
                                                 .replace("]", ""); // 移除方括号
                                    
                                    // 分割并转换为列表
                                    List<String> strengthList = Arrays.asList(value.split(","));
                                    securitySettings.put(key, strengthList);
                                } else {
                                    // 单个值
                                    securitySettings.put(key, Collections.singletonList(value));
                                }
                            } catch (Exception e) {
                                log.error("解析passwordStrength失败", e);
                                securitySettings.put(key, Arrays.asList("lowercase", "number"));
                            }
                        } else if ("passwordMinLength".equals(key) ||
                                "passwordExpireDays".equals(key) ||
                                "maxLoginAttempts".equals(key) ||
                                "accountLockTime".equals(key) ||
                                "sessionTimeout".equals(key)) {
                            // 转换为整数
                            try {
                                securitySettings.put(key, Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                                log.error("解析整数设置失败: {}", key, e);
                                // 设置默认值
                                securitySettings.put(key, getDefaultValueForKey(key));
                            }
                        } else if ("enableCaptcha".equals(key)) {
                            // 转换为布尔值
                            securitySettings.put(key, Boolean.parseBoolean(value));
                        } else {
                            // 其他类型直接保存
                            securitySettings.put(key, value);
                        }
                    }
                }
                log.info("从数据库加载了 {} 条安全设置", settings.size());
            } else {
                log.info("数据库中没有安全设置，将使用默认设置");
            }
        } catch (Exception e) {
            log.error("从数据库加载安全设置失败", e);
        }
    }
    
    /**
     * 保存所有设置到数据库
     */
    private void saveAllSettingsToDatabase() {
        securitySettings.forEach((key, value) -> {
            saveSettingToDatabase(key, value);
        });
    }
    
    /**
     * 保存单个设置到数据库
     */
    private void saveSettingToDatabase(String key, Object value) {
        try {
            // 查询是否已存在
            LambdaQueryWrapper<SystemSetting> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemSetting::getGroup, SECURITY_GROUP)
                    .eq(SystemSetting::getSettingKey, key);
            
            SystemSetting existingSetting = systemSettingMapper.selectOne(queryWrapper);
            
            String valueStr;
            if (value instanceof List) {
                // 将列表转换为JSON字符串
                valueStr = objectMapper.writeValueAsString(value);
            } else {
                valueStr = String.valueOf(value);
            }
            
            if (existingSetting == null) {
                // 创建新设置
                SystemSetting newSetting = new SystemSetting();
                newSetting.setGroup(SECURITY_GROUP);
                newSetting.setSettingKey(key);
                newSetting.setSettingValue(valueStr);
                newSetting.setDescription(getDescriptionForKey(key));
                newSetting.setCreateTime(LocalDateTime.now());
                newSetting.setUpdateTime(LocalDateTime.now());
                
                systemSettingMapper.insert(newSetting);
                log.info("已创建安全设置: {} = {}", key, valueStr);
            } else {
                // 更新已有设置
                existingSetting.setSettingValue(valueStr);
                existingSetting.setUpdateTime(LocalDateTime.now());
                
                systemSettingMapper.updateById(existingSetting);
                log.info("已更新安全设置: {} = {}", key, valueStr);
            }
        } catch (Exception e) {
            log.error("保存安全设置到数据库失败: {} = {}", key, value, e);
        }
    }
    
    /**
     * 获取设置的描述
     */
    private String getDescriptionForKey(String key) {
        switch (key) {
            case "passwordMinLength": return "密码最小长度";
            case "passwordStrength": return "密码强度要求";
            case "passwordExpireDays": return "密码过期天数";
            case "maxLoginAttempts": return "最大登录尝试次数";
            case "accountLockTime": return "账户锁定时间（分钟）";
            case "enableCaptcha": return "是否启用验证码";
            case "sessionTimeout": return "会话超时时间（分钟）";
            default: return key;
        }
    }
    
    /**
     * 获取设置的默认值
     */
    private Object getDefaultValueForKey(String key) {
        switch (key) {
            case "passwordMinLength": return 8;
            case "passwordStrength": return Arrays.asList("lowercase", "number");
            case "passwordExpireDays": return 90;
            case "maxLoginAttempts": return 5;
            case "accountLockTime": return 30;
            case "enableCaptcha": return true;
            case "sessionTimeout": return 30;
            default: return null;
        }
    }
    
    /**
     * 初始化默认设置
     */
    private void initDefaultSettings() {
        securitySettings.put("passwordMinLength", 8);
        securitySettings.put("passwordStrength", Arrays.asList("lowercase", "number"));
        securitySettings.put("passwordExpireDays", 90);
        securitySettings.put("maxLoginAttempts", 5);
        securitySettings.put("accountLockTime", 30);
        securitySettings.put("enableCaptcha", true);
        securitySettings.put("sessionTimeout", 30);
        
        log.info("已初始化默认安全设置");
    }
    
    /**
     * 更新安全设置
     * @param settings 设置键值对
     */
    public void updateSettings(Map<String, Object> settings) {
        if (settings == null || settings.isEmpty()) {
            return;
        }
        
        settings.forEach((key, value) -> {
            if (value != null) {
                securitySettings.put(key, value);
                // 保存到数据库
                saveSettingToDatabase(key, value);
                log.info("更新安全设置: {} = {}", key, value);
            }
        });
    }
    
    /**
     * 获取所有安全设置
     * @return 安全设置映射
     */
    public Map<String, Object> getAllSettings() {
        return new ConcurrentHashMap<>(securitySettings);
    }
    
    /**
     * 获取密码最小长度
     * @return 密码最小长度
     */
    public int getPasswordMinLength() {
        return (int) securitySettings.getOrDefault("passwordMinLength", 8);
    }
    
    /**
     * 获取密码强度要求
     * @return 密码强度要求列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPasswordStrength() {
        return (List<String>) securitySettings.getOrDefault("passwordStrength", Arrays.asList("lowercase", "number"));
    }
    
    /**
     * 获取密码过期天数
     * @return 密码过期天数
     */
    public int getPasswordExpireDays() {
        return (int) securitySettings.getOrDefault("passwordExpireDays", 90);
    }
    
    /**
     * 获取最大登录尝试次数
     * @return 最大登录尝试次数
     */
    public int getMaxLoginAttempts() {
        return (int) securitySettings.getOrDefault("maxLoginAttempts", 5);
    }
    
    /**
     * 获取账户锁定时间（分钟）
     * @return 账户锁定时间
     */
    public int getAccountLockTime() {
        return (int) securitySettings.getOrDefault("accountLockTime", 30);
    }
    
    /**
     * 是否启用验证码
     * @return 是否启用验证码
     */
    public boolean isEnableCaptcha() {
        return (boolean) securitySettings.getOrDefault("enableCaptcha", true);
    }
    
    /**
     * 获取会话超时时间（分钟）
     * @return 会话超时时间
     */
    public int getSessionTimeout() {
        return (int) securitySettings.getOrDefault("sessionTimeout", 30);
    }
}