package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent RAG文档元数据表实体
 */
@Data
@TableName("t_agent_knowledge_doc")
public class AgentKnowledgeDoc {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String docType;
    private String bizType;
    private Long bizId;
    private String title;
    private String sourceUri;
    private String pineconeNamespace;
    private String syncStatus;
    private Integer version;
    private String contentHash;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
