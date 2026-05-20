# AI Agent 工具参数柔性化 + 内联补全面板 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `UserWebsiteTool` 从"6 个必填散列参数"改造为"单 DTO + 全可选 + 智能推断 + 内联补全卡片"，支持 LLM 缺参数时前端展示结构化补全面板。

**Architecture:** 后端：`UserWebsiteTool` 参数改为 `WebsiteDraftDTO` → 内部 incomplete/submitted 分支 → 返回 `ToolResult` JSON → `ToolResultCaptureWrapper` 捕获 incomplete 结果 → 通过 `PendingToolResultStore` 跨 Reactor 线程传递 → `done` SSE 事件携带。前端：`done` 事件解析 → 注入 assistant 消息的 `toolIncomplete` 字段 → `AgentToolCompletionCard` 渲染补全卡片。

**Tech Stack:** Spring AI 1.1.5 + MyBatis-Plus 3.5.14 + Vue 3 Composition API + TypeScript + Tailwind CSS 4 + PrimeVue 4 (unstyled)

---

### Task 1: 创建 WebsiteDraftDTO

**Files:**
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/dto/agent/WebsiteDraftDTO.java`

- [ ] **Step 1: 编写 WebsiteDraftDTO**

```java
package com.yyyouth.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yyyouth zg
 * @date 2026-05-20
 *
 * Agent 网站投稿工具参数 DTO，所有字段可选，LLM 按实际对话内容自由填充
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteDraftDTO {

    /** 网站名称 */
    private String name;

    /** 网站URL（必须以 http:// 或 https:// 开头） */
    private String url;

    /** 网站图标 */
    private String icon;

    /** 一句话简介（不超过 200 字） */
    private String summary;

    /** 详细描述 */
    private String description;

    /** 分类ID */
    private Long categoryId;

    /** 标签名称，逗号分隔 */
    private String tags;
}
```

- [ ] **Step 2: 编译验证**

```bash
cd computer-favorites-back && mvn -pl cf-model compile
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/dto/agent/WebsiteDraftDTO.java
git commit -m "feat: 新建 WebsiteDraftDTO — Agent 投稿工具全可选参数 DTO"
```

---

### Task 2: 创建 ToolResult 协议类

**Files:**
- Create: `computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/dto/agent/ToolResult.java`

- [ ] **Step 1: 编写 ToolResult**

```java
package com.yyyouth.model.dto.agent;

import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-05-20
 *
 * Agent 工具返回协议。status 为 incomplete 时前端展示补全卡片，
 * submitted 时表示操作已完成。
 */
@Data
@Builder
public class ToolResult {

    /** incomplete | submitted */
    private String status;

    /** 已收集的字段 key → value */
    private Map<String, Object> collected;

    /** 缺失字段列表 */
    private List<MissingField> missing;

    /** 面向用户的提示文本 */
    private String message;

    @Data
    @Builder
    public static class MissingField {
        /** 字段名（对应 DTO 属性） */
        private String field;
        /** 中文标签 */
        private String label;
        /** text | select */
        private String type;
        /** 是否为必填 */
        private boolean required;
        /** 推荐值列表 */
        private List<Suggestion> suggestions;
    }

    @Data
    @Builder
    public static class Suggestion {
        private Object value;
        private String label;
    }

    /** 工厂：参数不完整 */
    public static ToolResult incomplete(Map<String, Object> collected,
                                         List<MissingField> missing,
                                         String message) {
        return ToolResult.builder()
                .status("incomplete")
                .collected(collected)
                .missing(missing)
                .message(message)
                .build();
    }

    /** 工厂：提交成功 */
    public static ToolResult submitted(Long websiteId, String name, String url) {
        return ToolResult.builder()
                .status("submitted")
                .message(String.format("网站投稿已提交成功！\n网站ID：%d\n名称：%s\nURL：%s\n请等待管理员审核。",
                        websiteId, name, url))
                .build();
    }

    public String toJson() {
        return JSONUtil.toJsonStr(this);
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd computer-favorites-back && mvn -pl cf-model compile
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-model/src/main/java/com/yyyouth/model/dto/agent/ToolResult.java
git commit -m "feat: 新建 ToolResult — Agent 工具返回协议类"
```

---

### Task 3: 创建 PendingToolResultStore（跨 Reactor 线程传递）

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/PendingToolResultStore.java`

- [ ] **Step 1: 编写 PendingToolResultStore**

```java
package com.yyyouth.service.aichat.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yyyouth zg
 * @date 2026-05-20
 *
 * 工具 incomplete 结果暂存器。工具在 Reactor 线程中执行时写入，
 * ChatOrchestrator 在 doOnComplete 中读取并清除。
 * ConcurrentHashMap 提供跨线程 happens-before 保证。
 */
@Slf4j
@Component
public class PendingToolResultStore {

    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

    /** 存入 incomplete 结果 */
    public void put(String conversationId, String toolResultJson) {
        store.put(conversationId, toolResultJson);
        log.debug("PendingToolResultStore.put: conversationId={}", conversationId);
    }

    /** 取出并清除 */
    public String getAndClear(String conversationId) {
        String result = store.remove(conversationId);
        if (result != null) {
            log.debug("PendingToolResultStore.getAndClear: conversationId={}", conversationId);
        }
        return result;
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd computer-favorites-back && mvn -pl cf-service compile
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/PendingToolResultStore.java
git commit -m "feat: 新建 PendingToolResultStore — 跨 Reactor 线程传递 tool incomplete 结果"
```

---

### Task 4: 创建 ToolResultCaptureWrapper

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/ToolResultCaptureWrapper.java`

- [ ] **Step 1: 编写包装器**

```java
package com.yyyouth.service.aichat.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * @author yyyouth zg
 * @date 2026-05-20
 *
 * ToolCallback 包装器：拦截工具返回值，若为 incomplete 则暂存到 PendingToolResultStore。
 * 每次 ChatOrchestrator.orchestrate() 调用时新建实例（持有当前 conversationId）。
 */
@Slf4j
public class ToolResultCaptureWrapper implements ToolCallback {

    private final ToolCallback delegate;
    private final PendingToolResultStore store;
    private final String conversationId;

    public ToolResultCaptureWrapper(ToolCallback delegate,
                                    PendingToolResultStore store,
                                    String conversationId) {
        this.delegate = delegate;
        this.store = store;
        this.conversationId = conversationId;
    }

    @Override
    public String call(String toolInput) {
        String result = delegate.call(toolInput);
        if (result != null && result.contains("\"status\":\"incomplete\"")) {
            log.info("ToolResultCaptureWrapper: 捕获 incomplete 结果, tool={}, conversationId={}",
                    delegate.getToolDefinition().name(), conversationId);
            store.put(conversationId, result);
        }
        return result;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd computer-favorites-back && mvn -pl cf-service compile
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/ToolResultCaptureWrapper.java
git commit -m "feat: 新建 ToolResultCaptureWrapper — 拦截 tool incomplete 结果"
```

---

### Task 5: 更新 AgentStreamSink — done 事件携带 pendingToolResult

**Files:**
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/chat/AgentStreamSink.java:40-42`

- [ ] **Step 1: done 方法增加可选参数**

将第 40-46 行替换为：

```java
    public void done(Long sessionId, String conversationId) {
        done(sessionId, conversationId, null);
    }

    /** done 事件，可附带 pendingToolResult（incomplete 工具返回的 JSON） */
    public void done(Long sessionId, String conversationId, String pendingToolResult) {
        var data = new java.util.HashMap<String, Object>();
        data.put("sessionId", sessionId);
        data.put("conversationId", conversationId);
        if (pendingToolResult != null) {
            data.put("pendingToolResult", pendingToolResult);
        }
        send("done", data);
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
    }
```

- [ ] **Step 2: 编译验证**

```bash
cd computer-favorites-back && mvn -pl cf-service compile
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/chat/AgentStreamSink.java
git commit -m "feat: AgentStreamSink.done 增加可选 pendingToolResult 参数"
```

---

### Task 6: 重写 UserWebsiteTool

**Files:**
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/UserWebsiteTool.java`

- [ ] **Step 1: 完全重写 UserWebsiteTool**

```java
package com.yyyouth.service.aichat.tool;

import cn.hutool.core.util.StrUtil;
import com.yyyouth.model.dto.agent.ToolResult;
import com.yyyouth.model.dto.agent.WebsiteDraftDTO;
import com.yyyouth.model.dto.user.UserWebsiteSubmissionCreateDTO;
import com.yyyouth.model.pojo.website.Tag;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.TagMapper;
import com.yyyouth.service.user.website.UserWebsiteSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * 用户端网站投稿工具。接受单 DTO（全字段可选），
 * 参数不全时返回 incomplete JSON 由前端展示补全卡片，
 * 参数齐全时执行真实提交。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserWebsiteTool {

    private final UserWebsiteSubmissionService submissionService;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    /** 投稿必填字段 */
    private static final Set<String> REQUIRED_FIELDS = Set.of("name", "url", "summary", "description", "categoryId");

    @Tool(name = "user_submit_website", description = "提交网站投稿。用户提供网站信息后创建投稿记录，待管理员审核")
    public String submitWebsiteDraft(
            @ToolParam(description = "网站投稿信息，尽可能从对话中提取已提供的信息，不确定的字段可留空")
            WebsiteDraftDTO dto) {

        log.info("UserWebsiteTool.submitWebsiteDraft: name={}, url={}", dto.getName(), dto.getUrl());

        // 1. 智能推断补全
        autoFill(dto);

        // 2. 收集已填字段
        Map<String, Object> collected = new LinkedHashMap<>();
        if (StrUtil.isNotBlank(dto.getName())) collected.put("name", dto.getName());
        if (StrUtil.isNotBlank(dto.getUrl())) collected.put("url", dto.getUrl());
        if (StrUtil.isNotBlank(dto.getIcon())) collected.put("icon", dto.getIcon());
        if (StrUtil.isNotBlank(dto.getSummary())) collected.put("summary", dto.getSummary());
        if (StrUtil.isNotBlank(dto.getDescription())) collected.put("description", dto.getDescription());
        if (dto.getCategoryId() != null) collected.put("categoryId", dto.getCategoryId());
        if (StrUtil.isNotBlank(dto.getTags())) collected.put("tags", dto.getTags());

        // 3. 检测必填字段缺失
        List<ToolResult.MissingField> missing = new ArrayList<>();

        if (StrUtil.isBlank(dto.getName())) {
            missing.add(ToolResult.MissingField.builder()
                    .field("name").label("网站名称").type("text").required(true).build());
        }
        if (StrUtil.isBlank(dto.getUrl())) {
            missing.add(ToolResult.MissingField.builder()
                    .field("url").label("网站URL").type("text").required(true).build());
        }
        if (StrUtil.isBlank(dto.getSummary())) {
            String suggestion = null;
            if (StrUtil.isNotBlank(dto.getDescription())) {
                suggestion = StrUtil.sub(dto.getDescription(), 0, 200);
            }
            missing.add(ToolResult.MissingField.builder()
                    .field("summary").label("一句话简介").type("text").required(true)
                    .suggestions(suggestion != null
                            ? List.of(ToolResult.Suggestion.builder().value(suggestion).label("从描述自动截取").build())
                            : List.of())
                    .build());
        }
        if (StrUtil.isBlank(dto.getDescription())) {
            missing.add(ToolResult.MissingField.builder()
                    .field("description").label("详细描述").type("text").required(true).build());
        }
        if (dto.getCategoryId() == null) {
            List<ToolResult.Suggestion> catSuggestions = new ArrayList<>();
            List<WebsiteCategory> categories = categoryMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WebsiteCategory>()
                            .eq(WebsiteCategory::getStatus, 1)
                            .eq(WebsiteCategory::getDeleted, 0)
                            .orderByAsc(WebsiteCategory::getSort));
            for (WebsiteCategory cat : categories) {
                catSuggestions.add(ToolResult.Suggestion.builder()
                        .value(cat.getId()).label(cat.getName()).build());
            }
            missing.add(ToolResult.MissingField.builder()
                    .field("categoryId").label("分类").type("select").required(true)
                    .suggestions(catSuggestions).build());
        }
        if (StrUtil.isBlank(dto.getTags())) {
            List<ToolResult.Suggestion> tagSuggestions = new ArrayList<>();
            List<Tag> tags = tagMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
                            .eq(Tag::getDeleted, 0)
                            .orderByDesc(Tag::getUseCount)
                            .last("LIMIT 10"));
            for (Tag tag : tags) {
                tagSuggestions.add(ToolResult.Suggestion.builder()
                        .value(tag.getName()).label(tag.getName() + "（" + tag.getUseCount() + "次使用）").build());
            }
            missing.add(ToolResult.MissingField.builder()
                    .field("tags").label("标签（逗号分隔）").type("text").required(false)
                    .suggestions(tagSuggestions).build());
        }

        if (!missing.isEmpty()) {
            log.info("UserWebsiteTool: 参数不全, collected={}, missing={}", collected.size(), missing.size());
            return ToolResult.incomplete(collected, missing,
                    "已收到 " + collected.size() + " 项信息，还需补充 " + missing.size() + " 项").toJson();
        }

        // 4. 参数齐全 → 执行提交
        UserWebsiteSubmissionCreateDTO createDTO = new UserWebsiteSubmissionCreateDTO();
        createDTO.setName(dto.getName());
        createDTO.setUrl(dto.getUrl());
        createDTO.setIcon(dto.getUrl() + "/favicon.ico");
        createDTO.setSummary(dto.getSummary());
        createDTO.setDescription(dto.getDescription());
        createDTO.setCategoryId(dto.getCategoryId());
        createDTO.setTags(dto.getTags());

        Long websiteId = submissionService.submitWebsite(createDTO);
        log.info("网站投稿成功: websiteId={}, name={}", websiteId, dto.getName());

        return ToolResult.submitted(websiteId, dto.getName(), dto.getUrl()).toJson();
    }

    /** 智能推断补全可自动填充的字段 */
    private void autoFill(WebsiteDraftDTO dto) {
        // icon: 从 URL 推断
        if (StrUtil.isBlank(dto.getIcon()) && StrUtil.isNotBlank(dto.getUrl())) {
            dto.setIcon(dto.getUrl().replaceAll("/$", "") + "/favicon.ico");
        }
        // summary: 从 description 截取
        if (StrUtil.isBlank(dto.getSummary()) && StrUtil.isNotBlank(dto.getDescription())) {
            dto.setSummary(StrUtil.sub(dto.getDescription(), 0, 200));
        }
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd computer-favorites-back && mvn -pl cf-service compile
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/UserWebsiteTool.java
git commit -m "feat: UserWebsiteTool 重写 — DTO 参数 + incomplete/submitted 分支 + 智能推断 + 真实分类/标签推荐"
```

---

### Task 7: 创建 DataLookupTool

**Files:**
- Create: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/DataLookupTool.java`

- [ ] **Step 1: 编写 DataLookupTool**

```java
package com.yyyouth.service.aichat.tool;

import cn.hutool.json.JSONUtil;
import com.yyyouth.model.pojo.website.Tag;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author yyyouth zg
 * @date 2026-05-20
 *
 * 数据查询工具集。提供分类/标签等参考数据的查询，
 * LLM 可在对话中主动调用以获取真实数据库中的数据。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLookupTool {

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Tool(name = "list_categories",
          description = "查询所有可用的网站分类列表，返回分类ID和名称。投稿或浏览网站时可用来获取分类选项")
    public String listCategories() {
        List<WebsiteCategory> list = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WebsiteCategory>()
                        .eq(WebsiteCategory::getStatus, 1)
                        .eq(WebsiteCategory::getDeleted, 0)
                        .orderByAsc(WebsiteCategory::getSort));

        List<Map<String, Object>> result = new ArrayList<>();
        for (WebsiteCategory cat : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", cat.getId());
            item.put("name", cat.getName());
            if (cat.getDescription() != null) {
                item.put("description", cat.getDescription());
            }
            result.add(item);
        }
        log.info("list_categories: 返回 {} 个分类", result.size());
        return JSONUtil.toJsonStr(result);
    }

    @Tool(name = "list_tags",
          description = "查询热门标签列表，返回标签名称和使用次数。投稿或搜索网站时可使用")
    public String listTags() {
        List<Tag> list = tagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
                        .eq(Tag::getDeleted, 0)
                        .orderByDesc(Tag::getUseCount)
                        .last("LIMIT 30"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Tag tag : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", tag.getId());
            item.put("name", tag.getName());
            item.put("useCount", tag.getUseCount());
            result.add(item);
        }
        log.info("list_tags: 返回 {} 个标签", result.size());
        return JSONUtil.toJsonStr(result);
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd computer-favorites-back && mvn -pl cf-service compile
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/tool/DataLookupTool.java
git commit -m "feat: 新建 DataLookupTool — list_categories + list_tags 查询工具"
```

---

### Task 8: 更新 ChatOrchestrator — 包装工具 + 检查 pending 结果

**Files:**
- Modify: `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/chat/ChatOrchestrator.java`

- [ ] **Step 1: 注入 PendingToolResultStore**

在 `ChatOrchestrator` 类中新增依赖：

```java
// 在已有字段中添加：
private final PendingToolResultStore pendingToolResultStore;
```

确保 `import com.yyyouth.service.aichat.tool.PendingToolResultStore;` 和 `import com.yyyouth.service.aichat.tool.ToolResultCaptureWrapper;`

- [ ] **Step 2: 修改 orchestrate 方法**

将第 165-191 行替换为：

```java
    private void orchestrate(AgentStreamSink sink, SkillDefinition skill, String message,
                             ToolCallback[] tools, AgentSession session) {
        log.info("orchestrate 开始: sessionId={}, tools={}", session.getId(), tools.length);
        sink.thinking(1);

        String conversationId = session.getConversationId();
        ToolCallback[] capturingTools = new ToolCallback[tools.length];
        for (int i = 0; i < tools.length; i++) {
            capturingTools[i] = new ToolResultCaptureWrapper(
                    tools[i], pendingToolResultStore, conversationId);
        }

        dashscopeChatClient.prompt()
                .system(skill.getSystemPrompt() != null
                        ? skill.getSystemPrompt() : GeneralChatPrompt.SYSTEM_PROMPT)
                .user(message)
                .tools(capturingTools)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .doOnNext(delta -> {
                    log.debug("orchestrate delta: {}", delta);
                    sink.message(delta);
                })
                .doOnComplete(() -> {
                    log.info("orchestrate 完成: sessionId={}", session.getId());
                    String pendingResult = pendingToolResultStore.getAndClear(conversationId);
                    sink.done(session.getId(), conversationId, pendingResult);
                })
                .doOnError(e -> {
                    log.error("orchestrate 异常: sessionId={}", session.getId(), e);
                    pendingToolResultStore.getAndClear(conversationId);
                    sink.error("STREAM_ERROR", e.getMessage());
                })
                .subscribe();
    }
```

- [ ] **Step 3: 编译验证**

```bash
cd computer-favorites-back && mvn -pl cf-service compile
```

- [ ] **Step 4: 提交**

```bash
git add computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/aichat/chat/ChatOrchestrator.java
git commit -m "feat: ChatOrchestrator 集成 ToolResultCaptureWrapper + PendingToolResultStore"
```

---

### Task 9: 更新数据库 — 新增工具定义 + 更新技能白名单

**Files:**
- 数据修改：`t_agent_tool_def`、`t_agent_skill` 表

- [ ] **Step 1: 编写 SQL**

```sql
-- 新增两个工具定义（如已存在则跳过）
INSERT IGNORE INTO t_agent_tool_def (tool_code, tool_name, description, tool_type, audience, risk_level, enabled, create_time, update_time)
VALUES
('list_categories', '查询分类', '查询所有可用网站分类列表', 'native', 'user', 'low', 1, NOW(), NOW()),
('list_tags', '查询标签', '查询热门标签列表', 'native', 'user', 'low', 1, NOW(), NOW());

-- 更新网站投稿技能的工具白名单
UPDATE t_agent_skill
SET tool_allowlist_json = '["user_submit_website","list_categories","list_tags"]'
WHERE skill_code = 'user_website_submit';
```

- [ ] **Step 2: 提交**

```bash
git add docs/sql/
git commit -m "feat: 新增 list_categories/list_tags 工具定义 + 更新技能白名单"
```

---

### Task 10: 更新前端类型定义

**Files:**
- Modify: `computer-favorites-web/src/types/agent.ts`

- [ ] **Step 1: 新增类型**

在文件末尾追加：

```typescript
/** 工具 incomplete 数据（由 done 事件的 pendingToolResult 携带） */
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
  required: boolean
  suggestions: Suggestion[]
}

export interface Suggestion {
  value: any
  label: string
}
```

- [ ] **Step 2: 更新 AgentMessage 接口**

在第 32-43 行的 `AgentMessage` 接口中添加字段：

```typescript
export interface AgentMessage {
  // ... 所有现有字段保持不变 ...
  /** 工具 incomplete 补全数据（仅 assistant 消息） */
  toolIncomplete?: ToolIncompleteData
}
```

在 `feedback?: 'like' | 'dislike' | null` 行后添加 `toolIncomplete` 字段。

- [ ] **Step 3: 类型检查**

```bash
cd computer-favorites-web && npm run type-check
```

- [ ] **Step 4: 提交**

```bash
git add computer-favorites-web/src/types/agent.ts
git commit -m "feat: 新增 ToolIncompleteData/MissingField/Suggestion 类型 + AgentMessage.toolIncomplete"
```

---

### Task 11: 更新 useAgentChat — done 事件解析 pendingToolResult

**Files:**
- Modify: `computer-favorites-web/src/composables/useAgentChat.ts:74-84`

- [ ] **Step 1: 修改 done 事件处理**

将第 74-84 行的 `case 'done':` 块替换为：

```typescript
      case 'done':
        store.connectionState = 'done'
        shouldStop = true
        if (data.sessionId) {
          store.currentSessionId = data.sessionId as number
        }
        if (data.conversationId) {
          store.currentConversationId = data.conversationId as string
        }
        // 检查是否有 pending tool incomplete 数据
        if (data.pendingToolResult) {
          try {
            const toolData = typeof data.pendingToolResult === 'string'
              ? JSON.parse(data.pendingToolResult)
              : data.pendingToolResult
            if (toolData.status === 'incomplete' && msgId) {
              const lastMsg = store.messages[store.messages.length - 1]
              if (lastMsg && lastMsg.role === 'assistant') {
                lastMsg.toolIncomplete = toolData
              }
            }
          } catch { /* JSON 解析失败则忽略 */ }
        }
        break
```

注意 `msgId` 变量来自 `handleSSEEvent` 函数作用域，在 `done` case 中可以访问。

- [ ] **Step 2: 类型检查**

```bash
cd computer-favorites-web && npm run type-check
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-web/src/composables/useAgentChat.ts
git commit -m "feat: useAgentChat done 事件解析 pendingToolResult → message.toolIncomplete"
```

---

### Task 12: 创建 AgentToolCompletionCard 组件

**Files:**
- Create: `computer-favorites-web/src/components/agent/AgentToolCompletionCard.vue`

- [ ] **Step 1: 编写补全卡片组件**

```vue
<template>
  <div
    class="cf-tool-card mt-3 rounded-xl bg-white/90 dark:bg-stone-900/80 ring-1 ring-stone-200/70 dark:ring-stone-700/60 shadow-sm overflow-hidden"
  >
    <!-- 头部 -->
    <div class="flex items-center gap-2 px-4 py-2.5 border-b border-stone-100 dark:border-stone-800">
      <span class="text-xs text-stone-500 dark:text-stone-400 font-medium">
        {{ data.message }}
      </span>
    </div>

    <!-- 已收集字段 -->
    <div v-if="collectedEntries.length > 0" class="px-4 py-2">
      <div class="text-[10px] font-semibold uppercase tracking-wider text-stone-400 dark:text-stone-500 mb-1.5">
        已收集
      </div>
      <div class="space-y-1">
        <div
          v-for="[key, val] in collectedEntries"
          :key="key"
          class="flex items-center gap-2 text-xs"
        >
          <CheckCircle class="text-emerald-500 shrink-0" :size="12" :stroke-width="2" />
          <span class="text-stone-500 dark:text-stone-400 w-20 shrink-0">{{ fieldLabel(key) }}</span>
          <span class="text-stone-800 dark:text-stone-200 truncate">{{ val }}</span>
        </div>
      </div>
    </div>

    <!-- 缺失字段 -->
    <div class="px-4 py-2">
      <div class="text-[10px] font-semibold uppercase tracking-wider text-amber-600 dark:text-amber-400 mb-1.5">
        待补充
      </div>
      <div class="space-y-2.5">
        <div v-for="field in data.missing" :key="field.field">
          <label class="block text-xs text-stone-500 dark:text-stone-400 mb-1">
            {{ field.label }}
            <span v-if="field.required" class="text-rose-500">*</span>
            <span v-else class="text-stone-400">(可选)</span>
          </label>

          <!-- select 类型 → 下拉 -->
          <select
            v-if="field.type === 'select'"
            v-model="userValues[field.field]"
            class="w-full rounded-lg px-3 py-1.5 text-xs bg-stone-50 dark:bg-stone-800 ring-1 ring-stone-200 dark:ring-stone-700 text-stone-800 dark:text-stone-100 outline-none focus:ring-2 focus:ring-primary-400 cursor-pointer"
          >
            <option value="" disabled>请选择</option>
            <option
              v-for="s in field.suggestions"
              :key="s.value"
              :value="s.value"
            >
              {{ s.label }}
            </option>
          </select>

          <!-- text 类型 → 输入框 -->
          <input
            v-else
            v-model="userValues[field.field]"
            type="text"
            class="w-full rounded-lg px-3 py-1.5 text-xs bg-stone-50 dark:bg-stone-800 ring-1 ring-stone-200 dark:ring-stone-700 text-stone-800 dark:text-stone-100 outline-none focus:ring-2 focus:ring-primary-400"
            :placeholder="'输入' + field.label"
          />

          <!-- 推荐项 -->
          <div v-if="field.suggestions.length > 0" class="flex flex-wrap gap-1 mt-1">
            <span class="text-[10px] text-stone-400 dark:text-stone-500">推荐：</span>
            <button
              v-for="s in field.suggestions"
              :key="s.value"
              type="button"
              class="text-[10px] px-1.5 py-0.5 rounded-full bg-stone-100 dark:bg-stone-800 text-stone-600 dark:text-stone-300 hover:bg-primary-500/10 hover:text-primary-600 dark:hover:text-primary-400 transition-colors cursor-pointer"
              @click="applySuggestion(field.field, s.value, field.type)"
            >
              {{ s.label }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="flex items-center gap-2 px-4 py-2.5 border-t border-stone-100 dark:border-stone-800">
      <button
        type="button"
        class="cf-tool-submit-btn inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-white bg-primary-500 hover:bg-primary-600 shadow-sm transition-colors cursor-pointer"
        :disabled="!hasRequiredFields"
        @click="submitCompletion"
      >
        <Send :size="12" :stroke-width="2" />
        补充并发送
      </button>
      <button
        type="button"
        class="text-xs text-stone-400 dark:text-stone-500 hover:text-stone-600 dark:hover:text-stone-300 transition-colors cursor-pointer"
        @click="$emit('close')"
      >
        让助手继续问
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { CheckCircle, Send } from 'lucide-vue-next'
import type { ToolIncompleteData } from '@/types/agent'

defineOptions({ name: 'AgentToolCompletionCard' })

const props = defineProps<{
  data: ToolIncompleteData
}>()

const emit = defineEmits<{
  submit: [message: string]
  close: []
}>()

const userValues = ref<Record<string, any>>({})

/** 已收集字段转为 entries */
const collectedEntries = computed(() => Object.entries(props.data.collected))

/** 是否所有必填字段已填写 */
const hasRequiredFields = computed(() => {
  return props.data.missing
    .filter((f) => f.required)
    .every((f) => userValues.value[f.field])
})

/** 字段名 → 中文标签 */
const fieldLabels: Record<string, string> = {
  name: '网站名称',
  url: '网站URL',
  icon: '图标',
  summary: '简介',
  description: '描述',
  categoryId: '分类',
  tags: '标签',
}

function fieldLabel(key: string): string {
  return fieldLabels[key] || key
}

/** 点击推荐项 → 填入 */
function applySuggestion(fieldName: string, value: any, type: string) {
  if (type === 'select') {
    userValues.value[fieldName] = value
  } else {
    userValues.value[fieldName] = value
  }
}

/** 拼装完整消息并发送 */
function submitCompletion() {
  const parts: string[] = []
  // 已收集字段
  for (const [key, val] of collectedEntries.value) {
    parts.push(`${fieldLabel(key)}：${val}`)
  }
  // 用户新填字段
  for (const field of props.data.missing) {
    if (userValues.value[field.field]) {
      parts.push(`${field.label}：${userValues.value[field.field]}`)
    }
  }
  const message = `请帮我把以下网站提交投稿：\n${parts.join('\n')}`
  emit('submit', message)
}
</script>

<style scoped>
.cf-tool-submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
```

- [ ] **Step 2: 类型检查**

```bash
cd computer-favorites-web && npm run type-check
```

- [ ] **Step 3: 提交**

```bash
git add computer-favorites-web/src/components/agent/AgentToolCompletionCard.vue
git commit -m "feat: 新建 AgentToolCompletionCard — 内联补全卡片组件"
```

---

### Task 13: 更新 AgentMessageBubble — 渲染补全卡片

**Files:**
- Modify: `computer-favorites-web/src/components/agent/AgentMessageBubble.vue`

- [ ] **Step 1: 模板中插入补全卡片**

在第 27 行 `MarkdownRender` 关闭标签之后（`/>` 后），action bar 之前插入：

```vue
      <!-- 工具补全卡片 -->
      <AgentToolCompletionCard
        v-if="msg.role === 'assistant' && msg.toolIncomplete && !isStreaming"
        :data="msg.toolIncomplete"
        class="mt-2"
        @submit="handleCompletionSubmit"
        @close="handleCompletionClose"
      />
```

- [ ] **Step 2: 导入组件**

在 `<script setup>` 的 import 块中添加：

```typescript
import AgentToolCompletionCard from '@/components/agent/AgentToolCompletionCard.vue'
```

- [ ] **Step 3: 新增 emit 和处理函数**

在 emit 定义中添加：

```typescript
const emit = defineEmits<{
  regenerate: []
  editResend: [msgId: string, newContent: string]
  completionSubmit: [message: string]
}>()
```

添加处理函数：

```typescript
function handleCompletionSubmit(message: string) {
  emit('completionSubmit', message)
}

function handleCompletionClose() {
  if (props.msg.toolIncomplete) {
    props.msg.toolIncomplete = undefined
  }
}
```

- [ ] **Step 4: 类型检查**

```bash
cd computer-favorites-web && npm run type-check
```

- [ ] **Step 5: 提交**

```bash
git add computer-favorites-web/src/components/agent/AgentMessageBubble.vue
git commit -m "feat: AgentMessageBubble 集成 AgentToolCompletionCard 渲染"
```

---

### Task 14: 更新 AgentChatUserView — 转发 completionSubmit 事件

**Files:**
- Modify: `computer-favorites-web/src/views/user/AgentChatUserView.vue:73-83`

- [ ] **Step 1: 在 AgentMessageBubble 上绑定新事件**

在第 73-83 行的 `<AgentMessageBubble>` 上添加 `@completion-submit` 事件：

```vue
            <AgentMessageBubble
              v-else
              :msg="msg"
              :is-streaming="..."
              @regenerate="handleRegenerate"
              @edit-resend="handleEditResend"
              @completion-submit="handleSend($event, [])"
            />
```

- [ ] **Step 2: 提交**

```bash
git add computer-favorites-web/src/views/user/AgentChatUserView.vue
git commit -m "feat: AgentChatUserView 转发 completionSubmit → handleSend"
```

---

### Task 15: 端到端验证

- [ ] **Step 1: 启动后端（local profile，无需鉴权）**

```bash
cd computer-favorites-back && mvn -pl cf-core spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

- [ ] **Step 2: 启动前端**

```bash
cd computer-favorites-web && npm run dev
```

- [ ] **Step 3: 测试场景 1 — 缺参数触发补全卡片**

在 AI 助手页面发送消息："帮我投稿一个网站，叫 Vue.js 官网"

验证：
- LLM 调用 `user_submit_website` tool（带部分参数 name="Vue.js官网", url="https://vuejs.org"）
- Tool 返回 incomplete JSON
- SSE 流结束后前端展示补全卡片
- 卡片显示已收集的 name 和 url
- 卡片显示缺失的 categoryId（下拉含真实分类）、summary、description、tags
- 推荐项可点击填充

- [ ] **Step 4: 测试场景 2 — 补全并提交**

在补全卡片中填写缺失字段，点击"补充并发送"

验证：
- 前端发出包含完整信息的消息
- LLM 再次调用 tool，参数齐全
- Tool 返回 submitted JSON
- SSE 流式输出成功消息

- [ ] **Step 5: 测试场景 3 — 数据查询工具**

发送消息："现在有哪些分类和标签可以用？"

验证：
- LLM 调用 `list_categories` / `list_tags`
- 返回真实数据库中的分类和标签列表
```

---

## 改动文件汇总

| # | 文件 | 操作 |
|---|------|------|
| 1 | `cf-model/.../dto/agent/WebsiteDraftDTO.java` | 新建 |
| 2 | `cf-model/.../dto/agent/ToolResult.java` | 新建 |
| 3 | `cf-service/.../tool/PendingToolResultStore.java` | 新建 |
| 4 | `cf-service/.../tool/ToolResultCaptureWrapper.java` | 新建 |
| 5 | `cf-service/.../chat/AgentStreamSink.java` | 修改 |
| 6 | `cf-service/.../tool/UserWebsiteTool.java` | 重写 |
| 7 | `cf-service/.../tool/DataLookupTool.java` | 新建 |
| 8 | `cf-service/.../chat/ChatOrchestrator.java` | 修改 |
| 9 | DB: `t_agent_tool_def` + `t_agent_skill` | 数据修改 |
| 10 | `src/types/agent.ts` | 修改 |
| 11 | `src/composables/useAgentChat.ts` | 修改 |
| 12 | `src/components/agent/AgentToolCompletionCard.vue` | 新建 |
| 13 | `src/components/agent/AgentMessageBubble.vue` | 修改 |
| 14 | `src/views/user/AgentChatUserView.vue` | 修改 |

---

## 自审清单

1. **Spec coverage:** 设计文档 5 章全覆盖 → Chapter 1 (Task 1,2,6) / Chapter 2 (Task 7,9) / Chapter 3 (Task 3,4,5,8,11) / Chapter 4 (Task 10,12,13,14) / Chapter 5 (Task 10,14)
2. **Placeholder scan:** 无 TBD/TODO/fill in details
3. **Type consistency:** `ToolIncompleteData`/`MissingField`/`Suggestion` 在 agent.ts 和 AgentToolCompletionCard.vue 中一致；`WebsiteDraftDTO` 字段名与 `ToolResult.collected` 和 `MissingField.field` 一致
