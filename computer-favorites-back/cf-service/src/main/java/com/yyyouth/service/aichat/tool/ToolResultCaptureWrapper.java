package com.yyyouth.service.aichat.tool;

import com.yyyouth.service.aichat.chat.AgentRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * @author yyyouth zg
 * @date 2026-05-20
 *
 * ToolCallback 包装器：拦截工具返回值，若为 incomplete 则暂存到 PendingToolResultStore。
 * 同时通过 AgentRequestContext 将 ownerId 传入 Reactor 线程，供工具方法使用。
 * 每次 ChatOrchestrator.orchestrate() 调用时新建实例。
 */
@Slf4j
public class ToolResultCaptureWrapper implements ToolCallback {

    private final ToolCallback delegate;
    private final PendingToolResultStore store;
    private final String conversationId;
    private final Long ownerId;

    public ToolResultCaptureWrapper(ToolCallback delegate,
                                    PendingToolResultStore store,
                                    String conversationId,
                                    Long ownerId) {
        this.delegate = delegate;
        this.store = store;
        this.conversationId = conversationId;
        this.ownerId = ownerId;
    }

    @Override
    public String call(String toolInput) {
        // 将 ownerId 注入当前 Reactor 线程的 ThreadLocal，工具方法可通过
        // AgentRequestContext.getCurrentUserId() 获取当前用户ID
        if (ownerId != null) {
            AgentRequestContext.setCurrentUserId(ownerId);
        }
        try {
            String result = delegate.call(toolInput);
            if (result != null && result.contains("\"status\":\"incomplete\"")) {
                log.info("ToolResultCaptureWrapper: 捕获 incomplete 结果, tool={}, conversationId={}",
                        delegate.getToolDefinition().name(), conversationId);
                store.put(conversationId, result);
            }
            return result;
        } finally {
            AgentRequestContext.clearCurrentUserId();
        }
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }
}
