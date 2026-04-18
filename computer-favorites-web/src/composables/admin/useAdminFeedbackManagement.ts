import { computed, onMounted, reactive, ref, shallowRef } from 'vue'
import {
  closeAdminFeedback,
  getAdminFeedbackDetail,
  getAdminFeedbackList,
  getAdminFeedbackStatistics,
  replyAdminFeedback,
} from '@/api/admin-feedback'
import type {
  AdminFeedbackDetail,
  AdminFeedbackHandleAction,
  AdminFeedbackHandleForm,
  AdminFeedbackListItem,
  AdminFeedbackQuery,
  AdminFeedbackStatistics,
} from '@/types/feedback'
import { FeedbackStatus, FeedbackType } from '@/types/feedback'

type RefreshResult = {
  ok: boolean
  message?: string
}

interface HandleDialogState extends AdminFeedbackHandleForm {
  open: boolean
  feedbackId: number | null
}

type LoadListOptions = {
  silent?: boolean
}

const DEFAULT_PAGE_SIZE = 6

const DEFAULT_STATISTICS: AdminFeedbackStatistics = {
  total: 0,
  pending: 0,
  processed: 0,
  closed: 0,
  last24Hours: 0,
  bugCount: 0,
  withImagesCount: 0,
  withContactCount: 0,
  replyRate: 0,
}

function createVisiblePages(totalPages: number, currentPage: number): Array<number | string> {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1)
  }

  const pages: Array<number | string> = [1]

  if (currentPage > 3) {
    pages.push('...')
  }

  for (let page = Math.max(2, currentPage - 1); page <= Math.min(totalPages - 1, currentPage + 1); page += 1) {
    pages.push(page)
  }

  if (currentPage < totalPages - 2) {
    pages.push('...')
  }

  pages.push(totalPages)
  return pages
}

function resolveErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallback
}

export function useAdminFeedbackManagement() {
  const feedbacks = ref<AdminFeedbackListItem[]>([])
  const loading = shallowRef(false)
  const detailLoading = shallowRef(false)
  const statisticsState = ref<AdminFeedbackStatistics>({ ...DEFAULT_STATISTICS })
  const totalItemsState = shallowRef(0)
  const totalPagesState = shallowRef(1)
  const detailOpen = shallowRef(false)
  const detailRecord = shallowRef<AdminFeedbackDetail | null>(null)
  const detailFeedbackId = shallowRef<number | null>(null)
  const imagePreviewOpen = shallowRef(false)
  const imagePreviewUrl = shallowRef('')
  const imagePreviewTitle = shallowRef('')
  const handleSubmitting = shallowRef(false)
  const lastErrorMessage = shallowRef('')
  const query = reactive<AdminFeedbackQuery>({
    pageNum: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    keyword: '',
    status: null,
    type: null,
    hasImages: false,
    hasContact: false,
  })
  const handleDialog = reactive<HandleDialogState>({
    open: false,
    feedbackId: null,
    action: 'reply',
    reply: '',
  })

  let listRequestId = 0
  let detailRequestId = 0

  const totalItems = computed(() => totalItemsState.value)
  const totalPages = computed(() => Math.max(1, totalPagesState.value))
  const visiblePages = computed(() => createVisiblePages(totalPages.value, query.pageNum))
  const pagedFeedbacks = computed(() => feedbacks.value)
  const statistics = computed(() => statisticsState.value)
  const statusSummaryText = computed(() => `${statistics.value.pending} 条待处理工单待跟进`)

  const loadFeedbackPage = async (options?: LoadListOptions) => {
    listRequestId += 1
    const requestId = listRequestId
    const silent = options?.silent === true

    if (!silent) {
      loading.value = true
    }
    lastErrorMessage.value = ''

    try {
      const pageData = await getAdminFeedbackList(query)

      if (requestId !== listRequestId) {
        return
      }

      const nextTotalPages = Math.max(1, Number(pageData.totalPages || 1))
      if (query.pageNum > nextTotalPages) {
        query.pageNum = nextTotalPages
        void loadFeedbackPage({ silent: true })
        return
      }

      feedbacks.value = pageData.records
      totalItemsState.value = Number(pageData.total || 0)
      totalPagesState.value = nextTotalPages
    } catch (error) {
      if (requestId !== listRequestId) {
        return
      }

      feedbacks.value = []
      totalItemsState.value = 0
      totalPagesState.value = 1
      lastErrorMessage.value = resolveErrorMessage(error, '反馈列表加载失败')
    } finally {
      if (requestId === listRequestId && !silent) {
        loading.value = false
      }
    }
  }

  const loadStatistics = async () => {
    try {
      statisticsState.value = await getAdminFeedbackStatistics()
    } catch (error) {
      statisticsState.value = { ...DEFAULT_STATISTICS }
      lastErrorMessage.value = resolveErrorMessage(error, '反馈统计加载失败')
    }
  }

  const loadDetail = async (feedbackId: number) => {
    detailRequestId += 1
    const requestId = detailRequestId
    detailLoading.value = true
    lastErrorMessage.value = ''

    try {
      const detail = await getAdminFeedbackDetail(feedbackId)
      if (requestId !== detailRequestId) {
        return
      }
      detailRecord.value = detail
    } catch (error) {
      if (requestId !== detailRequestId) {
        return
      }
      detailRecord.value = null
      lastErrorMessage.value = resolveErrorMessage(error, '反馈详情加载失败')
    } finally {
      if (requestId === detailRequestId) {
        detailLoading.value = false
      }
    }
  }

  const refreshCurrentDetailIfNeeded = async (feedbackId: number) => {
    if (!detailOpen.value || detailFeedbackId.value !== feedbackId) {
      return
    }

    await loadDetail(feedbackId)
  }

  const refreshData = async (): Promise<RefreshResult> => {
    await Promise.all([loadStatistics(), loadFeedbackPage()])
    if (lastErrorMessage.value) {
      return {
        ok: false,
        message: lastErrorMessage.value,
      }
    }

    return { ok: true }
  }

  const resetToFirstPage = () => {
    query.pageNum = 1
  }

  const setKeyword = (keyword: string) => {
    query.keyword = keyword
    resetToFirstPage()
    void loadFeedbackPage()
  }

  const setStatus = (status: FeedbackStatus | null) => {
    query.status = status
    resetToFirstPage()
    void loadFeedbackPage()
  }

  const setType = (type: FeedbackType | null) => {
    query.type = type
    resetToFirstPage()
    void loadFeedbackPage()
  }

  const setHasImages = (value: boolean) => {
    query.hasImages = value
    resetToFirstPage()
    void loadFeedbackPage()
  }

  const setHasContact = (value: boolean) => {
    query.hasContact = value
    resetToFirstPage()
    void loadFeedbackPage()
  }

  const prevPage = () => {
    if (query.pageNum <= 1) {
      return
    }

    query.pageNum -= 1
    void loadFeedbackPage()
  }

  const nextPage = () => {
    if (query.pageNum >= totalPages.value) {
      return
    }

    query.pageNum += 1
    void loadFeedbackPage()
  }

  const goToPage = (page: number | string) => {
    const nextPageNum = Number(page)
    if (!Number.isFinite(nextPageNum)) {
      return
    }

    const normalizedPage = Math.min(totalPages.value, Math.max(1, Math.trunc(nextPageNum)))
    if (normalizedPage === query.pageNum) {
      return
    }

    query.pageNum = normalizedPage
    void loadFeedbackPage()
  }

  const openDetail = (feedbackId: number) => {
    detailFeedbackId.value = feedbackId
    detailOpen.value = true
    detailRecord.value = null
    void loadDetail(feedbackId)
  }

  const closeDetail = () => {
    detailOpen.value = false
    detailRecord.value = null
    detailFeedbackId.value = null
  }

  const openImagePreview = (url: string, title: string) => {
    imagePreviewUrl.value = url
    imagePreviewTitle.value = title
    imagePreviewOpen.value = true
  }

  const closeImagePreview = () => {
    imagePreviewOpen.value = false
    imagePreviewUrl.value = ''
    imagePreviewTitle.value = ''
  }

  const openHandleDialog = (feedbackId: number, action: AdminFeedbackHandleAction) => {
    handleDialog.open = true
    handleDialog.feedbackId = feedbackId
    handleDialog.action = action
    handleDialog.reply = ''
  }

  const openReplyDialog = (feedbackId: number) => {
    openHandleDialog(feedbackId, 'reply')
  }

  const openCloseDialog = (feedbackId: number) => {
    openHandleDialog(feedbackId, 'close')
  }

  const closeHandleDialog = () => {
    handleDialog.open = false
    handleDialog.feedbackId = null
    handleDialog.action = 'reply'
    handleDialog.reply = ''
  }

  const updateHandleForm = (payload: Partial<AdminFeedbackHandleForm>) => {
    if (typeof payload.action !== 'undefined') {
      handleDialog.action = payload.action
    }

    if (typeof payload.reply !== 'undefined') {
      handleDialog.reply = payload.reply
    }
  }

  const submitHandleAction = async () => {
    if (handleDialog.feedbackId === null) {
      return {
        ok: false as const,
        message: '未找到需要处理的反馈记录。',
      }
    }

    if (handleSubmitting.value) {
      return {
        ok: false as const,
        message: '当前正在提交处理请求，请稍后再试。',
      }
    }

    const trimmedReply = handleDialog.reply.trim()
    if (handleDialog.action === 'reply' && trimmedReply.length < 10) {
      return {
        ok: false as const,
        message: '回复内容至少需要 10 个字符。',
      }
    }

    handleSubmitting.value = true
    try {
      const currentAction = handleDialog.action

      if (currentAction === 'reply') {
        await replyAdminFeedback(handleDialog.feedbackId, { reply: trimmedReply })
      } else {
        await closeAdminFeedback(handleDialog.feedbackId)
      }

      const affectedId = handleDialog.feedbackId
      closeHandleDialog()
      await Promise.all([
        loadFeedbackPage({ silent: true }),
        loadStatistics(),
        refreshCurrentDetailIfNeeded(affectedId),
      ])

      return {
        ok: true as const,
        message:
          currentAction === 'reply'
            ? '反馈已回复，列表与详情已同步刷新。'
            : '反馈已关闭，当前工单不再继续跟进。',
      }
    } catch (error) {
      return {
        ok: false as const,
        message: resolveErrorMessage(error, '处理反馈失败'),
      }
    } finally {
      handleSubmitting.value = false
    }
  }

  onMounted(() => {
    void refreshData()
  })

  return {
    loading,
    detailLoading,
    lastErrorMessage,
    query,
    pagedFeedbacks,
    totalItems,
    totalPages,
    visiblePages,
    detailOpen,
    detailRecord,
    imagePreviewOpen,
    imagePreviewUrl,
    imagePreviewTitle,
    handleDialog,
    statistics,
    statusSummaryText,
    setKeyword,
    setStatus,
    setType,
    setHasImages,
    setHasContact,
    refreshData,
    prevPage,
    nextPage,
    goToPage,
    openDetail,
    closeDetail,
    openImagePreview,
    closeImagePreview,
    openReplyDialog,
    openCloseDialog,
    closeHandleDialog,
    updateHandleForm,
    submitHandleAction,
  }
}
