package com.yyyouth.service.aichat.agent.rag;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 关键词元数据增强器
 *  //todo 对该项目的功能还为开发完，待优化和完善
 */
@RequiredArgsConstructor
@Component
public class AgentKeywordEnricher {

    private final DashScopeChatModel dashscopeChatModel;

    private final DeepSeekChatModel deepSeekChatModel;

    public List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(deepSeekChatModel)
                .keywordCount(5)
                .build();
        // 或者使用自定义模板
//        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
//               .keywordsTemplate(YOUR_CUSTOM_TEMPLATE)
//               .build();
        return enricher.apply(documents);
    }
}