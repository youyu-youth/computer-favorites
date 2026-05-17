package com.yyyouth.service.aichat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/14 AdvisorConfig 配置类
 * //todo 对该项目的功能还为开发完，待优化和完善
 */
@RequiredArgsConstructor
@Slf4j
@Configuration
public class AdvisorConfig {


    private final VectorStore vectorStore;


    /**
     * 检索增强 Advisor
     * @return
     */
    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor() {
         //todo 这里只是一个举例，请根据项目需求进行修改
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();
    }
}
