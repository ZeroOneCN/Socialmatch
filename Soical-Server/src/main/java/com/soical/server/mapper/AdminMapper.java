package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 根据用户名查询管理员
     *
     * @param username 用户名
     * @return 管理员
     */
    @Select("SELECT * FROM t_admin WHERE username = #{username}")
    Admin selectByUsername(@Param("username") String username);
} 