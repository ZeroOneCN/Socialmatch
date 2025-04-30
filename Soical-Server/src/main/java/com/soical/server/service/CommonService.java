package com.soical.server.service;

import java.util.List;
import java.util.Map;

/**
 * 通用服务接口
 * 提供各种通用选项数据和公共服务
 */
public interface CommonService {
    
    /**
     * 获取所有职业列表
     * @return 职业列表，包含code和name
     */
    List<Map<String, Object>> getAllOccupations();
    
    /**
     * 获取所有学历列表
     * @return 学历列表，包含code和name
     */
    List<Map<String, Object>> getAllEducations();
    
    /**
     * 获取性别选项列表
     * @return 性别选项列表，包含code和name
     */
    List<Map<String, Object>> getGenderOptions();
    
    /**
     * 获取应用配置信息
     * @return 应用配置信息
     */
    Map<String, Object> getAppConfig();
    
    /**
     * 根据经纬度坐标获取位置信息
     * @param latitude 纬度
     * @param longitude 经度
     * @return 位置信息，包含城市、省份等
     */
    Map<String, Object> getLocationByCoords(Double latitude, Double longitude);
    
    /**
     * 根据IP地址获取位置信息
     * @return 位置信息，包含城市、省份等
     */
    Map<String, Object> getLocationByIp();
} 