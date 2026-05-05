package com.yyyouth.service.aichat.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import com.yyyouth.service.aichat.demo.domain.MyResponseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/28 agent 配置类
 */
@RequiredArgsConstructor
@Slf4j
@Configuration
public class AgentConfig {

    String SYSTEM_PROMPT = "You are a helpful assistant.";


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


    @Bean(name = "deepseekChatClient")
    public ChatClient deepseekChatClient() {
        log.info("创建deepseekChatClient !~!!!!!");
        ChatClient chatClient = ChatClient.builder(deepSeekChatModel)
                .defaultSystem(SYSTEM_PROMPT)  // 设置默认系统提示
                .defaultAdvisors(PromptChatMemoryAdvisor.builder(jdbcChatMemory).build())
                .build();
        return chatClient;
    }


    @Bean(name = "dashscopeChatClient")
    public ChatClient dashscopeChatClient() {
        log.info("创建dashscopeChatClient !~!!!!!");
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)  // 设置默认系统提示
                .defaultAdvisors(PromptChatMemoryAdvisor.builder(jdbcChatMemory).build())
                .build();
        return chatClient;
    }


}
