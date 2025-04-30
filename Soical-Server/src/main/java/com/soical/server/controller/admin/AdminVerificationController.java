package com.soical.server.controller.admin;

import com.soical.server.dto.verification.VerificationAuditDTO;
import com.soical.server.entity.verification.VerificationConstants;
import com.soical.server.service.VerificationService;
import com.soical.server.common.Result;
import com.soical.server.vo.verification.VerificationVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 管理员认证审核控制器
 */
@RestController
@RequestMapping("/api/admin/verification")
public class AdminVerificationController {
    @Resource
    private VerificationService verificationService;
    
    /**
     * 获取所有身份认证列表
     */
    @GetMapping("/identity/all")
    public Result<List<VerificationVO>> getAllIdentityVerifications() {
        return Result.success(verificationService.getAllVerifications(VerificationConstants.Type.IDENTITY));
    }
    
    /**
     * 获取所有教育认证列表
     */
    @GetMapping("/education/all")
    public Result<List<VerificationVO>> getAllEducationVerifications() {
        return Result.success(verificationService.getAllVerifications(VerificationConstants.Type.EDUCATION));
    }
    
    /**
     * 获取待审核的身份认证列表
     */
    @GetMapping("/identity/pending")
    public Result<List<VerificationVO>> getPendingIdentityVerifications() {
        return Result.success(verificationService.getPendingVerifications(VerificationConstants.Type.IDENTITY));
    }
    
    /**
     * 获取待审核的教育认证列表
     */
    @GetMapping("/education/pending")
    public Result<List<VerificationVO>> getPendingEducationVerifications() {
        return Result.success(verificationService.getPendingVerifications(VerificationConstants.Type.EDUCATION));
    }
    
    /**
     * 获取认证详情
     */
    @GetMapping("/detail/{verificationId}")
    public Result<VerificationVO> getVerificationDetail(@PathVariable("verificationId") Long verificationId) {
        return Result.success(verificationService.getVerificationDetails(verificationId));
    }
    
    /**
     * 审核认证申请
     */
    @PostMapping("/audit")
    public Result<Boolean> auditVerification(@RequestBody @Valid VerificationAuditDTO dto) {
        return Result.success(verificationService.auditVerification(dto));
    }
} 