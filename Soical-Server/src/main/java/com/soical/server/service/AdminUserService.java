package com.soical.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.dto.UserDetailDTO;

import java.util.Map;

/**
 * 管理员用户服务接口
 */
public interface AdminUserService {
    
    /**
     * 获取用户列表
     *
     * @param page     页码
     * @param pageSize 每页条数
     * @param keyword  关键词搜索(用户名、昵称、手机号)
     * @param status   状态(0-禁用，1-正常，null-全部)
     * @param sortBy   排序字段
     * @param sortOrder 排序方式(asc-升序, desc-降序)
     * @return 用户列表
     */
    Page<UserDetailDTO> getUserList(Integer page, Integer pageSize, String keyword, Integer status, String sortBy, String sortOrder);
    
    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    UserDetailDTO getUserDetail(Long userId);
    
    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态(0-禁用，1-正常)
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, Integer status);
    
    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 新密码
     */
    String resetUserPassword(Long userId);
    
    /**
     * 获取用户统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getUserStats();
} 