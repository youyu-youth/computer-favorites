package com.yyyouth.service.aichat.agent.thinking_agent;

import cn.hutool.core.util.StrUtil;
import com.yyyouth.model.enums.ai.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-04-26
 *
 * 抽象基础代理类，管理状态转换和步骤循环。
 * 子类实现 step() 方法，通过 pushEvent 推送 SSE 事件。
 */
@Slf4j
@Data
public abstract class BaseAgent {

    private String agentName;
    private String systemPrompt;
    private int currentStep = 0;
    private int maxSteps = 10;
    private String nextStepPrompt;
    private AgentState state = AgentState.IDLE;
    private ChatClient chatClient;
    private List<Message> contextMsgList;
    private SseEmitter sseEmitter;

    protected void pushEvent(String eventType, Map<String, Object> data) {
        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event().name(eventType).data(data));
            } catch (IOException e) {
                log.error("SSE 推送失败: type={}", eventType, e);
            }
        }
    }

    public void run(String userPrompt, SseEmitter emitter) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        this.sseEmitter = emitter;
        this.contextMsgList = new ArrayList<>();
        state = AgentState.RUNNING;
        contextMsgList.add(new UserMessage(userPrompt));

        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);

                pushEvent("thinking", Map.of("step", stepNumber));
                String stepResult = step();
                log.info("Step {} result: {}", stepNumber, stepResult);
            }
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
            }
            pushEvent("done", Map.of("state", state.name()));
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            pushEvent("error", Map.of("code", "AGENT_ERROR", "msg", e.getMessage()));
        } finally {
            cleanUp();
        }
    }

    protected abstract String step();

    protected void cleanUp() {
    }
}
