# AI Agent 对话界面 — 从设计到联调全栈实录

> 日期：2026-05-17 | 分支：feat/user-ai-agent-new-cc | 会话：ai-agent-front-dev1

## 功能概述

为计算机专业个人网页收藏夹平台构建用户端 AI 对话界面。后端 AI Agent 框架已具备 SSE 流式对话能力（ChatOrchestrator + AgentStreamSink），前端从零搭建完整的对话 UI：类型定义 → API 层 → Pinia Store → Composable → 9 个组件 → 2 个视图 → 路由 → 导航入口。后端同步扩展了 6 个会话管理 REST 接口和 1 个数据库字段。

## 实施过程

采用 **subagent-driven-development** 模式，25 个 Task、6 个 Phase 逐一派发执行，每个 Task 由独立 subagent 实现并提交。

| Phase | 内容 | 文件数 |
|-------|------|--------|
| 1. 数据库+后端 | `pinned` 迁移、AgentSession 扩展、6×2 REST 端点 | 5 修改 + 1 新建 |
| 2. 类型+API | `types/agent.ts`、`api/agent.ts` | 2 新建 |
| 3. Store+Composable | Pinia store、SSE 解析 composable | 2 新建 |
| 4. 组件 ×9 | WelcomeScreen/ThinkingSteps/MessageBubble/CodeEditor/InputArea/PlanCard/SessionItem/SessionList/ChatView | 9 新建 |
| 5. 视图+路由 | AgentView/AdminAgentView/router | 2 新建 + 1 修改 |
| 6. 收尾 | AppNavbar 入口 | 1 修改 |

## 实现思路与关键设计

- **SSE 使用 fetch + ReadableStream 而非 EventSource**：EventSource 不支持 POST 请求，而后端 SSE 端点需要 POST 传递 message/skillCode/conversationId。通过 `fetch()` 拿到 `response.body`（ReadableStream），逐行解析 `event:xxx\ndata:xxx\n\n` 协议。

- **ReadableStream 简化**：初版在 `chatStream()` 中用双层异步包装（fetch 内部 + ReadableStream wrapper），导致错误信息吞咽。最终简化为：fetch → `response.body.getReader()` → 直接 pipe 到外层 ReadableStream。

- **错误信息传播链**：Store 新增 `errorMessage` 字段 → Composable 在 catch/SSE error 事件中设置 → AgentChatView 直接展示 `store.errorMessage`，不再显示通用"连接出错，请重试"。

- **技能码匹配问题**：前端硬编码的 `general_assistant`/`code_assistant`/`website_assistant` 在数据库 `t_agent_skill` 中不存在（实际只有 `website_submit_assistant` 和 `website_audit_assistant`），导致后端 `SkillLoader.get()` 返回 null → SSE error 事件。修复为使用实际 DB 技能码。

- **SSE IOException 被静默吞咽**：`AgentStreamSink.send()` 的 catch 块只注释了 `// SSE 连接可能已关闭`，无任何日志。加上 `log.warn` 后可发现发送失败的根本原因。

## 踩坑与问题

### SSE 连接报"技能编码无效"
- **现象**：前端发"你好"，控制台 `技能编码无效: general_assistant`
- **根因**：`stores/agentChat.ts` 硬编码了 3 个不存在的 skill code，数据库 `t_agent_skill` 仅有 `website_submit_assistant` 和 `website_audit_assistant`
- **解决**：`currentSkillCode` 和 `skills` 列表改为使用实际 DB 技能码

### SSE 连接成功但无消息回复
- **现象**：后端 log 显示请求已到达（`userId=199, skillCode=website_submit_assistant, message=你好`），前端无回复、无错误
- **根因**：排查中。`ChatOrchestrator.simpleStream()` 调用 DashScope API，Flux 通过 `doOnNext`/`doOnComplete`/`doOnError` 推送 SSE。问题可能为：1) DashScope API 无输出 2) SSE 事件发送失败（IOException 被吞） 3) LLM system prompt 限制了非投稿类对话
- **已做**：给 `ChatOrchestrator` 和 `AgentStreamSink` 加详细日志，等待重启后端获取日志定位

### Vite 代理端口与后端端口不匹配
- **现象**：CLAUDE.md 已知问题 — Vite `/api` 代理到 `127.0.0.1:18080`，但后端 dev 端口是 `18081`
- **解决**：需 `VITE_API_PORT=18081 npm run dev` 或使用 local profile（8080）启动后端

### 后端预存测试编译失败
- **现象**：`mvn install` 报 `ChatClientTest02.java` 找不到 `DateTimeTools`
- **说明**：预存问题，与本次改动无关。主代码 `mvn compile` 5 模块全部 BUILD SUCCESS

### 前端预存类型错误
- **现象**：`vue-tsc` 报 10+ 类型错误，分布在 `AppMessage.vue`、`useTechStack.ts`、`TechStackManagementView.vue` 等文件中
- **说明**：预存问题。我们的 14 个新建文件类型检查通过。Vite build 成功

## 扩展点与待优化项

- [ ] 前端 `skills` 列表应改为从后端 API 动态获取，而非硬编码
- [ ] `confirmPlan`/`rejectPlan` API 在 `src/api/agent.ts` 中仍为占位，后端 `AgentPlanController` 也是 mock 状态
- [ ] 管理端 AI 对话需要独立的 `adminAgentChat` store 或参数区分，当前共用 `agentChat` store
- [ ] `chatStream` 未针对管理端做 `buildAdminAuthHeaders()` 适配
- [ ] 文件上传实际内容传输未实现（目前仅拼接文件名到消息中）
- [ ] 消息持久化：当前消息仅存于前端内存，刷新后丢失。后端会话消息历史需 `t_agent_message` 或复用 JDBC ChatMemory

## 关键文件索引

| 文件 | 作用 |
|------|------|
| `docs/superpowers/specs/2026-05-17-ai-agent-assistant-design.md` | 设计规范（含前端 UI 12 节） |
| `docs/superpowers/plans/2026-05-17-ai-agent-chat-ui.md` | 25 Task 详细实施计划 |
| `docs/sql/2026-05-17-agent-session-pinned.sql` | DB 迁移：t_agent_session 加 pinned 列 |
| `cf-service/.../aichat/chat/ChatOrchestrator.java` | SSE 编排器（simpleStream/reactiveStream + 日志） |
| `cf-service/.../aichat/chat/AgentStreamSink.java` | SSE 事件推送（9 种事件 + IOException 日志） |
| `cf-service/.../aichat/session/AgentSessionService.java` | 新增 5 个会话管理方法 |
| `cf-web/.../controller/agent/UserAgentController.java` | 新增 6 个 REST 端点（用户端） |
| `cf-web/.../controller/agent/AdminAgentController.java` | 新增 6 个 REST 端点（管理端） |
| `cf-model/.../vo/agent/AgentSessionVO.java` | 新增 pinned/ownerType/ownerId |
| `cf-model/.../pojo/agent/AgentSession.java` | 新增 pinned 字段 |
| `src/types/agent.ts` | 前端所有 Agent 类型定义 |
| `src/api/agent.ts` | SSE chatStream + 6 个 REST API 调用 |
| `src/stores/agentChat.ts` | Pinia store：会话/消息/配额/技能状态管理 |
| `src/composables/useAgentChat.ts` | SSE 事件解析 + send/regenerate/edit 生命周期 |
| `src/components/agent/AgentChatView.vue` | 对话主组件（消息列表 + 输入区 + 状态指示） |
| `src/components/agent/AgentInputArea.vue` | 输入区（技能选择 + textarea + 代码/文件按钮） |
| `src/components/agent/AgentMessageBubble.vue` | 消息气泡（user/AI 双样式 + 编辑 + 操作栏） |
| `src/components/agent/AgentThinkingSteps.vue` | 思考过程折叠面板 |
| `src/components/agent/AgentSessionList.vue` | 会话列表侧栏 |
| `src/components/agent/AgentSessionItem.vue` | 单条会话项（置顶/重命名/删除悬浮操作） |
| `src/router/index.ts` | 新增 `/agent` 和 `/admin/agent` 路由 |
| `src/components/common/AppNavbar.vue` | 新增"AI 助手"导航链接 |
