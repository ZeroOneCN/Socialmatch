package com.soical.server.service.impl;

import com.soical.server.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public Integer countUserConversations(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        try {
            // 查询用户参与的对话数量
            // 这里假设有一个t_chat_conversation表记录用户间的对话
            // 如果实际表结构不同，请根据实际情况调整SQL
            String sql = "SELECT COUNT(DISTINCT conversation_id) FROM t_chat_message " +
                         "WHERE sender_id = ? OR receiver_id = ?";
            
            // 可以根据实际表结构调整
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, userId);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("统计用户对话数失败: userId={}", userId, e);
            // 出错时返回0
            return 0;
        }
    }
} 