/** 点赞状态 */
export interface LikeStatus {
  isLiked: boolean
}

/** 我的评分 */
export interface MyScore {
  score: number | null
}

/** 评分提交参数 */
export interface ScorePayload {
  score: number
}

/** 我点赞的网站条目 */
export interface LikedWebsiteItem {
  id: number
  websiteId: number
  websiteName: string
  websiteUrl: string
  websiteIcon: string
  websiteSummary: string
  websiteTags: Array<{ id: number; name: string; color: string }>
  likeCount: number
  score: number
  likeTime: string
}

/** 我点赞的网站分页结果 */
export interface LikedWebsitePage {
  records: LikedWebsiteItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

/** 我点赞的网站分页查询参数 */
export interface LikedWebsitePageQuery {
  pageNum: number
  pageSize: number
}
