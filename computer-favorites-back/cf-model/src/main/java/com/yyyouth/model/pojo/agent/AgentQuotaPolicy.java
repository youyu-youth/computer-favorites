package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent配额策略表实体
 */
@Data
@TableName("t_agent_quota_policy")
public class AgentQuotaPolicy {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String subjectType;
    private String subjectCode;
    private Integer dailyMessageLimit;
    private Integer dailyTokenLimit;
    private BigDecimal dailyCostLimit;
    private Integer enabled;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
