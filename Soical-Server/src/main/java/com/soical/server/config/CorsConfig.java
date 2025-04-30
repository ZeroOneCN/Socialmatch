package com.soical.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许跨域的头部信息
        config.addAllowedHeader("*");
        // 允许跨域的方法
        config.addAllowedMethod("*");
        // 允许跨域的来源
        config.addAllowedOriginPattern("*");
        // 是否允许携带cookie
        config.setAllowCredentials(true);
        // 跨域允许时间
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 