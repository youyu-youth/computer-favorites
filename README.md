# Computer Favorites

<div align="center">

**面向程序员的网站收藏与发现平台 · 内置 AI Agent 智能助手**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.12-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4fc08d)](https://vuejs.org/)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![License](https://img.shields.io/badge/license-MIT-blue)](./LICENSE)

</div>

---

## 1. 项目介绍

**Computer Favorites** 是一个专为 CS 学习者打造的全栈网站收藏与资源发现平台。程序员每天都在收藏各种技术站点：文档、教程、工具、博客——但这些收藏分散在浏览器书签、笔记、聊天记录里，难以检索、无法共享。这个项目试图用**结构化收藏 + 社区分享 + AI 智能辅助**三层能力解决这个问题。

核心思路：

- **文件夹式收藏体系**：像管理代码仓库一样管理网站收藏，支持多层级文件夹、拖拽排序、公开/私密切换。
- **社区资源发现**：浏览其他开发者公开的收藏夹，按分类、标签、技术栈筛选，找到经过人工筛选的优质资源。
- **AI Agent 深度介入**：不止是聊天。Agent 能搜索和推荐网站、辅助投稿、分析网站质量，还能理解用户的技术兴趣偏好。
- **用户投稿 + 管理员审核**：社区成员可提交新网站，经管理员审核后入库，形成可持续扩展的资源图谱。

平台分为**用户端**（浏览、收藏、投稿、AI 对话）和**管理后台**（网站审核、用户管理、数据看板、AI 审核辅助），两端共享同一套后端服务。数据库设计包含 **46 张表**（26 张核心业务表 + 6 张扩展表 + 14 张 AI Agent 专属表），覆盖内容、互动、运营、CQRS 分析和 AI 基础设施全链路。

---

## 2. AI Agent 智能助手

AI Agent 是整个项目技术深度最高的部分。它不是套壳聊天——内部包含完整的 Agent 编排引擎、RAG 检索管道、工具调用系统、技能注册机制和确认式高风险操作流程。

### 2.1 Agent 定位

Agent 的定位是**懂程序员的资源助手**，而非通用聊天机器人。它知道平台上有哪些网站、用户收藏了什么、什么技术栈热门、某个网站是否值得推荐。它可以直接操作用户的草稿、管理员的审核流，而不是只能"建议"。

### 2.2 Agent 能做什么

| 功能 | 说明 |
|------|------|
| 网站投稿辅助 | 用户描述想推荐的网站，Agent 搜索补充信息后生成结构化投稿草稿 |
| 网站审核辅助 | 管理员审核网站时，Agent 自动查询网站背景、分析内容质量、给出审核建议 |
| 语义资源搜索 | 用自然语言描述需求（"适合初学者的 Rust 教程"），通过 RAG 检索匹配的网站 |
| 智能网站分析 | 通过 MCP 联网搜索（Tavily），补充网站的 GitHub stars、技术栈、社区活跃度等信息 |
| 收藏夹智能查询 | 理解用户在收藏夹中"找什么"，结合用户的技术偏好给出推荐 |

### 2.3 Agent 工作流程

```
用户发送消息 (SSE POST)
  → 会话管理: 加载/创建 Agent Session
  → 配额校验: 检查每日消息/Token 限额
  → 技能加载: 从 DB 读取 Skill 的 system_prompt + 工具白名单
  → 意图路由: LLM 判断闲聊 vs 业务任务
      ├─ 闲聊 → ChatClient.stream() + Advisor 链 → 流式推送
      └─ 业务任务 → ReAct Agent 编排 (think → tool_call → act 循环)
           ├─ 低风险 → 直接执行 → 记录 t_agent_tool_call
           └─ 高风险 → 生成确认计划 → SSE 推送确认卡 → 等待用户确认
  → 记忆总结: 异步生成对话摘要写入长期记忆
  → 用量记录: 原子更新每日配额
```

### 2.4 核心技术组件

#### SSE 流式通信

前后端通过 Server-Sent Events 建立长连接（POST + ReadableStream），支持 9 种事件类型：`thinking`、`message`（流式 delta）、`tool_call`、`tool_result`、`plan`、`done`、`error`、`quota_exceeded`、`heartbeat`。前端组件逐 token 渲染打字机效果，思考过程和工具调用以可折叠面板展示。

#### RAG 向量检索

基于 Pinecone 向量数据库 + Spring AI `RetrievalAugmentationAdvisor` 实现。网站描述文档经切片后存入向量库，Agent 对话时自动通过 `RetrievalAugmentationAdvisor` 检索相关上下文注入 system prompt。支持 namespace 过滤（按知识库类型隔离），相似度阈值 0.6，topK 10。

**知识库类型：**
- `website` — 网站描述，支撑语义搜索和推荐
- `audit_policy` — 审核策略文档，为管理端 Agent 提供审核参考

#### MCP 工具集成

Agent 集成了 **Tavily Search MCP**（通过 `streamable_http` 传输），可以在对话中实时搜索互联网获取网站补充信息。MCP 服务器配置存储在 `t_agent_mcp_server` 表中，按 Skill 的 `mcp_allowlist` 动态加载。

#### 技能 (Skill) 系统

技能是 Agent 行为的顶层控制单元，定义在 `t_agent_skill` 表中：

| Skill | 适用端 | 允许工具 | MCP |
|-------|--------|---------|-----|
| `website_submit_assistant` | 用户端 | `user_submit_website` | `tavily` |
| `website_audit_assistant` | 管理端 | `admin_audit_website`、`admin_batch_audit_website` | `tavily` |

每个 Skill 包含 system_prompt、本地工具白名单、MCP 白名单。SkillLoader 启动时加载 + 每 5 分钟定时刷新，按 audience（user/admin/both）端过滤。

#### 工具 (Tool) 系统

工具由 `@Tool` 注解实现 + `t_agent_tool_def` DB 元数据控制。Spring 扫描所有 `@Tool` Bean，ToolRegistry 启动时从 DB 加载元数据并校验一致性。工具按风险等级分三类执行策略：

| 风险等级 | 执行策略 | 示例 |
|---------|---------|------|
| `low` | 直接执行 | 查询分类/标签 |
| `medium` | 直接执行（无副作用） | 创建投稿草稿 |
| `high` | 生成确认计划，用户确认后执行 | 审核通过/拒绝 |

#### Plan / Confirm 确认机制

高风险工具调用不直接执行，而是生成 `t_agent_action_plan`（含摘要、风险等级、结构化参数），通过 SSE `plan` 事件推送到前端 `.vue` 组件展示确认卡片。用户确认 → 执行；拒绝 → 本轮终止。plan 有过期时间，防止长时间挂起。

#### 意图路由 (IntentRouter)

使用独立的 DashScope ChatClient（无记忆）对用户消息做二分类：`chitchat` vs `functional`。闲聊走简单流式管道（不传 tools），功能请求走 ReAct 编排管道。

#### 配额管理

每次对话前 QuotaGuard 拦截校验。默认配额：**普通用户 20 条/天 + 10 万 token**，**管理员 200 条/天 + 100 万 token**。超限后返回 `quota_exceeded` 事件，前端禁用输入区。对话结束后 UsageTracker 原子 upsert 日用量。

#### 长期记忆

MemorySummarizer 在对话结束时异步触发，LLM 生成三类摘要（conversation / preference / task），写入 `t_agent_memory_summary`。下次对话时注入 system_prompt 的 `<user_memory>` 段，实现跨会话记忆。

#### 钩子事件系统

支持 6 种钩子类型（pre_think / post_think / pre_tool / post_tool / on_error / on_complete），写入 `t_agent_hook_event`，handler_code 指向 Spring Bean。用于日志审计、参数校验、通知告警等横切关注点。

### 2.5 前端 AI 对话界面

桌面端双栏布局（280px 会话列表 + 对话区），移动端全屏 + 抽屉式会话列表。支持：
- 技能切换、会话重命名/删除/置顶
- 思考过程折叠面板（thinking → tool_call → tool_result 可视化）
- 计划确认卡片（高风险操作需用户确认）
- 代码块插入、消息复制/重新生成/编辑
- 配额显示、暗黑模式适配

### 2.6 AI 功能进度

| 功能 | 状态 |
|------|------|
| SSE 流式对话（用户端 + 管理端） | ✅ 已完成 |
| ReAct Agent 编排引擎 | ✅ 已完成 |
| IntentRouter 意图路由 | ✅ 已完成 |
| Skill 加载与管理 | ✅ 已完成 |
| Tool 注册/发现/执行/审计 | ✅ 已完成 |
| Plan/Confirm 确认流程 | ✅ 已完成 |
| MCP Tavily 搜索集成 | ✅ 已完成 |
| RAG 向量检索 (Pinecone) | ✅ 已完成 |
| 配额管理 + 用量追踪 | ✅ 已完成 |
| 长期记忆摘要 | ✅ 已完成 |
| 钩子事件系统 | ✅ 已完成 |
| 工具柔性参数（字段级补全） | ✅ 已完成 |
| 前端 Agent 对话界面 | ✅ 已完成 |
| 用户投稿 Agent 流程 | ✅ 已完成 |
| 管理员审核 Agent 流程 | ✅ 已完成 |
| 网站自动分析与摘要 | 🟡 开发中 |
| 个性化推荐引擎 | 🟡 开发中 |
| 智能标签自动分类 | ⚫ 规划中 |
| 多模态支持（截图分析） | ⚫ 规划中 |

---

## 3. 项目架构

### 3.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                       Nginx / 网关                          │
├──────────────────────┬──────────────────────────────────────┤
│    computer-favorites-web (Vue 3)                          │
│    localhost:5173                                           │
│    ├─ UserLayout (/computer/*)                             │
│    └─ AdminLayout (/computer/admin/*)                      │
├──────────────────────┴──────────────────────────────────────┤
│    computer-favorites-back (Spring Boot 3.4.12)             │
│    localhost:18080 (local) / 18081 (dev)                   │
│                                                             │
│    cf-core ──→ cf-web ──→ cf-service ──→ cf-model ──→ cf-common
│                     │            │                           │
│                     ▼            ▼                           │
│              Sa-Token 双认证   MyBatis-Plus + Redis + MQ    │
├─────────────────────────────────────────────────────────────┤
│    MySQL 8.x  │  Redis 7.x  │  RabbitMQ 3.x  │  MinIO      │
│    Pinecone (向量)  │  DashScope (LLM)  │  Tavily (搜索)    │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 后端模块职责

| 模块 | 职责 |
|------|------|
| `cf-core` | 启动入口、`@SpringBootApplication`、`@MapperScan`、`@EnableScheduling`、资源配置文件 |
| `cf-web` | Controller 层（30 个 Controller）+ Sa-Token 配置 + 全局异常处理 + 定时任务 |
| `cf-service` | 业务 Service 层 + AI 引擎（聊天编排/工具执行/技能/RAG/MCP/记忆/配额）+ Mapper 层 + Redis/RabbitMQ/MinIO 中间件封装 |
| `cf-model` | 纯数据载体：POJO/VO/DTO/BO/枚举，不含业务逻辑 |
| `cf-common` | 基础组件：`HttpResult` 统一响应、常量、工具类、异常类、AOP 切面（审计日志/限流）、拦截器 |

模块依赖为单向不可逆链：`cf-core → cf-web → cf-service → cf-model → cf-common`。

### 3.3 前端架构

```
Vue 3 (Composition API + <script setup> + TypeScript)
  ├─ 路由: Vue Router 4 (30 条路由, 按 /computer/* 前缀)
  ├─ 状态: Pinia (13 个 store, Composition API 风格)
  ├─ HTTP: 自定义 fetch 封装 (非 Axios)
  ├─ UI: PrimeVue 4 (unstyled) + Tailwind CSS 4 + 21 个 U* 适配组件
  ├─ 主题: 令牌驱动双主题系统 (user 橙色 / admin 红橙, 亮/暗自动切换)
  ├─ 图标: FontAwesome SVG
  └─ 国际化: vue-i18n (zh-CN / en-US)
```

### 3.4 AI 模块内部架构

```
cf-service/src/main/java/com/yyyouth/service/aichat/
├── session/    — AgentSessionService: 会话 CRUD/归档/标题生成
├── chat/       — ChatOrchestrator: 路由决策 + SSE 流式编排
├── agent/      — ReActAgent + BaseAgent: think → tool_call → act 循环
├── tool/       — ToolRegistry: 启动时加载 DB 元数据 → 匹配 @Tool Bean → 构建 RuntimeToolDef
├── skill/      — SkillLoader: 从 DB 加载 Skill 定义，按 audience 过滤
├── rag/        — RagHandler: Pinecone 向量检索 + Document 存储
├── mcp/        — AgentMcpServerService: 读取 MCP 配置，动态构建 ToolCallback
├── memory/     — MemorySummarizer: 对话结束异步生成摘要
├── quota/      — QuotaGuard + UsageTracker: 配额校验 + 日用量原子更新
├── hook/       — HookEventPublisher: 发布事件到 t_agent_hook_event
└── config/     — AgentConfig / RAGConfig / AdvisorConfig / ChatMemoryConfig
```

### 3.5 数据存储

- **MySQL 8.x**：46 张表（`utf8mb4_unicode_ci`），分为四大类：

| 类别 | 表数 | 包含 |
|------|------|------|
| 核心业务 (`computer-favorites.sql`) | 26 | 用户认证 (6)、内容 (4)、用户互动 (8)、系统运营 (8) |
| 扩展表 (`database-补充脚本.sql`) | 6 | Session 持久化、投稿草稿、审计日志、CQRS 统计 (3) |
| AI Agent (`ai-agent.sql`) | 14 | Session/Task/Plan/Tool/Skill/MCP/Memory/Quota/Knowledge 等 |
| **合计** | **46** | 其中 16 张表含 `deleted` 软删除字段 |

- **Redis 7.x**：Cache-Aside 缓存（TTL + 抖动 / 空值缓存 / 互斥锁）、Lua 原子限流、Redisson 分布式锁
- **RabbitMQ 3.x**：异步通知 + 用户行为事件 + DLQ 死信队列，4 个消费者
- **MinIO**：头像、网站 Logo、技术栈图标、反馈截图
- **Pinecone**：网站描述向量 + 审核策略向量，namespace 隔离 (threshold 0.6, topK 10)

### 3.6 权限认证

基于 Sa-Token 的**双认证体系**：

- **用户端**：`StpUtil`（type="login"），请求头 `satoken: <token>`
- **管理端**：`StpAdminUtil`（type="admin"），请求头 `satoken: <token>` + `X-CF-Skip-Auth: 1`
- 拦截器按路径分流：`/api/admin/**` → `StpAdminUtil.checkLogin()`，其他 `/api/**` → `StpUtil.checkLogin()`

### 3.7 定时任务

- **`UserStatsRecomputeJob`**：每日凌晨 2:00 执行，使用 Redisson 分布式锁保证单节点执行，重算用户贡献统计（提交数/评论数/收藏数/点赞数/浏览数/贡献分）
- **`SkillLoader` 定时刷新**：每 5 分钟从 DB 重新加载 Skill 定义

---

## 4. 技术栈

### 4.1 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.12 | 应用框架 |
| Java | 17 | 运行环境 |
| MyBatis-Plus | 3.5.14 | ORM + 分页 + 逻辑删除 |
| Sa-Token | 1.44.0 | 双认证体系（用户 + 管理员 JWT） |
| Spring AI | 1.1.5 | AI Agent 框架（ChatClient + Advisor + VectorStore） |
| Spring AI Alibaba | 1.1.2.0 | DashScope 集成 |
| Spring AMQP | 3.x | RabbitMQ 消息队列 |
| Spring Data Redis | — | Redis 集成 |
| Redisson | 3.45.0 | 分布式锁 |
| Spring Mail | 3.4.12 | 邮件服务 |
| Druid | 1.2.23 | 数据库连接池 |
| MinIO | 7.1.0 | 对象存储 |
| Knife4j | 4.4.0 | API 文档 |
| Hutool | 5.8.38 | 通用工具类 |
| Fastjson2 | 2.0.57 | JSON 处理 |
| Guava | 33.4.8 | 增强工具库 |
| Lombok | 1.18.38 | 代码简化 |
| Sensitive Word | 0.29.3 | DFA 敏感词过滤 |

### 4.2 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.x | 前端框架 (Composition API + `<script setup>`) |
| TypeScript | 5.x | 类型安全 |
| Vite | 5.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| PrimeVue | 4.x | UI 组件库 (unstyled 模式) |
| Tailwind CSS | 4.x | 原子化 CSS 框架 |
| ECharts | 5.x | 数据可视化图表 |
| vue-i18n | — | 国际化 |
| vue3-slider-verify | — | 滑块验证码 |

### 4.3 AI

| 技术 | 用途 |
|------|------|
| Spring AI | Agent 编排、ChatClient、Advisor 链、VectorStore 抽象 |
| DashScope (Qwen-plus) | 主推理模型 |
| DeepSeek | 备选推理模型 |
| Pinecone | 向量数据库，存储网站描述和审核策略的 Embedding |
| Tavily MCP | 联网搜索，Agent 获取网站补充信息 |
| JDBC ChatMemory | 短期对话记忆（10 条消息窗口） |
| RetrievalAugmentationAdvisor | RAG 自动上下文注入 |

### 4.4 中间件 & 基础设施

| 技术 | 用途 |
|------|------|
| MySQL 8.x | 主数据库（utf8mb4_unicode_ci） |
| Redis 7.x | 缓存 + 限流 + 分布式锁 |
| RabbitMQ 3.x | 异步消息 + 死信队列 |
| MinIO | 文件/图片存储 |
| Pinecone | 向量检索 |

---

## 5. 核心功能

### 5.1 用户端

#### 首页 — 资源发现入口
全屏 Hero 区 + 标签筛选栏 + 网站卡片网格。支持按分类、标签、技术栈筛选，顶部搜索框支持关键词检索。卡片展示网站名称、图标、简介、标签、收藏/点赞数。

![首页](./docs/images/home.png)

#### 网站详情页
展示网站完整信息：名称、URL、图标、详细介绍、分类、标签、技术栈、GitHub 地址。右侧展示该网站的收藏/点赞/评论/评分统计面板。底部评论区和评分组件。

![网站详情](./docs/images/website-detail.png)

#### 收藏夹 — 文件夹式管理
多层级文件夹树 + 右侧网站卡片列表。支持创建/重命名/删除/移动文件夹，网站可拖拽到不同文件夹。支持设置文件夹为公开/私密，公开收藏夹可在用户主页被他人浏览。

![收藏夹](./docs/images/collection.png)

#### AI Agent 对话
独立全屏对话页面（`/computer/agent`）。左侧会话列表 + 右侧对话区。支持多技能切换、思考过程可视化、工具调用展示、计划确认卡。SSE 实时流式输出。

![AI 助手](./docs/images/agent.png)

#### 网站投稿
用户填写网站名称、URL、分类、标签、简介等信息提交投稿。投稿后进入管理员审核流程，用户可在投稿列表页跟踪审核状态。

![投稿](./docs/images/submission.png)

#### 用户主页
GitHub 风格的个人主页：Hero 区（头像、昵称、技术栈）、贡献热力图、数据看板（收藏数/点赞数/投稿数/影响力评分）、技能雷达图、公开收藏夹展示。

![用户主页](./docs/images/profile.png)

### 5.2 管理端

#### 网站管理
网站列表（表格视图）+ 搜索/筛选 + 批量操作。支持新增/编辑/上架/下架/删除。审核流：待审核 → 审核通过/驳回（含审核意见）。

![网站管理](./docs/images/admin-websites.png)

#### 数据看板（规划中）
网站总数、用户总数、今日新增、待审核数等核心指标卡片 + 趋势图表。

![数据看板](./docs/images/admin-dashboard.png)

#### 分类、标签、技术栈管理
三个独立管理页，每个包含表格 + 搜索 + 新增/编辑/删除 + 排序。标签有颜色标记，技术栈支持图标上传/删除。

#### 用户管理
用户列表（表格 + 搜索）+ 用户详情弹窗 + 编辑用户资料 + 状态切换 + 重置密码 + 踢会话。

![用户管理](./docs/images/admin-users.png)

#### 评论管理
评论列表 + 搜索筛选 + 详情查看 + 状态变更（通过/驳回/删除）+ 批量操作。

#### 举报处置
举报列表 + 详情面板 + 处理操作（忽略/警告/下架网站）+ 批量处置。

#### 反馈处理
用户反馈列表 + 详情查看 + 回复 + 关闭操作。

#### 公告管理
公告列表 + 富文本编辑器 + 置顶/发布/下架。

#### 审计日志（部分实现）
AOP 自动写入 `t_audit_log`，前端查询页面已创建，后端查询接口待完善。

---

## 6. 项目亮点

### 全链路 AI Agent 引擎
不是套壳 API 调用。从 Skill 注册 → 工具发现 → MCP 集成 → RAG 检索 → ReAct 编排 → Plan/Confirm → 配额管理 → 记忆持续化，构建了完整的 Agent 基础设施。Agent 可以真正"做事"——创建投稿草稿、执行审核操作——而不仅仅是聊天。

### 向量增强的语义搜索
通过 Pinecone 向量数据库 + RAG，实现了对网站资源的语义级搜索和理解。用户用自然语言描述需求，Agent 能找到技术栈匹配、难度合适、内容相关的网站，而不依赖关键词匹配。

### 结构化收藏体系
不同于浏览器扁平书签，支持多层级文件夹 + 标签 + 技术栈三维组织。收藏夹可设为公开，成为社区资源的一部分。

### 令牌驱动双主题系统
基于 CSS 自定义属性 + RGB 通道分离的令牌驱动主题系统。user 端（暖橙）和 admin 端（红橙）各一套主题令牌，亮/暗模式通过 Tailwind `dark` class 切换。设计上避开了 AI 生成界面常见的紫色/渐变 "AI 味"，走 TUI 复古 + 克制工业风。

### 完整的安全体系
BCrypt 密码加密、DFA 敏感词过滤、XSS 防护、CSRF Token、Redis Lua 原子限流、双认证隔离、审计日志 AOP、DDL 显式字符集（utf8mb4_unicode_ci）。

### 用户主页数据分析
CQRS 读模型 + 日粒度统计表 + 异步事件驱动（RabbitMQ），支撑用户主页的贡献热力图、影响力评分、技能雷达图等数据可视化。

---

## 7. 当前开发进度

> 进度来源于 `docs/spec-v2/01-模块状态矩阵.md`，基于真实代码与 SQL 结构校准。
>
> **状态说明**：✅ 已实现（前后端主链路可联通） | 🟡 部分实现 / 待实现（未形成可用闭环） | ⚫ 已废弃 | ⛔ 未开始

### 7.1 用户端 (15 个模块)

| # | 模块 | 状态 | 关键实现 |
|---|------|------|---------|
| user-01 | 统一认证 | ✅ | 注册/登录/登出/OAuth(GitHub/Gitee)/Session 续期 |
| user-02 | 用户中心 | ✅ | 个人资料/密码修改/账号管理/技术栈设置 |
| user-03 | 资源发现 | ✅ | 首页网站列表/分类筛选/标签筛选/关键词搜索 |
| user-04 | 互动行为 | ⚫ 已废弃 | 已拆分为 user-09 ~ user-12 |
| user-05 | 收藏管理 | ✅ | 11 接口：收藏/取消+文件夹树CRUD+隐藏/公开+密码验证，前端 CollectionView+PublicCollectionView 已闭环 |
| user-06 | 投稿工作台 | ✅ | 网站投稿/草稿保存/投稿状态跟踪 |
| user-07 | AI 对话助手 | 🟡 部分实现 | 22 接口已完成（SSE 对话/会话管理/技能列表/配额/知识库同步），前端 AgentView 已就绪，P0-P8 全链路精细设计待实施 |
| user-08 | 通知中心 | ✅ | 站内消息/评论回复/审核结果/举报反馈通知 |
| user-09 | 用户评论 | 🟡 部分实现 | 后端 6 接口已完成（发布/列表/删除/点赞），前端嵌入 WebsiteDetailView，独立评论管理页待开发 |
| user-10 | 用户点赞与评分 | ✅ | 点赞/取消点赞/评分 upsert/详情页交互，我点赞列表页待后续迭代 |
| user-11 | 用户浏览记录 | 🟡 待实现 | 表结构已就绪，前后端均未开始 |
| user-12 | 用户举报 | 🟡 部分实现 | 后端 3 接口已完成（提交/我的举报/详情），前端 ReportDialog 已完成，列表/详情页待开发 |
| user-13 | 用户操作日志 | 🟡 待实现 | 基于 `t_user_operation_log`，前后端均未开始 |
| user-14 | 用户设置 | 🟡 部分实现 | 后端 10 个核心接口已完成，前端 SettingsView 使用 mock 数据待对接 |
| user-15 | 用户主页 | ✅ | 贡献热力图/数据看板/技能雷达/公开收藏夹，CQRS 读模型已闭环 |

### 7.2 管理端 (19 个模块)

| # | 模块 | 状态 | 关键实现 |
|---|------|------|---------|
| admin-01 | 网站审核流 | ✅ | 待审核列表/审核通过-驳回/批量审核 |
| admin-02 | 网站内容治理 | ✅ | 网站列表/新增/编辑/上架/下架/删除 |
| admin-03 | 网站导入导出 | 🟡 待实现 | 未开始 |
| admin-04 | 分类管理 | ✅ | 分类 CRUD/排序/树形结构 |
| admin-05 | 标签管理 | ✅ | 标签 CRUD/颜色标记/使用计数 |
| admin-06 | 用户管理 | ✅ | 后端 6 接口（列表/详情/状态切换/重置密码/踢会话/统计），前端 UserManagementView 已就绪 |
| admin-07 | 举报处置 | ✅ | 后端 5 接口（列表/详情/单条处置/批量处置/统计），前端 AdminReportManagementView 已闭环 |
| admin-08 | 敏感词治理 | 🟡 待实现 | DFA 引擎已就绪，管理界面未开始 |
| admin-09 | 用户反馈处理 | ✅ | 后端 4 接口（列表/详情/回复/关闭），前端 AdminFeedbackManagementView 真实 API 已对接 |
| admin-10 | 公告管理 | ✅ | 公告 CRUD/置顶/发布状态 |
| admin-11 | 轮播管理 | 🟡 待实现 | 未开始 |
| admin-12 | 友情链接管理 | 🟡 待实现 | 未开始 |
| admin-13 | 系统配置中心 | 🟡 待实现 | 未开始 |
| admin-14 | 审计日志 | 🟡 部分实现 | AOP 切面已在多 Controller 生效写入 `t_audit_log`，前端 AuditLogManagementView 已创建，后端查询/详情/导出接口待实现 |
| admin-15 | 数据统计看板 | 🟡 待实现 | 未开始 |
| admin-16 | 用户收藏管理 | 🟡 待实现 | 未开始 |
| admin-17 | 用户操作日志 | ⛔ 未开始 | 基于 `t_user_operation_log`，全栈均待创建 |
| admin-18 | 用户评论管理 | ✅ | 后端 5 接口（分页/统计/详情/状态变更/批量），前端 AdminCommentManagementView 已闭环 |
| admin-19 | 技术栈管理 | ✅ | 后端 9 接口（CRUD/统计/图标上传删除/批量），前端 TechStackManagementView 已闭环 |

### 7.3 概要统计

| 维度 | 已完成 | 部分实现 | 待实现 | 未开始 | 已废弃 |
|------|--------|---------|--------|--------|--------|
| 用户端 (15) | 8 | 4 | 2 | 0 | 1 |
| 管理端 (19) | 10 | 1 | 7 | 1 | 0 |
| **合计 (34)** | **18** | **5** | **9** | **1** | **1** |

> 已完成闭环占比：**53%** (18/34)；若计入部分实现，前后端已有雏形的模块占比 **68%** (23/34)。

---

## 8. 项目目录结构

```
computer-favorites/
├── computer-favorites-back/          # 后端 Maven 多模块项目
│   ├── pom.xml                        # 父 POM（依赖版本管理）
│   ├── cf-core/                       # 启动模块
│   │   └── src/main/java/.../core/
│   │       └── ComputerFavoritesApplication.java
│   ├── cf-web/                        # Web 交互层
│   │   └── src/main/java/.../web/
│   │       ├── controller/            # Controller 层
│   │       │   ├── agent/             #   - AI Agent SSE 接口 (3 个)
│   │       │   ├── admin/             #   - 管理端接口 (11 个)
│   │       │   └── user/              #   - 用户端接口 (16 个)
│   │       └── config/                # SaTokenConfig / GlobalExceptionHandler
│   ├── cf-service/                    # 业务 + 中间件 + AI 引擎
│   │   └── src/main/java/.../service/
│   │       ├── aichat/                #   - AI Agent 核心引擎
│   │       │   ├── chat/              #     ChatOrchestrator 流式编排
│   │       │   ├── agent/             #     ReAct Agent 引擎
│   │       │   ├── tool/              #     ToolRegistry + Tool 实现
│   │       │   ├── skill/             #     SkillLoader 技能加载
│   │       │   ├── rag/               #     RagHandler 向量检索
│   │       │   ├── mcp/               #     MCP 服务集成
│   │       │   ├── memory/            #     长期记忆摘要
│   │       │   ├── quota/             #     配额管理
│   │       │   ├── hook/              #     钩子事件
│   │       │   └── config/            #     AI 相关配置
│   │       ├── user/                  #   用户端业务服务
│   │       ├── admin/                 #   管理端业务服务
│   │       ├── mapper/                #   MyBatis-Plus Mapper
│   │       ├── rabbitmq/              #   RabbitMQ 配置 + 消费者
│   │       ├── ratelimit/             #   Redis Lua 限流切面
│   │       └── config/redis/          #   RedisCache (Cache-Aside)
│   ├── cf-model/                      # 数据模型层
│   │   └── src/main/java/.../model/
│   │       ├── pojo/                  #   - 实体类 (30+)
│   │       ├── dto/                   #   - 数据传输对象 (70+)
│   │       ├── vo/                    #   - 视图对象 (90+)
│   │       ├── bo/                    #   - 业务对象
│   │       └── enums/                 #   - 枚举类
│   └── cf-common/                     # 公共基础层
│       └── src/main/java/.../common/
│           ├── constants/             #   常量定义
│           ├── utils/                 #   工具类
│           ├── exception/             #   异常类
│           └── web/                   #   HttpResult 统一响应
│
├── computer-favorites-web/            # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/                       # API 模块 (33 个文件)
│   │   ├── components/
│   │   │   ├── agent/                 #   - AI 对话组件 (9 个)
│   │   │   ├── admin/                 #   - 管理端组件 (70+ 个)
│   │   │   ├── user/                  #   - 用户端组件 (40+ 个)
│   │   │   ├── common/                #   - 通用组件
│   │   │   ├── auth/                  #   - 认证组件
│   │   │   └── ui-adapter/            #   - UI 适配层 (21 个 U* 组件)
│   │   ├── composables/               # 组合式函数 (16 个)
│   │   ├── stores/                    # Pinia 状态管理 (13 个)
│   │   ├── router/                    # 路由配置 (30 条)
│   │   ├── theme/                     # 双主题令牌系统
│   │   │   ├── user/                  #   - 用户端主题
│   │   │   └── admin/                 #   - 管理端主题
│   │   ├── types/                     # TypeScript 类型定义 (22 个)
│   │   ├── utils/                     # 工具函数
│   │   ├── i18n/                      # 国际化 (zh-CN / en-US)
│   │   ├── views/                     # 页面视图 (34 个)
│   │   │   ├── user/                  #   - 用户端 (18 个)
│   │   │   └── admin/                 #   - 管理端 (16 个)
│   │   └── layouts/                   # 布局组件
│   ├── vite.config.ts
│   └── package.json
│
├── docs/                              # 项目文档
│   ├── sql/                           #   - 数据库脚本 (建表/种子/补充/AI Agent DDL)
│   ├── spec-v2/                       #   - 模块规格与任务分解 (31 个模块)
│   ├── superpowers/                   #   - 设计规划文档
│   │   ├── plans/                     #     实现计划
│   │   └── specs/                     #     架构设计规格
│   ├── mem/                           #   - 开发经验记录
│   ├── img/                           #   - 项目截图
│   └── skills/                        #   - 技能存储库
│
├── .claude/                           # Claude Code 配置
│   ├── rules/                         #   - 编码规范 (6 个规则文件)
│   ├── skills/                        #   - 自定义技能
│   └── settings.json                  #   - 工作区设置
│
└── README.md
```

---

## 9. 部署说明

### 9.1 环境要求

| 组件 | 版本 | 必需 |
|------|------|------|
| JDK | 17+ | ✅ |
| Node.js | ^20.19.0 / >=22.12.0 | ✅ |
| MySQL | 8.x | ✅ |
| Redis | 7.x | ✅ |
| RabbitMQ | 3.x | 本地开发可选 |
| MinIO | 7.x | 本地开发可选 |
| Pinecone | — | AI Agent 功能必需 |

### 9.2 本地启动

#### 后端

```bash
# 1. 初始化数据库
mysql -u root -p < docs/sql/computer-favorites.sql
mysql -u root -p < docs/sql/database-补充脚本.sql
mysql -u root -p < docs/sql/ai-agent.sql
mysql -u root -p < docs/sql/init-data.sql

# 2. 启动后端 (local profile, 端口 18080, 鉴权关闭)
cd computer-favorites-back
mvn -pl cf-core spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# 或使用 dev profile (端口 18081, 鉴权开启)
mvn -pl cf-core spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### 前端

```bash
cd computer-favorites-web
npm install
npm run dev          # 启动开发服务器 (端口 5173, 代理 /api → 127.0.0.1:18080)
```

### 9.3 关键环境变量

后端配置文件使用 `application-{profile}.yml`。以下是 `application-dev.yml` 中需要配置的关键项：

| 配置项 | 说明 |
|--------|------|
| `spring.datasource.*` | MySQL 主从数据源 |
| `spring.data.redis.*` | Redis 连接 |
| `spring.rabbitmq.*` | RabbitMQ 连接 |
| `minio.*` | MinIO 对象存储 |
| `spring.ai.dashscope.*` | DashScope API Key |
| `spring.ai.vectorstore.pinecone.*` | Pinecone API Key + 环境 |
| `sa-token.*` | Sa-Token JWT 密钥 |
| `cf.auth.enable` | 鉴权开关 (local=false, dev=true) |

### 9.4 Docker 部署（规划中）

Docker Compose 配置尚未实现，规划包含：

- `mysql:8` + 初始化脚本挂载
- `redis:7-alpine`
- `rabbitmq:3-management-alpine`
- `minio/minio`
- 后端 Jar 镜像（多阶段构建）
- 前端 Nginx 镜像

---

## 10. 后续规划

以下规划基于 `docs/spec-v2/` 中 34 个模块的当前状态制定。

### 短期 — 当前迭代

| 优先级 | 模块 | 目标 |
|--------|------|------|
| P0 | user-07 AI 对话助手 | spec 中 P0-P8 全链路精细设计落地：柔性工具参数补全、多轮上下文增强、RAG 知识库自动同步 |
| P0 | user-09 用户评论 | 前端独立评论管理页 + 评论点赞/回复交互完善 |
| P0 | user-14 用户设置 | 前端 SettingsView 对接后端 10 个接口，替换 mock 数据 |
| P1 | user-12 用户举报 | MyReports 列表页 + ReportDetail 详情页 |
| P1 | admin-14 审计日志 | 后端查询/详情/导出接口 |
| P1 | admin-15 数据统计看板 | 前后端全链路 |

### 中期

| 模块 | 内容 |
|------|------|
| admin-03 网站导入导出 | 批量导入（JSON/CSV）+ 导出已审核网站 |
| admin-08 敏感词治理 | DFA 引擎管理界面：敏感词 CRUD + 分类（block/replace） + 测试工具 |
| admin-11 轮播管理 | 轮播图 CRUD + 排序 + 定时上下线 |
| admin-12 友情链接管理 | 友链 CRUD + 排序 + Logo 上传 |
| admin-13 系统配置中心 | 可视化配置编辑（当前只有 DB 种子数据） |
| admin-16 用户收藏管理 | 管理端查看/删除用户收藏 |
| user-11 用户浏览记录 | 浏览历史页面 + 自动清理策略 |
| user-13 用户操作日志 | 操作日志查询页面 |
| **AI 搜索增强** | 语义搜索 + 传统关键词搜索混合排序 |
| **个性化推荐引擎** | 基于 `t_user_tag_affinity` 偏好权重 + RAG 向量相似度混合推荐 |

### 长期

- **AI 收藏夹整理**：Agent 定期扫描用户收藏夹，自动去重、归类、补充标签和描述
- **AI 网站摘要自动生成**：Agent 抓取网站内容后自动生成中文摘要和技术栈分析
- **浏览器插件**：一键收藏当前页面，弹窗选择文件夹和标签
- **多端支持**：PWA 移动端适配，离线浏览已收藏网站列表
- **社区功能增强**：关注/粉丝体系、收藏夹评论、资源合集
- **Docker 一键部署**：完整容器化方案（mysql + redis + rabbitmq + minio + app + nginx）
- **CI/CD Pipeline**：GitHub Actions 自动构建 + 测试 + 部署
- **多语言完善**：英文界面全覆盖，新增更多语言
