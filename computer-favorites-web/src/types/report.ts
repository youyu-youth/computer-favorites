/**
 * 举报类型枚举
 */
export enum ReportType {
  WEBSITE = 1,
  COMMENT = 2,
}

/**
 * 举报状态枚举
 */
export enum ReportStatus {
  PENDING = 0,
  PROCESSED = 1,
  REJECTED = 2,
}

/**
 * 举报表单数据
 */
export interface ReportFormData {
  type: ReportType
  targetId: number
  reason: string
  images: string[]
}

/**
 * 举报原因选项
 */
export interface ReportReasonOption {
  value: number
  label: string
  description: string
}

/**
 * 举报记录
 */
export interface ReportRecord {
  id: number
  userId: number
  type: ReportType
  targetId: number
  reason: string
  images: string[] | null
  status: ReportStatus
  handleResult: string | null
  handlerId: number | null
  handleTime: string | null
  createTime: string
  updateTime: string
}

/**
 * 管理端举报处置动作
 */
export type AdminReportHandleAction = 'pass' | 'reject'

/**
 * 管理端举报状态标签色
 */
export type AdminReportStatusColor = 'warn' | 'success' | 'error'

/**
 * 管理端举报目标类型标签
 */
export type AdminReportTargetTypeLabel = '网站' | '评论'

/**
 * 管理端举报列表项
 */
export interface AdminReportListItem {
  id: number
  userId: number
  userName: string
  userEmail: string
  type: ReportType
  targetId: number
  targetName: string
  targetUrl: string | null
  uploaderId: number | null
  uploaderName: string | null
  reason: string
  images: string[]
  status: ReportStatus
  handleResult: string | null
  handlerId: number | null
  handlerName: string | null
  handleTime: string | null
  createTime: string
  updateTime: string
}

/**
 * 管理端举报详情
 */
export interface AdminReportDetail extends AdminReportListItem {
  targetStatusLabel: string
  evidenceSummary: string
  timeline: Array<{
    id: string
    title: string
    description: string
    time: string
    tone: 'pending' | 'done'
  }>
}

/**
 * 管理端举报统计
 */
export interface AdminReportStatistics {
  total: number
  pending: number
  processed: number
  rejected: number
  last24Hours: number
  websiteCount: number
  commentCount: number
  processRate: number
}

/**
 * 管理端举报分页结果
 */
export interface AdminReportPage {
  records: AdminReportListItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

/**
 * 管理端举报查询条件
 */
export interface AdminReportQuery {
  pageNum: number
  pageSize: number
  keyword: string
  status: ReportStatus | null
  type: ReportType | null
  pendingOnly: boolean
}

/**
 * 管理端举报处置表单
 */
export interface AdminReportHandleForm {
  action: AdminReportHandleAction
  handleResult: string
  executeAction: boolean
}

/**
 * 管理端单条举报处置结果
 */
export interface AdminReportHandleResult {
  reportId: number
  status: ReportStatus
  handleResult: string
  handleTime: string
  actionExecuted: boolean
}

/**
 * 管理端批量处置结果明细
 */
export interface AdminReportBatchHandleResultItem {
  reportId: number
  success: boolean
  message: string
}

/**
 * 管理端批量举报处置结果
 */
export interface AdminReportBatchHandleResult {
  total: number
  success: number
  failed: number
  results: AdminReportBatchHandleResultItem[]
}

/**
 * 举报状态映射
 */
export const REPORT_STATUS_META = {
  [ReportStatus.PENDING]: {
    label: '待处理',
    color: 'warn',
    shortLabel: '待处理',
  },
  [ReportStatus.PROCESSED]: {
    label: '已处理',
    color: 'success',
    shortLabel: '已通过',
  },
  [ReportStatus.REJECTED]: {
    label: '已驳回',
    color: 'error',
    shortLabel: '已驳回',
  },
} as const satisfies Record<
  ReportStatus,
  {
    label: string
    color: AdminReportStatusColor
    shortLabel: string
  }
>

/**
 * 举报类型映射
 */
export const REPORT_TYPE_META = {
  [ReportType.WEBSITE]: {
    label: '网站举报',
    shortLabel: '网站',
    icon: 'fas fa-globe',
    targetTypeLabel: '网站',
  },
  [ReportType.COMMENT]: {
    label: '评论举报',
    shortLabel: '评论',
    icon: 'fas fa-comments',
    targetTypeLabel: '评论',
  },
} as const satisfies Record<
  ReportType,
  {
    label: string
    shortLabel: string
    icon: string
    targetTypeLabel: AdminReportTargetTypeLabel
  }
>

export const REPORT_HANDLE_ACTION_OPTIONS: Array<{
  value: AdminReportHandleAction
  label: string
  description: string
}> = [
  {
    value: 'pass',
    label: '通过举报',
    description: '确认举报有效，标记为已处理，并可联动执行下架或隐藏动作。',
  },
  {
    value: 'reject',
    label: '驳回举报',
    description: '确认举报不成立，保留记录并标记为已驳回。',
  },
]

export const getReportStatusMeta = (status: ReportStatus) => REPORT_STATUS_META[status]

export const getReportTypeMeta = (type: ReportType) => REPORT_TYPE_META[type]
