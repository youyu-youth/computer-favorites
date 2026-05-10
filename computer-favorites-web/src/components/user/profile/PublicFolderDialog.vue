<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-10
 *
 * 公开主页收藏夹对话框（user-15）。
 *
 * 入口：ProfileSidebarCard 顶层公开收藏夹列表点击触发，传入起始 folder。
 * 行为：内部下钻（点击子文件夹切换内容、面包屑回退），网站分页 pageSize=12。
 * 隐私：所有数据走后端公开端点，不含当前用户操作态。
 */

import { computed, ref, watch } from 'vue'
import { Folder, FolderOpen, ExternalLink, ChevronLeft, Loader2 } from 'lucide-vue-next'
import UModal from '@/components/ui-adapter/UModal.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import {
  getPublicFolderChildren,
  type PublicFolderChildrenResponse,
  type PublicFolderItem,
  type PublicFolderChild,
  type PublicFolderWebsite,
} from '@/api/user-profile-public'

const props = defineProps<{
  open: boolean
  username: string
  /** 起始顶层文件夹（来自 ProfileSidebarCard） */
  folder: PublicFolderItem | null
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'view-all'): void
}>()

const PAGE_SIZE = 12

interface BreadcrumbNode {
  id: number
  name: string
}

const stack = ref<BreadcrumbNode[]>([])
const pageNum = ref(1)
const data = ref<PublicFolderChildrenResponse | null>(null)
const loading = ref(false)
const errorMsg = ref('')

const currentFolder = computed<BreadcrumbNode | null>(() =>
  stack.value.length > 0 ? stack.value[stack.value.length - 1]! : null,
)
const totalPages = computed(() => {
  const total = data.value?.websites.total ?? 0
  return Math.max(1, Math.ceil(total / PAGE_SIZE))
})
const showPagination = computed(() => (data.value?.websites.total ?? 0) > PAGE_SIZE)

const reset = () => {
  stack.value = []
  pageNum.value = 1
  data.value = null
  errorMsg.value = ''
}

const loadCurrent = async () => {
  if (!currentFolder.value || !props.username) {
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    data.value = await getPublicFolderChildren(props.username, currentFolder.value.id, {
      pageNum: pageNum.value,
      pageSize: PAGE_SIZE,
    })
  } catch (err) {
    data.value = null
    errorMsg.value = err instanceof Error ? err.message : '加载失败'
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.open, props.folder?.id] as const,
  ([open, folderId]) => {
    if (!open || !props.folder || folderId === undefined) {
      reset()
      return
    }
    stack.value = [{ id: props.folder.id, name: props.folder.name }]
    pageNum.value = 1
    void loadCurrent()
  },
  { immediate: true },
)

const handleEnterChild = (child: PublicFolderChild) => {
  stack.value.push({ id: child.id, name: child.name })
  pageNum.value = 1
  void loadCurrent()
}

const handleJumpTo = (index: number) => {
  if (index < 0 || index >= stack.value.length - 1) {
    return
  }
  stack.value = stack.value.slice(0, index + 1)
  pageNum.value = 1
  void loadCurrent()
}

const handleBack = () => {
  if (stack.value.length <= 1) {
    return
  }
  stack.value.pop()
  pageNum.value = 1
  void loadCurrent()
}

const handlePageChange = (num: number) => {
  if (num < 1 || num > totalPages.value || num === pageNum.value) {
    return
  }
  pageNum.value = num
  void loadCurrent()
}

const handleClose = (next: boolean) => {
  if (!next) {
    reset()
  }
  emit('update:open', next)
}

const formatScore = (score: PublicFolderWebsite['score']) =>
  score === null || score === undefined ? '—' : Number(score).toFixed(1)

const websitePlaceholder = (title: string) =>
  `https://api.dicebear.com/7.x/shapes/svg?seed=${encodeURIComponent(title)}&backgroundType=gradientLinear&backgroundColor=fbbf24,f59e0b`
</script>

<template>
  <UModal
    :open="open"
    :title="currentFolder?.name || folder?.name || '公开收藏夹'"
    :ui="{
      content: 'w-[min(94vw,720px)] rounded-xl bg-white shadow-2xl dark:bg-neutral-900',
      header:
        'border-b border-black/5 px-5 py-3.5 text-base font-semibold text-gray-900 dark:border-white/[0.06] dark:text-white',
      title: 'text-base font-semibold tracking-tight',
      body: 'px-5 pb-4 pt-3',
    }"
    @update:open="handleClose"
  >
    <div class="flex flex-col gap-3 max-h-[70vh]">
      <!-- 面包屑 + 返回 -->
      <div class="flex items-center gap-2 text-[12.5px] font-mono text-gray-500 dark:text-gray-400">
        <UButton
          v-if="stack.length > 1"
          variant="ghost"
          class="-ml-2 h-7 px-2 text-amber-600 hover:text-amber-700 dark:text-amber-400 dark:hover:text-amber-300"
          @click="handleBack"
        >
          <ChevronLeft class="size-3.5" :stroke-width="2" />
          返回
        </UButton>
        <ol class="flex flex-wrap items-center gap-1 truncate">
          <li v-for="(node, idx) in stack" :key="node.id" class="flex items-center gap-1 truncate">
            <button
              v-if="idx < stack.length - 1"
              type="button"
              class="cursor-pointer truncate transition-colors hover:text-amber-600 dark:hover:text-amber-400"
              @click="handleJumpTo(idx)"
            >
              {{ node.name }}
            </button>
            <span v-else class="truncate font-semibold text-gray-700 dark:text-gray-200">
              {{ node.name }}
            </span>
            <span v-if="idx < stack.length - 1" class="text-gray-300 dark:text-gray-600">/</span>
          </li>
        </ol>
      </div>

      <!-- loading / error -->
      <div
        v-if="loading"
        class="flex h-40 items-center justify-center text-gray-400 dark:text-gray-500"
      >
        <Loader2 class="size-5 animate-spin" :stroke-width="2" />
      </div>
      <div
        v-else-if="errorMsg"
        class="rounded-md border border-red-200 bg-red-50 p-3 font-mono text-[12.5px] text-red-600 dark:border-red-900/40 dark:bg-red-950/30 dark:text-red-400"
      >
        {{ errorMsg }}
      </div>

      <template v-else-if="data">
        <!-- 子文件夹 -->
        <section v-if="data.subFolders.length" class="space-y-2">
          <h4
            class="font-mono text-[11px] uppercase tracking-wider text-gray-400 dark:text-gray-500"
          >
            子收藏夹
          </h4>
          <div class="grid grid-cols-2 gap-2 sm:grid-cols-3">
            <button
              v-for="sf in data.subFolders"
              :key="sf.id"
              type="button"
              class="group flex cursor-pointer items-center gap-2 rounded-lg border border-black/5 bg-white p-2.5 text-left transition hover:border-amber-300 hover:bg-amber-50/60 dark:border-white/[0.06] dark:bg-white/[0.02] dark:hover:border-amber-500/40 dark:hover:bg-amber-950/20"
              @click="handleEnterChild(sf)"
            >
              <Folder
                class="size-4 shrink-0 text-amber-500 transition group-hover:hidden"
                :stroke-width="2"
              />
              <FolderOpen
                class="hidden size-4 shrink-0 text-amber-500 transition group-hover:block"
                :stroke-width="2"
              />
              <div class="flex min-w-0 flex-1 flex-col">
                <span class="truncate text-[12.5px] font-medium text-gray-800 dark:text-gray-100">
                  {{ sf.name }}
                </span>
                <span class="font-mono text-[10.5px] text-gray-400 dark:text-gray-500">
                  {{ sf.websiteCount }} 个
                </span>
              </div>
            </button>
          </div>
        </section>

        <!-- 直属网站 -->
        <section class="space-y-2">
          <h4
            class="font-mono text-[11px] uppercase tracking-wider text-gray-400 dark:text-gray-500"
          >
            网站 · {{ data.websites.total }} 个
          </h4>
          <ul
            v-if="data.websites.list.length"
            class="grid grid-cols-1 gap-2 overflow-y-auto pr-1 sm:grid-cols-2"
          >
            <li v-for="site in data.websites.list" :key="site.id">
              <a
                :href="site.url"
                target="_blank"
                rel="noopener noreferrer"
                class="group flex h-full cursor-pointer items-start gap-2.5 rounded-lg border border-black/5 bg-white p-2.5 transition hover:border-amber-300 hover:bg-amber-50/60 dark:border-white/[0.06] dark:bg-white/[0.02] dark:hover:border-amber-500/40 dark:hover:bg-amber-950/20"
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
                    <span class="truncate text-[13px] font-medium text-gray-800 dark:text-gray-100">
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
                    class="flex flex-wrap items-center gap-x-2.5 gap-y-1 font-mono text-[10.5px] text-gray-400 dark:text-gray-500"
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
            class="flex h-20 items-center justify-center font-mono text-[12.5px] text-gray-400 dark:text-gray-500"
          >
            暂无网站
          </div>
        </section>
      </template>
    </div>

    <template #footer>
      <div class="flex w-full items-center justify-between gap-2">
        <button
          v-if="folder"
          type="button"
          class="cursor-pointer font-mono text-[12px] text-amber-600 transition hover:text-amber-700 dark:text-amber-400 dark:hover:text-amber-300"
          @click="emit('view-all')"
        >
          查看全部公开收藏夹 →
        </button>
        <span v-else class="flex-1" />
        <div v-if="showPagination" class="flex items-center gap-1.5">
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
      </div>
    </template>
  </UModal>
</template>
