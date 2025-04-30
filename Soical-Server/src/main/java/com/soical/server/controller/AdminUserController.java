package com.soical.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.common.Result;
import com.soical.server.dto.UserDetailDTO;
import com.soical.server.service.AdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员用户管理控制器
 */
@Api(tags = "管理员用户管理接口")
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 获取用户列表
     */
    @ApiOperation("获取用户列表")
    @GetMapping("/list")
    public Result<Page<UserDetailDTO>> getUserList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("关键词搜索(用户名、昵称、手机号)") @RequestParam(required = false) String keyword,
            @ApiParam("状态(0-禁用，1-正常，null-全部)") @RequestParam(required = false) Integer status,
            @ApiParam("排序字段") @RequestParam(required = false) String sortBy,
            @ApiParam("排序方式(asc-升序, desc-降序)") @RequestParam(required = false) String sortOrder) {
        
        Page<UserDetailDTO> userPage = adminUserService.getUserList(page, pageSize, keyword, status, sortBy, sortOrder);
        return Result.success(userPage);
    }

    /**
     * 获取用户详情
     */
    @ApiOperation("获取用户详情")
    @GetMapping("/{userId}")
    public Result<UserDetailDTO> getUserDetail(@PathVariable Long userId) {
        UserDetailDTO userDetail = adminUserService.getUserDetail(userId);
        return Result.success(userDetail);
    }

    /**
     * 更新用户状态
     */
    @ApiOperation("更新用户状态")
    @PutMapping("/{userId}/status")
    public Result<Boolean> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> param) {
        Integer status = param.get("status");
        boolean result = adminUserService.updateUserStatus(userId, status);
        return Result.success(result);
    }

    /**
     * 重置用户密码
     */
    @ApiOperation("重置用户密码")
    @PutMapping("/{userId}/password/reset")
    public Result<Map<String, String>> resetUserPassword(@PathVariable Long userId) {
        String newPassword = adminUserService.resetUserPassword(userId);
        
        Map<String, String> result = new HashMap<>();
        result.put("password", newPassword);
        
        return Result.success(result);
    }

    /**
     * 获取用户统计数据
     */
    @ApiOperation("获取用户统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = adminUserService.getUserStats();
        return Result.success(stats);
    }
} 