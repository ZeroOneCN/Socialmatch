package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.util.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Api(tags = "文件上传接口")
@RestController
@RequestMapping("/api/file")
public class FileController {

    /**
     * 上传图片
     */
    @ApiOperation("上传图片")
    @PostMapping("/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = FileUtil.uploadImage(file);
        return Result.success(imageUrl);
    }

    /**
     * 上传文件
     */
    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "type", defaultValue = "common") String type) {
        String fileUrl = FileUtil.uploadFile(file, type);
        return Result.success(fileUrl);
    }
} 