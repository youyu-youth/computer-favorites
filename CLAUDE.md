# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作提供指导。

---

## 项目概述

**计算机专业个人网页收藏夹展示平台** — 面向 CS 学习者的全栈平台，提供优质学习资源的发现、收藏与管理功能，含管理后台、AI 助手和社区互动。

**技术栈：**
- 后端：Spring Boot 3.4.12 + Java 17 + MyBatis-Plus 3.5.14 + Sa-Token 1.44.0 + Redis + RabbitMQ
- 前端：Vue 3 + TypeScript 5 + Vite + Pinia + Tailwind CSS 4 + PrimeVue 4
- 数据库：MySQL 8.x（utf8mb4_unicode_ci）
- 中间件：MinIO（文件存储）、Spring AI 1.0.0（对话）、RabbitMQ（消息）

---

## 常用命令

### 后端（`computer-favorites-back/`）

```bash
# 构建所有模块
mvn clean install

# 以 dev 配置启动（端口 18081）
mvn -pl cf-core spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 运行指定模块测试
mvn -pl cf-core test
```

### 前端（`computer-favorites-web/`）

```bash
npm install
npm run dev          # 启动开发服务器，代理到 localhost:18080
npm run build        # 生产构建（含类型检查）
npm run type-check   # 仅 TypeScript 类型校验
npm run lint         # oxlint + ESLint 自动修复
npm run format       # Prettier 格式化
npm run e2e          # Playwright E2E 测试
```

**Node.js 要求：** `^20.19.0 || >=22.12.0`

---

## 架构说明

### 后端模块结构

```
computer-favorites-back（父 pom）
├── cf-common      — 工具类、异常、拦截器、常量、HttpResult 统一响应
├── cf-model       — POJO、VO、DTO、BO、枚举（不含业务逻辑）
├── cf-service     — 中间件封装：Redis、RabbitMQ、MinIO
├── cf-web         — Controller + 业务 Service（依赖以上所有模块）
└── cf-core        — 应用启动入口、资源配置文件
```

`cf-web` 下的 Controller 和 Service 按功能模块组织，例如：
- `web/user/` — 认证、个人资料、设置
- `web/websites/` — 资源浏览、投稿
- `web/admin/` — 审核、分类、标签管理

统一响应类型：`cf-common` 中的 `HttpResult<T>`。

### 前端目录结构

```
src/
├── api/          — 所有 API 调用统一汇总于 index.ts
├── components/   — auth/、common/、user/、admin/、ui-adapter/
├── composables/  — 可复用的 Vue 组合式逻辑
├── layouts/      — UserLayout、AdminLayout
├── router/       — 路由定义
├── stores/       — Pinia 状态管理（app、auth、website）
├── theme/        — admin/ 和 user/ 主题配置
├── views/        — 页面组件（auth/、user/、admin/）
└── types/        — TypeScript 类型定义
```

---

## 编码规范

### 后端

- **语言：** 代码实体（类名、方法名、变量名）使用英文；注释使用中文。
- **REST 动词：** GET=查询，POST=新增，PUT=修改，DELETE=删除。
- **参数校验：** 使用 Jakarta Validation 注解（`@NotNull`、`@NotBlank` 等），禁止手动 if 判断参数。
- **对象构造：** 使用 Lombok `@Builder`，不用 setter 赋值。
- **依赖注入：** `@RequiredArgsConstructor` 构造器注入，禁止 `@Autowired` 字段注入。
- **日志：** 每个 Controller 方法必须有日志记录。
- **权限格式：** `模块:功能:操作`（如 `website:resource:list`）。
- **软删除：** 使用 `deleted` 字段标记，不做物理删除。
- **审计字段：** 所有表必须包含 `create_time` / `update_time`。

### 前端

- **路由：** 所有路径遵循 `/computer/{业务}` 约定（如 `/computer/login`、`/computer/home`）。
- **导入：** 跨文件引用统一使用 `@/` 别名，禁止跨功能目录使用相对路径。
- **UI 组件：** PrimeVue（unstyled 模式）+ Tailwind CSS，**禁止使用 PrimeVue 默认样式**。
- **主题：** 每个组件必须同时支持亮色和暗色模式。
- **图标：** 仅使用 SVG，禁止 emoji 图标。
- **样式限制：** 不用渐变色；不用紫色；所有可点击元素必须设置 `cursor-pointer`。
- **响应式：** 必须同时支持桌面端和移动端（H5）。

### Redis 键命名

格式：`模块:biz:{id}`，例如 `website:detail:42`

使用 Cache-Aside 模式：所有键设置 TTL、空值缓存防穿透、Redisson 互斥锁防击穿、Jackson2 序列化。

---

## 数据库

共 29 张表（24 张核心表 + 5 张补充表）。

**核心领域：**
- 用户：`t_user`、`t_user_profile`、`t_user_setting`、`t_admin`、`t_oauth_bind`
- 内容：`t_website`、`t_category`、`t_tag`、`t_tech_stack`
- 行为：`t_user_folder`、`t_user_collect`、`t_website_like`、`t_comment`、`t_comment_like`、`t_website_score`、`t_browse_history`
- 运营：`t_message`、`t_feedback`、`t_report`、`t_sensitive_word`、`t_announcement`、`t_banner`、`t_friend_link`、`t_system_config`

**补充表（来自 `database-补充脚本.sql`）：**
`t_user_session`、`t_ai_conversation`、`t_ai_message`、`t_website_draft`、`t_audit_log`

完整建表脚本：`docs/sql/computer-favorites.sql` + `docs/sql/database-补充脚本.sql`

---

## 模块实现状态

状态矩阵维护在 `docs/spec-v2/01-模块状态矩阵.md`，每个模块对应一份 spec 文档和一份 tasks 文件。

**✅ 已完成：** user-01（认证）、user-02（用户中心）、user-03（资源发现）、user-06（网站投稿）、user-08（消息通知）、admin-01（审核流程）、admin-02（网站管理）、admin-04（分类管理）、admin-05（标签管理）

**🟡 待开发：** user-04（互动行为）、user-05（收藏管理）、user-07（AI 助手）、admin-03（导入导出）、admin-06～admin-15（用户管理、举报处理、敏感词、反馈、公告、Banner、友情链接、系统配置、审计日志、数据统计）

实现某模块时：
1. 参阅 `docs/spec-v2/user/` 或 `docs/spec-v2/admin/` 下对应的 spec 文件
2. 完成后更新 `docs/spec-v2/01-模块状态矩阵.md` 中的状态

---

## 关键参考文件

| 文件 | 用途 |
|------|------|
| `.claude/rules/back-end.md` | 后端 DDD/TDD 架构规则 |
| `.claude/rules/back-coding-standards.md` | 后端代码风格规范 |
| `.claude/rules/bakc-ddd.md` | 领域驱动设计规范 |
| `.claude/rules/front-end.md` | 前端组件与样式规则 |
| `.claude/rules/redis-coding-standards.md` | Redis 使用规范 |
| `docs/01-核心需求文档.md` | 核心需求（角色权限、数据模型、优先级） |
| `docs/spec-v2/00-开发计划总览.md` | 23 个模块的开发路线图 |
| `cf-core/src/main/resources/application-dev.yml` | 本地开发配置 |

---

## 本地环境配置

| 服务 | 默认值 |
|------|--------|
| 后端端口 | 18081（dev） |
| 前端代理目标 | localhost:18080 |
| MySQL | localhost:3306 |
| Redis | localhost:6379，DB 5 |
| MinIO | localhost:9000 |

可通过环境变量覆盖默认值：`CF_DB_*`、`CF_REDIS_*`、`CF_MINIO_*`、`CF_MAIL_*`
