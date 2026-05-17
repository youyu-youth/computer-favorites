package com.yyyouth.service.aichat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Author: yyyouth(yyyouth) --zg
 * @Date:2026/05/04
 * 聊天内存配置类
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class ChatMemoryConfig {


    @Bean(name = "jdbcChatMemory")
    ChatMemory jdbcChatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory
        .builder()
        .maxMessages(10) // 配置聊天记录最大存储数量
        .chatMemoryRepository(jdbcChatMemoryRepository).build();
    }

}
