package com.yyyouth.model.dto.agent;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 执行计划确认请求
 */
@Data
public class AgentPlanConfirmRequest {
    @NotNull(message = "计划ID不能为空")
    private Long planId;
}
