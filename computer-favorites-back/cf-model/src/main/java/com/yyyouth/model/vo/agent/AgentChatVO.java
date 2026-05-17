package com.yyyouth.model.vo.agent;

import lombok.Builder;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * SSE 连接元信息
 */
@Data
@Builder
public class AgentChatVO {
    private String conversationId;
    private Long sessionId;
    private String skillName;
}
