/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端评论相关类型定义、枚举与状态元数据映射
 */

/** 管理端评论状态：0=隐藏，1=显示 */
export enum AdminCommentStatus {
  HIDDEN = 0,
  VISIBLE = 1,
}

/** 管理端评论操作类型 */
export type AdminCommentAction = 'hide' | 'show'

/** 管理端评论状态标签色 */
export type AdminCommentStatusColor = 'success' | 'error'

/** 管理端评论列表项 */
export interface AdminCommentListItem {
  id: number
  websiteId: number
  websiteName: string
  websiteIcon: string
  userId: number
  userName: string
  userAvatar: string
  content: string
  likeCount: number
  replyCount: number
  reportCount: number
  status: AdminCommentStatus
  createTime: string
  updateTime: string
}

/** 管理端评论详情（含回复列表） */
export interface AdminCommentDetail extends AdminCommentListItem {
  replies: AdminCommentReplyItem[]
  lastAdminAction: string | null
  lastAdminActionTime: string | null
}

/** 管理端评论回复项 */
export interface AdminCommentReplyItem {
  id: number
  userId: number
  userName: string
  userAvatar: string
  content: string
  likeCount: number
  replyTo: string | null
  isDeleted: boolean
  createTime: string
}

/** 管理端评论统计 */
export interface AdminCommentStatistics {
  total: number
  todayNew: number
  visible: number
  hidden: number
}

/** 管理端评论分页结果 */
export interface AdminCommentPage {
  records: AdminCommentListItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

/** 管理端评论查询条件 */
export interface AdminCommentQuery {
  pageNum: number
  pageSize: number
  keyword: string
  status: AdminCommentStatus | null
}

/** 管理端评论操作表单 */
export interface AdminCommentHandleForm {
  action: AdminCommentAction
  reason: string
}

/** 状态元数据 */
export const COMMENT_STATUS_META: Record<
  AdminCommentStatus,
  {
    label: string
    color: AdminCommentStatusColor
    toneClass: string
    icon: string
  }
> = {
  [AdminCommentStatus.HIDDEN]: {
    label: '隐藏',
    color: 'error',
    toneClass:
      'bg-red-50 text-red-700 dark:bg-red-500/10 dark:text-red-200',
    icon: 'fas fa-eye-slash',
  },
  [AdminCommentStatus.VISIBLE]: {
    label: '显示',
    color: 'success',
    toneClass:
      'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-200',
    icon: 'fas fa-eye',
  },
}

export const getCommentStatusMeta = (status: AdminCommentStatus) => COMMENT_STATUS_META[status]
