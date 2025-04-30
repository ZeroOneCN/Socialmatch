package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.UserPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户同频偏好设置Mapper
 */
@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {
} 