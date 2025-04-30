package com.soical.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报实体类
 */
@Data
@TableName("t_report")
public class Report {

    /**
     * 举报ID
     */
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    /**
     * 举报类型：1-动态，2-评论，3-用户
     */
    @TableField("report_type")
    private Integer reportType;

    /**
     * 举报人ID
     */
    @TableField("reporter_id")
    private Long reporterId;

    /**
     * 被举报对象ID（动态ID、评论ID或用户ID）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 举报原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 举报内容描述
     */
    @TableField("content")
    private String content;

    /**
     * 证据图片（JSON格式）
     */
    @TableField("images")
    private String images;

    /**
     * 状态：0-待处理，1-已处理（违规），2-已处理（非违规），3-忽略
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理结果说明
     */
    @TableField("result")
    private String result;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理人ID（管理员ID）
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 非数据库字段
     */
    @TableField(exist = false)
    private String reporterName;

    @TableField(exist = false)
    private String targetContent;

    @TableField(exist = false)
    private String handlerName;
} 