/**
 * 用户反馈类型枚举
 */
export enum FeedbackType {
  SUGGESTION = 1,
  BUG = 2,
  COMPLAINT = 3,
  EXPERIENCE = 4,
}

/**
 * 用户反馈状态枚举
 */
export enum FeedbackStatus {
  PENDING = 0,
  PROCESSED = 1,
  CLOSED = 2,
}

/**
 * 管理端反馈处理动作
 */
export type AdminFeedbackHandleAction = 'reply' | 'close'

/**
 * 管理端反馈状态标签色
 */
export type AdminFeedbackStatusColor = 'warn' | 'success' | 'neutral'

/**
 * 管理端反馈列表项
 */
export interface AdminFeedbackListItem {
  id: number
  userId: number | null
  userName: string
  userEmail: string | null
  avatar: string
  contact: string | null
  type: FeedbackType
  content: string
  images: string[]
  status: FeedbackStatus
  createTime: string
  updateTime: string
}

/**
 * 管理端反馈详情时间线节点
 */
export interface AdminFeedbackTimelineItem {
  id: string
  title: string
  description: string
  time: string
  tone: 'pending' | 'done'
}

/**
 * 管理端反馈详情
 */
export interface AdminFeedbackDetail extends AdminFeedbackListItem {
  reply: string | null
  replyTime: string | null
  summaryText: string
  timeline: AdminFeedbackTimelineItem[]
}

/**
 * 管理端反馈统计
 */
export interface AdminFeedbackStatistics {
  total: number
  pending: number
  processed: number
  closed: number
  last24Hours: number
  bugCount: number
  withImagesCount: number
  withContactCount: number
  replyRate: number
}

/**
 * 管理端反馈分页结果
 */
export interface AdminFeedbackPage {
  records: AdminFeedbackListItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
  statistics: AdminFeedbackStatistics
}

/**
 * 管理端反馈查询条件
 */
export interface AdminFeedbackQuery {
  pageNum: number
  pageSize: number
  keyword: string
  status: FeedbackStatus | null
  type: FeedbackType | null
  hasImages: boolean
  hasContact: boolean
}

/**
 * 管理端反馈处理表单
 */
export interface AdminFeedbackHandleForm {
  action: AdminFeedbackHandleAction
  reply: string
}

/**
 * 管理端反馈处理结果
 */
export interface AdminFeedbackHandleResult {
  feedbackId: number
  status: FeedbackStatus
  reply: string | null
  replyTime: string | null
  updateTime: string
}

/**
 * 反馈状态映射
 */
export const FEEDBACK_STATUS_META = {
  [FeedbackStatus.PENDING]: {
    label: '待处理',
    color: 'warn',
    shortLabel: '待处理',
  },
  [FeedbackStatus.PROCESSED]: {
    label: '已处理',
    color: 'success',
    shortLabel: '已回复',
  },
  [FeedbackStatus.CLOSED]: {
    label: '已关闭',
    color: 'neutral',
    shortLabel: '已关闭',
  },
} as const satisfies Record<
  FeedbackStatus,
  {
    label: string
    color: AdminFeedbackStatusColor
    shortLabel: string
  }
>

/**
 * 反馈类型映射
 */
export const FEEDBACK_TYPE_META = {
  [FeedbackType.SUGGESTION]: {
    label: '建议',
    shortLabel: '建议',
    icon: 'fas fa-lightbulb',
    helper: '产品体验与功能建议',
    toneClass:
      'bg-amber-50 text-amber-700 dark:bg-amber-500/10 dark:text-amber-200',
  },
  [FeedbackType.BUG]: {
    label: 'Bug 反馈',
    shortLabel: 'Bug',
    icon: 'fas fa-bug',
    helper: '功能异常、报错或兼容问题',
    toneClass:
      'bg-red-50 text-red-700 dark:bg-red-500/10 dark:text-red-200',
  },
  [FeedbackType.COMPLAINT]: {
    label: '投诉',
    shortLabel: '投诉',
    icon: 'fas fa-triangle-exclamation',
    helper: '对服务流程或内容生态不满',
    toneClass:
      'bg-sky-50 text-sky-700 dark:bg-sky-500/10 dark:text-sky-200',
  },
  [FeedbackType.EXPERIENCE]: {
    label: '使用感受',
    shortLabel: '感受',
    icon: 'fas fa-face-smile',
    helper: '对当前版本的体验评价',
    toneClass:
      'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-200',
  },
} as const satisfies Record<
  FeedbackType,
  {
    label: string
    shortLabel: string
    icon: string
    helper: string
    toneClass: string
  }
>

/**
 * 反馈处理动作选项
 */
export const FEEDBACK_HANDLE_ACTION_OPTIONS: Array<{
  value: AdminFeedbackHandleAction
  label: string
  description: string
}> = [
  {
    value: 'reply',
    label: '回复反馈',
    description: '填写清晰回复内容，并将工单状态更新为已处理。',
  },
  {
    value: 'close',
    label: '关闭反馈',
    description: '当工单无需继续跟进时关闭，关闭后不再允许继续回复。',
  },
]

export const getFeedbackStatusMeta = (status: FeedbackStatus) => FEEDBACK_STATUS_META[status]

export const getFeedbackTypeMeta = (type: FeedbackType) => FEEDBACK_TYPE_META[type]
