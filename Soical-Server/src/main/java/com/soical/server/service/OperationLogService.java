package com.soical.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.soical.server.entity.OperationLog;

/**
 * 操作日志服务接口
 */
public interface OperationLogService extends IService<OperationLog> {
    
    /**
     * 记录操作日志
     *
     * @param adminId          管理员ID
     * @param operationType    操作类型
     * @param operationContent 操作内容
     * @param ip               IP地址
     */
    void addLog(Long adminId, String operationType, String operationContent, String ip);
    
    /**
     * 获取操作日志列表
     *
     * @param adminId        管理员ID（null表示所有）
     * @param operationType  操作类型（null表示所有）
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @param page           页码
     * @param pageSize       每页条数
     * @return 日志列表
     */
    Page<OperationLog> getLogList(Long adminId, String operationType, 
                                 String startTime, String endTime, 
                                 Integer page, Integer pageSize);
} 