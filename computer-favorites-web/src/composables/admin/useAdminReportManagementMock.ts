import { computed, reactive, readonly, ref, shallowRef } from 'vue'
import {
  type AdminReportDetail,
  type AdminReportHandleAction,
  type AdminReportHandleForm,
  type AdminReportListItem,
  type AdminReportQuery,
  type AdminReportStatistics,
  ReportStatus,
  ReportType,
  getReportStatusMeta,
  getReportTypeMeta,
} from '@/types/report'

type HandleDialogMode = 'single' | 'batch'

interface HandleDialogState extends AdminReportHandleForm {
  open: boolean
  mode: HandleDialogMode
  reportIds: number[]
}

const DEFAULT_PAGE_SIZE = 6

const createTimeline = (report: Omit<AdminReportDetail, 'timeline'>) => {
  const baseTimeline: AdminReportDetail['timeline'] = [
    {
      id: `create-${report.id}`,
      title: '举报已提交',
      description: `${report.userName} 提交了${getReportTypeMeta(report.type).label}，等待管理员处理。`,
      time: report.createTime,
      tone: 'done',
    },
  ]

  if (report.status === ReportStatus.PENDING) {
    baseTimeline.push({
      id: `pending-${report.id}`,
      title: '等待处置',
      description: '案件仍处于待处理状态，建议优先查看证据截图与目标对象信息。',
      time: report.updateTime,
      tone: 'pending',
    })
    return baseTimeline
  }

  baseTimeline.push({
    id: `handled-${report.id}`,
    title: report.status === ReportStatus.PROCESSED ? '举报已通过' : '举报已驳回',
    description: report.handleResult ?? '管理员已完成处置。',
    time: report.handleTime ?? report.updateTime,
    tone: 'done',
  })
  return baseTimeline
}

const createReportDetail = (
  detail: Omit<AdminReportDetail, 'timeline' | 'evidenceSummary' | 'targetStatusLabel'>,
): AdminReportDetail => {
  const targetStatusLabel =
    detail.type === ReportType.WEBSITE
      ? detail.status === ReportStatus.PROCESSED
        ? '已纳入治理观察'
        : '在线可访问'
      : detail.status === ReportStatus.PROCESSED
        ? '评论已纳入审查'
        : '评论可见'

  const evidenceSummary =
    detail.images.length > 0
      ? `已提交 ${detail.images.length} 张截图证据，可用于辅助判定。`
      : '当前未上传截图证据，建议结合目标对象内容进行人工复核。'

  const reportDetail: Omit<AdminReportDetail, 'timeline'> = {
    ...detail,
    targetStatusLabel,
    evidenceSummary,
  }

  return {
    ...reportDetail,
    timeline: createTimeline(reportDetail),
  }
}

const createMockReports = (): AdminReportDetail[] => [
  createReportDetail({
    id: 10001,
    userId: 3001,
    userName: '橙子收藏家',
    userEmail: 'orange@example.com',
    type: ReportType.WEBSITE,
    targetId: 8101,
    targetName: 'Digital Shortcut Hub',
    targetUrl: 'https://example.com/digital-shortcut-hub',
    uploaderId: 9001,
    uploaderName: '站长风纪官',
    reason: '内容误导: 页面宣称永久免费，但实际跳转后要求付费订阅。',
    images: [
      'https://picsum.photos/seed/report-10001-a/960/600',
      'https://picsum.photos/seed/report-10001-b/960/600',
    ],
    status: ReportStatus.PENDING,
    handleResult: null,
    handlerId: null,
    handlerName: null,
    handleTime: null,
    createTime: '2026-04-15 08:42:00',
    updateTime: '2026-04-15 08:42:00',
  }),
  createReportDetail({
    id: 10002,
    userId: 3002,
    userName: '深夜清单控',
    userEmail: 'nightlist@example.com',
    type: ReportType.COMMENT,
    targetId: 6201,
    targetName: '评论 #6201',
    targetUrl: null,
    uploaderId: 9002,
    uploaderName: '评论作者小星',
    reason: '人身攻击: 评论区存在明显辱骂和挑衅内容，影响讨论氛围。',
    images: ['https://picsum.photos/seed/report-10002-a/960/600'],
    status: ReportStatus.PENDING,
    handleResult: null,
    handlerId: null,
    handlerName: null,
    handleTime: null,
    createTime: '2026-04-15 10:18:00',
    updateTime: '2026-04-15 10:18:00',
  }),
  createReportDetail({
    id: 10003,
    userId: 3003,
    userName: '信息洁癖者',
    userEmail: 'cleaninfo@example.com',
    type: ReportType.WEBSITE,
    targetId: 8102,
    targetName: 'Zero Noise AI',
    targetUrl: 'https://example.com/zero-noise-ai',
    uploaderId: 9003,
    uploaderName: '工具仓管理员',
    reason: '失效链接: 首页按钮无法访问，连续三天无法打开主要功能页面。',
    images: ['https://picsum.photos/seed/report-10003-a/960/600'],
    status: ReportStatus.PROCESSED,
    handleResult: '经核查链接确有异常，已通知站点上传者处理，并加入重点巡检名单。',
    handlerId: 1,
    handlerName: '超级管理员',
    handleTime: '2026-04-14 16:35:00',
    createTime: '2026-04-14 15:10:00',
    updateTime: '2026-04-14 16:35:00',
  }),
  createReportDetail({
    id: 10004,
    userId: 3004,
    userName: '内容守门员',
    userEmail: 'keeper@example.com',
    type: ReportType.COMMENT,
    targetId: 6202,
    targetName: '评论 #6202',
    targetUrl: null,
    uploaderId: 9004,
    uploaderName: '匿名用户',
    reason: '虚假举报: 举报内容与目标评论不符，截图证据无法证明问题存在。',
    images: [],
    status: ReportStatus.REJECTED,
    handleResult: '核查截图与评论原文后，未发现违规内容，本次举报驳回。',
    handlerId: 2,
    handlerName: '审核管理员',
    handleTime: '2026-04-13 11:28:00',
    createTime: '2026-04-13 09:12:00',
    updateTime: '2026-04-13 11:28:00',
  }),
  createReportDetail({
    id: 10005,
    userId: 3005,
    userName: '低噪整理师',
    userEmail: 'focus@example.com',
    type: ReportType.WEBSITE,
    targetId: 8103,
    targetName: 'Minimal Atlas',
    targetUrl: 'https://example.com/minimal-atlas',
    uploaderId: 9005,
    uploaderName: '目录策展人',
    reason: '资源违规: 页面含诱导下载弹窗，存在与收藏站定位不符的推广行为。',
    images: [
      'https://picsum.photos/seed/report-10005-a/960/600',
      'https://picsum.photos/seed/report-10005-b/960/600',
      'https://picsum.photos/seed/report-10005-c/960/600',
    ],
    status: ReportStatus.PENDING,
    handleResult: null,
    handlerId: null,
    handlerName: null,
    handleTime: null,
    createTime: '2026-04-15 12:08:00',
    updateTime: '2026-04-15 12:08:00',
  }),
  createReportDetail({
    id: 10006,
    userId: 3006,
    userName: '慢速阅读派',
    userEmail: 'reader@example.com',
    type: ReportType.COMMENT,
    targetId: 6203,
    targetName: '评论 #6203',
    targetUrl: null,
    uploaderId: 9006,
    uploaderName: '评论作者阿宁',
    reason: '广告灌水: 连续发布无关推广信息，破坏评论区信息质量。',
    images: ['https://picsum.photos/seed/report-10006-a/960/600'],
    status: ReportStatus.PROCESSED,
    handleResult: '已确认属于广告灌水，评论内容已纳入治理队列。',
    handlerId: 1,
    handlerName: '超级管理员',
    handleTime: '2026-04-12 18:16:00',
    createTime: '2026-04-12 17:02:00',
    updateTime: '2026-04-12 18:16:00',
  }),
  createReportDetail({
    id: 10007,
    userId: 3007,
    userName: '精简控',
    userEmail: 'tinylist@example.com',
    type: ReportType.WEBSITE,
    targetId: 8104,
    targetName: 'Signal Search Weekly',
    targetUrl: 'https://example.com/signal-search-weekly',
    uploaderId: 9007,
    uploaderName: '周刊维护者',
    reason: '标题夸大: 标题宣称全网首发，但正文仅为普通转载信息，存在误导。',
    images: ['https://picsum.photos/seed/report-10007-a/960/600'],
    status: ReportStatus.REJECTED,
    handleResult: '当前证据不足以证明存在恶意夸大，建议继续观察。',
    handlerId: 2,
    handlerName: '审核管理员',
    handleTime: '2026-04-11 14:20:00',
    createTime: '2026-04-11 13:01:00',
    updateTime: '2026-04-11 14:20:00',
  }),
  createReportDetail({
    id: 10008,
    userId: 3008,
    userName: '证据主义者',
    userEmail: 'evidence@example.com',
    type: ReportType.COMMENT,
    targetId: 6204,
    targetName: '评论 #6204',
    targetUrl: null,
    uploaderId: 9008,
    uploaderName: '讨论区用户 K',
    reason: '剧透刷屏: 大量发布剧透内容，影响社区正常阅读体验。',
    images: [
      'https://picsum.photos/seed/report-10008-a/960/600',
      'https://picsum.photos/seed/report-10008-b/960/600',
    ],
    status: ReportStatus.PENDING,
    handleResult: null,
    handlerId: null,
    handlerName: null,
    handleTime: null,
    createTime: '2026-04-15 13:35:00',
    updateTime: '2026-04-15 13:35:00',
  }),
]

const sortReports = (reports: AdminReportDetail[]) => {
  return [...reports].sort((left, right) => {
    if (left.status !== right.status) {
      return left.status - right.status
    }
    return new Date(right.createTime).getTime() - new Date(left.createTime).getTime()
  })
}

const paginate = <T>(records: T[], pageNum: number, pageSize: number) => {
  const start = (pageNum - 1) * pageSize
  return records.slice(start, start + pageSize)
}

const createVisiblePages = (totalPages: number, currentPage: number): Array<number | string> => {
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

export function useAdminReportManagementMock() {
  const reports = ref<AdminReportDetail[]>(sortReports(createMockReports()))
  const loading = shallowRef(false)
  const query = reactive<AdminReportQuery>({
    pageNum: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    keyword: '',
    status: null,
    type: null,
    pendingOnly: false,
  })
  const selectedIds = ref<number[]>([])
  const detailReportId = shallowRef<number | null>(null)
  const detailOpen = shallowRef(false)
  const imagePreviewOpen = shallowRef(false)
  const imagePreviewUrl = shallowRef('')
  const imagePreviewTitle = shallowRef('')
  const handleDialog = reactive<HandleDialogState>({
    open: false,
    mode: 'single',
    reportIds: [],
    action: 'pass',
    handleResult: '',
    executeAction: true,
  })

  const normalizedKeyword = computed(() => query.keyword.trim().toLowerCase())

  const filteredReports = computed(() => {
    return reports.value.filter((report) => {
      if (query.pendingOnly && report.status !== ReportStatus.PENDING) {
        return false
      }

      if (query.status !== null && report.status !== query.status) {
        return false
      }

      if (query.type !== null && report.type !== query.type) {
        return false
      }

      if (!normalizedKeyword.value) {
        return true
      }

      const searchCorpus = [
        report.id,
        report.userName,
        report.userEmail,
        report.targetName,
        report.uploaderName,
        report.reason,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()

      return searchCorpus.includes(normalizedKeyword.value)
    })
  })

  const totalItems = computed(() => filteredReports.value.length)
  const totalPages = computed(() => Math.max(1, Math.ceil(totalItems.value / query.pageSize)))
  const pagedReports = computed(() => paginate(filteredReports.value, query.pageNum, query.pageSize))
  const visiblePages = computed(() => createVisiblePages(totalPages.value, query.pageNum))
  const selectedReports = computed(() =>
    reports.value.filter((report) => selectedIds.value.includes(report.id)),
  )
  const selectedCount = computed(() => selectedIds.value.length)
  const detailRecord = computed(() => {
    if (detailReportId.value === null) {
      return null
    }
    return reports.value.find((report) => report.id === detailReportId.value) ?? null
  })
  const statistics = computed<AdminReportStatistics>(() => {
    const total = reports.value.length
    const pending = reports.value.filter((report) => report.status === ReportStatus.PENDING).length
    const processed = reports.value.filter((report) => report.status === ReportStatus.PROCESSED).length
    const rejected = reports.value.filter((report) => report.status === ReportStatus.REJECTED).length
    const websiteCount = reports.value.filter((report) => report.type === ReportType.WEBSITE).length
    const commentCount = reports.value.filter((report) => report.type === ReportType.COMMENT).length
    const last24Hours = reports.value.filter((report) => report.createTime >= '2026-04-15 00:00:00').length
    const processRate = total === 0 ? 0 : Math.round(((processed + rejected) / total) * 100)

    return {
      total,
      pending,
      processed,
      rejected,
      last24Hours,
      websiteCount,
      commentCount,
      processRate,
    }
  })
  const pendingSelectionOnly = computed(() =>
    selectedReports.value.every((report) => report.status === ReportStatus.PENDING),
  )

  const ensureValidPage = () => {
    if (query.pageNum > totalPages.value) {
      query.pageNum = totalPages.value
    }
    if (query.pageNum < 1) {
      query.pageNum = 1
    }
  }

  const resetToFirstPage = () => {
    query.pageNum = 1
  }

  const setKeyword = (keyword: string) => {
    query.keyword = keyword
    clearSelection()
    resetToFirstPage()
  }

  const setStatus = (status: ReportStatus | null) => {
    query.status = status
    clearSelection()
    resetToFirstPage()
  }

  const setType = (type: ReportType | null) => {
    query.type = type
    clearSelection()
    resetToFirstPage()
  }

  const setPendingOnly = (value: boolean) => {
    query.pendingOnly = value
    clearSelection()
    resetToFirstPage()
  }

  const refreshData = () => {
    loading.value = true
    clearSelection()
    window.setTimeout(() => {
      reports.value = sortReports(reports.value)
      loading.value = false
    }, 180)
  }

  const prevPage = () => {
    if (query.pageNum > 1) {
      clearSelection()
      query.pageNum -= 1
    }
  }

  const nextPage = () => {
    if (query.pageNum < totalPages.value) {
      clearSelection()
      query.pageNum += 1
    }
  }

  const goToPage = (page: number | string) => {
    const nextPageNum = Number(page)
    if (!Number.isFinite(nextPageNum)) {
      return
    }
    clearSelection()
    query.pageNum = Math.min(totalPages.value, Math.max(1, Math.trunc(nextPageNum)))
  }

  const setSelectedIds = (ids: number[]) => {
    selectedIds.value = ids
  }

  const clearSelection = () => {
    selectedIds.value = []
  }

  const openDetail = (reportId: number) => {
    detailReportId.value = reportId
    detailOpen.value = true
  }

  const closeDetail = () => {
    detailOpen.value = false
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
    handleDialog.reportIds = [...selectedIds.value]
    handleDialog.action = action
    handleDialog.handleResult = ''
    handleDialog.executeAction = action === 'pass'
  }

  const closeHandleDialog = () => {
    handleDialog.open = false
    handleDialog.reportIds = []
    handleDialog.handleResult = ''
    handleDialog.executeAction = true
    handleDialog.action = 'pass'
    handleDialog.mode = 'single'
  }

  const updateHandleForm = (payload: Partial<AdminReportHandleForm>) => {
    if (typeof payload.action !== 'undefined') {
      handleDialog.action = payload.action
    }
    if (typeof payload.handleResult !== 'undefined') {
      handleDialog.handleResult = payload.handleResult
    }
    if (typeof payload.executeAction !== 'undefined') {
      handleDialog.executeAction = payload.executeAction
    }
  }

  const submitHandleAction = () => {
    const trimmedResult = handleDialog.handleResult.trim()
    if (trimmedResult.length < 10) {
      return {
        ok: false as const,
        message: '处理说明至少需要 10 个字符。',
      }
    }

    const now = '2026-04-15 15:30:00'

    reports.value = sortReports(
      reports.value.map((report) => {
        if (!handleDialog.reportIds.includes(report.id) || report.status !== ReportStatus.PENDING) {
          return report
        }

        const nextStatus =
          handleDialog.action === 'pass' ? ReportStatus.PROCESSED : ReportStatus.REJECTED
        const suffix =
          handleDialog.action === 'pass'
            ? handleDialog.executeAction
              ? '已执行 mock 联动治理动作。'
              : '本次仅更新举报状态，未执行联动动作。'
            : '该举报被判定为证据不足或不成立。'

        return createReportDetail({
          ...report,
          status: nextStatus,
          handleResult: `${trimmedResult} ${suffix}`.trim(),
          handlerId: 1,
          handlerName: '超级管理员',
          handleTime: now,
          updateTime: now,
        })
      }),
    )

    closeHandleDialog()
    clearSelection()
    ensureValidPage()

    return {
      ok: true as const,
      message: '举报状态已更新为 mock 结果。',
    }
  }

  const statusSummaryText = computed(() => {
    const pendingMeta = getReportStatusMeta(ReportStatus.PENDING)
    return `${statistics.value.pending} 条${pendingMeta.label}需要关注`
  })

  return {
    reports: readonly(reports),
    loading: readonly(loading),
    query,
    filteredReports,
    pagedReports: computed<AdminReportListItem[]>(() => pagedReports.value),
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
