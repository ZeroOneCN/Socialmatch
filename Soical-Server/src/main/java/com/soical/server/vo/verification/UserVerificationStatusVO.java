package com.soical.server.vo.verification;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户认证状态VO
 */
@Data
@Accessors(chain = true)
public class UserVerificationStatusVO {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 身份认证状态：not_submitted-未提交，pending-审核中，approved-已通过，rejected-已拒绝
     */
    private String identityStatus;
    
    /**
     * 教育认证状态：not_submitted-未提交，pending-审核中，approved-已通过，rejected-已拒绝
     */
    private String educationStatus;
    
    /**
     * 用户实名
     */
    private String realName;
    
    /**
     * 学校名称
     */
    private String school;
} 