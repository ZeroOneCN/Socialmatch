package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户资料Mapper接口
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
} 