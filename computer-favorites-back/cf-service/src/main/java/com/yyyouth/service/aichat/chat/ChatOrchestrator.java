package com.yyyouth.service.aichat.chat;

import com.yyyouth.model.pojo.agent.AgentSession;
import com.yyyouth.model.vo.agent.AgentQuotaVO;
import com.yyyouth.service.aichat.agent.tools.ToolRegistry;
import com.yyyouth.service.aichat.mcp.AgentMcpServerService;
import com.yyyouth.service.aichat.quota.QuotaGuard;
import com.yyyouth.service.aichat.skill.GeneralChatPrompt;
import com.yyyouth.service.aichat.session.AgentSessionService;
import com.yyyouth.service.aichat.skill.SkillDefinition;
import com.yyyouth.service.aichat.skill.SkillLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import com.yyyouth.service.aichat.tool.PendingToolResultStore;
import com.yyyouth.service.aichat.tool.ToolResultCaptureWrapper;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * Agent 对话编排引擎。
 * 无工具时走 simpleChat（纯流式），有工具时走 orchestrate（先 call 触发工具链再 stream 输出最终结果）。
 * Phase 3 将在工具执行层加入 ConfirmableToolCallback 包装器实现风险拦截。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOrchestrator {

    private final ChatClient dashscopeChatClient;
    private final SkillLoader skillLoader;
    private final ToolRegistry toolRegistry;
    private final QuotaGuard quotaGuard;
    private final AgentSessionService sessionService;
    private final AgentMcpServerService mcpServerService;
    private final IntentRouter intentRouter;
    private final PendingToolResultStore pendingToolResultStore;

    public void handle(String ownerType, Long ownerId, String message, String skillCode,
                        String conversationId, SseEmitter emitter) {
        AgentStreamSink sink = new AgentStreamSink(emitter);
        log.info("[handle] 开始: ownerType={}, ownerId={}, conversationId={}, skillCode={}",
                ownerType, ownerId, conversationId, skillCode);

        AgentQuotaVO quota = quotaGuard.checkAndGetQuota(ownerType, ownerId);
        log.info("[handle] 配额检查完成: remaining={}", quota.getRemainingMessages());
        if (quota.getRemainingMessages() <= 0) {
            log.warn("[handle] 配额不足，拦截请求");
            sink.quotaExceeded(quota.getDailyMessageLimit(), quota.getUsedMessageCount());
            emitter.complete();
            return;
        }

        // 意图路由：skillCode 为空时，LLM 判断闲聊还是功能请求
        String effectiveSkillCode = skillCode;
        if (skillCode == null || skillCode.isBlank()) {
            String intent = intentRouter.classify(message);
            log.info("[handle] 意图路由: intent={}", intent);
            if ("functional".equals(intent)) {
                SkillDefinition defaultSkill = skillLoader.getFirstForAudience(ownerType);
                if (defaultSkill != null) {
                    effectiveSkillCode = defaultSkill.getSkillCode();
                    log.info("[handle] 功能意图，自动匹配技能: {}", effectiveSkillCode);
                } else {
                    log.warn("[handle] 功能意图但无可匹配技能，走闲聊");
                }
            }
        }

        // 加载技能
        SkillDefinition skill = null;
        if (effectiveSkillCode != null && !effectiveSkillCode.isBlank()) {
            skill = skillLoader.get(effectiveSkillCode);
            if (skill == null) {
                log.warn("[handle] 技能不存在: {}", effectiveSkillCode);
                sink.error("INVALID_SKILL", "技能编码无效: " + effectiveSkillCode);
                emitter.complete();
                return;
            }
            log.info("[handle] 技能加载: skill={}", skill.getName());
        } else {
            log.info("[handle] 无技能，进入通用闲聊模式");
        }

        log.info("[handle] 开始 getOrCreate 会话...");
        AgentSession session;
        try {
            long t0 = System.currentTimeMillis();
            session = sessionService.getOrCreate(
                    conversationId, ownerType, ownerId, "default",
                    effectiveSkillCode, "dashscope", "qwen-plus");
            log.info("[handle] getOrCreate 耗时: {}ms", System.currentTimeMillis() - t0);
        } catch (Exception e) {
            log.error("[handle] getOrCreate 异常: conversationId={}, ownerType={}, ownerId={}",
                    conversationId, ownerType, ownerId, e);
            sink.error("SESSION_CREATE_ERROR", "创建会话失败: " + e.getMessage());
            return;
        }
        log.info("[handle] 会话就绪: sessionId={}, conversationId={}", session.getId(), session.getConversationId());

        // 工具加载
        ToolCallback[] tools;
        if (skill != null) {
            log.info("[handle] 获取工具: allowlist={}", skill.getToolAllowlist());
            ToolCallback[] localTools = toolRegistry.getFilteredCallbacks(skill.getToolAllowlist());
            log.info("[handle] 本地工具数: {}", localTools.length);
            tools = mcpServerService.mergeTools(localTools, skill.getMcpAllowlist());
            log.info("[handle] 合并后工具数: {} (本地{} + MCP白名单{})",
                    tools.length, localTools.length, skill.getMcpAllowlist());
        } else {
            tools = new ToolCallback[0];
            log.info("[handle] 无技能，无工具");
        }

        try {
            if (tools.length == 0) {
                log.info("[handle] 进入 simpleChat 分支");
                simpleChat(sink, skill, message, session);
            } else {
                log.info("[handle] 进入 orchestrate 分支, tools={}", tools.length);
                orchestrate(sink, skill, message, tools, session);
            }
            log.info("[handle] 编排方法返回");
        } catch (Exception e) {
            log.error("[handle] ChatOrchestrator 异常: sessionId={}", session.getId(), e);
            sink.error("ORCHESTRATE_ERROR", e.getMessage());
        }
    }

    /**
     * 纯对话模式：直接 SSE 流式输出
     */
    private void simpleChat(AgentStreamSink sink, SkillDefinition skill, String message,
                            AgentSession session) {
        log.info("simpleChat 开始: sessionId={}", session.getId());
        sink.thinking(1);

        dashscopeChatClient.prompt()
                .system(skill != null ? skill.getSystemPrompt() : GeneralChatPrompt.SYSTEM_PROMPT)
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, session.getConversationId()))
                .stream()
                .content()
                .doOnNext(sink::message)
                .doOnComplete(() -> {
                    log.info("simpleChat 完成: sessionId={}", session.getId());
                    sink.done(session.getId(), session.getConversationId());
                })
                .doOnError(e -> {
                    log.error("simpleChat 异常: sessionId={}", session.getId(), e);
                    sink.error("STREAM_ERROR", e.getMessage());
                })
                .subscribe();
    }

    /**
     * 工具编排模式：Spring AI 自动执行工具循环。
     * 通过 ToolResultCaptureWrapper 拦截工具返回值，
     * 若 incomplete 则暂存到 PendingToolResultStore，
     * 在 doOnComplete 中通过 done 事件携带给前端。
     */
    private void orchestrate(AgentStreamSink sink, SkillDefinition skill, String message,
                             ToolCallback[] tools, AgentSession session) {
        log.info("orchestrate 开始: sessionId={}, tools={}", session.getId(), tools.length);
        sink.thinking(1);

        String conversationId = session.getConversationId();
        Long ownerId = session.getOwnerId();
        ToolCallback[] capturingTools = new ToolCallback[tools.length];
        for (int i = 0; i < tools.length; i++) {
            capturingTools[i] = new ToolResultCaptureWrapper(
                    tools[i], pendingToolResultStore, conversationId, ownerId);
        }

        dashscopeChatClient.prompt()
                .system(skill.getSystemPrompt() != null
                        ? skill.getSystemPrompt() : GeneralChatPrompt.SYSTEM_PROMPT)
                .user(message)
                .toolCallbacks(capturingTools)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .doOnNext(delta -> {
                    log.debug("orchestrate delta: {}", delta);
                    sink.message(delta);
                })
                .doOnComplete(() -> {
                    log.info("orchestrate 完成: sessionId={}", session.getId());
                    String pendingResult = pendingToolResultStore.getAndClear(conversationId);
                    sink.done(session.getId(), conversationId, pendingResult);
                })
                .doOnError(e -> {
                    log.error("orchestrate 异常: sessionId={}", session.getId(), e);
                    pendingToolResultStore.getAndClear(conversationId);
                    sink.error("STREAM_ERROR", e.getMessage());
                })
                .subscribe();
    }
}
