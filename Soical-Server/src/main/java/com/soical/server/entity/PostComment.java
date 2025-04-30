package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_comment")
public class PostComment {
    @TableId(type = IdType.AUTO)
    private Long commentId;
    
    private Long postId;
    private Long userId;
    private String content;
    private Integer status;
    private Long parentId;
    private Long replyUserId;
    private String reviewReason;
    private LocalDateTime reviewTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 