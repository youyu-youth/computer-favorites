# AI Agent 对话界面 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为计算机专业个人网页收藏夹平台构建前端 AI 对话界面（用户端 + 管理端），包括 SSE 流式对话、会话管理、技能切换、思考过程展示等功能。

**Architecture:** 前端采用 Vue 3 Composition API + Pinia + Tailwind CSS 4 + PrimeVue unstyled。SSE 流式对话使用 fetch + ReadableStream 处理（EventSource 不支持 POST）。后端新增 6 个 REST 接口用于会话管理和配额查询。数据库 t_agent_session 表新增 `pinned` 字段。

**Tech Stack:** Vue 3 + TypeScript + Pinia + Vue Router 4 + Tailwind CSS 4 + PrimeVue 4 (unstyled) · Spring Boot 3.4.12 + MyBatis-Plus 3.5.14 + Sa-Token 1.44.0

---

## Phase 1: 数据库 + 后端会话管理 REST API

### Task 1: 数据库新增 pinned 字段

**Files:**
- Create: `docs/sql/agent-session-pinned.sql`

- [ ] **Step 1: 编写数据库迁移 SQL**

```sql
-- 为 t_agent_session 表增加置顶字段
ALTER TABLE t_agent_session ADD COLUMN pinned TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶 0否 1是' AFTER status;
```

验证: 执行后 `DESC t_agent_session` 应包含 pinned 字段。

- [ ] **Step 2: 执行迁移**

```bash
# 通过 MySQL 客户端执行（或使用项目的 SQL 执行方式）
mysql -u root -p computer_favorites < docs/sql/agent-session-pinned.sql
```

---

### Task 2: 更新 AgentSession 实体

**Files:**
- Modify: `cf-model/src/main/java/com/yyyouth/model/pojo/agent/AgentSession.java`

- [ ] **Step 1: 在 AgentSession 实体中新增 pinned 字段**

在 `status` 字段下方添加：

```java
/** 是否置顶 0否 1是 */
@TableField("pinned")
private Integer pinned;
```

---

### Task 3: 更新 AgentSessionVO

**Files:**
- Modify: `cf-model/src/main/java/com/yyyouth/model/vo/agent/AgentSessionVO.java`

- [ ] **Step 1: 在 VO 中新增 pinned 字段**

```java
/**
 * 是否置顶
 */
private Integer pinned;
```

同时增加缺少的 `ownerType` / `ownerId` 字段（非必需但有助于调试）：

```java
private String ownerType;
private Long ownerId;
```

更新后的完整 VO：

```java
@Builder
@Data
public class AgentSessionVO {
    private Long id;
    private String conversationId;
    private String title;
    private String skillCode;
    private String status;
    private Integer pinned;
    private String ownerType;
    private Long ownerId;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createTime;
}
```

---

### Task 4: 扩展 AgentSessionService

**Files:**
- Modify: `cf-service/src/main/java/com/yyyouth/service/aichat/session/AgentSessionService.java`

- [ ] **Step 1: 新增会话列表查询方法**

```java
/**
 * 查询指定用户/管理员的所有活跃会话列表
 *
 * @param ownerType 用户类型 user/admin
 * @param ownerId   用户ID
 * @param keyword   搜索关键词（可选，过滤 title）
 * @return 会话列表（置顶在前，按 lastMessageAt 倒序）
 */
public List<AgentSession> listByOwner(String ownerType, Long ownerId, String keyword) {
    LambdaQueryWrapper<AgentSession> wrapper = new LambdaQueryWrapper<AgentSession>()
            .eq(AgentSession::getOwnerType, ownerType)
            .eq(AgentSession::getOwnerId, ownerId)
            .eq(AgentSession::getStatus, "active")
            .orderByDesc(AgentSession::getPinned)
            .orderByDesc(AgentSession::getLastMessageAt);
    if (keyword != null && !keyword.isBlank()) {
        wrapper.like(AgentSession::getTitle, keyword);
    }
    return sessionMapper.selectList(wrapper);
}
```

- [ ] **Step 2: 新增重命名方法**

```java
/**
 * 重命名会话
 */
public void rename(Long sessionId, String title) {
    AgentSession session = new AgentSession();
    session.setId(sessionId);
    session.setTitle(title);
    sessionMapper.updateById(session);
}
```

- [ ] **Step 3: 新增删除方法（软删除）**

```java
/**
 * 软删除会话
 */
public void delete(Long sessionId) {
    sessionMapper.deleteById(sessionId);
}
```

- [ ] **Step 4: 新增置顶切换方法**

```java
/**
 * 切换会话置顶状态
 */
public void togglePin(Long sessionId, boolean pinned) {
    AgentSession session = new AgentSession();
    session.setId(sessionId);
    session.setPinned(pinned ? 1 : 0);
    sessionMapper.updateById(session);
}
```

- [ ] **Step 5: 新增按 ID 查询单个会话方法**

```java
/**
 * 按 ID 查询会话
 */
public AgentSession getById(Long sessionId) {
    return sessionMapper.selectById(sessionId);
}
```

- [ ] **Step 6: 保留现有 getOrCreate 和 archive 方法不动，不修改**

---

### Task 5: 扩展 UserAgentController

**Files:**
- Modify: `cf-web/src/main/java/com/yyyouth/web/controller/agent/UserAgentController.java`

- [ ] **Step 1: 新增依赖注入**

在现有 `private final ChatOrchestrator chatOrchestrator;` 下方新增：

```java
private final AgentSessionService sessionService;
private final QuotaGuard quotaGuard;
```

- [ ] **Step 2: 新增获取会话列表端点**

```java
/**
 * 获取当前用户的会话列表
 */
@GetMapping("/sessions")
public HttpResult getSessions(@RequestParam(required = false) String keyword) {
    Long userId = StpUtil.getLoginIdAsLong();
    List<AgentSession> sessions = sessionService.listByOwner("user", userId, keyword);
    List<AgentSessionVO> vos = sessions.stream().map(s -> AgentSessionVO.builder()
            .id(s.getId())
            .conversationId(s.getConversationId())
            .title(s.getTitle())
            .skillCode(s.getSkillCode())
            .status(s.getStatus())
            .pinned(s.getPinned())
            .ownerType(s.getOwnerType())
            .ownerId(s.getOwnerId())
            .lastMessageAt(s.getLastMessageAt())
            .createTime(s.getCreateTime())
            .build()).toList();
    return HttpResult.success(vos);
}
```

- [ ] **Step 3: 新增重命名会话端点**

```java
/**
 * 重命名会话
 */
@PutMapping("/sessions/{id}")
public HttpResult renameSession(@PathVariable Long id, @RequestBody @Valid Map<String, String> body) {
    String title = body.get("title");
    if (title == null || title.isBlank()) {
        return HttpResult.error("标题不能为空");
    }
    sessionService.rename(id, title);
    return HttpResult.success();
}
```

- [ ] **Step 4: 新增删除会话端点**

```java
/**
 * 删除会话
 */
@DeleteMapping("/sessions/{id}")
public HttpResult deleteSession(@PathVariable Long id) {
    sessionService.delete(id);
    return HttpResult.success();
}
```

- [ ] **Step 5: 新增置顶切换端点**

```java
/**
 * 切换会话置顶状态
 */
@PutMapping("/sessions/{id}/pin")
public HttpResult togglePin(@PathVariable Long id, @RequestBody @Valid Map<String, Boolean> body) {
    Boolean pinned = body.get("pinned");
    sessionService.togglePin(id, pinned != null && pinned);
    return HttpResult.success();
}
```

- [ ] **Step 6: 新增配额查询端点**

```java
/**
 * 查询当前用户配额
 */
@GetMapping("/quota")
public HttpResult getQuota() {
    Long userId = StpUtil.getLoginIdAsLong();
    AgentQuotaVO quota = quotaGuard.checkAndGetQuota("user", userId);
    return HttpResult.success(quota);
}
```

- [ ] **Step 7: 新增的单会话详情端点（可选，用于恢复会话时获取最新消息）**

```java
/**
 * 获取单会话详情
 */
@GetMapping("/sessions/{id}/detail")
public HttpResult getSessionDetail(@PathVariable Long id) {
    AgentSession session = sessionService.getById(id);
    if (session == null) {
        return HttpResult.error("会话不存在");
    }
    AgentSessionVO vo = AgentSessionVO.builder()
            .id(session.getId())
            .conversationId(session.getConversationId())
            .title(session.getTitle())
            .skillCode(session.getSkillCode())
            .status(session.getStatus())
            .pinned(session.getPinned())
            .ownerType(session.getOwnerType())
            .ownerId(session.getOwnerId())
            .lastMessageAt(session.getLastMessageAt())
            .createTime(session.getCreateTime())
            .build();
    return HttpResult.success(vo);
}
```

- [ ] **Step 8: 保留现有 chat 端点不变**

---

### Task 6: 扩展 AdminAgentController

**Files:**
- Modify: `cf-web/src/main/java/com/yyyouth/web/controller/agent/AdminAgentController.java`

- [ ] **Step 1: 新增依赖注入 + 同样的 6 个端点**

与 UserAgentController 完全相同的端点结构，区别为：
- 路径前缀为 `/api/admin/agent`
- ownerType = `"admin"`
- `StpUtil.getLoginIdAsLong()` → `StpAdminUtil.getLoginIdAsLong()`

完整新增代码（注入两个新依赖 + 6 个方法）：

```java
private final AgentSessionService sessionService;
private final QuotaGuard quotaGuard;

@GetMapping("/sessions")
public HttpResult getSessions(@RequestParam(required = false) String keyword) {
    Long adminId = StpAdminUtil.getLoginIdAsLong();
    List<AgentSession> sessions = sessionService.listByOwner("admin", adminId, keyword);
    List<AgentSessionVO> vos = sessions.stream().map(s -> AgentSessionVO.builder()
            .id(s.getId())
            .conversationId(s.getConversationId())
            .title(s.getTitle())
            .skillCode(s.getSkillCode())
            .status(s.getStatus())
            .pinned(s.getPinned())
            .ownerType(s.getOwnerType())
            .ownerId(s.getOwnerId())
            .lastMessageAt(s.getLastMessageAt())
            .createTime(s.getCreateTime())
            .build()).toList();
    return HttpResult.success(vos);
}

@PutMapping("/sessions/{id}")
public HttpResult renameSession(@PathVariable Long id, @RequestBody @Valid Map<String, String> body) {
    String title = body.get("title");
    if (title == null || title.isBlank()) {
        return HttpResult.error("标题不能为空");
    }
    sessionService.rename(id, title);
    return HttpResult.success();
}

@DeleteMapping("/sessions/{id}")
public HttpResult deleteSession(@PathVariable Long id) {
    sessionService.delete(id);
    return HttpResult.success();
}

@PutMapping("/sessions/{id}/pin")
public HttpResult togglePin(@PathVariable Long id, @RequestBody @Valid Map<String, Boolean> body) {
    Boolean pinned = body.get("pinned");
    sessionService.togglePin(id, pinned != null && pinned);
    return HttpResult.success();
}

@GetMapping("/quota")
public HttpResult getQuota() {
    Long adminId = StpAdminUtil.getLoginIdAsLong();
    AgentQuotaVO quota = quotaGuard.checkAndGetQuota("admin", adminId);
    return HttpResult.success(quota);
}

@GetMapping("/sessions/{id}/detail")
public HttpResult getSessionDetail(@PathVariable Long id) {
    AgentSession session = sessionService.getById(id);
    if (session == null) {
        return HttpResult.error("会话不存在");
    }
    AgentSessionVO vo = AgentSessionVO.builder()
            .id(session.getId())
            .conversationId(session.getConversationId())
            .title(session.getTitle())
            .skillCode(session.getSkillCode())
            .status(session.getStatus())
            .pinned(session.getPinned())
            .ownerType(session.getOwnerType())
            .ownerId(session.getOwnerId())
            .lastMessageAt(session.getLastMessageAt())
            .createTime(session.getCreateTime())
            .build();
    return HttpResult.success(vo);
}
```

- [ ] **Step 2: 保留现有 chat 端点不变**

---

### Task 7: Phase 1 编译验证

- [ ] **Step 1: 编译后端**

```bash
cd computer-favorites-back && mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

---

## Phase 2: 前端类型定义 + API 层

### Task 8: 创建 agent 类型定义

**Files:**
- Create: `src/types/agent.ts`

- [ ] **Step 1: 写入完整的 Agent 类型文件**

```typescript
// src/types/agent.ts

/** SSE 连接状态 */
export type ConnectionState =
  | 'idle'
  | 'connecting'
  | 'streaming'
  | 'done'
  | 'error'

/** 会话摘要（用于列表展示） */
export interface AgentSession {
  id: number
  conversationId: string
  title: string | null
  skillCode: string
  status: string
  pinned: number
  lastMessageAt: string | null
  createTime: string
}

/** SSE 思考步骤 */
export interface ThinkingStep {
  type: 'thinking' | 'tool_call' | 'tool_result'
  step?: number
  toolName?: string
  args?: string
  result?: string
  timestamp: number
}

/** 单条对话消息 */
export interface AgentMessage {
  id: string
  role: 'user' | 'assistant' | 'plan'
  content: string
  thinkingSteps: ThinkingStep[]
  thinkingCollapsed: boolean
  createdAt: string
  /** 仅 plan 消息使用 */
  plan?: AgentPlan
}

/** 执行计划（需确认的高风险操作） */
export interface AgentPlan {
  planId: string
  summary: string
  riskLevel: 'low' | 'medium' | 'high'
  status: 'wait_confirm' | 'approved' | 'rejected' | 'executed' | 'expired'
  expireTime?: string
}

/** 配额信息 */
export interface AgentQuota {
  dailyMessageLimit: number
  usedMessageCount: number
  remainingMessages: number
  dailyTokenLimit: number
  usedTokens: number
}

/** SSE 对话请求 */
export interface AgentChatRequest {
  conversationId?: string
  message: string
  skillCode: string
}

/** SSE 对话连接响应元信息 */
export interface AgentChatVO {
  conversationId: string
  sessionId: number
  skillName: string
}
```

---

### Task 9: 创建 agent API 层

**Files:**
- Create: `src/api/agent.ts`
- Modify: `src/api/index.ts`

- [ ] **Step 1: 创建 `src/api/agent.ts`**

```typescript
// src/api/agent.ts
import { getJson, putJson, deleteJson, postJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type {
  AgentSession,
  AgentQuota,
  AgentChatRequest,
} from '@/types/agent'

const BASE = '/api/agent'

/** 获取会话列表 */
export async function getSessions(keyword?: string): Promise<AgentSession[]> {
  const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''
  const res = await getJson<ApiResult<AgentSession[]>>(`${BASE}/sessions${params}`)
  if (res.code !== 200) throw new Error(res.msg || '获取会话列表失败')
  return res.data ?? []
}

/** 重命名会话 */
export async function renameSession(id: number, title: string): Promise<void> {
  const res = await putJson<ApiResult<null>>(`${BASE}/sessions/${id}`, { title })
  if (res.code !== 200) throw new Error(res.msg || '重命名失败')
}

/** 删除会话 */
export async function deleteSession(id: number): Promise<void> {
  const res = await deleteJson<ApiResult<null>>(`${BASE}/sessions/${id}`)
  if (res.code !== 200) throw new Error(res.msg || '删除失败')
}

/** 切换置顶 */
export async function togglePin(id: number, pinned: boolean): Promise<void> {
  const res = await putJson<ApiResult<null>>(`${BASE}/sessions/${id}/pin`, { pinned })
  if (res.code !== 200) throw new Error(res.msg || '操作失败')
}

/** 查询配额 */
export async function getQuota(): Promise<AgentQuota> {
  const res = await getJson<ApiResult<AgentQuota>>(`${BASE}/quota`)
  if (res.code !== 200) throw new Error(res.msg || '获取配额失败')
  if (!res.data) throw new Error('配额信息为空')
  return res.data
}

/** SSE 流式对话 — 返回 ReadableStream */
export function chatStream(
  request: AgentChatRequest,
): { stream: ReadableStream<Uint8Array>; abort: () => void } {
  const token = localStorage.getItem('accessToken')
  const tokenName = localStorage.getItem('tokenName') || 'satoken'
  const controller = new AbortController()

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (token) {
    headers[tokenName] = token
  }

  const responsePromise = fetch(`${BASE}/user/chat`, {
    method: 'POST',
    headers,
    body: JSON.stringify(request),
    signal: controller.signal,
  })

  // 工厂函数：返回 stream 和 abort
  let stream: ReadableStream<Uint8Array> | null = null

  const getStream = async (): Promise<ReadableStream<Uint8Array>> => {
    const response = await responsePromise
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }
    if (!response.body) {
      throw new Error('浏览器不支持 ReadableStream')
    }
    stream = response.body
    return stream
  }

  // 包装为同步获取的 stream（延迟解析）
  const lazyStream = new ReadableStream<Uint8Array>({
    async start(controller) {
      try {
        const realStream = await getStream()
        const reader = realStream.getReader()
        const pump = async () => {
          try {
            while (true) {
              const { done, value } = await reader.read()
              if (done) {
                controller.close()
                break
              }
              controller.enqueue(value)
            }
          } catch {
            controller.close()
          }
        }
        await pump()
      } catch (e) {
        controller.error(e)
      }
    },
  })

  return {
    stream: lazyStream,
    abort: () => controller.abort(),
  }
}
```

- [ ] **Step 2: 在 `src/api/index.ts` 末尾新增导出**

```typescript
export * from './agent'
```

---

## Phase 3: 前端 Store + Composable

### Task 10: 创建 agentChat Pinia Store

**Files:**
- Create: `src/stores/agentChat.ts`

- [ ] **Step 1: 写入完整的 store 文件**

```typescript
// src/stores/agentChat.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  AgentSession,
  AgentMessage,
  AgentQuota,
  AgentPlan,
  ThinkingStep,
  ConnectionState,
} from '@/types/agent'
import * as agentApi from '@/api/agent'

export const useAgentChatStore = defineStore('agentChat', () => {
  // === 会话列表 ===
  const sessions = ref<AgentSession[]>([])
  const sessionsLoading = ref(false)

  /** 加载会话列表 */
  const loadSessions = async (keyword?: string) => {
    sessionsLoading.value = true
    try {
      sessions.value = await agentApi.getSessions(keyword)
    } finally {
      sessionsLoading.value = false
    }
  }

  /** 新建会话后插入列表顶部 */
  const prependSession = (session: AgentSession) => {
    sessions.value.unshift(session)
  }

  /** 本地更新会话 */
  const updateSession = (id: number, patch: Partial<AgentSession>) => {
    const idx = sessions.value.findIndex((s) => s.id === id)
    if (idx !== -1) {
      sessions.value[idx] = { ...sessions.value[idx], ...patch }
    }
  }

  /** 从列表中移除 */
  const removeSession = (id: number) => {
    sessions.value = sessions.value.filter((s) => s.id !== id)
  }

  // === 当前对话 ===
  const currentSessionId = ref<number | null>(null)
  const currentConversationId = ref<string | null>(null)
  const currentSkillCode = ref<string>('general_assistant')
  const messages = ref<AgentMessage[]>([])
  const connectionState = ref<ConnectionState>('idle')

  // === 思考步骤（构建中） ===
  const pendingThinkingSteps = ref<ThinkingStep[]>([])

  /** 开始新对话 */
  const startNewChat = () => {
    currentSessionId.value = null
    currentConversationId.value = null
    messages.value = []
    pendingThinkingSteps.value = []
    connectionState.value = 'idle'
  }

  /** 切换到已有会话 */
  const switchToSession = (session: AgentSession) => {
    currentSessionId.value = session.id
    currentConversationId.value = session.conversationId
    currentSkillCode.value = session.skillCode || 'general_assistant'
    messages.value = []
    pendingThinkingSteps.value = []
    connectionState.value = 'idle'
  }

  /** 添加用户消息 */
  const addUserMessage = (content: string): string => {
    const id = `msg-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
    messages.value.push({
      id,
      role: 'user',
      content,
      thinkingSteps: [],
      thinkingCollapsed: true,
      createdAt: new Date().toISOString(),
    })
    return id
  }

  /** 创建 AI 回复占位 */
  const createAssistantMessage = (): string => {
    const id = `msg-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
    messages.value.push({
      id,
      role: 'assistant',
      content: '',
      thinkingSteps: [],
      thinkingCollapsed: true,
      createdAt: new Date().toISOString(),
    })
    pendingThinkingSteps.value = []
    return id
  }

  /** 追加 AI 回复文本 delta */
  const appendAssistantContent = (delta: string) => {
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      lastMsg.content += delta
    }
  }

  /** 追加思考步骤 */
  const addThinkingStep = (step: ThinkingStep) => {
    pendingThinkingSteps.value.push(step)
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      lastMsg.thinkingSteps = [...pendingThinkingSteps.value]
    }
  }

  /** 切换思考面板展开/折叠 */
  const toggleThinking = (msgId: string) => {
    const msg = messages.value.find((m) => m.id === msgId)
    if (msg) {
      msg.thinkingCollapsed = !msg.thinkingCollapsed
    }
  }

  /** 插入计划确认卡片 */
  const insertPlan = (plan: AgentPlan) => {
    messages.value.push({
      id: `plan-${plan.planId}`,
      role: 'plan',
      content: '',
      thinkingSteps: [],
      thinkingCollapsed: true,
      createdAt: new Date().toISOString(),
      plan,
    })
  }

  /** 更新消息内容（编辑用户消息后） */
  const updateMessageContent = (msgId: string, newContent: string) => {
    const msg = messages.value.find((m) => m.id === msgId)
    if (msg) {
      msg.content = newContent
    }
  }

  /** 点赞/踩 */
  const setMessageFeedback = (msgId: string, feedback: 'like' | 'dislike' | null) => {
    const msg = messages.value.find((m) => m.id === msgId)
    if (msg) {
      ;(msg as any).feedback = feedback
    }
  }

  // === 配额 ===
  const quota = ref<AgentQuota | null>(null)

  const loadQuota = async () => {
    try {
      quota.value = await agentApi.getQuota()
    } catch {
      quota.value = null
    }
  }

  // === 技能列表 ===
  const skills = ref<{ code: string; name: string }[]>([
    { code: 'general_assistant', name: '通用助手' },
    { code: 'code_assistant', name: '编程助手' },
    { code: 'website_assistant', name: '网站推荐' },
  ])

  const currentSkillName = computed(() => {
    const s = skills.value.find((s) => s.code === currentSkillCode.value)
    return s?.name ?? '通用助手'
  })

  // === 计算属性 ===
  const quotaRemaining = computed(() => {
    if (!quota.value) return null
    return quota.value.remainingMessages
  })

  const hasQuota = computed(() => {
    if (!quota.value) return true // 未加载时不阻断
    return quota.value.remainingMessages > 0
  })

  return {
    sessions,
    sessionsLoading,
    loadSessions,
    prependSession,
    updateSession,
    removeSession,
    currentSessionId,
    currentConversationId,
    currentSkillCode,
    messages,
    connectionState,
    pendingThinkingSteps,
    startNewChat,
    switchToSession,
    addUserMessage,
    createAssistantMessage,
    appendAssistantContent,
    addThinkingStep,
    toggleThinking,
    insertPlan,
    updateMessageContent,
    setMessageFeedback,
    quota,
    loadQuota,
    skills,
    currentSkillName,
    quotaRemaining,
    hasQuota,
  }
})
```

---

### Task 11: 创建 useAgentChat Composable

**Files:**
- Create: `src/composables/useAgentChat.ts`

- [ ] **Step 1: 写入 SSE 事件解析 + 连接生命周期管理**

```typescript
// src/composables/useAgentChat.ts
import { useAgentChatStore } from '@/stores/agentChat'
import type { ThinkingStep } from '@/types/agent'
import * as agentApi from '@/api/agent'
import { ref } from 'vue'

export function useAgentChat() {
  const store = useAgentChatStore()
  const abortController = ref<AbortController | null>(null)

  /** 解析单行 SSE data */
  function parseSSELine(line: string): { event: string; data: any } | null {
    if (!line.startsWith('data:')) return null

    const dataStr = line.slice(5).trim()
    if (!dataStr) return null

    try {
      const parsed = JSON.parse(dataStr)
      // 尝试从 data 中找到 event 类型
      // 后端格式: SseEmitter.event().name("message").data({delta: "xxx"})
      // SSE 标准: 先有 event: <name> 行, 后有 data: <json> 行
      return { event: '', data: parsed }
    } catch {
      return null
    }
  }

  /** SSE event 字段 → store 操作映射 */
  function handleSSEEvent(
    eventName: string,
    data: Record<string, any>,
    currentAssistantMsgId: string | null,
  ): { currentAssistantMsgId: string | null; shouldStop: boolean } {
    let msgId = currentAssistantMsgId
    let shouldStop = false

    switch (eventName) {
      case 'heartbeat':
        break

      case 'thinking': {
        const step: ThinkingStep = {
          type: 'thinking',
          step: data.step as number,
          timestamp: Date.now(),
        }
        store.addThinkingStep(step)
        break
      }

      case 'message':
        if (!msgId) {
          msgId = store.createAssistantMessage()
        }
        store.appendAssistantContent(data.delta as string)
        break

      case 'tool_call': {
        const step: ThinkingStep = {
          type: 'tool_call',
          toolName: data.toolName as string,
          args: data.args as string,
          timestamp: Date.now(),
        }
        store.addThinkingStep(step)
        break
      }

      case 'tool_result': {
        const step: ThinkingStep = {
          type: 'tool_result',
          toolName: data.toolName as string,
          result: data.result as string,
          timestamp: Date.now(),
        }
        store.addThinkingStep(step)
        break
      }

      case 'plan':
        store.insertPlan({
          planId: data.planId as string,
          summary: data.summary as string,
          riskLevel: (data.risk as 'low' | 'medium' | 'high') || 'medium',
          status: 'wait_confirm',
          expireTime: data.expireTime as string | undefined,
        })
        break

      case 'done':
        store.connectionState = 'done'
        shouldStop = true
        // 更新 sessionId
        if (data.sessionId) {
          store.currentSessionId = data.sessionId as number
        }
        break

      case 'error':
        store.connectionState = 'error'
        throw new Error((data.msg as string) || '对话出错')

      case 'quota_exceeded':
        store.connectionState = 'error'
        store.quota = {
          dailyMessageLimit: data.limit as number,
          usedMessageCount: data.used as number,
          remainingMessages: 0,
          dailyTokenLimit: store.quota?.dailyTokenLimit ?? 0,
          usedTokens: store.quota?.usedTokens ?? 0,
        }
        shouldStop = true
        break
    }

    return { currentAssistantMsgId: msgId, shouldStop }
  }

  /**
   * 发送消息并建立 SSE 连接
   */
  async function sendMessage(message: string): Promise<void> {
    if (!message.trim()) return
    if (!store.hasQuota) return

    store.connectionState = 'connecting'
    store.addUserMessage(message)

    try {
      const { stream, abort } = agentApi.chatStream({
        conversationId: store.currentConversationId || undefined,
        message,
        skillCode: store.currentSkillCode,
      })
      abortController.value = {
        abort: () => {
          try { abort() } catch { /* ignore */ }
        },
      } as AbortController

      const reader = stream.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let currentEventName = ''
      let assistantMsgId: string | null = null

      store.connectionState = 'streaming'

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })

        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const rawLine of lines) {
          const line = rawLine.trim()

          // 解析 event: <name> 行
          if (line.startsWith('event:')) {
            currentEventName = line.slice(6).trim()
            continue
          }

          // 解析 data: <json> 行
          if (line.startsWith('data:')) {
            const dataStr = line.slice(5).trim()
            if (!dataStr) continue

            try {
              const data = JSON.parse(dataStr)
              const result = handleSSEEvent(currentEventName, data, assistantMsgId)
              assistantMsgId = result.currentAssistantMsgId
              if (result.shouldStop) {
                reader.cancel()
                return
              }
            } catch (e) {
              // JSON 解析错误或 handleSSEEvent 抛出的错误
              store.connectionState = 'error'
              throw e
            }
          }
        }
      }

      // stream 正常结束
      if (store.connectionState === 'streaming') {
        store.connectionState = 'done'
      }
    } catch (e: any) {
      if (e.name === 'AbortError') {
        store.connectionState = 'idle'
        return
      }
      store.connectionState = 'error'
      throw e
    } finally {
      // 对话结束后刷新配额和会话列表
      store.loadQuota()
      store.loadSessions()
    }
  }

  /** 取消当前对话 */
  function cancelChat() {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
    }
    store.connectionState = 'idle'
  }

  /** 重新生成 AI 回复 */
  async function regenerate(message: string) {
    // 移除最后一条 assistant 消息
    if (store.messages.length > 0 && store.messages[store.messages.length - 1].role === 'assistant') {
      store.messages.pop()
    }
    await sendMessage(message)
  }

  /** 编辑消息后重新发送 */
  async function editAndResend(msgId: string, newContent: string) {
    // 找到该消息在列表中的位置，移除该消息及之后的所有消息
    const idx = store.messages.findIndex((m) => m.id === msgId)
    if (idx === -1) return

    // 保留之前的所有消息，移除从该消息开始的所有消息
    store.messages.splice(idx)
    // 重新发送新内容
    await sendMessage(newContent)
  }

  return {
    sendMessage,
    cancelChat,
    regenerate,
    editAndResend,
    isStreaming: () => store.connectionState === 'streaming',
  }
}
```

---

## Phase 4: 前端组件

### Task 12: AgentWelcomeScreen

**Files:**
- Create: `src/components/agent/AgentWelcomeScreen.vue`

- [ ] **Step 1: 写入欢迎页组件**

```vue
<template>
  <div class="flex flex-col items-center justify-center h-full text-center px-4">
    <div class="w-16 h-16 rounded-full bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center mb-4">
      <svg class="w-8 h-8 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.455 2.456L21.75 6l-1.036.259a3.375 3.375 0 00-2.455 2.456z" />
      </svg>
    </div>
    <h2 class="text-xl font-semibold text-gray-800 dark:text-gray-100 mb-2">
      有什么可以帮你的？
    </h2>
    <p class="text-sm text-gray-500 dark:text-gray-400 max-w-md">
      我是你的 CS 学习助手，可以帮你解答编程问题、分析代码、推荐学习资源。
    </p>

    <!-- 快捷入口 -->
    <div class="flex flex-wrap gap-2 mt-6 max-w-md justify-center">
      <button
        v-for="prompt in quickPrompts"
        :key="prompt"
        class="px-3 py-1.5 text-xs rounded-full border border-gray-200 dark:border-gray-700
               text-gray-600 dark:text-gray-300
               hover:border-amber-400 hover:text-amber-600 dark:hover:border-amber-500 dark:hover:text-amber-400
               transition-colors cursor-pointer"
        @click="$emit('quickPrompt', prompt)"
      >
        {{ prompt }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'AgentWelcomeScreen' })

defineEmits<{
  quickPrompt: [prompt: string]
}>()

const quickPrompts = [
  '解释时间复杂度 O(n log n)',
  'Git Flow 和 GitHub Flow 的区别',
  '如何优化 Java 代码性能',
  '推荐计算机学习路线',
]
</script>
```

---

### Task 13: AgentThinkingSteps

**Files:**
- Create: `src/components/agent/AgentThinkingSteps.vue`

- [ ] **Step 1: 写入思考步骤折叠面板组件**

```vue
<template>
  <div class="thinking-panel text-xs">
    <button
      class="flex items-center gap-1.5 px-2 py-1 rounded-md
             bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400
             hover:text-gray-700 dark:hover:text-gray-200 transition-colors cursor-pointer"
      @click="toggle"
    >
      <svg
        class="w-3 h-3 transition-transform"
        :class="{ 'rotate-90': !collapsed }"
        fill="none" stroke="currentColor" viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
      </svg>
      <span>
        思考过程 · {{ steps.length }} 步
        <template v-if="toolCallCount > 0">· 工具调用 {{ toolCallCount }} 次</template>
      </span>
    </button>

    <div v-if="!collapsed" class="mt-1.5 space-y-1 pl-4 border-l-2 border-gray-200 dark:border-gray-700">
      <div v-for="(step, idx) in steps" :key="idx" class="text-gray-500 dark:text-gray-400">
        <!-- thinking -->
        <div v-if="step.type === 'thinking'" class="flex items-start gap-1.5 py-0.5">
          <span class="text-amber-500 shrink-0 mt-0.5">🧠</span>
          <span>第{{ step.step }}步：分析中...</span>
        </div>

        <!-- tool_call -->
        <div v-else-if="step.type === 'tool_call'" class="flex items-start gap-1.5 py-0.5">
          <span class="text-sky-500 shrink-0 mt-0.5">🔧</span>
          <div>
            <span>调用工具: </span>
            <code class="font-mono text-[11px] bg-gray-100 dark:bg-gray-800 px-1 rounded">
              {{ step.toolName }}
            </code>
            <div v-if="step.args" class="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">
              {{ truncate(String(step.args), 120) }}
            </div>
          </div>
        </div>

        <!-- tool_result -->
        <div v-else-if="step.type === 'tool_result'" class="flex items-start gap-1.5 py-0.5">
          <span class="shrink-0 mt-0.5">{{ step.result ? '✅' : '❌' }}</span>
          <span class="text-gray-400 dark:text-gray-500">
            {{ truncate(String(step.result || '无结果'), 120) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ThinkingStep } from '@/types/agent'

defineOptions({ name: 'AgentThinkingSteps' })

const props = defineProps<{
  steps: ThinkingStep[]
}>()

const collapsed = ref(true)

function toggle() {
  collapsed.value = !collapsed.value
}

const toolCallCount = computed(() =>
  props.steps.filter((s) => s.type === 'tool_call').length,
)

function truncate(text: string, maxLen: number): string {
  if (text.length <= maxLen) return text
  return text.slice(0, maxLen) + '...'
}
</script>
```

---

### Task 14: AgentMessageBubble

**Files:**
- Create: `src/components/agent/AgentMessageBubble.vue`

- [ ] **Step 1: 写入消息气泡组件**

```vue
<template>
  <div
    class="message-bubble flex gap-3"
    :class="msg.role === 'user' ? 'flex-row-reverse' : ''"
  >
    <!-- 头像 -->
    <div class="shrink-0">
      <div
        v-if="msg.role === 'user'"
        class="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center"
      >
        <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0" />
        </svg>
      </div>
      <div
        v-else
        class="w-8 h-8 rounded-full bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center"
      >
        <svg class="w-4 h-4 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
        </svg>
      </div>
    </div>

    <!-- 消息体 -->
    <div class="max-w-[75%] md:max-w-[70%]">
      <!-- 思考过程（仅 AI 消息） -->
      <AgentThinkingSteps
        v-if="msg.role === 'assistant' && msg.thinkingSteps.length > 0"
        :steps="msg.thinkingSteps"
        class="mb-1.5"
      />

      <!-- 消息内容 -->
      <div
        v-if="msg.content"
        class="px-4 py-2.5 text-sm rounded-xl leading-relaxed whitespace-pre-wrap break-words"
        :class="msg.role === 'user'
          ? 'bg-amber-500 text-white rounded-br-md'
          : 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 rounded-bl-md'"
      >
        {{ msg.content }}
        <!-- 流式输出光标 -->
        <span
          v-if="isStreaming && msg.role === 'assistant'"
          class="inline-block w-0.5 h-4 bg-amber-500 animate-pulse ml-0.5 align-text-bottom"
        />
      </div>

      <!-- 操作栏（仅 AI 消息 + 非流式中） -->
      <div
        v-if="msg.role === 'assistant' && !isStreaming && msg.content"
        class="flex gap-3 mt-1.5 text-xs text-gray-400 dark:text-gray-500"
      >
        <button class="hover:text-gray-600 dark:hover:text-gray-300 transition-colors cursor-pointer" @click="copyContent">
          <span>复制</span>
        </button>
        <button class="hover:text-gray-600 dark:hover:text-gray-300 transition-colors cursor-pointer" @click="$emit('regenerate')">
          <span>重新生成</span>
        </button>
        <button
          class="transition-colors cursor-pointer"
          :class="feedback === 'like'
            ? 'text-green-500'
            : 'hover:text-gray-600 dark:hover:text-gray-300'"
          @click="toggleFeedback('like')"
        >
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 9V5a3 3 0 00-3-3l-4 9v11h11.28a2 2 0 002-1.7l1.38-9a2 2 0 00-2-2.3H14zM7 22H4a2 2 0 01-2-2v-7a2 2 0 012-2h3" />
          </svg>
        </button>
        <button
          class="transition-colors cursor-pointer"
          :class="feedback === 'dislike'
            ? 'text-red-500'
            : 'hover:text-gray-600 dark:hover:text-gray-300'"
          @click="toggleFeedback('dislike')"
        >
          <svg class="w-3.5 h-3.5 rotate-180" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 9V5a3 3 0 00-3-3l-4 9v11h11.28a2 2 0 002-1.7l1.38-9a2 2 0 00-2-2.3H14zM7 22H4a2 2 0 01-2-2v-7a2 2 0 012-2h3" />
          </svg>
        </button>
      </div>

      <!-- 编辑模式 -->
      <div v-if="isEditing" class="mt-2 flex gap-2">
        <textarea
          ref="editInputRef"
          v-model="editContent"
          class="flex-1 text-sm px-3 py-2 rounded-lg border border-gray-300 dark:border-gray-600
                 bg-white dark:bg-gray-900 text-gray-800 dark:text-gray-200 resize-none
                 focus:outline-none focus:border-amber-400"
          rows="2"
        />
        <div class="flex flex-col gap-1">
          <button
            class="px-3 py-1 text-xs rounded-md bg-amber-500 text-white hover:bg-amber-600 cursor-pointer"
            @click="confirmEdit"
          >
            发送
          </button>
          <button
            class="px-3 py-1 text-xs rounded-md border border-gray-300 dark:border-gray-600
                   text-gray-500 hover:text-gray-700 cursor-pointer"
            @click="cancelEdit"
          >
            取消
          </button>
        </div>
      </div>

      <!-- 编辑按钮（仅用户消息，非编辑模式） -->
      <div
        v-if="msg.role === 'user' && !isStreaming && !isEditing"
        class="flex justify-end mt-1"
      >
        <button
          class="text-xs text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300
                 transition-colors cursor-pointer"
          @click="startEdit"
        >
          编辑
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import type { AgentMessage } from '@/types/agent'
import AgentThinkingSteps from '@/components/agent/AgentThinkingSteps.vue'

defineOptions({ name: 'AgentMessageBubble' })

const props = defineProps<{
  msg: AgentMessage
  isStreaming: boolean
}>()

const emit = defineEmits<{
  regenerate: []
  editResend: [msgId: string, newContent: string]
}>()

// 复制
async function copyContent() {
  try {
    await navigator.clipboard.writeText(props.msg.content)
  } catch {
    // fallback
  }
}

// 点赞/踩
const feedback = ref<'like' | 'dislike' | null>(null)

function toggleFeedback(type: 'like' | 'dislike') {
  feedback.value = feedback.value === type ? null : type
}

// 编辑
const isEditing = ref(false)
const editContent = ref('')
const editInputRef = ref<HTMLTextAreaElement | null>(null)

function startEdit() {
  isEditing.value = true
  editContent.value = props.msg.content
  nextTick(() => {
    editInputRef.value?.focus()
  })
}

function cancelEdit() {
  isEditing.value = false
  editContent.value = ''
}

function confirmEdit() {
  if (!editContent.value.trim()) return
  emit('editResend', props.msg.id, editContent.value.trim())
  isEditing.value = false
}
</script>
```

---

### Task 15: AgentInputArea

**Files:**
- Create: `src/components/agent/AgentInputArea.vue`

- [ ] **Step 1: 写入输入区组件**

```vue
<template>
  <div class="border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-950 px-4 py-3">
    <!-- 技能选择 + 配额 -->
    <div class="flex items-center gap-2 mb-2 text-xs text-gray-500 dark:text-gray-400">
      <USelect
        v-model="selectedSkill"
        :options="skillOptions"
        option-label="name"
        option-value="code"
        class="w-auto"
        :pt="{ root: 'text-xs', input: 'text-xs py-1' }"
      />
      <span v-if="quotaRemaining !== null" class="ml-auto">
        配额 {{ quotaRemaining }}/{{ quotaTotal }}
      </span>
    </div>

    <!-- 输入框 -->
    <div class="flex items-end gap-2">
      <div class="flex-1 relative">
        <UTextarea
          v-model="inputText"
          :rows="1"
          auto-resize
          placeholder="输入问题，Enter 发送，Shift+Enter 换行..."
          class="w-full min-h-[40px] max-h-[160px] text-sm"
          :disabled="disabled"
          @keydown="handleKeydown"
        />
      </div>

      <!-- 按钮组 -->
      <div class="flex items-center gap-1 shrink-0">
        <UButton
          severity="secondary"
          size="small"
          @click="showCodeEditor = true"
          :disabled="disabled"
          title="插入代码"
        >
          { }
        </UButton>
        <UButton
          severity="secondary"
          size="small"
          @click="triggerFileUpload"
          :disabled="disabled"
          title="上传文件"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
          </svg>
        </UButton>
        <input
          ref="fileInputRef"
          type="file"
          accept=".java,.py,.js,.ts,.cpp,.c,.go,.rs,.txt,.md,.json,.xml,.yml,.yaml,image/*"
          class="hidden"
          @change="handleFileChange"
          multiple
        />
        <UButton
          :disabled="disabled || !inputText.trim()"
          @click="send"
        >
          发送
        </UButton>
      </div>
    </div>

    <!-- 已选文件提示 -->
    <div v-if="files.length > 0" class="flex gap-2 mt-2 flex-wrap">
      <div
        v-for="(file, idx) in files"
        :key="idx"
        class="flex items-center gap-1 px-2 py-0.5 text-xs rounded-md bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300"
      >
        <span class="max-w-[120px] truncate">{{ file.name }}</span>
        <button class="text-gray-400 hover:text-red-500 cursor-pointer" @click="removeFile(idx)">
          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 代码编辑器弹窗 -->
    <Teleport to="body">
      <AgentCodeEditor
        v-if="showCodeEditor"
        @close="showCodeEditor = false"
        @insert="insertCodeBlock"
      />
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { useAgentChatStore } from '@/stores/agentChat'
import AgentCodeEditor from '@/components/agent/AgentCodeEditor.vue'

defineOptions({ name: 'AgentInputArea' })

const emit = defineEmits<{
  send: [message: string, files: File[]]
}>()

const store = useAgentChatStore()

const inputText = ref('')
const files = ref<File[]>([])
const fileInputRef = ref<HTMLInputElement | null>(null)
const showCodeEditor = ref(false)

const selectedSkill = computed({
  get: () => store.currentSkillCode,
  set: (val: string) => { store.currentSkillCode = val },
})

const skillOptions = computed(() =>
  store.skills.map((s) => ({ code: s.code, name: s.name })),
)

const quotaRemaining = computed(() => store.quotaRemaining)
const quotaTotal = computed(() => store.quota?.dailyMessageLimit ?? 0)
const disabled = computed(() => store.connectionState === 'streaming')

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

function send() {
  if (!inputText.value.trim() && files.value.length === 0) return
  emit('send', inputText.value, [...files.value])
  inputText.value = ''
  files.value = []
}

function triggerFileUpload() {
  fileInputRef.value?.click()
}

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files) {
    files.value.push(...Array.from(target.files))
  }
  target.value = ''
}

function removeFile(idx: number) {
  files.value.splice(idx, 1)
}

function insertCodeBlock(code: string, language: string) {
  const langTag = language ? language : ''
  inputText.value += `\n\`\`\`${langTag}\n${code}\n\`\`\`\n`
  showCodeEditor.value = false
  nextTick(() => {
    const textarea = document.querySelector('textarea') as HTMLTextAreaElement
    textarea?.focus()
  })
}
</script>
```

---

### Task 16: AgentCodeEditor

**Files:**
- Create: `src/components/agent/AgentCodeEditor.vue`

- [ ] **Step 1: 写入代码编辑器弹窗组件**

```vue
<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click.self="$emit('close')">
    <div class="bg-white dark:bg-gray-900 rounded-xl shadow-xl w-full max-w-lg mx-4 max-h-[80vh] flex flex-col">
      <div class="flex items-center justify-between px-4 py-3 border-b border-gray-200 dark:border-gray-700">
        <h3 class="font-semibold text-sm text-gray-800 dark:text-gray-100">插入代码</h3>
        <button class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 cursor-pointer" @click="$emit('close')">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- 语言选择 -->
      <div class="px-4 pt-3">
        <USelect
          v-model="language"
          :options="languages"
          option-label="name"
          option-value="value"
          placeholder="选择语言"
          class="w-full"
        />
      </div>

      <!-- 代码输入 -->
      <div class="flex-1 px-4 py-3">
        <textarea
          ref="codeInputRef"
          v-model="code"
          class="w-full min-h-[200px] text-sm font-mono px-3 py-2 rounded-lg
                 border border-gray-300 dark:border-gray-600
                 bg-gray-50 dark:bg-gray-950 text-gray-800 dark:text-gray-200
                 resize-none focus:outline-none focus:border-amber-400"
          placeholder="在此粘贴代码..."
          spellcheck="false"
        />
      </div>

      <!-- 底部 -->
      <div class="px-4 py-3 border-t border-gray-200 dark:border-gray-700 flex justify-end gap-2">
        <UButton severity="secondary" size="small" @click="$emit('close')">取消</UButton>
        <UButton @click="insert" :disabled="!code.trim()">插入</UButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

defineOptions({ name: 'AgentCodeEditor' })

defineEmits<{
  close: []
  insert: [code: string, language: string]
}>()

const language = ref('java')
const code = ref('')
const codeInputRef = ref<HTMLTextAreaElement | null>(null)

const languages = [
  { name: 'Java', value: 'java' },
  { name: 'Python', value: 'python' },
  { name: 'JavaScript', value: 'javascript' },
  { name: 'TypeScript', value: 'typescript' },
  { name: 'C', value: 'c' },
  { name: 'C++', value: 'cpp' },
  { name: 'Go', value: 'go' },
  { name: 'Rust', value: 'rust' },
  { name: 'SQL', value: 'sql' },
  { name: 'Bash', value: 'bash' },
  { name: '无高亮', value: '' },
]

onMounted(() => {
  codeInputRef.value?.focus()
})
</script>
```

---

### Task 17: AgentPlanCard

**Files:**
- Create: `src/components/agent/AgentPlanCard.vue`

- [ ] **Step 1: 写入计划确认卡片组件**

```vue
<template>
  <div class="flex justify-center my-4">
    <div
      class="w-full max-w-md rounded-xl border overflow-hidden"
      :class="riskBorderClass"
    >
      <!-- 头部 -->
      <div class="px-4 py-2.5 flex items-center gap-2" :class="riskBgClass">
        <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
        </svg>
        <span class="text-sm font-semibold text-gray-900 dark:text-gray-100">
          执行计划确认
        </span>
        <span class="ml-auto text-xs px-1.5 py-0.5 rounded-full" :class="riskBadgeClass">
          {{ riskLabel }}
        </span>
      </div>

      <!-- 内容 -->
      <div class="px-4 py-3 bg-white dark:bg-gray-900">
        <p class="text-sm text-gray-700 dark:text-gray-300">{{ plan.summary }}</p>

        <!-- 按钮 -->
        <div class="flex gap-2 mt-3" v-if="plan.status === 'wait_confirm'">
          <UButton size="small" @click="$emit('reject', plan.planId)">
            拒绝
          </UButton>
          <UButton severity="success" size="small" @click="$emit('confirm', plan.planId)">
            确认执行
          </UButton>
        </div>
        <div v-else class="mt-2 text-xs text-gray-500">
          {{ plan.status === 'approved' ? '已确认执行' : plan.status === 'rejected' ? '已拒绝' : '' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AgentPlan } from '@/types/agent'

defineOptions({ name: 'AgentPlanCard' })

const props = defineProps<{
  plan: AgentPlan
}>()

defineEmits<{
  confirm: [planId: string]
  reject: [planId: string]
}>()

const riskLabel = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return '高风险'
    case 'medium': return '中风险'
    case 'low': return '低风险'
    default: return props.plan.riskLevel
  }
})

const riskBorderClass = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return 'border-red-300 dark:border-red-700'
    case 'medium': return 'border-amber-300 dark:border-amber-700'
    case 'low': return 'border-green-300 dark:border-green-700'
    default: return 'border-gray-300 dark:border-gray-700'
  }
})

const riskBgClass = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return 'bg-red-50 dark:bg-red-950/30'
    case 'medium': return 'bg-amber-50 dark:bg-amber-950/30'
    case 'low': return 'bg-green-50 dark:bg-green-950/30'
    default: return 'bg-gray-50 dark:bg-gray-800'
  }
})

const riskBadgeClass = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return 'bg-red-100 dark:bg-red-900/50 text-red-700 dark:text-red-400'
    case 'medium': return 'bg-amber-100 dark:bg-amber-900/50 text-amber-700 dark:text-amber-400'
    case 'low': return 'bg-green-100 dark:bg-green-900/50 text-green-700 dark:text-green-400'
    default: return 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400'
  }
})
</script>
```

---

### Task 18: AgentSessionItem

**Files:**
- Create: `src/components/agent/AgentSessionItem.vue`

- [ ] **Step 1: 写入会话列表项组件**

```vue
<template>
  <div
    class="group flex items-center gap-2 px-3 py-2.5 mx-1 rounded-lg cursor-pointer transition-colors text-sm"
    :class="isActive
      ? 'bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100'
      : 'text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800/50'"
    @click="$emit('select')"
  >
    <!-- 置顶图标 -->
    <svg
      v-if="session.pinned"
      class="w-3 h-3 shrink-0 text-amber-500"
      fill="currentColor" viewBox="0 0 24 24"
    >
      <path d="M16 9V4h1V2H7v2h1v5l-2 4v2h5v6l1 1 1-1v-6h5v-2l-2-4z" />
    </svg>

    <!-- 标题 / 编辑态 -->
    <input
      v-if="isRenaming"
      ref="renameInputRef"
      v-model="renameText"
      class="flex-1 text-sm px-1.5 py-0.5 rounded bg-white dark:bg-gray-900
             border border-amber-400 outline-none"
      @keydown.enter="confirmRename"
      @keydown.escape="cancelRename"
      @click.stop
    />
    <span
      v-else
      class="flex-1 truncate"
    >
      {{ session.title || '新对话' }}
    </span>

    <!-- 右键菜单触发器（桌面端悬停显示） -->
    <div class="hidden group-hover:flex items-center gap-0.5 shrink-0">
      <button
        v-if="session.pinned"
        class="p-0.5 text-amber-500 hover:text-amber-600 rounded cursor-pointer"
        title="取消置顶"
        @click.stop="$emit('togglePin', session.id, false)"
      >
        <svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 24 24">
          <path d="M16 9V4h1V2H7v2h1v5l-2 4v2h5v6l1 1 1-1v-6h5v-2l-2-4z" />
        </svg>
      </button>
      <button
        v-else
        class="p-0.5 text-gray-400 hover:text-amber-500 rounded cursor-pointer"
        title="置顶"
        @click.stop="$emit('togglePin', session.id, true)"
      >
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M8.25 15L12 18.75 15.75 15m-7.5-6L12 5.25 15.75 9" />
        </svg>
      </button>
      <button
        class="p-0.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 rounded cursor-pointer"
        title="重命名"
        @click.stop="startRename"
      >
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
        </svg>
      </button>
      <button
        class="p-0.5 text-gray-400 hover:text-red-500 rounded cursor-pointer"
        title="删除"
        @click.stop="$emit('delete', session.id)"
      >
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import type { AgentSession } from '@/types/agent'

defineOptions({ name: 'AgentSessionItem' })

defineProps<{
  session: AgentSession
  isActive: boolean
}>()

const emit = defineEmits<{
  select: []
  togglePin: [id: number, pinned: boolean]
  delete: [id: number]
  rename: [id: number, title: string]
}>()

const isRenaming = ref(false)
const renameText = ref('')
const renameInputRef = ref<HTMLInputElement | null>(null)

function startRename() {
  renameText.value = ''
  isRenaming.value = true
  nextTick(() => {
    renameInputRef.value?.focus()
  })
}

function confirmRename() {
  if (renameText.value.trim()) {
    emit('rename', 0, renameText.value.trim()) // id will be resolved by parent
  }
  isRenaming.value = false
}

function cancelRename() {
  isRenaming.value = false
}
</script>
```

---

### Task 19: AgentSessionList

**Files:**
- Create: `src/components/agent/AgentSessionList.vue`

- [ ] **Step 1: 写入会话列表组件**

```vue
<template>
  <div class="flex flex-col h-full bg-gray-50 dark:bg-gray-950">
    <!-- 新建按钮 -->
    <div class="px-4 py-3">
      <UButton class="w-full" @click="$emit('newChat')">
        + 新对话
      </UButton>
    </div>

    <!-- 搜索 -->
    <div class="px-4 pb-2">
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索会话..."
        class="w-full text-xs px-3 py-1.5 rounded-md border border-gray-200 dark:border-gray-700
               bg-white dark:bg-gray-900 text-gray-800 dark:text-gray-200
               outline-none focus:border-amber-400 transition-colors"
        @input="onSearchInput"
      />
    </div>

    <!-- 会话列表 -->
    <div class="flex-1 overflow-y-auto px-1">
      <div v-if="store.sessionsLoading" class="px-3 py-8 text-center text-sm text-gray-400">
        加载中...
      </div>
      <div v-else-if="store.sessions.length === 0" class="px-3 py-8 text-center text-sm text-gray-400">
        暂无对话
      </div>
      <AgentSessionItem
        v-for="session in store.sessions"
        :key="session.id"
        :session="session"
        :is-active="session.id === store.currentSessionId"
        @select="store.switchToSession(session)"
        @toggle-pin="handleTogglePin"
        @delete="handleDelete"
      />
    </div>

    <!-- 底部配额 -->
    <div v-if="store.quotaRemaining !== null" class="px-4 py-3 border-t border-gray-200 dark:border-gray-700">
      <p class="text-xs text-gray-500 dark:text-gray-400">
        今日配额: <span class="text-amber-500 font-semibold">{{ store.quotaRemaining }}</span>
        / {{ store.quota?.dailyMessageLimit ?? '-' }} 条消息
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAgentChatStore } from '@/stores/agentChat'
import { renameSession, deleteSession, togglePin } from '@/api/agent'
import AgentSessionItem from '@/components/agent/AgentSessionItem.vue'

defineOptions({ name: 'AgentSessionList' })

defineEmits<{
  newChat: []
}>()

const store = useAgentChatStore()
const searchKeyword = ref('')

let searchTimer: ReturnType<typeof setTimeout> | null = null

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    store.loadSessions(searchKeyword.value || undefined)
  }, 300)
}

async function handleTogglePin(id: number, pinned: boolean) {
  try {
    await togglePin(id, pinned)
    store.updateSession(id, { pinned: pinned ? 1 : 0 })
  } catch {
    // 操作失败静默处理
  }
}

async function handleDelete(id: number) {
  try {
    await deleteSession(id)
    store.removeSession(id)
    // 如果删除的是当前会话，启动新对话
    if (store.currentSessionId === id) {
      store.startNewChat()
    }
  } catch {
    // 操作失败静默处理
  }
}
</script>
```

---

### Task 20: AgentChatView

**Files:**
- Create: `src/components/agent/AgentChatView.vue`

- [ ] **Step 1: 写入对话主区域组件**

```vue
<template>
  <div class="flex flex-col h-full bg-white dark:bg-gray-950">
    <!-- 顶部栏 -->
    <div class="flex items-center gap-3 px-4 py-2.5 border-b border-gray-200 dark:border-gray-700 shrink-0">
      <!-- 移动端菜单按钮 -->
      <button
        class="md:hidden p-1 text-gray-500 hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer"
        @click="$emit('toggleSidebar')"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
        </svg>
      </button>

      <h2 class="text-sm font-semibold text-gray-800 dark:text-gray-100 truncate flex-1">
        {{ currentTitle }}
      </h2>
    </div>

    <!-- 消息列表 -->
    <div ref="messageListRef" class="flex-1 overflow-y-auto px-4 py-6 space-y-6">
      <AgentWelcomeScreen
        v-if="store.messages.length === 0"
        @quick-prompt="handleQuickPrompt"
      />

      <template v-for="msg in store.messages" :key="msg.id">
        <!-- 计划确认卡片 -->
        <AgentPlanCard
          v-if="msg.role === 'plan' && msg.plan"
          :plan="msg.plan"
          @confirm="handlePlanConfirm"
          @reject="handlePlanReject"
        />
        <!-- 普通消息 -->
        <AgentMessageBubble
          v-else
          :msg="msg"
          :is-streaming="store.connectionState === 'streaming' && msg.role === 'assistant' && msg === store.messages[store.messages.length - 1]"
          @regenerate="handleRegenerate"
          @edit-resend="handleEditResend"
        />
      </template>

      <!-- 加载/错误指示器 -->
      <div v-if="store.connectionState === 'connecting'" class="flex items-center gap-2 text-sm text-gray-400 pl-11">
        <div class="w-2 h-2 bg-amber-500 rounded-full animate-bounce" style="animation-delay: 0ms" />
        <div class="w-2 h-2 bg-amber-500 rounded-full animate-bounce" style="animation-delay: 150ms" />
        <div class="w-2 h-2 bg-amber-500 rounded-full animate-bounce" style="animation-delay: 300ms" />
      </div>

      <div v-if="store.connectionState === 'error'" class="text-center text-sm text-red-500 py-4">
        连接出错，请重试
      </div>

      <!-- 配额耗尽提示 -->
      <div v-if="!store.hasQuota && store.quotaRemaining === 0" class="text-center text-sm text-amber-500 py-4">
        今日对话配额已用完，请明天再来
      </div>
    </div>

    <!-- 输入区 -->
    <AgentInputArea
      @send="handleSend"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useAgentChatStore } from '@/stores/agentChat'
import { useAgentChat } from '@/composables/useAgentChat'
import { confirmPlan, rejectPlan } from '@/api/agent'
import AgentWelcomeScreen from '@/components/agent/AgentWelcomeScreen.vue'
import AgentMessageBubble from '@/components/agent/AgentMessageBubble.vue'
import AgentPlanCard from '@/components/agent/AgentPlanCard.vue'
import AgentInputArea from '@/components/agent/AgentInputArea.vue'

defineOptions({ name: 'AgentChatView' })

defineEmits<{
  toggleSidebar: []
}>()

const store = useAgentChatStore()
const { sendMessage, regenerate } = useAgentChat()

const messageListRef = ref<HTMLElement | null>(null)

// 当前标题
const currentTitle = computed(() => {
  const session = store.sessions.find((s) => s.id === store.currentSessionId)
  return session?.title || '新对话'
})

// 自动滚动到底部
watch(
  () => store.messages.length,
  () => { scrollToBottom() },
)
watch(
  () => store.messages[store.messages.length - 1]?.content,
  () => { scrollToBottom() },
)

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

async function handleSend(message: string, _files: File[]) {
  // 如果有文件，将文件名追加到消息中
  let fullMessage = message
  if (_files.length > 0) {
    const fileNames = _files.map((f) => f.name).join(', ')
    fullMessage = `[上传文件: ${fileNames}]\n${message}`
  }
  try {
    await sendMessage(fullMessage)
    await store.loadSessions()
  } catch (e) {
    console.error('发送消息失败', e)
  }
}

async function handleRegenerate() {
  const lastUserMsg = [...store.messages].reverse().find((m) => m.role === 'user')
  if (lastUserMsg) {
    await regenerate(lastUserMsg.content)
  }
}

async function handleEditResend(msgId: string, newContent: string) {
  const { editAndResend } = useAgentChat()
  await editAndResend(msgId, newContent)
}

function handleQuickPrompt(prompt: string) {
  handleSend(prompt, [])
}

async function handlePlanConfirm(planId: string) {
  try {
    await confirmPlan(planId)
  } catch {
    // silent
  }
}

async function handlePlanReject(planId: string) {
  try {
    await rejectPlan(planId)
  } catch {
    // silent
  }
}
</script>
```

---

## Phase 5: 视图 + 路由

### Task 21: AgentView（用户端）

**Files:**
- Create: `src/views/user/AgentView.vue`

- [ ] **Step 1: 写入用户端 AI 对话视图**

```vue
<template>
  <div class="flex h-[calc(100vh-4rem)]">
    <!-- 桌面端侧栏 -->
    <aside class="hidden md:flex flex-col w-72 shrink-0 border-r border-gray-200 dark:border-gray-700">
      <AgentSessionList @new-chat="handleNewChat" />
    </aside>

    <!-- 移动端抽屉 -->
    <Teleport to="body">
      <div
        v-if="mobileSidebarOpen"
        class="fixed inset-0 z-50 md:hidden"
        @click.self="mobileSidebarOpen = false"
      >
        <div class="absolute inset-0 bg-black/40" />
        <div class="absolute left-0 top-0 bottom-0 w-80 bg-white dark:bg-gray-950 shadow-xl">
          <AgentSessionList @new-chat="handleNewChat" />
        </div>
      </div>
    </Teleport>

    <!-- 对话区 -->
    <main class="flex-1 min-w-0">
      <AgentChatView @toggle-sidebar="mobileSidebarOpen = !mobileSidebarOpen" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAgentChatStore } from '@/stores/agentChat'
import AgentSessionList from '@/components/agent/AgentSessionList.vue'
import AgentChatView from '@/components/agent/AgentChatView.vue'

defineOptions({ name: 'AgentView' })

const store = useAgentChatStore()
const mobileSidebarOpen = ref(false)

onMounted(async () => {
  await Promise.all([store.loadSessions(), store.loadQuota()])
})

function handleNewChat() {
  store.startNewChat()
  mobileSidebarOpen.value = false
}
</script>
```

---

### Task 22: AdminAgentView（管理端）

**Files:**
- Create: `src/views/admin/AdminAgentView.vue`

- [ ] **Step 1: 写入管理端 AI 对话视图**

结构与 `AgentView.vue` 几乎相同，区别在于：
- API 调用使用管理端端点（`/api/admin/agent`）
- 需要使用 `buildAdminAuthHeaders()`

为此，需要在 `src/api/agent.ts` 中新增管理端专用 API 函数。先创建管理端 API 变体。

```typescript
// 在 src/api/agent.ts 中新增
import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'

const ADMIN_BASE = '/api/admin/agent'

export async function getAdminSessions(keyword?: string): Promise<AgentSession[]> {
  const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''
  const res = await getJson<ApiResult<AgentSession[]>>(
    `${ADMIN_BASE}/sessions${params}`,
    { headers: buildAdminAuthHeaders() },
  )
  if (res.code !== 200) throw new Error(res.msg || '获取会话列表失败')
  return res.data ?? []
}

// ... 同理 dispatch getAdminQuota, renameAdminSession, deleteAdminSession, toggleAdminPin
// 以及 chatAdminStream（SSE 请求也需 buildAdminAuthHeaders）
```

管理端视图 `src/views/admin/AdminAgentView.vue`：

```vue
<template>
  <div class="flex h-[calc(100vh-4rem)]">
    <aside class="hidden md:flex flex-col w-72 shrink-0 border-r border-gray-200 dark:border-gray-700">
      <AgentSessionList @new-chat="handleNewChat" />
    </aside>

    <Teleport to="body">
      <div
        v-if="mobileSidebarOpen"
        class="fixed inset-0 z-50 md:hidden"
        @click.self="mobileSidebarOpen = false"
      >
        <div class="absolute inset-0 bg-black/40" />
        <div class="absolute left-0 top-0 bottom-0 w-80 bg-white dark:bg-gray-950 shadow-xl">
          <AgentSessionList @new-chat="handleNewChat" />
        </div>
      </div>
    </Teleport>

    <main class="flex-1 min-w-0">
      <AgentChatView @toggle-sidebar="mobileSidebarOpen = !mobileSidebarOpen" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAgentChatStore } from '@/stores/agentChat'
import AgentSessionList from '@/components/agent/AgentSessionList.vue'
import AgentChatView from '@/components/agent/AgentChatView.vue'

defineOptions({ name: 'AdminAgentView' })

const store = useAgentChatStore()
const mobileSidebarOpen = ref(false)

onMounted(async () => {
  await Promise.all([store.loadSessions(), store.loadQuota()])
})

function handleNewChat() {
  store.startNewChat()
  mobileSidebarOpen.value = false
}
</script>
```

> **注意**: 管理端 store 需要区分用户端/管理端，通过 `ai-agent.ts` 中不同的 API 调用。在实际实现时，可创建 `src/stores/adminAgentChat.ts` 或通过 store 参数区分。

---

### Task 23: 更新路由

**Files:**
- Modify: `src/router/index.ts`

- [ ] **Step 1: 新增 AI 对话路由**

在路由数组的 `routes` 中，加入以下两个路由项：

```typescript
// 用户端 AI 对话页面
{
  path: '/computer/agent',
  name: 'agent',
  component: UserLayout,
  meta: {
    requiresAuth: true,
    title: 'AI 助手',
  },
  children: [
    {
      path: '',
      name: 'agent-chat',
      component: () => import('@/views/user/AgentView.vue'),
      meta: {
        requiresAuth: true,
        title: 'AI 助手',
      },
    },
  ],
},

// 管理端 AI 对话页面
{
  path: '/computer/admin/agent',
  name: 'admin-agent',
  component: AdminLayout,
  meta: {
    requiresAdminAuth: true,
    title: 'AI 管理助手',
  },
  children: [
    {
      path: '',
      name: 'admin-agent-chat',
      component: () => import('@/views/admin/AdminAgentView.vue'),
      meta: {
        requiresAdminAuth: true,
        title: 'AI 管理助手',
      },
    },
  ],
},
```

- [ ] **Step 2: 确保在 `beforeEach` 守卫中 theme scope 正确**

`/computer/agent` 路径不是 `/computer/admin/*`，所以 `resolveThemeScopeByPath()` 会自动分配 `'user'` 主题，无需额外修改。

---

## Phase 6: 导航入口 + 收尾

### Task 24: 导航栏添加 AI 入口

**Files:**
- Modify: `src/components/common/AppNavbar.vue`

- [ ] **Step 1: 在导航栏中添加"AI 助手"链接**

在 AppNavbar 的用户导航区域中添加：

```vue
<RouterLink
  to="/computer/agent"
  class="flex items-center gap-1.5 px-3 py-1.5 text-sm rounded-lg
         text-gray-600 dark:text-gray-300
         hover:text-amber-600 dark:hover:text-amber-400
         hover:bg-amber-50 dark:hover:bg-amber-900/20
         transition-colors cursor-pointer"
>
  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
      d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
  </svg>
  <span>AI 助手</span>
</RouterLink>
```

管理端导航 AI 入口（在 `AdminHeader.vue` 或 `AdminSidebarTree.vue` 中类似添加）。

---

### Task 25: 最终编译验证

- [ ] **Step 1: 前端类型检查**

```bash
cd computer-favorites-web
npm run type-check
```

Expected: 0 errors

- [ ] **Step 2: 前端 build**

```bash
npm run build
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 后端编译**

```bash
cd computer-favorites-back
mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

---

## 实施顺序建议

```
Phase 1:  DB migration → AgentSession entity → AgentSessionService → UserAgentController → AdminAgentController
Phase 2:  src/types/agent.ts → src/api/agent.ts → src/api/index.ts (export)
Phase 3:  src/stores/agentChat.ts → src/composables/useAgentChat.ts
Phase 4:  AgentWelcomeScreen → AgentThinkingSteps → AgentMessageBubble → AgentCodeEditor
          → AgentInputArea → AgentPlanCard → AgentSessionItem → AgentSessionList → AgentChatView
Phase 5:  AgentView → AdminAgentView → router update
Phase 6:  AppNavbar entry → final verification
```

每个 Phase 内的 Task 可以独立执行，Phase 之间需要按顺序推进。
