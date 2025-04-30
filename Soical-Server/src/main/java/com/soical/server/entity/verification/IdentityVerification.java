package com.soical.server.entity.verification;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 身份认证实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class IdentityVerification extends Verification {
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 身份证号
     */
    private String idNumber;
    
    /**
     * 身份证正面照片URL
     */
    private String idCardFront;
    
    /**
     * 身份证背面照片URL
     */
    private String idCardBack;
} 