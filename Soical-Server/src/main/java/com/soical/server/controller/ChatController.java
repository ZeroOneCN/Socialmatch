package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.dto.CreateConversationDTO;
import com.soical.server.entity.ChatMessage;
import com.soical.server.entity.Conversation;
import com.soical.server.service.ChatMessageQueueService;
import com.soical.server.service.ChatMessageService;
import com.soical.server.service.ConversationService;
import com.soical.server.util.UserUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@Api(tags = "聊天接口")
public class ChatController {
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    @Autowired
    private javax.sql.DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    @Autowired
    private ChatMessageQueueService chatMessageQueueService;
    
    @ApiOperation("获取用户的会话列表")
    @GetMapping("/conversations")
    public Result<List<Conversation>> getConversations() {
        // 获取当前登录用户ID
        Long userId = UserUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        
        List<Conversation> conversations = conversationService.getConversationsByUserId(userId);
        return Result.ok(conversations);
    }
    
    @ApiOperation("创建或获取与指定用户的会话")
    @PostMapping("/conversation")
    public Result<Conversation> createOrGetConversation(@RequestBody CreateConversationDTO dto) {
        // 获取当前登录用户ID
        Long currentUserId = UserUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.fail("用户未登录");
        }
        
        Long targetUserId = dto.getTargetUserId();
        if (targetUserId == null) {
            return Result.fail("目标用户ID不能为空");
        }
        
        // 检查是否已存在会话
        Conversation conversation = conversationService.getConversationBetweenUsers(currentUserId, targetUserId);
        if (conversation == null) {
            // 创建新会话
            conversation = conversationService.createConversation(currentUserId, targetUserId);
        }
        
        return Result.ok(conversation);
    }
    
    @ApiOperation("获取会话详情")
    @GetMapping("/conversation")
    public Result<Conversation> getConversation(
            @ApiParam(value = "会话ID", required = true) @RequestParam Long conversationId) {
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            return Result.fail("会话不存在");
        }
        return Result.ok(conversation);
    }
    
    @ApiOperation("标记会话为已读")
    @PostMapping("/conversation/read")
    public Result<Boolean> markConversationAsRead(
            @ApiParam(value = "会话ID", required = true) @RequestParam Long conversationId,
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId) {
        boolean result = conversationService.markAsRead(conversationId, userId);
        return Result.ok(result);
    }
    
    @ApiOperation("删除会话")
    @DeleteMapping("/conversation")
    public Result<Boolean> deleteConversation(
            @ApiParam(value = "会话ID", required = true) @RequestParam Long conversationId) {
        boolean result = conversationService.deleteConversation(conversationId);
        return Result.ok(result);
    }
    
    @ApiOperation("获取会话的消息列表")
    @GetMapping("/messages/{conversationId}")
    public Result<List<ChatMessage>> getMessages(
            @ApiParam(value = "会话ID", required = true) @PathVariable Long conversationId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByConversationId(conversationId);
        return Result.ok(messages);
    }
    
    @ApiOperation("获取两个用户之间的消息")
    @GetMapping("/messages/between")
    public Result<List<ChatMessage>> getMessagesBetweenUsers(
            @ApiParam(value = "用户A ID", required = true) @RequestParam Long userId1,
            @ApiParam(value = "用户B ID", required = true) @RequestParam Long userId2,
            @ApiParam(value = "页码", required = true) @RequestParam(defaultValue = "1") int page,
            @ApiParam(value = "每页条数", required = true) @RequestParam(defaultValue = "20") int size) {
        List<ChatMessage> messages = chatMessageService.getMessagesBetweenUsers(userId1, userId2, page, size);
        return Result.ok(messages);
    }
    
    @ApiOperation("发送消息")
    @PostMapping("/send")
    public Result<ChatMessage> sendMessage(@RequestBody final ChatMessage message) {
        // 获取当前登录用户ID
        final Long currentUserId = UserUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.fail("用户未登录");
        }
        
        // 设置发送者ID
        message.setSenderId(currentUserId);
        
        // 记录详细日志
        log.info("接收到发送消息请求 - 发送者: {}, 接收者: {}, 内容: {}", 
            currentUserId, message.getReceiverId(), message.getContent());
        
        // 保存消息
        try {
            // 设置必要的字段
            if (message.getCreateTime() == null) {
                message.setCreateTime(new Date());
            }
            if (message.getIsRead() == null) {
                message.setIsRead(false);
            }
            if (message.getMessageType() == null) {
                message.setMessageType(1); // 默认为文本消息
            }
            
            // 确保会话存在
            final Conversation conversation = conversationService.findByUsers(currentUserId, message.getReceiverId());
            final Conversation finalConversation;
            
            if (conversation == null) {
                log.info("未找到会话，正在创建新会话...");
                final Conversation newConversation = conversationService.createConversation(currentUserId, message.getReceiverId());
                if (newConversation == null) {
                    log.error("创建会话失败");
                    return Result.fail("创建会话失败");
                }
                log.info("新会话创建成功，ID: {}", newConversation.getConversationId());
                finalConversation = newConversation;
            } else {
                log.info("找到已存在会话，ID: {}", conversation.getConversationId());
                finalConversation = conversation;
            }
            
            // 设置会话ID
            final Long conversationId = finalConversation.getConversationId();
            message.setConversationId(conversationId);
            
            log.info("准备保存消息到数据库: conversation_id={}, sender_id={}, receiver_id={}, content={}", 
                message.getConversationId(), message.getSenderId(), message.getReceiverId(), message.getContent());
            
            // 尝试直接使用SQL插入
            Long messageId = null;
            
            // 提取需要在lambda中使用的所有变量，确保它们是final
            final Long finalReceiverID = message.getReceiverId();
            final String finalContent = message.getContent();
            final Integer finalMessageType = message.getMessageType();
            final Boolean finalIsRead = message.getIsRead();
            final Date finalCreateTime = message.getCreateTime();
            
            try {
                // 使用直接SQL插入方法
                final String sql = "INSERT INTO t_chat_message (conversation_id, sender_id, receiver_id, content, message_type, is_read, create_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                final KeyHolder keyHolder = new GeneratedKeyHolder();
                final JdbcTemplate localJdbcTemplate = new JdbcTemplate(dataSource);
                
                final int result = localJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        ps.setLong(1, conversationId);
                        ps.setLong(2, currentUserId);
                        ps.setLong(3, finalReceiverID);
                        ps.setString(4, finalContent);
                        ps.setInt(5, finalMessageType);
                        ps.setInt(6, finalIsRead ? 1 : 0);
                        ps.setTimestamp(7, new java.sql.Timestamp(finalCreateTime.getTime()));
                        return ps;
                    },
                    keyHolder
                );
                
                if (result > 0 && keyHolder.getKey() != null) {
                    messageId = keyHolder.getKey().longValue();
                    message.setMessageId(messageId);
                    log.info("消息直接SQL插入成功，ID: {}", messageId);
                    
                    // 更新会话的最后消息
                    finalConversation.setLastMessage(finalContent);
                    finalConversation.setLastMessageTime(finalCreateTime);
                    
                    // 更新未读消息计数
                    if (finalConversation.getUserAId().equals(finalReceiverID) || 
                        finalConversation.getUserBId().equals(finalReceiverID)) {
                        final Integer currentCount = finalConversation.getUnreadCount() != null ? 
                                               finalConversation.getUnreadCount() : 0;
                        finalConversation.setUnreadCount(currentCount + 1);
                    }
                    
                    conversationService.updateConversation(finalConversation);
                    log.info("会话更新成功");
                    
                    // 通过Redis发布消息
                    if (chatMessageQueueService.publishMessage(message)) {
                        log.info("消息已发布到Redis: {}", message.getMessageId());
                    } else {
                        log.warn("消息发布到Redis失败: {}", message.getMessageId());
                    }
                } else {
                    log.error("消息直接SQL插入失败，尝试使用服务方法");
                }
            } catch (Exception e) {
                log.error("消息直接SQL插入出错: {}", e.getMessage(), e);
                log.info("尝试使用服务方法保存");
            }
            
            // 如果直接SQL插入失败，使用服务方法
            if (messageId == null) {
                log.info("使用服务方法保存消息");
                messageId = chatMessageService.saveMessage(message);
                if (messageId != null) {
                    message.setMessageId(messageId);
                    
                    // 通过Redis发布消息
                    if (chatMessageQueueService.publishMessage(message)) {
                        log.info("消息已发布到Redis: {}", message.getMessageId());
                    } else {
                        log.warn("消息发布到Redis失败: {}", message.getMessageId());
                    }
                }
            }
            
            if (messageId == null) {
                log.error("消息保存失败，无法获取消息ID");
                return Result.fail("消息保存失败");
            }
            
            // 查询完整消息
            ChatMessage savedMessage = chatMessageService.getById(messageId);
            if (savedMessage == null) {
                savedMessage = message;
            }
            
            return Result.ok(savedMessage);
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.fail("发送消息失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("标记消息为已读")
    @PostMapping("/message/read")
    public Result<Boolean> markMessageAsRead(
            @ApiParam(value = "消息ID", required = true) @RequestParam Long messageId) {
        boolean result = chatMessageService.markMessageAsRead(messageId);
        return Result.ok(result);
    }
    
    @ApiOperation("批量标记消息为已读")
    @PostMapping("/messages/read")
    public Result<Integer> markMessagesAsRead(
            @ApiParam(value = "会话ID", required = true) @RequestParam Long conversationId,
            @ApiParam(value = "接收者ID", required = true) @RequestParam Long receiverId) {
        int count = chatMessageService.markMessagesAsRead(conversationId, receiverId);
        return Result.ok(count);
    }
    
    @ApiOperation("测试直接SQL插入消息")
    @PostMapping("/test/direct-insert")
    public Result<Map<String, Object>> testDirectInsert(@RequestBody final ChatMessage message) {
        final Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取当前登录用户ID
            final Long currentUserId = UserUtils.getCurrentUserId();
            if (currentUserId == null) {
                return Result.fail("用户未登录");
            }
            
            log.info("执行直接SQL插入测试，当前用户：{}, 接收者：{}", currentUserId, message.getReceiverId());
            
            // 设置发送者ID
            message.setSenderId(currentUserId);
            
            // 填充必要的默认值
            if (message.getCreateTime() == null) {
                message.setCreateTime(new Date());
            }
            if (message.getIsRead() == null) {
                message.setIsRead(false);
            }
            if (message.getMessageType() == null) {
                message.setMessageType(1);
            }
            
            // 查找会话ID
            final Conversation conversation = conversationService.findByUsers(currentUserId, message.getReceiverId());
            final Conversation finalConversation;
            
            if (conversation == null) {
                log.info("未找到会话，创建新会话");
                final Conversation newConversation = conversationService.createConversation(currentUserId, message.getReceiverId());
                if (newConversation == null) {
                    return Result.fail("创建会话失败");
                }
                finalConversation = newConversation;
            } else {
                finalConversation = conversation;
            }
            
            // 保存会话ID供后续使用
            final Long conversationId = finalConversation.getConversationId();
            message.setConversationId(conversationId);
            
            // 定义SQL语句
            final String sql = "INSERT INTO t_chat_message (conversation_id, sender_id, receiver_id, content, message_type, is_read, create_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            // 创建参数和JdbcTemplate实例
            final String content = message.getContent();
            final Integer messageType = message.getMessageType();
            final Boolean isRead = message.getIsRead();
            final Date createTime = message.getCreateTime();
            final Long receiverId = message.getReceiverId();
            
            final JdbcTemplate localJdbcTemplate = new JdbcTemplate(dataSource);
            final KeyHolder keyHolder = new GeneratedKeyHolder();
            
            // 执行插入操作
            final int rowsAffected = localJdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, conversationId);
                    ps.setLong(2, currentUserId);
                    ps.setLong(3, receiverId);
                    ps.setString(4, content);
                    ps.setInt(5, messageType);
                    ps.setInt(6, isRead ? 1 : 0);
                    ps.setTimestamp(7, new java.sql.Timestamp(createTime.getTime()));
                    return ps;
                },
                keyHolder
            );
            
            result.put("rowsAffected", rowsAffected);
            
            final Number key = keyHolder.getKey();
            if (key != null) {
                final Long messageId = key.longValue();
                result.put("messageId", messageId);
                
                // 查询插入后的记录
                final String querySql = "SELECT * FROM t_chat_message WHERE message_id = ?";
                final List<Map<String, Object>> messages = localJdbcTemplate.queryForList(querySql, messageId);
                
                if (!messages.isEmpty()) {
                    result.put("insertedMessage", messages.get(0));
                }
                
                // 更新会话
                finalConversation.setLastMessage(content);
                finalConversation.setLastMessageTime(createTime);
                
                // 更新未读消息计数
                if (finalConversation.getUserAId().equals(receiverId) || 
                    finalConversation.getUserBId().equals(receiverId)) {
                    final Integer currentCount = finalConversation.getUnreadCount() != null ? 
                                           finalConversation.getUnreadCount() : 0;
                    finalConversation.setUnreadCount(currentCount + 1);
                }
                
                conversationService.updateConversation(finalConversation);
                result.put("conversationUpdated", true);
            } else {
                result.put("error", "无法获取生成的ID");
            }
            
            return Result.ok(result);
        } catch (Exception e) {
            log.error("测试直接SQL插入时出错", e);
            result.put("error", e.getMessage());
            result.put("stackTrace", Arrays.toString(e.getStackTrace()));
            return Result.fail("插入消息失败: " + e.getMessage());
        }
    }
} 