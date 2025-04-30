package com.soical.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.entity.SystemSettings;

import java.util.List;
import java.util.Map;

/**
 * 系统设置服务接口
 */
public interface SystemSettingsService extends IService<SystemSettings> {
    
    /**
     * 根据分组获取系统设置
     * @param group 设置分组
     * @return 设置列表
     */
    List<SystemSettings> getSettingsByGroup(String group);

    /**
     * 根据分组获取系统设置并转换为Map
     * @param group 设置分组
     * @return 键值对Map
     */
    Map<String, String> getSettingsMapByGroup(String group);
    
    /**
     * 保存或更新系统设置
     * @param group 设置分组
     * @param settings 设置值Map
     * @return 是否保存成功
     */
    boolean saveOrUpdateSettings(String group, Map<String, String> settings);

    /**
     * 获取单个设置值
     * @param group 设置分组
     * @param key 设置键
     * @return 设置值
     */
    String getSettingValue(String group, String key);

    /**
     * 保存或更新单个设置
     * @param group 设置分组
     * @param key 设置键
     * @param value 设置值
     * @param description 描述
     * @return 是否保存成功
     */
    boolean saveOrUpdateSetting(String group, String key, String value, String description);
} 