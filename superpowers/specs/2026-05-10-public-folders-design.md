# 用户主页公开收藏夹 — 设计文档

- **日期**：2026-05-10
- **范围**：user-15（用户主页）扩展、收藏夹模块
- **状态**：设计已与用户确认，待生成实施计划

---

## 1. 背景与目标

### 1.1 现状

`ProfileSidebarCard.vue` L237-272 的"收藏网站"区块当前数据源是 `t_user_profile.favorite_websites`（一个文本字段，不是真实收藏关系），属于占位实现。

`t_user_setting` 已经有 `show_collections` 整体闸门（粗粒度，控制是否对他人展示整个收藏区块），但缺少**单个收藏夹粒度**的对外可见控制。

`t_user_folder` 仅有 `is_hide`（对自己隐藏，带密码保护的私密夹），无对外可见性字段。

`ProfilePublicService.checkDashboardAccess()` 已实现完整的访问者权限校验，并返回 `showCollections`，可复用。

`RedisCache` Cache-Aside 工具类已实现并落地，写法与 dashboard 一致。

### 1.2 目标

1. 在 `t_user_folder` 新增 `is_public TINYINT DEFAULT 0`（默认私密，安全优先）
2. 把 `ProfileSidebarCard` 的旧"收藏网站"占位区块替换为**真实顶级收藏夹列表**（最多 5 个）
3. 新增**对话框**：点击顶层收藏夹 → 一层平铺展示子收藏夹 + 直属网站卡片（不可下钻）
4. 新增**只读访客版收藏夹页** `/computer/u/:username/collections`
5. 新增**按 username 访问的 ProfileView**：`/computer/profile/:username`（既可看自己也可看别人）
6. 收藏夹页右键菜单新增"对外公开 / 对外私密"切换项
7. 全链路 Redis 缓存 + 写操作主动 `evictByPattern`
8. 复用现有隐私体系（`ProfilePublicService.checkDashboardAccess`），不重复造轮子

### 1.3 非目标

- 不改造 `is_hide` 密码保护机制
- 不实现批量公开/私密管理面板
- 不在对话框内做面包屑/下钻
- 不引入 MQ 失效（直接 evictByPattern）
- 本人 `/computer/collection` 收藏夹管理页不变
- 不做对外公开收藏夹的访问审计深度（仅基础 `@AuditLog`）

---

## 2. 关键决策（澄清问答归纳）

| 决策点 | 选择 | 理由 |
|---|---|---|
| 访问者范围 | 本人 + 别人主页都展示 | 复用 ProfilePublicService 体系，最完整 |
| 字段名 | `is_public TINYINT DEFAULT 0` | 默认私密，安全优先；老用户全部默认私密，需主动开启 |
| 对话框层级 | 一层平铺，不可下钻 | 结构最简单，"查看全部"已承担深度入口 |
| 单夹开关位置 | CollectionView 右键菜单 | 贴近使用场景，不污染设置中心 |
| `is_hide` × `is_public` | `is_hide=1` 自动 = 对外不可见 | 语义最清晰，避免"自己隐藏但他人可见"的怪状态 |
| "查看全部"目标页 | 新建 `/computer/u/:username/collections` | 与本人 CollectionView 解耦，纯只读访客版 |
| 接口粒度 | 3 个独立端点 | 顶层列表 / 单夹子项 / 全量树，缓存可独立过期 |
| 顶层展示策略 | 前 5 个，过滤空夹，统一 Folder 图标 + amber-500 | 符合侧边栏轻量入口风格，对话框/全量页才展示自定义图标和颜色 |
| 缓存失效策略 | 所有写操作主动 `evictByPattern` | 复用现有工具，仅需在 5-7 处加一行调用 |

---

## 3. 数据模型变更

### 3.1 数据库迁移（追加到 `docs/sql/database-补充脚本.sql`）

```sql
-- ============================================
-- 用户收藏夹对外可见性（user-15 主页公开收藏夹）
-- 新增 is_public：0=对外私密(默认)，1=对外公开
-- 与 is_hide 的叠加规则：is_hide=1 一定不可见；他人可见 = is_hide=0 AND is_public=1
-- ============================================
ALTER TABLE `t_user_folder`
  ADD COLUMN `is_public` TINYINT NOT NULL DEFAULT 0
  COMMENT '是否对其他用户可见：0-私密(默认)，1-公开'
  AFTER `is_hide`;

ALTER TABLE `t_user_folder`
  ADD INDEX `idx_user_public_hide` (`user_id`, `is_public`, `is_hide`, `deleted`, `parent_id`);
```

### 3.2 POJO 变更

`UserFolder.java` 在 `is_hide` 字段后追加：

```java
/**
 * 是否对其他用户可见：0-私密(默认)，1-公开
 */
@TableField("is_public")
private Integer isPublic;
```

### 3.3 既有 VO 扩展

`UserFolderTreeVO` 加 `Integer isPublic`，让本人 CollectionView 能在右键菜单读取当前状态用于切换。

### 3.4 新增 DTO / VO

| 类 | 路径 | 用途 |
|---|---|---|
| `UserFolderTogglePublicDTO` | `cf-model/.../dto/user/` | `{ isPublic: 0|1 }` 单字段 + `@NotNull` |
| `PublicFolderItemVO` | `cf-model/.../vo/userstats/` | 顶层列表项：`id/name/icon/color/parentId/sort/websiteCount/childrenCount` |
| `PublicFolderChildVO` | `cf-model/.../vo/userstats/` | 一层子项的子文件夹：`id/name/icon/color/parentId/websiteCount` |
| `PublicFolderWebsiteVO` | `cf-model/.../vo/userstats/` | 直属网站卡片：`id/title/url/cover/description/categoryName/tagNames/clickCount/likeCount/collectCount/score` |
| `PublicFolderChildrenVO` | `cf-model/.../vo/userstats/` | 子项分页聚合：`{ subFolders, websites: { list, total, pageNum, pageSize } }` |
| `PublicFolderTreeVO` | `cf-model/.../vo/userstats/` | 全量公开树：递归 `children`（树用 `websiteCount`，不展开网站） |

> **复用网站卡片字段**：`PublicFolderWebsiteVO` 字段从现有 `UserCollectItemVO` / `WebsiteListVO` 等中收敛，避免新建并行结构。

### 3.5 Redis 常量（追加到 `RedisConstant.java`）

```java
// ========== user-15 公开收藏夹 ==========

/** 公开主页顶层收藏夹列表：user:profile:public-folders:{userId}:top:{limit} */
public static final String PUBLIC_FOLDER_TOP_PREFIX = "user:profile:public-folders:";

/** 公开主页单收藏夹子项：user:profile:public-folder-children:{userId}:{folderId}:{pageNum}:{pageSize} */
public static final String PUBLIC_FOLDER_CHILDREN_PREFIX = "user:profile:public-folder-children:";

/** 公开主页全量收藏夹树：user:profile:public-folder-tree:{userId} */
public static final String PUBLIC_FOLDER_TREE_PREFIX = "user:profile:public-folder-tree:";
```

**TTL 规划**：
- 顶层列表 / 全量树：300s ±60s
- 单夹子项：180s ±30s

---

## 4. 后端架构

### 4.1 新增 `PublicFolderService`

接口：`cf-service/src/main/java/com/yyyouth/service/user/userstats/PublicFolderService.java`

```java
public interface PublicFolderService {
    List<PublicFolderItemVO> getTopFolders(Long targetUserId, Long currentUserId, int limit);
    PublicFolderChildrenVO getFolderChildren(Long targetUserId, Long currentUserId, Long folderId, int pageNum, int pageSize);
    PublicFolderTreeVO getPublicFolderTree(Long targetUserId, Long currentUserId);
}
```

**实现要点**：

1. 每个方法首行调 `profilePublicService.checkDashboardAccess(targetUserId, currentUserId, false)`：
   - 校验目标用户存在 + visibility（自抛 PRIVATE / LOGIN_REQUIRED）
   - 返回 `(isOwn, visibility, showContribution, showCollections)`
2. 若 `!isOwn && showCollections == 0` → 抛 `PROFILE_PRIVATE`
3. 可见性过滤：
   - 他人：`parent_id=X AND is_public=1 AND is_hide=0 AND status=1 AND deleted=0`
   - 本人：仅 `parent_id=X AND status=1 AND deleted=0`（保留所有）
4. 顶层接口过滤"空夹"（websiteCount=0 且无可见子文件夹），本人不过滤
5. 网站列表：在 `UserCollectMapper` 加 `selectPublicWebsitesByFolder(userId, folderId, page)`，按 `user_id + folder_id` 双条件过滤后 join `t_website`（必须显式带 `user_id` 防越权读取）
6. 缓存：复用 `RedisCache.getOrLoad(key, ..., loader)`，key 用 §3.5 常量

### 4.2 `UserFolderService` 新增 `togglePublic`

```java
void toggleFolderPublic(Long folderId, boolean isPublic);
```

实现要点：
- 复用 `getOwnedFolder(userId, folderId)` 防越权
- `is_hide=1` 时**允许**保存（语义独立），UI 层灰化按钮即可
- 写完调 `evictPublicFolderCache(userId)`

### 4.3 抽象 `evictPublicFolderCache`

在 `UserFolderServiceImpl` 提取私有方法，避免重复散落：

```java
private void evictPublicFolderCache(Long userId) {
    redisCache.evictByPattern(RedisConstant.PUBLIC_FOLDER_TOP_PREFIX + userId + ":*");
    redisCache.evictByPattern(RedisConstant.PUBLIC_FOLDER_CHILDREN_PREFIX + userId + ":*");
    redisCache.evict(RedisConstant.PUBLIC_FOLDER_TREE_PREFIX + userId);
}
```

调用点：
- `UserFolderServiceImpl`：createFolder / updateFolder / deleteFolder / toggleFolderHide / toggleFolderPublic
- `UserCollectServiceImpl`：collect / cancelCollect

### 4.4 Controller 端点

#### `ProfilePublicController` 追加 3 个

```java
@GetMapping("/{username}/folders")
@RateLimit(key = "profile:folders:#{#username}:#{#loginId}", limit = 10, window = 1)
@AuditLog(action = "public-folders", description = "查询公开主页顶层收藏夹列表，username=#{#username}")
public HttpResult getPublicFolders(
    @PathVariable @NotBlank String username,
    @RequestParam(defaultValue = "5") @Min(1) @Max(50) Integer limit) { ... }

@GetMapping("/{username}/folders/{folderId}/children")
@RateLimit(key = "profile:folder-children:#{#username}:#{#folderId}:#{#loginId}", limit = 10, window = 1)
@AuditLog(action = "public-folder-children", description = "查询公开收藏夹子项，username=#{#username}, folderId=#{#folderId}")
public HttpResult getPublicFolderChildren(
    @PathVariable @NotBlank String username,
    @PathVariable @Positive Long folderId,
    @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
    @RequestParam(defaultValue = "12") @Min(1) @Max(50) Integer pageSize) { ... }

@GetMapping("/{username}/folders/tree")
@RateLimit(key = "profile:folders-tree:#{#username}:#{#loginId}", limit = 5, window = 1)
@AuditLog(action = "public-folders-tree", description = "查询公开收藏夹全量树，username=#{#username}")
public HttpResult getPublicFolderTree(@PathVariable @NotBlank String username) { ... }
```

公开端点不加 `@SaCheckLogin`，通过 `currentUserIdOrNull()` 获取访问者。

#### `UserFolderController` 追加 1 个

```java
@PutMapping("/{id}/public")
@SaCheckLogin
@AuditLog(action = "toggle-public", description = "切换收藏夹对外可见性，id=#{#id}")
public HttpResult togglePublic(
    @PathVariable @NotNull @Positive Long id,
    @RequestParam boolean isPublic) {
    userFolderService.toggleFolderPublic(id, isPublic);
    return HttpResult.success(isPublic ? "已对外公开" : "已对外私密");
}
```

### 4.5 隐私链路串联（关键）

```
访客 GET /api/user/profile/public/{username}/folders
    ↓
ProfilePublicController.getPublicFolders
    ↓
PublicFolderService.getTopFolders(targetUserId, currentUserId, limit)
    ↓
profilePublicService.checkDashboardAccess(targetUserId, currentUserId, false)
    ├─ 校验目标用户存在 + 未禁用
    ├─ 校验 visibility（PRIVATE/LOGGED 抛错）
    └─ 返回 (isOwn, visibility, showContribution, showCollections)
    ↓
若 !isOwn && showCollections == 0 → PROFILE_PRIVATE
    ↓
RedisCache.getOrLoad(顶层 key, loader)
    ↓
loader：t_user_folder 按 is_public=1 AND is_hide=0 过滤；本人不过滤
```

### 4.6 设置中心联动

`ProfilePrivacyServiceImpl.evictPublicCache` **追加**清三类公开收藏夹缓存：

```java
redisCache.evictByPattern(RedisConstant.PUBLIC_FOLDER_TOP_PREFIX + userId + ":*");
redisCache.evictByPattern(RedisConstant.PUBLIC_FOLDER_CHILDREN_PREFIX + userId + ":*");
redisCache.evict(RedisConstant.PUBLIC_FOLDER_TREE_PREFIX + userId);
```

保证用户切 `show_collections` / `profile_visibility` 后访客立即生效。

### 4.7 错误码（全部复用，不新增）

- `AuthErrorCode.PROFILE_PRIVATE` — 整体不可见或 showCollections=0
- `AuthErrorCode.PROFILE_LOGIN_REQUIRED` — visibility=logged 且未登录
- `AuthErrorCode.USER_DISABLED` — 用户不存在或被禁用
- `HttpStatus.BAD_REQUEST` — folderId 不属于该用户 / 已删除

---

## 5. 前端架构

### 5.1 路由（`router/index.ts`）

UserLayout 子路由组追加：

```ts
{
  path: 'profile/:username',
  name: 'publicProfile',
  component: () => import('@/views/user/ProfileView.vue'),
  meta: { title: '用户主页' }
},
{
  path: 'u/:username/collections',
  name: 'publicCollections',
  component: () => import('@/views/user/PublicCollectionView.vue'),
  meta: { title: '收藏夹' }
}
```

`ProfileView.vue` 兼容性：
- `/computer/profile`（无 username）→ 本人主页（`getCurrentUserProfile`）
- `/computer/profile/:username` → 走 `getPublicProfile(username)`
- 当 username === authStore.username → 切回本人模式，避免双取

### 5.2 API 层

`api/user-profile-public.ts` 追加：

```ts
export interface PublicFolderItem {
  id: number; name: string; icon: string; color: string;
  parentId: number; sort: number; websiteCount: number; childrenCount: number;
}

export interface PublicFolderChild {
  id: number; name: string; icon: string; color: string;
  parentId: number; websiteCount: number;
}

export interface PublicFolderWebsite {
  id: number; title: string; url: string;
  cover?: string; description?: string;
  categoryName?: string; tagNames?: string[];
  clickCount: number; likeCount: number; collectCount: number; score?: number;
}

export interface PublicFolderChildrenPage {
  subFolders: PublicFolderChild[];
  websites: { list: PublicFolderWebsite[]; total: number; pageNum: number; pageSize: number };
}

export interface PublicFolderTreeNode extends PublicFolderItem {
  children: PublicFolderTreeNode[];
}

export async function getPublicTopFolders(username: string, limit?: number): Promise<PublicFolderItem[]>
export async function getPublicFolderChildren(username: string, folderId: number, pageNum?: number, pageSize?: number): Promise<PublicFolderChildrenPage>
export async function getPublicFolderTree(username: string): Promise<PublicFolderTreeNode[]>
```

`api/user-folder.ts` 追加：

```ts
export async function toggleFolderPublic(id: number, isPublic: boolean): Promise<void>
```

### 5.3 类型扩展

`types/profile.ts`：

```ts
export interface ProfileFolderItem {
  id: number; name: string;
  icon?: string; color?: string;
  websiteCount: number; childrenCount: number;
}

// ProfileData 上加可选字段
publicFolders?: ProfileFolderItem[]
publicFoldersUsername?: string
```

`types/collection.ts` 的 `CollectionCategory` 加 `isPublic: number`。

### 5.4 Pinia Store

在 `stores/profilePublic.ts` 追加：

```ts
const publicFolders = ref<PublicFolderItem[]>([])
const foldersLoading = ref(false)
const foldersError = ref<string>('')

const loadPublicFolders = async (username: string, limit = 5) => {
  foldersLoading.value = true; foldersError.value = ''
  try { publicFolders.value = await getPublicTopFolders(username, limit) }
  catch (e) {
    foldersError.value = e instanceof Error ? e.message : '加载收藏夹失败'
    publicFolders.value = []
  }
  finally { foldersLoading.value = false }
}
```

`stores/folder.ts` 追加 `handleTogglePublic(id, isPublic)`，写完后 `loadFolderTree()` 刷新。

对话框/查看全部页的网站列表分页用**局部 state**（生命周期短，不入 store）。

### 5.5 组件改造与新增

#### 5.5.1 `ProfileSidebarCard.vue` L237-272 改造

替换"收藏网站"区块为"公开收藏夹"区块：

```vue
<StatCard v-if="(isOwn || showCollections) && publicFolders?.length" dense>
  <template #header>
    <div class="flex w-full items-center justify-between gap-2">
      <span class="flex items-center gap-1.5 text-[12.5px] font-semibold ...">
        <FolderTree class="size-3.5 text-amber-500" :stroke-width="2" />
        公开收藏夹
      </span>
      <span class="font-mono text-[10.5px] text-gray-500 ...">
        {{ publicFolders.length >= 5 ? '5+' : publicFolders.length }} 个
      </span>
    </div>
  </template>
  <ul class="space-y-1.5">
    <li v-for="folder in publicFolders" :key="folder.id">
      <button
        type="button"
        class="flex w-full cursor-pointer items-start gap-1.5 rounded-sm border border-black/5 bg-black/[0.02] p-1.5 transition-colors hover:border-amber-500/30 hover:bg-amber-500/5 ..."
        @click="openFolderDialog(folder)"
      >
        <Folder class="mt-0.5 size-3.5 shrink-0 text-amber-500" :stroke-width="2" />
        <div class="min-w-0 flex-1 text-left">
          <p class="truncate text-[12.5px] font-semibold ...">{{ folder.name }}</p>
          <p class="font-mono text-[11px] leading-5 text-gray-500 ...">
            {{ folder.websiteCount }} 个网站 · {{ folder.childrenCount }} 个子夹
          </p>
        </div>
      </button>
    </li>
  </ul>
</StatCard>
```

新增 props：

```ts
defineProps<{
  profile: ProfileData
  isOwn?: boolean
  showCollections?: boolean
  publicFolders?: ProfileFolderItem[]
  publicFoldersUsername?: string
}>()
```

**侧边栏统一样式**：图标固定 Folder + amber-500，**不用** folder.color/folder.icon。

#### 5.5.2 新建 `PublicFolderDialog.vue`

`@/components/user/profile/PublicFolderDialog.vue`，用 `<UModal>`。

结构：
- 标题区：folder.name + folder.icon（**展示自定义图标和颜色**） + websiteCount/childrenCount 徽章
- Body 上半：子文件夹网格（4 列），保留 color，**不可下钻**
- Body 下半：直属网站卡片网格（响应式），底部分页器
- Footer：`查看全部 →` 跳 `/computer/u/{username}/collections?folderId={folderId}`

数据：mounted 调 `getPublicFolderChildren(username, folder.id, 1, 12)`，分页换页重请求。

网站卡片**新建轻量版**（标题/url/cover/统计），不耦合本人收藏态。

#### 5.5.3 新建 `PublicCollectionView.vue`

`@/views/user/PublicCollectionView.vue`

布局：
- 顶部：访客上下文条（用户头像 + 用户名 + 返回主页）
- 左侧：折叠树（`getPublicFolderTree(username)`），保留 folder.icon/color
- 中间：当前激活节点的子项（同对话框结构，分页大小 24）
- 与本人 CollectionView **解耦**：无右键菜单、无创建/隐藏/删除、无搜索/批量操作

数据：
- mounted 一次拉满 `getPublicFolderTree(username)`
- 节点切换：`getPublicFolderChildren(username, folderId, page, size)`
- query `?folderId=N` → 默认激活该节点

#### 5.5.4 `ProfileView.vue` 支持 username 路由

```ts
const route = useRoute()
const authStore = useAuthStore()
const profilePublicStore = useProfilePublicStore()

const targetUsername = computed(() => {
  const fromRoute = route.params.username as string | undefined
  return fromRoute?.trim() || authStore.username || ''
})

const isOwnView = computed(() =>
  !route.params.username || route.params.username === authStore.username
)

const showCollections = computed(() =>
  isOwnView.value || profilePublicStore.privacy?.showCollections === 1
)

onMounted(async () => {
  if (isOwnView.value) {
    await loadProfile()
    dashboardStore.loadInitial()
    approvedSubmissions.load()
    if (authStore.username) {
      await profilePublicStore.loadPublicFolders(authStore.username, 5)
    }
  } else {
    await profilePublicStore.loadPublicProfile(targetUsername.value)
    await profilePublicStore.loadPublicFolders(targetUsername.value, 5)
    // 看板按 targetUserId 分流（dashboardStore 需扩展 targetUserId 参数；
    // 本次范围若聚焦收藏夹链路，可暂在访客主页隐藏看板）
  }
})
```

侧边栏接入：

```vue
<ProfileSidebarCard
  :profile="displayProfile"
  :is-own="isOwnView"
  :show-collections="showCollections"
  :public-folders="profilePublicStore.publicFolders"
  :public-folders-username="targetUsername"
/>
```

对话框接入：放在 `ProfileSidebarCard.vue` 内（局部状态 + emit），因对话框只与该侧边栏交互。

#### 5.5.5 `CollectionView.vue` 右键菜单加切换项

`buildFolderMenuItems(category)` 加：

```ts
{
  label: category.isPublic ? '设为对外私密' : '对外公开',
  icon: category.isPublic ? 'lock' : 'globe',
  disabled: category.isHide === 1,
  onClick: () => folderStore.handleTogglePublic(category.id, category.isPublic !== 1),
}
```

### 5.6 跳转链路

```
ProfileSidebarCard 顶层卡片点击
    ↓
PublicFolderDialog 打开（拉一层子项）
    ↓
点击「查看全部 →」
    ↓
router.push(`/computer/u/${username}/collections?folderId=${folderId}`)
    ↓
PublicCollectionView 加载 → 全量树 + 默认激活该 folder
```

### 5.7 设置中心（前端无改动）

`PrivacySettingsSection.vue` 现有 `show_collections` 开关已存在，关闭后 `getPublicTopFolders` 抛 `PROFILE_PRIVATE` → 前端按错误码地图渲染整块空态。

---

## 6. 验收清单（DoD）

### 6.1 后端
- [ ] `t_user_folder` 含 `is_public` 字段 + `idx_user_public_hide` 索引
- [ ] `UserFolder` POJO 加 `isPublic`
- [ ] 6 个 VO/DTO 类创建完成
- [ ] `PublicFolderService` + impl 实现 3 方法，全部走 `RedisCache.getOrLoad` + 隐私校验
- [ ] `UserFolderServiceImpl` 加 `toggleFolderPublic` + 抽出 `evictPublicFolderCache`
- [ ] 7 处写入点（folder 5 处 + collect 2 处）调用 `evictPublicFolderCache`
- [ ] `ProfilePublicController` 加 3 端点，`UserFolderController` 加 1 端点
- [ ] `ProfilePrivacyServiceImpl.evictPublicCache` 追加清三类公开收藏夹缓存
- [ ] `RedisConstant` 加 3 常量
- [ ] 端点带 `@SaCheckLogin` / `@AuditLog` / `@RateLimit`，与现有 dashboard 一致

### 6.2 前端
- [ ] `router/index.ts` 加 2 路由
- [ ] `api/user-profile-public.ts` 加 3 函数；`api/user-folder.ts` 加 `toggleFolderPublic`
- [ ] `types/profile.ts` 加 `ProfileFolderItem`，`ProfileData` 加新字段
- [ ] `types/collection.ts` 的 `CollectionCategory` 加 `isPublic`
- [ ] `stores/profilePublic.ts` 加 `publicFolders` + `loadPublicFolders`
- [ ] `stores/folder.ts` 加 `handleTogglePublic`
- [ ] `ProfileSidebarCard.vue` L237-272 改造为公开收藏夹区块（统一图标 + 主色）
- [ ] 新建 `PublicFolderDialog.vue`
- [ ] 新建 `PublicCollectionView.vue`
- [ ] `ProfileView.vue` 支持 `:username` + isOwn 模式分流
- [ ] `CollectionView.vue` 右键菜单加切换项（is_hide=1 禁用）

### 6.3 数据流连通性
- [ ] 7 处写入点 → 自动清 3 类公开收藏夹缓存
- [ ] 设置中心改 `show_collections` → 端点立返 `PROFILE_PRIVATE`
- [ ] 切 `profile_visibility=private` → 端点全返 `PROFILE_PRIVATE`
- [ ] 用户重命名（username 变更） → 现有 `evictPublicCache` 已处理，扩展自动覆盖
- [ ] 访客点对话框"查看全部" → 跳转携带 `folderId` query → 落地页默认激活

---

## 7. 测试要点

### 7.1 隐私权限矩阵（最关键）

| 场景 | visibility | showCollections | folder.is_public | folder.is_hide | 期望 |
|---|---|---|---|---|---|
| 本人访问自己 | private | 0 | 任意 | 任意 | 看到全部（含未公开夹） |
| 访客访问 public 主页 | public | 1 | 1 | 0 | 看到该夹 |
| 访客访问 public 主页 | public | 1 | 0 | 0 | **不可见** |
| 访客访问 public 主页 | public | 1 | 1 | 1 | **不可见**（is_hide 优先） |
| 访客访问 logged 主页（未登录） | logged | 1 | 1 | 0 | PROFILE_LOGIN_REQUIRED |
| 访客访问 private 主页 | private | 1 | 1 | 0 | PROFILE_PRIVATE |
| showCollections=0 | public | **0** | 1 | 0 | PROFILE_PRIVATE |

### 7.2 单元 / 集成测试

1. 空收藏夹过滤：顶层接口对 websiteCount=0 且无可见子文件夹的过滤生效，本人不过滤
2. 缓存：写操作后 `evictByPattern` 命中所有相关 key（`redisTemplate.keys` 断言）
3. 越权写：用户 A 调 `togglePublic(folderIdOfB)` → 抛错
4. 限流：连续 11 次顶层端点 → 第 11 次抛限流错

### 7.3 前端 E2E（可选）

1. 自己访问 `/computer/profile` → 侧边栏看到自己所有顶层夹（含未公开）
2. 自己访问 `/computer/profile/{别人}` → 只看到对方公开夹
3. 点击侧边栏夹 → 对话框打开 → 子项加载 → "查看全部" 跳访客版页
4. 访客版页：左树 / 右子项分页 / `folderId` query 默认激活
5. 本人 CollectionView 右键 → 切对外公开 → toast 成功 → 重载 → 状态生效
6. 设置 → 隐私 → 关"展示收藏列表" → 别人主页侧边栏整块消失

---

## 8. 风险点 & 缓解

| 风险 | 影响 | 缓解 |
|---|---|---|
| 缓存污染：写接口漏 evict | 数据陈旧、隐私泄露 | 抽 `evictPublicFolderCache(userId)`，所有写入点强制调用，单测覆盖 |
| N+1 查询：顶层列表查 N 个夹的 child/website count | 响应慢 | `t_user_folder.website_count` 已存在；`childrenCount` 用一次 GROUP BY |
| 复合索引未命中 | 慢查询 | `idx_user_public_hide` 覆盖核心条件，`EXPLAIN` 验证 |
| 本人 ProfileView 双取数据 | 浪费请求 | `isOwnView` 分流，避免 `getCurrentUserProfile` + `loadPublicProfile` 同时发 |
| dashboardStore 当前只支持本人 | 访客主页看板报错 | **本次范围**：访客主页隐藏看板（仅渲染基础资料 + 公开收藏夹），dashboardStore 扩展 `targetUserId` 留作后续 PR |
| ProfileView 改造涉及面广 | 回归风险 | `/computer/profile` 行为不变兜底，仅 `:username` 分支新增逻辑 |
| 网站卡片复用复杂度 | 公开版需去耦 | 对话框 / 访客页用**轻量卡片**，仅展示标题/url/cover/统计 |
| is_hide × is_public 切换 UX | 用户困惑 | 右键菜单 is_hide=1 时禁用"对外公开"切换 + tooltip |
| 老用户数据全部默认 is_public=0 | "突然全没了" | 安全优先决策，release notes 提示主动开启 |

---

## 9. 范围外（明确不做）

- 收藏夹设置中心批量公开/私密管理面板
- 对话框内多层下钻
- 公开收藏夹访问审计深度（仅基础 `@AuditLog`）
- 公开收藏夹 RSS / 订阅
- 访客在公开页直接收藏到自己收藏夹（走常规登录态收藏流程，不在本次范围）

---

## 10. 后续步骤

完成本设计后下一步是调用 `writing-plans` skill 生成实施计划文档（任务拆分 / 依赖图 / 检查点），保存到 `superpowers/plans/2026-05-10-public-folders-plan.md`。

