import type { PublicWebsiteTagRef } from '@/types/public-website'

export type UserWebsiteSubmissionAuditStatus = 0 | 1 | 2

export interface UserWebsiteSubmissionCreatePayload {
  name: string
  url: string
  icon: string
  githubUrl?: string
  summary: string
  description?: string
  categoryId: number
  tags?: string
}

export interface UserWebsiteSubmissionIconUploadResult {
  objectKey: string
  iconUrl: string
}

export interface UserWebsiteSubmissionQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  auditStatus?: UserWebsiteSubmissionAuditStatus
}

export interface UserWebsiteSubmissionListItem {
  id: number
  name: string
  url: string
  icon?: string
  summary?: string
  categoryId?: number
  categoryName?: string
  tags?: PublicWebsiteTagRef[]
  auditStatus: UserWebsiteSubmissionAuditStatus
  auditRemark?: string
  status: number
  updateTime?: string
}

export interface UserWebsiteSubmissionPage {
  records: UserWebsiteSubmissionListItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

export interface UserWebsiteSubmissionDetail {
  id: number
  name: string
  url: string
  icon?: string
  githubUrl?: string
  summary?: string
  description?: string
  categoryId?: number
  categoryName?: string
  tags?: PublicWebsiteTagRef[]
  auditStatus: UserWebsiteSubmissionAuditStatus
  auditRemark?: string
  status: number
  createTime?: string
  updateTime?: string
}
