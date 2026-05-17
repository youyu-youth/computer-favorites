package com.yyyouth.service.aichat;


import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;

import org.springframework.boot.test.context.SpringBootTest;


/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/05
 */
@SpringBootTest(classes = com.yyyouth.core.ComputerFavoritesApplication.class)
public class ChatClientTest03MCP {

    @Resource(name = "dashscopeChatClient")
    ChatClient dashscopeChatClient;


    @Resource(name = "deepseekChatClient")
    ChatClient deepseekChatClient;

    @Resource(name = "nolChatClient")
    ChatClient nolChatClient;

    @Resource
    AsyncMcpToolCallbackProvider mcpToolCallbackProvider;
//    SyncMcpToolCallbackProvider mcpToolCallbackProvider;




    @Test
    public void test01(){
        String text = nolChatClient.prompt().user("帮我使用 tavily mcp 搜索一下2026年 github上热门的开源项目")
                .call().chatResponse().getResult().getOutput().getText();
        System.out.println("结果："+ text);
    }




    @Test
    public void listMcpTools() {
        ToolCallback[] toolCallbacks = mcpToolCallbackProvider.getToolCallbacks();
        System.out.println("MCP tools size：" + toolCallbacks.length);
        for (ToolCallback toolCallback : toolCallbacks) {
            System.out.println("工具名称：" + toolCallback.getToolDefinition().name());
            System.out.println("工具描述：" + toolCallback.getToolDefinition().description());
            System.out.println("工具参数：" + toolCallback.getToolDefinition().inputSchema());
        }
        Assertions.assertTrue(toolCallbacks.length > 0, "未发现 MCP 工具，请检查 spring.ai.mcp.client 配置和 Tavily 连接");
    }
}
