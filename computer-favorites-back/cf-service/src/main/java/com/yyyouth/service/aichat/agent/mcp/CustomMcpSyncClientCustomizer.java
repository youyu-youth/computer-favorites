package com.yyyouth.service.aichat.agent.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-10
 * <p>
 * MCP 同步客户端定制器
 * application.yml 中 type: SYNC，底层创建的是 Sync 客户端，必须使用 McpSyncClientCustomizer
 */
@Slf4j
@Component
public class CustomMcpSyncClientCustomizer implements McpSyncClientCustomizer {

    @Override
    public void customize(String name, McpClient.SyncSpec spec) {
        log.info("MCP 同步客户端定制器生效，客户端名称：{}", name);

        spec.requestTimeout(Duration.ofSeconds(60));

        spec.progressConsumer((McpSchema.ProgressNotification progress) -> {
            log.info("mcp工具的调用：progress message = > {}", progress.message());
            log.info("mcp工具的调用：progress progressToken = > {}", progress.progressToken());
            log.info("mcp工具的调用：progress progress = > {}", progress.progress().toString());
            log.info("mcp工具的调用：progress total = > {}", progress.total().toString());
        });

        spec.toolsChangeConsumer((List<McpSchema.Tool> tools) -> {
            log.info("mcp工具的调用：tools size = > {}", tools.size());
        });

        spec.resourcesChangeConsumer((List<McpSchema.Resource> resources) -> {
            log.info("mcp工具的调用：resources size = > {}", resources.size());
        });

        McpClient.SyncSpec loggingConsumerSpec = spec.loggingConsumer((McpSchema.LoggingMessageNotification logMsg) -> {
            log.info("mcp工具的调用：logMsg = > {}", logMsg);
        });

        McpClient.SyncSpec promptsChangeConsumerSpec = spec.promptsChangeConsumer((List<McpSchema.Prompt> prompts) -> {
            log.info("mcp工具的调用：prompts size = > {}", prompts.size());
        });
    }


}