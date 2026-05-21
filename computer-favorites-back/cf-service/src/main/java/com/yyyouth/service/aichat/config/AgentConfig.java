package com.yyyouth.service.aichat.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent ChatClient 配置。maxTokens 设为 4096 防止 tool call 参数 JSON 截断。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AgentConfig {

    private final DashScopeChatModel dashscopeChatModel;
    private final ChatMemory jdbcChatMemory;

    @Primary
    @Bean
    public ChatClient dashscopeChatClient() {
        log.info("创建 Agent DashScope ChatClient");
        return ChatClient.builder(dashscopeChatModel)
                .defaultOptions(DashScopeChatOptions.builder()
                        .maxToken(4096)
                        .build())
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(jdbcChatMemory).build())
                .build();
    }

    /**
     * 轻量 ChatClient（无 ChatMemory advisor），供 IntentRouter 分类使用
     */
    @Bean
    public ChatClient plainDashscopeChatClient() {
        return ChatClient.builder(dashscopeChatModel)
                .defaultOptions(DashScopeChatOptions.builder()
                        .maxToken(4096)
                        .build())
                .build();
    }
}
