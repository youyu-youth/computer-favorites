package com.yyyouth.service.aichat.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/28 agent 配置类
 */
@Slf4j
@Configuration
public class AgentConfig {

    String SYSTEM_PROMPT = "You are a helpful assistant.";


    @Autowired
    DashScopeChatModel dashscopeChatModel;

    @Bean
    public ChatClient chatClient() {
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)  // 设置默认系统提示
                .build();
        return chatClient;
    }


}
