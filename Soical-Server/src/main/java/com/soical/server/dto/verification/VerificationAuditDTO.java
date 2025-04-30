package com.soical.server.dto.verification;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 认证审核操作DTO
 */
@Data
public class VerificationAuditDTO {
    /**
     * 认证ID
     */
    @NotNull(message = "认证ID不能为空")
    private Long verificationId;
    
    /**
     * 审核结果：approved-通过，rejected-拒绝
     */
    @NotBlank(message = "审核结果不能为空")
    private String status;
    
    /**
     * 拒绝原因（status为rejected时必填）
     */
    @Size(max = 200, message = "拒绝原因不能超过200个字符")
    private String rejectReason;
} 