<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-11
 *
 * 公开主页"查看全部公开收藏夹"页面（user-15）。
 *
 */

import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ExternalLink,
  Lock,
  ArrowLeft,
  ArrowUpRight,
  ChevronLeft,
  ChevronRight,
  Star,
} from 'lucide-vue-next'
import FolderTreeRow from '@/components/user/profile/FolderTreeRow.vue'
import folderIcon from '@/assets/icons/png/folder.png'
import websiteClickIcon from '@/assets/icons/svg/user-website-click.svg'
import websiteLikeIcon from '@/assets/icons/svg/user-website-like.svg'
import websiteCollectionIcon from '@/assets/icons/svg/user-website-collection.svg'
import {
  getPublicFolderChildren,
  getPublicFolderTree,
  getPublicProfile,
  type ProfilePublicResponse,
  type PublicFolderChildrenResponse,
  type PublicFolderTreeNode,
  type PublicFolderTreeResponse,
  type PublicFolderWebsite,
} from '@/api/user-profile-public'

const route = useRoute()
const router = useRouter()

const PAGE_SIZE = 12
const PROFILE_LOGIN_REQUIRED_CODE = 100245
const PROFILE_PRIVATE_CODE = 100246
const USER_DISABLED_CODE = 100204

const username = computed(() => String(route.params.username || '').trim())

const profile = ref<ProfilePublicResponse | null>(null)
const profileLoading = ref(false)
const profileErrorCode = ref<number | null>(null)
const profileErrorMsg = ref('')

const treeData = ref<PublicFolderTreeResponse | null>(null)
const treeLoading = ref(false)
const treeErrorMsg = ref('')

const expandedIds = ref<Set<number>>(new Set())
const selectedId = ref<number | null>(null)

const childrenData = ref<PublicFolderChildrenResponse | null>(null)
const childrenLoading = ref(false)
const childrenErrorMsg = ref('')
const pageNum = ref(1)

/**
 * 请求序号：切换收藏夹/翻页时自增，loadChildren 内部只有序号一致的响应才会写入 state；
 * 配合同步的 `resetChildrenState()` 消除"旧数据在新响应前闪一下"的根因。
 */
let childrenReqSeq = 0

/** 骨架最短展示时长（ms）：防止缓存/极速响应时 skeleton → content 闪烁。 */
const MIN_SKELETON_MS = 180

const isPrivate = computed(() => profileErrorCode.value === PROFILE_PRIVATE_CODE)
const isLoginRequired = computed(() => profileErrorCode.value === PROFILE_LOGIN_REQUIRED_CODE)
const isNotFound = computed(() => profileErrorCode.value === USER_DISABLED_CODE)
const isOwn = computed(() => profile.value?.isOwn === true)
const showCollections = computed(() => isOwn.value || profile.value?.privacy?.showCollections !== 0)

const totalPages = computed(() => {
  const total = childrenData.value?.websites.total ?? 0
  return Math.max(1, Math.ceil(total / PAGE_SIZE))
})
const showPagination = computed(() => (childrenData.value?.websites.total ?? 0) > PAGE_SIZE)

const allRootIds = computed(() => treeData.value?.roots.map((r) => r.id) ?? [])

const findNode = (nodes: PublicFolderTreeNode[], id: number): PublicFolderTreeNode | null => {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) {
      const hit = findNode(n.children, id)
      if (hit) return hit
    }
  }
  return null
}

const selectedNode = computed(() => {
  if (selectedId.value === null || !treeData.value) return null
  return findNode(treeData.value.roots, selectedId.value)
})

const resolveErrorCode = (error: unknown): number | null => {
  if (typeof error === 'object' && error !== null && 'code' in error) {
    const code = (error as { code?: unknown }).code
    return typeof code === 'number' ? code : null
  }
  return null
}

const loadProfile = async () => {
  if (!username.value) return
  profileLoading.value = true
  profileErrorCode.value = null
  profileErrorMsg.value = ''
  try {
    profile.value = await getPublicProfile(username.value)
  } catch (err) {
    profile.value = null
    profileErrorCode.value = resolveErrorCode(err)
    profileErrorMsg.value = err instanceof Error ? err.message : '加载主页失败'
  } finally {
    profileLoading.value = false
  }
}

const loadTree = async () => {
  if (!username.value) return
  treeLoading.value = true
  treeErrorMsg.value = ''
  try {
    treeData.value = await getPublicFolderTree(username.value)
    expandedIds.value = new Set(allRootIds.value)
    const firstId = treeData.value.roots[0]?.id ?? null
    if (firstId !== null) {
      selectedId.value = firstId
      pageNum.value = 1
      resetChildrenState()
      void loadChildren()
    }
  } catch (err) {
    treeData.value = null
    treeErrorMsg.value = err instanceof Error ? err.message : '加载收藏夹树失败'
  } finally {
    treeLoading.value = false
  }
}

/**
 * 同步重置右侧内容 state：立刻清空旧数据并进入 loading 态。
 *
 * 这是"切夹闪白"修复的关键 —— 在任何 `loadChildren` 调用**之前**，
 * 同一个 tick 内把 `childrenData` 设为 null、`childrenLoading` 设为 true，
 * 避免 Vue 批处理把旧数据 + 新 key 扔进 transition-group diff。
 */
const resetChildrenState = () => {
  childrenData.value = null
  childrenErrorMsg.value = ''
  childrenLoading.value = true
}

const loadChildren = async () => {
  if (!username.value || selectedId.value === null) return
  const mySeq = ++childrenReqSeq
  // 保险：调用方可能没调 resetChildrenState（例如初始 mount），这里兜底打 loading
  if (!childrenLoading.value) childrenLoading.value = true
  childrenErrorMsg.value = ''
  const startedAt = Date.now()
  try {
    const data = await getPublicFolderChildren(username.value, selectedId.value, {
      pageNum: pageNum.value,
      pageSize: PAGE_SIZE,
    })
    if (mySeq !== childrenReqSeq) return // 已有更新的请求，丢弃该响应
    const elapsed = Date.now() - startedAt
    if (elapsed < MIN_SKELETON_MS) {
      await new Promise((r) => setTimeout(r, MIN_SKELETON_MS - elapsed))
      if (mySeq !== childrenReqSeq) return
    }
    childrenData.value = data
  } catch (err) {
    if (mySeq !== childrenReqSeq) return
    childrenData.value = null
    childrenErrorMsg.value = err instanceof Error ? err.message : '加载收藏夹内容失败'
  } finally {
    if (mySeq === childrenReqSeq) {
      childrenLoading.value = false
    }
  }
}

const toggleExpand = (id: number) => {
  const next = new Set(expandedIds.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  expandedIds.value = next
}

const handleSelect = (node: PublicFolderTreeNode) => {
  if (selectedId.value === node.id) return
  selectedId.value = node.id
  pageNum.value = 1
  resetChildrenState()
  void loadChildren()
}

/**
 * 子收藏夹按钮跳转（原本内联在 template 里，抽成方法以便统一走 resetChildrenState，
 * 避免遗漏闪白修复）。
 */
const jumpToSubFolder = (sf: { id: number; parentId: number }) => {
  if (selectedId.value === sf.id) return
  selectedId.value = sf.id
  pageNum.value = 1
  if (!expandedIds.value.has(sf.parentId)) {
    const next = new Set(expandedIds.value)
    next.add(sf.parentId)
    expandedIds.value = next
  }
  resetChildrenState()
  void loadChildren()
}

const handlePageChange = (num: number) => {
  if (num < 1 || num > totalPages.value || num === pageNum.value) return
  pageNum.value = num
  resetChildrenState()
  void loadChildren()
}

const goBackToProfile = () => {
  router.push({
    name: 'profilePublic',
    params: { username: username.value },
  })
}

const formatScore = (score: PublicFolderWebsite['score']) =>
  score === null || score === undefined ? '—' : Number(score).toFixed(1)

/**
 * stats 列数字格式化：>= 10000 -> "1.2k"；保持窄列对齐 + 不溢出。
 */
const formatCount = (n: number | null | undefined): string => {
  const v = Number(n ?? 0)
  if (!Number.isFinite(v) || v <= 0) return '0'
  if (v >= 10000) return `${(v / 1000).toFixed(v >= 100000 ? 0 : 1)}k`
  if (v >= 1000) return `${(v / 1000).toFixed(1)}k`
  return String(Math.trunc(v))
}

const websitePlaceholder = (title: string) =>
  `https://api.dicebear.com/7.x/shapes/svg?seed=${encodeURIComponent(title)}&backgroundType=gradientLinear&backgroundColor=fbbf24,f59e0b`

/** 递归汇总树中所有公开网站总数（hero 右侧统计用） */
const totalSiteCount = computed(() => {
  const sum = (nodes: PublicFolderTreeNode[]): number =>
    nodes.reduce((acc, n) => acc + n.websiteCount + (n.children?.length ? sum(n.children) : 0), 0)
  return treeData.value ? sum(treeData.value.roots) : 0
})

/**
 * 移动端用顶部 chip 选择器：仅展示根节点，深层通过右侧"子收藏夹"区下钻，避免移动端树嵌套体验差。
 */
const mobileRootList = computed<PublicFolderTreeNode[]>(() => treeData.value?.roots ?? [])

onMounted(() => {
  void Promise.all([loadProfile(), loadTree()])
})

watch(
  () => username.value,
  (cur, prev) => {
    if (cur && cur !== prev) {
      profile.value = null
      treeData.value = null
      childrenData.value = null
      childrenErrorMsg.value = ''
      childrenLoading.value = false
      childrenReqSeq += 1 // 作废上一用户的任何在途请求
      selectedId.value = null
      expandedIds.value = new Set()
      pageNum.value = 1
      void loadProfile()
      void loadTree()
    }
  },
)
</script>

<template>
  <div class="cf-public-collection mx-auto w-full max-w-[1400px] px-4 pb-12 pt-6 sm:px-8 sm:pt-8">
    <!-- 顶部返回（更克制；纯 mono、hover 仅文字色） -->
    <button
      type="button"
      class="cf-fade-in group mb-7 inline-flex cursor-pointer items-center gap-2 font-mono text-[11px] uppercase tracking-[0.2em] text-zinc-400 transition-colors hover:text-amber-600 active:scale-[0.98] dark:text-zinc-500 dark:hover:text-amber-400"
      @click="goBackToProfile"
    >
      <ArrowLeft
        class="size-3.5 transition-transform duration-200 group-hover:-translate-x-0.5"
        :stroke-width="2"
      />
      <span>Back · {{ username }}</span>
    </button>

    <!-- Hero · 编辑式不对称版面：左标题/右统计列 + hairline 分隔，无卡片框 -->
    <header
      class="cf-fade-in mb-10 grid grid-cols-1 gap-6 border-b border-zinc-200 pb-8 lg:grid-cols-[1fr_auto] lg:items-end lg:gap-12 dark:border-zinc-900"
      style="animation-delay: 40ms"
    >
      <!-- 左：mono eyebrow + 大字标题 + 用户元 -->
      <div class="flex min-w-0 flex-col gap-3">
        <div
          class="flex items-center gap-3 font-mono text-[10.5px] uppercase tracking-[0.28em] text-amber-600 dark:text-amber-400"
        >
          <span class="inline-block h-px w-8 bg-amber-500/70 dark:bg-amber-400/70" />
          <span>Public · Collections</span>
        </div>
        <h1
          class="cf-hero-title min-w-0 break-words text-[2.5rem] font-bold leading-[1.02] text-zinc-900 sm:text-5xl lg:text-[3.75rem] dark:text-zinc-50"
        >
          <span class="block">{{ profile?.user?.nickname || username }}</span>
          <span class="block text-zinc-400 dark:text-zinc-600" style="letter-spacing: -0.025em">
            的<span class="text-amber-500 dark:text-amber-400">公开</span>收藏夹
          </span>
        </h1>
        <div class="flex items-center gap-3 text-[13px] text-zinc-500 dark:text-zinc-400">
          <!-- 头像作为细节而非主体 -->
          <span
            class="inline-flex size-7 shrink-0 overflow-hidden rounded-full ring-1 ring-zinc-200 dark:ring-zinc-800"
          >
            <img
              v-if="profile?.user?.avatar"
              :src="profile.user.avatar"
              :alt="profile.user.username"
              class="h-full w-full object-cover"
              referrerpolicy="no-referrer"
            />
            <span
              v-else
              class="flex h-full w-full items-center justify-center bg-amber-100 font-mono text-[11px] font-semibold text-amber-700 dark:bg-amber-500/[0.1] dark:text-amber-300"
            >
              {{ (username[0] || '?').toUpperCase() }}
            </span>
          </span>
          <span class="font-mono">@{{ username }}</span>
          <span
            v-if="isOwn"
            class="font-mono text-[10.5px] uppercase tracking-[0.18em] text-amber-600 dark:text-amber-400"
          >
            · Owner
          </span>
        </div>
      </div>

      <!-- 右：3 条 mono 数字统计列（替代原渐变装饰） -->
      <dl
        class="flex shrink-0 divide-x divide-zinc-200 self-stretch text-left lg:self-end dark:divide-zinc-900"
      >
        <div class="flex flex-col gap-1 pr-6">
          <dt
            class="font-mono text-[10px] uppercase tracking-[0.2em] text-zinc-400 dark:text-zinc-600"
          >
            Folders
          </dt>
          <dd class="font-mono text-3xl font-semibold tabular-nums text-zinc-900 dark:text-zinc-50">
            {{ treeData?.roots.length ?? 0 }}
          </dd>
        </div>
        <div class="flex flex-col gap-1 px-6">
          <dt
            class="font-mono text-[10px] uppercase tracking-[0.2em] text-zinc-400 dark:text-zinc-600"
          >
            Sources
          </dt>
          <dd class="font-mono text-3xl font-semibold tabular-nums text-zinc-900 dark:text-zinc-50">
            {{ formatCount(totalSiteCount) }}
          </dd>
        </div>
        <div class="flex flex-col gap-1 pl-6">
          <dt
            class="font-mono text-[10px] uppercase tracking-[0.2em] text-zinc-400 dark:text-zinc-600"
          >
            Selected
          </dt>
          <dd
            class="font-mono text-3xl font-semibold tabular-nums text-amber-500 dark:text-amber-400"
          >
            {{ selectedNode?.websiteCount ?? '—' }}
          </dd>
        </div>
      </dl>
    </header>

    <!-- 隐私拦截态：编辑式空态，无卡片背景，居中 mono 文案 + 锁图标 -->
    <div v-if="isNotFound" class="cf-fade-in flex flex-col items-center gap-4 py-20 text-center">
      <Lock class="size-10 text-zinc-300 dark:text-zinc-700" :stroke-width="1.25" />
      <p class="font-mono text-[11px] uppercase tracking-[0.22em] text-zinc-500 dark:text-zinc-500">
        User · Not Found
      </p>
      <p class="text-[13px] text-zinc-600 dark:text-zinc-400">用户不存在或已停用</p>
    </div>
    <div
      v-else-if="isLoginRequired"
      class="cf-fade-in flex flex-col items-center gap-4 py-20 text-center"
    >
      <Lock class="size-10 text-zinc-300 dark:text-zinc-700" :stroke-width="1.25" />
      <p class="font-mono text-[11px] uppercase tracking-[0.22em] text-zinc-500 dark:text-zinc-500">
        Sign In · Required
      </p>
      <p class="text-[13px] text-zinc-600 dark:text-zinc-400">需要登录后查看</p>
    </div>
    <div
      v-else-if="isPrivate || (!showCollections && !isOwn)"
      class="cf-fade-in flex flex-col items-center gap-4 py-20 text-center"
    >
      <Lock class="size-10 text-zinc-300 dark:text-zinc-700" :stroke-width="1.25" />
      <p class="font-mono text-[11px] uppercase tracking-[0.22em] text-zinc-500 dark:text-zinc-500">
        Private · Collections
      </p>
      <p class="text-[13px] text-zinc-600 dark:text-zinc-400">该用户的收藏夹不公开</p>
    </div>

    <!-- 主体：左树 + 右内容（去除 rounded 卡片框；用 divide-x + 内边距分栏） -->
    <div v-else class="grid grid-cols-1 gap-8 lg:grid-cols-[280px_1fr] lg:gap-10">
      <!-- 移动端：水平 chip 选择器（root 列表） -->
      <div
        v-if="mobileRootList.length"
        class="cf-fade-in -mx-4 overflow-x-auto px-4 lg:hidden"
        style="animation-delay: 80ms"
      >
        <div class="flex min-w-min gap-2 pb-1">
          <button
            v-for="(root, idx) in mobileRootList"
            :key="root.id"
            type="button"
            :class="[
              'cf-chip group inline-flex shrink-0 cursor-pointer items-center gap-2 border border-zinc-200 bg-white px-3.5 py-2 text-[12.5px] font-medium transition-colors duration-200 active:scale-[0.97] dark:border-zinc-800 dark:bg-transparent',
              selectedId === root.id
                ? 'border-amber-500 text-amber-700 dark:border-amber-400 dark:text-amber-300'
                : 'text-zinc-700 hover:border-zinc-300 dark:text-zinc-300 dark:hover:border-zinc-700',
            ]"
            :style="{ animationDelay: `${idx * 40 + 100}ms` }"
            @click="handleSelect(root)"
          >
            <img
              :src="folderIcon"
              alt=""
              aria-hidden="true"
              class="size-5 transition-transform duration-200 group-hover:scale-110"
              :class="selectedId === root.id ? 'scale-110' : ''"
            />
            <span class="max-w-[140px] truncate">{{ root.name }}</span>
            <span class="font-mono text-[10.5px] tabular-nums opacity-60">
              {{ root.websiteCount }}
            </span>
          </button>
        </div>
      </div>

      <!-- 左侧树（桌面）：完全无卡片框；section 标签 + divide-y 行 -->
      <aside
        class="cf-fade-in hidden self-start lg:block lg:border-r lg:border-zinc-200 lg:pr-6 dark:lg:border-zinc-900"
        style="animation-delay: 80ms"
      >
        <div
          class="mb-4 flex items-center justify-between border-b border-zinc-200 pb-2.5 dark:border-zinc-900"
        >
          <h2
            class="font-mono text-[10.5px] uppercase tracking-[0.22em] text-zinc-500 dark:text-zinc-400"
          >
            Index
          </h2>
          <span class="font-mono text-[10.5px] tabular-nums text-zinc-400 dark:text-zinc-600">
            {{ treeData?.roots.length ?? 0 }}
          </span>
        </div>

        <!-- skeleton loading -->
        <ul v-if="treeLoading" class="space-y-1">
          <li v-for="i in 6" :key="i" class="cf-skeleton flex items-center gap-3 py-2.5">
            <span class="h-3 w-5 bg-zinc-200/70 dark:bg-zinc-800/70" />
            <span class="size-7 rounded bg-zinc-200/70 dark:bg-zinc-800/70" />
            <span class="h-3 flex-1 bg-zinc-200/70 dark:bg-zinc-800/70" />
          </li>
        </ul>
        <div
          v-else-if="treeErrorMsg"
          class="border-l-2 border-red-500 bg-transparent py-2 pl-3 font-mono text-[11.5px] text-red-600 dark:border-red-400 dark:text-red-400"
        >
          {{ treeErrorMsg }}
        </div>
        <div
          v-else-if="!treeData?.roots.length"
          class="py-6 text-center font-mono text-[11px] uppercase tracking-[0.18em] text-zinc-400 dark:text-zinc-600"
        >
          Empty · No Folders
        </div>
        <ul v-else class="divide-y divide-zinc-100 dark:divide-zinc-900/80">
          <li v-for="(node, idx) in treeData.roots" :key="node.id">
            <FolderTreeRow
              :node="node"
              :selected-id="selectedId"
              :expanded-ids="expandedIds"
              :index="idx"
              @toggle="toggleExpand"
              @select="handleSelect"
            />
          </li>
        </ul>
      </aside>

      <!-- 右侧内容（无卡片框；section 用 mono 标签 + divide-y 行流） -->
      <main class="cf-fade-in min-h-[420px]" style="animation-delay: 120ms">
        <!-- 选中夹标题区：超大 folder.png + 大字标题 + mono 副标题，hairline 分隔 -->
        <header
          class="mb-7 flex items-end gap-4 border-b border-zinc-200 pb-5 sm:gap-5 dark:border-zinc-900"
        >
          <img
            :src="folderIcon"
            alt=""
            aria-hidden="true"
            class="cf-header-folder size-14 shrink-0 sm:size-16"
            loading="lazy"
          />
          <div class="flex min-w-0 flex-1 flex-col gap-1.5">
            <span
              class="font-mono text-[10px] uppercase tracking-[0.22em] text-amber-600 dark:text-amber-400"
            >
              Folder
            </span>
            <h2
              class="truncate text-[1.5rem] font-bold leading-tight text-zinc-900 sm:text-[1.75rem] dark:text-zinc-50"
              style="letter-spacing: -0.015em"
            >
              {{ selectedNode?.name || '请选择收藏夹' }}
            </h2>
            <p
              class="flex flex-wrap items-center gap-x-3 gap-y-1 font-mono text-[10.5px] uppercase tracking-[0.16em] text-zinc-500 dark:text-zinc-500"
            >
              <span v-if="selectedNode" class="tabular-nums">
                {{ selectedNode.websiteCount }} sources
              </span>
              <span v-else>Select a folder</span>
              <span v-if="childrenData?.subFolders.length" class="tabular-nums">
                · {{ childrenData.subFolders.length }} sub-folders
              </span>
            </p>
          </div>
        </header>

        <!-- 内容切换用 out-in 过渡：确保 skeleton/错误/内容三态彻底顺序交接，根除闪白 -->
        <Transition name="cf-content" mode="out-in">
          <!-- skeleton loading: 4 行，shimmer -->
          <ul
            v-if="childrenLoading"
            key="skeleton"
            class="divide-y divide-zinc-100 dark:divide-zinc-900/80"
          >
            <li v-for="i in 4" :key="i" class="cf-skeleton flex items-center gap-4 py-5">
              <span class="size-12 shrink-0 rounded bg-zinc-200/70 dark:bg-zinc-800/70" />
              <span class="flex flex-1 flex-col gap-2">
                <span class="h-4 w-1/3 bg-zinc-200/70 dark:bg-zinc-800/70" />
                <span class="h-3 w-2/3 bg-zinc-200/70 dark:bg-zinc-800/70" />
              </span>
              <span class="hidden gap-6 sm:flex">
                <span class="h-3 w-10 bg-zinc-200/70 dark:bg-zinc-800/70" />
                <span class="h-3 w-10 bg-zinc-200/70 dark:bg-zinc-800/70" />
                <span class="h-3 w-10 bg-zinc-200/70 dark:bg-zinc-800/70" />
                <span class="h-3 w-10 bg-zinc-200/70 dark:bg-zinc-800/70" />
              </span>
            </li>
          </ul>

          <div
            v-else-if="childrenErrorMsg"
            key="error"
            class="border-l-2 border-red-500 py-2 pl-3 font-mono text-[12px] text-red-600 dark:border-red-400 dark:text-red-400"
          >
            {{ childrenErrorMsg }}
          </div>

          <!-- 内容区：整块用 selectedId-pageNum 作 key，确保切夹/翻页都是干净的 unmount→mount -->
          <div
            v-else-if="childrenData"
            :key="`content-${selectedId}-${pageNum}`"
            class="cf-content-block"
          >
            <!-- 子收藏夹（横向滚动 chip 行，无卡片网格） -->
            <section v-if="childrenData.subFolders.length" class="mb-8">
              <h3
                class="mb-3 flex items-center gap-2 font-mono text-[10px] uppercase tracking-[0.22em] text-zinc-500 dark:text-zinc-500"
              >
                <span class="h-px w-6 bg-zinc-300 dark:bg-zinc-700" />
                Sub-Folders
              </h3>
              <div class="-mx-4 overflow-x-auto px-4 sm:mx-0 sm:px-0">
                <div class="flex min-w-min gap-2 pb-1">
                  <button
                    v-for="(sf, idx) in childrenData.subFolders"
                    :key="sf.id"
                    type="button"
                    class="cf-subfolder group inline-flex shrink-0 cursor-pointer items-center gap-2.5 border border-zinc-200 bg-transparent px-3.5 py-2 text-left transition-colors duration-200 hover:border-amber-500 active:scale-[0.98] dark:border-zinc-800 dark:hover:border-amber-400"
                    :style="{ animationDelay: `${idx * 35}ms` }"
                    @click="jumpToSubFolder(sf)"
                  >
                    <img
                      :src="folderIcon"
                      alt=""
                      aria-hidden="true"
                      class="size-7 shrink-0 transition-transform duration-200 group-hover:scale-110 group-hover:rotate-[-4deg]"
                      loading="lazy"
                    />
                    <span class="flex min-w-0 flex-col">
                      <span
                        class="max-w-[160px] truncate text-[12.5px] font-medium text-zinc-800 group-hover:text-amber-700 dark:text-zinc-200 dark:group-hover:text-amber-300"
                      >
                        {{ sf.name }}
                      </span>
                      <span
                        class="font-mono text-[10px] uppercase tracking-wider text-zinc-400 dark:text-zinc-600"
                      >
                        <span class="tabular-nums">{{ sf.websiteCount }}</span>
                        sources
                      </span>
                    </span>
                  </button>
                </div>
              </div>
            </section>

            <!-- 网站列表（divide-y 行流；右侧 4 列 mono stats 替代 URL） -->
            <section>
              <h3
                class="mb-2 flex items-center gap-2 font-mono text-[10px] uppercase tracking-[0.22em] text-zinc-500 dark:text-zinc-500"
              >
                <span class="h-px w-6 bg-zinc-300 dark:bg-zinc-700" />
                Sources
                <span class="text-zinc-400 dark:text-zinc-600">·</span>
                <span class="tabular-nums text-zinc-700 dark:text-zinc-300">
                  {{ childrenData.websites.total }}
                </span>
              </h3>

              <!-- 网站行：不再用 transition-group（避免 absolute-leave 浮层遮盖新内容造成闪白）。
                 父级已被 <Transition> + :key 包裹，整块会干净重挂载；
                 每个 <li> 用 cfFadeInUp CSS 动画做级联入场，动画 delay 即 stagger。 -->
              <ul
                v-if="childrenData.websites.list.length"
                class="divide-y divide-zinc-100 dark:divide-zinc-900/80"
              >
                <li
                  v-for="(site, idx) in childrenData.websites.list"
                  :key="site.id"
                  class="cf-card-item relative"
                  :style="{ animationDelay: `${Math.min(idx * 40, 360)}ms` }"
                >
                  <a
                    :href="site.url"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="cf-website-row group relative flex flex-col gap-3 px-3 py-4 transition-colors duration-200 hover:bg-zinc-100/60 active:scale-[0.995] sm:grid sm:grid-cols-[auto_minmax(0,1fr)_auto_auto] sm:items-center sm:gap-5 sm:px-4 sm:py-5 dark:hover:bg-white/[0.025]"
                  >
                    <!-- 编辑式 mono 索引（仅桌面） -->
                    <span
                      class="hidden shrink-0 self-start pt-1.5 font-mono text-[10.5px] tabular-nums tracking-wider text-zinc-300 transition-colors group-hover:text-amber-500 sm:block dark:text-zinc-700 dark:group-hover:text-amber-400"
                    >
                      {{ String((pageNum - 1) * PAGE_SIZE + idx + 1).padStart(2, '0') }}
                    </span>

                    <!-- 标题 + 描述（移动端图标内联在标题左侧） -->
                    <div class="flex min-w-0 items-start gap-3 sm:items-center sm:gap-4">
                      <span
                        class="relative flex size-11 shrink-0 items-center justify-center overflow-hidden rounded border border-zinc-200 bg-white sm:size-12 dark:border-zinc-800 dark:bg-zinc-950"
                      >
                        <img
                          :src="site.cover || websitePlaceholder(site.title)"
                          :alt="site.title"
                          class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                          referrerpolicy="no-referrer"
                          loading="lazy"
                        />
                      </span>
                      <div class="flex min-w-0 flex-1 flex-col gap-1">
                        <div class="flex items-center gap-2">
                          <h4
                            class="truncate text-[15px] font-semibold leading-tight text-zinc-900 transition-colors group-hover:text-amber-700 dark:text-zinc-50 dark:group-hover:text-amber-300"
                            style="letter-spacing: -0.005em"
                          >
                            {{ site.title }}
                          </h4>
                          <span
                            v-if="site.categoryName"
                            class="hidden shrink-0 font-mono text-[10px] uppercase tracking-[0.18em] text-amber-600 sm:inline-flex dark:text-amber-400"
                          >
                            · {{ site.categoryName }}
                          </span>
                        </div>
                        <p
                          v-if="site.description"
                          class="line-clamp-1 max-w-[65ch] text-[12.5px] leading-5 text-zinc-500 dark:text-zinc-400"
                        >
                          {{ site.description }}
                        </p>
                      </div>
                    </div>

                    <!-- 右侧 stats：4 列等宽 mono 数字（评分/点赞/收藏/浏览），完全取代 URL -->
                    <dl
                      class="grid grid-cols-4 gap-x-3 gap-y-1 border-t border-zinc-100 pt-3 sm:gap-x-5 sm:border-0 sm:pt-0 dark:border-zinc-900/80"
                    >
                      <div class="flex flex-col items-end gap-0.5">
                        <dt
                          class="font-mono text-[9.5px] uppercase tracking-[0.16em] text-zinc-400 dark:text-zinc-600"
                        >
                          Score
                        </dt>
                        <dd
                          class="inline-flex items-center gap-1 font-mono text-[13px] font-semibold tabular-nums text-zinc-900 dark:text-zinc-100"
                        >
                          <Star
                            class="size-3.5 fill-amber-400 text-amber-400"
                            :stroke-width="1.5"
                          />
                          {{ formatScore(site.score) }}
                        </dd>
                      </div>
                      <div class="flex flex-col items-end gap-0.5">
                        <dt
                          class="font-mono text-[9.5px] uppercase tracking-[0.16em] text-zinc-400 dark:text-zinc-600"
                        >
                          Likes
                        </dt>
                        <dd
                          class="inline-flex items-center gap-1 font-mono text-[13px] font-medium tabular-nums text-zinc-700 dark:text-zinc-300"
                        >
                          <img
                            :src="websiteLikeIcon"
                            alt=""
                            aria-hidden="true"
                            class="size-3.5 opacity-80"
                          />
                          {{ formatCount(site.likeCount) }}
                        </dd>
                      </div>
                      <div class="flex flex-col items-end gap-0.5">
                        <dt
                          class="font-mono text-[9.5px] uppercase tracking-[0.16em] text-zinc-400 dark:text-zinc-600"
                        >
                          Saves
                        </dt>
                        <dd
                          class="inline-flex items-center gap-1 font-mono text-[13px] font-medium tabular-nums text-zinc-700 dark:text-zinc-300"
                        >
                          <img
                            :src="websiteCollectionIcon"
                            alt=""
                            aria-hidden="true"
                            class="size-3.5 opacity-80"
                          />
                          {{ formatCount(site.collectCount) }}
                        </dd>
                      </div>
                      <div class="flex flex-col items-end gap-0.5">
                        <dt
                          class="font-mono text-[9.5px] uppercase tracking-[0.16em] text-zinc-400 dark:text-zinc-600"
                        >
                          Views
                        </dt>
                        <dd
                          class="inline-flex items-center gap-1 font-mono text-[13px] font-medium tabular-nums text-zinc-700 dark:text-zinc-300"
                        >
                          <img
                            :src="websiteClickIcon"
                            alt=""
                            aria-hidden="true"
                            class="size-3.5 opacity-80"
                          />
                          {{ formatCount(site.clickCount) }}
                        </dd>
                      </div>
                    </dl>

                    <!-- 末端：极简外链箭头 -->
                    <span
                      class="hidden shrink-0 items-center justify-center self-stretch text-zinc-300 transition-all duration-200 group-hover:text-amber-500 sm:flex dark:text-zinc-700 dark:group-hover:text-amber-400"
                    >
                      <ArrowUpRight
                        class="size-4 transition-transform duration-200 group-hover:translate-x-0.5 group-hover:-translate-y-0.5"
                        :stroke-width="2"
                      />
                    </span>

                    <!-- 移动端外链小图标固定在右上 -->
                    <ExternalLink
                      class="absolute right-3 top-4 size-3.5 text-zinc-300 transition-colors group-hover:text-amber-500 sm:hidden dark:text-zinc-700"
                      :stroke-width="2"
                    />
                  </a>
                </li>
              </ul>

              <!-- 空态：编辑式无卡片 -->
              <div v-else class="flex flex-col items-center justify-center gap-4 py-16 text-center">
                <img
                  :src="folderIcon"
                  alt=""
                  aria-hidden="true"
                  class="size-12 opacity-30 grayscale"
                />
                <p
                  class="font-mono text-[10.5px] uppercase tracking-[0.22em] text-zinc-500 dark:text-zinc-500"
                >
                  Empty · No Sources
                </p>
              </div>

              <!-- 分页：极简 mono pager -->
              <nav
                v-if="showPagination"
                class="mt-6 flex items-center justify-between border-t border-zinc-200 pt-5 dark:border-zinc-900"
                aria-label="分页"
              >
                <button
                  type="button"
                  class="group inline-flex cursor-pointer items-center gap-1.5 font-mono text-[11px] uppercase tracking-[0.2em] text-zinc-500 transition-colors hover:text-amber-600 active:scale-[0.97] disabled:cursor-not-allowed disabled:opacity-30 disabled:hover:text-zinc-500 dark:text-zinc-400 dark:hover:text-amber-400 dark:disabled:hover:text-zinc-400"
                  :disabled="pageNum <= 1"
                  @click="handlePageChange(pageNum - 1)"
                >
                  <ChevronLeft
                    class="size-3.5 transition-transform duration-200 group-hover:-translate-x-0.5 group-disabled:transition-none"
                    :stroke-width="2"
                  />
                  Prev
                </button>
                <span
                  class="font-mono text-[11px] uppercase tracking-[0.22em] text-zinc-500 dark:text-zinc-500"
                >
                  <span class="tabular-nums text-zinc-900 dark:text-zinc-100">
                    {{ String(pageNum).padStart(2, '0') }}
                  </span>
                  <span class="mx-1 text-zinc-300 dark:text-zinc-700">/</span>
                  <span class="tabular-nums">
                    {{ String(totalPages).padStart(2, '0') }}
                  </span>
                </span>
                <button
                  type="button"
                  class="group inline-flex cursor-pointer items-center gap-1.5 font-mono text-[11px] uppercase tracking-[0.2em] text-zinc-500 transition-colors hover:text-amber-600 active:scale-[0.97] disabled:cursor-not-allowed disabled:opacity-30 disabled:hover:text-zinc-500 dark:text-zinc-400 dark:hover:text-amber-400 dark:disabled:hover:text-zinc-400"
                  :disabled="pageNum >= totalPages"
                  @click="handlePageChange(pageNum + 1)"
                >
                  Next
                  <ChevronRight
                    class="size-3.5 transition-transform duration-200 group-hover:translate-x-0.5 group-disabled:transition-none"
                    :stroke-width="2"
                  />
                </button>
              </nav>
            </section>
          </div>
        </Transition>
      </main>
    </div>
  </div>
</template>

<style scoped>
.cf-fade-in {
  animation: cfFadeInUp 380ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes cfFadeInUp {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* hero 主标题：极轻微 letter-spacing tighten（编辑式） */
.cf-hero-title {
  letter-spacing: -0.025em;
}

/* 选中夹标题旁的 folder.png：去除"塑料光泽"，仅做无渲染滤镜的尺寸放大 */
.cf-header-folder {
  filter: drop-shadow(0 1px 0 rgb(0 0 0 / 0.04));
}

.cf-subfolder {
  animation: cfFadeInUp 280ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.cf-card-item {
  animation: cfFadeInUp 360ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.cf-chip {
  animation: cfFadeInUp 280ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

/*
 * 内容区 out-in 过渡：修复切换收藏夹/翻页时右侧出现旧数据一闪的根因。
 * - enter-active 略慢一点，避免和 li 级 cfFadeInUp cascade 叠加显突兀
 * - leave 较快，让 skeleton 或下一内容尽快入场
 * - mode="out-in" 保证上一内容完全退出后新内容才进入，彻底无重叠
 */
.cf-content-enter-active {
  transition:
    opacity 220ms cubic-bezier(0.22, 1, 0.36, 1),
    transform 260ms cubic-bezier(0.22, 1, 0.36, 1);
}
.cf-content-leave-active {
  transition:
    opacity 140ms ease-in,
    transform 160ms ease-in;
}
.cf-content-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.cf-content-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* skeleton：低饱和 shimmer，不抢视觉 */
.cf-skeleton > * {
  position: relative;
  overflow: hidden;
  border-radius: 2px;
}
.cf-skeleton > *::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.5) 50%,
    transparent 100%
  );
  animation: cfShimmer 1.6s ease-in-out infinite;
}
:global(html.dark) .cf-skeleton > *::after {
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.04) 50%,
    transparent 100%
  );
}
@keyframes cfShimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

/* 移动端水平滚动条样式 */
.cf-public-collection .overflow-x-auto::-webkit-scrollbar {
  height: 3px;
}
.cf-public-collection .overflow-x-auto::-webkit-scrollbar-thumb {
  background: rgba(245, 158, 11, 0.28);
  border-radius: 0;
}

@media (prefers-reduced-motion: reduce) {
  .cf-fade-in,
  .cf-card-item,
  .cf-subfolder,
  .cf-chip,
  .cf-content-enter-active,
  .cf-content-leave-active,
  .cf-skeleton > *::after {
    animation: none !important;
    transition: none !important;
  }
}
</style>
