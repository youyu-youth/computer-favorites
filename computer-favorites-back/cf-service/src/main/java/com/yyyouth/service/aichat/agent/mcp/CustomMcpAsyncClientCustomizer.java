package com.yyyouth.service.aichat.agent.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.customizer.McpAsyncClientCustomizer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;


/**
 * @author yyyouth zg
 * @date 2026-05-10
 *
 * MCP 异步客户端定制器
 * webflux starter 底层创建的是 Async 客户端，必须使用 McpClientCustomizer<McpClient.AsyncSpec>
 *      对该项目的功能还为开发完，待优化和完善
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class CustomMcpAsyncClientCustomizer implements McpAsyncClientCustomizer {

    @Override
    public void customize(String name, McpClient.AsyncSpec spec) {
        log.info("🎢🎢🎢🎢MCP 异步客户端定制器生效，客户端名称：{}", name);

        spec.requestTimeout(Duration.ofSeconds(60));


        spec.progressConsumer((McpSchema.ProgressNotification progress) -> {
            log.info("mcp工具的调用：progress message = > {}", progress.message());
            log.info("mcp工具的调用：progress progressToken = > {}", progress.progressToken());
            log.info("mcp工具的调用：progress progress = > {}", progress.progress().toString());
            log.info("mcp工具的调用：progress total = > {}", progress.total().toString());
            return null;
        });

        spec.toolsChangeConsumer((List<McpSchema.Tool> tools) -> {
            log.info("mcp工具的调用：tools size = > {}", tools.size());
            return null;
        });

        spec.resourcesChangeConsumer((List<McpSchema.Resource> resources) -> {
            log.info("mcp工具的调用：resources size = > {}", resources.size());
            return null;
        });

        spec.promptsChangeConsumer((List<McpSchema.Prompt> prompts) -> {
            log.info("mcp工具的调用：prompts size = > {}", prompts.size());
            return null;
        });

        spec.loggingConsumer((McpSchema.LoggingMessageNotification logMsg) -> {
            log.info("mcp工具的调用：logMsg = > {}", logMsg);
            return null;
        });
    }

}
