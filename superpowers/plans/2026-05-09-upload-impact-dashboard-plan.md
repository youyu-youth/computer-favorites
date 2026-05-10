# 实施计划：上传网站影响力看板（Upload Impact Dashboard）

> 关联 spec：`superpowers/specs/2026-05-09-upload-impact-dashboard-design.md`
> 创建日期：2026-05-09
> 预计工时：1.5 ~ 2 人日

---

## Overview

为 ProfileView / ProfilePublicView 新增「我的网站影响力」看板，展示**他人对当前用户上传网站**的浏览/点赞/收藏/评论/评分行为统计，支持 7 天/30 天/90 天/全部 范围切换；视觉上 5 个并列迷你仪表盘（环比） + 5 行垂直堆叠 ECharts 柱图（去默认风格）。

后端策略：实时聚合 + Redis Cache-Aside（TTL 600s + jitter 60s）+ 复用现有两个控制器（`ProfileDashboardController` 追加端点、`ProfilePublicController` 追加 case）。本期不加索引（列入 P2）。

---

## Architecture Decisions

- **单接口聚合**：1 个 VO 同时承载 totals / rangeCounts / delta / points，避免 5 个事件指标重复 query `t_website`。
- **复用控制器**：不新建 `ProfileUploadImpactController`，直接在现有两个控制器上扩展。
- **5 表并发查**：`CompletableFuture.allOf` 同时跑 5 张事件表，墙钟时间 ≈ 最慢单表。
- **缓存纯 TTL 失效**：不在写路径主动失效，10 分钟自然过期。
- **指标维度色板**：browse=sky / like=rose / collect=amber / comment=emerald / score=teal（避开紫色）。
- **柱图去默认风格**：ECharts bar 关闭 axis/grid/tooltip 边框，柱透明度随 value/max 在 [0.35, 1.0] 间映射。
- **range=all 时**：跳过事件表查询；前端柱图变 5 行横向总计大柱、仪表盘隐藏改总额数字。

---

## 依赖图

```
[Phase 1: Backend Foundation]
  T1 UploadImpactVO (cf-model)
       │
       └─► T2 UploadImpactMapper + Mapper POJOs (cf-service)
              │
              └─► T3 UploadImpactService + impl (cf-service, 含缓存)
                     │
                     ├─► T4 ProfileDashboardController +/upload-impact (cf-web)
                     └─► T5 ProfilePublicController +case "upload-impact" (cf-web)

[Phase 2: Backend Tests]
  T6 UploadImpactServiceImplTest (cf-service test)
  T7 ProfileDashboardController/ProfilePublicController 集成测试 (cf-web test)

  >>> Checkpoint A: 后端可独立交付 <<<

[Phase 3: Frontend Foundation]
  T8 palette.ts (5 指标色板)
  T9 api/user-profile-impact.ts
       └─► T10 stores/profileImpact.ts

[Phase 4: Frontend Components - GitHub Stats 风重设计]
  T11 ImpactSparklineRow.vue (单行 sparkline · 24px 高 · ECharts 去默认风)
  T12 ImpactGaugePanel.vue (270° 半圆 5 弧叠加 + 5 行 legend)
  T13 (废弃 - 合并入 T12)
  T14 ProfileImpactSection.vue (左右两栏 + range tabs + 空态/加载态/错误态)

[Phase 5: Integration]
  T15 接入 ProfileView.vue (登录页)
  T16 接入 ProfilePublicView.vue (公开页)

[Phase 6: Frontend Tests]
  T17 profileImpact.spec.ts (Pinia store)
  T18 ImpactSparklineRow.spec.ts (sparkline 组件)
  T19 E2E 冒烟用例 (Playwright)

  >>> Checkpoint B: 端到端可交付 <<<
```

---

## Task List

### Phase 1: Backend Foundation

#### T1: UploadImpactVO（含 3 个内部静态 VO）

**Description**：在 `cf-model` 中新建响应 VO，含 `MetricGroupVO`、`DeltaGroupVO`、`DailyPointVO` 三个内部静态类。

**Acceptance criteria:**
- [ ] `UploadImpactVO` 字段：`range / websiteCount / totals / rangeCounts / delta / points`
- [ ] 全部用 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
- [ ] `delta` 字段类型 `BigDecimal`（支持 null）；`totals/rangeCounts/points` 用 `Long`
- [ ] 添加 javadoc，说明每个字段口径与单位

**Verification:**
- [ ] `mvn -pl cf-model compile` 通过
- [ ] IntelliJ 不报红、字段顺序与 spec 3.2 节一致

**Dependencies:** None

**Files likely touched:**
- `cf-model/src/main/java/com/yyyouth/model/vo/userstats/UploadImpactVO.java`

**Estimated scope:** S（1 文件）

---

#### T2: UploadImpactMapper + 辅助 POJO

**Description**：在 `cf-service` 中新建 6 方法 Mapper：1 个查上传网站集合 + 5 个事件表按日聚合。辅助 POJO 放 mapper 同包。

**Acceptance criteria:**
- [ ] `selectSitesBySubmitter(userId)` 返回 `List<WebsiteCounterRow>`，过滤 `submitter_id + deleted=0 + audit_status=1`
- [ ] `countBrowse / countLike / countCollect / countScore / countComment` 5 方法签名一致：`(websiteIds, from, to) -> List<DateCountRow>`
- [ ] `countComment` 额外加 `deleted=0 AND status=1`
- [ ] SQL 用 `<script>` 包裹 `<foreach>` 渲染 IN 子句
- [ ] `WebsiteCounterRow` 与 `DateCountRow` 用 `@Data` POJO

**Verification:**
- [ ] `mvn -pl cf-service compile` 通过
- [ ] 用 MyBatis-Plus 默认配置启动，扫描包覆盖 `com.yyyouth.service.mapper`（已有约定）

**Dependencies:** T1

**Files likely touched:**
- `cf-service/src/main/java/com/yyyouth/service/mapper/user/UploadImpactMapper.java`
- `cf-service/src/main/java/com/yyyouth/service/mapper/user/dto/WebsiteCounterRow.java`
- `cf-service/src/main/java/com/yyyouth/service/mapper/user/dto/DateCountRow.java`

**Estimated scope:** S（3 文件，仅声明）

---

#### T3: UploadImpactService 接口 + 实现（含 RedisCache）

**Description**：业务核心。算法见 spec 3.3，含：拿网站集合 → totals SUM → 5 表并发查 current+prev 区间 → 组装 points → 算 delta → Cache-Aside 包裹。

**Acceptance criteria:**
- [ ] `UploadImpactService.getUploadImpact(Long userId, String range): UploadImpactVO`
- [ ] `range` 校验：仅接受 `7d / 30d / 90d / all`，非法抛 `BusinessException(400, "range 仅支持 7d / 30d / 90d / all")`
- [ ] `range=all` 时**不查事件表**，points 返回空 list、delta 全 null、rangeCounts=totals
- [ ] `websiteCount=0` 时直接返回零值 VO，**不查事件表**
- [ ] 5 表 current/prev 共 10 次查询用 `CompletableFuture.allOf` 并发
- [ ] delta 计算规则：`prev=0 && curr=0 → 0`；`prev=0 && curr>0 → 100`；其他 `(curr-prev)*100/prev`，1 位小数 HALF_UP
- [ ] points 按日期升序、缺失日补 0
- [ ] Cache-Aside：key=`USER_PROFILE_DASHBOARD_PREFIX + userId + ":upload-impact:" + range`，TTL 600s + jitter 60s
- [ ] 引入 `private static final long TTL_UPLOAD_IMPACT_SECONDS = 600L;` 与 `JITTER_60_SECONDS = 60L;`

**Verification:**
- [ ] `mvn -pl cf-service compile` 通过
- [ ] 本地起 Redis，手动调用 service，第二次走缓存（日志验证）
- [ ] 单测见 T6

**Dependencies:** T1, T2

**Files likely touched:**
- `cf-service/src/main/java/com/yyyouth/service/user/userstats/UploadImpactService.java`
- `cf-service/src/main/java/com/yyyouth/service/user/userstats/impl/UploadImpactServiceImpl.java`

**Estimated scope:** M（2 文件、~250 行实现）

---

#### T4: 在 ProfileDashboardController 追加 /upload-impact

**Description**：登录端第 6 个端点，复用现有限流 key `profile:dashboard:#{#loginId}`。

**Acceptance criteria:**
- [ ] 路径：`GET /api/user/profile/dashboard/upload-impact?range=...`
- [ ] 注解齐全：`@SaCheckLogin / @RateLimit / @AuditLog / @ApiOperation`
- [ ] 默认 `range=30d`
- [ ] 构造器注入 `UploadImpactService`（保持 `@RequiredArgsConstructor` 的 final 注入风格）
- [ ] 日志输出：`log.info("[dashboard] uploadImpact userId={}, range={}", ...)`

**Verification:**
- [ ] `mvn -pl cf-web compile` 通过
- [ ] 启动 cf-core dev，curl 自测 `/api/user/profile/dashboard/upload-impact?range=7d` 200 OK
- [ ] 集成测试见 T7

**Dependencies:** T3

**Files likely touched:**
- `cf-web/src/main/java/com/yyyouth/web/controller/user/ProfileDashboardController.java`（仅追加方法 + 注入）

**Estimated scope:** S（1 文件、~25 行追加）

---

#### T5: 在 ProfilePublicController 追加 upload-impact case

**Description**：公开端 section 扩展，复用现有限流/审计/隐私校验。

**Acceptance criteria:**
- [ ] 新增常量 `SECTION_UPLOAD_IMPACT = "upload-impact"`
- [ ] 注入 `UploadImpactService`
- [ ] `requireContribution` 判断**保持原样**——upload-impact 不是「贡献私密」类，不计入
- [ ] switch 追加 case，调用 `uploadImpactService.getUploadImpact(targetUserId, range)`
- [ ] default 错误文案补一项支持的 section：`upload-impact`
- [ ] 日志格式与现有 5 个 section 一致

**Verification:**
- [ ] `mvn -pl cf-web compile` 通过
- [ ] curl 匿名 `/api/user/profile/public/{username}/dashboard/upload-impact?range=30d` 200 OK
- [ ] curl 不存在 username → 业务码非 200（由 ProfilePublicService 抛）
- [ ] 集成测试见 T7

**Dependencies:** T3

**Files likely touched:**
- `cf-web/src/main/java/com/yyyouth/web/controller/user/ProfilePublicController.java`（仅追加常量/注入/case）

**Estimated scope:** S（1 文件、~15 行追加）

---

### Phase 2: Backend Tests

#### T6: UploadImpactServiceImplTest

**Description**：6 个单元测试场景，全部走 `@Mock` mapper，覆盖核心算法分支。

**Acceptance criteria:**
- [ ] 测试 1：`websiteCount=0` → 全 0 + points 空 + delta 全 0
- [ ] 测试 2：`range=7d` 含 mock 5 表数据 → points 长度=7、按日期升序、缺失日补 0
- [ ] 测试 3：`range=30d` delta 边界：`prev=0 curr=0 → 0`、`prev=0 curr>0 → 100`、`prev>0 curr<prev → 负值`
- [ ] 测试 4：`range=all` → points 空、delta 全 null、rangeCounts=totals
- [ ] 测试 5：`range=invalid` → 抛 `BusinessException(400)`
- [ ] 测试 6：缓存路径——连续两次调用，第二次不再走 mapper（`verify(mapper, times(1))`）

**Verification:**
- [ ] `mvn -pl cf-service test -Dtest=UploadImpactServiceImplTest` 全绿

**Dependencies:** T3

**Files likely touched:**
- `cf-service/src/test/java/com/yyyouth/service/user/userstats/UploadImpactServiceImplTest.java`

**Estimated scope:** M（1 文件、~250 行测试）

---

#### T7: 控制器集成测试（登录端 + 公开端）

**Description**：用 `@SpringBootTest` + `MockMvc` 验证两个控制器路径。

**Acceptance criteria:**
- [ ] 登录端 GET 自身接口 → 200 + JSON 形态符合 VO
- [ ] 登录端未登录 GET → 401
- [ ] 登录端 `range=invalid` → 400
- [ ] 公开端匿名 GET 存在 username → 200
- [ ] 公开端 GET 不存在 username → 404 / 403（取决于 `ProfilePublicService` 实现）
- [ ] 触发 `RateLimit` → 429（连续 6 次 < 1 秒）

**Verification:**
- [ ] `mvn -pl cf-web test -Dtest=ProfileDashboardController*UploadImpact*` 全绿
- [ ] `mvn -pl cf-web test -Dtest=ProfilePublicController*UploadImpact*` 全绿

**Dependencies:** T4, T5

**Files likely touched:**
- `cf-web/src/test/java/com/yyyouth/web/controller/user/ProfileDashboardControllerUploadImpactTest.java`
- `cf-web/src/test/java/com/yyyouth/web/controller/user/ProfilePublicControllerUploadImpactTest.java`

**Estimated scope:** M（2 文件、~200 行测试）

---

### Checkpoint A：后端可独立交付

- [ ] `mvn clean install -pl cf-model,cf-common,cf-service,cf-web` 全绿
- [ ] T6 + T7 测试全部通过
- [ ] 启动 cf-core dev，手动 curl 4 个 range × 2 个端点 = 8 次请求全 200
- [ ] Redis 中能看到 8 个 cache key（10 min 过期）
- [ ] **Review with human before proceeding to frontend**

---

### Phase 3: Frontend Foundation

#### T8: palette.ts（GitHub Primer 风色板）

**Description**：建立 5 指标的 light/dark hex（GitHub Primer 工程感配色）与 alpha 工具函数。

**Acceptance criteria:**
- [ ] 导出常量 `IMPACT_PALETTE: Record<MetricKey, { light: string; dark: string }>`
- [ ] 5 指标 hex（GitHub Primer 同款）：
  - `browse: { light: '#4493f8', dark: '#58a6ff' }`（GitHub blue）
  - `like: { light: '#f78166', dark: '#ff8c75' }`（GitHub orange-pink）
  - `collect: { light: '#d4a72c', dark: '#e3b341' }`（GitHub gold）
  - `comment: { light: '#3fb950', dark: '#56d364' }`（GitHub green）
  - `score: { light: '#388bfd', dark: '#79c0ff' }`（cyan-blue）
- [ ] 导出 `withAlpha(hex: string, alpha: number): string` 工具，输出 `rgba()`
- [ ] 导出 `lerp(a: number, b: number, t: number): number` 用于柱透明度计算
- [ ] 导出 `metricLabel: Record<MetricKey, string>` 中文 label（浏览/点赞/收藏/评论/评分）

**Verification:**
- [ ] `npm run type-check` 通过
- [ ] 单元测试（vitest）：`withAlpha('#0ea5e9', 0.5) === 'rgba(14, 165, 233, 0.5)'`

**Dependencies:** None

**Files likely touched:**
- `computer-favorites-web/src/components/user/profile/impact/palette.ts`

**Estimated scope:** XS（1 文件、~30 行）

---

#### T9: api/user-profile-impact.ts

**Description**：fetch 封装的 API 客户端，URL 构造逻辑参考 `user-profile-dashboard.ts`。

**Acceptance criteria:**
- [ ] 导出类型 `ImpactRange / UploadImpact / MetricGroup / DeltaGroup / DailyPoint`
- [ ] 导出函数 `getUploadImpact(range, context?) => Promise<UploadImpact>`
- [ ] URL 构造：context.username 存在 → public 路径；否则 → dashboard 路径
- [ ] 错误处理与 `user-profile-dashboard.ts` 一致：`code !== 200 || !data` 抛 `Error(msg)`

**Verification:**
- [ ] `npm run type-check` 通过
- [ ] mock fetch 单元测试：success 返回 data；非 200 抛 Error

**Dependencies:** None

**Files likely touched:**
- `computer-favorites-web/src/api/user-profile-impact.ts`

**Estimated scope:** S（1 文件、~80 行）

---

#### T10: stores/profileImpact.ts（Pinia）

**Description**：独立 store，结构对标 `profileDashboard.ts` 但简化为单 section。

**Acceptance criteria:**
- [ ] `useProfileImpactStore` 暴露：`impact / range / context / load / setRange / reset`
- [ ] `range` 默认 `'30d'`
- [ ] `load(r?, ctx?)` 切换 range/context 后调用 `getUploadImpact`
- [ ] 内部 `handle` 工具与 `profileDashboard.ts` 同款，状态 `idle/loading/success/error`
- [ ] `reset()` 清空所有 state

**Verification:**
- [ ] `npm run type-check` 通过
- [ ] 单测 see T17

**Dependencies:** T9

**Files likely touched:**
- `computer-favorites-web/src/stores/profileImpact.ts`

**Estimated scope:** S（1 文件、~70 行）

---

### Phase 4: Frontend Components（GitHub Stats 风重设计）

> **重要变更**：原 T11 ImpactBarChart / T12 ImpactMiniGauge / T13 ImpactSummaryGrid 已废弃。
> 新设计采用 GitHub Readme Stats 风格（紧凑、克制、工程化），左右两栏布局：
> - 左栏 = 一个 270° 半圆 Gauge（5 段彩色弧按 share 叠加）+ 5 行 legend
> - 右栏 = 5 行 24px 极紧凑 sparkline
> 详见 `spec 4.2`。

#### T11: ImpactSparklineRow.vue（单行 sparkline）

**Description**：右栏单行组件，一行展示一个指标。布局 = `[● 指标名] [sparkline 柱图] [数值] [delta]`，行高 24px。

**Acceptance criteria:**
- [ ] props：`metric: MetricKey`、`data: number[]`、`max: number`、`dates?: string[]`、`total: number`、`delta: number | null`、`rangeIsAll: boolean`
- [ ] 行高度固定 24px；4 段弹性宽度 `grid-cols-[80px_1fr_72px_56px]`
- [ ] 左侧色块（8x8 rounded-full）+ 中文 label（12px）
- [ ] sparkline ECharts 配置（去默认风格）：
  - `grid: { left:0, right:0, top:1, bottom:1 }`
  - `xAxis/yAxis: show=false`
  - `barCategoryGap: '20%'`、`borderRadius: [1,1,0,0]`（近方角）
  - tooltip 自定义浅黑底：`rgba(20,20,20,0.92) + #e5e7eb 文字`
  - 柱色 alpha 按 `value/max` 在 [0.35, 1] 间映射
  - 0 值柱保留 1px 地基线（`itemStyle.opacity=0.2`）
  - 峰值柱 1px 顶部描边（同色实线高光）
- [ ] `rangeIsAll=true` 时：sparkline 替换为单根 100% 宽度 total bar，颜色为指标色 100%
- [ ] 数值用 `Intl.NumberFormat('zh-CN', { notation:'compact' })` 紧凑格式（如 `1.2K`），等宽字体右对齐
- [ ] delta 显示：`+12%` 绿 `text-emerald-600/70` / `-5%` 红 `text-red-500/70` / `0%` 灰；箭头用字符 `↑↓`（无图标库）
- [ ] `delta=null` 时不显示 delta 区域
- [ ] 暗黑模式自动切换 hex（监听 `useDark()`）
- [ ] 响应窗口：`echarts.resize()` 接 `useResizeObserver`

**Verification:**
- [ ] `npm run type-check` 通过
- [ ] 浏览器：5 个 metric × 7 数据点 / 30 数据点 / 90 数据点 渲染正常
- [ ] 单测 see T18

**Dependencies:** T8

**Files likely touched:**
- `computer-favorites-web/src/components/user/profile/impact/ImpactSparklineRow.vue`

**Estimated scope:** M（1 文件、~180 行）

---

#### T12: ImpactGaugePanel.vue（半圆 Gauge + 5 弧叠加 + Legend）

**Description**：左栏整体组件，含一个 270° 半圆 SVG gauge（5 段彩色弧按 share 叠加）+ 中心累计总额 + 下方 5 行 legend list。

**Acceptance criteria:**
- [ ] props：`totals: MetricGroup`、`rangeCounts: MetricGroup`、`range: ImpactRange`
- [ ] 270° SVG gauge：
  - 起点 `225°`、终点 `135°`（顺时针），背景灰弧 stroke 8px 绕完整 270°
  - 5 段前景弧按 share 叠加（不是各自独立）：每段长度 = `count / sum * 270°`
  - 段顺序：browse → like → collect → comment → score（与 legend 一致）
  - 段间无间隙，stroke 8px，圆头线帽（`stroke-linecap: round`）→ 视觉柔和
- [ ] 中心：
  - 主数字：`Intl.NumberFormat('zh-CN', { notation:'compact' })`，等宽字体 18px
  - 副文字：`累计互动`（range=all）/ `近 N 天互动`（其他），灰色 11px
- [ ] 数据源：sum = `range==='all' ? totals : rangeCounts` 各字段累加
- [ ] 5 行 legend（gauge 下方）：
  - 每行 24px：`● 指标名` 左 + `数值` 中 + `占比%` 右
  - 数值与占比等宽字体右对齐，列宽固定
  - 占比 `< 1%` 显示 `<1%`
- [ ] 动画：每段 stroke-dasharray 从 0 增长，`800ms cubic-bezier(0.22, 1, 0.36, 1)`，stagger 100ms
- [ ] sum=0（websiteCount=0 或全 0）时：gauge 灰底空环 + 中心 `0` + legend 全 `0` `<1%`
- [ ] 暗黑模式自动切换

**Verification:**
- [ ] `npm run type-check` 通过
- [ ] 浏览器视觉：light + dark 双主题对比度足够、5 段颜色清晰可分辨
- [ ] 边界：sum=0 / 单指标占 100% / 5 段均匀（各 20%）三种 share 分布渲染正常

**Dependencies:** T8

**Files likely touched:**
- `computer-favorites-web/src/components/user/profile/impact/ImpactGaugePanel.vue`

**Estimated scope:** M（1 文件、~220 行）

---

#### T13:（已废弃，原 ImpactSummaryGrid 不再需要）

> 原 T13 在新设计下被合并入 T12（左栏 ImpactGaugePanel 内部已含 legend list）。
> 任务编号保留以维持 T14~T19 的依赖编号不变；执行时跳过 T13。

---

#### T14: ProfileImpactSection.vue（主卡片 · 左右两栏）

**Description**：组合一切。标题 + range tabs + 副标题 + 左 GaugePanel + 中线分割 + 右 5 SparklineRow + 空态/加载态/错误态。

**Acceptance criteria:**
- [ ] props：`username?: string`（可选，存在则走公开端）
- [ ] 卡片样式：`border border-zinc-200/70 dark:border-zinc-800/70 rounded-md px-4 py-3`
- [ ] 标题：lucide `Activity` 图标 14px + 「我的网站影响力」`font-medium text-sm`
- [ ] range tabs（右上）：4 个 pill `7d / 30d / 90d / All`，灰底反白选中态，`px-2 py-0.5 text-xs gap-x-1`
- [ ] 副标题：12px 灰色 `基于 {websiteCount} 个上传网站 · 累计他人互动 {totalSum} 次`
- [ ] 桌面端布局：`grid grid-cols-[2fr_3fr]` + 中线 `border-l border-zinc-200/60 dark:border-zinc-800/60`
- [ ] 移动端布局：单列堆叠（`md:grid-cols-1`），中线消失
- [ ] 数据流：`onMounted` 调 `store.load`；`username` prop 变化 → `store.setContext`；range tab 点击 → `store.setRange`
- [ ] 5 个 SparklineRow 渲染：传入 `data=points.map(p=>p[metric])`、`max=Math.max(...data)`、`total=rangeCounts[metric]`、`delta=delta[metric]`、`rangeIsAll=range==='all'`
- [ ] **空态**（websiteCount=0）：左栏 gauge 隐藏，整张卡片中心显示 lucide `Inbox` 图标 + `暂无上传网站，影响力数据将在网站审核通过后开始累计`
- [ ] **加载态**：左栏 gauge skeleton 圆环 + 5 行 legend 骨架；右栏 5 行 sparkline 骨架
- [ ] **错误态**：错误条 + 重试按钮（调 `store.load`）
- [ ] range 切换微交互：左右两栏同时 `opacity 0.4 → 1` `200ms`

**Verification:**
- [ ] `npm run type-check` 通过
- [ ] 浏览器手动：4 个 range 切换流畅、桌面/移动断点切换正常、暗黑模式视觉一致
- [ ] websiteCount=0 / 加载中 / 错误 三种状态视觉验证

**Dependencies:** T10, T11, T12

**Files likely touched:**
- `computer-favorites-web/src/components/user/profile/ProfileImpactSection.vue`

**Estimated scope:** M（1 文件、~280 行）

---

### Phase 5: Integration

#### T15: 接入 ProfileView.vue

**Description**：在主区块前插入 `<ProfileImpactSection />`，无 username prop。

**Acceptance criteria:**
- [ ] 在 `<main>` 中第一个区块（`<ProfileDashboardCharts />` 之前）插入 `<ProfileImpactSection />`
- [ ] **保留** `<ProfileDashboardCharts />` 与 `<ProfileDashboardSummary />`
- [ ] import 路径正确

**Verification:**
- [ ] `npm run dev` 启动后访问 `/profile`，两个看板都能渲染（影响力在前、活跃在后）
- [ ] `npm run build` 通过

**Dependencies:** T14

**Files likely touched:**
- `computer-favorites-web/src/views/user/ProfileView.vue`

**Estimated scope:** XS（1 文件、~3 行追加）

---

#### T16: 接入 ProfilePublicView.vue

**Description**：在公开主页同步插入，传入 `username`。

**Acceptance criteria:**
- [ ] 找到 ProfilePublicView 的主内容区，插入 `<ProfileImpactSection :username="targetUsername" />`
- [ ] 位置与 ProfileView 对应（一致的视觉层级）
- [ ] 公开页可见性由 service 兜底，组件不需自己处理 404

**Verification:**
- [ ] `npm run dev`，访问 `/u/{username}` 显示影响力区块
- [ ] 匿名访问也能看到（不强制登录）

**Dependencies:** T14

**Files likely touched:**
- `computer-favorites-web/src/views/user/ProfilePublicView.vue`

**Estimated scope:** XS（1 文件、~3 行追加）

---

### Phase 6: Frontend Tests

#### T17: profileImpact.spec.ts（vitest）

**Description**：Pinia store 单测。

**Acceptance criteria:**
- [ ] 测 1：`setRange('all')` 触发 `getUploadImpact` 调用
- [ ] 测 2：API 失败 → state='error' + error msg 设置
- [ ] 测 3：context 切换（self ↔ username）触发 reload
- [ ] 测 4：`reset()` 清空 state

**Verification:**
- [ ] `npm run test:unit -- profileImpact` 全绿

**Dependencies:** T10

**Files likely touched:**
- `computer-favorites-web/src/stores/__tests__/profileImpact.spec.ts`

**Estimated scope:** S（1 文件、~80 行）

---

#### T18: ImpactSparklineRow.spec.ts

**Description**：sparkline 行组件渲染单测，覆盖关键视觉行为。

**Acceptance criteria:**
- [ ] 测 1：30 数据点 → ECharts option `series[0].data.length === 30`
- [ ] 测 2：最高柱 markPoint 高光配置存在（option 验证）
- [ ] 测 3：0 值柱 `itemStyle.opacity = 0.2`
- [ ] 测 4：暗黑模式色板切换（mock `useDark`）→ option color 用 dark hex
- [ ] 测 5：`rangeIsAll=true` 时 series 数据变为单根 total bar
- [ ] 测 6：`delta=null` 时 delta 区域不渲染

**Verification:**
- [ ] `npm run test:unit -- ImpactSparklineRow` 全绿

**Dependencies:** T11

**Files likely touched:**
- `computer-favorites-web/src/components/user/profile/impact/__tests__/ImpactSparklineRow.spec.ts`

**Estimated scope:** S（1 文件、~120 行）

---

#### T19: E2E 冒烟用例（Playwright）

**Description**：1 个 happy path，确认主流程不挂。

**Acceptance criteria:**
- [ ] 登录用户访问 `/profile`
- [ ] 等待「我的网站影响力」标题出现
- [ ] 点击「近 7 天」按钮
- [ ] 验证 5 仪表盘元素与 5 柱图元素全部存在
- [ ] 切换到「全部」，验证仪表盘消失、柱图变为总计柱

**Verification:**
- [ ] `npm run e2e -- upload-impact` 全绿

**Dependencies:** T15

**Files likely touched:**
- `computer-favorites-web/e2e/upload-impact.spec.ts`

**Estimated scope:** S（1 文件、~60 行）

---

### Checkpoint B：端到端可交付

- [ ] T1~T19 全部完成
- [ ] `mvn clean install` + `npm run build` 双绿
- [ ] T6/T7/T17/T18/T19 测试全绿
- [ ] 手动回归：4 range × 2 视图（self / public）= 8 次切换流畅
- [ ] 暗黑模式 + 移动端 视觉无明显问题
- [ ] **Final review with human**

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| `t_browse_history` 大表慢查询 | High | 已纳入 P2 索引项；本期靠 cache TTL + RateLimit 顶 |
| ECharts bar 暗黑模式色板溢出 | Med | T18 单测覆盖暗色快照；T11 监听 useDark |
| 5 表并发查触发 HikariCP 池压力 | Low | 单用户每次 5 connections 共 ~1s 内归还，PoolSize≥10 即可 |
| TTL 10min 让 demo 显得"延迟" | Low | spec 注明；用户取消点赞需等 10min 才在影响力面板生效 |
| 公开端 username 注入 SQL/XSS | Low | `@PathVariable` + service 内 prepared statement，不需额外处理 |

---

## Open Questions

- [ ] 是否需要在 `ProfileView` 顶部添加锚点 ID（`#impact`）以支持外部链接直达？暂未列入任务，需要再确认。
- [ ] `superpowers/` 目录是否要加入 `.gitignore`？目前未 ignore，意味着 spec/plan 会随代码进入 git。如需 ignore，请在 commit 前手动加。

---

## Parallelization Notes

- **可并行**：T1↔T8、T9 可独立起手；后端 Phase 1 完成后，T6/T7 与 Phase 3+4 可并行。
- **必须顺序**：T2 依赖 T1；T3 依赖 T2；T4/T5 依赖 T3；T11 依赖 T8；T13 依赖 T12；T14 依赖 T10/T11/T13；T15/T16 依赖 T14。
- **协调点**：API 契约（VO）在 T1 锁定后即可前后端并行，避免 spec 形态变化引发返工。

---

## 执行建议

按 Checkpoint A → Checkpoint B 的双阶段策略执行：

1. **第一阶段**（建议单 session）：T1 → T2 → T3 → T4 → T5 → T6 → T7 → Checkpoint A 人工 review
2. **第二阶段**（建议单 session）：T8/T9/T10 并行起手 → T11/T12 并行 → T13 → T14 → T15/T16 并行 → T17/T18/T19 并行 → Checkpoint B
