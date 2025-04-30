package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.SystemSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统设置Mapper接口
 */
@Mapper
public interface SystemSettingsMapper extends BaseMapper<SystemSettings> {

    /**
     * 根据分组获取系统设置列表
     * @param group 分组名称
     * @return 系统设置列表
     */
    @Select("SELECT * FROM t_system_settings WHERE `group` = #{group}")
    List<SystemSettings> getSettingsByGroup(@Param("group") String group);
} 