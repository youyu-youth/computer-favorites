package com.yyyouth.service.aichat.agent.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.ToolContextToMcpMetaConverter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-05-11
 * 自定义工具上下文转换为MCP元数据的转换器
 *  对该项目的功能还为开发完，待优化和完善
 */
@Slf4j
@Component
public class CustomToolContextToMcpMetaConverter implements ToolContextToMcpMetaConverter {

    @Override
    public Map<String, Object> convert(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            return Map.of();
        }

        // 将工具上下文转换为MCP元数据的自定义逻辑
        Map<String, Object> metadata = new HashMap<>();

        // 示例：为所有键添加自定义前缀
        for (Map.Entry<String, Object> entry : toolContext.getContext().entrySet()) {
            if (entry.getValue() != null) {
                metadata.put("app_" + entry.getKey(), entry.getValue());
            }
        }

        // 添加元数据
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.put("source", "spring-ai");

        return metadata;
    }
}