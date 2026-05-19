package com.yyyouth.service.aichat.chat;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * 当前请求 SSE 推送器上下文，使用 ThreadLocal 实现，
 * 使 ConfirmableToolCallback 在工具执行时能够推送 SSE 事件。
 */
public class AgentSseContext {

    private static final ThreadLocal<AgentStreamSink> HOLDER = new ThreadLocal<>();

    public static void set(AgentStreamSink sink) {
        HOLDER.set(sink);
    }

    public static AgentStreamSink get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
