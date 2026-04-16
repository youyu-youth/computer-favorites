import { computed, onMounted, reactive, ref, shallowRef } from 'vue'
import {
  batchHandleAdminReport,
  getAdminReportDetail,
  getAdminReportList,
  getAdminReportStatistics,
  handleAdminReport,
} from '@/api/admin-report'
import type {
  AdminReportDetail,
  AdminReportHandleAction,
  AdminReportHandleForm,
  AdminReportListItem,
  AdminReportQuery,
  AdminReportStatistics,
  ReportType,
} from '@/types/report'
import { ReportStatus, getReportStatusMeta } from '@/types/report'

type HandleDialogMode = 'single' | 'batch'

interface HandleDialogState extends AdminReportHandleForm {
  open: boolean
  mode: HandleDialogMode
  reportIds: number[]
}

type LoadListOptions = {
  silent?: boolean
}

type RefreshResult = {
  ok: boolean
  message?: string
}

const DEFAULT_PAGE_SIZE = 6

const DEFAULT_STATISTICS: AdminReportStatistics = {
  total: 0,
  pending: 0,
  processed: 0,
  rejected: 0,
  last24Hours: 0,
  websiteCount: 0,
  commentCount: 0,
  processRate: 0,
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

function buildSingleHandleSuccessMessage(
  action: AdminReportHandleAction,
  actionExecuted: boolean,
): string {
  if (action === 'reject') {
    return '举报已驳回，列表与统计已同步刷新。'
  }

  return actionExecuted
    ? '举报已通过并执行联动处置，列表与统计已同步刷新。'
    : '举报已通过，未执行联动处置，列表与统计已同步刷新。'
}

export function useAdminReportManagement() {
  const reports = ref<AdminReportListItem[]>([])
  const loading = shallowRef(false)
  const statisticsState = ref<AdminReportStatistics>({ ...DEFAULT_STATISTICS })
  const totalItemsState = shallowRef(0)
  const totalPagesState = shallowRef(1)
  const detailOpen = shallowRef(false)
  const detailLoading = shallowRef(false)
  const detailRecord = shallowRef<AdminReportDetail | null>(null)
  const detailReportId = shallowRef<number | null>(null)
  const imagePreviewOpen = shallowRef(false)
  const imagePreviewUrl = shallowRef('')
  const imagePreviewTitle = shallowRef('')
  const handleSubmitting = shallowRef(false)
  const lastErrorMessage = shallowRef('')
  const selectedIds = ref<number[]>([])
  const query = reactive<AdminReportQuery>({
    pageNum: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    keyword: '',
    status: null,
    type: null,
    pendingOnly: false,
  })
  const handleDialog = reactive<HandleDialogState>({
    open: false,
    mode: 'single',
    reportIds: [],
    action: 'pass',
    handleResult: '',
    executeAction: true,
  })

  let listRequestId = 0
  let detailRequestId = 0

  const totalItems = computed(() => totalItemsState.value)
  const totalPages = computed(() => Math.max(1, totalPagesState.value))
  const visiblePages = computed(() => createVisiblePages(totalPages.value, query.pageNum))
  const pagedReports = computed(() => reports.value)
  const selectedReports = computed(() =>
    reports.value.filter((report) => selectedIds.value.includes(report.id)),
  )
  const selectedCount = computed(() => selectedIds.value.length)
  const pendingSelectionOnly = computed(() =>
    selectedCount.value > 0 &&
    selectedReports.value.every((report) => report.status === ReportStatus.PENDING),
  )
  const statistics = computed(() => statisticsState.value)
  const statusSummaryText = computed(() => {
    const pendingMeta = getReportStatusMeta(ReportStatus.PENDING)
    return `${statistics.value.pending} 条${pendingMeta.label}需要关注`
  })

  const clearSelection = () => {
    selectedIds.value = []
  }

  const loadReportPage = async (options?: LoadListOptions) => {
    listRequestId += 1
    const requestId = listRequestId
    const silent = options?.silent === true

    if (!silent) {
      loading.value = true
    }
    lastErrorMessage.value = ''

    try {
      const pageData = await getAdminReportList({
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        keyword: query.keyword,
        status: query.status,
        type: query.type,
        pendingOnly: query.pendingOnly,
      })

      if (requestId !== listRequestId) {
        return
      }

      const nextTotalPages = Math.max(1, Number(pageData.totalPages || 1))
      if (query.pageNum > nextTotalPages) {
        query.pageNum = nextTotalPages
        void loadReportPage({ silent: true })
        return
      }

      reports.value = pageData.records || []
      totalItemsState.value = Number(pageData.total || 0)
      totalPagesState.value = nextTotalPages
      selectedIds.value = selectedIds.value.filter((id) =>
        reports.value.some((report) => report.id === id),
      )
    } catch (error) {
      if (requestId !== listRequestId) {
        return
      }

      reports.value = []
      totalItemsState.value = 0
      totalPagesState.value = 1
      clearSelection()
      lastErrorMessage.value = resolveErrorMessage(error, '举报列表加载失败')
    } finally {
      if (requestId === listRequestId && !silent) {
        loading.value = false
      }
    }
  }

  const loadStatistics = async () => {
    try {
      statisticsState.value = await getAdminReportStatistics()
    } catch (error) {
      statisticsState.value = { ...DEFAULT_STATISTICS }
      lastErrorMessage.value = resolveErrorMessage(error, '举报统计加载失败')
    }
  }

  const loadDetail = async (reportId: number) => {
    detailRequestId += 1
    const requestId = detailRequestId
    detailLoading.value = true
    lastErrorMessage.value = ''

    try {
      const detail = await getAdminReportDetail(reportId)
      if (requestId !== detailRequestId) {
        return
      }
      detailRecord.value = detail
    } catch (error) {
      if (requestId !== detailRequestId) {
        return
      }
      detailRecord.value = null
      lastErrorMessage.value = resolveErrorMessage(error, '举报详情加载失败')
    } finally {
      if (requestId === detailRequestId) {
        detailLoading.value = false
      }
    }
  }

  const refreshCurrentDetailIfNeeded = async (reportIds: number[]) => {
    if (!detailOpen.value || detailReportId.value === null) {
      return
    }

    if (!reportIds.includes(detailReportId.value)) {
      return
    }

    await loadDetail(detailReportId.value)
  }

  const refreshData = async (): Promise<RefreshResult> => {
    await Promise.all([loadStatistics(), loadReportPage()])
    if (lastErrorMessage.value) {
      return {
        ok: false,
        message: lastErrorMessage.value,
      }
    }
    return {
      ok: true,
    }
  }

  const resetToFirstPage = () => {
    query.pageNum = 1
  }

  const setKeyword = (keyword: string) => {
    query.keyword = keyword
    clearSelection()
    resetToFirstPage()
    void loadReportPage()
  }

  const setStatus = (status: ReportStatus | null) => {
    query.status = status
    clearSelection()
    resetToFirstPage()
    void loadReportPage()
  }

  const setType = (type: ReportType | null) => {
    query.type = type
    clearSelection()
    resetToFirstPage()
    void loadReportPage()
  }

  const setPendingOnly = (value: boolean) => {
    query.pendingOnly = value
    clearSelection()
    resetToFirstPage()
    void loadReportPage()
  }

  const prevPage = () => {
    if (query.pageNum <= 1) {
      return
    }

    clearSelection()
    query.pageNum -= 1
    void loadReportPage()
  }

  const nextPage = () => {
    if (query.pageNum >= totalPages.value) {
      return
    }

    clearSelection()
    query.pageNum += 1
    void loadReportPage()
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

    clearSelection()
    query.pageNum = normalizedPage
    void loadReportPage()
  }

  const setSelectedIds = (ids: number[]) => {
    selectedIds.value = ids
  }

  const openDetail = (reportId: number) => {
    detailReportId.value = reportId
    detailOpen.value = true
    detailRecord.value = null
    void loadDetail(reportId)
  }

  const closeDetail = () => {
    detailOpen.value = false
    detailRecord.value = null
    detailReportId.value = null
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

  const openSingleHandleDialog = (reportId: number, action: AdminReportHandleAction = 'pass') => {
    handleDialog.open = true
    handleDialog.mode = 'single'
    handleDialog.reportIds = [reportId]
    handleDialog.action = action
    handleDialog.handleResult = ''
    handleDialog.executeAction = action === 'pass'
  }

  const openBatchHandleDialog = (action: AdminReportHandleAction = 'pass') => {
    handleDialog.open = true
    handleDialog.mode = 'batch'
    handleDialog.reportIds = [...new Set(selectedIds.value)]
    handleDialog.action = action
    handleDialog.handleResult = ''
    handleDialog.executeAction = action === 'pass'
  }

  const closeHandleDialog = () => {
    handleDialog.open = false
    handleDialog.mode = 'single'
    handleDialog.reportIds = []
    handleDialog.action = 'pass'
    handleDialog.handleResult = ''
    handleDialog.executeAction = true
  }

  const updateHandleForm = (payload: Partial<AdminReportHandleForm>) => {
    if (typeof payload.action !== 'undefined') {
      handleDialog.action = payload.action
      if (payload.action === 'reject') {
        handleDialog.executeAction = false
      }
      if (payload.action === 'pass' && typeof payload.executeAction === 'undefined') {
        handleDialog.executeAction = true
      }
    }

    if (typeof payload.handleResult !== 'undefined') {
      handleDialog.handleResult = payload.handleResult
    }

    if (typeof payload.executeAction !== 'undefined' && handleDialog.action === 'pass') {
      handleDialog.executeAction = payload.executeAction
    }
  }

  const submitHandleAction = async () => {
    const trimmedResult = handleDialog.handleResult.trim()
    if (trimmedResult.length < 10) {
      return {
        ok: false as const,
        message: '处理说明至少需要 10 个字符。',
      }
    }

    if (handleDialog.reportIds.length === 0) {
      return {
        ok: false as const,
        message: '请先选择需要处置的举报。',
      }
    }

    if (handleSubmitting.value) {
      return {
        ok: false as const,
        message: '当前正在提交处置请求，请稍后再试。',
      }
    }

    handleSubmitting.value = true
    try {
      const handledReportIds = [...handleDialog.reportIds]

      if (handleDialog.mode === 'single') {
        const targetReportId = handledReportIds[0]
        if (typeof targetReportId !== 'number') {
          return {
            ok: false as const,
            message: '未找到需要处置的举报记录。',
          }
        }

        const result = await handleAdminReport(targetReportId, {
          action: handleDialog.action,
          handleResult: trimmedResult,
          executeAction: handleDialog.action === 'pass' ? handleDialog.executeAction : false,
        })

        closeHandleDialog()
        clearSelection()
        await Promise.all([
          loadReportPage({ silent: true }),
          loadStatistics(),
          refreshCurrentDetailIfNeeded(handledReportIds),
        ])

        return {
          ok: true as const,
          message: buildSingleHandleSuccessMessage(handleDialog.action, result.actionExecuted),
        }
      }

      const result = await batchHandleAdminReport({
        reportIds: handleDialog.reportIds,
        action: handleDialog.action,
        handleResult: trimmedResult,
        executeAction: handleDialog.action === 'pass' ? handleDialog.executeAction : false,
      })

      closeHandleDialog()
      clearSelection()
      await Promise.all([
        loadReportPage({ silent: true }),
        loadStatistics(),
        refreshCurrentDetailIfNeeded(handledReportIds),
      ])

      if (result.failed > 0) {
        const failedPreview = result.results
          .filter((item) => !item.success)
          .slice(0, 3)
          .map((item) => `#${item.reportId} ${item.message}`)
          .join('；')

        return {
          ok: false as const,
          message: `批量处置完成，成功 ${result.success} 条，失败 ${result.failed} 条。${failedPreview}`,
        }
      }

      return {
        ok: true as const,
        message: `批量处置完成，共成功处理 ${result.success} 条举报。`,
      }
    } catch (error) {
      return {
        ok: false as const,
        message: resolveErrorMessage(error, '提交处置请求失败'),
      }
    } finally {
      handleSubmitting.value = false
    }
  }

  onMounted(() => {
    void refreshData()
  })

  return {
    reports,
    loading,
    detailLoading,
    lastErrorMessage,
    query,
    pagedReports,
    totalItems,
    totalPages,
    visiblePages,
    selectedIds,
    selectedReports,
    selectedCount,
    pendingSelectionOnly,
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
    setPendingOnly,
    refreshData,
    prevPage,
    nextPage,
    goToPage,
    setSelectedIds,
    clearSelection,
    openDetail,
    closeDetail,
    openImagePreview,
    closeImagePreview,
    openSingleHandleDialog,
    openBatchHandleDialog,
    closeHandleDialog,
    updateHandleForm,
    submitHandleAction,
  }
}
