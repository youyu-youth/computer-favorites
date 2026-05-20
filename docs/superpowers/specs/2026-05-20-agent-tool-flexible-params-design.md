# AI Agent 工具参数柔性化 + 内联补全面板 — 设计文档

> 日期：2026-05-20 | 分支：feat/user-ai-agent-new-cc | 状态：设计中

## 问题描述

当前 `UserWebsiteTool` 的 6 个参数全部在 JSON Schema 的 `required` 数组中。LLM 没有完整参数时调用工具会触发 Spring AI `MethodToolCallbackProvider` 报错：

```
No @Tool annotated methods found in MethodToolCallback...
```

用户不知道应该给 Agent 提供哪些参数，体验很差。需要实现：LLM 能"缺啥问啥"，前端提供结构化辅助补全。

## 方案总览

**方案 C：灵活工具 + 内联补全面板（混合方案）**

三层改造：
1. 后端工具参数柔性化 — DTO + 全可选 + 智能推断 + 结构化返回协议
2. 前端内联补全面板 — 嵌在聊天流中的紧凑卡片，展示已收集/缺失/推荐
3. 补全重试流程 — 前端拼装完整消息，同一 conversationId 下 LLM 重新调用 tool
4. 分类/标签数据源 — `UserWebsiteTool` 内部查库 + 独立 `list_categories`/`list_tags` 工具

---

## 第一章：后端 — 工具参数柔性化

### 1.1 新建 WebsiteDraftDTO

位置：`cf-model/src/main/java/com/yyyouth/model/dto/agent/WebsiteDraftDTO.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteDraftDTO {
    /** 网站名称 */
    private String name;
    /** 网站URL */
    private String url;
    /** 网站图标（自动从 URL 推断） */
    private String icon;
    /** 一句话简介 */
    private String summary;
    /** 详细描述 */
    private String description;
    /** 分类ID */
    private Long categoryId;
    /** 标签名称（逗号分隔） */
    private String tags;
}
```

**关键：所有字段不加 `@NotNull`/`@NotBlank`**，Spring AI 生成的 JSON Schema 不包含 `required` 数组。

### 1.2 新建 ToolResult 协议类

位置：`cf-model/src/main/java/com/yyyouth/model/dto/agent/ToolResult.java`

```java
@Data
@Builder
public class ToolResult {
    /** "incomplete" | "submitted" */
    private String status;
    /** 已收集的字段 key→value */
    private Map<String, Object> collected;
    /** 缺失字段列表 */
    private List<MissingField> missing;
    /** 面向用户的提示文本 */
    private String message;

    @Data
    @Builder
    public static class MissingField {
        private String field;        // 字段名
        private String label;        // 中文标签
        private String type;         // "text" | "select"
        private List<Suggestion> suggestions; // LLM 推荐值

        @Data
        @Builder
        public static class Suggestion {
            private Object value;
            private String label;
        }
    }

    // 工厂方法
    public static ToolResult incomplete(...) { ... }
    public static ToolResult submitted(Long websiteId, String name, String url) { ... }
}
```

### 1.3 重写 UserWebsiteTool

位置：`cf-service/src/main/java/com/yyyouth/service/aichat/tool/UserWebsiteTool.java`

核心逻辑：

```java
@Tool(name = "user_submit_website", description = "提交网站投稿。用户提供网站信息后创建投稿记录，待管理员审核")
public String submitWebsiteDraft(
        @ToolParam(description = "网站投稿信息，尽可能从对话中提取已提供的信息")
        WebsiteDraftDTO dto) {

    // 1. 收集已填字段
    Map<String, Object> collected = collectNonNullFields(dto);

    // 2. 智能推断补全
    // - icon: url + "/favicon.ico"
    // - summary: 从 description 截取前 200 字
    // - categoryId: 关键词匹配
    // - tags: 从 name + description 提取技术关键词
    autoFill(dto);

    // 3. 检测必填字段（name, url 为硬必填）
    List<MissingField> missing = checkMissing(dto);

    if (!missing.isEmpty()) {
        // 查真实分类和标签数据填充 suggestions
        enrichWithRealData(missing);
        return ToolResult.incomplete(collected, missing, "已收到部分信息，还需补充")
            .toJson();
    }

    // 4. 全部齐全 → 执行提交
    UserWebsiteSubmissionCreateDTO createDTO = toCreateDTO(dto);
    Long websiteId = submissionService.submitWebsite(createDTO);
    return ToolResult.submitted(websiteId, dto.getName(), dto.getUrl()).toJson();
}
```

必填字段定义：`name`（硬必填）、`url`（硬必填）、`summary`（必填）、`description`（必填）、`categoryId`（必填）。`tags` 和 `icon` 为可选。

### 1.4 智能推断策略

| 缺失字段 | 推断方式 |
|---------|---------|
| icon | `url + "/favicon.ico"` |
| summary | 从 description 截取前 200 字符 |
| categoryId | 关键词匹配（"Vue"→前端框架，"Python"→后端…） |
| tags | 从 name + description 提取已知技术关键词 |

---

## 第二章：后端 — 分类/标签数据源

### 2.1 新建 DataLookupTool

位置：`cf-service/src/main/java/com/yyyouth/service/aichat/tool/DataLookupTool.java`

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLookupTool {

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Tool(name = "list_categories",
          description = "查询所有可用的网站分类列表，返回 id 和名称。投稿或浏览网站时可用来获取分类选项")
    public String listCategories() { ... }

    @Tool(name = "list_tags",
          description = "查询热门标签列表，返回标签 id 和名称。投稿或搜索网站时可使用")
    public String listTags() { ... }
}
```

### 2.2 两种接入方式的配合

**方式一 — 被动推荐：** `UserWebsiteTool` 内部注入 Mapper，在 `incomplete` 分支中查真实分类/标签数据构建 suggestion，前端补全卡片中的下拉和推荐直接可用。

**方式二 — 主动查询：** LLM 在对话中可以主动调用 `list_categories` / `list_tags`，提前了解可用选项，从而在追问用户时给出精准推荐。

```
用户："我想投稿一个网站"
LLM：调用 list_categories() 拿到真实分类
LLM："好的，现有分类：前端框架、后端框架、开发工具、设计资源... 你要投稿的网站属于哪类？"
用户："前端框架"
...
LLM：调用 user_submit_website，此时 categoryId 已确定
```

### 2.3 技能白名单配置

```sql
-- AgentToolDef 表新增
INSERT INTO t_agent_tool_def (tool_code, tool_name, description, risk_level, tool_type, enabled)
VALUES ('list_categories', '查询分类', '查询所有可用网站分类', 'low', 'native', 1),
       ('list_tags', '查询标签', '查询热门标签列表', 'low', 'native', 1);

-- 更新网站投稿技能的白名单
UPDATE t_agent_skill
SET tool_allowlist = 'user_submit_website,list_categories,list_tags'
WHERE skill_code = 'user_website_submit';
```

---

## 第三章：后端 — 工具结果穿透到前端

### 3.1 方案选择：为什么不用 Advisor

原方案使用 Spring AI Advisor 拦截工具返回值 → SSE 事件。但项目现有代码已明确记录 Reactor 线程中 `AgentSseContext`（ThreadLocal）不可用（见 `ChatOrchestrator.java` 第 160-163 行注释）。Advisor 方式面临同样的跨线程问题。

### 3.2 替代方案：LLM 系统 Prompt 标记 + 前端解析

**思路：** 不新增独立 SSE 事件。工具返回值经 LLM 自然语言处理后在文本中自然流转，前端从最终消息中解析结构化数据。

具体的，在技能的 system prompt 中追加指令：

```
当工具返回 {"status":"incomplete",...} 的 JSON 时，
你必须在回复末尾附上原始 JSON，格式为：
  <!--TOOL_DATA
  {"status":"incomplete","collected":{...},"missing":[...]}
  TOOL_DATA-->
```

LLM 看到 tool 返回的 incomplete JSON → LLM 生成友好的自然语言提示 → LLM 在末尾照抄 JSON（包裹在标记中）→ SSE 流式输出到前端 → 前端从完整消息中正则提取 `<!--TOOL_DATA...TOOL_DATA-->` → 展示补全卡片。

**为什么可靠：** LLM 对"照抄 JSON"的指令遵循度极高（比"生成特定格式"可靠得多），不需要额外的线程/advisor/SSE 通道。

### 3.3 前端解析逻辑

在 `agentChat store` 的消息组装过程中，从 assistant 消息完整文本中提取：

```typescript
// 在消息 streaming 完成后（connectionState 回到 idle）
const toolDataMatch = lastMsg.content.match(/<!--TOOL_DATA\n([\s\S]*?)\nTOOL_DATA-->/)
if (toolDataMatch) {
    const toolData = JSON.parse(toolDataMatch[1])
    lastMsg.toolIncomplete = toolData
    // 从显示文本中移除标记
    lastMsg.content = lastMsg.content.replace(/<!--TOOL_DATA[\s\S]*?TOOL_DATA-->/, '').trim()
}
```

### 3.4 ChatOrchestrator 改动

无需改动。工具调用和 SSE 流保持现状，LLM 在响应中自然携带结构化数据。

---

## 第四章：前端 — 内联补全面板

### 4.1 新增 SSE 事件类型

`src/stores/agentChat.ts` 的 SSE 事件解析增加：

```
case 'tool_incomplete':
    const data = JSON.parse(eventData);
    // 注入到最后一条 assistant 消息
    const lastMsg = messages.value[messages.value.length - 1];
    if (lastMsg && lastMsg.role === 'assistant') {
        lastMsg.toolIncomplete = data;
    }
    break;
```

### 4.2 类型扩展

`src/types/agent.ts`：

```typescript
export interface ToolIncompleteData {
  status: 'incomplete'
  collected: Record<string, any>
  missing: MissingField[]
  message: string
}

export interface MissingField {
  field: string
  label: string
  type: 'text' | 'select'
  suggestions: Suggestion[]
}

export interface Suggestion {
  value: any
  label: string
}
```

`AgentMessage` 类型新增字段 `toolIncomplete?: ToolIncompleteData`。

### 4.3 新建 AgentToolCompletionCard.vue

位置：`src/components/agent/AgentToolCompletionCard.vue`

UI 结构：

```
┌─────────────────────────────────────────────┐
│ 📋 待补充信息                                 │
│                                             │
│ ✅ 网站名称    Vue.js             (已收集)    │
│ ✅ 网站URL     https://vuejs.org   (已收集)   │
│                                             │
│ ⚠️ 分类       [▼ 前端框架 ▾]      (必选)     │
│     💡 推荐：前端框架 · 响应式框架             │
│                                             │
│ ⚠️ 一句话简介  [________________]  (必填)     │
│     💡 推荐：渐进式 JavaScript 框架            │
│                                             │
│ [补充并发送]  [让助手继续问]                   │
└─────────────────────────────────────────────┘
```

交互逻辑：

```typescript
// "补充并发送"：拼装完整自然语言消息，关闭卡片，emit 新消息
function submitCompletion() {
  const parts: string[] = []
  for (const [key, val] of Object.entries(collected)) {
    parts.push(`${fieldLabel(key)}：${val}`)
  }
  for (const field of missingFields) {
    if (field.userValue) {
      parts.push(`${field.label}：${field.userValue}`)
    }
  }
  const message = `请帮我把以下网站提交投稿：\n${parts.join('\n')}`
  emit('completion-submit', message)
}

// 推荐项点击 → 填入对应输入框
function applySuggestion(fieldName: string, value: any) { ... }
```

### 4.4 AgentMessageItem 渲染集成

在消息渲染组件中，检查 `message.toolIncomplete` 是否存在，存在则在消息气泡下方渲染 `<AgentToolCompletionCard>`。

---

## 第五章：补全重提交流程

```
用户首次输入 → ChatOrchestrator.handle()
  "投稿Vue.js，https://vuejs.org"
     ↓
LLM 调用 user_submit_website(name="Vue.js", url="https://vuejs.org")
  → tool 返回 incomplete JSON
  → ToolResultAdvisor 拦截 → SSE event: tool_incomplete
  → 前端展示补全卡片
     ↓
用户填写缺失字段，点击"补充并发送"
  → 前端拼装："请帮我把以下网站提交投稿：
             名称：Vue.js
             URL：https://vuejs.org
             分类ID：1
             简介：渐进式 JavaScript 框架
             标签：Vue,JavaScript,框架"
  → 发出 send(message) 事件
     ↓
ChatOrchestrator.handle()
  同一 conversationId ← 保持对话上下文，LLM 记得历史
  同一 skillCode ← 工具白名单不变
     ↓
LLM 看到完整信息 → 再次调用 user_submit_website(所有字段齐全)
  → tool 返回 submitted JSON
  → SSE 流式输出成功消息
```

**关键点：** 补全重试使用同一个 `conversationId`，LLM 通过 `ChatMemory` 看到之前的上下文，不会丢失信息。

---

## 改动文件清单

| # | 文件 | 操作 | 说明 |
|---|------|------|------|
| 1 | `cf-model/.../dto/agent/WebsiteDraftDTO.java` | 新建 | 投稿 DTO，全字段可选 |
| 2 | `cf-model/.../dto/agent/ToolResult.java` | 新建 | 工具返回协议类 |
| 3 | `cf-service/.../tool/UserWebsiteTool.java` | 重写 | 参数→DTO，incomplete/submitted 分支，智能推断 |
| 4 | `cf-service/.../tool/DataLookupTool.java` | 新建 | list_categories + list_tags |
| 5 | DB: `t_agent_tool_def` + `t_agent_skill` | 修改 | 新增工具定义，更新技能白名单 |
| 6 | `src/types/agent.ts` | 修改 | 新增 ToolIncompleteData/MissingField 类型 |
| 7 | `src/stores/agentChat.ts` | 修改 | 流完成后提取 `<!--TOOL_DATA-->` 标记 |
| 8 | `src/components/agent/AgentToolCompletionCard.vue` | 新建 | 内联补全卡片 |
| 9 | `src/components/agent/AgentMessageItem.vue` | 修改 | 消息下方渲染补全卡片 |

---

## 扩展性

该机制（tool 返回 `ToolResult.incomplete(...)` JSON → LLM 在 system prompt 指令下照抄 JSON → 前端解析 `<!--TOOL_DATA-->` 标记 → 展示补全卡片）是**通用机制**，不限于网站投稿。任何 tool 只要返回 `ToolResult.incomplete(...)` 格式，LLM 都会在 system prompt 指令下将其附在回复中，前端自动展示补全面板。后续新工具复用此机制，无需额外修改前端。

## 风险与备选

**风险：** LLM 可能不遵循 system prompt 中"照抄 JSON"的指令（概率低但非零）。
**备选：** 如果 LLM 漏掉标记，工具仍能正常工作——LLM 会基于 incomplete JSON 生成自然语言的追问，用户可以继续对话补充信息，只是不展示结构化补全卡片。降级体验可接受。
**长期方案：** 如果 Spring AI 后续版本支持非 ThreadLocal 的 advisor 参数传递，可迁移到独立 SSE 事件方案（更可靠但非必要）。
