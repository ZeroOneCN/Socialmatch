package com.soical.server.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员动态管理数据访问接口
 */
@Mapper
public interface AdminPostMapper {
    
    /**
     * 管理员查询动态列表
     *
     * @param page    分页对象
     * @param status  状态
     * @param keyword 关键词
     * @return 分页结果
     */
    Page<Post> selectPostsForAdmin(Page<Post> page, @Param("status") Integer status, @Param("keyword") String keyword);
    
    /**
     * 管理员查询动态详情
     *
     * @param postId 动态ID
     * @return 动态信息
     */
    Post selectPostDetailForAdmin(@Param("postId") Long postId);
    
    /**
     * 统计各状态动态数量
     *
     * @param status 状态
     * @return 数量
     */
    Long countPostsByStatus(@Param("status") Integer status);
    
    /**
     * 更新动态信息
     *
     * @param post 动态信息
     * @return 影响行数
     */
    int updateById(Post post);
} 