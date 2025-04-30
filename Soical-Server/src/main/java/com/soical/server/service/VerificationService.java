package com.soical.server.service;

import com.soical.server.dto.verification.EducationVerificationDTO;
import com.soical.server.dto.verification.IdentityVerificationDTO;
import com.soical.server.dto.verification.VerificationAuditDTO;
import com.soical.server.vo.verification.UserVerificationStatusVO;
import com.soical.server.vo.verification.VerificationVO;

import java.util.List;

/**
 * 认证服务接口
 */
public interface VerificationService {
    /**
     * 提交身份认证
     *
     * @param userId 用户ID
     * @param dto 身份认证信息
     * @return 认证ID
     */
    Long submitIdentityVerification(Long userId, IdentityVerificationDTO dto);
    
    /**
     * 提交教育认证
     *
     * @param userId 用户ID
     * @param dto 教育认证信息
     * @return 认证ID
     */
    Long submitEducationVerification(Long userId, EducationVerificationDTO dto);
    
    /**
     * 审核认证申请
     *
     * @param dto 审核信息
     * @return 是否成功
     */
    boolean auditVerification(VerificationAuditDTO dto);
    
    /**
     * 获取认证详情
     *
     * @param verificationId 认证ID
     * @return 认证详情
     */
    VerificationVO getVerificationDetails(Long verificationId);
    
    /**
     * 获取用户的认证状态
     *
     * @param userId 用户ID
     * @return 用户认证状态
     */
    UserVerificationStatusVO getUserVerificationStatus(Long userId);
    
    /**
     * 获取所有认证列表
     *
     * @param type 认证类型
     * @return 认证列表
     */
    List<VerificationVO> getAllVerifications(String type);
    
    /**
     * 获取待审核的认证列表
     *
     * @param type 认证类型
     * @return 认证列表
     */
    List<VerificationVO> getPendingVerifications(String type);
    
    /**
     * 获取用户的认证申请列表
     *
     * @param userId 用户ID
     * @param type 认证类型
     * @return 认证列表
     */
    List<VerificationVO> getUserVerifications(Long userId, String type);
    
    /**
     * 检查用户是否已通过指定类型的认证
     *
     * @param userId 用户ID
     * @param type 认证类型
     * @return 是否已认证
     */
    boolean isUserVerified(Long userId, String type);
} 