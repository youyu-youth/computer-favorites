package com.yyyouth.service.aichat.rag;

import com.yyyouth.model.pojo.agent.AgentKnowledgeDoc;
import com.yyyouth.model.pojo.agent.AgentKnowledgeChunk;
import com.yyyouth.service.aichat.agent.rag.AgentTokenTextSplitter;
import com.yyyouth.service.aichat.agent.rag.RagHandler;
import com.yyyouth.service.mapper.AgentKnowledgeDocMapper;
import com.yyyouth.service.mapper.AgentKnowledgeChunkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 知识库管理服务：文档切片到 Pinecone 向量同步
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentKnowledgeService {

    private final AgentKnowledgeDocMapper knowledgeDocMapper;
    private final AgentKnowledgeChunkMapper knowledgeChunkMapper;
    private final AgentTokenTextSplitter tokenTextSplitter;
    private final RagHandler ragHandler;

    public void syncDocument(Long docId) {
        AgentKnowledgeDoc doc = knowledgeDocMapper.selectById(docId);
        if (doc == null) {
            log.warn("知识文档不存在: docId={}", docId);
            return;
        }

        String content = "文档内容加载（后续接入文件读取）";
        List<Document> chunks = tokenTextSplitter.apply(List.of(new Document(content)));
        log.info("文档切片完成: docId={}, chunks={}", docId, chunks.size());

        List<Document> pineconeDocs = chunks.stream().map(chunk -> {
            chunk.getMetadata().put("namespace", doc.getPineconeNamespace());
            chunk.getMetadata().put("docId", doc.getId().toString());
            return chunk;
        }).collect(Collectors.toList());

        ragHandler.storeDocuments(pineconeDocs);

        for (int i = 0; i < chunks.size(); i++) {
            AgentKnowledgeChunk chunkRecord = new AgentKnowledgeChunk();
            chunkRecord.setDocId(docId);
            chunkRecord.setChunkNo(i);
            chunkRecord.setContentHash(String.valueOf(chunks.get(i).hashCode()));
            chunkRecord.setPineconeVectorId(doc.getPineconeNamespace() + "_" + docId + "_" + i);
            chunkRecord.setSyncStatus("synced");
            knowledgeChunkMapper.insert(chunkRecord);
        }

        doc.setSyncStatus("synced");
        knowledgeDocMapper.updateById(doc);
        log.info("知识文档同步完成: docId={}", docId);
    }
}
