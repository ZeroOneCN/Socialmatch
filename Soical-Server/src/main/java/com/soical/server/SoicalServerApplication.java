package com.soical.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 社交应用服务端启动类
 */
@SpringBootApplication
@MapperScan("com.soical.server.mapper")
@EnableTransactionManagement
public class SoicalServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoicalServerApplication.class, args);
    }
}