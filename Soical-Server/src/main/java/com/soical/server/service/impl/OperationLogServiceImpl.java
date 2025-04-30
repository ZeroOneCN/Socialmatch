package com.soical.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soical.server.entity.OperationLog;
import com.soical.server.mapper.OperationLogMapper;
import com.soical.server.service.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 操作日志服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
    
    @Override
    public void addLog(Long adminId, String operationType, String operationContent, String ip) {
        OperationLog log = new OperationLog();
        log.setAdminId(adminId);
        log.setOperationType(operationType);
        log.setOperationContent(operationContent);
        log.setIp(ip);
        
        save(log);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OperationLog> getLogList(Long adminId, String operationType, 
                                        String startTime, String endTime, 
                                        Integer page, Integer pageSize) {
        Page<OperationLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (adminId != null) {
            wrapper.eq(OperationLog::getAdminId, adminId);
        }
        
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        
        // 处理时间范围
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (startTime != null && !startTime.isEmpty()) {
                Date startDate = dateFormat.parse(startTime);
                wrapper.ge(OperationLog::getCreateTime, startDate);
            }
            
            if (endTime != null && !endTime.isEmpty()) {
                Date endDate = dateFormat.parse(endTime);
                wrapper.le(OperationLog::getCreateTime, endDate);
            }
        } catch (ParseException e) {
            // 时间格式不正确，忽略时间筛选
        }
        
        // 按时间降序排序
        wrapper.orderByDesc(OperationLog::getCreateTime);
        
        return page(pageParam, wrapper);
    }
} 