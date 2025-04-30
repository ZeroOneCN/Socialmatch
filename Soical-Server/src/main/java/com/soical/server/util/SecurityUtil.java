package com.soical.server.util;

import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 安全工具类
 */
@Component
public class SecurityUtil {

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isUser = authorities.stream()
                    .anyMatch(authority -> "ROLE_USER".equals(authority.getAuthority()));
            
            if (isUser) {
                return (Long) authentication.getPrincipal();
            }
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }
    
    /**
     * 获取当前登录管理员ID
     *
     * @return 管理员ID
     */
    public static Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream()
                    .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
            
            if (isAdmin) {
                return (Long) authentication.getPrincipal();
            }
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }
    
    /**
     * 检查当前用户是否已认证
     *
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());
    }
    
    /**
     * 检查当前用户是否是管理员
     *
     * @return 是否是管理员
     */
    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            return authorities.stream()
                    .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        }
        return false;
    }
} 