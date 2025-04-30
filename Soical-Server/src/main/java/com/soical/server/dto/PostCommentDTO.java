package com.soical.server.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostCommentDTO {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String content;
    private Integer status;
    private Long parentId;
    private Long replyUserId;
    private String replyUsername;
    private String replyNickname;
    private String reviewReason;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 