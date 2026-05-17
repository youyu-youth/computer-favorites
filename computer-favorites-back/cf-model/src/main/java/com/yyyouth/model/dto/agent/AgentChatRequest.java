package com.yyyouth.model.dto.agent;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent 对话请求
 */
@Data
public class AgentChatRequest {
    private String conversationId;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    @NotBlank(message = "技能编码不能为空")
    private String skillCode;
}
