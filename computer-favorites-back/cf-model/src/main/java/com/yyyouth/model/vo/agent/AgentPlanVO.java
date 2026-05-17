package com.yyyouth.model.vo.agent;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 执行计划展示
 */
@Data
@Builder
public class AgentPlanVO {
    private Long planId;
    private Long taskId;
    private String summary;
    private String riskLevel;
    private String status;
    private LocalDateTime expireTime;
}
