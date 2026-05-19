package com.yyyouth.service.aichat.rag;

import com.yyyouth.model.pojo.agent.AgentKnowledgeChunk;
import com.yyyouth.model.pojo.agent.AgentKnowledgeDoc;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.service.aichat.agent.rag.AgentTokenTextSplitter;
import com.yyyouth.service.aichat.agent.rag.RagHandler;
import com.yyyouth.service.mapper.AgentKnowledgeChunkMapper;
import com.yyyouth.service.mapper.AgentKnowledgeDocMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * RAG 知识库管理服务。将平台网站数据向量化写入 Pinecone。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentKnowledgeService {

    private static final String NAMESPACE_WEBSITE = "website";

    private final AgentKnowledgeDocMapper knowledgeDocMapper;
    private final AgentKnowledgeChunkMapper knowledgeChunkMapper;
    private final AgentTokenTextSplitter tokenTextSplitter;
    private final RagHandler ragHandler;
    private final WebsiteMapper websiteMapper;

    /**
     * 同步已收录网站到 Pinecone 向量库
     */
    public int syncWebsites() {
        List<Website> websites = websiteMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Website>()
                        .eq(Website::getDeleted, 0));

        if (websites.isEmpty()) {
            log.info("无网站数据需要同步");
            return 0;
        }

        String contentHash = String.valueOf(websites.hashCode());
        AgentKnowledgeDoc existingDoc = knowledgeDocMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentKnowledgeDoc>()
                        .eq(AgentKnowledgeDoc::getPineconeNamespace, NAMESPACE_WEBSITE)
                        .orderByDesc(AgentKnowledgeDoc::getCreateTime)
                        .last("LIMIT 1"));

        if (existingDoc != null && contentHash.equals(existingDoc.getContentHash())) {
            log.info("网站数据无变化，跳过同步");
            return 0;
        }

        AgentKnowledgeDoc doc = new AgentKnowledgeDoc();
        doc.setDocType("website");
        doc.setBizType("website");
        doc.setTitle("平台网站数据快照");
        doc.setPineconeNamespace(NAMESPACE_WEBSITE);
        doc.setSyncStatus("pending");
        doc.setContentHash(contentHash);
        doc.setVersion(1);
        knowledgeDocMapper.insert(doc);

        int chunkCount = 0;
        int websiteCount = 0;

        for (Website website : websites) {
            String text = buildWebsiteText(website);
            List<Document> chunks = tokenTextSplitter.splitWithBuilder(List.of(new Document(text)));
            log.debug("网站切片: websiteId={}, chunks={}", website.getId(), chunks.size());

            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);
                chunk.getMetadata().put("namespace", NAMESPACE_WEBSITE);
                chunk.getMetadata().put("docId", doc.getId().toString());
                chunk.getMetadata().put("websiteId", website.getId().toString());
                chunk.getMetadata().put("title", website.getName());

                AgentKnowledgeChunk chunkRecord = new AgentKnowledgeChunk();
                chunkRecord.setDocId(doc.getId());
                chunkRecord.setChunkNo(chunkCount);
                chunkRecord.setContentHash(String.valueOf(chunk.hashCode()));
                chunkRecord.setPineconeVectorId(NAMESPACE_WEBSITE + "_" + website.getId() + "_" + i);
                chunkRecord.setSyncStatus("synced");
                knowledgeChunkMapper.insert(chunkRecord);
                chunkCount++;
            }

            List<Document> pineconeDocs = chunks.stream().map(chunk -> {
                chunk.getMetadata().put("namespace", NAMESPACE_WEBSITE);
                chunk.getMetadata().put("docId", doc.getId().toString());
                chunk.getMetadata().put("websiteId", website.getId().toString());
                return chunk;
            }).toList();

            ragHandler.storeDocuments(pineconeDocs);
            websiteCount++;
        }

        doc.setSyncStatus("synced");
        knowledgeDocMapper.updateById(doc);

        log.info("网站数据同步完成: websites={}, chunks={}", websiteCount, chunkCount);
        return websiteCount;
    }

    public List<Document> searchWebsites(String query, int topK) {
        return ragHandler.search(query, NAMESPACE_WEBSITE, topK, 0.6);
    }

    private String buildWebsiteText(Website w) {
        return String.format(
                "网站名称: %s\nURL: %s\n简介: %s\n描述: %s\n标签: %s",
                w.getName(),
                w.getUrl(),
                w.getSummary() != null ? w.getSummary() : "",
                w.getDescription() != null ? w.getDescription() : "",
                w.getTags() != null ? w.getTags() : ""
        );
    }
}
