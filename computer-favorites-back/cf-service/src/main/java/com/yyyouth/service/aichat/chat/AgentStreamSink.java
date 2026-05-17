package com.yyyouth.service.aichat.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * SSE 事件推送封装
 */
@RequiredArgsConstructor
public class AgentStreamSink {

    private final SseEmitter emitter;

    public void thinking(int step) {
        send("thinking", Map.of("step", step));
    }

    public void message(String delta) {
        send("message", Map.of("delta", delta));
    }

    public void toolCall(String toolName, String args) {
        send("tool_call", Map.of("toolName", toolName, "args", args));
    }

    public void toolResult(String toolName, String result) {
        send("tool_result", Map.of("toolName", toolName, "result", result));
    }

    public void plan(Long planId, String summary, String risk) {
        send("plan", Map.of("planId", planId, "summary", summary, "risk", risk));
    }

    public void done(Long sessionId) {
        send("done", Map.of("sessionId", sessionId));
    }

    public void error(String code, String msg) {
        send("error", Map.of("code", code, "msg", msg));
    }

    public void quotaExceeded(int limit, int used) {
        send("quota_exceeded", Map.of("limit", limit, "used", used));
    }

    public void heartbeat() {
        send("heartbeat", "");
    }

    private void send(String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            log.warn("SSE 发送失败 event={}: {}", name, e.getMessage());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AgentStreamSink.class);
}
