export interface UserMessageQuery {
  pageNum?: number
  pageSize?: number
  isRead?: 0 | 1
  type?: number
}

export interface UserMessageItem {
  id: number
  title: string
  content: string
  type: number // 1系统通知 2评论回复 3收藏提醒 4审核结果 5举报反馈
  relatedId?: number
  websiteId?: number
  isRead: 0 | 1
  createTime: string
}

export interface UserMessagePage {
  list: UserMessageItem[]
  total: number
  pageNum: number
  pageSize: number
  unreadCount: number
}

export interface UserMessageBatchReadResult {
  successCount: number
  failedCount: number
  failedIds: number[]
}

export interface UserFeedbackCreateRequest {
  type: 1 | 2 | 3 | 4
  content: string
  contact?: string
  images?: string[]
}

export interface UserFeedbackImageUploadResult {
  objectKey: string
  imageUrl: string
}

export interface UserFeedbackQuery {
  pageNum?: number
  pageSize?: number
  status?: 0 | 1 | 2
}

export interface UserFeedbackItem {
  id: number
  type: number
  content: string
  status: 0 | 1 | 2
  reply?: string
  replyTime?: string
  updateTime: string
  createTime: string
}

export interface UserFeedbackPage {
  list: UserFeedbackItem[]
  total: number
  pageNum: number
  pageSize: number
}

export interface UserAnnouncementQuery {
  pageNum?: number
  pageSize?: number
  type?: number
  isTop?: 0 | 1
}

export interface UserAnnouncementItem {
  id: number
  title: string
  content: string
  type: number
  isTop: 0 | 1
  publishTime: string
}

export interface UserAnnouncementPage {
  list: UserAnnouncementItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

export interface UserAnnouncementDetail {
  id: number
  title: string
  content: string
  type: number
  isTop: 0 | 1
  publishTime: string
}
