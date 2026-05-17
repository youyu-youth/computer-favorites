package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent RAG切片元数据表实体
 */
@Data
@TableName("t_agent_knowledge_chunk")
public class AgentKnowledgeChunk {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long docId;
    private Integer chunkNo;
    private String contentHash;
    private String pineconeVectorId;
    private String metadataJson;
    private String syncStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
