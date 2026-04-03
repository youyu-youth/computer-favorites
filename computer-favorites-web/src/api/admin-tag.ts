import { deleteJson, getJson, postJson, putJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import type {
  AdminTagBatchDeletePayload,
  AdminTagCreatePayload,
  AdminTagEditPayload,
  AdminTagPage,
  AdminTagPageQuery,
  AdminTagStats,
} from '@/types/admin-tag'

function buildTagPageQueryString(query: AdminTagPageQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))

  if (query.keyword && query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  if (query.sortField) {
    params.set('sortField', query.sortField)
  }

  if (typeof query.sortOrder === 'number') {
    params.set('sortOrder', String(query.sortOrder))
  }

  return params.toString()
}

export async function getAdminTagPage(query: AdminTagPageQuery): Promise<AdminTagPage> {
  const queryString = buildTagPageQueryString(query)
  const res = await getJson<ApiResult<AdminTagPage>>(`/api/admin/tag/list?${queryString}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询标签列表失败')
  }
  if (!res.data) {
    throw new Error('标签列表数据为空')
  }
  return res.data
}

export async function getAdminTagStats(): Promise<AdminTagStats> {
  const res = await getJson<ApiResult<AdminTagStats>>('/api/admin/tag/stats', {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询标签统计失败')
  }
  if (!res.data) {
    throw new Error('标签统计数据为空')
  }
  return res.data
}

export async function createAdminTag(payload: AdminTagCreatePayload): Promise<number> {
  const res = await postJson<ApiResult<number>>('/api/admin/tag', payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '创建标签失败')
  }
  if (typeof res.data !== 'number') {
    throw new Error('创建标签返回数据异常')
  }
  return res.data
}

export async function updateAdminTag(tagId: number, payload: AdminTagEditPayload): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/tag/${tagId}`, payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '编辑标签失败')
  }
}

export async function deleteAdminTag(tagId: number): Promise<void> {
  const res = await deleteJson<ApiResult<unknown>>(`/api/admin/tag/${tagId}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '删除标签失败')
  }
}

export async function batchDeleteAdminTag(payload: AdminTagBatchDeletePayload): Promise<number> {
  const headers = buildAdminAuthHeaders()
  headers.set('Content-Type', 'application/json')

  const res = await deleteJson<ApiResult<number>>('/api/admin/tag/batch', {
    headers,
    body: JSON.stringify(payload),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '批量删除标签失败')
  }
  if (typeof res.data === 'number') {
    return res.data
  }
  return 0
}
