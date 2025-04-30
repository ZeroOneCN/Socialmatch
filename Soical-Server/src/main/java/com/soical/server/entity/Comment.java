package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * 评论实体类
 */
@Data
@TableName("t_comment")
public class Comment {

    /**
     * 评论ID
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    /**
     * 动态ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 状态：0-删除，1-正常，2-违规
     */
    @TableField("status")
    private Integer status;

    /**
     * 父评论ID，如果是回复评论则指向父评论，否则为null
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 被回复用户ID，如果是回复评论则为被回复用户ID，否则为null
     */
    @TableField("reply_user_id")
    private Long replyUserId;

    /**
     * 审核原因
     */
    @TableField("review_reason")
    private String reviewReason;

    /**
     * 审核时间
     */
    @TableField("review_time")
    private Date reviewTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 被回复用户昵称（非数据库字段，用于表连接查询）
     */
    @TableField(exist = false)
    private String replyNickname;

    /**
     * Getter and Setter methods
     */
    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getReplyUserId() {
        return replyUserId;
    }

    public void setReplyUserId(Long replyUserId) {
        this.replyUserId = replyUserId;
    }

    public String getReviewReason() {
        return reviewReason;
    }

    public void setReviewReason(String reviewReason) {
        this.reviewReason = reviewReason;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getReplyNickname() {
        return replyNickname;
    }

    public void setReplyNickname(String replyNickname) {
        this.replyNickname = replyNickname;
    }
} 