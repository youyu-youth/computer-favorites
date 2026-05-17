package com.yyyouth.service.aichat.config;

import com.yyyouth.service.aichat.agent.tool.WebsiteAgentTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * AI 代理工具注册配置
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class ToolsConfig {

    @Bean
    public ToolCallback[] registerTools(WebsiteAgentTools websiteAgentTools) {
        return ToolCallbacks.from(websiteAgentTools);
    }
}
