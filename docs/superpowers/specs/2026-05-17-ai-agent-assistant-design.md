# AI Agent 智能助手 — 架构设计 Spec

> 状态：已确认 | 日期：2026-05-17 | 分支：feat/user-ai-agent-new-cc

## 1. 概述

基于 Spring AI 混合技术栈（Spring AI Alibaba 1.1.x + 上游 Spring AI 组件），在现有 `ai-agent.sql` 数据库设计基础上，构建低耦合、可扩展的 AI Agent 智能助手框架。

### 1.1 核心决策

| 决策项 | 选择 |
|--------|------|
| Spring AI 版本 | Spring AI Alibaba 1.1.2.0 + 上游 Spring AI 组件（保持现有混合栈） |
| 主推理模型 | DashScope（阿里云灵积） |
| 功能范围 Phase 1 | 用户网站投稿 + 管理员网站审核 |
| 日常对话架构 | ChatClient.stream() + Advisor 链 |
| 复杂任务架构 | ReAct Agent 编排（改造现有 BaseAgent → ReActAgent → ToolCallAgent） |
| 工具加载 | 混合模式：@Tool 注解实现 + DB 元数据控制 |
| 旧表处理 | 全面使用 ai-agent.sql 新表，旧表 t_ai_conversation/t_ai_message 不用于 Agent 开发 |

## 2. 整体分层架构

### 2.1 Maven 模块归属

```
cf-model/   — 13 张 Agent 表 POJO + DTO/VO/枚举
cf-service/ — Agent 核心引擎（聊天编排/工具执行/技能/RAG/MCP/记忆/配额）
cf-web/     — SSE Controller（用户端 + 管理端）
```

### 2.2 cf-service 内部目录

```
aichat/
├── session/    — 会话管理（创建/归档/标题生成）
├── chat/       — 聊天编排器（路由决策 + SSE 流式推送）
├── agent/      — ReAct 引擎（保留现有并改造）
├── tool/       — 工具注册/发现/执行/审计
├── skill/      — 技能加载与管理
├── rag/        — RAG 管道（保留现有 + Pinecone 对接）
├── mcp/        — MCP 集成（保留现有）
├── memory/     — 长期记忆摘要
├── quota/      — 配额检查与用量记录
├── hook/       — 钩子事件发布
└── config/     — 配置类整合
```

### 2.3 核心数据流

```
用户发送消息 (SSE POST)
  → SessionManager 加载/创建 t_agent_session
  → QuotaGuard 校验 t_agent_quota_policy 每日限额
  → SkillLoader 加载 t_agent_skill（system_prompt + 工具白名单）
  → ChatOrchestrator 路由决策：
      ├─ 简单闲聊 → ChatClient.stream() + Advisor 链 → SSE 流式推送
      └─ 业务任务 → ReActAgent 编排（think→tool_call→act 循环）
           ├─ 高风险操作 → 生成 t_agent_action_plan → wait_confirm → SSE 推送确认卡
           └─ 低风险操作 → 直接执行 → 记录 t_agent_tool_call
  → MemorySummarizer 异步更新 t_agent_memory_summary
  → UsageTracker 原子更新 t_agent_usage_daily
```

### 2.4 SSE 事件契约

| 事件类型 | 含义 | payload |
|---------|------|---------|
| `thinking` | Agent 开始推理 | `{step: 1}` |
| `message` | 流式文本块 | `{delta: "文本"}` |
| `tool_call` | 工具调用开始 | `{toolName, args}` |
| `tool_result` | 工具执行结果 | `{toolName, result}` |
| `plan` | 需确认的执行计划 | `{planId, summary, risk, steps}` |
| `done` | 对话结束 | `{sessionId, usage}` |
| `error` | 异常 | `{code, msg}` |
| `quota_exceeded` | 配额耗尽 | `{limit, used}` |
| `heartbeat` | 心跳保活 | 空 |

## 3. 数据层设计

### 3.1 新增 POJO

```
cf-model/src/main/java/com/yyyouth/model/pojo/agent/
├── AgentSession.java          — t_agent_session（@TableLogic）
├── AgentTask.java             — t_agent_task
├── AgentActionPlan.java       — t_agent_action_plan
├── AgentToolDef.java          — t_agent_tool_def
├── AgentToolCall.java         — t_agent_tool_call
├── AgentSkill.java            — t_agent_skill
├── AgentMcpServer.java        — t_agent_mcp_server
├── AgentHookEvent.java        — t_agent_hook_event
├── AgentQuotaPolicy.java      — t_agent_quota_policy
├── AgentUsageDaily.java       — t_agent_usage_daily
├── AgentMemorySummary.java    — t_agent_memory_summary
├── AgentKnowledgeDoc.java     — t_agent_knowledge_doc（@TableLogic）
├── AgentKnowledgeChunk.java   — t_agent_knowledge_chunk
└── AgentBusinessBinding.java  — t_agent_business_binding
```

`deleted` 字段的表：Session/Task/ActionPlan/ToolDef/Skill/McpServer/KnowledgeDoc/KnowledgeChunk 使用 `@TableLogic`。

### 3.2 DTO/VO/枚举

```
dto/agent/
├── AgentChatRequest           — 消息请求（conversationId + message）
├── AgentPlanConfirmRequest    — 确认/拒绝计划
└── AgentQuotaQueryDTO         — 配额查询

vo/agent/
├── AgentChatVO                — SSE 连接元信息
├── AgentSessionVO             — 会话列表
├── AgentSessionDetailVO       — 会话详情（含消息历史、任务列表）
├── AgentPlanVO                — 执行计划展示
└── AgentQuotaVO               — 配额剩余

enums/ai/ (扩充)
├── AgentState.java            — 已有
├── TaskStatus.java            — planning/wait_confirm/executing/succeeded/failed/cancelled
├── PlanStatus.java            — wait_confirm/approved/rejected/executed/expired
├── OwnerType.java             — user/admin
├── ToolType.java              — local/mcp
├── RiskLevel.java             — low/medium/high
└── AgentHookType.java         — pre_think/post_think/pre_tool/post_tool/on_error/on_complete
```

### 3.3 Mapper

```
cf-service/src/main/java/com/yyyouth/service/mapper/
├── AgentSessionMapper.java
├── AgentTaskMapper.java
├── AgentActionPlanMapper.java
├── AgentToolDefMapper.java
├── AgentToolCallMapper.java
├── AgentSkillMapper.java
├── AgentMcpServerMapper.java
├── AgentHookEventMapper.java
├── AgentQuotaPolicyMapper.java
├── AgentUsageDailyMapper.java
├── AgentMemorySummaryMapper.java
├── AgentKnowledgeDocMapper.java
├── AgentKnowledgeChunkMapper.java
└── AgentBusinessBindingMapper.java
```

全部 `extends BaseMapper<POJO>`，遵循项目已有规范。

## 4. 工具系统设计

### 4.1 整体模型

```
t_agent_skill.tool_allowlist_json  ──白名单──→  t_agent_tool_def  ──元数据──→  @Tool 注解方法
     (技能可用哪些工具)                           (权限/风险/方法路径)              (实际执行逻辑)
```

### 4.2 启动注册流程

1. Spring 扫描所有 `@Tool` 注解 Bean → Map<toolName, Method>
2. `ToolRegistry.init()` 从 `t_agent_tool_def` 加载元数据
3. 校验：DB 中每个 `tool_code` 必须匹配一个 `@Tool` 方法，否则启动 WARN
4. 构建 `RuntimeToolDef`：DB 元数据 + Method 引用 + ToolCallback

### 4.3 Phase 1 业务工具

**UserWebsiteTool**（用户端）:
- `submitWebsiteDraft(name, url, summary, description, categoryId, tags)` → WebsiteDraftVO
- 生成 `t_website_draft` 草稿记录

**AdminWebsiteTool**（管理端）:
- `auditWebsite(websiteId, auditStatus, remark)` → AuditResultVO
- `batchAuditWebsite(batchJson)` → BatchAuditResultVO

### 4.4 工具执行链路

```
ChatOrchestrator
  → SkillLoader.getAllowlist(skillCode)
  → ToolRegistry.getToolCallbacks(allowlist)
  → ChatClient.call().tools(filteredCallbacks)
  → LLM 返回 ToolCall → ToolCallingManager 执行
  → AgentToolCallMapper.insert(callRecord)
  → 发布钩子事件（post_tool）
```

### 4.5 风险分级

| 风险等级 | 工具示例 | 执行策略 |
|---------|---------|---------|
| `low` | 查询分类/标签 | 直接执行 |
| `medium` | 创建投稿草稿 | 直接执行（无副作用） |
| `high` | 审核通过/拒绝/批量审核 | 生成 ActionPlan → 用户确认后执行 |

### 4.6 确认流程

```
LLM 调用高风险工具
  → 不立即执行，生成 t_agent_action_plan (status=wait_confirm)
  → SSE 推送 plan 事件到前端，展示确认卡片
  → 用户确认 → PlanExecutor.confirm(planId) → 执行 → status=executed
  → 用户拒绝 → status=rejected，本轮结束
```

## 5. ChatOrchestrator 流式编排引擎

### 5.1 路由决策

```
用户消息到达
  → 基于 Skill system_prompt + LLM 自然判断
     ├─ 无 ToolCall 产生 → SimpleStreamPipe
     └─ 有 ToolCall 产生 → ReActOrchestrationPipe
```

### 5.2 SimpleStreamPipe

```
ChatClient.stream()
  .system(skillPrompt)
  .advisors(
    RetrievalAugmentationAdvisor,  // RAG
    MessageChatMemoryAdvisor,       // 对话历史
    LoggerAdvisor                   // 日志
  )
  .user(userMessage)
  .stream().chatResponse()
  → Flux<String> 逐 token → SSE message 事件
```

不传 tools，纯对话场景。

### 5.3 ReActOrchestrationPipe

改造现有 `BaseAgent.run()` 接收 `SseEmitter` 回调：

```
ReActAgent.run(userPrompt, sseSink) {
    while (step < maxSteps && state != FINISHED) {
        sseSink.push(thinking, {step})
        response = chatClient.call().system(skillPrompt).tools(filteredTools)
        
        if (no ToolCall) {
            sseSink.push(message, {delta: response.text})
            break
        }
        
        for (tc : response.toolCalls) {
            sseSink.push(tool_call, {name, args})
            
            if (riskLevel == high) {
                plan = createPlan(tc)
                sseSink.push(plan, {planId, summary, risk})
                result = waitForUserConfirm(plan.id)
                if (result.rejected) { sseSink.push(done); return }
            }
            
            result = executeTool(tc)
            sseSink.push(tool_result, {name, result})
        }
    }
    sseSink.push(done, {sessionId, usage})
}
```

### 5.4 Controller 层

```java
// 用户端
@PostMapping("/api/agent/user/chat")
public SseEmitter chat(@RequestBody @Valid AgentChatRequest req)
// → StpUtil.checkLogin()

// 管理端
@PostMapping("/api/admin/agent/chat")
public SseEmitter adminChat(@RequestBody @Valid AgentChatRequest req)
// → StpAdminUtil.checkLogin()

// 确认计划
@PostMapping("/api/agent/plan/confirm")
public HttpResult<AgentPlanVO> confirmPlan(@RequestBody @Valid AgentPlanConfirmRequest req)

// 拒绝计划
@PostMapping("/api/agent/plan/reject")
public HttpResult<AgentPlanVO> rejectPlan(@RequestBody @Valid AgentPlanConfirmRequest req)
```

SseEmitter 配置：超时 5 分钟，每 30 秒心跳保活。

## 6. 技能系统

### 6.1 加载流程

```
SkillLoader（启动 + 每 5 分钟定时刷新）
  → t_agent_skill WHERE enabled=1
  → 内存 Map<skillCode, SkillDefinition>
  → 解析出：systemPrompt / toolAllowlist / mcpAllowlist / audience
```

### 6.2 审核匹配

| 请求方 | 过滤条件 |
|--------|---------|
| 用户端 | audience IN ("user", "both") |
| 管理端 | audience IN ("admin", "both") |

### 6.3 Phase 1 技能

| skill_code | audience | 白名单工具 | MCP |
|-----------|----------|-----------|-----|
| `website_submit_assistant` | user | `user_submit_website` | `tavily` |
| `website_audit_assistant` | admin | `admin_audit_website`, `admin_batch_audit_website` | `tavily` |

## 7. 记忆与配额

### 7.1 短期记忆

保留现有 `MessageWindowChatMemory`（JDBC，10 条窗口），对话结束时自动持久化。

### 7.2 长期记忆摘要

```
MemorySummarizer（异步，对话结束触发）
  → 取会话最近消息
  → LLM 生成摘要（conversation / preference / task）
  → 写入 t_agent_memory_summary
  → 下次会话注入 system_prompt 的 <user_memory> 段
```

### 7.3 配额控制

```
QuotaGuard（每次对话前拦截）
  → 匹配 t_agent_quota_policy（user_id → default_user 默认策略）
  → 查询 t_agent_usage_daily 当日用量
  → 超限 → SSE quota_exceeded 事件
  → 对话结束 → UsageTracker 原子 upsert
```

默认限额：普通用户 20 条/天 + 10万 token，管理员 200 条/天 + 100万 token。

## 8. 钩子事件系统

| 钩子类型 | 触发时机 | 用途 |
|---------|---------|------|
| `pre_think` | Think 前 | 注入额外上下文 |
| `post_think` | Think 后 | 记录思考日志 |
| `pre_tool` | 工具调用前 | 参数校验/脱敏 |
| `post_tool` | 工具调用后 | 审计/通知 |
| `on_error` | 异常时 | 告警通知 |
| `on_complete` | 对话完成 | 更新标题、生成摘要 |

写入 `t_agent_hook_event`，handler_code 指向 Spring Bean 方法。

## 9. RAG + Pinecone

### 9.1 文档入向量库

```
AgentKnowledgeService.syncDocument(docId)
  → t_agent_knowledge_doc 读元数据
  → 加载源文件（markdown / 网站描述 / 审核策略）
  → AgentTokenTextSplitter 切片
  → Pinecone API 写入向量（namespace = doc.pinecone_namespace）
  → 逐 chunk 写 t_agent_knowledge_chunk
  → doc.sync_status = synced
```

### 9.2 检索流程

复用现有 RAG 三阶段 + `RetrievalAugmentationAdvisor`：
QueryRewrite → Pinecone 检索 → 上下文增强注入 system prompt。

Phase 1 知识库类型：`website`（网站描述辅助搜索）、`audit_policy`（审核策略参考）。

## 10. MCP 集成

保留现有 `TavilyMcpClientHandlers` + `CustomMcpAsyncClientCustomizer`，在 ReAct 编排中按 Skill 的 mcp_allowlist 动态加载 MCP ToolCallback。

## 11. 现有代码改造策略

| 文件 | 操作 |
|------|------|
| `BaseAgent.java` | `run()` 签名改造，接收 SseEmitter 回调参数 |
| `ReActAgent.java` | `step()` 注入钩子事件发布 |
| `ToolCallAgent.java` | `action()` 前后加风险检查 + 计划确认逻辑 |
| `ToolRegistry.java` | 重构为 Map<toolName, RuntimeToolDef> + DB 元数据加载 |
| `RagHandler.java` | 填充 Pinecone 同步/检索逻辑 |
| `AgentConfig.java` | 精简为单一 DashScope ChatClient + Advisor 链 |
| `ToolsConfig.java` | 删除 `WebsiteAgentTools` 引用 |
| `UserAiChatServiceImpl` | 委托给 `ChatOrchestrator` |
| `WebsitesTool.java` | 重命名为 `UserWebsiteTool`，填充业务逻辑 |
| `TavilyMcpClientHandlers` | 保留，细节完善 |
| `AdvisorConfig.java` | 完善 `RetrievalAugmentationAdvisor` Bean |

## 12. 前端 AI 对话界面

### 12.1 用户交互决策

| 决策项 | 选择 |
|--------|------|
| 交互形态 | 独立对话页 `/computer/agent` |
| 页面布局 | 桌面双栏（左侧会话列表 + 右侧对话区）；移动端全屏 + 抽屉式会话列表 |
| 思考过程 | 默认折叠为一行提示（"▶ 思考过程 · N步 · 工具调用 N次"），点击展开查看详情 |
| 技能选择 | 默认"通用助手"技能，对话顶部可随时切换下拉选择 |
| 输入能力 | 多行文本输入 + 代码块快捷插入按钮 + 文件/图片上传 |
| 会话列表功能 | 重命名、删除、搜索过滤、置顶 |
| 消息操作 | 复制、重新生成、编辑已发送消息、点赞/踩 |

### 12.2 路由设计

```
/computer/agent              — 用户端 AI 对话页（需登录，StpUtil）
/computer/admin/agent        — 管理端 AI 对话页（需管理登录，StpAdminUtil）
```

### 12.3 前端文件结构

```
src/
├── api/
│   └── agent.ts                    — SSE 连接 + 会话/配额 REST API
├── stores/
│   └── agentChat.ts                — Pinia store：消息列表、连接状态、当前会话
├── composables/
│   └── useAgentChat.ts             — SSE 事件解析 + 连接生命周期管理
├── components/
│   └── agent/
│       ├── AgentSessionList.vue    — 会话列表（搜索/新建/重命名/删除/置顶）
│       ├── AgentSessionItem.vue    — 单条会话项（含右键菜单）
│       ├── AgentChatView.vue       — 对话主区域（消息列表 + 输入区）
│       ├── AgentMessageBubble.vue  — 单条消息气泡（用户/AI 两种样式 + 操作栏）
│       ├── AgentThinkingSteps.vue  — 思考过程折叠面板（thinking/tool_call/tool_result）
│       ├── AgentInputArea.vue      — 输入区（textarea + 代码块按钮 + 文件上传 + 技能下拉）
│       ├── AgentCodeEditor.vue     — 代码块插入弹窗（语言选择 + 语法高亮）
│       ├── AgentWelcomeScreen.vue  — 空状态欢迎页（无消息时展示）
│       └── AgentPlanCard.vue       — 计划确认卡片（高风险操作需用户确认）
├── views/
│   └── user/
│       └── AgentView.vue           — 用户端对话页面（组装 Sidebar + ChatView）
│   └── admin/
│       └── AdminAgentView.vue      — 管理端对话页面
└── router/
    └── index.ts                    — 新增 /agent 和 /admin/agent 路由
```

### 12.4 页面布局规范

**桌面端（≥768px）**：

```
┌──────────────┬──────────────────────────────────┐
│  会话列表      │  顶部栏: 会话标题 + 技能下拉 + 更多菜单 │
│  (280px)     │──────────────────────────────────│
│  [+ 新对话]   │                                  │
│  [搜索...]    │   消息区域                         │
│              │   - 用户消息（橙色气泡，右对齐）       │
│  📌 会话1     │   - AI 消息（灰色气泡，左对齐）       │
│  会话2        │     · 思考过程折叠条                │
│  会话3 ← 当前  │     · 文本内容                    │
│  会话4        │     · 操作栏（复制/重新生成/点赞）    │
│              │                                  │
│              │──────────────────────────────────│
│  配额: 18/50  │  输入区                           │
│              │  [技能下拉] [textarea] [代码][上传][发送]│
└──────────────┴──────────────────────────────────┘
```

**移动端（<768px）**：

```
┌────────────────────┐
│ ☰ 会话标题  [技能▼] │  ← 顶部栏，☰ 打开会话抽屉
├────────────────────┤
│                    │
│   消息区域（全屏）   │
│                    │
├────────────────────┤
│ [{ }] [📎] [___] ↑ │  ← 紧凑输入区
└────────────────────┘
       ↓ 抽屉层
┌────────────┐
│ [+ 新对话]  │
│ [搜索...]   │
│ 会话列表    │
│ 配额信息    │
└────────────┘
```

### 12.5 SSE 事件 → 前端映射

| SSE 事件 | 前端行为 |
|---------|---------|
| `heartbeat` | 连接保活，忽略（或更新连接状态指示灯） |
| `thinking` | 在 AI 消息气泡内追加/更新可折叠的"思考中..."条目 |
| `message` | 流式追加文本 delta 到当前 AI 消息气泡（打字机效果） |
| `tool_call` | 在思考面板中新增"🔧 调用工具: {toolName}"条目 |
| `tool_result` | 更新对应工具调用条目的结果为 "✅/❌" |
| `plan` | 在消息列表中插入 `AgentPlanCard` 组件（确认/拒绝按钮） |
| `done` | 结束流式输出，最终化消息内容，记录 sessionId |
| `error` | 显示错误提示 Toast + 消息气泡内错误状态 |
| `quota_exceeded` | 禁用输入区 + 显示配额耗尽提示 |

### 12.6 状态管理（Pinia Store）

```typescript
// agentChat.ts — Composition API
interface AgentChatState {
  sessions: AgentSession[]           // 会话列表
  currentSessionId: string | null    // 当前会话
  messages: AgentMessage[]           // 当前会话消息列表
  connectionState: 'idle' | 'connecting' | 'streaming' | 'done' | 'error'
  currentSkillCode: string           // 当前选中技能
  quota: AgentQuota | null           // 配额信息
  thinkingSteps: ThinkingStep[]      // 当前正在构建的思考步骤
}
```

### 12.7 API 设计

```typescript
// src/api/agent.ts

// SSE 对话连接（使用 fetch + ReadableStream，POST 不支持 EventSource）
function chatStream(request: AgentChatRequest): ReadableStream

// 会话管理
function getSessions(): Promise<AgentSessionVO[]>
function renameSession(id: string, title: string): Promise<void>
function deleteSession(id: string): Promise<void>
function pinSession(id: string, pinned: boolean): Promise<void>

// 计划确认
function confirmPlan(planId: string): Promise<void>
function rejectPlan(planId: string): Promise<void>

// 配额查询
function getQuota(): Promise<AgentQuotaVO>
```

### 12.8 后端需新增的 REST API

前端会话列表/配额等功能需要以下 REST 接口（`UserAgentController` + `AdminAgentController`）：

```
GET    /api/agent/sessions              → 当前用户会话列表（支持搜索参数 ?keyword=xx）
GET    /api/agent/sessions/{id}         → 单会话详情（含消息历史）
PUT    /api/agent/sessions/{id}         → 重命名会话 { title }
DELETE /api/agent/sessions/{id}         → 删除会话（设置 status=archived）
PUT    /api/agent/sessions/{id}/pin     → 切换置顶 { pinned: boolean }
GET    /api/agent/quota                 → 当前用户配额信息
```

实体变更：`t_agent_session` 表需增加 `pinned TINYINT DEFAULT 0` 字段（如尚未存在）。

### 12.9 技术实现要点

1. **SSE 连接**：使用 `fetch` + `ReadableStream` 读取 POST SSE 响应（浏览器 `EventSource` 不支持 POST）。解析 `data:` 行，按事件类型分发到 store
2. **打字机效果**：`message` 事件携带 `delta`，逐字追加到对应消息的 `content` 字段，触发 Vue 响应式更新
3. **移动端适配**：Tailwind `md:` 断点区分桌面/移动布局；移动端会话列表用 `<Teleport>` + 遮罩层实现抽屉
4. **暗黑模式**：复用现有主题系统（`data-cf-theme="user"` + `.dark` class），所有组件使用 Tailwind `dark:` 变体
5. **认证头**：使用 `localStorage.tokenName` → `satoken` header；管理端额外带 `X-CF-Skip-Auth`
6. **错误处理**：网络断开自动重连（指数退避 1s/2s/4s，最多 3 次）；SSE 超时 5 分钟后自动关闭

## 13. 不在范围

- 大模型多供应商切换
- 管理员动态配置 Skill/Tool
- 对话多模态（图片识别）
- AI 生成网站截图/摘要等富内容
- 对话分享功能
- 语音输入
