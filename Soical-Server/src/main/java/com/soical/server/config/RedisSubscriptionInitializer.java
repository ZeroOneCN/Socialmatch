package com.soical.server.config;

import com.soical.server.service.ChatMessageQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Redis订阅初始化组件
 * 应用启动时自动启动消息订阅
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriptionInitializer implements CommandLineRunner {

    private final ChatMessageQueueService chatMessageQueueService;

    @Override
    public void run(String... args) throws Exception {
        log.info("应用启动，初始化Redis消息订阅...");
        chatMessageQueueService.startMessageSubscription();
        log.info("Redis消息订阅初始化完成");
    }
} 