package com.soical.server.service.impl;

import com.soical.server.dto.verification.EducationVerificationDTO;
import com.soical.server.dto.verification.IdentityVerificationDTO;
import com.soical.server.dto.verification.VerificationAuditDTO;
import com.soical.server.entity.verification.EducationVerification;
import com.soical.server.entity.verification.IdentityVerification;
import com.soical.server.entity.verification.Verification;
import com.soical.server.entity.verification.VerificationConstants;
import com.soical.server.exception.BusinessException;
import com.soical.server.mapper.EducationVerificationMapper;
import com.soical.server.mapper.IdentityVerificationMapper;
import com.soical.server.mapper.VerificationMapper;
import com.soical.server.service.VerificationService;
import com.soical.server.vo.verification.UserVerificationStatusVO;
import com.soical.server.vo.verification.VerificationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 */
@Service
public class VerificationServiceImpl implements VerificationService {
    @Resource
    private VerificationMapper verificationMapper;
    
    @Resource
    private IdentityVerificationMapper identityVerificationMapper;
    
    @Resource
    private EducationVerificationMapper educationVerificationMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitIdentityVerification(Long userId, IdentityVerificationDTO dto) {
        // 检查用户是否已经提交过正在审核的身份认证
        List<Verification> userVerifications = verificationMapper.selectByUserIdAndType(userId, VerificationConstants.Type.IDENTITY);
        if (userVerifications != null && userVerifications.stream().anyMatch(v -> 
                VerificationConstants.Status.PENDING.equals(v.getStatus()))) {
            throw new BusinessException("您已提交身份认证申请，请等待审核");
        }
        
        // 创建基础认证记录
        Verification verification = new Verification()
                .setUserId(userId)
                .setType(VerificationConstants.Type.IDENTITY)
                .setStatus(VerificationConstants.Status.PENDING)
                .setCreateTime(LocalDateTime.now());
        verificationMapper.insert(verification);
        
        // 创建身份认证记录
        IdentityVerification identityVerification = new IdentityVerification();
        BeanUtils.copyProperties(verification, identityVerification);
        BeanUtils.copyProperties(dto, identityVerification);
        identityVerificationMapper.insert(identityVerification);
        
        return verification.getVerificationId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitEducationVerification(Long userId, EducationVerificationDTO dto) {
        // 检查用户是否已经提交过正在审核的教育认证
        List<Verification> userVerifications = verificationMapper.selectByUserIdAndType(userId, VerificationConstants.Type.EDUCATION);
        if (userVerifications != null && userVerifications.stream().anyMatch(v -> 
                VerificationConstants.Status.PENDING.equals(v.getStatus()))) {
            throw new BusinessException("您已提交教育认证申请，请等待审核");
        }
        
        // 创建基础认证记录
        Verification verification = new Verification()
                .setUserId(userId)
                .setType(VerificationConstants.Type.EDUCATION)
                .setStatus(VerificationConstants.Status.PENDING)
                .setCreateTime(LocalDateTime.now());
        verificationMapper.insert(verification);
        
        // 创建教育认证记录
        EducationVerification educationVerification = new EducationVerification();
        BeanUtils.copyProperties(verification, educationVerification);
        BeanUtils.copyProperties(dto, educationVerification);
        educationVerificationMapper.insert(educationVerification);
        
        return verification.getVerificationId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditVerification(VerificationAuditDTO dto) {
        // 获取认证记录
        Verification verification = verificationMapper.selectById(dto.getVerificationId());
        if (verification == null) {
            throw new BusinessException("认证记录不存在");
        }
        
        // 检查认证状态
        if (!VerificationConstants.Status.PENDING.equals(verification.getStatus())) {
            throw new BusinessException("该认证已审核，无需重复操作");
        }
        
        // 如果是拒绝需要有拒绝原因
        if (VerificationConstants.Status.REJECTED.equals(dto.getStatus())) {
            if (!StringUtils.hasText(dto.getRejectReason())) {
                throw new BusinessException("拒绝时请填写拒绝原因");
            }
        }
        
        // 更新认证状态
        return verificationMapper.updateStatus(
                dto.getVerificationId(),
                dto.getStatus(),
                dto.getRejectReason()) > 0;
    }
    
    @Override
    public VerificationVO getVerificationDetails(Long verificationId) {
        // 获取基础认证信息
        Verification verification = verificationMapper.selectById(verificationId);
        if (verification == null) {
            throw new BusinessException("认证记录不存在");
        }
        
        VerificationVO vo = new VerificationVO();
        BeanUtils.copyProperties(verification, vo);
        
        // 根据认证类型获取详细信息
        if (VerificationConstants.Type.IDENTITY.equals(verification.getType())) {
            IdentityVerification identityVerification = identityVerificationMapper.selectByVerificationId(verificationId);
            if (identityVerification != null) {
                // 脱敏处理身份证号
                identityVerification.setIdNumber(maskIdNumber(identityVerification.getIdNumber()));
                vo.setDetails(identityVerification);
            }
        } else if (VerificationConstants.Type.EDUCATION.equals(verification.getType())) {
            EducationVerification educationVerification = educationVerificationMapper.selectByVerificationId(verificationId);
            if (educationVerification != null) {
                vo.setDetails(educationVerification);
            }
        }
        
        return vo;
    }
    
    @Override
    public UserVerificationStatusVO getUserVerificationStatus(Long userId) {
        UserVerificationStatusVO statusVO = new UserVerificationStatusVO()
                .setUserId(userId)
                .setIdentityStatus("not_submitted")
                .setEducationStatus("not_submitted");
        
        // 获取用户身份认证状态
        List<Verification> identityVerifications = verificationMapper.selectByUserIdAndType(userId, VerificationConstants.Type.IDENTITY);
        if (identityVerifications != null && !identityVerifications.isEmpty()) {
            // 按创建时间降序排序，取最新的一条
            identityVerifications.sort((v1, v2) -> v2.getCreateTime().compareTo(v1.getCreateTime()));
            Verification latestIdentity = identityVerifications.get(0);
            statusVO.setIdentityStatus(latestIdentity.getStatus());
            
            // 如果已认证，设置实名
            if (VerificationConstants.Status.APPROVED.equals(latestIdentity.getStatus())) {
                IdentityVerification identityVerification = identityVerificationMapper.selectByVerificationId(latestIdentity.getVerificationId());
                if (identityVerification != null) {
                    statusVO.setRealName(identityVerification.getRealName());
                }
            }
        }
        
        // 获取用户教育认证状态
        List<Verification> educationVerifications = verificationMapper.selectByUserIdAndType(userId, VerificationConstants.Type.EDUCATION);
        if (educationVerifications != null && !educationVerifications.isEmpty()) {
            // 按创建时间降序排序，取最新的一条
            educationVerifications.sort((v1, v2) -> v2.getCreateTime().compareTo(v1.getCreateTime()));
            Verification latestEducation = educationVerifications.get(0);
            statusVO.setEducationStatus(latestEducation.getStatus());
            
            // 如果已认证，设置学校
            if (VerificationConstants.Status.APPROVED.equals(latestEducation.getStatus())) {
                EducationVerification educationVerification = educationVerificationMapper.selectByVerificationId(latestEducation.getVerificationId());
                if (educationVerification != null) {
                    statusVO.setSchool(educationVerification.getSchool());
                }
            }
        }
        
        return statusVO;
    }
    
    @Override
    public List<VerificationVO> getPendingVerifications(String type) {
        List<Verification> pendingList = verificationMapper.selectByStatusAndType(VerificationConstants.Status.PENDING, type);
        if (pendingList == null || pendingList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return pendingList.stream().map(verification -> {
            VerificationVO vo = new VerificationVO();
            BeanUtils.copyProperties(verification, vo);
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<VerificationVO> getAllVerifications(String type) {
        List<Verification> allList = verificationMapper.selectByType(type);
        if (allList == null || allList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return allList.stream().map(verification -> {
            VerificationVO vo = new VerificationVO();
            BeanUtils.copyProperties(verification, vo);
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<VerificationVO> getUserVerifications(Long userId, String type) {
        List<Verification> userVerifications = verificationMapper.selectByUserIdAndType(userId, type);
        if (userVerifications == null || userVerifications.isEmpty()) {
            return new ArrayList<>();
        }
        
        return userVerifications.stream().map(verification -> {
            VerificationVO vo = new VerificationVO();
            BeanUtils.copyProperties(verification, vo);
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public boolean isUserVerified(Long userId, String type) {
        Verification verification = verificationMapper.selectApprovedByUserIdAndType(userId, type);
        return verification != null;
    }
    
    /**
     * 身份证号脱敏处理
     *
     * @param idNumber 身份证号
     * @return 脱敏后的身份证号
     */
    private String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.length() < 8) {
            return idNumber;
        }
        
        // 保留前3位和后4位，中间用星号代替
        int length = idNumber.length();
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < length - 7; i++) {
            stars.append("*");
        }
        
        return idNumber.substring(0, 3) + stars.toString() + idNumber.substring(length - 4);
    }
}