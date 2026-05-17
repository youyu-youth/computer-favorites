package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent长期记忆摘要表实体
 */
@Data
@TableName("t_agent_memory_summary")
public class AgentMemorySummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String summaryType;
    private String summaryText;
    private Long sourceMessageFrom;
    private Long sourceMessageTo;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
