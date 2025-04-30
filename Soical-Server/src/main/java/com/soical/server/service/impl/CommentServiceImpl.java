package com.soical.server.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import com.soical.server.dto.CommentDTO;
import com.soical.server.entity.Comment;
import com.soical.server.entity.Post;
import com.soical.server.entity.User;
import com.soical.server.entity.UserProfile;
import com.soical.server.mapper.CommentMapper;
import com.soical.server.service.CommentService;
import com.soical.server.service.PostService;
import com.soical.server.service.UserProfileService;
import com.soical.server.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final ApplicationContext applicationContext;
    
    // 使用懒加载方式获取PostService，避免循环依赖
    private PostService getPostService() {
        return applicationContext.getBean(PostService.class);
    }
    
    @Autowired
    public CommentServiceImpl(UserService userService,
                            UserProfileService userProfileService,
                            ApplicationContext applicationContext) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.applicationContext = applicationContext;
    }

    @Override
    public Long addComment(Long userId, Long postId, String content) {
        return addComment(userId, postId, content, null, null);
    }
    
    @Override
    public Long addComment(Long userId, Long postId, String content, Long parentId, Long replyUserId) {
        // 验证用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证动态是否存在
        Post post = getPostService().getById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "动态不存在或已删除");
        }
        
        // 如果是回复评论，验证父评论是否存在
        if (parentId != null) {
            Comment parentComment = getById(parentId);
            if (parentComment == null || parentComment.getStatus() != 1) {
                throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "原评论不存在或已删除");
            }
            
            // 验证被回复用户是否存在
            if (replyUserId != null) {
                User replyUser = userService.getById(replyUserId);
                if (replyUser == null) {
                    throw new BusinessException(ResultCode.USER_NOT_EXIST.getCode(), "被回复用户不存在");
                }
            }
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setContent(content);
        comment.setStatus(Integer.valueOf(1));
        comment.setParentId(parentId);
        comment.setReplyUserId(replyUserId);

        // 保存评论
        boolean saved = save(comment);
        if (!saved) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "评论发表失败");
        }

        // 更新动态评论数
        post.setCommentCount(post.getCommentCount() + 1);
        getPostService().updateById(post);

        return comment.getCommentId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentList(Long postId, Integer page, Integer pageSize) {
        // 验证动态是否存在
        Post post = getPostService().getById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "动态不存在或已删除");
        }

        // 分页查询
        Page<Comment> commentPage = new Page<>(page, pageSize);
        Page<Comment> commentResult = baseMapper.selectCommentsWithUser(commentPage, postId);

        // 转换为DTO
        return convertToCommentDTOPage(commentResult);
    }

    @Override
    public boolean deleteComment(Long userId, Long commentId) {
        // 查询评论
        Comment comment = getById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "评论不存在");
        }

        // 验证权限
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除该评论");
        }

        // 逻辑删除
        comment.setStatus(Integer.valueOf(0));
        boolean updated = updateById(comment);
        
        if (updated) {
            // 更新动态评论数
            Post post = getPostService().getById(comment.getPostId());
            if (post != null) {
                post.setCommentCount(post.getCommentCount() - 1);
                getPostService().updateById(post);
            }
        }
        
        return updated;
    }

    /**
     * 将Comment对象转换为CommentDTO
     */
    private CommentDTO convertToCommentDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        BeanUtils.copyProperties(comment, commentDTO);

        // 获取用户信息
        User user = userService.getById(comment.getUserId());
        if (user != null) {
            commentDTO.setAvatar(user.getAvatar());
            
            // 获取用户资料
            UserProfile userProfile = userProfileService.getProfileByUserId(comment.getUserId());
            if (userProfile != null) {
                commentDTO.setNickname(userProfile.getNickname());
            }
        }

        // 处理回复信息
        if (comment.getReplyUserId() != null) {
            // 从Comment中获取回复用户昵称（通过mapper的关联查询得到）
            if (comment.getReplyNickname() != null) {
                commentDTO.setReplyNickname(comment.getReplyNickname());
            } else {
                // 如果没有从mapper中获取到，则查询用户资料
                UserProfile replyUserProfile = userProfileService.getProfileByUserId(comment.getReplyUserId());
                if (replyUserProfile != null) {
                    commentDTO.setReplyNickname(replyUserProfile.getNickname());
                }
            }
        }

        return commentDTO;
    }

    /**
     * 将Comment分页对象转换为CommentDTO分页对象
     */
    private Page<CommentDTO> convertToCommentDTOPage(Page<Comment> commentPage) {
        Page<CommentDTO> commentDTOPage = new Page<>();
        BeanUtils.copyProperties(commentPage, commentDTOPage, "records");

        List<CommentDTO> commentDTOList = commentPage.getRecords().stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());

        commentDTOPage.setRecords(commentDTOList);
        return commentDTOPage;
    }
} 