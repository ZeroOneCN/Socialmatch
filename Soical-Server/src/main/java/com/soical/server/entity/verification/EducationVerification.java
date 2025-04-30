package com.soical.server.entity.verification;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 教育认证实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class EducationVerification extends Verification {
    /**
     * 学校名称
     */
    private String school;
    
    /**
     * 学院
     */
    private String college;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 学号
     */
    private String studentId;
    
    /**
     * 入学年份
     */
    private Integer enrollmentYear;
    
    /**
     * 学生证封面照片URL
     */
    private String studentCardFront;
    
    /**
     * 学生证内页照片URL
     */
    private String studentCardInside;
} 