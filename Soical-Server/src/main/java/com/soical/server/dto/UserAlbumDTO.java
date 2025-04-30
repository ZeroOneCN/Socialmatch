package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户相册DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("用户相册信息")
public class UserAlbumDTO {

    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("用户昵称")
    private String nickname;
    
    @ApiModelProperty("照片列表")
    private List<String> photos;
    
    @ApiModelProperty("照片总数")
    private Integer totalPhotos;
} 