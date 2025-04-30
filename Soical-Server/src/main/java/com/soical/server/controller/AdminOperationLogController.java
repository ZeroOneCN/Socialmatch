package com.soical.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.common.Result;
import com.soical.server.entity.OperationLog;
import com.soical.server.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员操作日志控制器
 */
@RestController
@RequestMapping("/api/admin/log")
@RequiredArgsConstructor
@Api(tags = "管理员操作日志接口")
public class AdminOperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/list")
    @ApiOperation("获取操作日志列表")
    public Result<Page<OperationLog>> getLogList(
            @ApiParam(value = "管理员ID") @RequestParam(required = false) Long adminId,
            @ApiParam(value = "操作类型") @RequestParam(required = false) String operationType,
            @ApiParam(value = "开始时间，格式：yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) String startTime,
            @ApiParam(value = "结束时间，格式：yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) String endTime,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<OperationLog> logList = operationLogService.getLogList(adminId, operationType, startTime, endTime, page, pageSize);
        return Result.success(logList);
    }
} 