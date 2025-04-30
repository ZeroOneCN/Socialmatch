package com.soical.server.config.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置
 */
@Configuration
@Data
public class OssConfig {
    
    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;
    
    @Value("${aliyun.oss.accessKeyId:}")
    private String accessKeyId;
    
    @Value("${aliyun.oss.accessKeySecret:}")
    private String accessKeySecret;
    
    @Value("${aliyun.oss.bucketName:}")
    private String bucketName;
    
    @Value("${aliyun.oss.url:}")
    private String ossUrl;
    
    @Bean
    public OSS ossClient() {
        if (endpoint.isEmpty() || accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            return null;
        }
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
} 