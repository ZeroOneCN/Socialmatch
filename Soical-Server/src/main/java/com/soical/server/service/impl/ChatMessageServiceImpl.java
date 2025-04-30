package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.entity.ChatMessage;
import com.soical.server.entity.Conversation;
import com.soical.server.mapper.ChatMessageMapper;
import com.soical.server.service.ChatMessageService;
import com.soical.server.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * 聊天消息服务实现类
 */
@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveMessage(ChatMessage message) {
        if (message == null) {
            log.error("消息对象为空");
            return null;
        }
        
        // 打印完整的消息对象，帮助调试
        log.info("准备保存的消息详情: {}", message);
        
        // 确保所有必要字段都已设置
        if (message.getSenderId() == null || message.getReceiverId() == null) {
            log.error("消息缺少发送者或接收者信息: senderId={}, receiverId={}", 
                message.getSenderId(), message.getReceiverId());
            return null;
        }
        
        // 设置消息创建时间
        if (message.getCreateTime() == null) {
            message.setCreateTime(new java.util.Date());
        }
        
        // 初始化已读状态
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }
        
        // 初始化消息类型
        if (message.getMessageType() == null) {
            message.setMessageType(1); // 默认为文本消息
        }
        
        // 确保会话ID已设置，如果没有，则尝试创建或查找会话
        if (message.getConversationId() == null) {
            log.info("消息未设置会话ID，尝试查找或创建会话");
            Conversation conversation = conversationService.findByUsers(
                    message.getSenderId(), message.getReceiverId());
            
            if (conversation == null) {
                log.info("未找到会话，创建新会话");
                conversation = conversationService.createConversation(
                        message.getSenderId(), message.getReceiverId());
                if (conversation == null) {
                    log.error("创建会话失败");
                    return null;
                }
            }
            
            message.setConversationId(conversation.getConversationId());
            log.info("设置会话ID: {}", conversation.getConversationId());
        }
        
        try {
            // 直接使用SQL插入语句，避免ORM映射问题
            String sql = "INSERT INTO t_chat_message (conversation_id, sender_id, receiver_id, content, message_type, is_read, create_time) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            Object[] params = new Object[] {
                message.getConversationId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getContent(),
                message.getMessageType(),
                message.getIsRead() ? 1 : 0,
                message.getCreateTime()
            };
            
            log.info("执行SQL插入: {}, 参数: {}", sql, params);
            
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, message.getConversationId());
                ps.setLong(2, message.getSenderId());
                ps.setLong(3, message.getReceiverId());
                ps.setString(4, message.getContent());
                ps.setInt(5, message.getMessageType());
                ps.setInt(6, message.getIsRead() ? 1 : 0);
                ps.setTimestamp(7, new java.sql.Timestamp(message.getCreateTime().getTime()));
                return ps;
            }, keyHolder);
            
            Number messageId = keyHolder.getKey();
            if (messageId != null) {
                message.setMessageId(messageId.longValue());
                log.info("消息保存成功: ID={}", messageId.longValue());
                
                // 更新会话的最后一条消息
                updateConversationLastMessage(message);
                
                return messageId.longValue();
            } else {
                log.error("消息保存失败，无法获取生成的ID");
                return null;
            }
        } catch (Exception e) {
            log.error("保存消息时发生异常: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 更新会话的最后一条消息信息
     */
    private void updateConversationLastMessage(ChatMessage message) {
        try {
            // 获取会话
            Conversation conversation = conversationService.getById(message.getConversationId());
            if (conversation == null) {
                log.warn("无法更新会话最后消息，会话不存在: {}", message.getConversationId());
                return;
            }
            
            // 更新会话信息
            conversation.setLastMessage(message.getContent());
            conversation.setLastMessageTime(message.getCreateTime());
            
            // 更新未读消息计数
            if (conversation.getUserAId().equals(message.getReceiverId()) || 
                conversation.getUserBId().equals(message.getReceiverId())) {
                Integer currentCount = conversation.getUnreadCount();
                if (currentCount == null) {
                    currentCount = 0;
                }
                conversation.setUnreadCount(currentCount + 1);
            }
            
            // 保存更新
            boolean updated = conversationService.updateConversation(conversation);
            if (updated) {
                log.info("会话最后消息更新成功: {}", message.getConversationId());
            } else {
                log.warn("会话最后消息更新失败: {}", message.getConversationId());
            }
        } catch (Exception e) {
            log.error("更新会话最后消息时出错: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public List<ChatMessage> getMessagesByConversationId(Long conversationId) {
        if (conversationId == null) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getConversationId, conversationId)
                .orderByDesc(ChatMessage::getCreateTime);
        
        return this.list(queryWrapper);
    }
    
    @Override
    public List<ChatMessage> getMessagesBetweenUsers(Long senderId, Long receiverId, int page, int size) {
        if (senderId == null || receiverId == null || page < 1 || size < 1) {
            return Collections.emptyList();
        }
        
        int offset = (page - 1) * size;
        return baseMapper.getMessagesBetweenUsers(senderId, receiverId, size, offset);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markMessageAsRead(Long messageId) {
        if (messageId == null) {
            return false;
        }
        
        int rows = baseMapper.markAsRead(messageId);
        return rows > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markMessagesAsRead(Long conversationId, Long receiverId) {
        if (conversationId == null || receiverId == null) {
            return 0;
        }
        
        int rows = baseMapper.markConversationMessagesAsRead(conversationId, receiverId);
        
        // 如果有消息被标记为已读，重置会话的未读计数
        if (rows > 0) {
            conversationService.markAsRead(conversationId, receiverId);
        }
        
        return rows;
    }

    @Override
    public ChatMessage getById(Long messageId) {
        if (messageId == null) {
            return null;
        }
        log.info("获取消息详情，ID: {}", messageId);
        return this.baseMapper.selectById(messageId);
    }
} 