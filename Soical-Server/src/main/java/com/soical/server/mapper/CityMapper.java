package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.City;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 城市数据访问接口
 */
@Mapper
public interface CityMapper extends BaseMapper<City> {
    
    /**
     * 获取所有省份列表
     * 
     * @return 省份列表
     */
    List<City> selectProvinces();
    
    /**
     * 根据省份编码获取城市列表
     * 
     * @param provinceCode 省份编码
     * @return 城市列表
     */
    List<City> selectCitiesByProvince(@Param("provinceCode") String provinceCode);
} 