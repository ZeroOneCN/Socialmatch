package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.dto.UserAlbumDTO;
import com.soical.server.entity.UserProfile;
import com.soical.server.service.UserProfileService;
import com.soical.server.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户相册控制器
 */
@RestController
@RequestMapping("/api/user/album")
@RequiredArgsConstructor
@Api(tags = "用户相册接口")
public class UserAlbumController {

    private final UserProfileService userProfileService;

    @PostMapping("/upload")
    @ApiOperation("上传照片到个人相册")
    public Result<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserId();
        String photoUrl = userProfileService.addPhoto(userId, file);
        return Result.success(photoUrl);
    }

    @GetMapping("/photos")
    @ApiOperation("获取个人相册照片列表")
    public Result<UserAlbumDTO> getPhotos() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<String> photos = userProfileService.getPhotos(userId);
        UserProfile profile = userProfileService.getProfileByUserId(userId);
        
        UserAlbumDTO albumDTO = UserAlbumDTO.builder()
                .userId(userId)
                .nickname(profile.getNickname())
                .photos(photos)
                .totalPhotos(photos.size())
                .build();
        
        return Result.success(albumDTO);
    }

    @DeleteMapping("/photo")
    @ApiOperation("删除个人相册照片")
    public Result<Boolean> deletePhoto(@RequestParam("photoUrl") String photoUrl) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean result = userProfileService.deletePhoto(userId, photoUrl);
        return Result.success(result);
    }
    
    @GetMapping("/user/{userId}")
    @ApiOperation("获取指定用户的相册")
    public Result<UserAlbumDTO> getUserAlbum(@PathVariable("userId") Long userId) {
        List<String> photos = userProfileService.getPhotos(userId);
        UserProfile profile = userProfileService.getProfileByUserId(userId);
        
        UserAlbumDTO albumDTO = UserAlbumDTO.builder()
                .userId(userId)
                .nickname(profile.getNickname())
                .photos(photos)
                .totalPhotos(photos.size())
                .build();
        
        return Result.success(albumDTO);
    }
} 