package com.soical.server.controller.admin;

import com.soical.server.common.Result;
import com.soical.server.service.SystemSettingsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端AI配置控制器
 */
@Slf4j
@Api(tags = "管理端AI配置")
@RestController
@RequestMapping("/api/admin/ai-config")
public class AdminAiConfigController {

    @Autowired
    private SystemSettingsService systemSettingsService;

    @ApiOperation("获取AI助手配置")
    @GetMapping
    public Result<Map<String, String>> getAiConfig() {
        try {
            Map<String, String> config = systemSettingsService.getSettingsMapByGroup("ai_assistant");
            // 出于安全考虑，从返回结果中移除API密钥，仅返回密钥是否已设置
            if (config.containsKey("api_key")) {
                String apiKey = config.get("api_key");
                if (apiKey != null && !apiKey.isEmpty()) {
                    config.put("api_key_set", "true");
                } else {
                    config.put("api_key_set", "false");
                }
                config.put("api_key", ""); // 不返回实际的API密钥
            }
            return Result.ok(config);
        } catch (Exception e) {
            log.error("获取AI助手配置失败", e);
            return Result.fail("获取配置失败: " + e.getMessage());
        }
    }

    @ApiOperation("更新AI助手配置")
    @PutMapping
    public Result<Boolean> updateAiConfig(@RequestBody Map<String, String> config) {
        try {
            boolean success = systemSettingsService.saveOrUpdateSettings("ai_assistant", config);
            if (success) {
                log.info("AI助手配置已更新");
                Result<Boolean> result = Result.ok(success);
                result.setMessage("配置已更新");
                return result;
            } else {
                return Result.fail("更新配置失败");
            }
        } catch (Exception e) {
            log.error("更新AI助手配置失败", e);
            return Result.fail("更新配置失败: " + e.getMessage());
        }
    }

    @ApiOperation("启用/禁用AI助手")
    @PutMapping("/toggle")
    public Result<Boolean> toggleAiEnabled(@RequestParam Boolean enabled) {
        try {
            boolean success = systemSettingsService.saveOrUpdateSetting(
                    "ai_assistant", "enabled", enabled.toString(), "AI助手功能是否启用");
            if (success) {
                log.info("AI助手已{}启用", enabled ? "" : "禁");
                Result<Boolean> result = Result.ok(success);
                result.setMessage("AI助手已" + (enabled ? "启用" : "禁用"));
                return result;
            } else {
                return Result.fail("操作失败");
            }
        } catch (Exception e) {
            log.error("切换AI助手启用状态失败", e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    @ApiOperation("设置DeepSeek API密钥")
    @PutMapping("/api-key")
    public Result<Boolean> setApiKey(@RequestParam String apiKey) {
        try {
            boolean success = systemSettingsService.saveOrUpdateSetting(
                    "ai_assistant", "api_key", apiKey, "DeepSeek API密钥");
            if (success) {
                log.info("DeepSeek API密钥已更新");
                Result<Boolean> result = Result.ok(success);
                result.setMessage("API密钥已更新");
                return result;
            } else {
                return Result.fail("更新API密钥失败");
            }
        } catch (Exception e) {
            log.error("更新DeepSeek API密钥失败", e);
            return Result.fail("更新API密钥失败: " + e.getMessage());
        }
    }
} 