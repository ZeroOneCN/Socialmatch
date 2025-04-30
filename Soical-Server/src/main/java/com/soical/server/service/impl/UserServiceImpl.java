package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import com.soical.server.config.SecuritySettingsManager;
import com.soical.server.dto.LoginDTO;
import com.soical.server.dto.RegisterDTO;
import com.soical.server.dto.UserLoginResponseDTO;
import com.soical.server.entity.User;
import com.soical.server.entity.UserProfile;
import com.soical.server.mapper.UserMapper;
import com.soical.server.service.UserProfileService;
import com.soical.server.service.UserService;
import com.soical.server.util.FileUtil;
import com.soical.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;
    private final JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Autowired
    private SecuritySettingsManager securitySettingsManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (isUsernameExists(registerDTO.getUsername())) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        // 检查手机号是否已存在
        if (isPhoneExists(registerDTO.getPhone())) {
            throw new BusinessException(ResultCode.USER_EXIST.getCode(), "手机号已注册");
        }
        
        // 应用密码强度策略
        validatePasswordStrength(registerDTO.getPassword());

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPhone(registerDTO.getPhone());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setGender(registerDTO.getGender());
        user.setBirthday(registerDTO.getBirthday());
        user.setStatus(1);
        save(user);

        // 创建用户资料
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(user.getUserId());
        userProfile.setNickname(registerDTO.getNickname());
        userProfileService.save(userProfile);

        return user.getUserId();
    }
    
    /**
     * 校验密码强度
     */
    private void validatePasswordStrength(String password) {
        // 获取安全设置
        int minLength = securitySettingsManager.getPasswordMinLength();
        List<String> strengthRequirements = securitySettingsManager.getPasswordStrength();
        
        // 校验密码长度
        if (password.length() < minLength) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), 
                    "密码长度不能少于" + minLength + "位");
        }
        
        // 校验密码复杂度
        boolean meetsRequirements = true;
        String errorMsg = "密码必须包含";
        
        for (String requirement : strengthRequirements) {
            switch (requirement) {
                case "lowercase":
                    if (!Pattern.compile("[a-z]").matcher(password).find()) {
                        meetsRequirements = false;
                        errorMsg += "小写字母、";
                    }
                    break;
                case "uppercase":
                    if (!Pattern.compile("[A-Z]").matcher(password).find()) {
                        meetsRequirements = false;
                        errorMsg += "大写字母、";
                    }
                    break;
                case "number":
                    if (!Pattern.compile("\\d").matcher(password).find()) {
                        meetsRequirements = false;
                        errorMsg += "数字、";
                    }
                    break;
                case "special":
                    if (!Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) {
                        meetsRequirements = false;
                        errorMsg += "特殊字符、";
                    }
                    break;
            }
        }
        
        if (!meetsRequirements) {
            // 移除最后的逗号和顿号
            errorMsg = errorMsg.substring(0, errorMsg.length() - 1);
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), errorMsg);
        }
    }
    
    /**
     * 检查用户名是否存在
     */
    private boolean isUsernameExists(String username) {
        return count(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }
    
    /**
     * 检查手机号是否存在
     */
    private boolean isPhoneExists(String phone) {
        return count(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) > 0;
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public UserLoginResponseDTO login(LoginDTO loginDTO) {
        // 查询用户
        User user = this.getUserByUsernameOrPhone(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 校验状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getUserId());
        
        // 获取或创建用户资料
        String nickname = getOrCreateUserProfile(user);
        
        // 构建响应对象
        return UserLoginResponseDTO.builder()
                .token(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(nickname)
                .avatar(user.getAvatar())
                .build();
    }

    /**
     * 根据用户名或手机号查询用户
     */
    @Transactional(readOnly = true)
    private User getUserByUsernameOrPhone(String usernameOrPhone) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, usernameOrPhone)
                .or()
                .eq(User::getPhone, usernameOrPhone));
    }

    /**
     * 获取或创建用户资料
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String getOrCreateUserProfile(User user) {
        try {
            // 获取资料
            UserProfile userProfile = userProfileService.getProfileByUserId(user.getUserId());
            return userProfile.getNickname();
        } catch (BusinessException e) {
            // 用户资料不存在时创建默认资料
            if (e.getCode().equals(ResultCode.USER_PROFILE_NOT_EXIST.getCode())) {
                log.warn("用户 {} 的资料不存在，将创建默认资料", user.getUserId());
                try {
                    UserProfile userProfile = new UserProfile();
                    userProfile.setUserId(user.getUserId());
                    userProfile.setNickname(user.getUsername()); // 默认使用用户名作为昵称
                    userProfileService.save(userProfile);
                    return userProfile.getNickname();
                } catch (Exception ex) {
                    log.error("创建用户资料失败", ex);
                }
            } else {
                log.error("获取用户资料失败", e);
            }
        } catch (Exception e) {
            log.error("获取用户资料时发生错误", e);
        }
        
        // 兜底返回用户名作为昵称
        return user.getUsername();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 出于安全考虑，清除密码
        user.setPassword(null);
        return user;
    }

    @Override
    public boolean updateStatus(Long userId, Integer status) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        user.setStatus(status);
        return updateById(user);
    }
    
    @Override
    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 上传头像
        String avatarUrl = FileUtil.uploadImage(file);
        
        // 更新用户头像
        user.setAvatar(avatarUrl);
        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ResultCode.FAILED);
        }
        
        // 同步头像到用户资料表
        try {
            UserProfile userProfile = userProfileService.getProfileByUserId(userId);
            userProfile.setAvatar(avatarUrl);
            userProfileService.updateById(userProfile);
        } catch (BusinessException e) {
            // 如果用户资料不存在，创建新的资料
            if (e.getCode().equals(ResultCode.USER_PROFILE_NOT_EXIST.getCode())) {
                UserProfile newProfile = new UserProfile();
                newProfile.setUserId(userId);
                newProfile.setAvatar(avatarUrl);
                
                // 初始化昵称
                newProfile.setNickname(user.getUsername());
                
                userProfileService.save(newProfile);
            } else {
                // 如果是其他错误，记录日志但不影响主流程
                log.error("同步头像到用户资料表失败", e);
            }
        }
        
        return avatarUrl;
    }
    
    @Override
    @Transactional
    public boolean updateUserBasicInfo(Long userId, Map<String, String> basicInfo) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 更新用户表中的基本信息
        boolean updated = false;
        if (basicInfo.containsKey("nickname")) {
            // 不再直接更新User表中不存在的nickname字段
            updated = true;
        }
        
        if (basicInfo.containsKey("avatar")) {
            String avatar = basicInfo.get("avatar");
            user.setAvatar(avatar);
            updated = true;
        }
        
        if (updated) {
            updateById(user);
        }
        
        // 同步到用户资料表
        try {
            UserProfile userProfile = userProfileService.getProfileByUserId(userId);
            boolean profileUpdated = false;
            
            if (basicInfo.containsKey("nickname")) {
                userProfile.setNickname(basicInfo.get("nickname"));
                profileUpdated = true;
            }
            
            if (basicInfo.containsKey("avatar")) {
                userProfile.setAvatar(basicInfo.get("avatar"));
                profileUpdated = true;
            }
            
            if (profileUpdated) {
                userProfileService.updateById(userProfile);
            }
            
            return true;
        } catch (BusinessException e) {
            // 如果找不到用户资料，可能是新用户，尝试创建
            if (e.getCode().equals(ResultCode.USER_PROFILE_NOT_EXIST.getCode())) {
                UserProfile newProfile = new UserProfile();
                newProfile.setUserId(userId);
                
                if (basicInfo.containsKey("nickname")) {
                    newProfile.setNickname(basicInfo.get("nickname"));
                } else {
                    newProfile.setNickname(user.getUsername());
                }
                
                if (basicInfo.containsKey("avatar")) {
                    newProfile.setAvatar(basicInfo.get("avatar"));
                } else {
                    newProfile.setAvatar(user.getAvatar());
                }
                
                userProfileService.save(newProfile);
                return true;
            }
            throw e;
        }
    }
    
    @Override
    @Transactional
    public boolean syncUserToProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        try {
            // 尝试获取用户资料
            UserProfile userProfile = userProfileService.getProfileByUserId(userId);
            
            // 同步昵称和头像，使用username作为昵称
            userProfile.setNickname(user.getUsername());
            userProfile.setAvatar(user.getAvatar());
            
            // 同步性别和生日等
            userProfile.setGender(user.getGender());
            if (user.getBirthday() != null) {
                userProfile.setBirthday(user.getBirthday().toString());
            }
            
            // 更新资料
            return userProfileService.updateById(userProfile);
        } catch (BusinessException e) {
            // 如果找不到用户资料，创建新的资料
            if (e.getCode().equals(ResultCode.USER_PROFILE_NOT_EXIST.getCode())) {
                UserProfile newProfile = new UserProfile();
                newProfile.setUserId(userId);
                newProfile.setNickname(user.getUsername());
                newProfile.setAvatar(user.getAvatar());
                newProfile.setGender(user.getGender());
                
                if (user.getBirthday() != null) {
                    newProfile.setBirthday(user.getBirthday().toString());
                }
                
                return userProfileService.save(newProfile);
            }
            throw e;
        }
    }
} 