package com.yyyouth.service.aichat.agent.tools;

import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/09 定义增强参数（模型额外提供的推理信息）
 */
public record AgentThinking(
        @ToolParam(description = "调用工具的推理过程", required = true)
        String innerThought,

        @ToolParam(description = "信心等级 (low, medium, high)", required = false)
        String confidence
) {
}
