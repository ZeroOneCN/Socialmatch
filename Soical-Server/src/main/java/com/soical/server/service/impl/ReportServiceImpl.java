package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.entity.Report;
import com.soical.server.mapper.ReportMapper;
import com.soical.server.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 举报服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    @Override
    @Transactional
    public void submitReport(Report report) {
        // 设置默认值
        report.setStatus(0); // 0-待处理
        report.setCreateTime(LocalDateTime.now());
        
        reportMapper.insert(report);
        log.info("用户 {} 提交举报，类型: {}, 目标ID: {}", 
                report.getReporterId(), report.getReportType(), report.getTargetId());
    }

    @Override
    public Page<Report> getUserReportHistory(Long userId, Integer page, Integer pageSize) {
        Page<Report> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        
        // 查询条件：举报人ID = 当前用户ID
        queryWrapper.eq(Report::getReporterId, userId);
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Report::getCreateTime);
        
        return reportMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    @Transactional
    public void cancelReport(Long reportId, Long userId) {
        // 查询举报记录
        Report report = reportMapper.selectById(reportId);
        
        // 判断是否存在和是否属于当前用户
        if (report != null && report.getReporterId().equals(userId)) {
            // 判断是否可以取消（只有待处理的举报才能取消）
            if (report.getStatus() == 0) {
                reportMapper.deleteById(reportId);
                log.info("用户 {} 取消举报，举报ID: {}", userId, reportId);
            } else {
                log.warn("用户 {} 尝试取消已处理的举报，举报ID: {}", userId, reportId);
                throw new RuntimeException("已处理的举报不能取消");
            }
        } else {
            log.warn("用户 {} 尝试取消不存在或不属于自己的举报，举报ID: {}", userId, reportId);
            throw new RuntimeException("举报不存在或不属于您");
        }
    }

    @Override
    @Transactional
    public void handleReport(Long reportId, Integer status, String result, Long handlerId) {
        // 查询举报记录
        Report report = reportMapper.selectById(reportId);
        
        if (report != null) {
            // 设置处理结果
            report.setStatus(status);
            report.setResult(result);
            report.setHandlerId(handlerId);
            report.setHandleTime(LocalDateTime.now());
            
            reportMapper.updateById(report);
            log.info("管理员 {} 处理举报，举报ID: {}, 状态: {}", handlerId, reportId, status);
        } else {
            log.warn("管理员 {} 尝试处理不存在的举报，举报ID: {}", handlerId, reportId);
            throw new RuntimeException("举报不存在");
        }
    }
} 