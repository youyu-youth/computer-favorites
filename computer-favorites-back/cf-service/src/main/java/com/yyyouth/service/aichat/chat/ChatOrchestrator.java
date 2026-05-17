package com.yyyouth.service.aichat.chat;

import com.yyyouth.model.pojo.agent.AgentSession;
import com.yyyouth.model.vo.agent.AgentQuotaVO;
import com.yyyouth.service.aichat.agent.tools.ToolRegistry;
import com.yyyouth.service.aichat.quota.QuotaGuard;
import com.yyyouth.service.aichat.session.AgentSessionService;
import com.yyyouth.service.aichat.skill.SkillDefinition;
import com.yyyouth.service.aichat.skill.SkillLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOrchestrator {

    private final ChatClient dashscopeChatClient;
    private final SkillLoader skillLoader;
    private final ToolRegistry toolRegistry;
    private final QuotaGuard quotaGuard;
    private final AgentSessionService sessionService;

    public void handle(String ownerType, Long ownerId, String message, String skillCode,
                        String conversationId, SseEmitter emitter) {
        AgentStreamSink sink = new AgentStreamSink(emitter);
        AgentQuotaVO quota = quotaGuard.checkAndGetQuota(ownerType, ownerId);
        if (quota.getRemainingMessages() <= 0) {
            sink.quotaExceeded(quota.getDailyMessageLimit(), quota.getUsedMessageCount());
            emitter.complete();
            return;
        }
        SkillDefinition skill = skillLoader.get(skillCode);
        if (skill == null) {
            sink.error("INVALID_SKILL", "技能编码无效: " + skillCode);
            emitter.complete();
            return;
        }
        AgentSession session = sessionService.getOrCreate(
                conversationId, ownerType, ownerId, "default", skillCode, "dashscope", "qwen-plus");
        ToolCallback[] filteredTools = toolRegistry.getFilteredCallbacks(skill.getToolAllowlist());
        if (filteredTools.length == 0) {
            simpleStream(sink, skill, message, session);
        } else {
            reactiveStream(sink, skill, message, filteredTools, session);
        }
    }

    private void simpleStream(AgentStreamSink sink, SkillDefinition skill, String message, AgentSession session) {
        try {
            log.info("simpleStream 开始: sessionId={}", session.getId());
            Flux<String> flux = dashscopeChatClient.prompt()
                    .system(skill.getSystemPrompt()).user(message).stream().content();
            flux.doOnNext(delta -> {
                        log.debug("simpleStream delta: {}", delta);
                        sink.message(delta);
                    })
                    .doOnComplete(() -> {
                        log.info("simpleStream 完成: sessionId={}", session.getId());
                        sink.done(session.getId());
                    })
                    .doOnError(e -> {
                        log.error("simpleStream 异常: {}", e.getMessage(), e);
                        sink.error("STREAM_ERROR", e.getMessage());
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("simpleStream 启动失败", e);
            sink.error("CHAT_ERROR", e.getMessage());
        }
    }

    private void reactiveStream(AgentStreamSink sink, SkillDefinition skill, String message,
                                 ToolCallback[] tools, AgentSession session) {
        try {
            log.info("reactiveStream 开始: sessionId={}, tools={}", session.getId(), tools.length);
            Flux<String> flux = dashscopeChatClient.prompt()
                    .system(skill.getSystemPrompt()).user(message).tools(tools).stream().content();
            flux.doOnNext(delta -> {
                        log.debug("reactiveStream delta: {}", delta);
                        sink.message(delta);
                    })
                    .doOnComplete(() -> {
                        log.info("reactiveStream 完成: sessionId={}", session.getId());
                        sink.done(session.getId());
                    })
                    .doOnError(e -> {
                        log.error("reactiveStream 异常: {}", e.getMessage(), e);
                        sink.error("STREAM_ERROR", e.getMessage());
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("reactiveStream 启动失败", e);
            sink.error("CHAT_ERROR", e.getMessage());
        }
    }
}
