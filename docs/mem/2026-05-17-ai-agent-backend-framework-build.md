# Spring AI Agent 后端框架搭建全流程经验

> 日期：2026-05-17 | 分支：feat/user-ai-agent-new-cc | 会话：ai-agent-back-init-dev

## 功能概述

在 computer-favorites 项目中基于 Spring AI 混合技术栈（Alibaba 1.1.2.0 + 上游组件）搭建 AI Agent 智能助手后端框架。Phase 1 覆盖用户网站投稿和管理员网站审核两个核心场景，采用 SSE 流式输出，10 个 commit、106 个文件、+8200 行代码，cf-model + cf-service 编译通过。

## 实现思路与关键设计

### 1. 混合架构：ChatClient 流式 + ReAct 编排

**决策**：日常闲聊走 `ChatClient.stream()` 轻量链路，复杂业务任务（投稿/审核）走 ReAct Agent 同步编排。

**理由**：纯 Advisor 链无法实现多步 think→tool_call→act 循环；纯 ReAct 同步循环阻塞 SSE 流式输出。混合架构两全其美。实际实现中，ChatOrchestrator 通过检查 Skill 是否有可用工具来决定路由——无工具走 simpleStream，有工具走 reactiveStream。

### 2. 工具系统：@Tool 注解 + DB 元数据混合模式

**决策**：业务逻辑用 `@Tool` 注解实现（UserWebsiteTool / AdminWebsiteTool），DB 的 `t_agent_tool_def` 表存储权限码、风险等级、白名单等元数据。

**理由**：纯注解方式无法实现管理端动态配置；纯 DB 驱动需要在 DB 中维护代码逻辑引用，复杂度高。混合模式将代码逻辑与配置元数据解耦，`ToolRegistry.init()` 启动时将两者关联。

### 3. 风险分级确认机制

**决策**：高风险操作（审核通过/拒绝）生成 `t_agent_action_plan` 等待用户确认，低/中风险操作（查询、创建草稿）直接执行。

**Trade-off**：增加了一次用户交互往返，但避免了 AI 误操作导致的生产事故。Phase 1 中确认流程的 `waitForUserConfirm()` 是占位实现，后续需通过 SSE plan 事件 + 前端确认卡片 + `/api/agent/plan/confirm` 接口完成闭环。

### 4. 技能驱动的工具白名单

**决策**：每个 Skill 通过 `tool_allowlist_json` 字段声明可用工具列表，SkillLoader 每 5 分钟从 DB 刷新。

**理由**：用户端的 `website_submit_assistant` 和管理端的 `website_audit_assistant` 看到的工具集合不同，防止用户调用管理端工具。白名单在 ChatOrchestrator 中通过 `toolRegistry.getFilteredCallbacks(allowlist)` 过滤。

### 5. SSE 事件契约设计

9 种事件类型：`thinking`、`message`、`tool_call`、`tool_result`、`plan`、`done`、`error`、`quota_exceeded`、`heartbeat`。前端按事件类型 name 订阅，payload 为 JSON map。SseEmitter 超时 5 分钟，30 秒心跳保活。

### 6. 配额控制前置

**决策**：每次对话开始前 `QuotaGuard.isExceeded()` 检查，对话结束后 `UsageTracker.record()` 原子更新。默认普通用户 20 条/天。

**理由**：配额检查放在 ChatOrchestrator.handle() 入口，超限直接返回 `quota_exceeded` 事件并 complete，不消耗任何 LLM token。

## 踩坑与问题

### fastjson2 不在项目依赖中

- **现象**：`SkillLoader.java` 编译报 `找不到 com.alibaba.fastjson2`
- **根因**：项目 pom.xml 没有 fastjson2 依赖，但 SkillLoader 中使用了 `com.alibaba.fastjson2.JSON`
- **解决**：改用项目中已有的 hutool `cn.hutool.json.JSONUtil`，方法 `JSONUtil.toList(json, String.class)` 替换 `JSON.parseArray(json, String.class)`

### ToolRegistry 包路径不一致

- **现象**：`ChatOrchestrator.java` 编译报 `找不到 ToolRegistry`，但实际文件存在
- **根因**：ToolRegistry 在 `com.yyyouth.service.aichat.agent.tools`，而 ChatOrchestrator 的 import 写成了 `com.yyyouth.service.aichat.tool`
- **解决**：修正 import 为 `com.yyyouth.service.aichat.agent.tools.ToolRegistry`

### 历史代码 import 路径过期

- **现象**：`MyManus.java` 和 `ReActAgent.java` 分别报 `找不到 com.yyyouth.superagent.agent.advisor` 和 `找不到 com.yyyouth.superagent.constants`
- **根因**：旧代码使用了 `superagent` 包名，重构后 Advisor 和 Constants 移到了 `aichat` 包下。之前的编译未触发是因为 cf-model 未被 clean
- **解决**：修正 MyManus 为 `com.yyyouth.service.aichat.agent.advisor.LoggerAdvisor`，ReActAgent 为 `com.yyyouth.model.constants.ai.AgentConstants`

### ToolCallback 的 getToolDefinition() 返回 ToolDefinition 而非 String

- **现象**：`ToolCallbackConfig.java` 编译报类型不匹配，`.toMap(ToolCallback::getToolDefinition, ...)` 期望 `String` 但实际是 `ToolDefinition`
- **根因**：Spring AI 的 `ToolCallback.getToolDefinition()` 返回 `ToolDefinition` 对象，`.name()` 才是工具编码字符串
- **解决**：改为 `tc -> tc.getToolDefinition().name()`

### AgentTokenTextSplitter 方法名不一致

- **现象**：`AgentKnowledgeService.java` 编译报 `找不到 apply(List<Document>)`
- **根因**：项目中已有的 `AgentTokenTextSplitter` 方法名是 `splitWithBuilder` 而非 `apply`
- **解决**：改为 `tokenTextSplitter.splitWithBuilder(List.of(new Document(content)))`

### Maven 多模块编译顺序

- **现象**：单独 `mvn -pl cf-service compile` 报 61 个错误，全是 `找不到 com.yyyouth.model.*`
- **根因**：`cf-service` 依赖 `cf-model`，单独编译 cf-service 时 cf-model 的 class 不在 reactor 中
- **解决**：始终用 `mvn -pl cf-model,cf-service compile` 同时编译两个模块。实际开发中 `mvn -pl cf-core spring-boot:run` 会递归构建所有依赖模块，不会出现此问题

### MyManus ChatModel 注入二义性

- **现象**：Spring Boot 启动报 `Parameter 1 of constructor in MyManus required a single bean, but 2 were found: dashScopeChatModel, deepSeekChatModel`
- **根因**：项目同时有 DashScope 和 DeepSeek 两个 ChatModel Bean，MyManus 构造器参数类型是 `ChatModel`，Spring 无法确定注入哪个
- **解决**：在构造器参数上添加 `@Qualifier("dashScopeChatModel")`

## 扩展点与待优化项

- [ ] **工具确认流程闭环**：当前高风险工具的 `waitForUserConfirm()` 是占位实现，需通过 SSE plan 事件 + 前端确认卡片完成
- [ ] **业务工具对接真实 Service**：UserWebsiteTool 和 AdminWebsiteTool 当前只返回格式化字符串，未实际调用业务 Mapper/Service 写入数据库
- [ ] **ToolRegistry 定时刷新**：工具定义变更后需调用 `refresh()` 重新加载，当前无自动刷新机制
- [ ] **MemorySummarizer LLM 摘要**：当前使用固定字符串替代 LLM 摘要生成
- [ ] **RAG 文档内容加载**：AgentKnowledgeService 中 source content 是硬编码占位，需对接文件读取
- [ ] **会话列表/管理接口**：AgentSessionService 已有 `listByOwner`/`rename`/`delete`/`togglePin`，但缺少对应的 Controller 暴露
- [ ] **管理员动态配置 Skill/Tool**：当前 Skill 和 Tool 的 CRUD 无管理端接口
- [ ] **对话多模态支持**：当前仅支持文本对话
- [ ] **大模型供应商切换**：t_agent_session 已有 model_provider/model_name 字段，但硬编码为 dashscope/qwen-plus

## 关键文件索引

| 文件 | 作用 |
|------|------|
| `cf-model/.../enums/ai/{TaskStatus,PlanStatus,OwnerType,ToolType,RiskLevel,AgentHookType}.java` | Agent 相关枚举定义 |
| `cf-model/.../pojo/agent/Agent{Session,Task,ActionPlan,ToolDef,...}.java` | 14 张 Agent 表的 MyBatis-Plus 实体 |
| `cf-model/.../dto/agent/AgentChatRequest.java` | SSE 对话请求 DTO |
| `cf-model/.../constants/ai/AgentConstants.java` | Agent 常量（扩展后） |
| `cf-service/.../aichat/agent/tools/ToolRegistry.java` | DB 驱动的工具注册表（重大重构） |
| `cf-service/.../aichat/tool/{UserWebsiteTool,AdminWebsiteTool,ToolCallbackConfig}.java` | Phase 1 业务工具实现 |
| `cf-service/.../aichat/skill/SkillLoader.java` | 技能加载器（内存缓存 + 定时刷新） |
| `cf-service/.../aichat/session/AgentSessionService.java` | 会话生命周期管理 |
| `cf-service/.../aichat/quota/{QuotaGuard,UsageTracker}.java` | 配额检查与用量记录 |
| `cf-service/.../aichat/memory/MemorySummarizer.java` | 长期记忆异步摘要 |
| `cf-service/.../aichat/hook/HookEventPublisher.java` | 钩子事件发布 |
| `cf-service/.../aichat/chat/{ChatOrchestrator,AgentStreamSink}.java` | 核心编排引擎 + SSE 推送封装 |
| `cf-service/.../aichat/agent/thinking_agent/BaseAgent.java` | 改造后支持 SseEmitter 的基类 |
| `cf-service/.../aichat/agent/rag/RagHandler.java` | RAG 向量检索入口 |
| `cf-service/.../aichat/config/AgentConfig.java` | 精简后的 ChatClient 配置 |
| `cf-web/.../controller/agent/{UserAgentController,AdminAgentController,AgentPlanController}.java` | SSE 对话端点 + 计划确认接口 |
| `docs/sql/ai-agent.sql` | 13 张 Agent 表 DDL + 种子数据 |
| `docs/superpowers/specs/2026-05-17-ai-agent-assistant-design.md` | 架构设计 spec |
| `docs/superpowers/plans/2026-05-17-ai-agent-assistant-implementation.md` | 2481 行开发实施计划 |
