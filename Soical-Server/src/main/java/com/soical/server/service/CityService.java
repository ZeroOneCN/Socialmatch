package com.soical.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.entity.City;

import java.util.List;
import java.util.Map;

/**
 * 城市服务接口
 */
public interface CityService extends IService<City> {
    
    /**
     * 获取所有省份列表
     * 
     * @return 省份列表
     */
    List<Map<String, Object>> getProvinces();
    
    /**
     * 根据省份编码获取城市列表
     * 
     * @param provinceCode 省份编码
     * @return 城市列表
     */
    List<Map<String, Object>> getCitiesByProvince(String provinceCode);
    
    /**
     * 获取省市两级数据
     * 
     * @return 省市两级数据
     */
    List<Map<String, Object>> getProvincesWithCities();
} 