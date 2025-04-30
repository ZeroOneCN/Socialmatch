package com.soical.server.config;

import com.soical.server.websocket.ChatWebSocketHandler;
import com.soical.server.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("注册WebSocket处理器: 路径=/ws/chat");
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
               .addInterceptors(webSocketAuthInterceptor)
               .setAllowedOrigins("*"); // 允许所有跨域请求，生产环境应当限制
        log.info("WebSocket处理器注册完成");
    }
    
    /**
     * 配置WebSocket会话属性
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        log.info("配置WebSocket容器参数");
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 设置消息缓冲区大小
        container.setMaxTextMessageBufferSize(8192);
        // 设置二进制消息缓冲区大小
        container.setMaxBinaryMessageBufferSize(8192);
        // 设置会话空闲超时时间（毫秒）
        container.setMaxSessionIdleTimeout(60000L);
        return container;
    }
} 