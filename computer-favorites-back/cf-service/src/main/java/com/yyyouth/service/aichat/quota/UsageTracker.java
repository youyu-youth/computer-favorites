package com.yyyouth.service.aichat.quota;

import com.yyyouth.model.pojo.agent.AgentUsageDaily;
import com.yyyouth.service.mapper.AgentUsageDailyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 用量追踪：对话结束后更新每日用量
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsageTracker {

    private final AgentUsageDailyMapper usageDailyMapper;

    @Transactional
    public void record(String ownerType, Long ownerId, int messageCount, int inputTokens, int outputTokens) {
        LocalDate today = LocalDate.now();
        AgentUsageDaily usage = usageDailyMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentUsageDaily>()
                        .eq(AgentUsageDaily::getOwnerType, ownerType)
                        .eq(AgentUsageDaily::getOwnerId, ownerId)
                        .eq(AgentUsageDaily::getUsageDate, today));

        if (usage == null) {
            usage = new AgentUsageDaily();
            usage.setOwnerType(ownerType);
            usage.setOwnerId(ownerId);
            usage.setUsageDate(today);
            usage.setMessageCount(messageCount);
            usage.setInputTokens(inputTokens);
            usage.setOutputTokens(outputTokens);
            usage.setEstimatedCost(BigDecimal.ZERO);
            usageDailyMapper.insert(usage);
        } else {
            usage.setMessageCount(usage.getMessageCount() + messageCount);
            usage.setInputTokens(usage.getInputTokens() + inputTokens);
            usage.setOutputTokens(usage.getOutputTokens() + outputTokens);
            usageDailyMapper.updateById(usage);
        }
    }
}
