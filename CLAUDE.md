# AGENTS.md

本文件为 claude code 在此仓库中工作提供指导。仅包含"不写下来就容易踩坑"的信息。

---

## 项目概述

**计算机专业个人网页收藏夹展示平台** — 面向 CS 学习者的全栈平台，含用户端、管理后台和 AI 对话助手。

- 后端：`computer-favorites-back/`（Spring Boot 3.4.12 + Java 17 + MyBatis-Plus 3.5.14 + Sa-Token 1.44.0）
- 前端：`computer-favorites-web/`（Vue 3 + TS + Vite + Pinia + Tailwind CSS 4 + PrimeVue 4）
- 数据库：MySQL 8.x（utf8mb4_unicode_ci），共 30 张表
- 中间件：Redis 7.x + Redisson 3.45.0、RabbitMQ 3.x、MinIO 7.1.x、Spring AI 1.0.0

---

## 规则文件索引

| 文件 | 核心内容 |
|------|---------|
| `./rules/back-end.md` | DDD/TDD 方法论、技术栈版本、Maven 模块结构、包结构、注入/分层/权限规范 |
| `./rules/back-coding-standards.md` | REST 接口规范、Jakarta Validation、@Builder、行尾注释禁令、类职责拆分 |
| `./rules/bakc-ddd.md` | 领域模型组织、分层架构、统一语言、领域事件 |
| `./rules/front-end.md` | 响应式/暗黑模式/PrimeVue+Tailwind 规范、技术栈、目录结构、主题规范 |
| `./rules/redis-coding-standards.md` | Cache-Aside 模式、TTL+抖动/空值缓存/互斥锁/序列化、键设计 |

## 常用命令

### 后端（`computer-favorites-back/`）

```bash
mvn clean install                                                          # 构建全部模块
mvn -pl cf-core spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"   # dev 启动（端口 18081）
mvn -pl cf-core spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"  # local 启动（端口 8080，默认 profile）
mvn -pl cf-core test                                                       # 运行测试
```

### 前端（`computer-favorites-web/`）

```bash
npm install           # 安装依赖
npm run dev           # 开发服务器（代理 /api → 127.0.0.1:18080）
npm run build         # 生产构建（含 type-check）
npm run build-only    # 仅 vite build，不做类型检查
npm run type-check    # vue-tsc 类型校验
npm run lint          # oxlint + ESLint 自动修复
npm run format        # Prettier 格式化
npm run e2e           # Playwright E2E 测试
npm run preview       # 预览生产构建
```

**Node.js 要求：** `^20.19.0 || >=22.12.0`；根目录 `package.json` 无工程脚本，前端命令仅在 `computer-favorites-web/` 执行。

---

## 架构要点

### 后端模块依赖（不可逆）

```
cf-core → cf-web → { cf-service, cf-model, cf-common }
                   cf-service → { cf-model, cf-common }
                   cf-model → cf-common
```

- `cf-common`：HttpResult、常量、工具类、异常、AOP、拦截器  （单一原则优先）
- `cf-model`：POJO/VO/DTO/BO/枚举，**不含业务逻辑**
- `cf-service`：Redis/RabbitMQ/MinIO 中间件封装
- `cf-web`：Controller + 业务 Service（按领域模块分目录，**禁止单一 impl 目录**）
- `cf-core`：启动入口 + 资源配置

统一响应：`cf-common` 的 `HttpResult<T>`，字段为 `code/msg/data`（**不是 message**）。

### 前端关键结构

```
src/
├── api/            — 按模块拆分文件，统一从 index.ts re-export
├── components/     — auth/ common/ user/ admin/ ui-adapter/
├── composables/    — 组合式函数
├── i18n/           — vue-i18n 国际化
├── layouts/        — UserLayout / AdminLayout
├── plugins/        — registerUiAdapter（全局注册 U* 组件）
├── stores/         — app.ts auth.ts adminAuth.ts（Pinia Composition API）
├── theme/          — scope.ts tokens.ts admin/ user/
├── utils/          — http.ts（fetch 封装，非 Axios）
└── views/          — auth/ user/ admin/
```

---

## ⚠️ 高频踩坑点（Agent 必读）

### 1. 后端双认证体系

项目使用**两套独立 Sa-Token 登录体系**：
- 普通用户：`StpUtil`（type="login"）
- 管理员：自定义 `StpAdminUtil`（包装 `StpLogic("admin")`）

拦截器按路径分流：`/api/admin/**` → `StpAdminUtil.checkLogin()`，其他 `/api/**` → `StpUtil.checkLogin()`

**管理员接口的权限注解必须加 `type = "admin"`**：
```java
@SaCheckPermission(value = "admin:auth:renew", type = "admin")
```
不加 `type = "admin"` 会检查用户权限空间，而非管理员权限空间。

### 2. 认证头不是 `Authorization: Bearer`

Sa-Token 使用 **动态 header 名**（默认 `satoken`），前端从 `localStorage.tokenName` 读取：

```
请求头：satoken: <tokenValue>
```

管理端与用户端 token **完全隔离**，管理端请求使用 `buildAdminAuthHeaders()` 并带 `X-CF-Skip-Auth: 1` 防止用户端 token 混入。

### 3. 前端 HTTP 客户端是 fetch，不是 Axios

`src/utils/http.ts` 基于 `fetch` API 封装了 `getJson/postJson/putJson/deleteJson/postFormData`。所有 API 调用都走这个封装。**不使用 Axios**（尽管技术栈文档提到）。

### 4. API 响应格式

```json
{ "code": 200, "msg": "操作成功", "data": {} }
```

### 5. HttpResult 继承 HashMap

`cf-common` 的 `HttpResult<T>` 继承 `HashMap<String, Object>`，不是 POJO。字段用常量键：`CODE_TAG="code"`, `MSG_TAG="msg"`, `DATA_TAG="data"`。

静态工厂：`HttpResult.success(data)` / `HttpResult.error(code, msg)` / `HttpResult.warn(msg)`

可链式 `put()` 扩展字段，但**不要加 `@JsonProperty`**（HashMap 序列化忽略注解）。

状态码来自自定义 `com.yyyouth.common.constants.HttpStatus`（**非** Spring 的 `org.springframework.http.HttpStatus`）。

### 6. 扫描包和 Mapper 路径是显式配置的

```java
@SpringBootApplication(scanBasePackages = "com.yyyouth")
@MapperScan("com.yyyouth.service.mapper")
```

### 7. Redis Cache-Aside 工具尚未实现

`redis-coding-standards.md` 规范了 Cache-Aside 模式（TTL+抖动/空值缓存/互斥锁），但代码中**尚未实现**此工具类。当前仅有 `RedisConstant`（key 前缀常量）和 YAML 配置。实现缓存功能时需在 `cf-service` 中从零创建。

同理，RabbitMQ 仅有常量类（`exchange.sssp` / `queue.sssp.post`），无配置类。

### 8. Vite 代理指向 18080，后端 dev 端口是 18081

`vite.config.ts` 配置 `/api` 代理到 `http://127.0.0.1:18080`。后端 `application-dev.yml` 端口为 `18081`。本地开发需确认中间层（nginx 等）或调整代理目标。

`application-local.yml` 中 `cf.auth.enable = false`（本地开发关闭鉴权），dev/test 环境为 `true`。

### 9. PrimeVue unstyled 模式 + UI 适配层

`main.ts` 配置 `PrimeVue, { unstyled: true }`。所有 PrimeVue 组件**无默认样式**。

项目通过 `src/plugins/registerUiAdapter.ts` 全局注册了 20+ U* 适配组件（UButton、UInput、UCard、UModal、UFormField、UInputTags 等），封装 PrimeVue + Tailwind。

**规则**：视图/页面中**禁止**直接 import `primevue/*`，必须用 `<UButton>` 等 U* 组件。如适配层没有所需组件，先在 `ui-adapter/` 中新建 U* 适配器再使用。

### 10. 主题作用域系统（双主题 + RGB 变量）

主题分 `user`（橙色主色 `#f59e0b`）和 `admin`（深橙色主色 `#ef4444`）两个 scope，由路由路径自动切换：

- `/computer/admin/*` → admin 主题
- 其他 → user 主题

切换方式：`document.documentElement.setAttribute('data-cf-theme', scope)`
暗黑模式：`<html class="dark">`（Tailwind `darkMode: 'class'`）


### 11. Token 存储键

| 端 | localStorage 键 |
|---|---|
| 用户端 | `accessToken`、`tokenName`、`userNickname`、`userAvatar` |
| 管理端 | `adminAccessToken`、`adminTokenName` |

### 12. 前端路由守卫不信任 localStorage

`ensureSession()` 会调用后端 profile/session 接口验证 token 有效性，不依赖 localStorage 中的 token 存在性。session 续期间隔 5 分钟。

### 13. 审计日志 AOP 默认开启

`AuditLogAspect` 通过 `@ConditionalOnProperty(prefix = "audit.log", name = "enabled", matchIfMissing = true)` 控制，**默认开启**。支持 SpEL 表达式描述，自动解析操作者（先 `StpAdminUtil` 再 `StpUtil` 最后 anonymous）。

---

## 数据库

**建表脚本**：`docs/sql/computer-favorites.sql`（25 核心表）+ `docs/sql/database-补充脚本.sql`（5 补充表）+ `docs/sql/init-data.sql`（种子数据）

**软删除**：`deleted TINYINT DEFAULT 0`。**注意**：仅 16 张表有此字段，以下 14 张表**没有**软删除：
`t_user_setting` `t_oauth_bind` `t_user_collect` `t_website_like` `t_comment_like` `t_website_score` `t_browse_history` `t_user_operation_log` `t_message` `t_system_config` `t_audit_log` `t_ai_message` `t_website_draft` `t_user_session`

---
