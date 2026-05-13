package com.yyyouth.service.aichat.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import com.yyyouth.service.aichat.demo.domain.MyResponseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/28 agent 配置类
 */
@RequiredArgsConstructor
@Slf4j
@Configuration
public class AgentConfig {


    @Value("classpath:/docs/SYSTEM_PROMPT_ROLE.md")
    private Resource systemResource;


//    String SYSTEM_PROMPT = "You are a helpful assistant.";


    /**
     * 结构化输出验证顾问
     **/
    StructuredOutputValidationAdvisor validationAdvisor = StructuredOutputValidationAdvisor.builder()
            .outputType(MyResponseType.class)
            .maxRepeatAttempts(3)  // 最大尝试次数
            .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 1000)
            .build();

    private final DashScopeChatModel dashscopeChatModel;

    private final DeepSeekChatModel deepSeekChatModel;

    private final ChatMemory jdbcChatMemory;

    private final ToolCallback[] toolCallback;

    private final VectorStore vectorStore;

    // 同步mcp服务
//    private final SyncMcpToolCallbackProvider mcpToolCallbackProvider;

    //异步 mcp服务
    private final AsyncMcpToolCallbackProvider  mcpToolCallbackProvider;



    @Bean(name = "nolChatClient")
    public ChatClient nolChatClient() {
        log.info("nolChatClient !~!!!!!");
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(systemResource)  // 设置默认系统提示
                .defaultToolCallbacks(mcpToolCallbackProvider)  // 添加异步mcp服务
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder().similarityThreshold(0.6d).topK(6).build())
                        .build())  // rag知识库
                .build();
        return chatClient;
    }

    @Bean(name = "deepseekChatClient")
    public ChatClient deepseekChatClient() {
        log.info("创建deepseekChatClient !~!!!!!");
        ChatClient chatClient = ChatClient.builder(deepSeekChatModel)
                .defaultSystem(systemResource)  // 设置默认系统提示
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(jdbcChatMemory).build())
                .defaultToolCallbacks(toolCallback) // 设置工具回调
                .build();
        return chatClient;
    }


    @Bean(name = "dashscopeChatClient")
    public ChatClient dashscopeChatClient() {
        log.info("创建dashscopeChatClient !~!!!!!");
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(systemResource)  // 设置默认系统提示
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(jdbcChatMemory).build())
                .defaultToolCallbacks(toolCallback) // 设置工具回调
                .build();
        return chatClient;
    }


}
