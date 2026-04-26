<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Globe,
  Clock,
  CheckCircle,
  XCircle,
  Menu,
  Plus,
  Search,
} from 'lucide-vue-next'
import SubmissionSidebar from '@/components/user/submission/SubmissionSidebar.vue'
import SubmissionStatsBar from '@/components/user/submission/SubmissionStatsBar.vue'
import SubmissionCard from '@/components/user/submission/SubmissionCard.vue'
import SubmissionViewToggle from '@/components/user/submission/SubmissionViewToggle.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import {
  cancelMyWebsiteSubmission,
  getMyWebsiteSubmissionPage,
} from '@/api/user-website-submission'
import { useToast } from '@/composables/useToast'
import type {
  UserWebsiteSubmissionAuditStatus,
  UserWebsiteSubmissionListItem,
} from '@/types/user-website-submission'

defineOptions({ name: 'WebsitesUploadView' })

const { add: showToast } = useToast()
const router = useRouter()

type StatusKey = -1 | UserWebsiteSubmissionAuditStatus

interface NavItem {
  id: StatusKey
  name: string
  count: number
  icon: typeof Globe
  active: boolean
}

interface CategoryOption {
  id: number
  name: string
  count: number
}

const isLoading = ref(false)
const loadError = ref('')
const searchQuery = ref('')
const keywordForQuery = ref('')
const selectedStatus = ref<StatusKey>(-1)
const selectedCategoryId = ref(0)
const records = ref<UserWebsiteSubmissionListItem[]>([])
const currentPage = ref(1)
const pageSize = ref(12)
const totalPages = ref(1)
const total = ref(0)
const viewMode = ref<'card' | 'list'>('card')
const mobileSidebarOpen = ref(false)
const starredIds = ref<Set<number>>(new Set())
const cancelConfirmOpen = ref(false)
const cancelCandidate = ref<UserWebsiteSubmissionListItem | null>(null)
const cancelLoading = ref(false)

let searchDebounceTimer: number | undefined
let latestRequestId = 0

const statusNavItems = computed<NavItem[]>(() => {
  const items: { id: StatusKey; name: string; icon: typeof Globe }[] = [
    { id: -1, name: '全部网站', icon: Globe },
    { id: 0, name: '待审核', icon: Clock },
    { id: 1, name: '已通过', icon: CheckCircle },
    { id: 2, name: '已拒绝', icon: XCircle },
  ]
  return items.map((item) => ({
    ...item,
    count:
      item.id === -1
        ? total.value
        : records.value.filter((r) => r.auditStatus === item.id).length,
    active: selectedStatus.value === item.id,
  }))
})

const categoryOptions = computed<CategoryOption[]>(() => {
  const categoryMap = new Map<number, { name: string; count: number }>()
  records.value.forEach((record) => {
    if (typeof record.categoryId === 'number' && record.categoryName?.trim()) {
      const existing = categoryMap.get(record.categoryId)
      if (existing) {
        existing.count++
      } else {
        categoryMap.set(record.categoryId, { name: record.categoryName, count: 1 })
      }
    }
  })
  return Array.from(categoryMap, ([id, data]) => ({ id, ...data }))
})

const statsItems = computed(() => [
  {
    id: -1,
    label: '全部网站',
    count: total.value,
    icon: Globe,
    iconBgClass: 'bg-[#06326d]/10',
    iconColorClass: 'text-[#06326d] dark:text-[#4a90d9]',
    active: selectedStatus.value === -1,
  },
  {
    id: 0,
    label: '待审核',
    count: records.value.filter((r) => r.auditStatus === 0).length,
    icon: Clock,
    iconBgClass: 'bg-gray-100/50 dark:bg-white/[0.04]',
    iconColorClass: 'text-gray-500 dark:text-gray-400',
    active: selectedStatus.value === 0,
  },
  {
    id: 1,
    label: '已通过',
    count: records.value.filter((r) => r.auditStatus === 1).length,
    icon: CheckCircle,
    iconBgClass: 'bg-green-500/5 dark:bg-green-500/5',
    iconColorClass: 'text-green-500 dark:text-green-400',
    active: selectedStatus.value === 1,
  },
  {
    id: 2,
    label: '已拒绝',
    count: records.value.filter((r) => r.auditStatus === 2).length,
    icon: XCircle,
    iconBgClass: 'bg-red-500/5 dark:bg-red-500/5',
    iconColorClass: 'text-red-500 dark:text-red-400',
    active: selectedStatus.value === 2,
  },
])

const visibleRecords = computed(() => {
  if (selectedCategoryId.value === 0) return records.value
  return records.value.filter(
    (record) => Number(record.categoryId) === selectedCategoryId.value,
  )
})

const resolveErrorMessage = (error: unknown, fallback: string) => {
  if (error instanceof Error && error.message) return error.message
  return fallback
}

const handleNavSelect = (id: number | string) => {
  selectedStatus.value = id as StatusKey
}

const handleCategorySelect = (id: number | string) => {
  selectedCategoryId.value = id as number
}

const handleStatsSelect = (id: number | string) => {
  selectedStatus.value = id as StatusKey
}

const handleStar = (id: number) => {
  const next = new Set(starredIds.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  starredIds.value = next
}

const handleEdit = (item: UserWebsiteSubmissionListItem) => {
  void router.push({
    name: 'uploadWebsite',
    query: { submissionId: String(item.id) },
  })
}

const handleVisit = (item: UserWebsiteSubmissionListItem) => {
  if (!item.url) {
    showToast({ type: 'warning', title: '该投稿暂无可访问链接' })
    return
  }
  window.open(item.url, '_blank', 'noopener,noreferrer')
}

const canCancel = (status: UserWebsiteSubmissionAuditStatus) => {
  return status === 0 || status === 2
}

const openCancelConfirm = (item: UserWebsiteSubmissionListItem) => {
  if (!canCancel(item.auditStatus)) return
  cancelCandidate.value = item
  cancelConfirmOpen.value = true
}

const closeCancelConfirm = () => {
  if (cancelLoading.value) return
  cancelConfirmOpen.value = false
  cancelCandidate.value = null
}

const confirmCancel = async () => {
  if (!cancelCandidate.value) return
  const record = cancelCandidate.value
  if (!canCancel(record.auditStatus)) return
  cancelLoading.value = true
  try {
    await cancelMyWebsiteSubmission(record.id)
    showToast({ type: 'success', title: '投稿已取消' })
    closeCancelConfirm()
    await loadSubmissionData()
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '取消投稿失败，请稍后重试'),
    })
  } finally {
    cancelLoading.value = false
  }
}

const resolveStatusText = (status: UserWebsiteSubmissionAuditStatus) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  return '已拒绝'
}

const toLogoText = (name: string) => {
  const n = name.trim()
  return n ? n.slice(0, 2).toUpperCase() : 'NA'
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

const getStatusBadgeClass = (status: UserWebsiteSubmissionAuditStatus) => {
  switch (status) {
    case 1:
      return 'bg-green-100/70 text-green-600 dark:bg-green-500/10 dark:text-green-400'
    case 0:
      return 'bg-yellow-100/70 text-yellow-600 dark:bg-yellow-500/10 dark:text-yellow-400'
    case 2:
      return 'bg-red-100/70 text-red-600 dark:bg-red-500/10 dark:text-red-400'
    default:
      return 'bg-gray-100/70 text-gray-500 dark:bg-white/5 dark:text-gray-400'
  }
}

const loadSubmissionData = async () => {
  const requestId = ++latestRequestId
  isLoading.value = true
  loadError.value = ''
  try {
    const pageResult = await getMyWebsiteSubmissionPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: keywordForQuery.value || undefined,
      auditStatus: selectedStatus.value === -1 ? undefined : selectedStatus.value,
    })
    if (requestId !== latestRequestId) return
    records.value = pageResult.records || []
    total.value = Number(pageResult.total || 0)
    totalPages.value = Math.max(1, Number(pageResult.totalPages || 1))
    if (currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value
    }
  } catch (error) {
    if (requestId === latestRequestId) {
      records.value = []
      total.value = 0
      totalPages.value = 1
      loadError.value = resolveErrorMessage(error, '投稿列表加载失败，请稍后重试')
    }
  } finally {
    if (requestId === latestRequestId) {
      isLoading.value = false
    }
  }
}

watch(selectedStatus, () => {
  selectedCategoryId.value = 0
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }
  void loadSubmissionData()
})

watch(currentPage, () => {
  void loadSubmissionData()
})

watch(searchQuery, (value) => {
  if (searchDebounceTimer) window.clearTimeout(searchDebounceTimer)
  searchDebounceTimer = window.setTimeout(() => {
    keywordForQuery.value = value.trim()
    if (currentPage.value !== 1) {
      currentPage.value = 1
      return
    }
    void loadSubmissionData()
  }, 300)
})

onMounted(() => {
  void loadSubmissionData()
})

onUnmounted(() => {
  if (searchDebounceTimer) window.clearTimeout(searchDebounceTimer)
})
</script>

<template>
  <div class="flex min-h-screen bg-gray-100 p-3 text-gray-900 transition-colors duration-300 dark:bg-[#020202] dark:text-gray-200 md:p-5 lg:p-6 subpixel-antialiased">
    <SubmissionSidebar
      :main-nav="statusNavItems"
      :categories="categoryOptions"
      :active-nav-id="selectedStatus"
      :active-category-id="selectedCategoryId"
      :mobile-open="mobileSidebarOpen"
      @update:active-nav-id="handleNavSelect"
      @update:active-category-id="handleCategorySelect"
      @update:mobile-open="mobileSidebarOpen = $event"
    />

    <main class="flex min-w-0 flex-1 flex-col overflow-hidden">
      <header class="z-10 flex h-14 shrink-0 items-center justify-between bg-transparent px-3 sm:px-5 lg:px-6 md:h-16">
        <div class="flex flex-1 items-center">
          <button
            class="mr-4 cursor-pointer border-0 bg-transparent p-0 text-gray-500 transition-colors hover:text-gray-800 dark:hover:text-white md:hidden"
            @click="mobileSidebarOpen = true"
          >
            <Menu class="h-6 w-6" />
          </button>
          <div class="hidden w-full max-w-md sm:block">
            <div
              class="flex items-center rounded-lg border-0 bg-white/60 py-2 pl-3 pr-4 backdrop-blur-sm transition-all focus-within:bg-white/80 dark:bg-white/[0.04] dark:focus-within:bg-white/[0.06]"
            >
              <Search class="h-[18px] w-[18px] shrink-0 text-gray-400 dark:text-gray-300" />
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索投稿网站..."
                class="ml-2 flex-1 border-0 bg-transparent text-sm text-gray-800 outline-none placeholder:text-gray-400 dark:text-gray-200 dark:placeholder:text-gray-500"
              />
            </div>
          </div>
        </div>
        <div class="flex items-center space-x-3 sm:space-x-4">
          <SubmissionViewToggle v-model="viewMode" />
          <button
            class="inline-flex cursor-pointer items-center rounded-lg border-0 bg-[#06326d] px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-[#052758]"
            @click="router.push({ name: 'uploadWebsite' })"
          >
            <Plus class="mr-1.5 h-[18px] w-[18px]" />
            <span class="hidden sm:inline">提交新网站</span>
            <span class="sm:hidden">提交</span>
          </button>
        </div>
      </header>

      <div class="flex-1 overflow-y-auto p-3 sm:p-5 lg:p-6">
        <div class="mx-auto max-w-6xl space-y-5">
          <h2 class="text-sm text-gray-600 dark:text-gray-400">
            共 <span class="font-bold text-gray-800 dark:text-gray-200">{{ total }}</span> 个网站
          </h2>

          <SubmissionStatsBar :items="statsItems" @select="handleStatsSelect" />

          <div
            v-if="isLoading"
            class="rounded-xl bg-white/50 px-4 py-3 text-sm text-gray-500 backdrop-blur-sm dark:bg-white/[0.03] dark:text-gray-400"
          >
            正在加载投稿列表...
          </div>

          <div
            v-else-if="loadError"
            class="rounded-xl bg-rose-50/60 px-4 py-4 text-sm text-rose-700 backdrop-blur-sm dark:bg-rose-950/10 dark:text-rose-300"
          >
            <p>{{ loadError }}</p>
            <button
              class="mt-2 cursor-pointer rounded border-0 bg-transparent p-0 text-sm font-medium text-rose-600 transition-colors hover:text-rose-700 dark:text-rose-400"
              @click="loadSubmissionData"
            >
              重试
            </button>
          </div>

          <div
            v-else-if="visibleRecords.length === 0"
            class="rounded-xl bg-white/50 px-4 py-10 text-center text-sm text-gray-500 backdrop-blur-sm dark:bg-white/[0.03] dark:text-gray-400"
          >
            暂无匹配的投稿网站。
          </div>

          <template v-else>
            <div
              v-if="viewMode === 'card'"
              class="grid grid-cols-1 gap-3 sm:gap-4 md:grid-cols-2 lg:grid-cols-3"
            >
              <SubmissionCard
                v-for="item in visibleRecords"
                :key="item.id"
                :item="item"
                :starred="starredIds.has(item.id)"
                @star="handleStar"
                @edit="handleEdit"
                @cancel="openCancelConfirm"
                @visit="handleVisit"
              />
            </div>

            <div v-else class="space-y-2">
              <div
                class="mb-2 hidden gap-4 px-2 py-3 text-[11px] font-medium uppercase tracking-widest text-gray-500 dark:text-gray-500 lg:grid lg:grid-cols-[60px_minmax(0,2.5fr)_minmax(0,1.2fr)_120px_180px]"
              >
                <div class="flex justify-center">Logo</div>
                <div>网站名称</div>
                <div>更新时间</div>
                <div class="text-center">状态</div>
                <div class="pr-6 text-right">操作</div>
              </div>

              <div
                v-for="item in visibleRecords"
                :key="'list-' + item.id"
                class="group overflow-hidden rounded-xl bg-white/40 transition-all duration-300 hover:bg-white/60 dark:bg-white/[0.02] dark:hover:bg-white/[0.04]"
              >
                <div class="grid grid-cols-[auto_1fr_auto] items-center gap-x-4 gap-y-3 p-3.5 lg:grid-cols-[60px_minmax(0,2.5fr)_minmax(0,1.2fr)_120px_180px] lg:gap-4 lg:px-4 lg:py-4">
                  <div class="col-start-1 row-span-2 flex items-center justify-center lg:row-span-1">
                    <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-[#06326d] text-sm font-bold text-white transition-transform duration-300 group-hover:scale-105">
                      {{ toLogoText(item.name) }}
                    </div>
                  </div>

                  <div class="col-start-2 row-start-1 min-w-0 flex flex-col justify-center pl-1">
                    <h3 class="truncate text-sm font-bold text-gray-900 transition-colors group-hover:text-[#06326d] dark:text-gray-100 dark:group-hover:text-[#4a90d9]">
                      {{ item.name }}
                    </h3>
                    <p class="mt-1 flex items-center gap-1.5 truncate text-[10.5px] font-medium text-gray-500 opacity-80 dark:text-gray-400">
                      {{ item.categoryName || '未分类' }}
                    </p>
                  </div>

                  <div class="col-start-2 row-start-2 flex items-center truncate text-[11px] font-mono text-gray-500 lg:col-start-3 lg:row-start-1 dark:text-gray-400">
                    {{ formatTime(item.updateTime) }}
                  </div>

                  <div class="col-start-3 row-start-1 flex justify-end lg:col-start-4 lg:justify-center">
                    <span :class="['rounded px-2 py-0.5 text-xs font-medium', getStatusBadgeClass(item.auditStatus)]">
                      {{ resolveStatusText(item.auditStatus) }}
                    </span>
                  </div>

                  <div class="col-span-3 col-start-1 row-start-3 flex items-center justify-end gap-1.5 pt-3 lg:col-span-1 lg:col-start-5 lg:row-start-1 lg:pt-0 mt-2 lg:mt-0">
                    <button
                      v-if="item.auditStatus === 0"
                      class="inline-flex cursor-pointer items-center gap-1 rounded-md bg-white/80 px-2.5 py-1 text-xs font-medium text-gray-600 transition-colors hover:bg-white hover:text-[#06326d] dark:bg-white/5 dark:text-gray-300 dark:hover:bg-white/10"
                      @click="handleEdit(item)"
                    >
                      编辑
                    </button>
                    <button
                      v-if="canCancel(item.auditStatus)"
                      class="inline-flex cursor-pointer items-center gap-1 rounded-md border-0 bg-[#06326d] px-2.5 py-1 text-xs font-medium text-white transition-colors hover:bg-[#052758]"
                      @click="openCancelConfirm(item)"
                    >
                      取消投稿
                    </button>
                    <button
                      v-else-if="item.auditStatus === 1"
                      class="inline-flex cursor-pointer items-center gap-1 rounded-md bg-white/80 px-2.5 py-1 text-xs font-medium text-gray-600 transition-colors hover:bg-white hover:text-[#06326d] dark:bg-white/5 dark:text-gray-300 dark:hover:bg-white/10"
                      @click="handleVisit(item)"
                    >
                      访问
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex flex-col gap-2 pt-4">
              <p class="text-xs text-gray-500 dark:text-gray-400">共 {{ total }} 条投稿记录</p>
              <AppPagination
                :currentPage="currentPage"
                :totalPages="totalPages"
                @update:currentPage="currentPage = $event"
              />
            </div>
          </template>

      
        </div>
      </div>
    </main>

    <UModal
      :open="cancelConfirmOpen"
      title="确认取消投稿"
      description="取消后该投稿将不再进入审核流程，你可以稍后重新投稿。"
      :ui="{
        overlay: 'bg-black/45 backdrop-blur-[1px] z-[120]',
        content: 'w-[min(92vw,440px)] rounded-lg border border-gray-200 bg-white shadow-[0_16px_36px_rgba(15,23,42,0.28)] dark:border-white/10 dark:bg-[#0a0a0a] dark:shadow-[0_22px_44px_rgba(2,6,23,0.62)] overflow-hidden',
        header: 'border-b border-gray-200 bg-gray-50 px-5 py-4 dark:border-white/10 dark:bg-black',
        title: 'text-base font-semibold text-gray-900 dark:text-gray-100',
        description: 'mt-1 text-sm text-gray-600 dark:text-gray-300',
        body: 'px-5 py-4',
        footer: 'px-5 py-4 border-t border-gray-200 bg-gray-50 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end dark:border-white/10 dark:bg-black',
      }"
      @update:open="(value: boolean) => { if (!value) closeCancelConfirm() }"
    >
      <template #body>
        <p class="break-all text-sm text-gray-700 dark:text-gray-200">
          投稿名称：
          <span class="font-medium text-gray-900 dark:text-gray-100">
            {{ cancelCandidate?.name || '未命名网站' }}
          </span>
        </p>
      </template>

      <template #footer>
        <button
          class="cursor-pointer rounded-lg border border-gray-200 bg-transparent px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-100 dark:border-white/10 dark:text-gray-300 dark:hover:bg-white/5"
          :disabled="cancelLoading"
          @click="closeCancelConfirm"
        >
          再想想
        </button>
        <button
          class="cursor-pointer rounded-lg border-0 bg-red-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-red-600 disabled:opacity-60"
          :disabled="!cancelCandidate || cancelLoading"
          @click="confirmCancel"
        >
          {{ cancelLoading ? '取消中...' : '确认取消' }}
        </button>
      </template>
    </UModal>
  </div>
</template>

<style scoped>
</style>
