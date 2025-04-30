package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * 举报数据访问接口
 */
@Repository
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 获取待处理举报数量
     */
    @Select("SELECT COUNT(*) FROM t_report WHERE status = 0")
    Long getPendingReportCount();
    
    /**
     * 获取待处理举报数量（别名，保持与其他接口命名一致）
     */
    @Select("SELECT COUNT(*) FROM t_report WHERE status = 0")
    Integer getPendingReportsCount();

    /**
     * 获取今日新增举报数量
     */
    @Select("SELECT COUNT(*) FROM t_report WHERE DATE(create_time) = CURDATE()")
    Integer getTodayNewReportsCount();
    
    /**
     * 获取昨日新增举报数量
     */
    @Select("SELECT COUNT(*) FROM t_report WHERE DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Integer getYesterdayNewReportsCount();

    /**
     * 获取违规举报数量
     */
    @Select("SELECT COUNT(*) FROM t_report WHERE status = 1")
    Long getViolationReportCount();
    
    /**
     * 获取特定类型的举报数量
     * 
     * @param reportType 举报类型：1-动态，2-评论，3-用户
     * @return 举报数量
     */
    @Select("SELECT COUNT(*) FROM t_report WHERE report_type = #{reportType}")
    Integer getReportCountByType(@Param("reportType") Integer reportType);
    
    /**
     * 获取举报列表（带关联信息）
     */
    Page<Report> selectReportsWithDetail(Page<Report> page, @Param("reportType") Integer reportType, @Param("status") Integer status);
    
    /**
     * 更新举报状态
     */
    @Update("UPDATE t_report SET status = #{status}, result = #{result}, handle_time = NOW(), handler_id = #{handlerId} WHERE report_id = #{reportId}")
    int updateReportStatus(@Param("reportId") Long reportId, @Param("status") Integer status, @Param("result") String result, @Param("handlerId") Long handlerId);
} 