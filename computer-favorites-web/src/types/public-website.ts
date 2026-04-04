export interface PublicWebsiteListQuery {
  pageNum: number
  pageSize: number
  categoryId?: number
  keyword?: string
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
  tags: string[]
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
  tags: string[]
  createTime?: string
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
