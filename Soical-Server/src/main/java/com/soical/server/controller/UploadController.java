package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.util.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传控制器 - 兼容前端路径
 */
@Api(tags = "上传接口")
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    /**
     * 上传图片
     */
    @ApiOperation("上传图片")
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = FileUtil.uploadImage(file);
        return Result.success(imageUrl);
    }
} 