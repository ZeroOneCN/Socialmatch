package com.soical.server.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
@Slf4j
public class JwtUtil {

    /**
     * 密钥
     */
    private String secret = "soical-jwt-secret";

    /**
     * 过期时间（毫秒）
     */
    private Long expiration = 604800000L; // 7天

    /**
     * 生成用户token
     *
     * @param userId 用户ID
     * @return token
     */
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "user");
        return generateToken(claims);
    }

    /**
     * 生成管理员token
     *
     * @param adminId 管理员ID
     * @return token
     */
    public String generateAdminToken(Long adminId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", adminId);
        claims.put("type", "admin");
        return generateToken(claims);
    }

    /**
     * 从token中获取用户ID
     *
     * @param token token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null && claims.get("userId") != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Integer) {
                    return ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                } else if (userIdObj instanceof String) {
                    try {
                        return Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException e) {
                        log.error("无法将userId字符串转换为Long: {}", userIdObj, e);
                        return null;
                    }
                } else {
                    log.error("从token中获取的userId类型不支持: {}", userIdObj.getClass().getName());
                    return null;
                }
            }
            log.error("从token中未找到userId字段");
            return null;
        } catch (ExpiredJwtException e) {
            log.error("token已过期", e);
            return null;
        } catch (Exception e) {
            log.error("从token中获取userId时发生异常", e);
            return null;
        }
    }

    /**
     * 判断token是否为管理员token
     *
     * @param token token
     * @return 是否是管理员token
     */
    public boolean isAdminToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && "admin".equals(claims.get("type"));
        } catch (Exception e) {
            log.error("判断token是否为管理员token时发生异常", e);
            return false;
        }
    }

    /**
     * 验证token是否有效
     *
     * @param token token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.error("token已过期", e);
            return false;
        } catch (Exception e) {
            log.error("验证token时发生异常", e);
            return false;
        }
    }

    /**
     * 生成token
     *
     * @param claims 数据声明
     * @return token
     */
    private String generateToken(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从token中获取数据声明
     *
     * @param token token
     * @return 数据声明
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析token失败: {}", e.getMessage());
            return null;
        }
    }
} 