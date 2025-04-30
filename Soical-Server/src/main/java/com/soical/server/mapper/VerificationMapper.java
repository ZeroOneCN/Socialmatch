package com.soical.server.mapper;

import com.soical.server.entity.verification.Verification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 认证基础表数据库操作接口
 */
@Mapper
public interface VerificationMapper {
    /**
     * 新增认证记录
     *
     * @param verification 认证信息
     * @return 影响行数
     */
    int insert(Verification verification);
    
    /**
     * 更新认证状态
     *
     * @param verificationId 认证ID
     * @param status 认证状态
     * @param rejectReason 拒绝原因
     * @return 影响行数
     */
    int updateStatus(@Param("verificationId") Long verificationId, 
                     @Param("status") String status, 
                     @Param("rejectReason") String rejectReason);
    
    /**
     * 根据ID查询认证信息
     *
     * @param verificationId 认证ID
     * @return 认证信息
     */
    Verification selectById(@Param("verificationId") Long verificationId);
    
    /**
     * 查询用户的认证记录
     *
     * @param userId 用户ID
     * @param type 认证类型
     * @return 认证列表
     */
    List<Verification> selectByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    
    /**
     * 查询指定类型的所有认证记录
     *
     * @param type 认证类型
     * @return 认证列表
     */
    List<Verification> selectByType(@Param("type") String type);
    
    /**
     * 查询指定状态的认证记录
     *
     * @param status 认证状态
     * @param type 认证类型
     * @return 认证列表
     */
    List<Verification> selectByStatusAndType(@Param("status") String status, @Param("type") String type);
    
    /**
     * 查询用户是否有已通过的指定类型认证
     *
     * @param userId 用户ID
     * @param type 认证类型
     * @return 认证信息
     */
    Verification selectApprovedByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
} 