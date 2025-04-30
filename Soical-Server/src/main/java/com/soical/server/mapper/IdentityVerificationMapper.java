package com.soical.server.mapper;

import com.soical.server.entity.verification.IdentityVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 身份认证表数据库操作接口
 */
@Mapper
public interface IdentityVerificationMapper {
    /**
     * 新增身份认证记录
     *
     * @param identityVerification 身份认证信息
     * @return 影响行数
     */
    int insert(IdentityVerification identityVerification);
    
    /**
     * 根据认证ID查询身份认证详情
     *
     * @param verificationId 认证ID
     * @return 身份认证信息
     */
    IdentityVerification selectByVerificationId(@Param("verificationId") Long verificationId);
    
    /**
     * 根据用户ID查询已通过的身份认证
     *
     * @param userId 用户ID
     * @return 身份认证信息
     */
    IdentityVerification selectApprovedByUserId(@Param("userId") Long userId);
} 