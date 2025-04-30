package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.entity.Occupation;
import com.soical.server.mapper.OccupationMapper;
import com.soical.server.service.OccupationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 职业服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OccupationServiceImpl extends ServiceImpl<OccupationMapper, Occupation> implements OccupationService {
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return baseMapper.selectCategories();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOccupationsByCategory(String category) {
        List<Occupation> occupations = baseMapper.selectByCategory(category);
        return occupations.stream().map(occupation -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", occupation.getOccupationId());
            map.put("name", occupation.getName());
            map.put("code", occupation.getCode());
            map.put("category", occupation.getCategory());
            return map;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllOccupationsGroupByCategory() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 获取所有职业类别
        List<String> categories = baseMapper.selectCategories();
        
        // 遍历类别，为每个类别添加职业列表
        for (String category : categories) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("category", category);
            
            // 获取该类别的所有职业
            List<Occupation> occupations = baseMapper.selectByCategory(category);
            List<Map<String, Object>> occupationList = occupations.stream().map(occupation -> {
                Map<String, Object> occupationMap = new HashMap<>();
                occupationMap.put("id", occupation.getOccupationId());
                occupationMap.put("name", occupation.getName());
                occupationMap.put("code", occupation.getCode());
                return occupationMap;
            }).collect(Collectors.toList());
            
            categoryMap.put("occupations", occupationList);
            result.add(categoryMap);
        }
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllOccupations() {
        LambdaQueryWrapper<Occupation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Occupation::getStatus, 1)
                   .orderByAsc(Occupation::getCategory)
                   .orderByAsc(Occupation::getCode);
        
        List<Occupation> occupations = list(queryWrapper);
        return occupations.stream().map(occupation -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", occupation.getOccupationId());
            map.put("name", occupation.getName());
            map.put("code", occupation.getCode());
            map.put("category", occupation.getCategory());
            return map;
        }).collect(Collectors.toList());
    }
} 