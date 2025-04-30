package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.entity.User;
import com.soical.server.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户关注数据访问接口
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
    
    /**
     * 查询用户关注的用户列表（带用户信息）
     *
     * @param page       分页对象
     * @param followerId 关注者ID
     * @return 带用户信息的关注列表
     */
    Page<User> selectFollowingUsersWithInfo(Page<User> page, @Param("followerId") Long followerId);
    
    /**
     * 查询用户的粉丝列表（带用户信息）
     *
     * @param page       分页对象
     * @param followedId 被关注者ID
     * @return 带用户信息的粉丝列表
     */
    Page<User> selectFollowerUsersWithInfo(Page<User> page, @Param("followedId") Long followedId);
    
    /**
     * 查询用户关注的用户ID列表
     *
     * @param followerId 关注者ID
     * @return 关注的用户ID列表
     */
    List<Long> selectFollowingIds(@Param("followerId") Long followerId);
    
    /**
     * 统计用户的粉丝数
     *
     * @param followedId 被关注者ID
     * @return 粉丝数量
     */
    Integer countFollowers(@Param("followedId") Long followedId);
    
    /**
     * 统计用户关注的人数
     *
     * @param followerId 关注者ID
     * @return 关注数量
     */
    Integer countFollowing(@Param("followerId") Long followerId);
} 