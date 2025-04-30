package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.entity.SystemSettings;
import com.soical.server.mapper.SystemSettingsMapper;
import com.soical.server.service.SystemSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置服务实现类
 */
@Slf4j
@Service
public class SystemSettingsServiceImpl extends ServiceImpl<SystemSettingsMapper, SystemSettings> implements SystemSettingsService {

    @Override
    public List<SystemSettings> getSettingsByGroup(String group) {
        return baseMapper.getSettingsByGroup(group);
    }

    @Override
    public Map<String, String> getSettingsMapByGroup(String group) {
        List<SystemSettings> settings = getSettingsByGroup(group);
        Map<String, String> result = new HashMap<>();
        for (SystemSettings setting : settings) {
            result.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateSettings(String group, Map<String, String> settings) {
        try {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // 查询是否已存在
                LambdaQueryWrapper<SystemSettings> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SystemSettings::getGroup, group)
                           .eq(SystemSettings::getSettingKey, key);
                
                SystemSettings existingSetting = getOne(queryWrapper);
                
                if (existingSetting != null) {
                    // 更新
                    existingSetting.setSettingValue(value);
                    existingSetting.setUpdateTime(new Date());
                    updateById(existingSetting);
                } else {
                    // 新增
                    SystemSettings newSetting = new SystemSettings();
                    newSetting.setGroup(group);
                    newSetting.setSettingKey(key);
                    newSetting.setSettingValue(value);
                    newSetting.setDescription(key);
                    newSetting.setCreateTime(new Date());
                    newSetting.setUpdateTime(new Date());
                    save(newSetting);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("保存系统设置失败: group={}", group, e);
            return false;
        }
    }

    @Override
    public String getSettingValue(String group, String key) {
        LambdaQueryWrapper<SystemSettings> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemSettings::getGroup, group)
                   .eq(SystemSettings::getSettingKey, key);
        
        SystemSettings setting = getOne(queryWrapper);
        return setting != null ? setting.getSettingValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateSetting(String group, String key, String value, String description) {
        try {
            // 查询是否已存在
            LambdaQueryWrapper<SystemSettings> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemSettings::getGroup, group)
                       .eq(SystemSettings::getSettingKey, key);
            
            SystemSettings existingSetting = getOne(queryWrapper);
            
            if (existingSetting != null) {
                // 更新
                existingSetting.setSettingValue(value);
                if (description != null) {
                    existingSetting.setDescription(description);
                }
                existingSetting.setUpdateTime(new Date());
                return updateById(existingSetting);
            } else {
                // 新增
                SystemSettings newSetting = new SystemSettings();
                newSetting.setGroup(group);
                newSetting.setSettingKey(key);
                newSetting.setSettingValue(value);
                newSetting.setDescription(description != null ? description : key);
                newSetting.setCreateTime(new Date());
                newSetting.setUpdateTime(new Date());
                return save(newSetting);
            }
        } catch (Exception e) {
            log.error("保存系统设置失败: group={}, key={}", group, key, e);
            return false;
        }
    }
} 