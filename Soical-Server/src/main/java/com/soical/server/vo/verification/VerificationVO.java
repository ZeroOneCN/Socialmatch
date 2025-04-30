package com.soical.server.vo.verification;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 认证信息展示VO
 */
@Data
@Accessors(chain = true)
public class VerificationVO {
    /**
     * 认证ID
     */
    private Long verificationId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 认证类型：identity-身份认证，education-教育认证
     */
    private String type;
    
    /**
     * 认证状态：pending-待审核，approved-已通过，rejected-已拒绝
     */
    private String status;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 认证提交时间
     */
    private LocalDateTime createTime;
    
    /**
     * 认证审核时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 认证详情（子类填充）
     */
    private Object details;
} 