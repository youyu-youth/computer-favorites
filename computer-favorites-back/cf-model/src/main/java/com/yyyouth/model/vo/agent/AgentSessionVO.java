package com.yyyouth.model.vo.agent;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 会话列表项
 */
@Data
@Builder
public class AgentSessionVO {
    private Long id;
    private String conversationId;
    private String title;
    private String skillCode;
    private String status;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createTime;
}
