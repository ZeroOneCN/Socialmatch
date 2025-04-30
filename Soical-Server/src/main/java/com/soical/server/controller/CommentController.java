package com.soical.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.common.Result;
import com.soical.server.dto.CommentDTO;
import com.soical.server.service.CommentService;
import com.soical.server.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Api(tags = "评论接口")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    @ApiOperation("发表评论")
    public Result<Long> addComment(
            @ApiParam(value = "动态ID", required = true) @RequestParam Long postId,
            @ApiParam(value = "评论内容", required = true) @RequestParam String content) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long commentId = commentService.addComment(userId, postId, content, null, null);
        return Result.success(commentId);
    }

    @PostMapping("/reply")
    @ApiOperation("回复评论")
    public Result<Long> replyComment(
            @ApiParam(value = "动态ID", required = true) @RequestParam Long postId,
            @ApiParam(value = "评论内容", required = true) @RequestParam String content,
            @ApiParam(value = "父评论ID", required = true) @RequestParam Long parentId,
            @ApiParam(value = "被回复用户ID", required = true) @RequestParam Long replyUserId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long commentId = commentService.addComment(userId, postId, content, parentId, replyUserId);
        return Result.success(commentId);
    }

    @GetMapping("/list/{postId}")
    @ApiOperation("获取动态评论列表")
    public Result<Page<CommentDTO>> getCommentList(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", defaultValue = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<CommentDTO> commentList = commentService.getCommentList(postId, page, pageSize);
        return Result.success(commentList);
    }

    @DeleteMapping("/{commentId}")
    @ApiOperation("删除评论")
    public Result<Boolean> deleteComment(
            @ApiParam(value = "评论ID", required = true) @PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean result = commentService.deleteComment(userId, commentId);
        return Result.success(result);
    }
} 