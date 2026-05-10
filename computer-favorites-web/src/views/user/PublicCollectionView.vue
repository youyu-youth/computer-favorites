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
 */

import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Folder, FolderOpen, ExternalLink, Loader2, Lock, ArrowLeft } from 'lucide-vue-next'
import UButton from '@/components/ui-adapter/UButton.vue'
import FolderTreeRow from '@/components/user/profile/FolderTreeRow.vue'
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
  <div class="mx-auto w-full max-w-6xl px-4 py-6">
    <!-- 顶部返回 -->
    <button
      type="button"
      class="mb-4 inline-flex cursor-pointer items-center gap-1 font-mono text-[12px] text-gray-500 transition hover:text-amber-600 dark:text-gray-400 dark:hover:text-amber-400"
      @click="goBackToProfile"
    >
      <ArrowLeft class="size-3.5" :stroke-width="2" />
      返回 {{ username }} 的公开主页
    </button>

    <!-- Hero -->
    <header
      class="mb-5 flex items-start gap-4 rounded-xl border border-black/5 bg-white p-5 dark:border-white/[0.06] dark:bg-neutral-900"
    >
      <div
        class="size-16 shrink-0 overflow-hidden rounded-full border border-black/5 dark:border-white/[0.06]"
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
          class="flex h-full w-full items-center justify-center bg-amber-50 font-mono text-lg font-semibold text-amber-600 dark:bg-amber-950/30 dark:text-amber-400"
        >
          {{ (username[0] || '?').toUpperCase() }}
        </div>
      </div>
      <div class="flex min-w-0 flex-1 flex-col gap-1">
        <h1 class="truncate text-xl font-semibold text-gray-900 dark:text-white">
          {{ profile?.user?.nickname || username }} 的公开收藏夹
        </h1>
        <p class="truncate text-[12.5px] text-gray-500 dark:text-gray-400">
          @{{ username }}
          <span
            v-if="isOwn"
            class="ml-2 rounded bg-amber-100 px-1.5 py-0.5 font-mono text-[10.5px] text-amber-700 dark:bg-amber-900/30 dark:text-amber-300"
          >
            本人
          </span>
        </p>
      </div>
    </header>

    <!-- 隐私拦截态 -->
    <div
      v-if="isNotFound"
      class="rounded-lg border border-black/5 bg-white p-8 text-center dark:border-white/[0.06] dark:bg-neutral-900"
    >
      <Lock class="mx-auto mb-2 size-8 text-gray-400" :stroke-width="1.5" />
      <p class="font-mono text-[13px] text-gray-500 dark:text-gray-400">用户不存在或已停用</p>
    </div>
    <div
      v-else-if="isLoginRequired"
      class="rounded-lg border border-black/5 bg-white p-8 text-center dark:border-white/[0.06] dark:bg-neutral-900"
    >
      <Lock class="mx-auto mb-2 size-8 text-gray-400" :stroke-width="1.5" />
      <p class="font-mono text-[13px] text-gray-500 dark:text-gray-400">需要登录后查看</p>
    </div>
    <div
      v-else-if="isPrivate || (!showCollections && !isOwn)"
      class="rounded-lg border border-black/5 bg-white p-8 text-center dark:border-white/[0.06] dark:bg-neutral-900"
    >
      <Lock class="mx-auto mb-2 size-8 text-gray-400" :stroke-width="1.5" />
      <p class="font-mono text-[13px] text-gray-500 dark:text-gray-400">该用户的收藏夹不公开</p>
    </div>

    <!-- 主体：左树 + 右内容 -->
    <div v-else class="grid grid-cols-1 gap-4 lg:grid-cols-[300px_1fr]">
      <!-- 左侧树 -->
      <aside
        class="rounded-xl border border-black/5 bg-white p-3 dark:border-white/[0.06] dark:bg-neutral-900"
      >
        <h2
          class="mb-2 px-1 font-mono text-[11px] uppercase tracking-wider text-gray-400 dark:text-gray-500"
        >
          全部公开收藏夹
        </h2>

        <div v-if="treeLoading" class="flex h-32 items-center justify-center text-gray-400">
          <Loader2 class="size-4 animate-spin" :stroke-width="2" />
        </div>
        <div
          v-else-if="treeErrorMsg"
          class="rounded-md bg-red-50 p-2 font-mono text-[11.5px] text-red-600 dark:bg-red-950/30 dark:text-red-400"
        >
          {{ treeErrorMsg }}
        </div>
        <div
          v-else-if="!treeData?.roots.length"
          class="px-1 py-3 font-mono text-[12px] text-gray-400"
        >
          暂无公开收藏夹
        </div>
        <ul v-else class="space-y-0.5">
          <li v-for="node in treeData.roots" :key="node.id">
            <FolderTreeRow
              :node="node"
              :selected-id="selectedId"
              :expanded-ids="expandedIds"
              @toggle="toggleExpand"
              @select="handleSelect"
            />
          </li>
        </ul>
      </aside>

      <!-- 右侧内容 -->
      <main
        class="min-h-[400px] rounded-xl border border-black/5 bg-white p-4 dark:border-white/[0.06] dark:bg-neutral-900"
      >
        <header
          class="mb-3 flex items-center gap-2 border-b border-black/5 pb-3 dark:border-white/[0.06]"
        >
          <FolderOpen class="size-4 text-amber-500" :stroke-width="2" />
          <h2 class="text-[14px] font-semibold text-gray-900 dark:text-white">
            {{ selectedNode?.name || '请选择收藏夹' }}
          </h2>
          <span v-if="selectedNode" class="ml-1 font-mono text-[10.5px] text-gray-400">
            · {{ selectedNode.websiteCount }} 个网站
          </span>
        </header>

        <div v-if="childrenLoading" class="flex h-40 items-center justify-center text-gray-400">
          <Loader2 class="size-5 animate-spin" :stroke-width="2" />
        </div>
        <div
          v-else-if="childrenErrorMsg"
          class="rounded-md border border-red-200 bg-red-50 p-3 font-mono text-[12.5px] text-red-600 dark:border-red-900/40 dark:bg-red-950/30 dark:text-red-400"
        >
          {{ childrenErrorMsg }}
        </div>
        <template v-else-if="childrenData">
          <!-- 子文件夹 -->
          <section v-if="childrenData.subFolders.length" class="mb-4 space-y-2">
            <h3
              class="font-mono text-[11px] uppercase tracking-wider text-gray-400 dark:text-gray-500"
            >
              子收藏夹
            </h3>
            <div class="grid grid-cols-2 gap-2 sm:grid-cols-3">
              <button
                v-for="sf in childrenData.subFolders"
                :key="sf.id"
                type="button"
                class="group flex cursor-pointer items-center gap-2 rounded-lg border border-black/5 p-2.5 text-left transition hover:border-amber-300 hover:bg-amber-50/60 dark:border-white/[0.06] dark:hover:border-amber-500/40 dark:hover:bg-amber-950/20"
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
                <Folder class="size-4 shrink-0 text-amber-500" :stroke-width="2" />
                <div class="flex min-w-0 flex-1 flex-col">
                  <span class="truncate text-[12.5px] font-medium text-gray-800 dark:text-gray-100">
                    {{ sf.name }}
                  </span>
                  <span class="font-mono text-[10.5px] text-gray-400">
                    {{ sf.websiteCount }} 个
                  </span>
                </div>
              </button>
            </div>
          </section>

          <!-- 网站列表 -->
          <section class="space-y-2">
            <h3
              class="font-mono text-[11px] uppercase tracking-wider text-gray-400 dark:text-gray-500"
            >
              网站 · {{ childrenData.websites.total }} 个
            </h3>
            <ul
              v-if="childrenData.websites.list.length"
              class="grid grid-cols-1 gap-2 sm:grid-cols-2"
            >
              <li v-for="site in childrenData.websites.list" :key="site.id">
                <a
                  :href="site.url"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="group flex h-full cursor-pointer items-start gap-2.5 rounded-lg border border-black/5 p-2.5 transition hover:border-amber-300 hover:bg-amber-50/60 dark:border-white/[0.06] dark:hover:border-amber-500/40 dark:hover:bg-amber-950/20"
                >
                  <img
                    :src="site.cover || websitePlaceholder(site.title)"
                    :alt="site.title"
                    class="size-9 shrink-0 rounded border border-black/5 object-cover dark:border-white/[0.06]"
                    referrerpolicy="no-referrer"
                    loading="lazy"
                  />
                  <div class="flex min-w-0 flex-1 flex-col gap-0.5">
                    <div class="flex items-center gap-1.5">
                      <span
                        class="truncate text-[13px] font-medium text-gray-800 dark:text-gray-100"
                      >
                        {{ site.title }}
                      </span>
                      <ExternalLink
                        class="size-3 shrink-0 text-gray-400 transition group-hover:text-amber-500"
                        :stroke-width="2"
                      />
                    </div>
                    <p
                      v-if="site.description"
                      class="line-clamp-2 text-[11.5px] leading-5 text-gray-500 dark:text-gray-400"
                    >
                      {{ site.description }}
                    </p>
                    <div
                      class="flex flex-wrap items-center gap-x-2.5 gap-y-1 font-mono text-[10.5px] text-gray-400"
                    >
                      <span v-if="site.categoryName" class="text-amber-600 dark:text-amber-400">
                        {{ site.categoryName }}
                      </span>
                      <span>★ {{ formatScore(site.score) }}</span>
                      <span>♥ {{ site.likeCount }}</span>
                      <span>☆ {{ site.collectCount }}</span>
                    </div>
                  </div>
                </a>
              </li>
            </ul>
            <div
              v-else
              class="flex h-24 items-center justify-center font-mono text-[12.5px] text-gray-400"
            >
              暂无网站
            </div>

            <!-- 分页 -->
            <div
              v-if="showPagination"
              class="mt-3 flex items-center justify-end gap-2 border-t border-black/5 pt-3 dark:border-white/[0.06]"
            >
              <UButton
                variant="ghost"
                class="h-7 px-2 text-[12px]"
                :disabled="pageNum <= 1"
                @click="handlePageChange(pageNum - 1)"
              >
                上一页
              </UButton>
              <span class="font-mono text-[11.5px] text-gray-500 dark:text-gray-400">
                {{ pageNum }} / {{ totalPages }}
              </span>
              <UButton
                variant="ghost"
                class="h-7 px-2 text-[12px]"
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
