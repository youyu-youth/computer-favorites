package com.yyyouth.service.aichat.agent.mcp;

import com.yyyouth.service.aichat.agent.tools.ToolRegistry;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/11
 * 自定义 Tavily MCP客户端处理器
 *  对该项目的功能还为开发完，待优化和完善
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class TavilyMcpClientHandlers {


    private final ChatClient nolChatClient;

    private final ToolRegistry toolRegistry;


    /**
     * 生成LLM响应
     * @param request 用户输入
     * @return
     */
    private String generateLLMResponse(McpSchema.CreateMessageRequest request) {
        String userPrompt = request.messages().stream()
                .map(msg -> msg.content().toString())
                .collect(Collectors.joining("\n"));

        return nolChatClient.prompt()
                .user(userPrompt)
                .call()
                .content();
    }


    /**
     * 处理来自 MCP 服务器的日志消息通知
     * @param notification
     */
    @McpLogging(clients = "tavily")
    public void handleLoggingMessage(McpSchema.LoggingMessageNotification notification) {
        System.out.println("TavilyMcpClientHandlers Received log: " + notification.level() +
                " - " + notification.data());
    }

    /**
     *  处理来自 MCP 服务器的 LLM 完成采样请求。
     * @param request
     * @return
     */
    @McpSampling(clients = "tavily")
    public McpSchema.CreateMessageResult handleSamplingRequest(McpSchema.CreateMessageRequest request) {
        System.out.println("TavilyMcpClientHandlers Received sampling request: " + request.messages());
        // 处理请求并生成响应
        String response = generateLLMResponse(request);

        return McpSchema.CreateMessageResult.builder()
                .role(McpSchema.Role.ASSISTANT)
                .content(new McpSchema.TextContent(response))
                .model("gpt-4")
                .build();
    }

    /**
     * 处理长时间运行操作的进度通知
     * @param notification
     */
    @McpProgress(clients = "tavily")
    public void handleProgressNotification(McpSchema.ProgressNotification notification) {
        double percentage = notification.progress() * 100;
        System.out.println(String.format("TavilyMcpClientHandlers Progress: %.2f%% - %s",
                percentage, notification.message()));
    }

    /**
     *处理服务器工具列表更改时的通知
     * @param updatedTools
     */
    @McpToolListChanged(clients = "tavily")
    public void handleToolListChanged(List<McpSchema.Tool> updatedTools) {
        System.out.println("TavilyMcpClientHandlers Tool list updated: " + updatedTools.size() + " tools available");
        // 更新本地工具注册表
        toolRegistry.updateTools(updatedTools);
    }

    /**
     * 处理服务器资源列表更改时的通知
     * @param updatedResources
     */
    @McpResourceListChanged(clients = "tavily")
    public void handleResourceListChanged(List<McpSchema.Resource> updatedResources) {
        System.out.println("TavilyMcpClientHandlers Resources updated: " + updatedResources.size());

//        //
//        resourceCache.clear();
//        for (McpSchema.Resource resource : updatedResources) {
//            resourceCache.register(resource);
//        }
    }

}
