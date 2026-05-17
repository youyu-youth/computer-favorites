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
 * Agent工具调用流水表实体
 */
@Data
@TableName("t_agent_tool_call")
public class AgentToolCall {

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
     * 执行计划ID
     */
    private Long planId;

    /**
     * 工具编码
     */
    private String toolCode;

    /**
     * 工具类型：local/mcp
     */
    private String toolType;

    /**
     * 调用请求
     */
    private String requestJson;

    /**
     * 调用响应
     */
    private String responseJson;

    /**
     * 状态：pending/running/succeeded/failed
     */
    private String status;

    /**
     * 耗时毫秒
     */
    private Integer durationMs;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
