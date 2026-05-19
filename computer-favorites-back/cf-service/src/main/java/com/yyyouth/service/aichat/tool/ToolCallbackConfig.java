package com.yyyouth.service.aichat.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 收集所有 @Tool Bean，构建 toolCode → ToolCallback 映射
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ToolCallbackConfig {

    private final UserWebsiteTool userWebsiteTool;
    private final AdminWebsiteTool adminWebsiteTool;

    @Bean
    public Map<String, ToolCallback> allToolCallbacks() {
        ToolCallback[] callbacks = ToolCallbacks.from(userWebsiteTool, adminWebsiteTool);
        Map<String, ToolCallback> map = Stream.of(callbacks)
                .collect(Collectors.toMap(
                        tc -> tc.getToolDefinition().name(),
                        Function.identity(),
                        (a, b) -> a));
        log.info("ToolCallback 注册完成，共 {} 个工具", map.size());
        return map;
    }
}
