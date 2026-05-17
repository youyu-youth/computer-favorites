package com.yyyouth.service.aichat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.yyyouth.model.constants.ai.RagConstants.SIMILARITY_THRESHOLD;
import static com.yyyouth.model.constants.ai.RagConstants.TOP_K;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/13
 * RAG 配置类
 */
@RequiredArgsConstructor
@Slf4j
@Configuration
public class RAGConfig {


    private final VectorStore vectorStore;


    /**
     * 文档合并
     * @return
     */
    @Bean
    public DocumentJoiner documentJoiner(){
        return new ConcatenationDocumentJoiner();
    }



    /**
     * 进行文档检索
     * @return
     */
    @Bean
    public DocumentRetriever documentRetriever() {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .topK(TOP_K)
                .build();
    }
}
