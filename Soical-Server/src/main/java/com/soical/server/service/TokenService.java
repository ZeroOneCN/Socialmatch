package com.soical.server.service;

/**
 * 令牌服务接口
 */
public interface TokenService {
    
    /**
     * 创建令牌
     * @param userId 用户ID
     * @return 令牌字符串
     */
    String createToken(Long userId);
    
    /**
     * 验证令牌
     * @param token 令牌字符串
     * @param userId 用户ID
     * @return 是否有效
     */
    boolean verify(String token, Long userId);
    
    /**
     * 从令牌中获取用户ID
     * @param token 令牌字符串
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);
    
    /**
     * 删除令牌
     * @param token 令牌字符串
     * @return 是否成功
     */
    boolean removeToken(String token);
} 