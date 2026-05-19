package com.yyyouth.service.aichat.memory;

import com.yyyouth.model.pojo.agent.AgentMemorySummary;
import com.yyyouth.service.mapper.AgentMemorySummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * 长期记忆摘要服务。对话消息数超阈值时异步生成 LLM 摘要存入 t_agent_memory_summary。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemorySummarizer {

    private static final int SUMMARY_TRIGGER_COUNT = 20;

    private final AgentMemorySummaryMapper memorySummaryMapper;
    private final ChatModel dashScopeChatModel;
    private final ChatMemory jdbcChatMemory;

    @Async
    public void summarizeAsync(Long sessionId, String conversationId, String summaryType) {
        log.info("开始生成记忆摘要: sessionId={}, type={}", sessionId, summaryType);

        List<Message> messages = jdbcChatMemory.get(conversationId);
        if (messages.isEmpty()) {
            log.info("无足够消息用于摘要: sessionId={}", sessionId);
            return;
        }

        String conversationText = messages.stream()
                .map(m -> m.getMessageType() + ": " + m.getText())
                .collect(Collectors.joining("\n"));

        String summary = generateSummary(conversationText, summaryType);

        AgentMemorySummary mem = new AgentMemorySummary();
        mem.setSessionId(sessionId);
        mem.setSummaryType(summaryType);
        mem.setSummaryText(summary);
        memorySummaryMapper.insert(mem);

        log.info("记忆摘要生成完成: sessionId={}, length={}", sessionId, summary.length());
    }

    private String generateSummary(String conversationText, String summaryType) {
        String template = """
                请对以下对话内容进行简洁摘要（200字以内），提取关键信息和用户意图。
                对话内容：
                {conversation}

                摘要：""";

        Prompt prompt = new PromptTemplate(template)
                .create(Map.of("conversation", conversationText));

        try {
            return dashScopeChatModel.call(prompt).getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("LLM 摘要生成失败，使用降级摘要", e);
            return "对话摘要（降级）：共 " + conversationText.length() + " 字符";
        }
    }
}
