import { deleteJson, getJson, postJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type {
  CommentPageResult,
  CommentCreatePayload,
  CommentPageQuery,
  MyCommentPageResult,
  MyCommentPageQuery,
} from '@/types/comment'

function buildCommentPageQueryString(query: CommentPageQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))
  if (query.sort) {
    params.set('sort', query.sort)
  }
  return params.toString()
}

function buildMyCommentPageQueryString(query: MyCommentPageQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))
  return params.toString()
}

/**
 * 发布评论
 */
export async function publishComment(
  websiteId: number,
  payload: CommentCreatePayload,
): Promise<number> {
  const res = await postJson<ApiResult<number>>(
    `/api/user/website/${websiteId}/comments`,
    payload,
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '评论发布失败')
  }
  return res.data ?? 0
}

/**
 * 获取评论列表（两级折叠）
 */
export async function getCommentPage(
  websiteId: number,
  query: CommentPageQuery,
): Promise<CommentPageResult> {
  const queryString = buildCommentPageQueryString(query)
  const res = await getJson<ApiResult<CommentPageResult>>(
    `/api/user/website/${websiteId}/comments?${queryString}`,
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '查询评论列表失败')
  }
  if (!res.data) {
    return { records: [], total: 0, pageNum: query.pageNum, pageSize: query.pageSize, totalPages: 0 }
  }
  return res.data
}

/**
 * 删除评论
 */
export async function deleteComment(commentId: number): Promise<void> {
  const res = await deleteJson<ApiResult<null>>(`/api/user/comment/${commentId}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '删除评论失败')
  }
}

/**
 * 点赞评论
 */
export async function likeComment(commentId: number): Promise<void> {
  const res = await postJson<ApiResult<null>>(`/api/user/comment/${commentId}/like`)
  if (res.code !== 200) {
    throw new Error(res.msg || '点赞失败')
  }
}

/**
 * 取消点赞评论
 */
export async function unlikeComment(commentId: number): Promise<void> {
  const res = await deleteJson<ApiResult<null>>(`/api/user/comment/${commentId}/like`)
  if (res.code !== 200) {
    throw new Error(res.msg || '取消点赞失败')
  }
}

/**
 * 获取我的评论列表
 */
export async function getMyCommentPage(
  query: MyCommentPageQuery,
): Promise<MyCommentPageResult> {
  const queryString = buildMyCommentPageQueryString(query)
  const res = await getJson<ApiResult<MyCommentPageResult>>(
    `/api/user/my/comments?${queryString}`,
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '查询我的评论失败')
  }
  if (!res.data) {
    return { records: [], total: 0, pageNum: query.pageNum, pageSize: query.pageSize, totalPages: 0 }
  }
  return res.data
}
