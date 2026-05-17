package com.yyyouth.service.aichat.agent.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * RAG 处理器：统一向量检索入口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagHandler {

    private final VectorStore vectorStore;

    public List<Document> search(String query, String namespace, int topK, double similarityThreshold) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .similarityThreshold(similarityThreshold)
                        .topK(topK)
                        .filterExpression("namespace == '" + namespace + "'")
                        .build());
    }

    public void storeDocuments(List<Document> documents) {
        vectorStore.add(documents);
        log.info("已写入 {} 条文档向量", documents.size());
    }
}
