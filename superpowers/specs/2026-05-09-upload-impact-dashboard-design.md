# 上传网站影响力看板（Upload Impact Dashboard）— 设计文档

| 项 | 值 |
|---|---|
| 日期 | 2026-05-09 |
| 作者 | Cascade（与 yyyouth-zg 共同 brainstorming） |
| 关联模块 | user-profile-dashboard |
| 状态 | 待实施（spec 已审，进入 writing-plans） |

---

## 1. 背景

### 1.1 当前现状

`computer-favorites-web/src/views/user/ProfileView.vue` 中的「上传内容数据看板」由
`ProfileDashboardSummary.vue` 渲染，看似展示用户的上传影响力，实际口径是 **当前登录用户自身的行为统计**：

- `t_user_stats_*` 系列由 `UserStatsAggregateServiceImpl` 按 **操作者 userId** 聚合。
- 即 `totalBrowse=10` 表示「我浏览过 10 次别人的网站」，而非「我的网站被浏览了 10 次」。

证据：
- `UserCollectServiceImpl.java:139` `userActivityPublisher.publish(userId, COLLECT, ...)` 中 `userId` = 收藏者。
- `UserStatsAggregateMapper.java:52-66` 全部 `WHERE user_id = #{userId}`，是操作者口径。

### 1.2 用户的真实诉求

> 我想看 **其他用户对我上传的网站** 的浏览/点赞/收藏/评论/评分行为统计，
> 按 7 天 / 30 天 / 90 天 / 全部 时间范围切片，
> 是 **网站上传者所有网站合并** 的累计视图。

### 1.3 已有可复用资产

- `t_website` 包含实时计数器 `click_count / like_count / collect_count / comment_count / score_count`，
  按 `submitter_id` 即可得到全时累计。
- 5 张事件表（`t_browse_history`、`t_website_like`、`t_user_collect`、`t_comment`、`t_website_score`）
  全部含 `website_id + create_time`，足以做时间序列。
- `RedisCache.getOrLoad`（Cache-Aside）已存在，TTL+jitter+空值缓存全到位。
- `RedisConstant.USER_PROFILE_DASHBOARD_PREFIX` 已为 dashboard 系列保留前缀。

### 1.4 关键约束（用户决策）

- **不新增索引**（接受性能风险作为 P2）
- **不做封禁用户特殊过滤**
- **下架网站的历史数据保留**（`status=0` 仍计入累计与时间序列）

---

## 2. 设计目标

| 目标 | 度量 |
|---|---|
| 主目标 | 让用户能直观看到「我的上传网站」被他人交互的累计与趋势 |
| 性能目标（缓存命中） | P95 < 50ms |
| 性能目标（缓存 miss，无索引） | 接受 P95 ≤ 5s（百万行级 `t_browse_history`），慢日志触发后补索引 |
| 代码量目标 | 后端 1 Service+1 Controller+1 Mapper+1 VO；前端 1 主区块+3 子组件+1 Store+1 API |
| 可访问性 | 登录页（自己看自己）+ 公开页（GitHub Profile 风格、匿名可查） |

非目标（明确不做）：
- 不做实时事件失效缓存（依赖 TTL 自然过期）
- 不做全站排名 / P95 分位数对标
- 不做按月聚合（"全部"范围下不展示时间序列）
- 不做"封禁用户隐藏"

---

## 3. 后端设计

### 3.1 接口契约

**实现策略：复用现有两个控制器，不新建独立控制器。**

- 登录端：在 `ProfileDashboardController` 中追加第 6 个端点（与现有 5 个 dashboard section 并列）。
- 公开端：在 `ProfilePublicController.getPublicDashboardSection` 的 switch 中追加 `upload-impact` case（与现有 5 个 section 共享同一个端点 `/{username}/dashboard/{section}` + `range` 参数）。

#### 登录端（仅本人查自己）

```
GET /api/user/profile/dashboard/upload-impact?range=7d|30d|90d|all
```

- 鉴权：`@SaCheckLogin`
- 限流：`@RateLimit(key = "profile:dashboard:#{#loginId}", limit = 5, window = 1)`（沿用现有 dashboard 限流 key）
- 审计：`@AuditLog(action = "upload-impact", description = "查询上传网站影响力，range=#{#range}")`
- userId 取自 `StpUtil.getLoginIdAsLong()`

#### 公开端（匿名 / 任何登录用户均可查）

```
GET /api/user/profile/public/{username}/dashboard/upload-impact?range=7d|30d|90d|all
```

- 不加 `@SaCheckLogin`（控制器级别已不强制登录）
- 限流：沿用现有 `profile:dashboard:public:#{#username}:#{#section}:#{#loginId}` key，`section=upload-impact`
- 审计：沿用现有 `public-dashboard` action
- userId 通过 `profilePublicService.getPublicProfile(username, viewerId).getUser().getId()` 解析（用户不存在/不可见 → service 抛 404/403）
- 隐私校验：调用 `profilePublicService.checkDashboardAccess(targetUserId, currentUserId, requireContribution=false)`，与 `category-distribution / tech-radar` 同档


### 3.2 响应 VO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadImpactVO {
    /** 范围：7d / 30d / 90d / all */
    private String range;

    /** 已审核通过的上传网站数（基数提示） */
    private Integer websiteCount;

    /** 全时累计（来自 t_website 计数器 SUM，与 range 无关） */
    private MetricGroupVO totals;

    /** 当前 range 内事件数（all 时 = totals 同值，前端据此渲染柱图最大柱） */
    private MetricGroupVO rangeCounts;

    /** 本期 vs 上一同长期间 环比百分比，all 时全部为 null */
    private DeltaGroupVO delta;

    /** 7d/30d/90d 时为日序列，all 时为空数组 */
    private List<DailyPointVO> points;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MetricGroupVO {
        private Long browse;
        private Long like;
        private Long collect;
        private Long comment;
        private Long score;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DeltaGroupVO {
        /** 单位：百分比保留 1 位小数；上期 0、本期 > 0 → 100；都 0 → 0 */
        private BigDecimal browse;
        private BigDecimal like;
        private BigDecimal collect;
        private BigDecimal comment;
        private BigDecimal score;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DailyPointVO {
        /** ISO yyyy-MM-dd */
        private String date;
        private Long browse;
        private Long like;
        private Long collect;
        private Long comment;
        private Long score;
    }
}
```

**为什么单接口聚合而非 5 个独立接口**：5 指标共享同一份「用户上传网站 ID 集合」，分接口会重复 query `t_website` 并放大开销。前端柱图、仪表盘、总额数字牌全部由 1 个 VO 渲染。

### 3.3 Service 实现要点

新建 `cf-service/src/main/java/com/yyyouth/service/user/userstats/UploadImpactService` + `impl/UploadImpactServiceImpl.java`。

#### 算法步骤

```
INPUT: userId, range
OUTPUT: UploadImpactVO

1. 拿网站 + totals
   sites = t_website.selectList(submitter_id=userId AND deleted=0 AND audit_status=1)
   websiteCount = sites.size
   totals = SUM(click_count, like_count, collect_count, comment_count, score_count)
   websiteIds = sites.map(id)

2. IF range == 'all':
     return VO(range=all, websiteCount, totals, rangeCounts=totals, delta=null*5, points=[])

3. IF websiteIds is empty:
     return VO(range, websiteCount=0, totals=zeros, rangeCounts=zeros, delta=zeros, points=[])

4. days = parseRangeDays(range)        // 7 / 30 / 90
   today = LocalDate.now()
   currFrom = today.minusDays(days - 1)
   prevFrom = currFrom.minusDays(days)
   prevTo   = currFrom

5. 并发查 5 张事件表（CompletableFuture.allOf）
   - 每张表 2 次：current 区间 + prev 区间
   - mapper 方法签名：
       Map<LocalDate, Long> countByDate(websiteIds, fromTs, toTs)
       (其中 t_comment 还要 deleted=0 AND status=1)

6. 组装 points[]
   for each date in [currFrom .. today]:
       points.add(DailyPointVO(date, browse=map.get(date)||0, ..., score=...))

7. 计算 rangeCounts
   rangeCounts.browse = sum(points.browse), 同理 5 项

8. 计算 delta（每个指标独立）
   currCount = rangeCounts.X
   prevCount = sum(prevMap.values)
   IF prevCount == 0 AND currCount == 0: delta = 0
   ELIF prevCount == 0: delta = 100.0
   ELSE: delta = (currCount - prevCount) * 100 / prevCount，保留 1 位小数（HALF_UP）

9. return VO(...)
```

#### 缓存

```java
String key = RedisConstant.USER_PROFILE_DASHBOARD_PREFIX + userId + ":upload-impact:" + range;
return redisCache.getOrLoad(key, UploadImpactVO.class,
        TTL_UPLOAD_IMPACT_SECONDS,   // 600
        JITTER_60_SECONDS,            // 60
        () -> loadUploadImpact(userId, range));
```

### 3.4 Mapper SQL

新建 `cf-service/src/main/java/com/yyyouth/service/mapper/user/UploadImpactMapper.java`，共 6 个方法（1 站点列表 + 5 事件计数）：

```java
@Mapper
public interface UploadImpactMapper {

    /** 拿到所有审核通过、未删除的上传网站（含计数器列） */
    @Select("SELECT id, click_count, like_count, collect_count, comment_count, score_count "
          + "FROM t_website "
          + "WHERE submitter_id = #{userId} AND deleted = 0 AND audit_status = 1")
    List<WebsiteCounterRow> selectSitesBySubmitter(@Param("userId") Long userId);

    /** 浏览：按日期分组计数 */
    @Select("<script>"
          + "SELECT DATE(create_time) d, COUNT(*) c FROM t_browse_history "
          + "WHERE website_id IN <foreach collection='websiteIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
          + "  AND create_time &gt;= #{from} AND create_time &lt; #{to} "
          + "GROUP BY DATE(create_time)"
          + "</script>")
    List<DateCountRow> countBrowse(@Param("websiteIds") List<Long> websiteIds,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    /** 点赞：按日期分组计数（t_website_like） */
    @Select("<script>...同 countBrowse 模板，FROM t_website_like...</script>")
    List<DateCountRow> countLike(@Param("websiteIds") List<Long> websiteIds,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);

    /** 收藏：按日期分组计数（t_user_collect） */
    @Select("<script>...同 countBrowse 模板，FROM t_user_collect...</script>")
    List<DateCountRow> countCollect(@Param("websiteIds") List<Long> websiteIds,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    /** 评分：按日期分组计数（t_website_score） */
    @Select("<script>...同 countBrowse 模板，FROM t_website_score...</script>")
    List<DateCountRow> countScore(@Param("websiteIds") List<Long> websiteIds,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    /** 评论：额外加 deleted=0 AND status=1 */
    @Select("<script>"
          + "SELECT DATE(create_time) d, COUNT(*) c FROM t_comment "
          + "WHERE website_id IN <foreach collection='websiteIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
          + "  AND create_time &gt;= #{from} AND create_time &lt; #{to} "
          + "  AND deleted = 0 AND status = 1 "
          + "GROUP BY DATE(create_time)"
          + "</script>")
    List<DateCountRow> countComment(@Param("websiteIds") List<Long> websiteIds,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);
}
```

辅助 POJO（放 `cf-service` 内，不进 `cf-model`，因仅服务层使用）：
- `WebsiteCounterRow(id, clickCount, likeCount, collectCount, commentCount, scoreCount)`
- `DateCountRow(d /* LocalDate */, c /* Long */)`

### 3.5 控制器改造

#### 3.5.1 登录端：在 `ProfileDashboardController` 追加第 6 个端点

```java
@ApiOperation(value = "上传网站影响力")
@GetMapping("/upload-impact")
@SaCheckLogin
@RateLimit(key = "profile:dashboard:#{#loginId}", limit = 5, window = 1)
@AuditLog(action = "upload-impact", description = "查询上传网站影响力，range=#{#range}")
public HttpResult uploadImpact(
        @RequestParam(value = "range", required = false, defaultValue = "30d") String range) {
    Long userId = StpUtil.getLoginIdAsLong();
    log.info("[dashboard] uploadImpact userId={}, range={}", userId, range);
    return HttpResult.success(uploadImpactService.getUploadImpact(userId, range));
}
```

构造器注入新增 `private final UploadImpactService uploadImpactService;`。

#### 3.5.2 公开端：在 `ProfilePublicController.getPublicDashboardSection` 追加 case

```java
// 1. 在 SECTION_* 常量区追加：
private static final String SECTION_UPLOAD_IMPACT = "upload-impact";

// 2. 注入：
private final UploadImpactService uploadImpactService;

// 3. requireContribution 判断保持原样（upload-impact 不是「贡献私密」类，requireContribution=false）

// 4. switch 追加 case：
case SECTION_UPLOAD_IMPACT -> {
    UploadImpactVO data = uploadImpactService.getUploadImpact(targetUserId, range);
    yield HttpResult.success(data);
}

// 5. default 文案补一项支持的 section：
default -> HttpResult.error(400, "未知 section：" + section
        + "（支持 overview / contribution-graph / category-distribution / tech-radar / trend-series / upload-impact）");
```

**复用现有限流/审计/隐私校验**：无需重复声明 `@RateLimit / @AuditLog`，已在方法级生效；隐私走 `profilePublicService.checkDashboardAccess(targetUserId, currentUserId, false)`。

### 3.6 索引（P2 待优化项，本次不做）

```sql
-- 触发慢日志后再执行
ALTER TABLE t_website        ADD INDEX idx_submitter (submitter_id, deleted, audit_status);
ALTER TABLE t_browse_history ADD INDEX idx_website_create (website_id, create_time);
ALTER TABLE t_website_like   ADD INDEX idx_website_create (website_id, create_time);
ALTER TABLE t_user_collect   ADD INDEX idx_website_create (website_id, create_time);
ALTER TABLE t_comment        ADD INDEX idx_website_create (website_id, create_time, deleted, status);
ALTER TABLE t_website_score  ADD INDEX idx_website_create (website_id, create_time);
```

落库位置：`docs/sql/database-索引补充-upload-impact.sql`（与现有 `database-补充脚本.sql` 同级）。

---

## 4. 前端设计

### 4.1 文件结构

```
computer-favorites-web/src/
├── api/
│   └── user-profile-impact.ts                   # 新建
├── stores/
│   └── profileImpact.ts                         # 新建（独立 Pinia store）
└── components/user/profile/
    ├── ProfileImpactSection.vue                 # 新建：主卡片（GitHub Stats 风格 · 左右两栏）
    └── impact/
        ├── ImpactGaugePanel.vue                 # 新建：左栏（270° 半圆 gauge + 5 弧叠加 + 5 行 legend）
        ├── ImpactSparklineRow.vue               # 新建：右栏单行（指标名 + sparkline 柱图 + 数值 + delta）
        └── palette.ts                           # 新建：GitHub Primer 风工程感色板
```

接入位置：

- `computer-favorites-web/src/views/user/ProfileView.vue:272-277` 在 `<main>` 中追加 `<ProfileImpactSection />`，置于 `<ProfileDashboardCharts />` 之前。
- `computer-favorites-web/src/views/user/ProfilePublicView.vue` 同步以 `<ProfileImpactSection :username="..." />` 接入。

### 4.2 视觉规范

**总体方向**：**GitHub Readme Stats 风**——紧凑、克制、专业、工程化、信息密度高，禁止 ECharts 默认配色与默认轴/边框风格。卡片单一边框包裹，分割线分左右两栏（约 40 / 60），暗色优先并自适应当前主题。

#### 5 指标色板（GitHub Primer 风 · 双主题）

| 指标 | Light | Dark | 说明 |
|---|---|---|---|
| browse | `#4493f8` | `#58a6ff` | GitHub blue（沉静蓝） |
| like | `#f78166` | `#ff8c75` | GitHub orange-pink（暖而不刺） |
| collect | `#d4a72c` | `#e3b341` | GitHub gold（与 star 同色系） |
| comment | `#3fb950` | `#56d364` | GitHub green |
| score | `#1f6feb` 偏冷青 → `#388bfd` | `#79c0ff` | cyan-blue，与 browse 蓝错开 |

> 完全照搬 GitHub Primer 设计 token，避免甜腻 / 撞色 / 紫色系（项目规则）。

#### 卡片整体规范

- 单卡片：`border-1 border-zinc-200/70 dark:border-zinc-800/70 rounded-md`
- 内边距 `px-4 py-3`，行间距 `gap-y-2`
- 中间垂直分割线 `border-l border-zinc-200/60 dark:border-zinc-800/60`
- 左右两栏比例：桌面端 `grid-cols-[2fr_3fr]`，移动端整体堆叠
- 字体：数字一律 `font-mono tabular-nums`，文字 `font-sans`
- 字号：标题 14px、副标题 12px、数值 13px、delta 11px
- 深浅色靠 `useDark()` 切换 hex；不依赖 Tailwind class（ECharts color 必须 hex）

#### 左栏 `ImpactGaugePanel.vue`（半圆 Gauge + 5 弧叠加 + Legend）

```
        ◜⏤⏤⏤⏤⏤◝
      ◜              ◝         ┌──────────┐
     │   ┌──────────┐   │      │ 中心:     │
     │   │  1,620   │   │      │ 累计互动 │
     │   │ 累计互动  │   │      │ 数值:    │
     │   └──────────┘   │      │ K/M 缩写 │
      ◝              ◞         └──────────┘
        ◞⏤⏤⏤⏤⏤◟

  ●浏览  1,234     76.2%
  ●点赞    180     11.1%
  ●收藏     89      5.5%
  ●评论     45      2.8%
  ●评分     72      4.4%
```

- **270° 半圆 Gauge**（SVG 手绘）：
  - 起点 `225°`、终点 `135°`（顺时针），总弧长 `270°`
  - **5 段彩色弧按 share 叠加**（不是各自独立小弧）：
    `browse` 起点 → `like` 接续 → `collect` → `comment` → `score`，长度 = `count / total * 270°`
  - 背景弧：完整 270° 灰色 `#e5e7eb / dark #27272a`，stroke 8px
  - 前景每段：对应指标色，stroke 8px，segment 间无间隙（pure stack）
- **中心**：
  - 主数字：`Intl.NumberFormat('zh-CN', { notation: 'compact' })` 格式化（如 `1.6K`、`12.3K`）
  - 副文字：`累计互动`（range=all）/ `近 N 天互动`（其他 range），灰色 11px
- **动画**：每段 `stroke-dasharray` 从 0 增长到目标值，`800ms cubic-bezier(0.22, 1, 0.36, 1)`，5 段 stagger 100ms
- **Legend（gauge 下方）**：
  - 5 行，每行 24px：`●`(8px) + 指标名(12px) + 数值(12px monospace) + 占比%(11px 灰)
  - 数值与占比右对齐，列宽固定，等宽字体
  - 占比 `< 1%` 显示 `<1%` 不显示具体值
- **range=all** 与其他 range 的差异：仅副文字与数值口径不同，gauge 与 legend 形态保持一致

#### 右栏 `ImpactSparklineRow.vue` × 5（极紧凑 sparkline）

每行布局（高 24px）：

```
┌─────┬───────────────────────────────┬────────┬────┐
│●浏览 │ ▁▂▃▅▇█▆▄▃▂▃▅▆▇▆▅▄▃▂ │ 1,234 │+12%│
└─────┴───────────────────────────────┴────────┴────┘
  12%              60%                   18%   10%
```

- **行高度**：24px（紧凑），行间距 `gap-y-1.5`
- **左侧标签** `12%`：色块 `8x8 rounded-full` + 指标名（12px）
- **中间 sparkline** `60%`：ECharts bar，关键去默认风格配置：
  ```ts
  {
    grid: { left: 0, right: 0, top: 1, bottom: 1, containLabel: false },
    xAxis: { show: false, type: 'category', boundaryGap: true },
    yAxis: { show: false, type: 'value', min: 0 },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'none' },
      backgroundColor: 'rgba(20,20,20,0.92)',
      borderColor: 'transparent',
      textStyle: { color: '#e5e7eb', fontSize: 11 },
      formatter: (p) => `${p[0].axisValue} · ${p[0].value}`
    },
    animationDuration: 600,
    animationEasing: 'cubicOut',
    animationDelay: (idx) => idx * 20,
    series: [{
      type: 'bar',
      barCategoryGap: '20%',
      itemStyle: {
        color: (p) => withAlpha(metricHex, lerp(0.35, 1.0, p.value / max)),
        borderRadius: [1, 1, 0, 0],   // 几乎方角
      },
      markPoint: { /* 峰值 1px 顶高光 */ },
    }],
  }
  ```
- **0 值柱**：保留 1px 地基线（指标色 20% alpha）
- **峰值柱**：100% 指标色 + 顶部 1px 描边（同色实线）
- **右侧数值** `18%`：`Intl.NumberFormat compact` 格式（如 `1.2K`）
- **delta** `10%`：`+12%` 绿 70% / `-5%` 红 70% / `0%` 灰 60%；箭头用字符 `↑↓`，不引图标库
- **range=all**：sparkline 替换为 100% 宽的「**total bar**」（一根柱填满，颜色按指标），数值显示 totals.metric

#### 主卡片 `ProfileImpactSection.vue` 整体

```
┌────────────────────────────────────────────────────────────────┐
│ ▰ 我的网站影响力                       [7d][30d][90d][All]    │
│ 基于 N 个上传网站 · 累计他人互动 X 次                           │
├────────────────────────────┬────────────────────────────────────┤
│                              │                                    │
│   270° gauge 5 弧叠加        │  ●浏览 [▁▂▃▅▇█▆▄▃] 1,234  +12%  │
│   中心: 1.6K / 累计互动      │  ●点赞 [▁▁▂▃▂▁▃▅▆] 180     +5%   │
│                              │  ●收藏 [▁▁▁▂▂▁▂▃▅] 89      -2%   │
│   ●浏览  1,234   76.2%      │  ●评论 [▁▁▁▁▂▁▃▄▃] 45      +50%  │
│   ●点赞    180   11.1%      │  ●评分 [▁▁▁▁▁▂▁▁▁] 4.6     0%    │
│   ●收藏     89    5.5%      │                                    │
│   ●评论     45    2.8%      │                                    │
│   ●评分     72    4.4%      │                                    │
│                              │                                    │
└────────────────────────────┴────────────────────────────────────┘
```

- **标题**：左侧 lucide `Activity` 图标 14px（不用 emoji），文字 `font-medium text-sm`
- **range tabs**（右上）：4 个 pill `px-2 py-0.5 text-xs`，灰底反白选中态，`gap-x-1`
  - 文案：`7d / 30d / 90d / All`（保持 GitHub Stats 简短英文风）
- **副标题**：12px 灰色，`基于 {N} 个上传网站 · 累计他人互动 {sum} 次`
- **桌面端布局**：`grid grid-cols-[2fr_3fr]` + 中线分割
- **移动端布局**：单列堆叠（gauge panel 在上、5 sparkline 在下），中线消失
- **空态**（websiteCount=0）：左侧 gauge 隐藏，整张卡片用居中提示「暂无上传网站，影响力数据将在网站审核通过后开始累计」
- **加载态**：左侧 gauge skeleton 圆环 + 5 行 legend 骨架，右侧 5 行 sparkline 骨架
- **错误态**：错误条 + 重试按钮，复用 `ProfileDashboardSummary` 错误样式

#### 动画与微交互

- 卡片入场：整体 `opacity 0→1 + translateY 4px → 0`，`400ms ease-out`
- Gauge 5 弧 stagger 增长：`100ms × 5 = 500ms` 总时长
- Sparkline 柱 stagger：每柱 `20ms` 延迟，单柱 `scaleY 0→1` `300ms`
- range 切换：左右两栏同时 `opacity 0.4 → 1` `200ms`，避免抖动

### 4.3 Store 与 API

#### `@/api/user-profile-impact.ts`

```ts
export type ImpactRange = '7d' | '30d' | '90d' | 'all'

export interface UploadImpact {
  range: ImpactRange
  websiteCount: number
  totals: MetricGroup
  rangeCounts: MetricGroup
  delta: DeltaGroup
  points: DailyPoint[]
}

export interface MetricGroup {
  browse: number; like: number; collect: number; comment: number; score: number
}

export interface DeltaGroup {
  browse: number | null; like: number | null; collect: number | null
  comment: number | null; score: number | null
}

export interface DailyPoint {
  date: string
  browse: number; like: number; collect: number; comment: number; score: number
}

export async function getUploadImpact(
  range: ImpactRange = '30d',
  context?: { username?: string }
): Promise<UploadImpact>
```

URL 构造逻辑与 `user-profile-dashboard.ts` 一致：
- `context.username` 存在 → `/api/user/profile/public/{username}/dashboard/upload-impact`
- 否则 → `/api/user/profile/dashboard/upload-impact`

#### `@/stores/profileImpact.ts`

```ts
export const useProfileImpactStore = defineStore('profileImpact', () => {
  const impact = ref<SectionState<UploadImpact>>(initial())
  const range = ref<ImpactRange>('30d')
  const context = ref<{ username?: string }>({})

  const load = async (r?: ImpactRange, ctx?: { username?: string }) => {
    if (r && r !== range.value) range.value = r
    if (ctx) context.value = ctx.username ? { username: ctx.username } : {}
    await handle(impact, () => getUploadImpact(range.value, context.value))
  }

  const setRange = (r: ImpactRange) => load(r)
  const reset = () => { /* ... */ }

  return { impact, range, context, load, setRange, reset }
})
```

不并入 `profileDashboard` store 的理由：
1. `range` 集合不同（多了 `all`）。
2. 缓存策略独立，与 dashboard 的 5min 二级缓存解耦。
3. 公开页与登录页共用同一 store + 不同 `context.username`，沿用 dashboard 既有模式。

### 4.4 边界与空态

| 场景 | 表现 |
|---|---|
| `websiteCount = 0` | 整区块显示空态：`暂无数据`（lucide `Inbox` 图标 + 文字） |
| 加载中 | 5 仪表盘 + 5 柱图区域骨架屏（`animate-pulse rounded-md bg-black/5 dark:bg-white/5`），与 `ProfileDashboardSummary` 风格一致 |
| 接口失败 | 区块内错误条 + 重试按钮，不影响其他 ProfileView 区块（独立 store） |
| 公开页用户不存在 | `ProfilePublicView` 已经先于 dashboard 兜底 404，组件不需自己处理 |
| `range = all` | 仪表盘隐藏 → 静态总额数字；柱图 → 5 行横向总计大柱 |

---

## 5. 缓存策略

| 维度 | 值 |
|---|---|
| Key 模板 | `user:profile:dashboard:{userId}:upload-impact:{range}` |
| Key 前缀 | 复用 `RedisConstant.USER_PROFILE_DASHBOARD_PREFIX` |
| TTL | 600s（10 分钟） |
| Jitter | 60s |
| 空值缓存 | 启用（`RedisCache` 默认行为） |
| 失效策略 | **纯 TTL 自然过期**，不在写路径主动失效 |

**为什么不在写路径主动失效**：

1. 当用户 A 给用户 B 的网站点赞时，要失效的是 B 的 owner 缓存。要在 `UserStatsAggregateServiceImpl` 中查 `t_website.submitter_id` 反查 owner，引入额外 query + 给写路径加耦合。
2. 10 分钟延迟对"影响力面板"完全可接受，这不是实时账户。
3. 现有 `evictDashboardCache(userId)` 按操作者 userId 做 SCAN+UNLINK，**不会误伤** owner 缓存（key 中的 userId 不同）。

**P2 演进点**：未来如果对实时性有诉求，在 `UserStatsAggregateServiceImpl.apply()` 末尾追加：

```java
Long ownerId = websiteMapper.selectSubmitterIdById(event.getWebsiteId());
if (ownerId != null) {
    redisCache.evictByPattern(USER_PROFILE_DASHBOARD_PREFIX + ownerId + ":upload-impact:*");
}
```

---

## 6. 性能预算

| 场景 | 估算 |
|---|---|
| 缓存命中（>95% 请求） | <5ms |
| miss + 上传站 ≤ 50 + `t_browse_history` 1M 行 | 单查 200~600ms；5 表并发 600~1000ms |
| miss + 上传站 ≤ 50 + `t_browse_history` 10M 行 | 单查 2~5s（无索引下的实际风险，需 P2 补） |
| `range = all` | 跳过事件表查询，仅 SUM `t_website` ≤ 50ms |

保护措施：
- `@RateLimit` 限制单用户每秒 5 次。
- 5 表并发用 `CompletableFuture.allOf`，墙钟时间 ≈ 最慢的单表。
- 慢日志触发 → 一次性跑 6 索引 DDL（10 行 ALTER）。

---

## 7. 测试范围

### 7.1 后端单元测试 `UploadImpactServiceImplTest`

1. 用户无审核通过网站 → websiteCount=0、totals/rangeCounts 全 0、points 空、delta 全 0。
2. `range=7d`：mock 5 表数据，验证 points 长度=7、按日期升序、缺失日补 0。
3. `range=30d`：验证 delta 计算（含 prev=0/curr>0 → 100、都为 0 → 0、负值场景）。
4. `range=all`：跳过事件表 mock，验证 points=空、delta 全 null、rangeCounts=totals。
5. 评论表 `deleted=0 AND status=1` 过滤：1 条软删评论被排除。
6. 已下架 (`status=0`) 网站的事件**仍计入**。

### 7.2 后端集成测试 `ProfileUploadImpactControllerTest`

1. 登录用户 GET 自身接口 → 200 + VO 形态正确。
2. 匿名 GET 公开接口 + 不存在 username → 404。
3. 触发 `RateLimit` → 429。
4. 缓存路径：连续两次 GET，第二次 hit Redis（`RedisCache` mock 验证）。

### 7.3 前端单测（vitest）

`profileImpact.spec.ts`：
1. `setRange('all')` 触发 reload。
2. API 失败 → state='error' + error msg。
3. 切换 context（`username` ↔ self）重置数据。

`ImpactBarChart.spec.ts`：
1. 给定 30 个数据点 + max → 验证渲染 30 根柱、最高柱有高光、0 值柱保留地基线。

### 7.4 E2E（Playwright，1 个冒烟用例）

打开 `/profile` → 等到「我的网站影响力」区块 → 点击「近 7 天」→ 仪表盘指针动画 + 柱图重渲染。

---

## 8. 实施切片

| Slice | 内容 | 工时 |
|---|---|---|
| 1. 后端骨架 | VO + Service + Mapper + Controller + 单测 + 集成测试 | 0.5d |
| 2. 前端组件 | API + Store + 4 组件 + 双视图接入 + 单测 + E2E 冒烟 | 0.5d |
| 3. 联调与回归 | 4 range 切换、暗黑模式、响应式、公开页匿名 | 0.25d |

总：**1.5~2 人日**，3 个独立 PR。

---

## 9. 风险登记

| 风险 | 概率 | 影响 | 兜底 |
|---|---|---|---|
| 大数据量下 `t_browse_history` 慢查询 | 中 | 接口 P95 飙到 2~5s | 慢日志触发后补 6 索引 DDL |
| 用户取消点赞/收藏 → 历史柱图非单调（事件表物理删） | 高 | 视觉上某日点赞数会下降 | 设计上接受——这正是真实历史；不做特殊处理 |
| ECharts bar 暗黑模式色板溢出 | 低 | 视觉小毛病 | 单测覆盖暗色模式快照 |
| TTL 10min 在 demo 时显得"延迟" | 低 | UX 抱怨 | 明文注明；必要时改 60s + P2 主动失效 |

---

## 10. 决策记录

| # | 决策 | 备选 | 理由 |
|---|---|---|---|
| 1 | 新增 + 保留原看板 | 替换/隐藏原 | 双口径区分清晰：「我的活跃数据」vs「我的网站影响力」 |
| 2 | 5 指标色板（指标维度色） | 每柱一色 / 柱高驱动色阶 | 语义清晰，主流做法 |
| 3 | 5 个并列迷你仪表盘 | 1 主 + 5 迷你 / 1 主 + 右侧网格 | 信息密度高、视觉对称 |
| 4 | 仪表盘指针 = 本期/上期环比 | 总额达成 / P95 分位数 / 混合 | 计算成本最低，反映「增长趋势」 |
| 5 | range=all 时柱图变 5 总计大柱 | 复用 90d / 按月聚合 | 后端零额外查询 |
| 6 | 实时聚合 + Redis Cache-Aside | 预聚合表 / 定时快照 | 代码最少、零事件链路侵入、数据准确 |
| 7 | 公开端可匿名访问 | 仅本人 / 隐私设置控制 | GitHub Profile 风格，扩大产品价值 |
| 8 | 不加索引（本期） | 加全部 6 个索引 | 用户决策，列入 P2 |
| 9 | 评分指标 = teal-500 | indigo / violet | 规则禁紫，避免与 amber 撞色 |
| 10 | 柱图 = ECharts bar 去默认风格 | 纯 SVG 手画 | 复用现有依赖，去样式细则可控 |
| 11 | 5 行垂直堆叠柱图 | 5 列横向 | 单图更宽、更易读 |
| 12 | 空态文案 = 「暂无数据」 | 自定义引导文案 | 简洁中性 |
| 13 | 缓存纯 TTL 失效 | 写路径主动失效 | 零侵入、10min 延迟可接受 |
| 14 | 下架网站历史保留 | 排除 | 仅过滤 deleted+audit_status，下架前的影响力仍归用户 |
| 15 | 前端整体改 GitHub Stats 风 | 普通后台仪表盘 | 用户明确要求紧凑/克制/工程化、避免大空白、信息密度高 |
| 16 | 5 指标色板改 GitHub Primer | 原 sky/rose/amber/emerald/teal | 工程感更强、避免甜腻撞色，与 GitHub Stats 卡片一致 |
| 17 | 左侧 = 单 270° 半圆 5 弧叠加 + Legend | 5 并列迷你仪表盘 | 用户决策；按 share 拼成一个 gauge 而非每指标独立仪表 |
| 18 | 右侧 = 5 行 24px 极紧凑 sparkline | 5 行普通柱图 / 5 列网格 | 用户决策；最贴 GitHub commit chart 视觉 |
| 19 | range tabs 文案改英文短词 `7d/30d/90d/All` | `近 7 天` 中文 | GitHub Stats 风格统一，节省空间 |

---

## 11. 引用

- `computer-favorites-web/src/views/user/ProfileView.vue:272-277` 接入位置
- `computer-favorites-web/src/components/user/profile/ProfileDashboardSummary.vue` 原看板（保留）
- `computer-favorites-web/src/stores/profileDashboard.ts` Store 模式参考
- `computer-favorites-web/src/api/user-profile-dashboard.ts` API 模式参考
- `computer-favorites-back/cf-web/src/main/java/com/yyyouth/web/controller/user/ProfileDashboardController.java` 控制器模式参考（登录端追加位置）
- `computer-favorites-back/cf-web/src/main/java/com/yyyouth/web/controller/user/ProfilePublicController.java` 公开端控制器（追加 case 位置）
- `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/user/userstats/ProfilePublicService.java` 公开端 userId 解析 + 隐私校验
- `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/user/userstats/impl/ProfileDashboardServiceImpl.java` Service 模式参考
- `computer-favorites-back/cf-service/src/main/java/com/yyyouth/service/redis/RedisCache.java` Cache-Aside 工具
- `computer-favorites-back/cf-common/src/main/java/com/yyyouth/common/constants/RedisConstant.java` Redis Key 前缀
