/**
 * 评论用户信息
 */
export interface CommentUser {
  id: number
  nickname: string
  avatar: string
}

/**
 * 评论回复
 */
export interface CommentReply {
  id: number
  user: CommentUser
  content: string
  createTime: string
  likeCount: number
  isLiked: boolean
  isDeleted: boolean
  replyTo?: string
}

/**
 * 顶级评论
 */
export interface CommentItem {
  id: number
  user: CommentUser
  content: string
  createTime: string
  likeCount: number
  isLiked: boolean
  isDeleted: boolean
  replyCount: number
  replies: CommentReply[]
}

/**
 * 评论分页结果
 */
export interface CommentPageResult {
  records: CommentItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

/**
 * 我的评论条目
 */
export interface MyCommentItem {
  id: number
  websiteId: number
  websiteName: string
  websiteIcon: string
  content: string
  createTime: string
  likeCount: number
  parentId: number
  isDeleted: boolean
}

/**
 * 我的评论分页结果
 */
export interface MyCommentPageResult {
  records: MyCommentItem[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

/**
 * 发布评论参数
 */
export interface CommentCreatePayload {
  content: string
  parentId?: number
  replyUserId?: number
}

/**
 * 评论列表查询参数
 */
export interface CommentPageQuery {
  pageNum: number
  pageSize: number
  sort?: 'time' | 'hot'
}

/**
 * 我的评论查询参数
 */
export interface MyCommentPageQuery {
  pageNum: number
  pageSize: number
}
