package com.soical.server.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有不可知的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.FAILED.getCode(), e.getMessage());
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        log.error("业务异常", e);
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数异常", e);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public Result<Void> handleSecurityException(SecurityException e) {
        log.error("权限异常", e);
        return Result.error(ResultCode.FORBIDDEN.getCode(), e.getMessage());
    }
} 