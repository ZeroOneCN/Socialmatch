package com.soical.server.entity.verification;

/**
 * 认证相关常量
 */
public class VerificationConstants {
    /**
     * 认证类型
     */
    public static class Type {
        /**
         * 身份认证
         */
        public static final String IDENTITY = "identity";
        
        /**
         * 教育认证
         */
        public static final String EDUCATION = "education";
    }
    
    /**
     * 认证状态
     */
    public static class Status {
        /**
         * 待审核
         */
        public static final String PENDING = "pending";
        
        /**
         * 已通过
         */
        public static final String APPROVED = "approved";
        
        /**
         * 已拒绝
         */
        public static final String REJECTED = "rejected";
    }
} 