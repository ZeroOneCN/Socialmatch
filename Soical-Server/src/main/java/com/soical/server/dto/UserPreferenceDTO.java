package com.soical.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 用户同频偏好设置DTO
 */
@Data
@ApiModel("用户同频偏好设置")
public class UserPreferenceDTO {

    @ApiModelProperty("兴趣爱好")
    private String interests;

    @ApiModelProperty("意向城市")
    private String city;

    @ApiModelProperty("城市编码")
    private String cityCode;

    @ApiModelProperty("是否仅查看附近")
    private Boolean nearbyOnly;

    @ApiModelProperty("最大匹配距离(公里)")
    private Integer maxDistance;

    @ApiModelProperty("年龄范围[最小年龄,最大年龄]")
    private List<Integer> ageRange;

    @ApiModelProperty("教育程度要求")
    private Object education;

    @ApiModelProperty("职业意向")
    private String occupation;

    @ApiModelProperty("职业编码")
    private String occupationCode;

    @ApiModelProperty("是否仅显示已认证用户")
    private Boolean verifiedOnly;
} 