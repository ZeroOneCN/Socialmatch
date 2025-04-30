package com.soical.server.common;

import lombok.Data;

/**
 * 统一响应结果类
 */
@Data
public class Result<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        return result;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }
    
    /**
     * 成功响应（ok方法，与success等价）
     */
    public static <T> Result<T> ok(T data) {
        return success(data);
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> ok() {
        return success();
    }
    
    /**
     * 成功响应（int类型）
     */
    public static Result<Integer> ok(int data) {
        return success(data);
    }
    
    /**
     * 成功响应（boolean类型）
     */
    public static Result<Boolean> ok(boolean data) {
        return success(data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.FAILED.getCode());
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败响应（fail方法，用自定义消息）
     */
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.FAILED.getCode());
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败响应（使用错误码）
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }
} 