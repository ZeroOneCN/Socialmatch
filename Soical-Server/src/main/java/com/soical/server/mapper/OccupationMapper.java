package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.Occupation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 职业数据访问接口
 */
@Mapper
public interface OccupationMapper extends BaseMapper<Occupation> {
    
    /**
     * 获取所有职业类别
     * 
     * @return 职业类别列表
     */
    List<String> selectCategories();
    
    /**
     * 根据职业类别获取职业列表
     * 
     * @param category 职业类别
     * @return 职业列表
     */
    List<Occupation> selectByCategory(@Param("category") String category);
} 