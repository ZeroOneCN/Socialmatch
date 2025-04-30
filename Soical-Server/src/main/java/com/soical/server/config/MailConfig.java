package com.soical.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 邮件配置类
 * 提供默认的JavaMailSender Bean，实际发送邮件时会使用动态配置
 */
@Configuration
public class MailConfig {
    
    /**
     * 创建默认的JavaMailSender Bean
     * 注意：此Bean仅作为默认配置，实际发送邮件时会使用动态配置的参数
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        // 设置一个默认的空配置，实际发送时会动态设置
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        return mailSender;
    }
} 