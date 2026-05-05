import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import type { ApiResult } from '@/api/types'
import { getJson, putJson } from '@/utils/http'
import type {
  AdminCommentDetail,
  AdminCommentHandleForm,
  AdminCommentPage,
  AdminCommentQuery,
  AdminCommentStatistics,
} from '@/types/admin-comment'

export type AdminCommentBatchStatusResult = {
  total: number
  success: number
  skipped: number
  message: string
}

function buildCommentListQueryString(query: AdminCommentQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))

  if (query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  if (typeof query.status === 'number') {
    params.set('status', String(query.status))
  }

  return params.toString()
}

export async function getAdminCommentList(query: AdminCommentQuery): Promise<AdminCommentPage> {
  const queryString = buildCommentListQueryString(query)
  const res = await getJson<ApiResult<AdminCommentPage>>(`/api/admin/comments?${queryString}`, {
    headers: buildAdminAuthHeaders(),
  })

  if (res.code !== 200) {
    throw new Error(res.msg || '查询评论列表失败')
  }

  if (!res.data) {
    throw new Error('评论列表为空')
  }

  return res.data
}

export async function getAdminCommentStatistics(): Promise<AdminCommentStatistics> {
  const res = await getJson<ApiResult<AdminCommentStatistics>>('/api/admin/comments/statistics', {
    headers: buildAdminAuthHeaders(),
  })

  if (res.code !== 200) {
    throw new Error(res.msg || '查询评论统计失败')
  }

  if (!res.data) {
    throw new Error('评论统计为空')
  }

  return res.data
}

export async function getAdminCommentDetail(commentId: number): Promise<AdminCommentDetail> {
  const res = await getJson<ApiResult<AdminCommentDetail>>(`/api/admin/comments/${commentId}`, {
    headers: buildAdminAuthHeaders(),
  })

  if (res.code !== 200) {
    throw new Error(res.msg || '查询评论详情失败')
  }

  if (!res.data) {
    throw new Error('评论详情为空')
  }

  return res.data
}

export async function updateAdminCommentStatus(
  commentId: number,
  form: AdminCommentHandleForm,
): Promise<void> {
  const status = form.action === 'hide' ? 0 : 1
  const res = await putJson<ApiResult<null>>(
    `/api/admin/comments/${commentId}/status`,
    { status, reason: form.reason },
    { headers: buildAdminAuthHeaders() },
  )

  if (res.code !== 200) {
    throw new Error(res.msg || '变更评论状态失败')
  }
}

export async function batchUpdateAdminCommentStatus(
  ids: number[],
  form: AdminCommentHandleForm,
): Promise<AdminCommentBatchStatusResult> {
  const status = form.action === 'hide' ? 0 : 1
  const res = await putJson<ApiResult<AdminCommentBatchStatusResult>>(
    '/api/admin/comments/batch/status',
    { ids, status, reason: form.reason },
    { headers: buildAdminAuthHeaders() },
  )

  if (res.code !== 200) {
    throw new Error(res.msg || '批量变更评论状态失败')
  }

  if (!res.data) {
    throw new Error('批量操作结果为空')
  }

  return res.data
}
