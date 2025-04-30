package com.soical.server.dto;

import lombok.Data;

/**
 * 登录DTO
 */
@Data
public class LoginDTO {

    /**
     * 用户名/手机号
     */
    private String username;

    /**
     * 密码
     */
    private String password;
} 