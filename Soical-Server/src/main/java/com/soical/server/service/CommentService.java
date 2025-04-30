package com.soical.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.dto.CommentDTO;
import com.soical.server.entity.Comment;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<Comment> {
    
    /**
     * 发表评论
     *
     * @param userId  用户ID
     * @param postId  动态ID
     * @param content 评论内容
     * @return 评论ID
     */
    Long addComment(Long userId, Long postId, String content);
    
    /**
     * 发表评论或回复评论
     *
     * @param userId     用户ID
     * @param postId     动态ID
     * @param content    评论内容
     * @param parentId   父评论ID，如果是回复评论则不为null
     * @param replyUserId 被回复用户ID，如果是回复评论则不为null
     * @return 评论ID
     */
    Long addComment(Long userId, Long postId, String content, Long parentId, Long replyUserId);
    
    /**
     * 获取动态评论列表
     *
     * @param postId   动态ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 评论列表
     */
    Page<CommentDTO> getCommentList(Long postId, Integer page, Integer pageSize);
    
    /**
     * 删除评论
     *
     * @param userId    用户ID
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean deleteComment(Long userId, Long commentId);
} 