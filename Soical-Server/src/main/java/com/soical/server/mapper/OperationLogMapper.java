package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志数据访问接口
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
} 