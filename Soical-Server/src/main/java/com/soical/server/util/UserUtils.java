package com.soical.server.util;

import com.soical.server.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户工具类，用于获取当前登录用户信息
 */
@Slf4j
public class UserUtils {

    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("用户认证对象为空或未通过认证");
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            log.error("用户principal为null");
            return null;
        }
        
        log.debug("当前用户principal类型: {}", principal.getClass().getName());
        
        try {
            if (principal instanceof User) {
                User user = (User) principal;
                log.debug("从User对象获取userId: {}", user.getUserId());
                return user.getUserId();
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                // 从UserDetails中获取用户名，然后查询用户ID
                String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                log.debug("从UserDetails获取username: {}", username);
                // 尝试将用户名解析为用户ID
                try {
                    return Long.parseLong(username);
                } catch (NumberFormatException e) {
                    log.error("无法将username转换为userId: {}", username, e);
                }
            } else if (principal instanceof String) {
                log.debug("principal是字符串: {}", principal);
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    log.error("无法将字符串principal转换为userId: {}", principal, e);
                }
            } else if (principal instanceof Long) {
                log.debug("principal是Long类型: {}", principal);
                return (Long) principal;
            } else if (principal instanceof Integer) {
                log.debug("principal是Integer类型: {}", principal);
                return ((Integer) principal).longValue();
            } else if (principal instanceof Number) {
                log.debug("principal是Number类型: {}", principal);
                return ((Number) principal).longValue();
            } else if ("anonymousUser".equals(principal)) {
                log.debug("匿名用户");
                return null;
            } else {
                // 尝试调用toString()然后解析为Long
                try {
                    String principalStr = principal.toString();
                    log.debug("尝试将principal.toString()解析为userId: {}", principalStr);
                    return Long.parseLong(principalStr);
                } catch (NumberFormatException e) {
                    log.error("无法将principal.toString()转换为userId: {}", principal, e);
                }
            }
        } catch (Exception e) {
            log.error("获取用户ID时发生未预期的异常", e);
        }
        
        log.error("无法从principal中提取userId: {}", principal);
        return null;
    }
    
    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return ((User) principal).getUsername();
        }
        
        return authentication.getName();
    }
    
    /**
     * 判断当前用户是否已登录
     * @return 是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }
} 