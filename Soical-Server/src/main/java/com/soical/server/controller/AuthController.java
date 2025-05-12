package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.dto.LoginDTO;
import com.soical.server.dto.RegisterDTO;
import com.soical.server.dto.UserLoginResponseDTO;
import com.soical.server.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@Slf4j
@Api(tags = "认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Long> register(@RequestBody RegisterDTO registerDTO) {
        log.info("开始处理用户注册请求: {}", registerDTO.getUsername());
        try {
            Long userId = userService.register(registerDTO);
            log.info("用户注册成功: {}, userId: {}", registerDTO.getUsername(), userId);
            return Result.success(userId);
        } catch (Exception e) {
            log.error("用户注册失败: {}, 错误: {}", registerDTO.getUsername(), e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        log.info("开始处理用户登录请求: {}", loginDTO.getUsername());
        try {
            UserLoginResponseDTO response = userService.login(loginDTO);
            log.info("用户登录成功: {}", loginDTO.getUsername());
            return Result.success(response);
        } catch (Exception e) {
            log.error("用户登录失败: {}, 错误: {}", loginDTO.getUsername(), e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
} 