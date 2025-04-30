package com.soical.server.service;

import com.soical.server.dto.AdminDTO;
import com.soical.server.dto.AdminLoginDTO;
import com.soical.server.entity.Admin;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 管理员登录
     *
     * @param loginDTO 登录参数
     * @return token
     */
    String login(AdminLoginDTO loginDTO);
    
    /**
     * 获取当前登录管理员信息
     *
     * @return 管理员信息
     */
    AdminDTO getCurrentAdmin();
    
    /**
     * 更新管理员信息
     *
     * @param adminDTO 管理员信息
     * @return 是否成功
     */
    boolean updateAdminInfo(AdminDTO adminDTO);
    
    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(String oldPassword, String newPassword);
    
    /**
     * 通过ID获取管理员信息
     *
     * @param adminId 管理员ID
     * @return 管理员信息
     */
    Admin getById(Long adminId);
    
    /**
     * 更新管理员信息
     *
     * @param admin 管理员信息
     * @return 是否成功
     */
    boolean updateById(Admin admin);
} 