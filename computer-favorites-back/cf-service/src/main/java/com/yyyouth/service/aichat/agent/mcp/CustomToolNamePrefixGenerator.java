package com.yyyouth.service.aichat.agent.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.mcp.McpConnectionInfo;
import org.springframework.ai.mcp.McpToolNamePrefixGenerator;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-11
 * 自定义mcp工具名称前缀生成器
 *  对该项目的功能还为开发完，待优化和完善
 */
@Slf4j
@Component
public class CustomToolNamePrefixGenerator implements McpToolNamePrefixGenerator {

    /**
     * 自定义mcp工具名称前缀生成器
     * @param connectionInfo
     * @param tool
     * @return
     */
    @NotNull
    @Override
    public String prefixedToolName(McpConnectionInfo connectionInfo, McpSchema.Tool tool) {
        // 服务器名称
        String serverName = connectionInfo.initializeResult().serverInfo().name();
        // 服务器版本
        String serverVersion = connectionInfo.initializeResult().serverInfo().version();
        log.info("serverName: {}, serverVersion: {}", serverName, serverVersion);
        // 生成前缀
        return serverName + "_v" + serverVersion.replace(".", "_") + "_" + tool.name();
    }
}