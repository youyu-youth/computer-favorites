package com.yyyouth.service.aichat.agent.rag.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/22 阶段2：Retrieval  检索阶段的配置
 * 对该项目的功能还为开发完
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AgentDocumentRetriever {
    private final VectorStore vectorStore;


    private final AgentContextualGeneration contextualGeneration;



    /**
     * 根据文档检索创建顾问
     * @param status
     * @return
     */
    public Advisor createAdvisorByDocumentRetriever(String status){
        // 1. 进行关键词过滤
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();

        //2. 进行文档检索
        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.7)
                .topK(5)
                .filterExpression(expression)
                .build();

        //3. 创建顾问
        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(contextualGeneration.emptyContextHandle())  // 查询增强
                .build();

        //4. 返回顾问
        return advisor;
    }

}
