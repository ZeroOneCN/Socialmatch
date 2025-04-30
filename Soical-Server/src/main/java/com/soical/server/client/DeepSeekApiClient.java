package com.soical.server.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek API客户端
 */
@Slf4j
@Component
public class DeepSeekApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DeepSeekApiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 向DeepSeek API发送聊天请求
     *
     * @param apiKey      API密钥
     * @param baseUrl     API基础URL
     * @param modelName   模型名称
     * @param systemPrompt 系统提示词
     * @param messages    对话历史消息
     * @param temperature 温度参数
     * @param maxTokens   最大token数
     * @return AI助手回复
     */
    public String chatCompletion(String apiKey, String baseUrl, String modelName, String systemPrompt,
                              List<Map<String, String>> messages, double temperature, int maxTokens) {
        try {
            // 构建请求URL
            String url = baseUrl + "/chat/completions";

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelName);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            // 添加系统提示词
            ArrayNode messagesArray = requestBody.putArray("messages");
            
            // 添加系统提示词
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                ObjectNode systemMessage = messagesArray.addObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
            }

            // 添加对话历史
            for (Map<String, String> message : messages) {
                ObjectNode messageNode = messagesArray.addObject();
                messageNode.put("role", message.get("role"));
                messageNode.put("content", message.get("content"));
            }

            // 发送请求
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            // 解析响应
            JsonNode responseBody = objectMapper.readTree(response.getBody());
            String content = responseBody.path("choices").path(0).path("message").path("content").asText();

            // 获取用量信息
            JsonNode usage = responseBody.path("usage");
            int promptTokens = usage.path("prompt_tokens").asInt();
            int completionTokens = usage.path("completion_tokens").asInt();
            int totalTokens = usage.path("total_tokens").asInt();

            log.info("DeepSeek API调用成功: prompt_tokens={}, completion_tokens={}, total_tokens={}",
                    promptTokens, completionTokens, totalTokens);

            return content;

        } catch (Exception e) {
            log.error("DeepSeek API调用失败", e);
            return "抱歉，我现在无法回答您的问题。请稍后再试。";
        }
    }
} 