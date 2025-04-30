package com.soical.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.dto.LoginDTO;
import com.soical.server.dto.RegisterDTO;
import com.soical.server.dto.UserLoginResponseDTO;
import com.soical.server.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户ID
     */
    Long register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 用户登录响应（包含token和用户基本信息）
     */
    UserLoginResponseDTO login(LoginDTO loginDTO);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long userId, Integer status);
    
    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像URL
     */
    String updateAvatar(Long userId, MultipartFile file);
    
    /**
     * 更新用户基本信息
     *
     * @param userId 用户ID
     * @param basicInfo 基本信息
     * @return 是否成功
     */
    boolean updateUserBasicInfo(Long userId, Map<String, String> basicInfo);
    
    /**
     * 同步用户基本信息到用户资料表
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean syncUserToProfile(Long userId);
} 