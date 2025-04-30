package com.soical.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.dto.AiChatRequestDTO;
import com.soical.server.dto.AiChatResponseDTO;
import com.soical.server.entity.AiChat;

import java.util.List;

/**
 * AI聊天服务接口
 */
public interface AiChatService extends IService<AiChat> {

    /**
     * 发送消息给AI助手并获取回复
     * @param userId 用户ID
     * @param request 聊天请求
     * @return AI回复
     */
    AiChatResponseDTO sendMessage(Long userId, AiChatRequestDTO request);

    /**
     * 获取用户与AI助手的聊天历史
     * @param userId 用户ID
     * @param limit 限制返回的消息数量，默认20条
     * @return 聊天历史记录
     */
    List<AiChatResponseDTO> getChatHistory(Long userId, Integer limit);

    /**
     * 清除用户与AI助手的聊天历史
     * @param userId 用户ID
     * @return 是否清除成功
     */
    boolean clearChatHistory(Long userId);
} 