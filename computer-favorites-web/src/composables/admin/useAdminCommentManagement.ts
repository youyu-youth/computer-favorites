import { computed, onMounted, reactive, ref, shallowRef } from 'vue'
import {
  batchUpdateAdminCommentStatus,
  getAdminCommentDetail,
  getAdminCommentList,
  getAdminCommentStatistics,
  updateAdminCommentStatus,
} from '@/api/admin-comment'
import type {
  AdminCommentAction,
  AdminCommentDetail,
  AdminCommentHandleForm,
  AdminCommentListItem,
  AdminCommentQuery,
  AdminCommentStatistics,
} from '@/types/admin-comment'
import { AdminCommentStatus } from '@/types/admin-comment'

type RefreshResult = { ok: boolean; message?: string }

interface HandleDialogState extends AdminCommentHandleForm {
  open: boolean
  commentId: number | null
  isBatch: boolean
  batchIds: number[]
}

const DEFAULT_PAGE_SIZE = 10

const DEFAULT_STATISTICS: AdminCommentStatistics = {
  total: 0,
  todayNew: 0,
  visible: 0,
  hidden: 0,
}

function createVisiblePages(totalPages: number, currentPage: number): Array<number | string> {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i + 1)
  }

  const pages: Array<number | string> = [1]
  if (currentPage > 3) pages.push('...')
  for (let p = Math.max(2, currentPage - 1); p <= Math.min(totalPages - 1, currentPage + 1); p += 1) {
    pages.push(p)
  }
  if (currentPage < totalPages - 2) pages.push('...')
  pages.push(totalPages)
  return pages
}

function resolveErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallback
}

export function useAdminCommentManagement() {
  const loading = shallowRef(false)
  const detailLoading = shallowRef(false)
  const pagedComments = ref<AdminCommentListItem[]>([])
  const statisticsState = ref<AdminCommentStatistics>({ ...DEFAULT_STATISTICS })
  const totalItemsState = shallowRef(0)
  const totalPagesState = shallowRef(1)
  const detailOpen = shallowRef(false)
  const detailRecord = shallowRef<AdminCommentDetail | null>(null)
  const selectedIds = ref<number[]>([])
  const handleSubmitting = shallowRef(false)

  const query = reactive<AdminCommentQuery>({
    pageNum: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    keyword: '',
    status: null,
  })

  const handleDialog = reactive<HandleDialogState>({
    open: false,
    commentId: null,
    action: 'hide',
    reason: '',
    isBatch: false,
    batchIds: [],
  })

  const totalItems = computed(() => totalItemsState.value)
  const totalPages = computed(() => Math.max(1, totalPagesState.value))
  const visiblePages = computed(() => createVisiblePages(totalPages.value, query.pageNum))
  const statistics = computed(() => statisticsState.value)
  const statusSummaryText = computed(() => `${statistics.value.hidden} 条评论已隐藏`)
  const selectedCount = computed(() => selectedIds.value.length)

  const loadCommentPage = async () => {
    loading.value = true
    try {
      const page = await getAdminCommentList(query)
      pagedComments.value = page.records
      totalItemsState.value = page.total
      totalPagesState.value = page.totalPages

      if (query.pageNum > totalPagesState.value) {
        query.pageNum = totalPagesState.value
      }
    } catch (error) {
      pagedComments.value = []
      totalItemsState.value = 0
      totalPagesState.value = 1
    } finally {
      loading.value = false
    }
  }

  const recalculateStatistics = async () => {
    try {
      const stats = await getAdminCommentStatistics()
      statisticsState.value = stats
    } catch (error) {
      statisticsState.value = { ...DEFAULT_STATISTICS }
    }
  }

  const refreshData = async (): Promise<RefreshResult> => {
    await Promise.all([loadCommentPage(), recalculateStatistics()])
    return { ok: true }
  }

  const resetToFirstPage = () => {
    query.pageNum = 1
  }

  const setKeyword = (keyword: string) => {
    query.keyword = keyword
    resetToFirstPage()
    loadCommentPage()
  }

  const setStatus = (status: AdminCommentStatus | null) => {
    query.status = status
    resetToFirstPage()
    loadCommentPage()
  }

  const prevPage = () => {
    if (query.pageNum <= 1) return
    query.pageNum -= 1
    loadCommentPage()
  }

  const nextPage = () => {
    if (query.pageNum >= totalPages.value) return
    query.pageNum += 1
    loadCommentPage()
  }

  const goToPage = (page: number | string) => {
    const n = Number(page)
    if (!Number.isFinite(n)) return
    const normalized = Math.min(totalPages.value, Math.max(1, Math.trunc(n)))
    if (normalized === query.pageNum) return
    query.pageNum = normalized
    loadCommentPage()
  }

  const openDetail = async (commentId: number) => {
    detailOpen.value = true
    detailLoading.value = true

    try {
      const detail = await getAdminCommentDetail(commentId)
      detailRecord.value = detail
    } catch (error) {
      detailRecord.value = null
    } finally {
      detailLoading.value = false
    }
  }

  const closeDetail = () => {
    detailOpen.value = false
    detailRecord.value = null
  }

  const setSelectedIds = (ids: number[]) => {
    selectedIds.value = ids
  }

  const clearSelection = () => {
    selectedIds.value = []
  }

  const openSingleHandleDialog = (commentId: number, action: AdminCommentAction) => {
    handleDialog.open = true
    handleDialog.commentId = commentId
    handleDialog.action = action
    handleDialog.reason = ''
    handleDialog.isBatch = false
    handleDialog.batchIds = []
  }

  const openBatchHandleDialog = (action: AdminCommentAction) => {
    handleDialog.open = true
    handleDialog.commentId = null
    handleDialog.action = action
    handleDialog.reason = ''
    handleDialog.isBatch = true
    handleDialog.batchIds = [...selectedIds.value]
  }

  const closeHandleDialog = () => {
    handleDialog.open = false
    handleDialog.commentId = null
    handleDialog.action = 'hide'
    handleDialog.reason = ''
    handleDialog.isBatch = false
    handleDialog.batchIds = []
  }

  const updateHandleForm = (payload: Partial<AdminCommentHandleForm>) => {
    if (typeof payload.action !== 'undefined') handleDialog.action = payload.action
    if (typeof payload.reason !== 'undefined') handleDialog.reason = payload.reason
  }

  const submitHandleAction = async (): Promise<RefreshResult> => {
    if (handleSubmitting.value) {
      return { ok: false, message: '当前正在提交请求，请稍后再试。' }
    }

    handleSubmitting.value = true
    try {
      const form: AdminCommentHandleForm = {
        action: handleDialog.action,
        reason: handleDialog.reason,
      }

      if (handleDialog.isBatch) {
        const result = await batchUpdateAdminCommentStatus(handleDialog.batchIds, form)
        selectedIds.value = selectedIds.value.filter((id) => !handleDialog.batchIds.includes(id))
        closeHandleDialog()
        await refreshData()
        return { ok: true, message: result.message }
      }

      if (handleDialog.commentId) {
        await updateAdminCommentStatus(handleDialog.commentId, form)
        closeHandleDialog()
        await refreshData()
        return {
          ok: true,
          message: `评论已${handleDialog.action === 'hide' ? '隐藏' : '显示'}。`,
        }
      }

      return { ok: false, message: '未指定操作目标' }
    } catch (error) {
      const msg = resolveErrorMessage(error, '操作失败，请稍后重试')
      return { ok: false, message: msg }
    } finally {
      handleSubmitting.value = false
    }
  }

  onMounted(() => {
    loadCommentPage()
    recalculateStatistics()
  })

  return {
    loading,
    detailLoading,
    query,
    pagedComments,
    totalItems,
    totalPages,
    visiblePages,
    detailOpen,
    detailRecord,
    handleDialog,
    selectedIds,
    selectedCount,
    statistics,
    statusSummaryText,
    setKeyword,
    setStatus,
    refreshData,
    prevPage,
    nextPage,
    goToPage,
    openDetail,
    closeDetail,
    setSelectedIds,
    clearSelection,
    openSingleHandleDialog,
    openBatchHandleDialog,
    closeHandleDialog,
    updateHandleForm,
    submitHandleAction,
  }
}
