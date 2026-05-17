package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent业务任务表实体
 */
@Data
@TableName("t_agent_task")
public class AgentTask {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent会话ID
     */
    private Long sessionId;

    /**
     * 主体类型：user/admin
     */
    private String ownerType;

    /**
     * 主体ID
     */
    private Long ownerId;

    /**
     * 任务类型：website_submit/website_audit
     */
    private String taskType;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 状态：planning/wait_confirm/executing/succeeded/failed/cancelled
     */
    private String status;

    /**
     * 意图识别结果
     */
    private String intentJson;

    /**
     * 任务结果
     */
    private String resultJson;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
