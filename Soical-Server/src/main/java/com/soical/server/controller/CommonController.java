package com.soical.server.controller;

import com.soical.server.common.Result;
import com.soical.server.service.CityService;
import com.soical.server.service.OccupationService;
import com.soical.server.service.CommonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用数据控制器
 */
@Api(tags = "通用数据接口")
@Slf4j
@RestController
@RequestMapping("/api/common")
public class CommonController {

    private final CityService cityService;
    private final OccupationService occupationService;
    private final CommonService commonService;
    
    @Autowired
    public CommonController(CityService cityService, OccupationService occupationService, CommonService commonService) {
        this.cityService = cityService;
        this.occupationService = occupationService;
        this.commonService = commonService;
    }
    
    @ApiOperation("获取所有省份列表")
    @GetMapping("/provinces")
    public Result<List<Map<String, Object>>> getProvinces() {
        List<Map<String, Object>> provinces = cityService.getProvinces();
        return Result.success(provinces);
    }
    
    @ApiOperation("根据省份编码获取城市列表")
    @GetMapping("/cities/{provinceCode}")
    public Result<List<Map<String, Object>>> getCitiesByProvince(
            @ApiParam("省份编码") @PathVariable String provinceCode) {
        List<Map<String, Object>> cities = cityService.getCitiesByProvince(provinceCode);
        return Result.success(cities);
    }
    
    @ApiOperation("获取所有省份及其城市列表")
    @GetMapping("/provinces-with-cities")
    public Result<List<Map<String, Object>>> getProvincesWithCities() {
        List<Map<String, Object>> provincesWithCities = cityService.getProvincesWithCities();
        return Result.success(provincesWithCities);
    }
    
    @ApiOperation("获取所有职业分类")
    @GetMapping("/occupation-categories")
    public Result<List<String>> getOccupationCategories() {
        List<String> categories = occupationService.getCategories();
        return Result.success(categories);
    }
    
    @ApiOperation("根据分类获取职业列表")
    @GetMapping("/occupations/{categoryCode}")
    public Result<List<Map<String, Object>>> getOccupationsByCategory(
            @ApiParam("分类编码") @PathVariable String categoryCode) {
        List<Map<String, Object>> occupations = occupationService.getOccupationsByCategory(categoryCode);
        return Result.success(occupations);
    }
    
    @ApiOperation("获取所有职业列表（按分类分组）")
    @GetMapping("/occupations-by-category")
    public Result<List<Map<String, Object>>> getAllOccupationsGroupByCategory() {
        List<Map<String, Object>> occupationsByCategory = occupationService.getAllOccupationsGroupByCategory();
        return Result.success(occupationsByCategory);
    }
    
    @ApiOperation("获取所有职业列表")
    @GetMapping("/occupations")
    public Result<List<Map<String, Object>>> getAllOccupations() {
        List<Map<String, Object>> occupations = commonService.getAllOccupations();
        return Result.success(occupations);
    }
    
    @ApiOperation("获取所有学历列表")
    @GetMapping("/educations")
    public Result<List<Map<String, Object>>> getAllEducations() {
        List<Map<String, Object>> educations = commonService.getAllEducations();
        return Result.success(educations);
    }
    
    @ApiOperation("获取性别选项")
    @GetMapping("/gender-options")
    public Result<List<Map<String, Object>>> getGenderOptions() {
        List<Map<String, Object>> genderOptions = commonService.getGenderOptions();
        return Result.success(genderOptions);
    }
    
    @ApiOperation("获取应用配置")
    @GetMapping("/app-config")
    public Result<Map<String, Object>> getAppConfig() {
        Map<String, Object> appConfig = commonService.getAppConfig();
        return Result.success(appConfig);
    }
    
    @ApiOperation("通过坐标获取位置信息")
    @GetMapping("/location/coords")
    public Result<Map<String, Object>> getLocationByCoords(
            @ApiParam("纬度") @RequestParam Double latitude,
            @ApiParam("经度") @RequestParam Double longitude) {
        log.info("通过坐标获取位置信息: latitude={}, longitude={}", latitude, longitude);
        
        try {
            Map<String, Object> locationInfo = commonService.getLocationByCoords(latitude, longitude);
            return Result.success(locationInfo);
        } catch (Exception e) {
            log.error("获取位置信息失败", e);
            return Result.fail("获取位置信息失败");
        }
    }
    
    @ApiOperation("通过IP获取位置信息")
    @GetMapping("/location/ip")
    public Result<Map<String, Object>> getLocationByIp() {
        log.info("通过IP获取位置信息");
        
        try {
            Map<String, Object> locationInfo = commonService.getLocationByIp();
            return Result.success(locationInfo);
        } catch (Exception e) {
            log.error("获取位置信息失败", e);
            return Result.fail("获取位置信息失败");
        }
    }
} 