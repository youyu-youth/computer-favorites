export interface PublicWebsiteListQuery {
  pageNum: number
  pageSize: number
  categoryId?: number
  keyword?: string
  tagIds?: number[]
}

export type PublicWebsiteTagSortField =
  | 'id'
  | 'name'
  | 'color'
  | 'useCount'
  | 'createTime'
  | 'updateTime'

export interface PublicWebsiteTagQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  sortField?: PublicWebsiteTagSortField
  sortOrder?: 1 | -1
}

export interface PublicWebsiteTagItem {
  id: number
  name: string
  color: string
  useCount: number
}

export interface PublicWebsiteTagRef {
  id: number
  name: string
  color?: string
}

export interface PublicWebsiteTagPage {
  records: PublicWebsiteTagItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

export interface PublicWebsiteListItem {
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
  tags: PublicWebsiteTagRef[]
}

export interface PublicWebsiteDetail {
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
  tags: PublicWebsiteTagRef[]
  isOfficial?: number
  isRecommend?: number
  submitterId?: number
  providerName?: string
  createTime?: string
  shelfTime?: string
  updateTime?: string
}

export interface PublicWebsitePage {
  records: PublicWebsiteListItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

export interface PublicWebsiteCategory {
  id: number
  name: string
  count: number
}
