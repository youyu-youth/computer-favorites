# 管理端全局面包屑组件替换设计（PrimeVue Breadcrumb）

## 1. 背景
当前管理端网站模块的面包屑由业务组件手写实现，位于 `src/components/admin/websites/WebsitesBreadcrumbs.vue`。现有实现存在以下问题：
- 复用边界不清晰，无法作为全局通用组件沉淀到 `ui-adapter`。
- 交互与样式逻辑和业务代码耦合，后续扩展成本高。
- 面包屑组件尚未使用 PrimeVue Breadcrumb 体系，不利于统一组件标准。

本次目标是将管理端面包屑统一迁移到全局通用组件，并保持现有视觉风格与交互语义。

## 2. 已确认决策
- 方案选择：采用“全局 `UBreadcrumb` 组件”方案。
- 替换范围：管理端全部面包屑先统一替换。
- 点击规则：所有层级均可点击。
- 添加网站层行为：点击后保持当前页面（无副作用跳转）。
- 样式规则：禁止使用 PrimeVue 默认样式，必须适配当前主题体系，支持响应式、移动端和明暗模式。

## 3. 目标与非目标
### 3.1 目标
- 在 `ui-adapter` 新增全局通用 `UBreadcrumb`，内部基于 PrimeVue Breadcrumb（unstyled 模式）。
- 在管理端业务组件中改用 `UBreadcrumb`，不再手写原生面包屑结构。
- 保持与现有视觉一致：颜色、间距、滚动行为、分隔符风格。
- 实现“全部层级可点击”且行为可配置。

### 3.2 非目标
- 本次不改造管理端顶部模块切换按钮区（移动端/桌面端菜单区域保留）。
- 本次不新增用户端面包屑替换。
- 本次不引入新的全局路由结构重构。

## 4. 组件边界与职责
### 4.1 `UBreadcrumb`（全局通用）
建议新增文件：`src/components/ui-adapter/UBreadcrumb.vue`

职责：
- 封装 PrimeVue Breadcrumb 渲染。
- 提供统一的 unstyled + Tailwind 主题样式。
- 接收标准化数据项并处理点击行为。
- 提供移动端横向滚动与无障碍语义。

### 4.2 `WebsitesBreadcrumbs`（业务组装层）
文件：`src/components/admin/websites/WebsitesBreadcrumbs.vue`

职责：
- 从 `adminNavStore` 和 `viewMode` 组装面包屑业务数据。
- 将数据映射到 `UBreadcrumb` 所需结构。
- 定义每个层级点击行为（路由跳转或状态切换）。

## 5. 数据模型与交互协议
建议在 `UBreadcrumb` 组件中定义如下 item 结构：

```ts
type UIBreadcrumbItem = {
  key: string
  label: string
  current?: boolean
  clickable?: boolean
  to?: import('vue-router').RouteLocationRaw
  onClick?: () => void | Promise<void>
}
```

处理优先级：
1. 若存在 `onClick`，优先执行 `onClick`。
2. 若不存在 `onClick` 且存在 `to`，执行 `router.push(to)`。
3. 其余情况无副作用返回。

错误处理：
- `router.push` 异常统一捕获并忽略控制台噪声，不中断页面。
- 不因单个 item 执行失败影响其他 item 渲染。

## 6. 跳转规则设计（网站管理模块）
在 `WebsitesBreadcrumbs` 内按当前已存在路由和状态实现：

- `管理后台`：点击跳转到 `adminWebsites`。
- `当前模块`（如网站管理）：点击触发模块激活逻辑，并保持页面一致。
- 第三级 `添加网站`：点击后保持当前页面，不跳转（按已确认规则）。
- 第三级 `修改网站`：点击保持当前编辑页面（可执行同路由 push 或无副作用）。
- 第三级 `分类标签`：点击回到网站管理列表视图，并保持当前分类上下文。

## 7. 视觉与主题规范
### 7.1 样式原则
- 禁用 PrimeVue 默认样式，所有视觉由 Tailwind 类控制。
- 与当前原生面包屑视觉保持一致：
  - 文本字号与颜色层级不倒退。
  - 分隔符使用右箭头图标。
  - 当前层级字体强调。

### 7.2 明暗模式
- 亮色模式：主文本深色、弱文本灰色、hover 橙色强调。
- 暗色模式：文本高对比，hover 与 focus 可见性明确。

### 7.3 响应式与移动端
- 面包屑容器支持横向滚动。
- 移动端单行不换行，长文本截断展示。
- 保留滚动条隐藏策略，与现有 `menu-scroll` 体验一致。

### 7.4 可访问性
- 当前层级标记 `aria-current="page"`。
- 可点击项使用可聚焦语义元素，支持键盘触发。
- 提供 focus ring，避免键盘用户无反馈。

## 8. 实施步骤
1. 新增 `UBreadcrumb.vue` 到 `src/components/ui-adapter/`。
2. 在 `src/plugins/registerUiAdapter.ts` 中全局注册 `UBreadcrumb`。
3. 改造 `WebsitesBreadcrumbs.vue`：
   - 删除当前手写面包屑模板块。
   - 新增业务数据到 `UBreadcrumb` 的映射。
   - 接入层级点击行为。
4. 保留并不改动当前菜单切换区（`border-b` 下方 mobile/desktop 按钮区）。
5. 验证管理端两处引用页面：
   - `src/views/admin/WebsitesManagementView.vue`
   - `src/views/admin/AdminWebsiteEditView.vue`

## 9. 验证计划
- 类型检查：`npm run type-check`
- 构建检查：`npm run build-only`
- 手工交互验证：
  - 面包屑所有层级可点击。
  - “添加网站”点击无页面跳转。
  - 明暗模式切换下颜色可读。
  - 移动端横向滚动和布局不破坏。
  - 编辑页和列表页行为一致。

## 10. 风险与兜底
- 风险：PrimeVue Breadcrumb 插槽结构与项目语义化按钮不一致，可能导致可访问性退化。
  - 兜底：在 `UBreadcrumb` 内统一渲染可聚焦交互节点并补充 ARIA。
- 风险：模块态页面不是独立路由，点击语义容易与“跳转页面”认知冲突。
  - 兜底：在业务层明确区分“路由跳转”和“状态切换”，保持行为可预测。
- 风险：长文案导致移动端可读性下降。
  - 兜底：强制单行滚动 + 截断 + title 提示。

## 11. 交付结果定义
满足以下条件即视为完成：
- 管理端网站模块已完全由 `UBreadcrumb` 驱动，不再保留手写原生面包屑模板。
- 视觉与交互满足当前主题规范（响应式、移动端、明暗模式）。
- 全部层级点击行为符合已确认规则。
- 类型检查与构建通过。