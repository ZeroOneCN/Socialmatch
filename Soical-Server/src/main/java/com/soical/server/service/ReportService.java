package com.soical.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.entity.Report;

/**
 * 举报服务接口
 */
public interface ReportService {
    
    /**
     * 提交举报
     * @param report 举报信息
     */
    void submitReport(Report report);
    
    /**
     * 获取用户举报历史
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页条数
     * @return 举报列表
     */
    Page<Report> getUserReportHistory(Long userId, Integer page, Integer pageSize);
    
    /**
     * 取消举报
     * @param reportId 举报ID
     * @param userId 用户ID
     */
    void cancelReport(Long reportId, Long userId);
    
    /**
     * 处理举报
     * @param reportId 举报ID
     * @param status 处理状态
     * @param result 处理结果
     * @param handlerId 处理人ID
     */
    void handleReport(Long reportId, Integer status, String result, Long handlerId);
} 