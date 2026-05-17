package com.yyyouth.service.aichat.agent.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/15
 * 文档合并工具
 */
@RequiredArgsConstructor
@Component
public class ConcatenationDocumentJoiner {


    private final DocumentJoiner documentJoiner;

    /**
     * 文档合并
     * @param documentsForQuery
     * @return
     */
    public List<Document> join(Map<Query, List<List<Document>>> documentsForQuery) {
        return documentJoiner.join(documentsForQuery);
    }

}
