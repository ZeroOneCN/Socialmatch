package com.soical.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.entity.City;
import com.soical.server.mapper.CityMapper;
import com.soical.server.service.CityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 城市服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CityServiceImpl extends ServiceImpl<CityMapper, City> implements CityService {
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProvinces() {
        List<City> provinces = baseMapper.selectProvinces();
        return provinces.stream().map(province -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", province.getProvinceCode());
            map.put("name", province.getProvinceName());
            return map;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCitiesByProvince(String provinceCode) {
        List<City> cities = baseMapper.selectCitiesByProvince(provinceCode);
        return cities.stream().map(city -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", city.getCityCode());
            map.put("name", city.getCityName());
            return map;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProvincesWithCities() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 获取所有省份
        List<City> provinces = baseMapper.selectProvinces();
        
        // 遍历省份，为每个省份添加城市列表
        for (City province : provinces) {
            Map<String, Object> provinceMap = new HashMap<>();
            provinceMap.put("code", province.getProvinceCode());
            provinceMap.put("name", province.getProvinceName());
            
            // 获取该省份的所有城市
            List<City> cities = baseMapper.selectCitiesByProvince(province.getProvinceCode());
            List<Map<String, Object>> cityList = cities.stream().map(city -> {
                Map<String, Object> cityMap = new HashMap<>();
                cityMap.put("code", city.getCityCode());
                cityMap.put("name", city.getCityName());
                return cityMap;
            }).collect(Collectors.toList());
            
            provinceMap.put("cities", cityList);
            result.add(provinceMap);
        }
        
        return result;
    }
} 