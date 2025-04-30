package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.entity.Conversation;
import com.soical.server.entity.UserProfile;
import com.soical.server.mapper.ConversationMapper;
import com.soical.server.service.ConversationService;
import com.soical.server.service.UserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 会话服务实现类
 */
@Slf4j
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {
    
    private final UserProfileService userProfileService;
    
    public ConversationServiceImpl(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }
    
    @Override
    public List<Conversation> getConversationsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        log.info("查询用户会话列表: {}", userId);
        
        // 使用关联查询获取带有用户资料的会话列表
        List<Conversation> conversations = baseMapper.getConversationsWithUserInfo(userId);
        
        if (conversations.isEmpty()) {
            log.info("用户 {} 没有会话", userId);
        } else {
            log.info("查询到 {} 个会话", conversations.size());
            
            // 确保非null字段，避免前端显示问题
            for (Conversation conversation : conversations) {
                if (conversation.getTargetUserNickname() == null) {
                    conversation.setTargetUserNickname("用户" + conversation.getTargetUserId());
                }
                if (conversation.getLastMessage() == null) {
                    conversation.setLastMessage("");
                }
            }
        }
        
        return conversations;
    }
    
    @Override
    public Conversation getById(Long conversationId) {
        if (conversationId == null) {
            return null;
        }
        return super.getById(conversationId);
    }
    
    @Override
    public Conversation findByUsers(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            return null;
        }
        return baseMapper.findByUsers(userId1, userId2);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConversation(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        
        // 确保userAId < userBId，保持一致性
        if (conversation.getUserAId() > conversation.getUserBId()) {
            Long temp = conversation.getUserAId();
            conversation.setUserAId(conversation.getUserBId());
            conversation.setUserBId(temp);
        }
        
        // 检查会话是否已存在
        Conversation existingConv = findByUsers(conversation.getUserAId(), conversation.getUserBId());
        if (existingConv != null) {
            return existingConv.getConversationId();
        }
        
        // 保存新会话
        boolean saved = this.save(conversation);
        if (saved) {
            log.info("新会话创建成功: {}", conversation.getConversationId());
            return conversation.getConversationId();
        } else {
            log.error("新会话创建失败");
            return null;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConversation(Conversation conversation) {
        if (conversation == null || conversation.getConversationId() == null) {
            return false;
        }
        
        boolean updated = this.updateById(conversation);
        if (updated) {
            log.info("会话更新成功: {}", conversation.getConversationId());
        } else {
            log.error("会话更新失败: {}", conversation.getConversationId());
        }
        return updated;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return false;
        }
        
        // 获取会话
        Conversation conversation = this.getById(conversationId);
        if (conversation == null) {
            log.warn("会话不存在: {}", conversationId);
            return false;
        }
        
        // 确保当前用户是会话的参与者
        if (!conversation.getUserAId().equals(userId) && !conversation.getUserBId().equals(userId)) {
            log.warn("用户 {} 无权访问会话 {}", userId, conversationId);
            return false;
        }
        
        // 重置未读计数
        int rows = baseMapper.resetUnreadCount(conversationId);
        return rows > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConversation(Long conversationId) {
        if (conversationId == null) {
            return false;
        }
        
        boolean removed = this.removeById(conversationId);
        if (removed) {
            log.info("会话删除成功: {}", conversationId);
        } else {
            log.error("会话删除失败: {}", conversationId);
        }
        return removed;
    }

    @Override
    public Conversation getConversationBetweenUsers(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            return null;
        }
        
        // 确保查询时userAId < userBId
        Long smallerId = userId1 < userId2 ? userId1 : userId2;
        Long largerId = userId1 < userId2 ? userId2 : userId1;
        
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserAId, smallerId)
                .eq(Conversation::getUserBId, largerId);
        
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Conversation createConversation(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            return null;
        }
        
        // 确保userAId < userBId
        Long smallerId = userId1 < userId2 ? userId1 : userId2;
        Long largerId = userId1 < userId2 ? userId2 : userId1;
        
        // 检查是否已存在会话
        Conversation existingConv = getConversationBetweenUsers(smallerId, largerId);
        if (existingConv != null) {
            return existingConv;
        }
        
        // 创建新会话
        Conversation conversation = new Conversation();
        conversation.setUserAId(smallerId);
        conversation.setUserBId(largerId);
        conversation.setLastMessageTime(new java.util.Date());
        conversation.setCreateTime(new java.util.Date());
        
        try {
            // 尝试设置未读消息数，如果字段不存在会被忽略
            conversation.setUnreadCount(0); 
        } catch (Exception e) {
            log.warn("设置unreadCount失败，可能是字段不存在: {}", e.getMessage());
        }
        
        try {
            boolean saved = this.save(conversation);
            if (saved) {
                log.info("新会话创建成功: {}", conversation.getConversationId());
                return conversation;
            } else {
                log.error("新会话创建失败");
                return null;
            }
        } catch (Exception e) {
            log.error("保存会话时出错: {}", e.getMessage());
            return null;
        }
    }
} 