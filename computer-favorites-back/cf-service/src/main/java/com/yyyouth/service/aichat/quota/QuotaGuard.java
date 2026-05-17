package com.yyyouth.service.aichat.quota;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.model.pojo.agent.AgentQuotaPolicy;
import com.yyyouth.model.pojo.agent.AgentUsageDaily;
import com.yyyouth.model.vo.agent.AgentQuotaVO;
import com.yyyouth.service.mapper.AgentQuotaPolicyMapper;
import com.yyyouth.service.mapper.AgentUsageDailyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 配额守卫：检查用户/管理员当日使用量是否超限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuotaGuard {

    private final AgentQuotaPolicyMapper quotaPolicyMapper;
    private final AgentUsageDailyMapper usageDailyMapper;

    public AgentQuotaVO checkAndGetQuota(String ownerType, Long ownerId) {
        String defaultSubjectType = "admin".equals(ownerType) ? "default_admin" : "default_user";

        AgentQuotaPolicy policy = quotaPolicyMapper.selectOne(
                new LambdaQueryWrapper<AgentQuotaPolicy>()
                        .eq(AgentQuotaPolicy::getSubjectType, defaultSubjectType)
                        .eq(AgentQuotaPolicy::getEnabled, 1));

        if (policy == null) {
            return AgentQuotaVO.builder()
                    .dailyMessageLimit(Integer.MAX_VALUE)
                    .usedMessageCount(0)
                    .remainingMessages(Integer.MAX_VALUE)
                    .build();
        }

        AgentUsageDaily usage = usageDailyMapper.selectOne(
                new LambdaQueryWrapper<AgentUsageDaily>()
                        .eq(AgentUsageDaily::getOwnerType, ownerType)
                        .eq(AgentUsageDaily::getOwnerId, ownerId)
                        .eq(AgentUsageDaily::getUsageDate, LocalDate.now()));

        int used = usage != null ? usage.getMessageCount() : 0;
        int usedTokens = usage != null
                ? usage.getInputTokens() + usage.getOutputTokens()
                : 0;
        int remaining = policy.getDailyMessageLimit() - used;
        int remainingTokens = policy.getDailyTokenLimit() - usedTokens;

        return AgentQuotaVO.builder()
                .dailyMessageLimit(policy.getDailyMessageLimit())
                .usedMessageCount(used)
                .remainingMessages(Math.max(0, remaining))
                .dailyTokenLimit(policy.getDailyTokenLimit())
                .usedTokens(usedTokens)
                .build();
    }

    public boolean isExceeded(String ownerType, Long ownerId) {
        AgentQuotaVO quota = checkAndGetQuota(ownerType, ownerId);
        return quota.getRemainingMessages() <= 0;
    }
}
