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
 * Agent执行计划与确认表实体
 */
@Data
@TableName("t_agent_action_plan")
public class AgentActionPlan {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent任务ID
     */
    private Long taskId;

    /**
     * 计划类型
     */
    private String planType;

    /**
     * 计划摘要
     */
    private String summary;

    /**
     * 风险等级：low/medium/high
     */
    private String riskLevel;

    /**
     * 结构化执行参数
     */
    private String actionPayloadJson;

    /**
     * 状态：wait_confirm/approved/rejected/executed/expired
     */
    private String status;

    /**
     * 确认者类型
     */
    private String confirmUserType;

    /**
     * 确认者ID
     */
    private Long confirmUserId;

    /**
     * 确认时间
     */
    private LocalDateTime confirmedAt;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 拒绝原因
     */
    private String rejectReason;

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
