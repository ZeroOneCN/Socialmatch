package com.soical.server.dto;

import lombok.Data;

import java.util.Date;

/**
 * 注册DTO
 */
@Data
public class RegisterDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 昵称
     */
    private String nickname;
} 