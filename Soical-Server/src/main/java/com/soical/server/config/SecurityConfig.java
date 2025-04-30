package com.soical.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * 安全配置类
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider;
    private final CorsFilter corsFilter;

    @Autowired
    public SecurityConfig(ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider, CorsFilter corsFilter) {
        this.jwtAuthenticationFilterProvider = jwtAuthenticationFilterProvider;
        this.corsFilter = corsFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = jwtAuthenticationFilterProvider.getIfAvailable();
        
        if (jwtAuthenticationFilter == null) {
            log.warn("JwtAuthenticationFilter尚未准备好");
        }
        
        HttpSecurity security = http
                .cors()
                .and()
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class);
        
        if (jwtAuthenticationFilter != null) {
            security.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }
        
        security.sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 放行接口
                        .antMatchers("/api/auth/**").permitAll()
                        .antMatchers("/api/admin/login").permitAll() // 管理员登录接口放行
                        .antMatchers("/api/common/**").permitAll()
                        // 放行WebSocket端点
                        .antMatchers("/ws/chat").permitAll() // WebSocket端点允许所有访问，认证在WebSocketAuthInterceptor中进行
                        // 放行上传文件访问路径
                        .antMatchers("/uploads/**").permitAll()
                        // Swagger/Knife4j相关放行
                        .antMatchers("/swagger-ui/**").permitAll()
                        .antMatchers("/swagger-ui.html").permitAll()
                        .antMatchers("/swagger-resources/**").permitAll()
                        .antMatchers("/v2/api-docs").permitAll()
                        .antMatchers("/v3/api-docs/**").permitAll()
                        .antMatchers("/doc.html").permitAll()
                        .antMatchers("/webjars/**").permitAll()
                        // 管理员相关接口需要ADMIN角色
                        .antMatchers("/api/admin/**").hasRole("ADMIN")
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                );
        return security.build();
    }
} 