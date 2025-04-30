package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 操作日志实体类
 */
@Data
@TableName("t_operation_log")
public class OperationLog {
    
    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
    
    /**
     * 管理员ID
     */
    private Long adminId;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 操作内容
     */
    private String operationContent;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 