package com.yyyouth.service.aichat.agent.tools;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yyyouth zg
 * @date 2026-05-11
 * 工具注册表
 */
@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, McpSchema.Tool> tools = new ConcurrentHashMap<>();

    public void updateTools(List<McpSchema.Tool> updatedTools) {
        // 将原工具列表清空
        tools.clear();
        // 添加新的工具
        for (McpSchema.Tool tool : updatedTools) {
            tools.put(tool.name(), tool);
        }
        log.info("本地工具更新: " + tools.keySet());
    }

    public McpSchema.Tool getTool(String name) {
        return tools.get(name);
    }

    public Collection<McpSchema.Tool> getAllTools() {
        return tools.values();
    }
}
