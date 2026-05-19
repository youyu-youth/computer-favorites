package com.yyyouth.service.aichat.agent.tools;

import com.yyyouth.model.enums.ai.RiskLevel;
import com.yyyouth.model.enums.ai.ToolType;
import com.yyyouth.model.pojo.agent.AgentToolDef;
import com.yyyouth.service.aichat.plan.AgentPlanService;
import com.yyyouth.service.aichat.tool.ConfirmableToolCallback;
import com.yyyouth.service.mapper.AgentToolDefMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final AgentToolDefMapper agentToolDefMapper;
    private final AgentPlanService agentPlanService;
    private final Map<String, ToolCallback> allToolCallbacks;
    private final Map<String, RuntimeToolDef> runtimeTools = new ConcurrentHashMap<>();
    private final Map<String, McpSchema.Tool> mcpTools = new ConcurrentHashMap<>();

    @Data
    public static class RuntimeToolDef {
        private AgentToolDef meta;
        private ToolCallback toolCallback;
        private RiskLevel riskLevel;
        private ToolType toolType;
        private List<String> requiredPermissionCodes;
    }

    @PostConstruct
    public void init() {
        List<AgentToolDef> dbTools = agentToolDefMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentToolDef>()
                        .eq(AgentToolDef::getEnabled, 1));
        for (AgentToolDef dbTool : dbTools) {
            ToolCallback callback = allToolCallbacks.get(dbTool.getToolCode());
            if (callback == null) {
                log.warn("工具 {} 在 DB 中定义但未找到对应的 @Tool 方法", dbTool.getToolCode());
                continue;
            }
            RiskLevel riskLevel = RiskLevel.valueOf(dbTool.getRiskLevel().toUpperCase());
            ToolCallback effectiveCallback = riskLevel == RiskLevel.HIGH
                    ? new ConfirmableToolCallback(callback, dbTool.getToolCode(), riskLevel, agentPlanService)
                    : callback;

            RuntimeToolDef rt = new RuntimeToolDef();
            rt.setMeta(dbTool);
            rt.setToolCallback(effectiveCallback);
            rt.setRiskLevel(riskLevel);
            rt.setToolType(ToolType.valueOf(dbTool.getToolType().toUpperCase()));
            rt.setRequiredPermissionCodes(
                    dbTool.getPermissionCode() != null
                            ? Collections.singletonList(dbTool.getPermissionCode())
                            : Collections.emptyList());
            runtimeTools.put(dbTool.getToolCode(), rt);
        }
        log.info("ToolRegistry 初始化完成，已注册 {} 个工具", runtimeTools.size());
    }

    public void updateTools(List<McpSchema.Tool> updatedTools) {
        mcpTools.clear();
        for (McpSchema.Tool tool : updatedTools) {
            mcpTools.put(tool.name(), tool);
        }
        log.info("MCP 工具更新: {}", mcpTools.keySet());
    }

    public McpSchema.Tool getMcpTool(String name) {
        return mcpTools.get(name);
    }

    public Collection<McpSchema.Tool> getAllMcpTools() {
        return mcpTools.values();
    }

    public RuntimeToolDef get(String toolCode) {
        return runtimeTools.get(toolCode);
    }

    public Collection<RuntimeToolDef> getAll() {
        return runtimeTools.values();
    }

    public ToolCallback[] getFilteredCallbacks(List<String> allowlist) {
        if (allowlist == null || allowlist.isEmpty()) {
            return new ToolCallback[0];
        }
        return allowlist.stream()
                .map(runtimeTools::get)
                .filter(Objects::nonNull)
                .map(RuntimeToolDef::getToolCallback)
                .toArray(ToolCallback[]::new);
    }

    public RiskLevel getRiskLevel(String toolCode) {
        RuntimeToolDef rt = runtimeTools.get(toolCode);
        return rt != null ? rt.getRiskLevel() : RiskLevel.LOW;
    }

    public void refresh() {
        runtimeTools.clear();
        init();
    }
}
