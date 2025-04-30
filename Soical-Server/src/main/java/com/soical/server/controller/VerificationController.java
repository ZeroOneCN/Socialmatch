package com.soical.server.controller;

import com.soical.server.dto.verification.EducationVerificationDTO;
import com.soical.server.dto.verification.IdentityVerificationDTO;
import com.soical.server.entity.verification.VerificationConstants;
import com.soical.server.service.VerificationService;
import com.soical.server.common.Result;
import com.soical.server.vo.verification.UserVerificationStatusVO;
import com.soical.server.vo.verification.VerificationVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户认证控制器
 */
@RestController
@RequestMapping("/api/verification")
public class VerificationController {
    @Resource
    private VerificationService verificationService;
    
    /**
     * 获取用户认证状态
     */
    @GetMapping("/status")
    public Result<UserVerificationStatusVO> getVerificationStatus(@RequestParam("userId") Long userId) {
        return Result.success(verificationService.getUserVerificationStatus(userId));
    }
    
    /**
     * 提交身份认证
     */
    @PostMapping("/identity")
    public Result<Long> submitIdentityVerification(
            @RequestParam("userId") Long userId,
            @RequestBody @Valid IdentityVerificationDTO dto) {
        return Result.success(verificationService.submitIdentityVerification(userId, dto));
    }
    
    /**
     * 提交教育认证
     */
    @PostMapping("/education")
    public Result<Long> submitEducationVerification(
            @RequestParam("userId") Long userId,
            @RequestBody @Valid EducationVerificationDTO dto) {
        return Result.success(verificationService.submitEducationVerification(userId, dto));
    }
    
    /**
     * 获取用户身份认证列表
     */
    @GetMapping("/identity/list")
    public Result<List<VerificationVO>> getUserIdentityVerifications(@RequestParam("userId") Long userId) {
        return Result.success(verificationService.getUserVerifications(userId, VerificationConstants.Type.IDENTITY));
    }
    
    /**
     * 获取用户教育认证列表
     */
    @GetMapping("/education/list")
    public Result<List<VerificationVO>> getUserEducationVerifications(@RequestParam("userId") Long userId) {
        return Result.success(verificationService.getUserVerifications(userId, VerificationConstants.Type.EDUCATION));
    }
    
    /**
     * 获取认证详情
     */
    @GetMapping("/detail/{verificationId}")
    public Result<VerificationVO> getVerificationDetail(@PathVariable("verificationId") Long verificationId) {
        return Result.success(verificationService.getVerificationDetails(verificationId));
    }
    
    /**
     * 检查用户是否已认证
     */
    @GetMapping("/check")
    public Result<Boolean> checkUserVerified(
            @RequestParam("userId") Long userId,
            @RequestParam("type") String type) {
        return Result.success(verificationService.isUserVerified(userId, type));
    }
} 