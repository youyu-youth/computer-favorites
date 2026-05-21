package com.yyyouth.service.aichat.chat;

/**
 * @author yyyouth zg
 * @date 2026-05-21
 *
 * 请求级上下文。ToolResultCaptureWrapper 在 Reactor 线程中通过 ThreadLocal
 * 设置当前 userId，UserWebsiteTool 在执行时通过 {@link #getCurrentUserId()} 读取。
 */
public class AgentRequestContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void clearCurrentUserId() {
        CURRENT_USER_ID.remove();
    }
}
