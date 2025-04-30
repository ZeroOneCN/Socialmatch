package com.soical.server.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Token工具类
 * 用于从请求中获取token以及其他token相关操作
 */
@Slf4j
@Component
public class TokenUtil {

    /**
     * 从请求中获取token
     * 
     * @param request HTTP请求
     * @return 请求中的token，如果没有则返回null
     */
    public static String getRequestToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        // 从Header中获取
        String token = request.getHeader("Authorization");
        
        // 如果header中没有，尝试从参数中获取
        if (!StringUtils.hasText(token)) {
            token = request.getParameter("token");
        }
        
        // 处理"Bearer "前缀
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return token;
    }
    
    /**
     * 从token中获取用户ID
     * 适配器方法，委托给JwtUtil
     * 
     * @param token JWT token
     * @return 用户ID，如果无效则返回null
     */
    public Long getUserIdFromToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        
        try {
            JwtUtil jwtUtil = SpringContextUtil.getBean(JwtUtil.class);
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("从token获取用户ID失败", e);
            return null;
        }
    }
    
    /**
     * 验证token是否有效
     * 适配器方法，委托给JwtUtil
     * 
     * @param token JWT token
     * @param userId 用户ID
     * @return 是否有效
     */
    public boolean verify(String token, Long userId) {
        if (!StringUtils.hasText(token) || userId == null) {
            return false;
        }
        
        try {
            JwtUtil jwtUtil = SpringContextUtil.getBean(JwtUtil.class);
            Long tokenUserId = jwtUtil.getUserIdFromToken(token);
            
            // 检查token中的用户ID是否与传入的用户ID一致
            if (tokenUserId != null && tokenUserId.equals(userId)) {
                return jwtUtil.validateToken(token);
            }
            return false;
        } catch (Exception e) {
            log.error("验证token失败", e);
            return false;
        }
    }
    
    /**
     * 移除token
     * 这个方法可能需要与Redis等存储系统集成，暂时返回true
     * 
     * @param token JWT token
     * @return 是否成功
     */
    public boolean removeToken(String token) {
        // 实际实现应该将token加入黑名单或从Redis中删除
        if (!StringUtils.hasText(token)) {
            return false;
        }
        
        log.info("移除token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        return true;
    }
} 