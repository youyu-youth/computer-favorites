<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-10
 *
 * 公开主页"查看全部公开收藏夹"页面（user-15）。
 *
 * 路径：/computer/u/:username/collections
 * 数据：getPublicProfile（hero）+ getPublicFolderTree（左侧树）+ getPublicFolderChildren（右侧选中夹）
 * 行为：左侧树折叠/展开/选中、选中后右侧分页（pageSize=12）；
 *      visibility=PRIVATE 或 showCollections=0 由 hero 段落给出空态提示。
 *
 * 视觉重设计要点（2026-05-10 美化）：
 *   - 编辑式排版：左侧树用 folder.png + 选中立柱；右侧内容用大尺寸 folder.png 作为标题装饰。
 *   - 网站卡片改为单列横排，复用 user-website-click/like/collection svg + lucide Star 显示统计。
 *   - 启用错落 fade-in-up 动画；hover 时整卡微抬升 + 琥珀色描边。
 *   - 移动端：树折叠为顶部水平滑动 chip 行；卡片单列堆叠；分页按钮全宽。
 *   - 深浅模式：浅色暖灰底 + 米白卡；深色纯黑底 + #0c0c10 卡，避免塑料化高饱和。
 */

import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ExternalLink, Loader2, Lock, ArrowLeft, Star } from 'lucide-vue-next'
import UButton from '@/components/ui-adapter/UButton.vue'
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
      void loadChildren()
    }
  } catch (err) {
    treeData.value = null
    treeErrorMsg.value = err instanceof Error ? err.message : '加载收藏夹树失败'
  } finally {
    treeLoading.value = false
  }
}

const loadChildren = async () => {
  if (!username.value || selectedId.value === null) return
  childrenLoading.value = true
  childrenErrorMsg.value = ''
  try {
    childrenData.value = await getPublicFolderChildren(username.value, selectedId.value, {
      pageNum: pageNum.value,
      pageSize: PAGE_SIZE,
    })
  } catch (err) {
    childrenData.value = null
    childrenErrorMsg.value = err instanceof Error ? err.message : '加载收藏夹内容失败'
  } finally {
    childrenLoading.value = false
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
  void loadChildren()
}

const handlePageChange = (num: number) => {
  if (num < 1 || num > totalPages.value || num === pageNum.value) return
  pageNum.value = num
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

const websitePlaceholder = (title: string) =>
  `https://api.dicebear.com/7.x/shapes/svg?seed=${encodeURIComponent(title)}&backgroundType=gradientLinear&backgroundColor=fbbf24,f59e0b`

/**
 * 提取 URL 的可读 host（用于网站卡片右侧显示），失败时回退原 URL。
 */
const extractHost = (url: string): string => {
  if (!url) return ''
  try {
    const u = new URL(url)
    return u.host.replace(/^www\./, '')
  } catch {
    return (
      url
        .replace(/^https?:\/\//, '')
        .replace(/^www\./, '')
        .split('/')[0] ?? url
    )
  }
}

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
  <div class="cf-public-collection mx-auto w-full max-w-6xl px-4 py-6 sm:px-6 sm:py-8">
    <!-- 顶部返回 -->
    <button
      type="button"
      class="cf-fade-in group mb-5 inline-flex cursor-pointer items-center gap-1.5 font-mono text-[12px] tracking-wide text-gray-500 transition-colors hover:text-amber-600 dark:text-gray-400 dark:hover:text-amber-400"
      @click="goBackToProfile"
    >
      <ArrowLeft
        class="size-3.5 transition-transform duration-200 group-hover:-translate-x-0.5"
        :stroke-width="2"
      />
      返回 {{ username }} 的公开主页
    </button>

    <!-- Hero -->
    <header
      class="cf-fade-in cf-hero relative mb-6 overflow-hidden rounded-2xl border border-black/[0.06] bg-[rgb(var(--cf-color-surface-card-rgb))] p-5 sm:p-7 dark:border-white/[0.06]"
      style="animation-delay: 40ms"
    >
      <div class="relative z-10 flex items-start gap-4 sm:gap-5">
        <div
          class="cf-hero-avatar size-16 shrink-0 overflow-hidden rounded-full ring-1 ring-black/[0.06] sm:size-20 dark:ring-white/[0.08]"
        >
          <img
            v-if="profile?.user?.avatar"
            :src="profile.user.avatar"
            :alt="profile.user.username"
            class="h-full w-full object-cover"
            referrerpolicy="no-referrer"
          />
          <div
            v-else
            class="flex h-full w-full items-center justify-center bg-amber-50 font-mono text-xl font-semibold text-amber-600 dark:bg-amber-500/[0.08] dark:text-amber-300"
          >
            {{ (username[0] || '?').toUpperCase() }}
          </div>
        </div>
        <div class="flex min-w-0 flex-1 flex-col gap-1.5">
          <div
            class="font-mono text-[10.5px] uppercase tracking-[0.2em] text-amber-600/80 dark:text-amber-400/80"
          >
            Public Collections
          </div>
          <h1
            class="truncate text-xl font-bold text-gray-900 sm:text-2xl dark:text-white"
            style="letter-spacing: -0.01em"
          >
            {{ profile?.user?.nickname || username }}
            <span class="text-gray-500 dark:text-gray-400">的公开收藏夹</span>
          </h1>
          <p
            class="flex flex-wrap items-center gap-2 text-[12.5px] text-gray-500 dark:text-gray-400"
          >
            <span class="font-mono">@{{ username }}</span>
            <span
              v-if="isOwn"
              class="inline-flex items-center rounded-full border border-amber-300/60 bg-amber-100/80 px-2 py-0.5 font-mono text-[10.5px] tracking-wide text-amber-700 dark:border-amber-400/30 dark:bg-amber-500/[0.1] dark:text-amber-300"
            >
              本人视图
            </span>
            <span
              v-if="treeData?.roots.length"
              class="inline-flex items-center gap-1 rounded-full bg-black/[0.03] px-2 py-0.5 font-mono text-[10.5px] text-gray-500 dark:bg-white/[0.05] dark:text-gray-400"
            >
              <img :src="folderIcon" alt="" aria-hidden="true" class="size-3" />
              {{ treeData.roots.length }} 个公开收藏夹
            </span>
          </p>
        </div>
      </div>

      <!-- 装饰：右上角放置一个超大半透明 folder.png 作为版式锚点 -->
      <img
        :src="folderIcon"
        alt=""
        aria-hidden="true"
        class="cf-hero-deco pointer-events-none absolute -right-6 -top-6 size-40 select-none opacity-[0.05] sm:opacity-[0.07] dark:opacity-[0.06]"
      />
    </header>

    <!-- 隐私拦截态 -->
    <div
      v-if="isNotFound"
      class="cf-fade-in rounded-2xl border border-black/[0.06] bg-[rgb(var(--cf-color-surface-card-rgb))] p-10 text-center dark:border-white/[0.06]"
    >
      <Lock class="mx-auto mb-3 size-9 text-gray-400" :stroke-width="1.4" />
      <p class="font-mono text-[13px] text-gray-500 dark:text-gray-400">用户不存在或已停用</p>
    </div>
    <div
      v-else-if="isLoginRequired"
      class="cf-fade-in rounded-2xl border border-black/[0.06] bg-[rgb(var(--cf-color-surface-card-rgb))] p-10 text-center dark:border-white/[0.06]"
    >
      <Lock class="mx-auto mb-3 size-9 text-gray-400" :stroke-width="1.4" />
      <p class="font-mono text-[13px] text-gray-500 dark:text-gray-400">需要登录后查看</p>
    </div>
    <div
      v-else-if="isPrivate || (!showCollections && !isOwn)"
      class="cf-fade-in rounded-2xl border border-black/[0.06] bg-[rgb(var(--cf-color-surface-card-rgb))] p-10 text-center dark:border-white/[0.06]"
    >
      <Lock class="mx-auto mb-3 size-9 text-gray-400" :stroke-width="1.4" />
      <p class="font-mono text-[13px] text-gray-500 dark:text-gray-400">该用户的收藏夹不公开</p>
    </div>

    <!-- 主体：左树 + 右内容 -->
    <div v-else class="grid grid-cols-1 gap-5 lg:grid-cols-[280px_1fr]">
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
            class="cf-chip group inline-flex shrink-0 cursor-pointer items-center gap-2 rounded-full border border-black/[0.08] bg-[rgb(var(--cf-color-surface-card-rgb))] px-3.5 py-2 text-[12.5px] font-medium transition-all duration-200 hover:-translate-y-0.5 hover:border-amber-300 dark:border-white/[0.08] dark:hover:border-amber-400/40"
            :class="
              selectedId === root.id
                ? 'border-amber-300 bg-amber-50 text-amber-800 shadow-[0_4px_14px_-6px_rgba(245,158,11,0.5)] dark:border-amber-400/40 dark:bg-amber-500/[0.08] dark:text-amber-200'
                : 'text-gray-700 dark:text-gray-300'
            "
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
            <span class="truncate max-w-[140px]">{{ root.name }}</span>
            <span
              class="rounded-full bg-black/[0.05] px-1.5 py-0.5 font-mono text-[10.5px] tabular-nums leading-none dark:bg-white/[0.06]"
            >
              {{ root.websiteCount }}
            </span>
          </button>
        </div>
      </div>

      <!-- 左侧树（桌面） -->
      <aside
        class="cf-fade-in hidden self-start rounded-2xl border border-black/[0.06] bg-[rgb(var(--cf-color-surface-card-rgb))] p-3.5 lg:block dark:border-white/[0.06]"
        style="animation-delay: 80ms"
      >
        <div class="mb-3 flex items-center justify-between px-2">
          <h2
            class="font-mono text-[10.5px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500"
          >
            All Folders
          </h2>
          <span class="font-mono text-[10.5px] text-gray-400 dark:text-gray-500">
            {{ treeData?.roots.length ?? 0 }}
          </span>
        </div>

        <div v-if="treeLoading" class="flex h-32 items-center justify-center text-gray-400">
          <Loader2 class="size-5 animate-spin" :stroke-width="2" />
        </div>
        <div
          v-else-if="treeErrorMsg"
          class="rounded-lg border border-red-200/70 bg-red-50/70 p-2.5 font-mono text-[11.5px] text-red-600 dark:border-red-900/40 dark:bg-red-950/30 dark:text-red-400"
        >
          {{ treeErrorMsg }}
        </div>
        <div
          v-else-if="!treeData?.roots.length"
          class="px-2 py-6 text-center font-mono text-[12px] text-gray-400 dark:text-gray-500"
        >
          暂无公开收藏夹
        </div>
        <ul v-else class="space-y-1">
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

      <!-- 右侧内容 -->
      <main
        class="cf-fade-in min-h-[420px] rounded-2xl border border-black/[0.06] bg-[rgb(var(--cf-color-surface-card-rgb))] p-4 sm:p-5 dark:border-white/[0.06]"
        style="animation-delay: 120ms"
      >
        <header
          class="mb-5 flex items-center gap-3.5 border-b border-black/[0.06] pb-4 dark:border-white/[0.06]"
        >
          <span
            class="cf-folder-badge relative flex size-12 shrink-0 items-center justify-center sm:size-14"
          >
            <img
              :src="folderIcon"
              alt=""
              aria-hidden="true"
              class="size-12 sm:size-14 drop-shadow-[0_4px_10px_rgba(245,158,11,0.25)]"
              loading="lazy"
            />
          </span>
          <div class="flex min-w-0 flex-1 flex-col gap-0.5">
            <h2
              class="truncate text-[16px] font-bold text-gray-900 sm:text-[17px] dark:text-white"
              style="letter-spacing: -0.005em"
            >
              {{ selectedNode?.name || '请选择收藏夹' }}
            </h2>
            <p
              class="flex items-center gap-2 font-mono text-[11px] text-gray-500 dark:text-gray-400"
            >
              <span v-if="selectedNode">{{ selectedNode.websiteCount }} 个网站</span>
              <span v-else>从左侧选择一个收藏夹查看内容</span>
              <span v-if="childrenData?.subFolders.length" class="text-gray-400 dark:text-gray-500">
                · {{ childrenData.subFolders.length }} 个子收藏夹
              </span>
            </p>
          </div>
        </header>

        <div v-if="childrenLoading" class="flex h-48 items-center justify-center text-gray-400">
          <Loader2 class="size-6 animate-spin" :stroke-width="2" />
        </div>
        <div
          v-else-if="childrenErrorMsg"
          class="rounded-lg border border-red-200/70 bg-red-50/70 p-3 font-mono text-[12.5px] text-red-600 dark:border-red-900/40 dark:bg-red-950/30 dark:text-red-400"
        >
          {{ childrenErrorMsg }}
        </div>
        <template v-else-if="childrenData">
          <!-- 子文件夹 -->
          <section v-if="childrenData.subFolders.length" class="mb-6 space-y-3">
            <h3
              class="flex items-center gap-2 font-mono text-[10.5px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500"
            >
              <span class="h-px w-6 bg-gradient-to-r from-amber-400/60 to-transparent" />
              子收藏夹
            </h3>
            <div class="grid grid-cols-2 gap-2.5 sm:grid-cols-3 lg:grid-cols-4">
              <button
                v-for="(sf, idx) in childrenData.subFolders"
                :key="sf.id"
                type="button"
                class="cf-subfolder group flex cursor-pointer items-center gap-2.5 rounded-xl border border-black/[0.06] bg-[rgb(var(--cf-color-surface-card-rgb))] p-3 text-left transition-all duration-200 hover:-translate-y-0.5 hover:border-amber-300/80 hover:shadow-[0_8px_20px_-12px_rgba(245,158,11,0.4)] dark:border-white/[0.06] dark:hover:border-amber-400/40 dark:hover:shadow-[0_8px_20px_-12px_rgba(0,0,0,0.6)]"
                :style="{ animationDelay: `${idx * 35}ms` }"
                @click="
                  () => {
                    selectedId = sf.id
                    pageNum = 1
                    if (!expandedIds.has(sf.parentId)) {
                      const next = new Set(expandedIds)
                      next.add(sf.parentId)
                      expandedIds = next
                    }
                    void loadChildren()
                  }
                "
              >
                <img
                  :src="folderIcon"
                  alt=""
                  aria-hidden="true"
                  class="size-8 shrink-0 transition-transform duration-200 group-hover:scale-110 group-hover:rotate-[-4deg]"
                  loading="lazy"
                />
                <div class="flex min-w-0 flex-1 flex-col">
                  <span
                    class="truncate text-[12.5px] font-semibold text-gray-800 dark:text-gray-100"
                  >
                    {{ sf.name }}
                  </span>
                  <span class="font-mono text-[10.5px] text-gray-400 dark:text-gray-500">
                    {{ sf.websiteCount }} 个网站
                  </span>
                </div>
              </button>
            </div>
          </section>

          <!-- 网站列表 -->
          <section class="space-y-3">
            <h3
              class="flex items-center gap-2 font-mono text-[10.5px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500"
            >
              <span class="h-px w-6 bg-gradient-to-r from-amber-400/60 to-transparent" />
              网站
              <span class="text-gray-400 dark:text-gray-500">·</span>
              <span class="tabular-nums">{{ childrenData.websites.total }}</span>
            </h3>

            <transition-group
              v-if="childrenData.websites.list.length"
              tag="ul"
              name="cf-card-list"
              class="flex flex-col gap-2"
            >
              <li
                v-for="(site, idx) in childrenData.websites.list"
                :key="`${selectedId}-${pageNum}-${site.id}`"
                class="cf-card-item"
                :style="{ animationDelay: `${Math.min(idx * 40, 360)}ms` }"
              >
                <a
                  :href="site.url"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="cf-website-card group relative flex flex-col gap-3 overflow-hidden rounded-xl border border-black/[0.06] bg-[rgb(var(--cf-color-surface-card-rgb))] p-3.5 transition-all duration-200 hover:-translate-y-0.5 hover:border-amber-300/80 hover:bg-amber-50/40 hover:shadow-[0_10px_30px_-15px_rgba(245,158,11,0.45)] sm:flex-row sm:items-center sm:gap-4 dark:border-white/[0.06] dark:hover:border-amber-400/40 dark:hover:bg-amber-500/[0.04] dark:hover:shadow-[0_10px_30px_-15px_rgba(0,0,0,0.7)]"
                >
                  <!-- 网站封面/图标 -->
                  <div
                    class="relative flex size-12 shrink-0 items-center justify-center overflow-hidden rounded-xl border border-black/[0.06] bg-gradient-to-br from-amber-50 to-white sm:size-14 dark:border-white/[0.06] dark:from-amber-500/[0.04] dark:to-transparent"
                  >
                    <img
                      :src="site.cover || websitePlaceholder(site.title)"
                      :alt="site.title"
                      class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                      referrerpolicy="no-referrer"
                      loading="lazy"
                    />
                  </div>

                  <!-- 中部：标题 / 描述 / 统计 -->
                  <div class="flex min-w-0 flex-1 flex-col gap-1.5">
                    <div class="flex items-center gap-1.5">
                      <h4
                        class="truncate text-[14px] font-semibold text-gray-900 transition-colors group-hover:text-amber-700 dark:text-gray-100 dark:group-hover:text-amber-300"
                      >
                        {{ site.title }}
                      </h4>
                      <span
                        v-if="site.categoryName"
                        class="hidden shrink-0 rounded-md bg-amber-50 px-1.5 py-0.5 font-mono text-[10px] tracking-wide text-amber-700 sm:inline-flex dark:bg-amber-500/[0.08] dark:text-amber-300"
                      >
                        {{ site.categoryName }}
                      </span>
                    </div>
                    <p
                      v-if="site.description"
                      class="line-clamp-1 text-[12px] leading-5 text-gray-500 dark:text-gray-400"
                    >
                      {{ site.description }}
                    </p>

                    <!-- 统计行：评分 / 点赞 / 收藏 / 浏览 -->
                    <div
                      class="flex flex-wrap items-center gap-x-3.5 gap-y-1 font-mono text-[11px] text-gray-500 dark:text-gray-400"
                    >
                      <span class="inline-flex items-center gap-1">
                        <Star class="size-3.5 fill-amber-400 text-amber-400" :stroke-width="1.5" />
                        <span class="font-semibold tabular-nums text-gray-700 dark:text-gray-200">
                          {{ formatScore(site.score) }}
                        </span>
                      </span>
                      <span class="inline-flex items-center gap-1">
                        <img
                          :src="websiteLikeIcon"
                          alt=""
                          aria-hidden="true"
                          class="size-3.5 opacity-80"
                        />
                        <span class="tabular-nums">{{ site.likeCount }}</span>
                      </span>
                      <span class="inline-flex items-center gap-1">
                        <img
                          :src="websiteCollectionIcon"
                          alt=""
                          aria-hidden="true"
                          class="size-3.5 opacity-80"
                        />
                        <span class="tabular-nums">{{ site.collectCount }}</span>
                      </span>
                      <span class="inline-flex items-center gap-1">
                        <img
                          :src="websiteClickIcon"
                          alt=""
                          aria-hidden="true"
                          class="size-3.5 opacity-80"
                        />
                        <span class="tabular-nums">{{ site.clickCount }}</span>
                      </span>
                    </div>
                  </div>

                  <!-- 右侧：URL host + 外链 -->
                  <div
                    class="hidden shrink-0 items-center gap-2 self-stretch border-l border-black/[0.06] pl-4 sm:flex dark:border-white/[0.06]"
                  >
                    <span
                      class="max-w-[180px] truncate font-mono text-[11.5px] text-gray-500 transition-colors group-hover:text-amber-600 dark:text-gray-400 dark:group-hover:text-amber-400"
                    >
                      {{ extractHost(site.url) }}
                    </span>
                    <span
                      class="flex size-7 items-center justify-center rounded-md border border-black/[0.06] text-gray-400 transition-all duration-200 group-hover:border-amber-300 group-hover:bg-amber-50 group-hover:text-amber-600 dark:border-white/[0.06] dark:group-hover:border-amber-400/40 dark:group-hover:bg-amber-500/[0.08] dark:group-hover:text-amber-300"
                    >
                      <ExternalLink class="size-3.5" :stroke-width="2" />
                    </span>
                  </div>

                  <!-- 移动端：URL 外链行（行底） -->
                  <div
                    class="flex items-center justify-between gap-2 border-t border-black/[0.06] pt-2 sm:hidden dark:border-white/[0.06]"
                  >
                    <span
                      v-if="site.categoryName"
                      class="rounded-md bg-amber-50 px-1.5 py-0.5 font-mono text-[10px] text-amber-700 dark:bg-amber-500/[0.08] dark:text-amber-300"
                    >
                      {{ site.categoryName }}
                    </span>
                    <span class="ml-auto inline-flex items-center gap-1.5">
                      <span
                        class="max-w-[160px] truncate font-mono text-[11px] text-gray-500 dark:text-gray-400"
                      >
                        {{ extractHost(site.url) }}
                      </span>
                      <ExternalLink
                        class="size-3.5 text-gray-400 group-hover:text-amber-500"
                        :stroke-width="2"
                      />
                    </span>
                  </div>

                  <!-- 底部 hover 强调线 -->
                  <span
                    class="pointer-events-none absolute inset-x-0 bottom-0 h-px scale-x-0 bg-gradient-to-r from-transparent via-amber-400/70 to-transparent transition-transform duration-300 group-hover:scale-x-100"
                  />
                </a>
              </li>
            </transition-group>

            <div
              v-else
              class="flex flex-col items-center justify-center gap-3 rounded-xl border border-dashed border-black/[0.08] py-12 dark:border-white/[0.08]"
            >
              <img
                :src="folderIcon"
                alt=""
                aria-hidden="true"
                class="size-14 opacity-40 grayscale"
              />
              <p class="font-mono text-[12.5px] text-gray-400 dark:text-gray-500">暂无网站</p>
            </div>

            <!-- 分页 -->
            <div
              v-if="showPagination"
              class="mt-5 flex flex-col items-stretch gap-2 border-t border-black/[0.06] pt-4 sm:flex-row sm:items-center sm:justify-end sm:gap-3 dark:border-white/[0.06]"
            >
              <UButton
                variant="ghost"
                class="h-8 px-3 text-[12px]"
                :disabled="pageNum <= 1"
                @click="handlePageChange(pageNum - 1)"
              >
                上一页
              </UButton>
              <span
                class="text-center font-mono text-[11.5px] tabular-nums text-gray-500 dark:text-gray-400"
              >
                {{ pageNum }} / {{ totalPages }}
              </span>
              <UButton
                variant="ghost"
                class="h-8 px-3 text-[12px]"
                :disabled="pageNum >= totalPages"
                @click="handlePageChange(pageNum + 1)"
              >
                下一页
              </UButton>
            </div>
          </section>
        </template>
      </main>
    </div>
  </div>
</template>

<style scoped>
.cf-fade-in {
  animation: cfFadeInUp 360ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes cfFadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cf-hero {
  background-image:
    radial-gradient(circle at 100% 0%, rgba(245, 158, 11, 0.06), transparent 55%),
    radial-gradient(circle at 0% 100%, rgba(217, 119, 6, 0.04), transparent 55%);
}

:global(html.dark) .cf-hero {
  background-image:
    radial-gradient(circle at 100% 0%, rgba(245, 158, 11, 0.05), transparent 55%),
    radial-gradient(circle at 0% 100%, rgba(217, 119, 6, 0.04), transparent 55%);
}

.cf-hero-deco {
  filter: saturate(0.9);
  transform: rotate(-12deg);
}

.cf-subfolder {
  animation: cfFadeInUp 320ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.cf-card-item {
  animation: cfFadeInUp 380ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.cf-chip {
  animation: cfFadeInUp 320ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* TransitionGroup move（翻页/切换收藏夹时的位置过渡） */
.cf-card-list-move {
  transition: transform 320ms cubic-bezier(0.22, 1, 0.36, 1);
}
.cf-card-list-leave-active {
  position: absolute;
  transition:
    opacity 200ms ease,
    transform 200ms ease;
}
.cf-card-list-enter-from,
.cf-card-list-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

/* 移动端水平滚动条样式 */
.cf-public-collection .overflow-x-auto::-webkit-scrollbar {
  height: 4px;
}
.cf-public-collection .overflow-x-auto::-webkit-scrollbar-thumb {
  background: rgba(245, 158, 11, 0.25);
  border-radius: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .cf-fade-in,
  .cf-card-item,
  .cf-subfolder,
  .cf-chip,
  .cf-card-list-move,
  .cf-card-list-leave-active {
    animation: none !important;
    transition: none !important;
  }
}
</style>
