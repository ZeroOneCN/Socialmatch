package com.soical.server.controller;

import com.soical.server.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin/data-statistics")
@Api(tags = "管理后台统计数据", description = "管理后台统计数据相关API")
public class AdminStatisticsController {

    @GetMapping("/users")
    @ApiOperation("获取用户统计数据")
    public Result<Map<String, Object>> getUserStats() {
        // 模拟数据，实际开发中应该从service层获取
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", 5324);
        data.put("newUsersToday", 45);
        data.put("newUsersYesterday", 38);
        data.put("newUsersWeek", 286);
        data.put("newUsersMonth", 864);
        data.put("activeUsers", 1283);
        data.put("userGrowthRate", 18.4);
        
        return Result.success(data);
    }
    
    @GetMapping("/content")
    @ApiOperation("获取内容统计数据")
    public Result<Map<String, Object>> getContentStats() {
        // 模拟数据，实际开发中应该从service层获取
        Map<String, Object> data = new HashMap<>();
        data.put("totalPosts", 13468);
        data.put("totalComments", 28764);
        data.put("postsToday", 127);
        data.put("commentsToday", 356);
        data.put("avgPostsPerDay", 102);
        data.put("avgCommentsPerPost", 2.14);
        
        return Result.success(data);
    }
    
    @GetMapping("/system")
    @ApiOperation("获取系统概要信息")
    public Result<Map<String, Object>> getSystemSummary() {
        // 模拟数据，实际开发中应该从service层获取
        Map<String, Object> data = new HashMap<>();
        data.put("version", "1.0.0");
        data.put("startTime", "2023-08-01 00:00:00");
        data.put("runningDays", 135);
        data.put("cpuUsage", 23.5);
        data.put("memoryUsage", 42.8);
        data.put("diskUsage", 36.2);
        
        return Result.success(data);
    }
    
    @GetMapping("/match-stats")
    @ApiOperation("获取用户匹配统计数据")
    public Result<Map<String, Object>> getMatchStats() {
        // 模拟数据，实际开发中应该从service层获取
        Map<String, Object> data = new HashMap<>();
        data.put("totalMatches", 2356);
        data.put("totalMatchAttempts", 18742);
        data.put("matchSuccessRate", 12.57); // 匹配成功率百分比
        data.put("lastWeekMatchRate", 11.23); // 上周匹配成功率
        data.put("matchesToday", 86);
        data.put("matchesYesterday", 72);
        data.put("avgMatchesPerUser", 0.44);
        data.put("mostActiveTime", "20:00-22:00"); // 匹配最活跃时段
        
        return Result.success(data);
    }
    
    @GetMapping("/user-gender")
    @ApiOperation("获取用户性别分布数据")
    public Result<Map<String, Object>> getUserGenderDistribution() {
        // 模拟数据，实际开发中应该从service层获取
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> genderData = new ArrayList<>();
        
        Map<String, Object> male = new HashMap<>();
        male.put("name", "男");
        male.put("value", 2876);
        genderData.add(male);
        
        Map<String, Object> female = new HashMap<>();
        female.put("name", "女");
        female.put("value", 2124);
        genderData.add(female);
        
        Map<String, Object> other = new HashMap<>();
        other.put("name", "未知");
        other.put("value", 324);
        genderData.add(other);
        
        data.put("data", genderData);
        return Result.success(data);
    }
    
    @GetMapping("/user-growth")
    @ApiOperation("获取用户增长趋势数据")
    public Result<Map<String, Object>> getUserGrowthTrend(@RequestParam(defaultValue = "week") String type) {
        // 模拟数据，实际开发中应该从service层获取
        Map<String, Object> data = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        Random random = new Random();
        
        int days = "week".equals(type) ? 7 : 30;
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));
            values.add(20 + random.nextInt(60));
        }
        
        data.put("dates", dates);
        data.put("values", values);
        return Result.success(data);
    }
    
    @GetMapping("/match-trend")
    @ApiOperation("获取匹配趋势数据")
    public Result<Map<String, Object>> getMatchTrend(@RequestParam(defaultValue = "week") String type) {
        // 模拟数据，实际开发中应该从service层获取
        Map<String, Object> data = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> totalValues = new ArrayList<>();
        List<Integer> successValues = new ArrayList<>();
        List<Double> rateValues = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        Random random = new Random();
        
        int days = "week".equals(type) ? 7 : 30;
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));
            
            int total = 100 + random.nextInt(150);
            int success = 10 + random.nextInt(30);
            double rate = Math.round((double) success / total * 1000) / 10.0;
            
            totalValues.add(total);
            successValues.add(success);
            rateValues.add(rate);
        }
        
        data.put("dates", dates);
        data.put("totalValues", totalValues);
        data.put("successValues", successValues);
        data.put("rateValues", rateValues);
        return Result.success(data);
    }
    
    @GetMapping("/content-trend")
    @ApiOperation("获取内容发布趋势数据")
    public Result<Map<String, Object>> getContentPublishTrend(@RequestParam(defaultValue = "week") String type) {
        // 模拟数据，实际开发中应该从service层获取
        Map<String, Object> data = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> postValues = new ArrayList<>();
        List<Integer> commentValues = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        Random random = new Random();
        
        int days = "week".equals(type) ? 7 : 30;
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));
            postValues.add(50 + random.nextInt(100));
            commentValues.add(100 + random.nextInt(300));
        }
        
        data.put("dates", dates);
        data.put("postValues", postValues);
        data.put("commentValues", commentValues);
        return Result.success(data);
    }
}