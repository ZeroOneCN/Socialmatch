package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.SystemSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统设置Mapper接口
 */
@Mapper
public interface SystemSettingMapper extends BaseMapper<SystemSetting> {
    
    /**
     * 根据组获取设置
     */
    @Select("SELECT * FROM t_system_settings WHERE `group` = #{group}")
    List<SystemSetting> getSettingsByGroup(@Param("group") String group);
} 