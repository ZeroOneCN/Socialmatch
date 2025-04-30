package com.soical.server.common;

import lombok.Getter;

/**
 * 响应状态码
 */
@Getter
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 失败
     */
    FAILED(500, "操作失败"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    
    /**
     * 禁止访问
     */
    FORBIDDEN(403, "没有相关权限"),
    
    /**
     * 访问被拒绝
     */
    ACCESS_DENIED(403, "访问被拒绝"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 用户名或密码错误
     */
    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    
    /**
     * 用户不存在
     */
    USER_NOT_EXIST(1002, "用户不存在"),
    
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1002, "用户不存在"),
    
    /**
     * 用户已存在
     */
    USER_EXIST(1003, "用户已存在"),
    
    /**
     * 用户已存在
     */
    USER_ALREADY_EXIST(1003, "用户已存在"),
    
    /**
     * 密码错误
     */
    PASSWORD_ERROR(1004, "密码错误"),
    
    /**
     * 旧密码错误
     */
    OLD_PASSWORD_ERROR(1005, "旧密码错误"),
    
    /**
     * 账号已禁用
     */
    USER_DISABLED(1006, "账号已禁用"),
    
    /**
     * 账号已禁用
     */
    ACCOUNT_DISABLED(1006, "账号已禁用"),
    
    /**
     * 用户资料不存在
     */
    USER_PROFILE_NOT_EXIST(1007, "用户资料不存在"),
    
    /**
     * 动态不存在
     */
    POST_NOT_EXIST(2001, "动态不存在"),
    
    /**
     * 评论不存在
     */
    COMMENT_NOT_EXIST(2002, "评论不存在"),
    
    /**
     * 管理员不存在
     */
    ADMIN_NOT_FOUND(3001, "管理员不存在"),
    
    /**
     * 管理员已存在
     */
    ADMIN_ALREADY_EXIST(3002, "管理员已存在"),
    
    /**
     * 参数检验失败
     */
    VALIDATE_FAILED(404, "参数检验失败");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 消息
     */
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
} 