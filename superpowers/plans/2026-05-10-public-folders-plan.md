# 用户主页公开收藏夹 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** ProfileSidebarCard 的"收藏网站"占位区块 → 真实公开收藏夹链路；新增对话框 + 访客版页面 + 单夹可见性开关；全链路打通隐私 + Redis 缓存。

**Architecture:** `t_user_folder` 加 `is_public`；后端 `PublicFolderService`（3 端点 + 隐私校验复用 `ProfilePublicService.checkDashboardAccess`）；前端 `/computer/profile/:username` + `/computer/u/:username/collections`；写操作 `evictByPattern` 清三类缓存。

**Tech Stack:** Spring Boot 3.4.12 / MyBatis-Plus / Sa-Token / Redisson；Vue 3.5 / Pinia 3 / Vue Router 4 / PrimeVue 4 / Tailwind 4

**Spec：** `superpowers/specs/2026-05-10-public-folders-design.md`（详细代码块在 spec 中，本 plan 仅给步骤、命令、commit 指引）

**约定**：每 Task 完成后跑 `mvn -pl <module> -am compile`（后端）或 `npm run type-check`（前端）做静态校验，再 git commit。

---

## File Structure

详见 `spec §3 数据模型 / §4 后端 / §5 前端`。本 plan 按依赖自底向上拆分 18 个 Task。

---

## 阶段 A：数据库 + Model（A1-A3）

### Task A1：SQL 迁移

**File:** Modify `docs/sql/database-补充脚本.sql`

- [ ] 文件末尾追加 spec §3.1 的 ALTER 块（含 `is_public` 列 + `idx_user_public_hide` 索引）
- [ ] 在本地 MySQL 执行该 SQL：`mysql -uroot -p computer_favorites < docs/sql/database-补充脚本.sql`
- [ ] 验证：`SHOW COLUMNS FROM t_user_folder LIKE 'is_public'` 返回新列；`SHOW INDEX FROM t_user_folder WHERE Key_name='idx_user_public_hide'` 返回索引
- [ ] Commit: `feat(db): t_user_folder 加 is_public 字段与复合索引`

### Task A2：UserFolder POJO 加 isPublic 字段

**File:** Modify `cf-model/.../pojo/user/UserFolder.java`

- [ ] 在 `is_hide` 字段后追加 `isPublic` 字段（spec §3.2 完整代码）
- [ ] `mvn -f computer-favorites-back/pom.xml -pl cf-model -am compile`
- [ ] Commit: `feat(model): UserFolder 加 isPublic 字段`

### Task A3：UserFolderTreeVO 加 isPublic + Service.toTreeVO 同步

**Files:** 
- Modify `cf-model/.../vo/user/UserFolderTreeVO.java`
- Modify `cf-service/.../user/folder/impl/UserFolderServiceImpl.java`（toTreeVO 方法）

- [ ] VO 在 `isHide` 后追加 `Integer isPublic`
- [ ] `toTreeVO` builder 链补 `.isPublic(folder.getIsPublic())`
- [ ] 编译：`mvn ... -pl cf-service -am compile`
- [ ] Commit: `feat(model): UserFolderTreeVO 加 isPublic 字段`

---

## 阶段 B：5 个新 VO（B1-B5，可一次性创建）

### Task B1：创建 5 个 VO 文件

**Files Create（路径都在 `cf-model/src/main/java/com/yyyouth/model/vo/userstats/`）：**
- `PublicFolderItemVO.java`
- `PublicFolderChildVO.java`
- `PublicFolderWebsiteVO.java`
- `PublicFolderChildrenVO.java`（含内部类 `WebsitePage`）
- `PublicFolderTreeVO.java`（含内部类 `TreeNode`）

- [ ] 按 spec §3.4 字段表逐个创建，全部 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`，加 `@ApiModel/@ApiModelProperty` 中文描述
- [ ] `mvn ... -pl cf-model -am compile` 通过
- [ ] Commit: `feat(model): 公开收藏夹链路 5 个 VO`

> **关键字段对齐**（必须严格一致，后续任务依赖）：
> - `PublicFolderItemVO`: id/name/icon/color/parentId/sort/websiteCount/childrenCount
> - `PublicFolderChildVO`: id/name/icon/color/parentId/websiteCount
> - `PublicFolderWebsiteVO`: id/title/url/cover/description/categoryName/tagNames(List<String>)/clickCount/likeCount/collectCount/score(BigDecimal)
> - `PublicFolderChildrenVO`: subFolders(List<PublicFolderChildVO>) + websites(WebsitePage{list,total,pageNum,pageSize})
> - `PublicFolderTreeVO`: roots(List<TreeNode>); TreeNode{id,name,icon,color,parentId,sort,websiteCount,children}

---

## 阶段 C：Redis 常量 + Mapper（C1-C2）

### Task C1：RedisConstant 加 3 常量

**File:** Modify `cf-common/.../constants/RedisConstant.java`

- [ ] 文件末尾追加 spec §3.5 的 3 个常量：`PUBLIC_FOLDER_TOP_PREFIX` / `PUBLIC_FOLDER_CHILDREN_PREFIX` / `PUBLIC_FOLDER_TREE_PREFIX`
- [ ] `mvn ... -pl cf-common -am compile`
- [ ] Commit: `feat(common): 加公开收藏夹 3 个 Redis 缓存键常量`

### Task C2：UserCollectMapper 加 selectPublicWebsitesByFolder

**File:** Modify `cf-service/.../mapper/user/UserCollectMapper.java`

- [ ] 加 `IPage<PublicFolderWebsiteVO> selectPublicWebsitesByFolder(Page page, @Param("userId") Long userId, @Param("folderId") Long folderId)`，用 `@Select` 注解
- [ ] SQL 联 `t_user_collect uc` + `t_website w` + `t_category c`，过滤 `uc.user_id` + `uc.folder_id` + `w.deleted=0 AND w.status=1 AND w.audit_status=1`，按 `uc.create_time DESC` 排序
- [ ] **必须显式带 `uc.user_id = #{userId}` 防越权**
- [ ] tagNames 本期返回 null（无并行查标签需求）
- [ ] `mvn ... -pl cf-service -am compile`
- [ ] Commit: `feat(mapper): UserCollectMapper 加按 folder 分页查询直属网站`

---

## 阶段 D：PublicFolderService（D1-D4）

### Task D1：PublicFolderService 接口

**File:** Create `cf-service/.../user/userstats/PublicFolderService.java`

- [ ] 定义 3 方法：
  ```java
  List<PublicFolderItemVO> getTopFolders(String username, Long currentUserId, int limit);
  PublicFolderChildrenVO getFolderChildren(String username, Long currentUserId, Long folderId, int pageNum, int pageSize);
  PublicFolderTreeVO getPublicFolderTree(String username, Long currentUserId);
  ```
- [ ] 类 javadoc 注明：参数为 `username`（非 userId）让 Service 内部解析，避免 Controller 多查
- [ ] Commit: `feat(service): PublicFolderService 接口定义`

### Task D2：PublicFolderServiceImpl 骨架 + getTopFolders

**File:** Create `cf-service/.../user/userstats/impl/PublicFolderServiceImpl.java`

- [ ] 注入：`ProfilePublicService` / `UserAccountMapper` / `UserFolderMapper` / `UserCollectMapper` / `RedisCache`
- [ ] 私有方法 `resolveUserIdAndGuard(username, currentUserId) → GuardResult(targetUserId, check)`：
  1. username 非空校验
  2. `UserAccountMapper.selectOne` 按 username 解析 userId（deleted=0），不存在抛 `USER_DISABLED`
  3. 调 `profilePublicService.checkDashboardAccess(targetUserId, currentUserId, false)` 复用 visibility 校验
  4. `!isOwn && showCollections == 0` → 抛 `PROFILE_PRIVATE`
  5. 返回 `record GuardResult(Long targetUserId, ProfileVisibilityCheckResult check) { }`
- [ ] `getTopFolders` 实现：
  - cache key: `PUBLIC_FOLDER_TOP_PREFIX + targetUserId + ":top:" + limit + (isOwn ? ":own" : ":visitor")`
  - TTL 300s ±60s
  - loader：`parent_id=0 + status=1 + deleted=0`，他人加 `is_hide=0 AND is_public=1`
  - 一次 GROUP BY 查所有顶层夹的 `childrenCount`（私有方法 `countVisibleChildren`）
  - 他人视图过滤"空夹"（websiteCount=0 且 childrenCount=0），本人不过滤
  - 截至 `limit` 个返回
  - 用 `RedisCache.getOrLoad(key, TypeReference<List<PublicFolderItemVO>>{}, ttl, jitter, loader)`
- [ ] `getFolderChildren` 与 `getPublicFolderTree` 暂占位 `throw new UnsupportedOperationException`，留 D3/D4
- [ ] `mvn ... -pl cf-service -am compile`
- [ ] Commit: `feat(service): PublicFolderService 骨架 + getTopFolders 实现`

### Task D3：getFolderChildren 实现

**File:** Modify `PublicFolderServiceImpl.java`

- [ ] 替换占位实现：
  - 入参校验：`folderId > 0`，`pageNum >= 1`，`pageSize ∈ [1,50]` 默认 12
  - cache key: `PUBLIC_FOLDER_CHILDREN_PREFIX + targetUserId + ":" + folderId + ":" + pageNum + ":" + pageSize + (isOwn ? ":own" : ":visitor")`
  - TTL 180s ±30s
  - loader：
    1. 校验 folder 归属 + 可见性（同顶层规则），不可见抛 `PROFILE_PRIVATE`
    2. 查子文件夹（`parent_id=folderId` + 同可见过滤），按 sort/id 排序
    3. 网站列表：`new Page<>(pageNum, pageSize)` + `userCollectMapper.selectPublicWebsitesByFolder(page, userId, folderId)`
    4. 组装 `PublicFolderChildrenVO`
- [ ] `mvn ... -pl cf-service -am compile`
- [ ] Commit: `feat(service): PublicFolderService.getFolderChildren 实现`

### Task D4：getPublicFolderTree 实现

**File:** Modify `PublicFolderServiceImpl.java`

- [ ] 替换占位实现：
  - cache key: `PUBLIC_FOLDER_TREE_PREFIX + targetUserId + (isOwn ? ":own" : ":visitor")`
  - TTL 300s ±60s
  - loader：
    1. 一次查所有该用户文件夹（同可见性过滤）
    2. 用 `LinkedHashMap<Long, TreeNode>` 索引
    3. 遍历建树：`parent_id=0` 或父节点不在 map 中（且 isOwn）→ 加入 roots；父节点存在 → 加 parent.children；**他人视图遇到孤儿（父对访客不可见）整支挂掉**
- [ ] 删除未用 import
- [ ] `mvn ... -pl cf-service -am compile`
- [ ] Commit: `feat(service): PublicFolderService.getPublicFolderTree 实现`

---

## 阶段 E：UserFolderService 扩展 + 缓存失效（E1-E5）

### Task E1：UserFolderService 接口加 toggleFolderPublic

**File:** Modify `cf-service/.../user/folder/UserFolderService.java`

- [ ] `toggleFolderHide` 后追加 `void toggleFolderPublic(Long folderId, boolean isPublic);`
- [ ] Commit 与 E2 合并

### Task E2：UserFolderServiceImpl 加 evictPublicFolderCache + toggleFolderPublic

**File:** Modify `cf-service/.../user/folder/impl/UserFolderServiceImpl.java`

- [ ] import + 注入 `RedisCache redisCache`，import `RedisConstant`
- [ ] 类常量补 `NOT_PUBLIC = 0` / `IS_PUBLIC = 1`
- [ ] 文件底部加私有方法 `evictPublicFolderCache(Long userId)`：
  ```java
  redisCache.evictByPattern(RedisConstant.PUBLIC_FOLDER_TOP_PREFIX + userId + ":*");
  redisCache.evictByPattern(RedisConstant.PUBLIC_FOLDER_CHILDREN_PREFIX + userId + ":*");
  redisCache.evict(RedisConstant.PUBLIC_FOLDER_TREE_PREFIX + userId);
  ```
  外层 try/catch 仅 log warn（缓存失效非主流程）
- [ ] 实现 `toggleFolderPublic(folderId, isPublic)`：
  1. `getCurrentUserId()` + `getOwnedFolder(userId, folderId)` 防越权
  2. 计算 target；若 `folder.getIsPublic() == target` → 直接 return
  3. `LambdaUpdateWrapper` 仅 set `is_public`，update == 1 校验
  4. `evictPublicFolderCache(userId)`
  5. `@Transactional`
- [ ] `mvn ... -pl cf-service -am compile`
- [ ] Commit: `feat(service): UserFolderService 加 toggleFolderPublic + evictPublicFolderCache`

### Task E3：UserFolderServiceImpl 5 处写入点接入 evict

**File:** Modify same as E2

- [ ] 在以下 5 个方法的最后（log 之后）加 `evictPublicFolderCache(userId);`：
  1. `createFolder` — log 之后、return 之前
  2. `updateFolder` — 末尾
  3. `deleteFolder` — 末尾
  4. `toggleFolderHide` — log 之后
  5. `toggleFolderPublic` — 已在 E2 加（不重复）
- [ ] `mvn ... -pl cf-service -am compile`
- [ ] Commit: `feat(service): folder 5 处写入点接入公开收藏夹缓存失效`

### Task E4：UserCollectServiceImpl 2 处写入点接入

**File:** Modify `cf-service/.../user/collect/impl/UserCollectServiceImpl.java`

- [ ] 注入 `RedisCache`，import `RedisConstant`
- [ ] 文件底部加 `evictPublicFolderCache(Long userId)` 私有方法（与 E2 同样实现，复制即可——故意不下沉到通用工具，因为目前只两处域用）
- [ ] `collect` 方法 return 前 `evictPublicFolderCache(userId);`
- [ ] `cancelCollect` 方法 return 前 `evictPublicFolderCache(userId);`
- [ ] `mvn ... -pl cf-service -am compile`
- [ ] Commit: `feat(service): collect/cancelCollect 接入公开收藏夹缓存失效`

### Task E5：ProfilePrivacyServiceImpl 联动清理 3 类公开收藏夹缓存

**File:** Modify `cf-service/.../user/userstats/impl/ProfilePrivacyServiceImpl.java`

- [ ] 在 `evictPublicCache(username, userId)` 方法 try 块内 `redisCache.evictByPattern(USER_PROFILE_DASHBOARD_PREFIX + userId + ":*");` 之后追加 3 行清理（spec §4.6）
- [ ] `mvn ... -pl cf-service -am compile`
- [ ] Commit: `feat(service): 隐私设置变更联动清理公开收藏夹缓存`

---

## 阶段 F：Controller 端点（F1-F2）

### Task F1：ProfilePublicController 加 3 端点

**File:** Modify `cf-web/.../controller/user/ProfilePublicController.java`

- [ ] import + 注入 `PublicFolderService`，import 3 个 VO + `@NotBlank/@Min/@Max/@Positive`
- [ ] 末尾追加 3 个端点（spec §4.4.1 完整代码）：
  - `GET /{username}/folders` — `getPublicFolders(username, limit=5)` — `@RateLimit(limit=10, window=1)`
  - `GET /{username}/folders/{folderId}/children` — `getPublicFolderChildren(username, folderId, pageNum=1, pageSize=12)` — `@RateLimit(limit=10, window=1)`
  - `GET /{username}/folders/tree` — `getPublicFolderTree(username)` — `@RateLimit(limit=5, window=1)`
- [ ] 全部 `@AuditLog(action=..., description=...)`，不加 `@SaCheckLogin`（公开端点，通过 `currentUserIdOrNull()` 取访问者）
- [ ] `mvn ... -pl cf-web -am compile`
- [ ] Commit: `feat(controller): ProfilePublicController 加公开收藏夹 3 端点`

### Task F2：UserFolderController 加 togglePublic 端点

**File:** Modify `cf-web/.../controller/user/UserFolderController.java`

- [ ] import `AuditLog`（如未引入）
- [ ] `toggleHide` 端点之后追加：
  ```java
  @PutMapping("/{id}/public")
  @SaCheckLogin
  @AuditLog(module = "user-folder", action = "toggle-public",
            description = "切换收藏夹对外可见性，id=#{#id}, isPublic=#{#isPublic}")
  public HttpResult togglePublic(@PathVariable @NotNull @Positive Long id,
                                 @RequestParam boolean isPublic) {
      log.info("收藏夹对外公开切换请求，id={}, isPublic={}", id, isPublic);
      userFolderService.toggleFolderPublic(id, isPublic);
      return HttpResult.success(isPublic ? "已对外公开" : "已对外私密");
  }
  ```
- [ ] `mvn ... -pl cf-core -am compile`
- [ ] Commit: `feat(controller): UserFolderController 加 togglePublic 端点`

---

## 阶段 G：后端集成验证（G1，无 commit）

### Task G1：本地启动并按 spec §7.1 矩阵验证

- [ ] 启动 `mvn -f computer-favorites-back/pom.xml -pl cf-core spring-boot:run`
- [ ] 用 curl/Postman 跑权限矩阵 7 条用例（spec §7.1）：本人 / 公开访客 / private 主页 / showCollections=0 / is_hide=1 / is_public=0 / logged 未登录
- [ ] 验证缓存 evict：调 `togglePublic` 后立即调顶层端点应见变化（不等 5 min TTL）
- [ ] 全通过则进入前端阶段；任一失败回到对应 Task 修

---

## 阶段 H：前端类型 + API + Store（H1-H6）

### Task H1：types/profile.ts 加 ProfileFolderItem

**File:** Modify `computer-favorites-web/src/types/profile.ts`

- [ ] 文件末尾追加 `ProfileFolderItem` 接口（spec §5.3）
- [ ] `ProfileData` 接口内追加可选 `publicFolders?: ProfileFolderItem[]` / `publicFoldersUsername?: string`
- [ ] Commit: `feat(types): 加 ProfileFolderItem + ProfileData 公开收藏夹字段`

### Task H2：types/collection.ts CollectionCategory 加 isPublic

**File:** Modify `computer-favorites-web/src/types/collection.ts`

- [ ] `CollectionCategory` 接口内 `isHide` 后加 `isPublic: number`
- [ ] Commit: `feat(types): CollectionCategory 加 isPublic 字段`

### Task H3：api/user-profile-public.ts 加 3 函数

**File:** Modify `computer-favorites-web/src/api/user-profile-public.ts`

- [ ] 文件末尾追加 6 个类型 + 3 个函数（spec §5.2 完整代码）：
  - 类型：`PublicFolderItem` / `PublicFolderChild` / `PublicFolderWebsite` / `PublicFolderChildrenPage` / `PublicFolderTreeNode` / `PublicFolderTreeResponse{roots}`
  - 函数：`getPublicTopFolders(username, limit=5)` / `getPublicFolderChildren(username, folderId, pageNum=1, pageSize=12)` / `getPublicFolderTree(username)`
- [ ] 错误处理风格与该文件现有 `getPublicProfile` 保持一致（throw HttpError 或 throw new Error）
- [ ] `getPublicFolderTree` 返回 `res.data?.roots ?? []`（剥掉 wrapper）
- [ ] `npm run type-check`（本批新增无报错即可）
- [ ] Commit: `feat(api): 加公开收藏夹 3 个 API 函数`

### Task H4：api/user-folder.ts 加 toggleFolderPublic

**File:** Modify `computer-favorites-web/src/api/user-folder.ts`

- [ ] 末尾加：
  ```ts
  export async function toggleFolderPublic(id: number, isPublic: boolean): Promise<void> {
    const res = await putJson<ApiResult<null>>(`/api/user/folder/${id}/public?isPublic=${isPublic}`)
    if (res.code !== 200) throw new Error(res.msg || '操作失败')
  }
  ```
- [ ] Commit: `feat(api): user-folder 加 toggleFolderPublic`

### Task H5：stores/profilePublic.ts 加 publicFolders state

**File:** Modify `computer-favorites-web/src/stores/profilePublic.ts`

- [ ] 顶部 import 追加 `getPublicTopFolders` + `PublicFolderItem`
- [ ] `defineStore('profilePublic', () => { ... })` 内 return 之前追加：
  - state：`publicFolders = ref<PublicFolderItem[]>([])` / `foldersLoading = ref(false)` / `foldersError = ref('')`
  - action：`loadPublicFolders(username, limit=5)` 含 try/catch；username 空直接清空
  - action：`resetPublicFolders()` 清空
- [ ] 把 5 个新成员加进 return 对象
- [ ] `npm run type-check`
- [ ] Commit: `feat(store): profilePublic 加 publicFolders state + loadPublicFolders`

### Task H6：stores/folder.ts 加 handleTogglePublic

**File:** Modify `computer-favorites-web/src/stores/folder.ts`

- [ ] 顶部 import 追加 `toggleFolderPublic`
- [ ] `handleShowHiddenFolders` 后追加 `handleTogglePublic(id, isPublic)`：
  - 调 `toggleFolderPublic(id, isPublic)`
  - toast 成功/失败
  - 成功后 `await loadFolderTree()` 刷新
- [ ] return 对象加 `handleTogglePublic`
- [ ] Commit: `feat(store): folder 加 handleTogglePublic action`

---

## 阶段 I：前端组件 - 对话框 + 访客版页面（I1-I2）

### Task I1：新建 PublicFolderDialog.vue

**File:** Create `computer-favorites-web/src/components/user/profile/PublicFolderDialog.vue`

- [ ] props：`open: boolean` / `username: string` / `folder: ProfileFolderItem | null`；emit `update:open`
- [ ] state：loading / errorText / subFolders / websites / total / pageNum / pageSize=12
- [ ] watch `props.open`：true 时重置 pageNum=1 并 `loadChildren()`
- [ ] `loadChildren` 调 `getPublicFolderChildren(username, folder.id, pageNum, pageSize)`
- [ ] `goPage(next)` 边界校验后切页重拉
- [ ] `handleViewAll` 调 `router.push({ path: \`/computer/u/${username}/collections\`, query: { folderId: String(folder.id) } })` 后 closeDialog
- [ ] 模板用 `<UModal>` + `<UButton>`（项目全局已注册），prop 与 ui-adapter 对齐（dismissable / size / variant）
- [ ] Body 三段：子文件夹网格（4 列，保留 folder.color/icon 视觉）/ 网站列表（标题+url+cover+统计）/ 空态
- [ ] Footer：关闭 + 查看全部 →
- [ ] `npm run type-check`
- [ ] Commit: `feat(component): 新建 PublicFolderDialog 公开收藏夹对话框`

### Task I2：新建 PublicCollectionView.vue

**File:** Create `computer-favorites-web/src/views/user/PublicCollectionView.vue`

- [ ] route 取 `username = route.params.username` / `initialFolderId = Number(route.query.folderId)`
- [ ] state：tree / treeLoading / activeId / expanded(Set) / subFolders / websites / total / pageNum / pageSize=24
- [ ] `onMounted`：
  1. `getPublicFolderTree(username)` 加载左树
  2. 若 `initialFolderId` 存在 → `activeId = initialFolderId`，展开其全部祖先节点（递归找祖先 id 加入 expanded）
  3. 调 `loadChildren(activeId)` 加载右侧
- [ ] `loadChildren(folderId)` 调 `getPublicFolderChildren(username, folderId, pageNum, pageSize)`
- [ ] 左树：递归节点组件 / 缩进按 depth；点击 chevron 切 expanded；点击 name 切 activeId 并 loadChildren；保留 folder.color / icon
- [ ] 右侧：与对话框 body 一致（子文件夹网格 + 网站列表 + 分页）
- [ ] 顶部访客上下文：用户头像 + nickname + "返回主页"链接到 `/computer/profile/{username}`
- [ ] 错误码处理：`HttpError` body.code=PROFILE_PRIVATE / PROFILE_LOGIN_REQUIRED / USER_DISABLED → 渲染对应空态（复用 `error-code-map.ts`）
- [ ] `npm run type-check`
- [ ] Commit: `feat(view): 新建 PublicCollectionView 访客版收藏夹页`

---

## 阶段 J：前端集成 - 路由 + ProfileView + Sidebar + CollectionView（J1-J4）

### Task J1：router/index.ts 加 2 路由

**File:** Modify `computer-favorites-web/src/router/index.ts`

- [ ] 在 UserLayout 子路由组追加：
  ```ts
  { path: 'profile/:username', name: 'publicProfile', component: () => import('@/views/user/ProfileView.vue'), meta: { title: '用户主页' } },
  { path: 'u/:username/collections', name: 'publicCollections', component: () => import('@/views/user/PublicCollectionView.vue'), meta: { title: '收藏夹' } }
  ```
- [ ] 注意：`profile/:username` 与现有无参 `profile` 路由不冲突，新路由在后面注册即可
- [ ] `npm run type-check`
- [ ] Commit: `feat(router): 加 publicProfile + publicCollections 路由`

### Task J2：ProfileSidebarCard.vue 改造 L237-272 + 集成对话框

**File:** Modify `computer-favorites-web/src/components/user/profile/ProfileSidebarCard.vue`

- [ ] script setup：
  - import `PublicFolderDialog`，import `Folder`/`FolderTree` 已有
  - props 加：`publicFolders?: ProfileFolderItem[]` / `publicFoldersUsername?: string`
  - state：`dialogOpen = ref(false)` / `selectedFolder = ref<ProfileFolderItem | null>(null)`
  - method：`openFolderDialog(folder)` set selectedFolder + dialogOpen=true
- [ ] 模板：删除原 L237-272 "收藏网站" 区块，替换为"公开收藏夹"区块（spec §5.5.1 完整 vue 代码）：
  - `v-if="(isOwn || showCollections) && publicFolders?.length"`
  - 列表项 button @click=openFolderDialog(folder)
  - **统一图标**：lucide `Folder` + `text-amber-500`，**不**用 folder.color/icon
  - 副文本："{websiteCount} 个网站 · {childrenCount} 个子夹"
- [ ] 模板末尾追加：
  ```vue
  <PublicFolderDialog
    v-model:open="dialogOpen"
    :username="publicFoldersUsername || ''"
    :folder="selectedFolder"
  />
  ```
- [ ] `npm run type-check`
- [ ] Commit: `feat(component): ProfileSidebarCard 改造为公开收藏夹区块 + 集成对话框`

### Task J3：ProfileView.vue 支持 :username 路由 + isOwn 分流

**File:** Modify `computer-favorites-web/src/views/user/ProfileView.vue`

- [ ] script setup：
  - import `useRoute` / `useAuthStore` / `useProfilePublicStore`
  - computed `targetUsername = route.params.username?.trim() || authStore.username || ''`
  - computed `isOwnView = !route.params.username || route.params.username === authStore.username`
  - computed `showCollections = isOwnView.value || profilePublicStore.privacy?.showCollections === 1`
  - `onMounted` 分流：
    - **本人**：`loadProfile()` + `dashboardStore.loadInitial()` + `approvedSubmissions.load()` + `profilePublicStore.loadPublicFolders(authStore.username, 5)`
    - **访客**：`profilePublicStore.loadPublicProfile(targetUsername.value)` + `profilePublicStore.loadPublicFolders(targetUsername.value, 5)`；本期访客主页**隐藏看板**（不调 dashboardStore）
- [ ] 模板：
  - `<ProfileSidebarCard>` 加 `:is-own="isOwnView"` `:show-collections="showCollections"` `:public-folders="profilePublicStore.publicFolders"` `:public-folders-username="targetUsername"`
  - 主区域：访客模式下用 `v-if="isOwnView"` 包裹 `<ProfileDashboardCharts />` `<ProfileHeatmapSection />` `<ProfileImpactSection />` `<ProfileDashboardSummary />`
  - 错误态：访客模式下额外处理 `profilePublicStore.isPrivate / isLoginRequired / isNotFound`
- [ ] `npm run type-check`
- [ ] Commit: `feat(view): ProfileView 支持 :username 路由 + isOwn 分流 + 公开收藏夹接入`

### Task J4：CollectionView.vue 右键菜单加切换项

**File:** Modify `computer-favorites-web/src/views/user/CollectionView.vue`

- [ ] script setup：useFolderStore（如未用），引出 `handleTogglePublic`
- [ ] `buildFolderMenuItems(category)` 返回数组中追加（位置：`隐藏` 后、`删除` 前）：
  ```ts
  {
    label: category.isPublic ? '设为对外私密' : '对外公开',
    icon: category.isPublic ? 'lock' : 'globe',
    disabled: category.isHide === 1,  // 自我隐藏中禁用，提示先取消隐藏
    onClick: () => folderStore.handleTogglePublic(category.id, category.isPublic !== 1),
  }
  ```
- [ ] `npm run type-check`
- [ ] Commit: `feat(view): CollectionView 右键菜单加 对外公开/私密 切换`

---

## 阶段 K：端到端验收（K1）

### Task K1：手动 E2E 关键链路（按 spec §7.3 6 条用例）

- [ ] 启动后端 + 前端 dev server
- [ ] 用例 1：自己访问 `/computer/profile` → 侧边栏看到所有顶层夹（含 is_public=0）
- [ ] 用例 2：自己访问 `/computer/profile/{某访客}` → 只看到对方 is_public=1 AND is_hide=0 顶层夹
- [ ] 用例 3：点侧边栏夹 → 对话框开 → 子项加载 → 点"查看全部" → 跳访客版页 + 默认激活该夹
- [ ] 用例 4：访客版页 — 左树 / 右子项分页 / `?folderId=N` query 默认激活
- [ ] 用例 5：本人 CollectionView 右键 → 切对外公开 → toast 成功 → 重载 → 状态生效
- [ ] 用例 6：设置 → 隐私 → 关"展示收藏列表" → 别人主页侧边栏整块消失（PROFILE_PRIVATE）
- [ ] 全部通过：本任务完成；任一失败：定位 task 修复

---

## Self-Review 已完成

按 writing-plans skill 要求做了 self-review：

1. **Spec 覆盖**：spec §3-§5 + §6 验收清单全部映射到 Task A1-K1（无遗漏）
2. **Placeholder scan**：无 TBD/TODO/省略代码（详细代码体在 spec，plan 步骤指引明确）
3. **类型一致性**：
   - Java 端：`PublicFolderItemVO/PublicFolderChildVO/PublicFolderWebsiteVO/PublicFolderChildrenVO/PublicFolderTreeVO` 5 类名跨 Task B1/D2-D4/F1 一致
   - TS 端：`PublicFolderItem/PublicFolderChild/PublicFolderWebsite/PublicFolderChildrenPage/PublicFolderTreeNode/PublicFolderTreeResponse` 跨 H3/H5/I1/I2 一致
   - 方法签名 `getTopFolders(String, Long, int)` / `getFolderChildren(String, Long, Long, int, int)` / `getPublicFolderTree(String, Long)` 跨 D1-D4/F1 一致
   - 缓存 key 模板 `PUBLIC_FOLDER_TOP_PREFIX/CHILDREN_PREFIX/TREE_PREFIX` + `:own/:visitor` 后缀跨 D2-D4/E2/E4/E5 一致
   - 字段名 `is_public/isPublic/childrenCount/showCollections` 跨数据库/POJO/VO/TS 类型一致

---

## Execution Handoff

Plan complete and saved to `superpowers/plans/2026-05-10-public-folders-plan.md`. Two execution options:

**1. Subagent-Driven (recommended)** — Cascade dispatches a fresh subagent per task, reviews between tasks, fast iteration

**2. Inline Execution** — Cascade executes tasks in this session using executing-plans, batch execution with checkpoints

Which approach?
