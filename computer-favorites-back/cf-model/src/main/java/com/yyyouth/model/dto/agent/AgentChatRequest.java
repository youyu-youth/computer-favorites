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

    /** 技能编码（可选，为空时走意图路由自动判断） */
    private String skillCode;
}
