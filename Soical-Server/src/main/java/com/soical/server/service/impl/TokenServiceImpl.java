package com.soical.server.service.impl;

import com.soical.server.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 令牌服务实现类
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {
    
    @Value("${jwt.secret:soical_app_secret_key}")
    private String secret;
    
    @Value("${jwt.expiration:86400}")
    private long expiration; // 默认24小时
    
    @Override
    public String createToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        
        Date createdDate = new Date();
        Date expirationDate = new Date(createdDate.getTime() + expiration * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    @Override
    public boolean verify(String token, Long userId) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            
            String subject = claims.getSubject();
            if (subject == null) {
                log.error("Token中subject为null");
                return false;
            }
            
            Long tokenUserId;
            try {
                tokenUserId = Long.valueOf(subject);
            } catch (NumberFormatException e) {
                log.error("Token中subject不是有效的Long值: {}", subject, e);
                return false;
            }
            
            Date expirationDate = claims.getExpiration();
            
            boolean valid = userId.equals(tokenUserId) && 
                           expirationDate != null && 
                           expirationDate.after(new Date());
                           
            if (!valid) {
                log.debug("Token验证失败: userId不匹配或已过期, token userId: {}, 提供的userId: {}, 过期时间: {}",
                        tokenUserId, userId, expirationDate);
            }
            
            return valid;
        } catch (ExpiredJwtException e) {
            log.error("Token已过期", e);
            return false;
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return false;
        }
    }
    
    @Override
    public Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            log.error("Token为空");
            return null;
        }
        
        try {
            log.debug("开始解析token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                log.error("解析token后claims为null");
                return null;
            }
            
            log.debug("解析token成功, claims: {}", claims);
            
            // 首先尝试从subject获取
            String subject = claims.getSubject();
            if (subject != null) {
                try {
                    Long userId = Long.valueOf(subject);
                    log.debug("从token的subject成功提取userId: {}", userId);
                    return userId;
                } catch (NumberFormatException e) {
                    log.warn("无法将token的subject转换为userId: {}", subject, e);
                    // 继续尝试其他方法
                }
            }
            
            // 然后尝试从claims中的userId字段获取
            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                log.debug("从token的claims中找到userId字段, 类型: {}, 值: {}", 
                        userIdObj.getClass().getName(), userIdObj);
                
                if (userIdObj instanceof Integer) {
                    Long userId = ((Integer) userIdObj).longValue();
                    log.debug("从Integer转换为Long: {}", userId);
                    return userId;
                } else if (userIdObj instanceof Long) {
                    log.debug("直接使用Long类型的userId: {}", userIdObj);
                    return (Long) userIdObj;
                } else if (userIdObj instanceof String) {
                    try {
                        Long userId = Long.parseLong((String) userIdObj);
                        log.debug("从String转换为Long: {}", userId);
                        return userId;
                    } catch (NumberFormatException e) {
                        log.error("无法将userId字符串转换为Long: {}", userIdObj, e);
                    }
                } else {
                    log.error("不支持的userId类型: {}", userIdObj.getClass().getName());
                }
            } else {
                log.error("token的claims中不存在userId字段");
            }
            
            return null;
        } catch (ExpiredJwtException e) {
            log.error("Token已过期", e);
            return null;
        } catch (Exception e) {
            log.error("从Token获取用户ID时发生异常", e);
            return null;
        }
    }
    
    @Override
    public boolean removeToken(String token) {
        // JWT是无状态的，不需要服务端存储，因此不支持主动失效
        // 实际应用中可以使用Redis等存储黑名单实现
        log.warn("JWT不支持主动失效，请考虑使用Redis实现黑名单");
        return true;
    }
    
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("解析Token时发现Token已过期", e);
            throw e;
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage(), e);
            return null;
        }
    }
} 