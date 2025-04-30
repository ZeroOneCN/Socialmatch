package com.soical.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import com.soical.server.dto.AdminDTO;
import com.soical.server.dto.AdminLoginDTO;
import com.soical.server.entity.Admin;
import com.soical.server.mapper.AdminMapper;
import com.soical.server.service.AdminService;
import com.soical.server.util.JwtUtil;
import com.soical.server.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理员服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public String login(AdminLoginDTO loginDTO) {
        // 根据用户名查询管理员
        Admin admin = adminMapper.selectByUsername(loginDTO.getUsername());
        if (admin == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 验证状态
        if (admin.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 生成token
        return jwtUtil.generateAdminToken(admin.getAdminId());
    }

    @Override
    public AdminDTO getCurrentAdmin() {
        // 获取当前登录管理员ID
        Long adminId = SecurityUtil.getCurrentAdminId();

        // 查询管理员信息
        Admin admin = this.getById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 转换为DTO
        AdminDTO adminDTO = new AdminDTO();
        BeanUtils.copyProperties(admin, adminDTO);
        
        return adminDTO;
    }

    @Override
    public boolean updateAdminInfo(AdminDTO adminDTO) {
        // 获取当前登录管理员ID
        Long adminId = SecurityUtil.getCurrentAdminId();

        // 查询管理员信息
        Admin admin = this.getById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新管理员信息
        admin.setNickname(adminDTO.getNickname());
        admin.setAvatar(adminDTO.getAvatar());
        
        return this.updateById(admin);
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword) {
        // 获取当前登录管理员ID
        Long adminId = SecurityUtil.getCurrentAdminId();

        // 查询管理员信息
        Admin admin = this.getById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 更新密码
        admin.setPassword(passwordEncoder.encode(newPassword));
        
        return this.updateById(admin);
    }

    @Override
    public Admin getById(Long adminId) {
        return super.getById(adminId);
    }

    @Override
    public boolean updateById(Admin admin) {
        return super.updateById(admin);
    }
} 