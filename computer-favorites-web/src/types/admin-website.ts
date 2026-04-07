export type DeletedFilterValue = -1 | 0 | 1

export type AdminWebsiteStatusValue = 0 | 1

export type AdminWebsiteAuditStatusValue = 0 | 1 | 2

export type AdminWebsiteAuditActionValue = 1 | 2

export interface AdminWebsiteListQuery {
  pageNum: number
  pageSize: number
  deleted: DeletedFilterValue
  categoryId?: number
  auditStatus?: AdminWebsiteAuditStatusValue
  keyword?: string
}

export interface AdminWebsiteListItem {
  id: number
  name: string
  url: string
  icon?: string
  summary?: string
  description?: string
  categoryId: number
  categoryName?: string
  clickCount: number
  likeCount: number
  collectCount: number
  commentCount: number
  score: number
  tags?: string
  isTop: number
  isRecommend: number
  status: AdminWebsiteStatusValue
  source: number
  auditStatus: number
  deleted: number
  updateTime?: string
}

export interface AdminWebsiteDetail {
  id: number
  name: string
  url: string
  icon?: string
  summary?: string
  description?: string
  categoryId?: number
  categoryName?: string
  clickCount: number
  likeCount: number
  collectCount: number
  commentCount: number
  score: number
  scoreCount: number
  tags?: string
  isTop: number
  isRecommend: number
  status: AdminWebsiteStatusValue
  sort: number
  source: number
  submitterId?: number
  auditStatus: number
  auditRemark?: string
  createTime?: string
  updateTime?: string
  deleted: number
  shelfTime?: string
  takedownTime?: string
  auditAdminId?: number
}

export interface AdminWebsitePage {
  records: AdminWebsiteListItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

export interface AdminWebsiteCategory {
  id: number
  name: string
  count: number
}

export interface AdminWebsiteStats {
  total: number
  online: number
  offline: number
  pendingAudit: number
  rejectedAudit: number
  deleted: number
  latestUpdateTime?: string
}

export interface AdminWebsiteLogoUploadResult {
  objectKey: string
  logoUrl: string
}

export interface AdminWebsiteCreatePayload {
  name: string
  url: string
  icon?: string
  summary?: string
  description?: string
  categoryId: number
  tags?: string
  isTop: boolean
  isRecommend: boolean
  sort: number
}

export interface AdminWebsiteEditPayload {
  name: string
  url: string
  icon?: string
  summary?: string
  description?: string
  categoryId: number
  tags?: string
  isTop: boolean
  isRecommend: boolean
  sort: number
}

export interface AdminWebsiteStatusUpdatePayload {
  websiteId: number
  status: AdminWebsiteStatusValue
}

export interface AdminWebsiteBatchStatusUpdatePayload {
  websiteIds: number[]
  status: AdminWebsiteStatusValue
}

export interface AdminWebsiteAuditPayload {
  action: AdminWebsiteAuditActionValue
  remark?: string
}

export interface AdminWebsiteBatchAuditPayload {
  websiteIds: number[]
  action: AdminWebsiteAuditActionValue
  remark?: string
}

export interface AdminWebsiteBatchAuditFailItem {
  websiteId: number
  reason: string
}

export interface AdminWebsiteBatchAuditResult {
  successCount: number
  failedCount: number
  failItems: AdminWebsiteBatchAuditFailItem[]
}
