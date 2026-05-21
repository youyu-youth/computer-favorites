package com.yyyouth.service.aichat.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yyyouth zg
 * @date 2026-05-20
 *
 * 工具 incomplete 结果暂存器。工具在 Reactor 线程中执行时写入，
 * ChatOrchestrator 在 doOnComplete 中读取并清除。
 * ConcurrentHashMap 提供跨线程 happens-before 保证。
 */
@Slf4j
@Component
public class PendingToolResultStore {

    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

    /** 存入 incomplete 结果 */
    public void put(String conversationId, String toolResultJson) {
        store.put(conversationId, toolResultJson);
        log.debug("PendingToolResultStore.put: conversationId={}", conversationId);
    }

    /** 取出并清除 */
    public String getAndClear(String conversationId) {
        String result = store.remove(conversationId);
        if (result != null) {
            log.debug("PendingToolResultStore.getAndClear: conversationId={}", conversationId);
        }
        return result;
    }
}
