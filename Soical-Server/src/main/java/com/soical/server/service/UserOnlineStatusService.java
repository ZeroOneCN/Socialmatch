package com.soical.server.service;

import java.util.Map;
import java.util.List;

/**
 * 用户在线状态服务接口
 */
public interface UserOnlineStatusService {

    /**
     * 用户上线
     * @param userId 用户ID
     */
    void userOnline(Long userId);

    /**
     * 用户下线
     * @param userId 用户ID
     */
    void userOffline(Long userId);

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isUserOnline(Long userId);

    /**
     * 批量获取用户在线状态
     * @param userIds 用户ID列表
     * @return 用户ID到在线状态的映射，true表示在线，false表示离线
     */
    Map<Long, Boolean> batchGetUserOnlineStatus(List<Long> userIds);
} 