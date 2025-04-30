package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.dto.LoginDTO;
import com.soical.server.dto.RegisterDTO;
import com.soical.server.dto.UserLoginResponseDTO;
import com.soical.server.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
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
        Long userId = userService.register(registerDTO);
        return Result.success(userId);
    }

    /**
     * 用户登录
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        UserLoginResponseDTO response = userService.login(loginDTO);
        return Result.success(response);
    }
} 