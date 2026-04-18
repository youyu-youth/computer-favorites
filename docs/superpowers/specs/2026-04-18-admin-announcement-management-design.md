# 管理端公告管理页面设计文档

## 1. 背景与目标
- 面向管理端新增一个“平台公告管理”页面，服务于公告的查询、查看、新增、编辑、显示/隐藏、置顶及批量操作。
- 当前阶段仅实现前端页面与本地假数据交互，不接入后端接口，但页面结构和数据契约需要为后续联调预留清晰边界。
- 页面需对齐现有管理端工作台模式，保持高信息密度、强操作效率，并完整适配响应式布局与暗黑/明亮主题。

## 2. 设计结论
- 页面采用“标准管理”范围，不扩展为内容运营编排页。
- 页面主体采用“双栏运营台”结构：
  - 左侧为公告列表工作台，承担筛选、勾选、批量操作与行级快捷操作。
  - 右侧为常驻详情预览面板，承担当前选中公告的详细信息展示与二次操作入口。
- 新建与编辑统一采用弹窗表单，不单独拆编辑路由。
- 视觉风格严格复用管理端主题令牌，不引入页面私有色板，不使用渐变。

## 3. 参考基线
- 布局基线：`computer-favorites-web/src/layouts/AdminLayout.vue`
- 页面组织基线：
  - `computer-favorites-web/src/views/admin/AdminFeedbackManagementView.vue`
  - `computer-favorites-web/src/views/admin/AdminReportManagementView.vue`
- 可复用公共组件：
  - `computer-favorites-web/src/components/admin/common/AdminPagination.vue`
  - `computer-favorites-web/src/components/admin/common/AdminSelect.vue`
- 主题基线：
  - `computer-favorites-web/src/theme/admin/tokens.css`
  - `computer-favorites-web/src/theme/admin/tokens.ts`
- 数据表基线：
  - `docs/sql/computer-favorites.sql` 中 `t_announcement`

## 4. 数据结构约束
`t_announcement` 相关字段如下：

```sql
id
title
content
type
is_top
status
publish_time
create_time
update_time
deleted
```

其中类型语义固定为：
- `1`：新增内容
- `2`：Bug 修复
- `3`：更新系统

当前前端假数据模型定义为：

```ts
type AnnouncementRecord = {
  id: number
  title: string
  content: string
  type: 1 | 2 | 3
  isTop: 0 | 1
  status: 0 | 1
  publishTime: string | null
  createTime: string
  updateTime: string
}
```

页面内可额外派生以下展示字段，但不单独持久化：
- `typeLabel`
- `statusLabel`
- `contentPreview`
- `publishTimeText`

## 5. 页面信息架构

### 5.1 顶部区域
- 面包屑
- 页面标题“公告管理”
- 简短说明文案
- 主按钮“新建公告”

### 5.2 概览统计区
展示 4 张统计卡：
- 公告总数
- 已发布/显示
- 已隐藏
- 置顶中

### 5.3 筛选工具栏
提供以下筛选能力：
- 关键词
- 状态
- 类型
- 是否置顶
- 刷新

工具栏右侧提供以下操作入口：
- 新建公告
- 批量显示
- 批量隐藏
- 批量取消置顶

### 5.4 主内容区
- 左栏：公告列表表格 + 分页
- 右栏：详情预览面板

### 5.5 弹窗表单
统一用于：
- 新建公告
- 编辑公告

## 6. 组件拆分方案

### 6.1 路由视图
`AdminAnnouncementManagementView.vue`

职责：
- 组装页面
- 接入 composable
- 串联子组件事件
- 呈现页面级反馈条

### 6.2 功能组件

`AnnouncementsBreadcrumbs.vue`
- 渲染面包屑、标题与说明文案

`AnnouncementsOverviewCards.vue`
- 渲染统计卡片

`AnnouncementsToolbar.vue`
- 渲染筛选区、刷新按钮、新建按钮、批量操作按钮

`AnnouncementsTable.vue`
- 渲染桌面端表格
- 支持行点击、勾选、快捷操作

`AnnouncementDetailPanel.vue`
- 渲染当前选中公告的右侧预览区
- 提供编辑、显示/隐藏、置顶/取消置顶入口

`AnnouncementFormDialog.vue`
- 渲染新建/编辑弹窗
- 管理表单展示与提交事件

### 6.3 复用组件
- 直接复用 `AdminPagination.vue`
- 下拉选择优先复用 `AdminSelect.vue`
- 弹窗容器优先复用现有 UI Adapter 的弹窗模式

### 6.4 状态编排
新增 composable：

`useAdminAnnouncementManagement`

职责：
- 管理本地假数据
- 管理筛选条件
- 管理分页
- 管理多选状态
- 派生统计数据
- 管理详情预览对象
- 管理弹窗表单状态
- 封装单条与批量操作

建议输出：

```ts
loading
query
pagedAnnouncements
statistics
selectedIds
selectedCount
detailOpen
detailRecord
formDialog
setKeyword()
setStatus()
setType()
setIsTop()
refreshData()
openDetail()
closeDetail()
openCreateDialog()
openEditDialog()
closeFormDialog()
updateForm()
submitForm()
toggleAnnouncementStatus()
toggleAnnouncementTop()
batchShow()
batchHide()
batchCancelTop()
prevPage()
nextPage()
goToPage()
```

## 7. 交互设计

### 7.1 默认进入页面
- 页面加载后显示统计卡、筛选栏、列表和详情区。
- 若存在数据，默认选中第一页第一条公告，并同步右侧详情预览。
- 若无数据，列表区显示空状态，并保留“新建公告”入口。

### 7.2 筛选与刷新
- 关键词、状态、类型、置顶支持组合筛选。
- 任一筛选条件变化时，页码重置为第一页。
- 点击“刷新”后重新计算当前结果，并显示成功或失败反馈条。

### 7.3 行级操作
- 点击表格行：切换当前预览公告。
- 点击“编辑”：打开弹窗并回填数据。
- 点击“显示/隐藏”：切换 `status`。
- 点击“置顶/取消置顶”：切换 `isTop`。

操作成功后需同步更新：
- 列表状态
- 统计卡片
- 右侧预览区
- 页面反馈条

### 7.4 批量操作
- 勾选多条记录后启用批量按钮。
- 支持：
  - 批量显示
  - 批量隐藏
  - 批量取消置顶
- 批量操作完成后，更新列表、统计与详情区，并清理当前选择。

### 7.5 新建与编辑弹窗
- 新建时打开空白表单。
- 编辑时按当前记录回填。
- 关闭弹窗不污染原始列表数据。
- 保存成功后更新本地数据源，并关闭弹窗。

## 8. 弹窗表单设计

### 8.1 字段
- 标题
- 类型
- 内容
- 是否置顶
- 状态
- 发布时间

### 8.2 校验
- 标题必填
- 内容必填
- 标题长度不超过 200
- 发布时间本期允许为空

### 8.3 布局规范
- 最大宽度控制在 `820px - 880px`
- 最大高度控制在 `82vh`
- 采用“头部固定 + 内容滚动 + 底部固定”布局

### 8.4 按钮
- 取消
- 保存

## 9. 列表与详情展示规范

### 9.1 桌面端表格列
- 选择框
- 标题
- 类型
- 状态
- 置顶
- 发布时间
- 更新时间
- 操作

### 9.2 详情预览区信息
- 标题
- 类型标签
- 状态标签
- 是否置顶
- 发布时间
- 创建时间
- 更新时间
- 正文摘要

详情区操作：
- 编辑
- 显示/隐藏
- 置顶/取消置顶

## 10. 响应式策略

### 10.1 大屏 `>= 1280px`
- 双栏运营台布局
- 统计卡四列横排
- 右侧详情面板常驻

### 10.2 中屏 `768px - 1279px`
- 保持双栏
- 右侧面板宽度收缩
- 表格仅保留核心列

### 10.3 小屏 `< 768px`
- 页面切为单列
- 表格退化为公告卡片列表
- 详情改为抽屉
- 筛选改为折叠式入口
- 批量操作入口聚合展示，避免首屏拥挤

## 11. 视觉与主题规范
- 严格使用管理端主题令牌与 `dark:` 双态类名。
- 不使用渐变。
- 不使用紫色系主视觉。
- 图标统一使用 Font Awesome。
- 主色使用管理端蓝色语义。
- 状态色建议：
  - 显示：绿色
  - 隐藏：灰蓝
  - 置顶：琥珀

同时满足以下约束：
- 亮色和暗色下的子组件颜色必须完整适配，不能只调整外层容器。
- 所有可点击按钮或操作项均需带指针手势。
- 状态表达不能只靠颜色，必须保留文字标签。

## 12. 假数据策略
- 前端先使用本地假数据，不接后端。
- 假数据建议条数为 `10 - 16` 条。
- 数据需覆盖：
  - 已显示
  - 已隐藏
  - 置顶
  - 长标题
  - 长正文
  - 不同发布时间

本地行为定义：
- 新建：插入数组头部
- 编辑：按 `id` 更新
- 显示/隐藏：切换 `status`
- 置顶/取消置顶：切换 `isTop`
- 批量操作：批量更新选中项

所有以下能力都从同一份数据源派生：
- 列表
- 分页
- 统计卡
- 详情区
- 筛选结果

## 13. 本期实现边界
本期纳入：
- 公告管理页
- 本地假数据
- 统计卡
- 筛选
- 表格/卡片列表
- 详情预览
- 新建/编辑弹窗
- 显示/隐藏
- 置顶/取消置顶
- 批量操作
- 响应式
- 暗黑/明亮模式

本期不纳入：
- 后端接口接入
- 富文本编辑器
- 真实定时发布任务
- 删除确认链路
- 审计日志联动

## 14. 风险与注意事项
- 弹窗表单用于长正文编辑时，必须控制滚动区域，否则移动端体验会明显下降。
- 表格与详情区需共享同一当前选中记录源，避免左侧状态变化后右侧预览不同步。
- 编辑表单不能直接绑定原始列表对象，避免未保存内容污染视图。
- 移动端不能仅依赖宽表格横向滚动，必须提供卡片化退化方案。

## 15. 验收标准
- 页面风格与现有管理端一致，并适配 `AdminLayout`。
- 页面可在桌面端、平板端、移动端正常使用。
- 亮色与暗色模式下，页面及所有子组件均表现一致。
- 假数据可完整跑通查询、查看、新建、编辑、显隐、置顶、批量操作链路。
- 组件职责清晰，路由视图不承载大段业务逻辑。
