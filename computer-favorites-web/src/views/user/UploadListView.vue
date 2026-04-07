<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import UserButton from '@/components/user/UserButton.vue'
import UserSearchInput from '@/components/user/UserSearchInput.vue'
import UserSelect from '@/components/user/UserSelect.vue'
import UserBadge from '@/components/user/UserBadge.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import { useRouter } from 'vue-router'
import {
  cancelMyWebsiteSubmission,
  getMyWebsiteSubmissionPage,
} from '@/api/user-website-submission'
import { useToast } from '@/composables/useToast'
import type {
  UserWebsiteSubmissionAuditStatus,
  UserWebsiteSubmissionListItem,
} from '@/types/user-website-submission'

type StatusOption = {
  id: -1 | UserWebsiteSubmissionAuditStatus
  name: string
}

type CategoryOption = {
  id: number
  name: string
}

const { add: showToast } = useToast()
const router = useRouter()

const statusOptions: StatusOption[] = [
  { id: -1, name: '全部状态' },
  { id: 0, name: '待审核' },
  { id: 1, name: '已通过' },
  { id: 2, name: '已拒绝' },
]

const searchQuery = ref('')
const keywordForQuery = ref('')
const selectedStatus = ref<StatusOption['id']>(-1)
const selectedCategoryId = ref(0)
const isLoading = ref(false)
const loadError = ref('')
const records = ref<UserWebsiteSubmissionListItem[]>([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const totalPages = ref(1)
const expandedRecordIds = ref<number[]>([])
const cancelingRecordIds = ref<number[]>([])
const cancelConfirmOpen = ref(false)
const cancelCandidate = ref<UserWebsiteSubmissionListItem | null>(null)

let searchDebounceTimer: number | undefined
let latestRequestId = 0

const categoryOptions = computed<CategoryOption[]>(() => {
  const categoryMap = new Map<number, string>()
  records.value.forEach((record) => {
    if (typeof record.categoryId === 'number' && record.categoryName?.trim()) {
      categoryMap.set(record.categoryId, record.categoryName)
    }
  })
  return [
    { id: 0, name: '全部类别' },
    ...Array.from(categoryMap, ([id, name]) => ({ id, name })),
  ]
})

const visibleRecords = computed(() => {
  if (selectedCategoryId.value === 0) {
    return records.value
  }
  return records.value.filter((record) => Number(record.categoryId) === selectedCategoryId.value)
})

const resolveErrorMessage = (error: unknown, fallbackMessage: string) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallbackMessage
}

const resolveStatusText = (status: UserWebsiteSubmissionAuditStatus) => {
  if (status === 0) {
    return '待审核'
  }
  if (status === 1) {
    return '已通过'
  }
  return '已拒绝'
}

const getStatusSeverity = (status: UserWebsiteSubmissionAuditStatus) => {
  if (status === 0) {
    return 'warn'
  }
  if (status === 1) {
    return 'success'
  }
  return 'danger'
}

const toLogoText = (name: string) => {
  const normalized = name.trim()
  if (!normalized) {
    return 'NA'
  }
  return normalized.slice(0, 2).toUpperCase()
}

const formatTime = (time?: string) => {
  if (!time) {
    return '-'
  }
  return time.replace('T', ' ')
}

const resolveTagNames = (record: UserWebsiteSubmissionListItem) => {
  if (!Array.isArray(record.tags)) {
    return []
  }
  return record.tags
    .map((tag) => (tag.name || '').trim())
    .filter((tag) => tag.length > 0)
}

const resolveSummary = (record: UserWebsiteSubmissionListItem) => {
  return record.summary?.trim() || '暂无项目描述'
}

const retryLoad = () => {
  void loadSubmissionData()
}

const openWebsite = (record: UserWebsiteSubmissionListItem) => {
  if (!record.url) {
    showToast({
      type: 'warning',
      title: '该投稿暂无可访问链接',
    })
    return
  }
  window.open(record.url, '_blank', 'noopener,noreferrer')
}

const canCancelSubmission = (status: UserWebsiteSubmissionAuditStatus) => {
  return status === 0 || status === 2
}

const canEditSubmission = (status: UserWebsiteSubmissionAuditStatus) => {
  return status === 0
}

const isCanceling = (recordId: number) => {
  return cancelingRecordIds.value.includes(recordId)
}

const isCancelConfirmLoading = computed(() => {
  if (!cancelCandidate.value) {
    return false
  }
  return isCanceling(cancelCandidate.value.id)
})

const handleEditSubmission = (record: UserWebsiteSubmissionListItem) => {
  if (!canEditSubmission(record.auditStatus)) {
    return
  }

  void router.push({
    name: 'uploadWebsite',
    query: {
      submissionId: String(record.id),
    },
  })
}

const openCancelSubmissionConfirm = (record: UserWebsiteSubmissionListItem) => {
  if (!canCancelSubmission(record.auditStatus) || isCanceling(record.id)) {
    return
  }

  cancelCandidate.value = record
  cancelConfirmOpen.value = true
}

const closeCancelSubmissionConfirm = () => {
  if (isCancelConfirmLoading.value) {
    return
  }

  cancelConfirmOpen.value = false
  cancelCandidate.value = null
}

const confirmCancelSubmission = async () => {
  if (!cancelCandidate.value) {
    return
  }

  const record = cancelCandidate.value
  if (!canCancelSubmission(record.auditStatus) || isCanceling(record.id)) {
    return
  }

  cancelingRecordIds.value = [...cancelingRecordIds.value, record.id]
  try {
    await cancelMyWebsiteSubmission(record.id)
    showToast({
      type: 'success',
      title: '投稿已取消',
    })
    closeCancelSubmissionConfirm()
    await loadSubmissionData()
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '取消投稿失败，请稍后重试'),
    })
  } finally {
    cancelingRecordIds.value = cancelingRecordIds.value.filter((id) => id !== record.id)
  }
}

const isExpanded = (recordId: number) => {
  return expandedRecordIds.value.includes(recordId)
}

const toggleExpand = (recordId: number) => {
  if (isExpanded(recordId)) {
    expandedRecordIds.value = expandedRecordIds.value.filter((id) => id !== recordId)
    return
  }
  expandedRecordIds.value = [...expandedRecordIds.value, recordId]
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

    if (requestId !== latestRequestId) {
      return
    }

    records.value = pageResult.records || []
    total.value = Number(pageResult.total || 0)
    totalPages.value = Math.max(1, Number(pageResult.totalPages || 1))
    expandedRecordIds.value = []

    if (currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value
    }
  } catch (error) {
    if (requestId !== latestRequestId) {
      return
    }

    records.value = []
    total.value = 0
    totalPages.value = 1

    const message = resolveErrorMessage(error, '投稿列表加载失败，请稍后重试')
    loadError.value = message
    showToast({
      type: 'error',
      title: message,
    })
  } finally {
    if (requestId === latestRequestId) {
      isLoading.value = false
    }
  }
}

watch(currentPage, () => {
  void loadSubmissionData()
})

watch(selectedStatus, () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }
  void loadSubmissionData()
})

watch(searchQuery, (value) => {
  if (searchDebounceTimer) {
    window.clearTimeout(searchDebounceTimer)
  }

  searchDebounceTimer = window.setTimeout(() => {
    keywordForQuery.value = value.trim()
    if (currentPage.value !== 1) {
      currentPage.value = 1
      return
    }
    void loadSubmissionData()
  }, 300)
})

watch(categoryOptions, (options) => {
  const hasSelection = options.some((option) => option.id === selectedCategoryId.value)
  if (!hasSelection) {
    selectedCategoryId.value = 0
  }
})

onMounted(() => {
  void loadSubmissionData()
})

onUnmounted(() => {
  if (searchDebounceTimer) {
    window.clearTimeout(searchDebounceTimer)
  }
})
</script>

<template>
  <div class="bg-gray-50 text-gray-900 dark:bg-black dark:text-gray-100 transition-colors duration-300 subpixel-antialiased min-h-screen w-full relative">
    <div class="p-4 md:p-6 md:px-10 max-w-[1300px] mx-auto space-y-8">
      <header class="flex flex-col lg:flex-row lg:items-center justify-between gap-6 pb-6 border-b border-gray-200 dark:border-zinc-800/80">
        <div class="pb-1 lg:pb-0">
          <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight">我的投稿网站</h1>
          <p class="text-gray-500 dark:text-zinc-500 mt-2 text-xs md:text-sm font-medium">查看并管理你提交的网站内容。</p>
        </div>

        <div class="flex flex-wrap items-center gap-3 w-full lg:w-auto">
          <UserSearchInput
            v-model="searchQuery"
            placeholder="搜索投稿网站..."
          />

          <UserSelect
            v-model="selectedCategoryId"
            :options="categoryOptions"
            optionLabel="name"
            optionValue="id"
            placeholder="类别"
            minWidth="130px"
          />

          <UserSelect
            v-model="selectedStatus"
            :options="statusOptions"
            optionLabel="name"
            optionValue="id"
            placeholder="状态"
            minWidth="120px"
          />
        </div>
      </header>

      <main class="space-y-0">
        <div
          v-if="isLoading"
          class="rounded-lg border border-gray-200 bg-white px-4 py-3 text-sm text-gray-500 dark:border-dark-border dark:bg-dark-card dark:text-gray-400"
        >
          正在加载投稿列表...
        </div>

        <div
          v-if="loadError && !isLoading"
          class="rounded-lg border border-rose-200 bg-rose-50/60 px-4 py-4 text-sm text-rose-700 dark:border-rose-900/50 dark:bg-rose-950/20 dark:text-rose-300"
        >
          <p>{{ loadError }}</p>
          <UserButton
            label="重试"
            size="small"
            severity="danger"
            text
            class="mt-2"
            @click="retryLoad"
          />
        </div>

        <div
          v-if="!isLoading && !loadError && visibleRecords.length === 0"
          class="rounded-lg border border-dashed border-gray-300 bg-white px-4 py-10 text-center text-sm text-gray-500 dark:border-dark-border dark:bg-dark-card dark:text-gray-400"
        >
          暂无匹配的投稿网站。
        </div>

        <template v-if="!loadError && visibleRecords.length > 0">
          <div class="hidden lg:grid grid-cols-[60px_minmax(0,2.5fr)_minmax(0,1.2fr)_120px_180px] gap-4 px-2 py-3 text-[11px] font-medium text-gray-500 dark:text-zinc-500 uppercase tracking-widest border-b border-gray-200 dark:border-zinc-800/80 mb-2">
            <div class="flex justify-center">Logo</div>
            <div>网站名称</div>
            <div>更新时间</div>
            <div class="text-center">状态</div>
            <div class="text-right pr-6">操作</div>
          </div>

          <div
            v-for="item in visibleRecords"
            :key="item.id"
            class="bg-transparent border-b border-gray-100 dark:border-zinc-800/60 overflow-hidden transition-all duration-300 hover:bg-gray-50 dark:hover:bg-zinc-900/30 group"
          >
            <div class="p-3.5 md:px-2 md:py-4 grid grid-cols-[auto_1fr_auto] lg:grid-cols-[60px_minmax(0,2.5fr)_minmax(0,1.2fr)_120px_180px] items-center gap-x-4 gap-y-3 lg:gap-4 relative">
              <div class="col-start-1 row-start-1 row-span-2 lg:row-span-1 flex items-center justify-center">
                <div class="w-10 h-10 rounded-full bg-gradient-to-br from-gray-100 to-gray-200 dark:from-zinc-800 dark:to-zinc-900 border border-gray-200 dark:border-zinc-700 flex items-center justify-center font-bold text-gray-700 dark:text-zinc-300 shrink-0 text-sm tracking-wider shadow-sm group-hover:scale-105 transition-transform duration-300">
                  {{ toLogoText(item.name) }}
                </div>
              </div>

              <div class="col-start-2 row-start-1 min-w-0 flex flex-col justify-center pl-1">
                <h3 class="font-bold text-sm md:text-[14px] text-gray-900 dark:text-gray-100 truncate group-hover:text-emerald-600 dark:group-hover:text-emerald-400 transition-colors">{{ item.name }}</h3>
                <p class="text-[10.5px] text-gray-500 dark:text-zinc-400 mt-1 truncate flex items-center gap-1.5 opacity-80 font-medium">
                  <i class="pi pi-folder-open text-[9px]"></i>
                  {{ item.categoryName || '未分类' }}
                </p>
              </div>

              <div class="col-start-2 row-start-2 lg:col-start-3 lg:row-start-1 flex items-center text-[11px] text-gray-500 dark:text-zinc-400 font-mono truncate">
                <i class="pi pi-clock text-[10px] mr-1.5 opacity-50"></i>
                {{ formatTime(item.updateTime) }}
              </div>

              <div class="col-start-3 row-start-1 lg:col-start-4 flex justify-end lg:justify-center">
                <UserBadge
                  :severity="getStatusSeverity(item.auditStatus)"
                  :value="resolveStatusText(item.auditStatus)"
                />
              </div>

              <div class="col-span-3 lg:col-span-1 col-start-1 lg:col-start-5 row-start-3 lg:row-start-1 flex justify-end items-center gap-1.5 pt-3 lg:pt-0 lg:border-none mt-2 lg:mt-0">
                <UserButton
                  v-if="canEditSubmission(item.auditStatus)"
                  icon="pi pi-pencil"
                  label="编辑"
                  severity="secondary"
                  text
                  size="small"
                  @click="handleEditSubmission(item)"
                />
                <UserButton
                  v-if="canCancelSubmission(item.auditStatus)"
                  :icon="isCanceling(item.id) ? 'pi pi-spin pi-spinner' : 'pi pi-times'"
                  :label="isCanceling(item.id) ? '取消中' : '取消投稿'"
                  severity="secondary"
                  size="small"
                  :disabled="isCanceling(item.id)"
                  class="!bg-primary-500 !text-white hover:!bg-primary-600 dark:!bg-primary-500 dark:hover:!bg-primary-400 disabled:!bg-primary-400 dark:disabled:!bg-primary-700"
                  @click="openCancelSubmissionConfirm(item)"
                />
                <UserButton
                  v-else
                  icon="pi pi-external-link"
                  label="访问"
                  severity="secondary"
                  text
                  size="small"
                  @click="openWebsite(item)"
                />
                <UserButton
                  icon="pi pi-chevron-down"
                  text
                  rounded
                  :iconClass="['transition-transform duration-300', { 'rotate-180': isExpanded(item.id) }]"
                  :class="['w-6 h-6 p-0 flex items-center justify-center text-gray-400 dark:text-zinc-500 hover:text-gray-900 transition-all ml-1', isExpanded(item.id) ? 'text-gray-900 dark:text-white' : '']"
                  @click="toggleExpand(item.id)"
                />
              </div>
            </div>

            <div
              class="grid transition-all duration-500 ease-[cubic-bezier(0.4,0,0.2,1)]"
              :class="isExpanded(item.id) ? 'grid-rows-[1fr] opacity-100' : 'grid-rows-[0fr] opacity-0'"
            >
              <div class="overflow-hidden">
                <div class="p-4 md:p-6 bg-transparent border-t border-dashed border-gray-200 dark:border-zinc-800/80 mx-2 lg:mx-0 mb-4 mt-2">
                  <div class="flex flex-col lg:flex-row gap-6 lg:gap-10 pl-2 md:pl-16">
                    <div class="flex-1 space-y-5">
                      <div class="space-y-2">
                        <h4 class="text-[10px] font-bold text-gray-400 dark:text-zinc-500 tracking-widest uppercase">项目描述</h4>
                        <p class="text-sm text-gray-700 dark:text-gray-300 leading-relaxed max-w-3xl">
                          {{ resolveSummary(item) }}
                        </p>
                      </div>

                      <div class="space-y-2.5">
                        <h4 class="text-[10px] font-bold text-gray-400 dark:text-zinc-500 tracking-widest uppercase">标签</h4>
                        <div class="flex flex-wrap gap-2">
                          <span
                            v-for="tag in resolveTagNames(item)"
                            :key="tag"
                            class="px-2.5 py-1 rounded bg-gray-100 text-gray-600 dark:bg-zinc-800 dark:text-zinc-300 text-[11px] font-medium tracking-wide"
                          >
                            {{ tag }}
                          </span>
                          <span
                            v-if="resolveTagNames(item).length === 0"
                            class="text-[11px] text-gray-400 dark:text-zinc-500 italic"
                          >
                            暂无标签
                          </span>
                        </div>
                      </div>

                      <div
                        v-if="item.auditStatus === 2 && item.auditRemark"
                        class="rounded-md border border-rose-200 bg-rose-50/50 px-4 py-3 text-sm text-rose-700 dark:border-rose-900/30 dark:bg-rose-950/10 dark:text-rose-400 shadow-sm"
                      >
                        <span class="font-bold mr-1">审核意见：</span>{{ item.auditRemark }}
                      </div>
                    </div>

                    <div class="w-full lg:w-72 shrink-0 space-y-3">
                      <h4 class="text-[10px] font-bold text-gray-400 dark:text-zinc-500 tracking-widest uppercase">快速链接</h4>
                      <div class="space-y-2">
                        <a
                          :href="item.url"
                          target="_blank"
                          rel="noopener noreferrer"
                          class="flex items-center gap-2.5 text-xs text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 transition-colors group"
                        >
                          <div class="p-1.5 bg-blue-50 dark:bg-blue-500/10 rounded-md group-hover:bg-blue-100 dark:group-hover:bg-blue-500/20 transition-colors">
                            <i class="pi pi-link text-[12px]"></i>
                          </div>
                          <span class="truncate underline decoration-blue-200 dark:decoration-blue-900 underline-offset-4">{{ item.url }}</span>
                        </a>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="flex flex-col gap-2">
            <p class="text-xs text-gray-500 dark:text-gray-400">共 {{ total }} 条投稿记录</p>
            <AppPagination
              :currentPage="currentPage"
              :totalPages="totalPages"
              @update:currentPage="currentPage = $event"
            />
          </div>
        </template>

        <UModal
          :open="cancelConfirmOpen"
          title="确认取消投稿"
          description="取消后该投稿将不再进入审核流程，你可以稍后重新投稿。"
          :ui="{
            overlay: 'bg-black/45 backdrop-blur-[1px] z-[120]',
            content:
              'w-[min(92vw,440px)] rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-[0_16px_36px_rgba(15,23,42,0.28)] dark:shadow-[0_22px_44px_rgba(2,6,23,0.62)] overflow-hidden',
            header:
              'border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-5 py-4',
            title: 'text-base font-semibold text-gray-900 dark:text-gray-100',
            description: 'mt-1 text-sm text-gray-600 dark:text-gray-300',
            body: 'px-5 py-4',
            footer:
              'px-5 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg flex flex-col-reverse gap-2 sm:flex-row sm:justify-end',
          }"
          @update:open="
            (value) => {
              if (!value) {
                closeCancelSubmissionConfirm()
              }
            }
          "
        >
          <template #body>
            <p class="text-sm text-gray-700 dark:text-gray-200 break-all">
              投稿名称：
              <span class="font-medium text-gray-900 dark:text-gray-100">
                {{ cancelCandidate?.name || '未命名网站' }}
              </span>
            </p>
          </template>

          <template #footer>
            <UserButton
              label="再想想"
              severity="secondary"
              size="small"
              :disabled="isCancelConfirmLoading"
              @click="closeCancelSubmissionConfirm"
            />
            <UserButton
              :icon="isCancelConfirmLoading ? 'pi pi-spin pi-spinner' : 'pi pi-times'"
              :label="isCancelConfirmLoading ? '取消中' : '确认取消'"
              severity="danger"
              size="small"
              :disabled="!cancelCandidate || isCancelConfirmLoading"
              @click="confirmCancelSubmission"
            />
          </template>
        </UModal>
      </main>
    </div>
  </div>
</template>
