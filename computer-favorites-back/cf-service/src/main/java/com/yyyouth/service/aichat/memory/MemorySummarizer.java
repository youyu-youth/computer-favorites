package com.yyyouth.service.aichat.memory;

import com.yyyouth.model.pojo.agent.AgentMemorySummary;
import com.yyyouth.service.mapper.AgentMemorySummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 长期记忆摘要：对话完成后异步生成摘要存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemorySummarizer {

    private final AgentMemorySummaryMapper memorySummaryMapper;

    @Async
    public void summarizeAsync(Long sessionId, String conversationText, String summaryType) {
        log.info("开始生成记忆摘要: sessionId={}, type={}", sessionId, summaryType);

        String summary = "会话 #" + sessionId + " 的摘要内容（后续接入 LLM 摘要生成）";

        AgentMemorySummary mem = new AgentMemorySummary();
        mem.setSessionId(sessionId);
        mem.setSummaryType(summaryType);
        mem.setSummaryText(summary);
        memorySummaryMapper.insert(mem);

        log.info("记忆摘要生成完成: sessionId={}", sessionId);
    }
}
