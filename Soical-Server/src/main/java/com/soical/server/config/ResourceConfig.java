package com.soical.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 资源配置
 */
@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件访问路径
        String uploadPath = "file:" + System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        registry.addResourceHandler("/uploads/**").addResourceLocations(uploadPath);
    }
} 