package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.dto.SystemSecuritySettingsDTO;
import com.soical.server.service.SystemSettingsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/api/system")
@Api(tags = "系统设置接口")
public class SystemController {

    private static final Logger log = LoggerFactory.getLogger(SystemController.class);
    
    @Autowired
    private SystemSettingsService systemSettingsService;

    /**
     * 获取安全设置
     */
    @GetMapping("/settings/security")
    @ApiOperation("获取安全设置")
    public Result<Map<String, String>> getSecuritySettings() {
        Map<String, String> data = systemSettingsService.getSettingsMapByGroup("security");
        return Result.ok(data);
    }

    /**
     * 更新安全设置
     */
    @PostMapping("/settings/security")
    @ApiOperation("更新安全设置")
    public Result<String> updateSecuritySettings(@RequestBody SystemSecuritySettingsDTO settings) {
        // 将DTO转换为Map
        Map<String, String> securitySettings = new HashMap<>();
        if (settings.getPasswordMinLength() != null) 
            securitySettings.put("passwordMinLength", settings.getPasswordMinLength().toString());
        if (settings.getPasswordStrength() != null) 
            securitySettings.put("passwordStrength", settings.getPasswordStrength().toString());
        if (settings.getPasswordExpireDays() != null) 
            securitySettings.put("passwordExpireDays", settings.getPasswordExpireDays().toString());
        if (settings.getMaxLoginAttempts() != null) 
            securitySettings.put("maxLoginAttempts", settings.getMaxLoginAttempts().toString());
        if (settings.getAccountLockTime() != null) 
            securitySettings.put("accountLockTime", settings.getAccountLockTime().toString());
        if (settings.getEnableCaptcha() != null) 
            securitySettings.put("enableCaptcha", settings.getEnableCaptcha().toString());
        if (settings.getSessionTimeout() != null) 
            securitySettings.put("sessionTimeout", settings.getSessionTimeout().toString());
        
        boolean result = systemSettingsService.saveOrUpdateSettings("security", securitySettings);
        return Result.ok("安全设置已更新");
    }
    
    /**
     * 获取AI助手设置
     */
    @GetMapping("/settings/ai")
    @ApiOperation("获取AI助手设置")
    public Result<Map<String, String>> getAiSettings() {
        try {
            Map<String, String> aiSettings = systemSettingsService.getSettingsMapByGroup("ai_assistant");
            
            // 出于安全考虑，从返回结果中移除API密钥，仅返回密钥是否已设置
            if (aiSettings.containsKey("api_key")) {
                String apiKey = aiSettings.get("api_key");
                if (apiKey != null && !apiKey.isEmpty()) {
                    aiSettings.put("api_key_set", "true");
                } else {
                    aiSettings.put("api_key_set", "false");
                }
                aiSettings.put("api_key", ""); // 不返回实际的API密钥
            }
            
            return Result.ok(aiSettings);
        } catch (Exception e) {
            log.error("获取AI助手设置失败", e);
            return Result.fail("获取AI助手设置失败: " + e.getMessage());
        }
    }
} 