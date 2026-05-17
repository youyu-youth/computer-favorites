package com.yyyouth.service.aichat.agent.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.McpConnectionInfo;
import org.springframework.ai.mcp.McpToolFilter;
import org.springframework.stereotype.Component;


/**
 * @author yyyouth zg
 * @date 2026-05-10
 *  自定义mcp工具过滤器
 *  对该项目的功能还为开发完，待优化和完善
 */
@Component
public class CustomMcpToolFilter implements McpToolFilter {

    @Override
    public boolean test(McpConnectionInfo connectionInfo, McpSchema.Tool tool) {

        if (tool.name().contains("delete") || tool.name().contains("exec")) {
            return false;
        }

        return true;
    }
}
