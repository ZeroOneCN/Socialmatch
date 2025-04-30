package com.soical.server.dto.verification;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 教育认证提交DTO
 */
@Data
public class EducationVerificationDTO {
    /**
     * 学校名称
     */
    @NotBlank(message = "学校名称不能为空")
    private String school;
    
    /**
     * 学院
     */
    private String college;
    
    /**
     * 专业
     */
    @NotBlank(message = "专业不能为空")
    private String major;
    
    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    private String studentId;
    
    /**
     * 入学年份
     */
    @NotNull(message = "入学年份不能为空")
    @Min(value = 1950, message = "入学年份不合法")
    @Max(value = 2100, message = "入学年份不合法")
    private Integer enrollmentYear;
    
    /**
     * 学生证封面照片URL
     */
    @NotBlank(message = "请上传学生证封面照片")
    private String studentCardFront;
    
    /**
     * 学生证内页照片URL
     */
    @NotBlank(message = "请上传学生证内页照片")
    private String studentCardInside;
} 