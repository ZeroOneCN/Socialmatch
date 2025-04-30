package com.soical.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soical.server.dto.LoginDTO;
import com.soical.server.dto.UserLoginResponseDTO;
import com.soical.server.entity.User;
import com.soical.server.entity.UserProfile;
import com.soical.server.mapper.UserMapper;
import com.soical.server.mapper.UserProfileMapper;
import com.soical.server.service.UserService;
import com.soical.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 手动事务登录测试
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = new HashMap<>();
        
        // 开启手动事务
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        
        try {
            // 1. 查询用户
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.eq(User::getUsername, loginDTO.getUsername())
                    .or()
                    .eq(User::getPhone, loginDTO.getUsername());
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                result.put("code", 1002);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 2. 校验密码
            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                result.put("code", 1001);
                result.put("message", "用户名或密码错误");
                return result;
            }
            
            // 3. 校验状态
            if (user.getStatus() == 0) {
                result.put("code", 1004);
                result.put("message", "账号已禁用");
                return result;
            }
            
            // 4. 生成token
            String token = jwtUtil.generateToken(user.getUserId());
            
            // 5. 获取用户资料
            LambdaQueryWrapper<UserProfile> profileQueryWrapper = new LambdaQueryWrapper<>();
            profileQueryWrapper.eq(UserProfile::getUserId, user.getUserId());
            UserProfile userProfile = userProfileMapper.selectOne(profileQueryWrapper);
            
            String nickname = "";
            if (userProfile == null) {
                // 如果用户资料不存在，创建一个默认的
                userProfile = new UserProfile();
                userProfile.setUserId(user.getUserId());
                userProfile.setNickname(user.getUsername());
                userProfileMapper.insert(userProfile);
                nickname = userProfile.getNickname();
            } else {
                nickname = userProfile.getNickname();
            }
            
            // 6. 构建响应对象
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getUserId());
            data.put("username", user.getUsername());
            data.put("nickname", nickname);
            data.put("avatar", user.getAvatar());
            
            result.put("code", 200);
            result.put("message", "操作成功");
            result.put("data", data);
            
            // 提交事务
            transactionManager.commit(status);
            return result;
        } catch (Exception e) {
            // 回滚事务
            transactionManager.rollback(status);
            
            result.put("code", 500);
            result.put("message", "操作失败: " + e.getMessage());
            return result;
        }
    }
} 