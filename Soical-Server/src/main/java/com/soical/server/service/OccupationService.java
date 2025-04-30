package com.soical.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.entity.Occupation;

import java.util.List;
import java.util.Map;

/**
 * 职业服务接口
 */
public interface OccupationService extends IService<Occupation> {
    
    /**
     * 获取所有职业类别
     * 
     * @return 职业类别列表
     */
    List<String> getCategories();
    
    /**
     * 根据职业类别获取职业列表
     * 
     * @param category 职业类别
     * @return 职业列表
     */
    List<Map<String, Object>> getOccupationsByCategory(String category);
    
    /**
     * 获取按类别分组的所有职业
     * 
     * @return 按类别分组的所有职业
     */
    List<Map<String, Object>> getAllOccupationsGroupByCategory();
    
    /**
     * 获取所有职业列表
     * 
     * @return 所有职业列表
     */
    List<Map<String, Object>> getAllOccupations();
} 