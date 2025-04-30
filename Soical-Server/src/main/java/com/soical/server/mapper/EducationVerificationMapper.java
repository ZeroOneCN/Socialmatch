package com.soical.server.mapper;

import com.soical.server.entity.verification.EducationVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 教育认证表数据库操作接口
 */
@Mapper
public interface EducationVerificationMapper {
    /**
     * 新增教育认证记录
     *
     * @param educationVerification 教育认证信息
     * @return 影响行数
     */
    int insert(EducationVerification educationVerification);
    
    /**
     * 根据认证ID查询教育认证详情
     *
     * @param verificationId 认证ID
     * @return 教育认证信息
     */
    EducationVerification selectByVerificationId(@Param("verificationId") Long verificationId);
    
    /**
     * 根据用户ID查询已通过的教育认证
     *
     * @param userId 用户ID
     * @return 教育认证信息
     */
    EducationVerification selectApprovedByUserId(@Param("userId") Long userId);
} 