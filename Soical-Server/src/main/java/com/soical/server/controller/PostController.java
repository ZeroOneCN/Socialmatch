package com.soical.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.common.Result;
import com.soical.server.dto.PostDTO;
import com.soical.server.service.PostService;
import com.soical.server.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * 动态控制器
 */
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Api(tags = "动态接口")
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    @ApiOperation("发布动态")
    public Result<Long> createPost(
            @ApiParam(value = "内容", required = true) @RequestParam String content,
            @ApiParam(value = "图片列表") @RequestParam(required = false) List<MultipartFile> images) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long postId = postService.createPost(userId, content, images);
        return Result.success(postId);
    }

    @PostMapping("/share/{postId}")
    @ApiOperation("分享动态")
    public Result<Long> sharePost(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId,
            @ApiParam(value = "分享内容") @RequestParam(required = false, defaultValue = "") String content) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long sharedPostId = postService.sharePost(userId, postId, content);
        return Result.success(sharedPostId);
    }

    @GetMapping("/list")
    @ApiOperation("获取动态列表")
    public Result<Page<PostDTO>> getPostList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PostDTO> postList = postService.getPostList(page, pageSize);
        return Result.success(postList);
    }

    @GetMapping("/user/{userId}")
    @ApiOperation("获取用户动态列表")
    public Result<Page<PostDTO>> getUserPostList(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PostDTO> postList = postService.getUserPostList(userId, page, pageSize);
        return Result.success(postList);
    }

    @GetMapping("/{postId}")
    @ApiOperation("获取动态详情")
    public Result<PostDTO> getPostDetail(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId) {
        PostDTO postDTO = postService.getPostDetail(postId);
        return Result.success(postDTO);
    }

    @DeleteMapping("/{postId}")
    @ApiOperation("删除动态")
    public Result<Boolean> deletePost(
            @ApiParam(value = "动态ID", required = true) @PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean result = postService.deletePost(userId, postId);
        return Result.success(result);
    }

    @GetMapping("/list/community")
    @ApiOperation("获取社区动态列表")
    public Result<Page<PostDTO>> getCommunityPosts(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<PostDTO> postList = postService.getCommunityPosts(userId, page, pageSize);
        return Result.success(postList);
    }

    @GetMapping("/list/hot")
    @ApiOperation("获取热门动态列表")
    public Result<Page<PostDTO>> getHotPosts(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<PostDTO> postList = postService.getHotPosts(userId, page, pageSize);
        return Result.success(postList);
    }

    @GetMapping("/list/followed")
    @ApiOperation("获取关注用户的动态列表")
    public Result<Page<PostDTO>> getFollowedPosts(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<PostDTO> postList = postService.getFollowedPosts(userId, page, pageSize);
        return Result.success(postList);
    }

    @GetMapping("/list/nearby")
    @ApiOperation("获取附近的动态列表")
    public Result<Page<PostDTO>> getNearbyPosts(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "城市") @RequestParam(required = false) String city) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<PostDTO> postList = postService.getNearbyPosts(userId, page, pageSize, city);
        return Result.success(postList);
    }

    @GetMapping("/list/liked")
    @ApiOperation("获取用户点赞的动态列表")
    public Result<Page<PostDTO>> getLikedPosts(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<PostDTO> postList = postService.getLikedPosts(userId, page, pageSize);
        return Result.success(postList);
    }
} 