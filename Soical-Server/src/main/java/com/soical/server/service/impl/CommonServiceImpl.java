package com.soical.server.service.impl;

import com.soical.server.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用服务实现类
 */
@Service
@Transactional(readOnly = true)
public class CommonServiceImpl implements CommonService {

    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public CommonServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * 获取所有职业列表
     */
    @Override
    public List<Map<String, Object>> getAllOccupations() {
        List<Map<String, Object>> occupations = new ArrayList<>();
        
        // 如果有职业表，可以查询，否则提供默认列表
        try {
            // 尝试查询职业表
            String sql = "SELECT code, name FROM t_occupation ORDER BY sort_order";
            occupations = jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            // 表不存在或查询出错，提供默认数据
            occupations = getDefaultOccupations();
        }
        
        return occupations;
    }
    
    /**
     * 获取所有学历列表
     */
    @Override
    public List<Map<String, Object>> getAllEducations() {
        List<Map<String, Object>> educations = new ArrayList<>();
        
        // 添加学历选项
        Map<String, Object> edu0 = new HashMap<>();
        edu0.put("code", 0);
        edu0.put("name", "未设置");
        
        Map<String, Object> edu1 = new HashMap<>();
        edu1.put("code", 1);
        edu1.put("name", "高中及以下");
        
        Map<String, Object> edu2 = new HashMap<>();
        edu2.put("code", 2);
        edu2.put("name", "大专");
        
        Map<String, Object> edu3 = new HashMap<>();
        edu3.put("code", 3);
        edu3.put("name", "本科");
        
        Map<String, Object> edu4 = new HashMap<>();
        edu4.put("code", 4);
        edu4.put("name", "硕士");
        
        Map<String, Object> edu5 = new HashMap<>();
        edu5.put("code", 5);
        edu5.put("name", "博士及以上");
        
        educations.add(edu0);
        educations.add(edu1);
        educations.add(edu2);
        educations.add(edu3);
        educations.add(edu4);
        educations.add(edu5);
        
        return educations;
    }
    
    /**
     * 获取性别选项列表
     */
    @Override
    public List<Map<String, Object>> getGenderOptions() {
        List<Map<String, Object>> genders = new ArrayList<>();
        
        Map<String, Object> gender0 = new HashMap<>();
        gender0.put("code", 0);
        gender0.put("name", "未设置");
        
        Map<String, Object> gender1 = new HashMap<>();
        gender1.put("code", 1);
        gender1.put("name", "男");
        
        Map<String, Object> gender2 = new HashMap<>();
        gender2.put("code", 2);
        gender2.put("name", "女");
        
        Map<String, Object> gender3 = new HashMap<>();
        gender3.put("code", 3);
        gender3.put("name", "保密");
        
        genders.add(gender0);
        genders.add(gender1);
        genders.add(gender2);
        genders.add(gender3);
        
        return genders;
    }
    
    /**
     * 获取应用配置信息
     */
    @Override
    public Map<String, Object> getAppConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 版本信息
        config.put("version", "1.0.0");
        config.put("build", "202401001");
        
        // 功能配置
        Map<String, Object> features = new HashMap<>();
        features.put("enableChat", true);
        features.put("enableMoments", true);
        features.put("enableGroup", true);
        features.put("enableVoiceCall", false);
        features.put("enableVideoCall", false);
        
        config.put("features", features);
        
        return config;
    }
    
    /**
     * 根据经纬度坐标获取位置信息
     */
    @Override
    public Map<String, Object> getLocationByCoords(Double latitude, Double longitude) {
        // 这里可以调用第三方地图API获取实际位置
        // 这里简化实现，根据坐标范围返回一些默认城市
        Map<String, Object> locationInfo = new HashMap<>();
        
        // 北京坐标范围
        if (latitude >= 39.4 && latitude <= 41.6 && longitude >= 115.7 && longitude <= 117.4) {
            locationInfo.put("city", "北京市");
            locationInfo.put("province", "北京市");
            locationInfo.put("provinceCode", "110000");
            locationInfo.put("cityCode", "110100");
        } 
        // 上海坐标范围
        else if (latitude >= 30.7 && latitude <= 31.9 && longitude >= 120.9 && longitude <= 121.8) {
            locationInfo.put("city", "上海市");
            locationInfo.put("province", "上海市");
            locationInfo.put("provinceCode", "310000");
            locationInfo.put("cityCode", "310100");
        }
        // 广州坐标范围 
        else if (latitude >= 22.8 && latitude <= 23.3 && longitude >= 113.0 && longitude <= 113.6) {
            locationInfo.put("city", "广州市");
            locationInfo.put("province", "广东省");
            locationInfo.put("provinceCode", "440000");
            locationInfo.put("cityCode", "440100");
        }
        // 深圳坐标范围
        else if (latitude >= 22.4 && latitude <= 22.8 && longitude >= 113.8 && longitude <= 114.4) {
            locationInfo.put("city", "深圳市");
            locationInfo.put("province", "广东省");
            locationInfo.put("provinceCode", "440000");
            locationInfo.put("cityCode", "440300");
        }
        // 成都坐标范围
        else if (latitude >= 30.4 && latitude <= 31.0 && longitude >= 103.6 && longitude <= 104.4) {
            locationInfo.put("city", "成都市");
            locationInfo.put("province", "四川省");
            locationInfo.put("provinceCode", "510000");
            locationInfo.put("cityCode", "510100");
        }
        // 其他情况默认返回北京
        else {
            locationInfo.put("city", "北京市");
            locationInfo.put("province", "北京市");
            locationInfo.put("provinceCode", "110000");
            locationInfo.put("cityCode", "110100");
        }
        
        return locationInfo;
    }
    
    /**
     * 根据IP地址获取位置信息
     */
    @Override
    public Map<String, Object> getLocationByIp() {
        // 这里可以调用第三方IP定位API
        // 简化实现，返回默认位置（北京）
        Map<String, Object> locationInfo = new HashMap<>();
        locationInfo.put("city", "北京市");
        locationInfo.put("province", "北京市");
        locationInfo.put("provinceCode", "110000");
        locationInfo.put("cityCode", "110100");
        locationInfo.put("latitude", 39.9042);
        locationInfo.put("longitude", 116.4074);
        
        return locationInfo;
    }
    
    /**
     * 提供默认职业列表
     */
    private List<Map<String, Object>> getDefaultOccupations() {
        List<Map<String, Object>> occupations = new ArrayList<>();
        
        String[][] defaultOccupations = {
            {"SE001", "软件工程师"},
            {"PM001", "产品经理"},
            {"MK001", "市场营销"},
            {"AC001", "财务会计"},
            {"HR001", "人力资源"},
            {"TC001", "教师"},
            {"DR001", "医生"},
            {"ST001", "学生"},
            {"FR001", "自由职业"},
            {"OT001", "其他"}
        };
        
        for (String[] occupation : defaultOccupations) {
            Map<String, Object> occMap = new HashMap<>();
            occMap.put("code", occupation[0]);
            occMap.put("name", occupation[1]);
            occupations.add(occMap);
        }
        
        return occupations;
    }
} 