# AI Agent 智能助手 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 基于 Spring AI 混合栈构建 AI Agent 智能助手框架，Phase 1 覆盖用户网站投稿 + 管理员网站审核两个核心场景。

**Architecture:** 数据层（13 张新表 POJO/Mapper）→ 核心服务（Tool/Skill/Session/Quota）→ Agent 引擎（SimpleStreamPipe + ReActOrchestrationPipe SSE 流式）→ Controller 暴露 SSE 端点。

**Tech Stack:** Spring AI Alibaba 1.1.2.0 + 上游 Spring AI 组件、DashScope ChatModel、Pinecone VectorStore、MyBatis-Plus、Sa-Token、SSE (SseEmitter)

---

## File Map

| 层 | 新增文件 | 修改文件 |
|----|---------|---------|
| cf-model | 15+ POJOs, 5 DTOs, 5 VOs, 7 Enums | 无 |
| cf-service | 10+ Mappers, 10+ Services, 2 Tools, ChatOrchestrator | BaseAgent, ReActAgent, ToolCallAgent, ToolRegistry, AgentConfig, ToolsConfig, UserAiChatServiceImpl, RagHandler |
| cf-web | 2 Controllers, 1 PlanController | 无 |

---

## Phase 1: 数据基础层（枚举 + POJO + DTO/VO + Mapper）

### Task 1.1: 创建 AI Agent 枚举类

**Files:**
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/enums/ai/TaskStatus.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/enums/ai/PlanStatus.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/enums/ai/OwnerType.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/enums/ai/ToolType.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/enums/ai/RiskLevel.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/enums/ai/AgentHookType.java`

- [ ] **Step 1: 创建所有枚举文件并提交**

```java
// TaskStatus.java
package com.yyyouth.model.enums.ai;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent业务任务状态枚举
 */
public enum TaskStatus {
    PLANNING,
    WAIT_CONFIRM,
    EXECUTING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}
```

```java
// PlanStatus.java
package com.yyyouth.model.enums.ai;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 执行计划状态枚举
 */
public enum PlanStatus {
    WAIT_CONFIRM,
    APPROVED,
    REJECTED,
    EXECUTED,
    EXPIRED
}
```

```java
// OwnerType.java
package com.yyyouth.model.enums.ai;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 主体类型枚举
 */
public enum OwnerType {
    USER,
    ADMIN
}
```

```java
// ToolType.java
package com.yyyouth.model.enums.ai;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 工具类型枚举
 */
public enum ToolType {
    LOCAL,
    MCP
}
```

```java
// RiskLevel.java
package com.yyyouth.model.enums.ai;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 工具风险等级枚举
 */
public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}
```

```java
// AgentHookType.java
package com.yyyouth.model.enums.ai;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent钩子事件类型枚举
 */
public enum AgentHookType {
    PRE_THINK,
    POST_THINK,
    PRE_TOOL,
    POST_TOOL,
    ON_ERROR,
    ON_COMPLETE
}
```

- [ ] **Step 2: 提交**

```bash
git add computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/enums/ai/
git commit -m "feat: add Agent enums (TaskStatus, PlanStatus, OwnerType, ToolType, RiskLevel, AgentHookType)"
```

---

### Task 1.2: 创建 Agent 模块 POJO 实体

**Files:**
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentSession.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentTask.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentActionPlan.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentToolDef.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentToolCall.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentSkill.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentMcpServer.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentHookEvent.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentQuotaPolicy.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentUsageDaily.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentMemorySummary.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentKnowledgeDoc.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentKnowledgeChunk.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentBusinessBinding.java`

- [ ] **Step 1: 创建 AgentSession.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent会话主表实体
 */
@Data
@TableName("t_agent_session")
public class AgentSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String conversationId;
    private Long aiConversationId;
    private String ownerType;
    private Long ownerId;
    private String agentCode;
    private String skillCode;
    private String title;
    private String status;
    private String modelProvider;
    private String modelName;
    private LocalDateTime lastMessageAt;
    private String metadataJson;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: 创建 AgentTask.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent业务任务表实体
 */
@Data
@TableName("t_agent_task")
public class AgentTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String ownerType;
    private Long ownerId;
    private String taskType;
    private String targetType;
    private Long targetId;
    private String status;
    private String intentJson;
    private String resultJson;
    private String errorMsg;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 3: 创建 AgentActionPlan.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent执行计划与确认表实体
 */
@Data
@TableName("t_agent_action_plan")
public class AgentActionPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String planType;
    private String summary;
    private String riskLevel;
    private String actionPayloadJson;
    private String status;
    private String confirmUserType;
    private Long confirmUserId;
    private LocalDateTime confirmedAt;
    private LocalDateTime expireTime;
    private String rejectReason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 4: 创建 AgentToolDef.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent工具定义表实体
 */
@Data
@TableName("t_agent_tool_def")
public class AgentToolDef {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String toolCode;
    private String toolName;
    private String toolType;
    private String audience;
    private String riskLevel;
    private String permissionCode;
    private String serviceMethod;
    private String schemaJson;
    private Integer enabled;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 5: 创建 AgentToolCall.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent工具调用流水表实体
 */
@Data
@TableName("t_agent_tool_call")
public class AgentToolCall {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long planId;
    private String toolCode;
    private String toolType;
    private String requestJson;
    private String responseJson;
    private String status;
    private Integer durationMs;
    private String errorMsg;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 6: 创建 AgentSkill.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent技能模块表实体
 */
@Data
@TableName("t_agent_skill")
public class AgentSkill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String skillCode;
    private String name;
    private String audience;
    private String systemPrompt;
    private String toolAllowlistJson;
    private String mcpAllowlistJson;
    private Integer enabled;
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 7: 创建 AgentMcpServer.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent MCP服务配置表实体
 */
@Data
@TableName("t_agent_mcp_server")
public class AgentMcpServer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String serverCode;
    private String name;
    private String transport;
    private String baseUrl;
    private String endpoint;
    private String authType;
    private String secretRef;
    private Integer enabled;
    private Integer timeoutMs;
    private String metadataJson;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 8: 创建 AgentHookEvent.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent钩子事件表实体
 */
@Data
@TableName("t_agent_hook_event")
public class AgentHookEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String hookType;
    private String handlerCode;
    private String eventPayloadJson;
    private String status;
    private String errorMsg;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 9: 创建 AgentQuotaPolicy.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent配额策略表实体
 */
@Data
@TableName("t_agent_quota_policy")
public class AgentQuotaPolicy {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String subjectType;
    private String subjectCode;
    private Integer dailyMessageLimit;
    private Integer dailyTokenLimit;
    private BigDecimal dailyCostLimit;
    private Integer enabled;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 10: 创建 AgentUsageDaily.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent每日用量表实体
 */
@Data
@TableName("t_agent_usage_daily")
public class AgentUsageDaily {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ownerType;
    private Long ownerId;
    private LocalDate usageDate;
    private Integer messageCount;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal estimatedCost;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 11: 创建 AgentMemorySummary.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent长期记忆摘要表实体
 */
@Data
@TableName("t_agent_memory_summary")
public class AgentMemorySummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String summaryType;
    private String summaryText;
    private Long sourceMessageFrom;
    private Long sourceMessageTo;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 12: 创建 AgentKnowledgeDoc.java**

```java
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
```

- [ ] **Step 13: 创建 AgentKnowledgeChunk.java**

```java
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
```

- [ ] **Step 14: 创建 AgentBusinessBinding.java**

```java
package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent业务绑定表实体
 */
@Data
@TableName("t_agent_business_binding")
public class AgentBusinessBinding {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String bizType;
    private Long bizId;
    private String relationType;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 15: 提交**

```bash
git add computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/pojo/agent/
git commit -m "feat: add Agent POJO entities (14 tables)"
```

---

### Task 1.3: 创建 DTO 和 VO 数据对象

**Files:**
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/dto/agent/AgentChatRequest.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/dto/agent/AgentPlanConfirmRequest.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/vo/agent/AgentChatVO.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/vo/agent/AgentSessionVO.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/vo/agent/AgentPlanVO.java`
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/vo/agent/AgentQuotaVO.java`

- [ ] **Step 1: 创建 DTOs**

```java
// AgentChatRequest.java
package com.yyyouth.model.dto.agent;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent 对话请求
 */
@Data
public class AgentChatRequest {
    private String conversationId;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    @NotBlank(message = "技能编码不能为空")
    private String skillCode;
}
```

```java
// AgentPlanConfirmRequest.java
package com.yyyouth.model.dto.agent;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 执行计划确认请求
 */
@Data
public class AgentPlanConfirmRequest {
    @NotNull(message = "计划ID不能为空")
    private Long planId;
}
```

- [ ] **Step 2: 创建 VOs**

```java
// AgentChatVO.java
package com.yyyouth.model.vo.agent;

import lombok.Builder;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * SSE 连接元信息
 */
@Data
@Builder
public class AgentChatVO {
    private String conversationId;
    private Long sessionId;
    private String skillName;
}
```

```java
// AgentSessionVO.java
package com.yyyouth.model.vo.agent;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 会话列表项
 */
@Data
@Builder
public class AgentSessionVO {
    private Long id;
    private String conversationId;
    private String title;
    private String skillCode;
    private String status;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createTime;
}
```

```java
// AgentPlanVO.java
package com.yyyouth.model.vo.agent;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 执行计划展示
 */
@Data
@Builder
public class AgentPlanVO {
    private Long planId;
    private Long taskId;
    private String summary;
    private String riskLevel;
    private String status;
    private LocalDateTime expireTime;
}
```

```java
// AgentQuotaVO.java
package com.yyyouth.model.vo.agent;

import lombok.Builder;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 当日剩余配额
 */
@Data
@Builder
public class AgentQuotaVO {
    private Integer dailyMessageLimit;
    private Integer usedMessageCount;
    private Integer remainingMessages;
    private Integer dailyTokenLimit;
    private Integer usedTokens;
}
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/dto/agent/ computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/vo/agent/
git commit -m "feat: add Agent DTOs and VOs"
```

---

### Task 1.4: 创建 MyBatis-Plus Mapper 接口

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentSessionMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentTaskMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentActionPlanMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentToolDefMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentToolCallMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentSkillMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentMcpServerMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentHookEventMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentQuotaPolicyMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentUsageDailyMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentMemorySummaryMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentKnowledgeDocMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentKnowledgeChunkMapper.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/AgentBusinessBindingMapper.java`

- [ ] **Step 1: 创建 14 个 Mapper 接口并提交**

每个 Mapper 接口模式相同：

```java
// AgentSessionMapper.java (示例，其余 13 个同理)
package com.yyyouth.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.agent.AgentSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent会话 Mapper
 */
@Mapper
public interface AgentSessionMapper extends BaseMapper<AgentSession> {
}
```

其余 Mapper 接口：
- `AgentTaskMapper extends BaseMapper<AgentTask>`
- `AgentActionPlanMapper extends BaseMapper<AgentActionPlan>`
- `AgentToolDefMapper extends BaseMapper<AgentToolDef>`
- `AgentToolCallMapper extends BaseMapper<AgentToolCall>`
- `AgentSkillMapper extends BaseMapper<AgentSkill>`
- `AgentMcpServerMapper extends BaseMapper<AgentMcpServer>`
- `AgentHookEventMapper extends BaseMapper<AgentHookEvent>`
- `AgentQuotaPolicyMapper extends BaseMapper<AgentQuotaPolicy>`
- `AgentUsageDailyMapper extends BaseMapper<AgentUsageDaily>`
- `AgentMemorySummaryMapper extends BaseMapper<AgentMemorySummary>`
- `AgentKnowledgeDocMapper extends BaseMapper<AgentKnowledgeDoc>`
- `AgentKnowledgeChunkMapper extends BaseMapper<AgentKnowledgeChunk>`
- `AgentBusinessBindingMapper extends BaseMapper<AgentBusinessBinding>`

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/mapper/Agent*.java
git commit -m "feat: add Agent Mapper interfaces (14 mappers)"
```

---

## Phase 2: 工具系统

### Task 2.1: 创建 RuntimeToolDef 内部类并重构 ToolRegistry

**Files:**
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/agent/tools/ToolRegistry.java`

- [ ] **Step 1: 重写 ToolRegistry**

```java
package com.yyyouth.service.aichat.agent.tools;

import com.yyyouth.model.enums.ai.RiskLevel;
import com.yyyouth.model.enums.ai.ToolType;
import com.yyyouth.model.pojo.agent.AgentToolDef;
import com.yyyouth.service.mapper.AgentToolDefMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent 工具注册表：内存中维护工具定义、@Tool 方法引用、ToolCallback
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final AgentToolDefMapper agentToolDefMapper;

    private final Map<String, ToolCallback> allToolCallbacks;

    private final Map<String, RuntimeToolDef> runtimeTools = new ConcurrentHashMap<>();

    /**
     * 运行时工具定义：DB 元数据 + Method 引用
     */
    @Data
    public static class RuntimeToolDef {
        private AgentToolDef meta;
        private ToolCallback toolCallback;
        private RiskLevel riskLevel;
        private ToolType toolType;
        private List<String> requiredPermissionCodes;
    }

    @PostConstruct
    public void init() {
        List<AgentToolDef> dbTools = agentToolDefMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentToolDef>()
                        .eq(AgentToolDef::getEnabled, 1));

        for (AgentToolDef dbTool : dbTools) {
            ToolCallback callback = allToolCallbacks.get(dbTool.getToolCode());
            if (callback == null) {
                log.warn("工具 {} 在 DB 中定义但未找到对应的 @Tool 方法", dbTool.getToolCode());
                continue;
            }
            RuntimeToolDef rt = new RuntimeToolDef();
            rt.setMeta(dbTool);
            rt.setToolCallback(callback);
            rt.setRiskLevel(RiskLevel.valueOf(dbTool.getRiskLevel().toUpperCase()));
            rt.setToolType(ToolType.valueOf(dbTool.getToolType().toUpperCase()));
            rt.setRequiredPermissionCodes(
                    dbTool.getPermissionCode() != null
                            ? Collections.singletonList(dbTool.getPermissionCode())
                            : Collections.emptyList());
            runtimeTools.put(dbTool.getToolCode(), rt);
        }
        log.info("ToolRegistry 初始化完成，已注册 {} 个工具", runtimeTools.size());
    }

    public RuntimeToolDef get(String toolCode) {
        return runtimeTools.get(toolCode);
    }

    public Collection<RuntimeToolDef> getAll() {
        return runtimeTools.values();
    }

    /**
     * 根据白名单过滤出允许使用的 ToolCallback 数组
     */
    public ToolCallback[] getFilteredCallbacks(List<String> allowlist) {
        if (allowlist == null || allowlist.isEmpty()) {
            return new ToolCallback[0];
        }
        return allowlist.stream()
                .map(runtimeTools::get)
                .filter(Objects::nonNull)
                .map(RuntimeToolDef::getToolCallback)
                .toArray(ToolCallback[]::new);
    }

    /**
     * 获取工具的风险等级
     */
    public RiskLevel getRiskLevel(String toolCode) {
        RuntimeToolDef rt = runtimeTools.get(toolCode);
        return rt != null ? rt.getRiskLevel() : RiskLevel.LOW;
    }

    public void refresh() {
        runtimeTools.clear();
        init();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/agent/tools/ToolRegistry.java
git commit -m "refactor: rewrite ToolRegistry with DB-driven metadata and RuntimeToolDef"
```

---

### Task 2.2: 创建业务工具 @Tool Bean（用户端 + 管理端）

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/UserWebsiteTool.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/AdminWebsiteTool.java`
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/agent/tools/WebsitesTool.java` → 删除
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/ToolCallbackConfig.java`

- [ ] **Step 1: 创建 UserWebsiteTool**

```java
package com.yyyouth.service.aichat.tool;

import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.model.dto.user.UserWebsiteSubmissionCreateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 用户端网站投稿工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserWebsiteTool {

    /**
     * 创建网站投稿草稿
     */
    @Tool(description = "创建网站投稿草稿。用户提供网站信息后生成草稿，待用户确认后正式提交审核")
    public String submitWebsiteDraft(
            @ToolParam(description = "网站名称") String name,
            @ToolParam(description = "网站URL地址") String url,
            @ToolParam(description = "网站简短简介（一句话）") String summary,
            @ToolParam(description = "网站详细描述") String description,
            @ToolParam(description = "分类ID") Long categoryId,
            @ToolParam(description = "标签，逗号分隔") String tags) {

        log.info("UserWebsiteTool.submitWebsiteDraft: name={}, url={}, categoryId={}", name, url, categoryId);

        return String.format(
                "网站投稿草稿已生成：\n名称：%s\nURL：%s\n简介：%s\n分类ID：%d\n标签：%s\n请确认是否提交审核。",
                name, url, summary, categoryId, tags);
    }
}
```

- [ ] **Step 2: 创建 AdminWebsiteTool**

```java
package com.yyyouth.service.aichat.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 管理端网站审核工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminWebsiteTool {

    /**
     * 审核单个网站
     */
    @Tool(description = "审核单个网站投稿，审核结果为1通过2拒绝")
    public String auditWebsite(
            @ToolParam(description = "网站ID") Long websiteId,
            @ToolParam(description = "审核结果：1表示通过，2表示拒绝") Integer auditStatus,
            @ToolParam(description = "审核备注/理由") String remark) {

        log.info("AdminWebsiteTool.auditWebsite: websiteId={}, auditStatus={}, remark={}", websiteId, auditStatus, remark);

        String statusText = auditStatus == 1 ? "通过" : "拒绝";
        return String.format("网站审核结果：ID=%d，结果=%s，备注=%s。请确认执行此审核操作。",
                websiteId, statusText, remark);
    }

    /**
     * 批量审核网站
     */
    @Tool(description = "批量审核多个网站投稿")
    public String batchAuditWebsite(
            @ToolParam(description = "批量审核数据，JSON数组，每项包含websiteId和auditStatus") String batchJson) {

        log.info("AdminWebsiteTool.batchAuditWebsite: batchJson={}", batchJson);

        return String.format("批量审核数据已解析：%s。请确认是否批量执行审核。", batchJson);
    }
}
```

- [ ] **Step 3: 创建 ToolCallbackConfig（收集所有 @Tool Bean 为 Map）**

```java
package com.yyyouth.service.aichat.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 收集所有 @Tool 注解 Bean，构建 toolName -> ToolCallback 映射
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ToolCallbackConfig {

    private final UserWebsiteTool userWebsiteTool;
    private final AdminWebsiteTool adminWebsiteTool;

    @Bean
    public Map<String, ToolCallback> allToolCallbacks() {
        ToolCallback[] callbacks = ToolCallbacks.from(userWebsiteTool, adminWebsiteTool);
        return Stream.of(callbacks)
                .collect(Collectors.toMap(
                        ToolCallback::getToolDefinition,
                        Function.identity(),
                        (a, b) -> a));
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/
git rm computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/agent/tools/WebsitesTool.java
git commit -m "feat: add UserWebsiteTool and AdminWebsiteTool with ToolCallbackConfig"
```

---

## Phase 3: 核心服务层

### Task 3.1: 创建 SkillLoader 技能加载器

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/skill/SkillLoader.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/skill/SkillDefinition.java`

- [ ] **Step 1: 创建 SkillDefinition 内部模型**

```java
package com.yyyouth.service.aichat.skill;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 技能运行时定义
 */
@Data
@Builder
public class SkillDefinition {
    private String skillCode;
    private String name;
    private String audience;
    private String systemPrompt;
    private List<String> toolAllowlist;
    private List<String> mcpAllowlist;
}
```

- [ ] **Step 2: 创建 SkillLoader**

```java
package com.yyyouth.service.aichat.skill;

import com.alibaba.fastjson2.JSON;
import com.yyyouth.model.pojo.agent.AgentSkill;
import com.yyyouth.service.mapper.AgentSkillMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 技能加载器：从 DB 加载技能定义，维护内存缓存，支持定时刷新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillLoader {

    private final AgentSkillMapper agentSkillMapper;

    private final Map<String, SkillDefinition> skillCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedRate = 300_000)
    public void refresh() {
        List<AgentSkill> skills = agentSkillMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentSkill>()
                        .eq(AgentSkill::getEnabled, 1));

        skillCache.clear();
        for (AgentSkill skill : skills) {
            List<String> toolAllowlist = JSON.parseArray(skill.getToolAllowlistJson(), String.class);
            List<String> mcpAllowlist = JSON.parseArray(skill.getMcpAllowlistJson(), String.class);

            SkillDefinition def = SkillDefinition.builder()
                    .skillCode(skill.getSkillCode())
                    .name(skill.getName())
                    .audience(skill.getAudience())
                    .systemPrompt(skill.getSystemPrompt())
                    .toolAllowlist(toolAllowlist != null ? toolAllowlist : List.of())
                    .mcpAllowlist(mcpAllowlist != null ? mcpAllowlist : List.of())
                    .build();
            skillCache.put(skill.getSkillCode(), def);
        }
        log.info("SkillLoader 刷新完成，加载 {} 个技能", skillCache.size());
    }

    public SkillDefinition get(String skillCode) {
        return skillCache.get(skillCode);
    }

    /**
     * 获取指定端可用的技能列表
     */
    public List<SkillDefinition> getByAudience(String ownerType) {
        return skillCache.values().stream()
                .filter(s -> s.getAudience().equals(ownerType) || s.getAudience().equals("both"))
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/skill/
git commit -m "feat: add SkillLoader with scheduled refresh from DB"
```

---

### Task 3.2: 创建 SessionManager 会话管理服务

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/session/AgentSessionService.java`

- [ ] **Step 1: 创建 AgentSessionService**

```java
package com.yyyouth.service.aichat.session;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.model.enums.ai.OwnerType;
import com.yyyouth.model.pojo.agent.AgentSession;
import com.yyyouth.service.mapper.AgentSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent 会话管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentSessionService {

    private final AgentSessionMapper agentSessionMapper;

    /**
     * 获取或创建会话
     */
    public AgentSession getOrCreate(String conversationId, String ownerType, Long ownerId,
                                     String agentCode, String skillCode, String modelProvider, String modelName) {
        if (conversationId != null && !conversationId.isBlank()) {
            AgentSession existing = agentSessionMapper.selectOne(
                    new LambdaQueryWrapper<AgentSession>()
                            .eq(AgentSession::getConversationId, conversationId));
            if (existing != null) {
                existing.setLastMessageAt(LocalDateTime.now());
                agentSessionMapper.updateById(existing);
                return existing;
            }
        }

        AgentSession session = new AgentSession();
        session.setConversationId(UUID.fastUUID().toString());
        session.setOwnerType(ownerType);
        session.setOwnerId(ownerId);
        session.setAgentCode(agentCode);
        session.setSkillCode(skillCode);
        session.setStatus("active");
        session.setModelProvider(modelProvider);
        session.setModelName(modelName);
        session.setLastMessageAt(LocalDateTime.now());
        agentSessionMapper.insert(session);

        log.info("创建 Agent 会话: conversationId={}, ownerType={}, ownerId={}",
                session.getConversationId(), ownerType, ownerId);
        return session;
    }

    /**
     * 归档会话
     */
    public void archive(Long sessionId) {
        AgentSession session = agentSessionMapper.selectById(sessionId);
        if (session != null) {
            session.setStatus("archived");
            agentSessionMapper.updateById(session);
        }
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/session/
git commit -m "feat: add AgentSessionService for session lifecycle"
```

---

### Task 3.3: 创建 QuotaGuard 配额守卫 + UsageTracker 用量追踪

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/quota/QuotaGuard.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/quota/UsageTracker.java`

- [ ] **Step 1: 创建 QuotaGuard**

```java
package com.yyyouth.service.aichat.quota;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.model.pojo.agent.AgentQuotaPolicy;
import com.yyyouth.model.pojo.agent.AgentUsageDaily;
import com.yyyouth.model.vo.agent.AgentQuotaVO;
import com.yyyouth.service.mapper.AgentQuotaPolicyMapper;
import com.yyyouth.service.mapper.AgentUsageDailyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 配额守卫：检查用户/管理员当日使用量是否超限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuotaGuard {

    private final AgentQuotaPolicyMapper quotaPolicyMapper;
    private final AgentUsageDailyMapper usageDailyMapper;

    /**
     * 检查配额，返回剩余配额信息。如果超限返回 null
     */
    public AgentQuotaVO checkAndGetQuota(String ownerType, Long ownerId) {
        String defaultSubjectType = "admin".equals(ownerType) ? "default_admin" : "default_user";

        AgentQuotaPolicy policy = quotaPolicyMapper.selectOne(
                new LambdaQueryWrapper<AgentQuotaPolicy>()
                        .eq(AgentQuotaPolicy::getSubjectType, defaultSubjectType)
                        .eq(AgentQuotaPolicy::getEnabled, 1));

        if (policy == null) {
            return AgentQuotaVO.builder()
                    .dailyMessageLimit(Integer.MAX_VALUE)
                    .usedMessageCount(0)
                    .remainingMessages(Integer.MAX_VALUE)
                    .build();
        }

        AgentUsageDaily usage = usageDailyMapper.selectOne(
                new LambdaQueryWrapper<AgentUsageDaily>()
                        .eq(AgentUsageDaily::getOwnerType, ownerType)
                        .eq(AgentUsageDaily::getOwnerId, ownerId)
                        .eq(AgentUsageDaily::getUsageDate, LocalDate.now()));

        int used = usage != null ? usage.getMessageCount() : 0;
        int usedTokens = usage != null
                ? usage.getInputTokens() + usage.getOutputTokens()
                : 0;
        int remaining = policy.getDailyMessageLimit() - used;
        int remainingTokens = policy.getDailyTokenLimit() - usedTokens;

        return AgentQuotaVO.builder()
                .dailyMessageLimit(policy.getDailyMessageLimit())
                .usedMessageCount(used)
                .remainingMessages(Math.max(0, remaining))
                .dailyTokenLimit(policy.getDailyTokenLimit())
                .usedTokens(usedTokens)
                .build();
    }

    public boolean isExceeded(String ownerType, Long ownerId) {
        AgentQuotaVO quota = checkAndGetQuota(ownerType, ownerId);
        return quota.getRemainingMessages() <= 0;
    }
}
```

- [ ] **Step 2: 创建 UsageTracker**

```java
package com.yyyouth.service.aichat.quota;

import com.yyyouth.model.pojo.agent.AgentUsageDaily;
import com.yyyouth.service.mapper.AgentUsageDailyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 用量追踪：对话结束后更新每日用量
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsageTracker {

    private final AgentUsageDailyMapper usageDailyMapper;

    @Transactional
    public void record(String ownerType, Long ownerId, int messageCount, int inputTokens, int outputTokens) {
        LocalDate today = LocalDate.now();
        AgentUsageDaily usage = usageDailyMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentUsageDaily>()
                        .eq(AgentUsageDaily::getOwnerType, ownerType)
                        .eq(AgentUsageDaily::getOwnerId, ownerId)
                        .eq(AgentUsageDaily::getUsageDate, today));

        if (usage == null) {
            usage = new AgentUsageDaily();
            usage.setOwnerType(ownerType);
            usage.setOwnerId(ownerId);
            usage.setUsageDate(today);
            usage.setMessageCount(messageCount);
            usage.setInputTokens(inputTokens);
            usage.setOutputTokens(outputTokens);
            usage.setEstimatedCost(BigDecimal.ZERO);
            usageDailyMapper.insert(usage);
        } else {
            usage.setMessageCount(usage.getMessageCount() + messageCount);
            usage.setInputTokens(usage.getInputTokens() + inputTokens);
            usage.setOutputTokens(usage.getOutputTokens() + outputTokens);
            usageDailyMapper.updateById(usage);
        }
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/quota/
git commit -m "feat: add QuotaGuard and UsageTracker for daily rate limiting"
```

---

### Task 3.4: 创建 MemorySummarizer 长期记忆摘要服务

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/memory/MemorySummarizer.java`

- [ ] **Step 1: 创建 MemorySummarizer**

```java
package com.yyyouth.service.aichat.memory;

import com.yyyouth.model.pojo.agent.AgentMemorySummary;
import com.yyyouth.service.mapper.AgentMemorySummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 长期记忆摘要：对话完成后异步生成摘要存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemorySummarizer {

    private final AgentMemorySummaryMapper memorySummaryMapper;

    @Async
    public void summarizeAsync(Long sessionId, String conversationText, String summaryType) {
        log.info("开始生成记忆摘要: sessionId={}, type={}", sessionId, summaryType);

        String summary = "会话 #" + sessionId + " 的摘要内容（后续接入 LLM 摘要生成）";

        AgentMemorySummary mem = new AgentMemorySummary();
        mem.setSessionId(sessionId);
        mem.setSummaryType(summaryType);
        mem.setSummaryText(summary);
        memorySummaryMapper.insert(mem);

        log.info("记忆摘要生成完成: sessionId={}", sessionId);
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/memory/
git commit -m "feat: add MemorySummarizer for long-term memory"
```

---

### Task 3.5: 创建 HookEventPublisher 钩子事件发布器

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/hook/HookEventPublisher.java`

- [ ] **Step 1: 创建 HookEventPublisher**

```java
package com.yyyouth.service.aichat.hook;

import com.yyyouth.model.pojo.agent.AgentHookEvent;
import com.yyyouth.service.mapper.AgentHookEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent 钩子事件发布器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HookEventPublisher {

    private final AgentHookEventMapper hookEventMapper;

    public void publish(String hookType, Long taskId, String handlerCode, String eventPayloadJson) {
        AgentHookEvent event = new AgentHookEvent();
        event.setTaskId(taskId);
        event.setHookType(hookType);
        event.setHandlerCode(handlerCode);
        event.setEventPayloadJson(eventPayloadJson);
        event.setStatus("succeeded");
        hookEventMapper.insert(event);
        log.debug("钩子事件发布: type={}, taskId={}", hookType, taskId);
    }

    public void publishError(String hookType, Long taskId, String errorMsg) {
        AgentHookEvent event = new AgentHookEvent();
        event.setTaskId(taskId);
        event.setHookType(hookType);
        event.setStatus("failed");
        event.setErrorMsg(errorMsg);
        hookEventMapper.insert(event);
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/hook/
git commit -m "feat: add HookEventPublisher for agent lifecycle events"
```

---

## Phase 4: Agent 引擎改造

### Task 4.1: 改造 BaseAgent 支持 SseEmitter 回调

**Files:**
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/agent/thinking_agent/BaseAgent.java`
- Modify: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/constants/ai/AgentConstants.java`

- [ ] **Step 1: 扩展 AgentConstants**

```java
package com.yyyouth.model.constants.ai;

/**
 * @author yyyouth zg
 * @date 2026-04-26
 *
 * agent 常量类
 */
public interface AgentConstants {
    String AGENT_STEP_RESULT = "思考完成，无需行动";
    String AGENT_STEP_RESULT_ERROR = "执行步骤中出现异常：";

    // --- 新增 ---
    String DEFAULT_AGENT_CODE = "default";
    String DEFAULT_MODEL_PROVIDER = "dashscope";
    String CLOSE_FINAL_ANSWER = "任务处理完成，感谢你的耐心等待。";
}
```

- [ ] **Step 2: 改造 BaseAgent.run()**

```java
package com.yyyouth.service.aichat.agent.thinking_agent;

import cn.hutool.core.util.StrUtil;
import com.yyyouth.model.enums.ai.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-04-26
 *
 * 抽象基础代理类，管理状态转换和步骤循环。
 * 子类实现 step() 方法，通过 pushEvent 推送 SSE 事件。
 */
@Slf4j
@Data
public abstract class BaseAgent {

    private String agentName;
    private String systemPrompt;
    private int currentStep = 0;
    private int maxSteps = 10;
    private String nextStepPrompt;
    private AgentState state = AgentState.IDLE;
    private ChatClient chatClient;
    private List<Message> contextMsgList;
    private SseEmitter sseEmitter;

    /**
     * 向 SSE 连接推送事件
     */
    protected void pushEvent(String eventType, Map<String, Object> data) {
        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event().name(eventType).data(data));
            } catch (IOException e) {
                log.error("SSE 推送失败: type={}", eventType, e);
            }
        }
    }

    public void run(String userPrompt, SseEmitter emitter) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        this.sseEmitter = emitter;
        this.contextMsgList = new ArrayList<>();
        state = AgentState.RUNNING;
        contextMsgList.add(new UserMessage(userPrompt));

        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);

                pushEvent("thinking", Map.of("step", stepNumber));
                String stepResult = step();
                log.info("Step {} result: {}", stepNumber, stepResult);
            }
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
            }
            pushEvent("done", Map.of("state", state.name()));
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            pushEvent("error", Map.of("code", "AGENT_ERROR", "msg", e.getMessage()));
        } finally {
            cleanUp();
        }
    }

    protected abstract String step();
    protected void cleanUp() {}
}
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/agent/thinking_agent/BaseAgent.java
git add computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/constants/ai/AgentConstants.java
git commit -m "refactor: BaseAgent added SseEmitter pushEvent support"
```

---

### Task 4.2: 改造 ToolCallAgent 添加风险检查 + 计划确认

**Files:**
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/agent/thinking_agent/ToolCallAgent.java`

- [ ] **Step 1: 在 action() 方法中添加风险检查和事件推送**

修改 ToolCallAgent，在 `action()` 执行工具前添加：

```java
// 在 action() 方法的工具执行循环中，executionResult 之前插入：

pushEvent("tool_call", Map.of(
    "toolName", toolResponse.name(),
    "args", toolResponse.responseData()
));

// 集成风险等级检查（通过字段注入 riskChecker）：
// if (riskChecker != null && riskChecker.isHighRisk(toolResponse.name())) {
//     pushEvent("plan", Map.of(...));  生成确认计划
//     return "等待用户确认操作";
// }
```

完整 ToolCallAgent 参见 spec 4.3 节 ReActOrchestrationPipe 设计伪代码。

- [ ] **Step 2: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/agent/thinking_agent/ToolCallAgent.java
git commit -m "refactor: ToolCallAgent added risk check + SSE event push"
```

---

### Task 4.3: 创建 ChatOrchestrator 编排引擎 + SimpleStreamPipe

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/chat/ChatOrchestrator.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/chat/AgentStreamSink.java`

- [ ] **Step 1: 创建 AgentStreamSink**

```java
package com.yyyouth.service.aichat.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * SSE 事件推送封装
 */
@RequiredArgsConstructor
public class AgentStreamSink {

    private final SseEmitter emitter;

    public void thinking(int step) {
        send("thinking", Map.of("step", step));
    }

    public void message(String delta) {
        send("message", Map.of("delta", delta));
    }

    public void toolCall(String toolName, String args) {
        send("tool_call", Map.of("toolName", toolName, "args", args));
    }

    public void toolResult(String toolName, String result) {
        send("tool_result", Map.of("toolName", toolName, "result", result));
    }

    public void plan(Long planId, String summary, String risk) {
        send("plan", Map.of("planId", planId, "summary", summary, "risk", risk));
    }

    public void done(Long sessionId) {
        send("done", Map.of("sessionId", sessionId));
    }

    public void error(String code, String msg) {
        send("error", Map.of("code", code, "msg", msg));
    }

    public void quotaExceeded(int limit, int used) {
        send("quota_exceeded", Map.of("limit", limit, "used", used));
    }

    public void heartbeat() {
        send("heartbeat", "");
    }

    private void send(String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            // SSE 连接可能已关闭
        }
    }
}
```

- [ ] **Step 2: 创建 ChatOrchestrator**

```java
package com.yyyouth.service.aichat.chat;

import com.yyyouth.model.pojo.agent.AgentSession;
import com.yyyouth.model.vo.agent.AgentQuotaVO;
import com.yyyouth.service.aichat.quota.QuotaGuard;
import com.yyyouth.service.aichat.session.AgentSessionService;
import com.yyyouth.service.aichat.skill.SkillDefinition;
import com.yyyouth.service.aichat.skill.SkillLoader;
import com.yyyouth.service.aichat.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 聊天编排器：路由决策 + 委托 SimpleStream / ReAct 管道
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOrchestrator {

    private final ChatClient dashscopeChatClient;
    private final SkillLoader skillLoader;
    private final ToolRegistry toolRegistry;
    private final QuotaGuard quotaGuard;
    private final AgentSessionService sessionService;

    public void handle(String ownerType, Long ownerId, String message, String skillCode,
                        String conversationId, SseEmitter emitter) {
        AgentStreamSink sink = new AgentStreamSink(emitter);

        AgentQuotaVO quota = quotaGuard.checkAndGetQuota(ownerType, ownerId);
        if (quota.getRemainingMessages() <= 0) {
            sink.quotaExceeded(quota.getDailyMessageLimit(), quota.getUsedMessageCount());
            emitter.complete();
            return;
        }

        SkillDefinition skill = skillLoader.get(skillCode);
        if (skill == null) {
            sink.error("INVALID_SKILL", "技能编码无效: " + skillCode);
            emitter.complete();
            return;
        }

        AgentSession session = sessionService.getOrCreate(
                conversationId, ownerType, ownerId, "default", skillCode, "dashscope", "qwen-plus");

        ToolCallback[] filteredTools = toolRegistry.getFilteredCallbacks(skill.getToolAllowlist());

        if (filteredTools.length == 0) {
            simpleStream(sink, skill, message, session);
        } else {
            reactiveStream(sink, skill, message, filteredTools, session);
        }
    }

    private void simpleStream(AgentStreamSink sink, SkillDefinition skill, String message, AgentSession session) {
        try {
            Flux<String> flux = dashscopeChatClient.prompt()
                    .system(skill.getSystemPrompt())
                    .user(message)
                    .stream()
                    .content();

            flux.doOnNext(sink::message)
                    .doOnComplete(() -> {
                        sink.done(session.getId());
                    })
                    .doOnError(e -> sink.error("STREAM_ERROR", e.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            sink.error("CHAT_ERROR", e.getMessage());
        }
    }

    private void reactiveStream(AgentStreamSink sink, SkillDefinition skill, String message,
                                 ToolCallback[] tools, AgentSession session) {
        try {
            Flux<String> flux = dashscopeChatClient.prompt()
                    .system(skill.getSystemPrompt())
                    .user(message)
                    .tools(tools)
                    .stream()
                    .content();

            flux.doOnNext(sink::message)
                    .doOnComplete(() -> {
                        sink.done(session.getId());
                    })
                    .doOnError(e -> sink.error("STREAM_ERROR", e.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            sink.error("CHAT_ERROR", e.getMessage());
        }
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/chat/
git commit -m "feat: add ChatOrchestrator with SimpleStream and ReactiveStream pipes"
```

---

## Phase 5: RAG + Pinecone 实现

### Task 5.1: 填充 RagHandler + AgentKnowledgeService

**Files:**
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/agent/rag/RagHandler.java`
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/rag/AgentKnowledgeService.java`

- [ ] **Step 1: 填充 RagHandler**

```java
package com.yyyouth.service.aichat.agent.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
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

    /**
     * 按命名空间检索相关文档
     */
    public List<Document> search(String query, String namespace, int topK, double similarityThreshold) {
        return vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.builder()
                        .query(query)
                        .similarityThreshold(similarityThreshold)
                        .topK(topK)
                        .filterExpression("namespace == '" + namespace + "'")
                        .build());
    }

    /**
     * 写入文档向量
     */
    public void storeDocuments(List<Document> documents) {
        vectorStore.add(documents);
        log.info("已写入 {} 条文档向量", documents.size());
    }
}
```

- [ ] **Step 2: 创建 AgentKnowledgeService**

```java
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
 * 知识库管理服务：文档切片 → Pinecone 向量同步
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
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/agent/rag/RagHandler.java
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/rag/
git commit -m "feat: fill RagHandler + add AgentKnowledgeService for Pinecone sync"
```

---

## Phase 6: Controller 层 SSE 端点

### Task 6.1: 创建用户端 + 管理端 + 计划确认 Controller

**Files:**
- Create: `computer-favorites-back/cf-web/src/main/java/com/yyyouth/web/controller/agent/UserAgentController.java`
- Create: `computer-favorites-back/cf-web/src/main/java/com/yyyouth/web/controller/agent/AdminAgentController.java`
- Create: `computer-favorites-back/cf-web/src/main/java/com/yyyouth/web/controller/agent/AgentPlanController.java`

- [ ] **Step 1: 创建 UserAgentController**

```java
package com.yyyouth.web.controller.agent;

import cn.dev33.satoken.stp.StpUtil;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.agent.AgentChatRequest;
import com.yyyouth.service.aichat.chat.ChatOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 用户端 AI Agent 对话接口
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/user")
@RequiredArgsConstructor
public class UserAgentController {

    private final ChatOrchestrator chatOrchestrator;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody @Valid AgentChatRequest request) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();

        log.info("用户端 Agent 对话: userId={}, skillCode={}, message={}",
                userId, request.getSkillCode(), request.getMessage());

        SseEmitter emitter = new SseEmitter(300_000L);

        chatOrchestrator.handle("user", userId, request.getMessage(),
                request.getSkillCode(), request.getConversationId(), emitter);

        return emitter;
    }
}
```

- [ ] **Step 2: 创建 AdminAgentController**

```java
package com.yyyouth.web.controller.agent;

import cn.dev33.satoken.stp.StpAdminUtil;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.agent.AgentChatRequest;
import com.yyyouth.service.aichat.chat.ChatOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 管理端 AI Agent 对话接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/agent")
@RequiredArgsConstructor
public class AdminAgentController {

    private final ChatOrchestrator chatOrchestrator;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody @Valid AgentChatRequest request) {
        StpAdminUtil.checkLogin();
        Long adminId = StpAdminUtil.getLoginIdAsLong();

        log.info("管理端 Agent 对话: adminId={}, skillCode={}, message={}",
                adminId, request.getSkillCode(), request.getMessage());

        SseEmitter emitter = new SseEmitter(300_000L);

        chatOrchestrator.handle("admin", adminId, request.getMessage(),
                request.getSkillCode(), request.getConversationId(), emitter);

        return emitter;
    }
}
```

- [ ] **Step 3: 创建 AgentPlanController**

```java
package com.yyyouth.web.controller.agent;

import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.agent.AgentPlanConfirmRequest;
import com.yyyouth.model.vo.agent.AgentPlanVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 执行计划确认/拒绝接口
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentPlanController {

    @PostMapping("/plan/confirm")
    public HttpResult<AgentPlanVO> confirmPlan(@RequestBody @Valid AgentPlanConfirmRequest request) {
        log.info("确认执行计划: planId={}", request.getPlanId());
        AgentPlanVO vo = AgentPlanVO.builder()
                .planId(request.getPlanId())
                .status("executed")
                .build();
        return HttpResult.success(vo);
    }

    @PostMapping("/plan/reject")
    public HttpResult<AgentPlanVO> rejectPlan(@RequestBody @Valid AgentPlanConfirmRequest request) {
        log.info("拒绝执行计划: planId={}", request.getPlanId());
        AgentPlanVO vo = AgentPlanVO.builder()
                .planId(request.getPlanId())
                .status("rejected")
                .build();
        return HttpResult.success(vo);
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add computer-favorites-back/cf-web/src/main/java/com/yyyouth/web/controller/agent/
git commit -m "feat: add SSE chat endpoints (user + admin) + plan confirm controller"
```

---

## Phase 7: 配置整合 + 清理

### Task 7.1: 精简 AgentConfig + 更新 UserAiChatServiceImpl

**Files:**
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/config/AgentConfig.java`
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/user/impl/UserAiChatServiceImpl.java`
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/config/ToolsConfig.java`

- [ ] **Step 1: 精简 AgentConfig 为单一 bean**

```java
package com.yyyouth.service.aichat.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent ChatClient 配置（精简版）
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AgentConfig {

    private final DashScopeChatModel dashscopeChatModel;
    private final ChatMemory jdbcChatMemory;

    @Bean
    public ChatClient dashscopeChatClient() {
        log.info("创建 Agent DashScope ChatClient");
        return ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(jdbcChatMemory).build())
                .build();
    }
}
```

- [ ] **Step 2: 更新 UserAiChatServiceImpl 委托给 ChatOrchestrator**

```java
package com.yyyouth.service.aichat.user.impl;

import com.yyyouth.service.aichat.chat.ChatOrchestrator;
import com.yyyouth.service.aichat.user.UserAiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAiChatServiceImpl implements UserAiChatService {

    private final ChatOrchestrator chatOrchestrator;

    @Override
    public String chatTest(String userPrompt) {
        log.info("测试对话: {}", userPrompt);
        return "Agent 框架已就绪，请使用 SSE 端点 /api/agent/user/chat 进行对话";
    }
}
```

- [ ] **Step 3: 删除 ToolsConfig 中无效引用并清理**

删除 `ToolsConfig.java` 文件（已被 `ToolCallbackConfig` 替代）。

```bash
git rm computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/config/ToolsConfig.java
```

或者如果后续还需要注册更多工具，保留但更新：
```java
// 保留为空或删除，当前由 ToolCallbackConfig 负责
```

- [ ] **Step 4: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/config/AgentConfig.java
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/user/impl/UserAiChatServiceImpl.java
git rm computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/config/ToolsConfig.java
git commit -m "refactor: simplify AgentConfig, update UserAiChatServiceImpl, remove ToolsConfig"
```

---

### Task 7.2: 最终验证 — 编译 + 测试

- [ ] **Step 1: 编译所有模块**

```bash
cd computer-favorites-back && mvn clean compile -pl cf-model,cf-service,cf-web
```
Expected: BUILD SUCCESS

- [ ] **Step 2: 运行现有测试确保无回归**

```bash
cd computer-favorites-back && mvn -pl cf-service test
```
Expected: 现有测试全部 PASS

```bash
cd computer-favorites-back && mvn -pl cf-web test
```
Expected: 现有测试全部 PASS

- [ ] **Step 3: 提交最终状态**

```bash
git status
git add -A
git commit -m "chore: final verification, all modules compile and tests pass"
```

---

## 实施顺序总结

```
Phase 1 (数据层) → Phase 2 (工具) → Phase 3 (核心服务)
    → Phase 4 (引擎) → Phase 5 (RAG) → Phase 6 (Controller)
    → Phase 7 (配置)
```

Phase 1-3 有严格顺序依赖，Phase 5 可与 Phase 3-4 并行。Phase 6 依赖 Phase 3-4 完成。
