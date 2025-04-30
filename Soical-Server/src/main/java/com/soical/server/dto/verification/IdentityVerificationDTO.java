package com.soical.server.dto.verification;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 身份认证提交DTO
 */
@Data
public class IdentityVerificationDTO {
    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "真实姓名长度应在2-20个字符之间")
    private String realName;
    
    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号格式不正确")
    private String idNumber;
    
    /**
     * 身份证正面照片URL
     */
    @NotBlank(message = "请上传身份证正面照片")
    private String idCardFront;
    
    /**
     * 身份证背面照片URL
     */
    @NotBlank(message = "请上传身份证背面照片")
    private String idCardBack;
} 