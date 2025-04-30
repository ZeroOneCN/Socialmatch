package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.client.DeepSeekApiClient;
import com.soical.server.dto.AiChatRequestDTO;
import com.soical.server.dto.AiChatResponseDTO;
import com.soical.server.entity.AiChat;
import com.soical.server.mapper.AiChatMapper;
import com.soical.server.service.AiChatService;
import com.soical.server.service.SystemSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI聊天服务实现类
 */
@Slf4j
@Service
public class AiChatServiceImpl extends ServiceImpl<AiChatMapper, AiChat> implements AiChatService {

    @Autowired
    private DeepSeekApiClient deepSeekApiClient;

    @Autowired
    private SystemSettingsService systemSettingsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatResponseDTO sendMessage(Long userId, AiChatRequestDTO request) {
        if (userId == null || request == null || request.getMessage() == null) {
            throw new IllegalArgumentException("用户ID和消息内容不能为空");
        }

        try {
            // 检查AI助手功能是否启用
            String enabled = systemSettingsService.getSettingValue("ai_assistant", "enabled");
            if (!"true".equals(enabled)) {
                throw new IllegalStateException("AI助手功能未启用");
            }

            // 获取AI配置
            Map<String, String> aiSettings = systemSettingsService.getSettingsMapByGroup("ai_assistant");
            String apiKey = aiSettings.get("api_key");
            String baseUrl = aiSettings.get("api_base_url");
            String modelName = aiSettings.get("model_name");
            String systemPrompt = aiSettings.get("system_prompt");
            double temperature = Double.parseDouble(aiSettings.getOrDefault("temperature", "0.7"));
            int maxTokens = Integer.parseInt(aiSettings.getOrDefault("max_tokens", "2000"));

            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("未配置DeepSeek API密钥");
            }

            // 保存用户消息
            AiChat userChat = new AiChat();
            userChat.setUserId(userId);
            userChat.setMessage(request.getMessage());
            userChat.setRole("user");
            userChat.setCreateTime(new Date());
            save(userChat);

            // 获取历史对话（最近10条）
            List<AiChat> chatHistory = getChatHistoryEntities(userId, 10);
            
            // 转换为DeepSeek API所需格式
            List<Map<String, String>> messages = new ArrayList<>();
            for (AiChat chat : chatHistory) {
                Map<String, String> message = new HashMap<>();
                message.put("role", chat.getRole());
                message.put("content", chat.getMessage());
                messages.add(message);
            }

            // 调用DeepSeek API
            String aiResponse = deepSeekApiClient.chatCompletion(
                    apiKey, baseUrl, modelName, systemPrompt, 
                    messages, temperature, maxTokens);

            // 保存AI回复
            AiChat assistantChat = new AiChat();
            assistantChat.setUserId(userId);
            assistantChat.setMessage(aiResponse);
            assistantChat.setRole("assistant");
            assistantChat.setCreateTime(new Date());
            assistantChat.setTokens(estimateTokens(aiResponse)); // 估算token数量
            save(assistantChat);

            // 构建响应DTO
            AiChatResponseDTO responseDTO = new AiChatResponseDTO();
            responseDTO.setChatId(assistantChat.getChatId());
            responseDTO.setMessage(aiResponse);
            responseDTO.setRole("assistant");
            responseDTO.setCreateTime(assistantChat.getCreateTime());
            responseDTO.setTokens(assistantChat.getTokens());

            return responseDTO;
        } catch (Exception e) {
            log.error("AI聊天处理失败: userId={}", userId, e);
            
            // 发生错误时返回一个友好的错误消息
            AiChat errorChat = new AiChat();
            errorChat.setUserId(userId);
            errorChat.setMessage("抱歉，我暂时无法理解您的问题。请稍后再试或联系管理员。");
            errorChat.setRole("assistant");
            errorChat.setCreateTime(new Date());
            save(errorChat);

            AiChatResponseDTO errorResponse = new AiChatResponseDTO();
            errorResponse.setChatId(errorChat.getChatId());
            errorResponse.setMessage(errorChat.getMessage());
            errorResponse.setRole("assistant");
            errorResponse.setCreateTime(errorChat.getCreateTime());
            
            return errorResponse;
        }
    }

    @Override
    public List<AiChatResponseDTO> getChatHistory(Long userId, Integer limit) {
        if (userId == null) {
            return new ArrayList<>();
        }

        if (limit == null || limit <= 0) {
            limit = 20; // 默认获取最近20条记录
        }

        List<AiChat> chatHistory = getChatHistoryEntities(userId, limit);
        
        // 转换为DTO
        return chatHistory.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearChatHistory(Long userId) {
        if (userId == null) {
            return false;
        }

        try {
            LambdaQueryWrapper<AiChat> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AiChat::getUserId, userId);
            return remove(queryWrapper);
        } catch (Exception e) {
            log.error("清除聊天历史失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 获取聊天历史实体列表
     */
    private List<AiChat> getChatHistoryEntities(Long userId, Integer limit) {
        LambdaQueryWrapper<AiChat> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiChat::getUserId, userId)
                   .orderByDesc(AiChat::getCreateTime)
                   .last("LIMIT " + limit);
        
        List<AiChat> chatList = list(queryWrapper);
        
        // 按时间正序排列
        chatList.sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
        
        return chatList;
    }

    /**
     * 将AiChat实体转换为DTO
     */
    private AiChatResponseDTO convertToDTO(AiChat chat) {
        AiChatResponseDTO dto = new AiChatResponseDTO();
        dto.setChatId(chat.getChatId());
        dto.setMessage(chat.getMessage());
        dto.setRole(chat.getRole());
        dto.setCreateTime(chat.getCreateTime());
        dto.setTokens(chat.getTokens());
        return dto;
    }

    /**
     * 简单估算token数量（中文约1.5字符/token，英文约4字符/token）
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int chineseChars = 0;
        int otherChars = 0;
        
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                chineseChars++;
            } else {
                otherChars++;
            }
        }
        
        // 简单估算：中文1个字约0.7token，其他字符约0.25token
        return (int) (chineseChars * 0.7 + otherChars * 0.25);
    }
} 