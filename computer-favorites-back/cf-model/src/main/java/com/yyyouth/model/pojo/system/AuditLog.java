package com.yyyouth.model.pojo.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 审计日志实体
 */
@Data
@TableName("t_audit_log")
public class AuditLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户类型
     */
    @TableField("user_type")
    private String userType;

    /**
     * 模块
     */
    private String module;

    /**
     * 动作
     */
    private String action;

    /**
     * 目标类型
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标ID
     */
    @TableField("target_id")
    private Long targetId;


    /**
     * 执行结果
     */
    private Integer result;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 请求IP
     */
    private String ip;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求方法
     */
    @TableField("request_method")
    private String requestMethod;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
