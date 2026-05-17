package com.yyyouth.model.vo.agent;

import lombok.Builder;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 当日剩余配额
 */
@Data
@Builder
public class AgentQuotaVO {
    private Integer dailyMessageLimit;
    private Integer usedMessageCount;
    private Integer remainingMessages;
    private Integer dailyTokenLimit;
    private Integer usedTokens;
}
