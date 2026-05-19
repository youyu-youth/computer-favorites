package com.yyyouth.service.aichat.mcp;

import com.yyyouth.model.pojo.agent.AgentMcpServer;
import com.yyyouth.service.mapper.AgentMcpServerMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * MCP 服务管理。读取 DB 配置，提供按技能白名单过滤的 MCP ToolCallback。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentMcpServerService {

    private final AgentMcpServerMapper mcpServerMapper;
    private final Map<String, McpSyncClient> mcpClients;

    /**
     * 获取已启用的 MCP Server 列表（从 DB 读取配置元数据）
     */
    public List<AgentMcpServer> listEnabled() {
        return mcpServerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentMcpServer>()
                        .eq(AgentMcpServer::getEnabled, 1));
    }

    /**
     * 根据白名单获取 MCP ToolCallback 数组
     */
    public ToolCallback[] getFilteredMcpCallbacks(List<String> mcpAllowlist) {
        if (mcpAllowlist == null || mcpAllowlist.isEmpty()) {
            return new ToolCallback[0];
        }

        List<ToolCallback> result = new ArrayList<>();
        for (String serverCode : mcpAllowlist) {
            McpSyncClient client = mcpClients.get(serverCode);
            if (client != null) {
                SyncMcpToolCallbackProvider provider = new SyncMcpToolCallbackProvider(client);
                Collections.addAll(result, provider.getToolCallbacks());
            } else {
                log.warn("MCP Server 未连接: {}", serverCode);
            }
        }
        return result.toArray(new ToolCallback[0]);
    }

    /**
     * 获取已连接 MCP 客户端名称列表
     */
    public Set<String> getConnectedServers() {
        return mcpClients.keySet();
    }

    /**
     * 合并本地工具和 MCP 工具
     */
    public ToolCallback[] mergeTools(ToolCallback[] localTools, List<String> mcpAllowlist) {
        ToolCallback[] mcpTools = getFilteredMcpCallbacks(mcpAllowlist);
        if (mcpTools.length == 0) {
            return localTools;
        }
        ToolCallback[] merged = Arrays.copyOf(localTools, localTools.length + mcpTools.length);
        System.arraycopy(mcpTools, 0, merged, localTools.length, mcpTools.length);
        log.info("工具合并完成: 本地{} + MCP{} = {}", localTools.length, mcpTools.length, merged.length);
        return merged;
    }
}
