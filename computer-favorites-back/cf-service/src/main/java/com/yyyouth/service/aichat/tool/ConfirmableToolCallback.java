package com.yyyouth.service.aichat.tool;

import com.yyyouth.model.enums.ai.RiskLevel;
import com.yyyouth.model.pojo.agent.AgentActionPlan;
import com.yyyouth.service.aichat.chat.AgentSseContext;
import com.yyyouth.service.aichat.chat.AgentStreamSink;
import com.yyyouth.service.aichat.plan.AgentPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * 带风险拦截的 ToolCallback 包装器。
 * 低/中风险工具直接执行，高风险工具生成执行计划等待用户确认。
 */
@Slf4j
public class ConfirmableToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final String toolCode;
    private final RiskLevel riskLevel;
    private final AgentPlanService planService;

    public ConfirmableToolCallback(ToolCallback delegate, String toolCode,
                                    RiskLevel riskLevel, AgentPlanService planService) {
        this.delegate = delegate;
        this.toolCode = toolCode;
        this.riskLevel = riskLevel;
        this.planService = planService;
    }

    @Override
    public String call(String toolInput) {
        if (riskLevel == RiskLevel.HIGH) {
            return executeWithConfirm(toolInput);
        }
        return delegate.call(toolInput);
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    private String executeWithConfirm(String toolInput) {
        String summary = "工具: " + toolCode + ", 参数: " + truncate(toolInput, 200);

        AgentActionPlan plan = planService.createPlan(
                null, "tool_execution", summary, "high", toolInput);
        planService.createFuture(plan.getId());

        AgentStreamSink sink = AgentSseContext.get();
        if (sink != null) {
            sink.plan(plan.getId(), summary, "high");
        }

        log.info("等待用户确认: planId={}, toolCode={}", plan.getId(), toolCode);
        boolean confirmed = planService.waitForConfirm(plan.getId());

        if (confirmed) {
            planService.markExecuted(plan.getId());
            log.info("计划已确认，执行工具: planId={}, toolCode={}", plan.getId(), toolCode);
            return delegate.call(toolInput);
        } else {
            log.info("计划被拒绝或超时: planId={}, toolCode={}", plan.getId(), toolCode);
            return "操作被拒绝或超时，请与用户沟通后重新尝试。";
        }
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
