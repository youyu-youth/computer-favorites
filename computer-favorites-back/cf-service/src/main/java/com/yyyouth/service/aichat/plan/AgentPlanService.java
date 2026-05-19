package com.yyyouth.service.aichat.plan;

import com.yyyouth.model.pojo.agent.AgentActionPlan;
import com.yyyouth.service.mapper.AgentActionPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * Agent 执行计划管理。高风险工具调用前创建计划，等待用户确认后继续执行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentPlanService {

    private final AgentActionPlanMapper planMapper;
    private final Map<Long, CompletableFuture<Boolean>> pendingFutures = new ConcurrentHashMap<>();

    /**
     * 创建执行计划并持久化
     */
    public AgentActionPlan createPlan(Long taskId, String planType, String summary,
                                       String riskLevel, String actionPayloadJson) {
        AgentActionPlan plan = new AgentActionPlan();
        plan.setTaskId(taskId);
        plan.setPlanType(planType);
        plan.setSummary(summary);
        plan.setRiskLevel(riskLevel);
        plan.setActionPayloadJson(actionPayloadJson);
        plan.setStatus("wait_confirm");
        plan.setExpireTime(LocalDateTime.now().plusMinutes(2));
        planMapper.insert(plan);
        log.info("创建执行计划: planId={}, summary={}, risk={}", plan.getId(), summary, riskLevel);
        return plan;
    }

    /**
     * 创建等待 future，工具回调将阻塞于此
     */
    public CompletableFuture<Boolean> createFuture(Long planId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        pendingFutures.put(planId, future);
        return future;
    }

    /**
     * 阻塞等待用户确认（超时 2 分钟）
     */
    public boolean waitForConfirm(Long planId) {
        CompletableFuture<Boolean> future = pendingFutures.get(planId);
        if (future == null) {
            return false;
        }
        try {
            return future.get(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("计划确认超时或异常: planId={}", planId);
            return false;
        } finally {
            pendingFutures.remove(planId);
        }
    }

    /**
     * 用户确认计划
     */
    public void confirm(Long planId, String confirmUserType, Long confirmUserId) {
        AgentActionPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            log.warn("计划不存在: planId={}", planId);
            return;
        }
        plan.setStatus("approved");
        plan.setConfirmUserType(confirmUserType);
        plan.setConfirmUserId(confirmUserId);
        plan.setConfirmedAt(LocalDateTime.now());
        planMapper.updateById(plan);
        log.info("计划已确认: planId={}", planId);

        CompletableFuture<Boolean> future = pendingFutures.get(planId);
        if (future != null) {
            future.complete(true);
        }
    }

    /**
     * 用户拒绝计划
     */
    public void reject(Long planId, String reason) {
        AgentActionPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            log.warn("计划不存在: planId={}", planId);
            return;
        }
        plan.setStatus("rejected");
        plan.setRejectReason(reason);
        planMapper.updateById(plan);
        log.info("计划已拒绝: planId={}, reason={}", planId, reason);

        CompletableFuture<Boolean> future = pendingFutures.get(planId);
        if (future != null) {
            future.complete(false);
        }
    }

    /**
     * 计划执行完成
     */
    public void markExecuted(Long planId) {
        AgentActionPlan plan = planMapper.selectById(planId);
        if (plan != null) {
            plan.setStatus("executed");
            planMapper.updateById(plan);
        }
    }
}
