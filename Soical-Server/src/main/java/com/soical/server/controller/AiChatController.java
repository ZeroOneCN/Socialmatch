package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.dto.AiChatRequestDTO;
import com.soical.server.dto.AiChatResponseDTO;
import com.soical.server.service.AiChatService;
import com.soical.server.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI聊天控制器
 */
@Slf4j
@Api(tags = "AI助手")
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @ApiOperation("发送消息给AI助手")
    @PostMapping("/chat")
    public Result<AiChatResponseDTO> sendMessage(@RequestBody AiChatRequestDTO request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            AiChatResponseDTO response = aiChatService.sendMessage(userId, request);
            return Result.ok(response);
        } catch (Exception e) {
            log.error("AI聊天请求处理失败", e);
            return Result.fail("处理请求时出错: " + e.getMessage());
        }
    }

    @ApiOperation("获取与AI助手的聊天历史")
    @GetMapping("/history")
    public Result<List<AiChatResponseDTO>> getChatHistory(
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            List<AiChatResponseDTO> history = aiChatService.getChatHistory(userId, limit);
            return Result.ok(history);
        } catch (Exception e) {
            log.error("获取AI聊天历史失败", e);
            return Result.fail("获取聊天历史时出错: " + e.getMessage());
        }
    }

    @ApiOperation("清除与AI助手的聊天历史")
    @DeleteMapping("/history")
    public Result<Boolean> clearChatHistory() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            boolean success = aiChatService.clearChatHistory(userId);
            return Result.ok(success);
        } catch (Exception e) {
            log.error("清除AI聊天历史失败", e);
            return Result.fail("清除聊天历史时出错: " + e.getMessage());
        }
    }
} 