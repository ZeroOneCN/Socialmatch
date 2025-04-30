package com.soical.server.config;

import com.soical.server.entity.User;
import com.soical.server.service.UserService;
import com.soical.server.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectProvider<UserService> userServiceProvider;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, ObjectProvider<UserService> userServiceProvider) {
        this.jwtUtil = jwtUtil;
        this.userServiceProvider = userServiceProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.debug("处理请求: {}", requestURI);
        
        try {
            // 获取token
            String token = getTokenFromRequest(request);

            // 验证token
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 从token中获取用户ID
                Long userId = jwtUtil.getUserIdFromToken(token);
                log.debug("JWT Token有效，用户/管理员ID: {}", userId);
                
                if (userId != null) {
                    // 判断是否是管理员token
                    boolean isAdmin = jwtUtil.isAdminToken(token);
                    
                    // 创建权限列表
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    if (isAdmin) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        log.debug("管理员已认证，ID: {}", userId);
                    } else {
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        log.debug("用户已认证，ID: {}", userId);
                    }
                    
                    // 创建authentication - 使用userId作为principal，避免UserUtils中的类型转换问题
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, authorities
                    );
                    
                    // 设置authentication到SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("无法从token中解析有效的用户ID");
                }
            } else {
                if (StringUtils.hasText(token)) {
                    log.debug("无效的JWT Token: {}", token);
                }
            }
        } catch (Exception ex) {
            log.error("JWT Token 验证失败", ex);
            // 出现异常时，清除安全上下文
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取token
     *
     * @param request 请求
     * @return token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 