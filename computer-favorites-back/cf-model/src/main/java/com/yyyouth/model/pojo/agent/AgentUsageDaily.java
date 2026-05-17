package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent每日用量表实体
 */
@Data
@TableName("t_agent_usage_daily")
public class AgentUsageDaily {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ownerType;
    private Long ownerId;
    private LocalDate usageDate;
    private Integer messageCount;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal estimatedCost;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
