import { deleteJson, getJson, postFormData, postJson, putJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import type {
  AdminWebsiteAuditPayload,
  AdminWebsiteBatchAuditPayload,
  AdminWebsiteBatchAuditResult,
  AdminWebsiteBatchStatusUpdatePayload,
  AdminWebsiteCreatePayload,
  AdminWebsiteCategory,
  AdminWebsiteDetail,
  AdminWebsiteEditPayload,
  AdminWebsiteListQuery,
  AdminWebsiteLogoUploadResult,
  AdminWebsitePage,
  AdminWebsiteStatusUpdatePayload,
  AdminWebsiteStats,
  DeletedFilterValue,
} from '@/types/admin-website'

function buildListQueryString(query: AdminWebsiteListQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))
  params.set('deleted', String(query.deleted))

  if (typeof query.categoryId === 'number' && query.categoryId > 0) {
    params.set('categoryId', String(query.categoryId))
  }

  if (typeof query.auditStatus === 'number') {
    params.set('auditStatus', String(query.auditStatus))
  }

  if (query.keyword && query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  return params.toString()
}

export async function getAdminWebsitePage(query: AdminWebsiteListQuery): Promise<AdminWebsitePage> {
  const queryString = buildListQueryString(query)
  const res = await getJson<ApiResult<AdminWebsitePage>>(`/api/admin/website/list?${queryString}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询网站列表失败')
  }
  if (!res.data) {
    throw new Error('网站列表为空')
  }
  return res.data
}

export async function getAdminWebsiteDetail(websiteId: number): Promise<AdminWebsiteDetail> {
  const res = await getJson<ApiResult<AdminWebsiteDetail>>(`/api/admin/website/${websiteId}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询网站详情失败')
  }
  if (!res.data) {
    throw new Error('网站详情为空')
  }
  return res.data
}

export async function getAdminWebsiteCategories(
  deleted: DeletedFilterValue,
): Promise<AdminWebsiteCategory[]> {
  const res = await getJson<ApiResult<AdminWebsiteCategory[]>>(
    `/api/admin/website/categories?deleted=${deleted}`,
    {
      headers: buildAdminAuthHeaders(),
    },
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '查询分类统计失败')
  }
  return res.data || []
}

export async function getAdminWebsiteStats(
  deleted: DeletedFilterValue,
): Promise<AdminWebsiteStats> {
  const res = await getJson<ApiResult<AdminWebsiteStats>>(
    `/api/admin/website/stats?deleted=${deleted}`,
    {
      headers: buildAdminAuthHeaders(),
    },
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '查询网站统计失败')
  }
  if (!res.data) {
    throw new Error('网站统计数据为空')
  }
  return res.data
}

export async function uploadAdminWebsiteLogo(file: File): Promise<AdminWebsiteLogoUploadResult> {
  const formData = new FormData()
  formData.append('file', file)

  const res = await postFormData<ApiResult<AdminWebsiteLogoUploadResult>>(
    '/api/admin/website/logo',
    formData,
    {
      headers: buildAdminAuthHeaders(),
    },
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '上传 Logo 失败')
  }
  if (!res.data) {
    throw new Error('上传结果为空')
  }
  return res.data
}

export async function createAdminWebsite(payload: AdminWebsiteCreatePayload): Promise<number> {
  const res = await postJson<ApiResult<number>>('/api/admin/website', payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '添加网站失败')
  }
  if (typeof res.data !== 'number') {
    throw new Error('添加网站返回数据异常')
  }
  return res.data
}

export async function updateAdminWebsite(
  websiteId: number,
  payload: AdminWebsiteEditPayload,
): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/website/${websiteId}`, payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '修改网站失败')
  }
}

export async function deleteAdminWebsite(websiteId: number): Promise<void> {
  const res = await deleteJson<ApiResult<unknown>>(`/api/admin/website/${websiteId}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '删除网站失败')
  }
}

export async function deleteAdminWebsiteLogo(objectKey: string): Promise<void> {
  const encodedObjectKey = encodeURIComponent(objectKey)
  const res = await deleteJson<ApiResult<unknown>>(
    `/api/admin/website/logo?objectKey=${encodedObjectKey}`,
    {
      headers: buildAdminAuthHeaders(),
    },
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '删除 Logo 失败')
  }
}

export async function updateAdminWebsiteStatus(
  payload: AdminWebsiteStatusUpdatePayload,
): Promise<void> {
  const res = await putJson<ApiResult<unknown>>('/api/admin/website/status', payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '更新网站状态失败')
  }
}

export async function batchUpdateAdminWebsiteStatus(
  payload: AdminWebsiteBatchStatusUpdatePayload,
): Promise<number> {
  const res = await putJson<ApiResult<number>>('/api/admin/website/status/batch', payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '批量更新网站状态失败')
  }
  if (typeof res.data === 'number') {
    return res.data
  }
  return 0
}

export async function auditAdminWebsite(
  websiteId: number,
  payload: AdminWebsiteAuditPayload,
): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/website/${websiteId}/audit`, payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '网站审核失败')
  }
}

export async function batchAuditAdminWebsite(
  payload: AdminWebsiteBatchAuditPayload,
): Promise<AdminWebsiteBatchAuditResult> {
  const res = await putJson<ApiResult<AdminWebsiteBatchAuditResult>>(
    '/api/admin/website/audit/batch',
    payload,
    {
      headers: buildAdminAuthHeaders(),
    },
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '批量审核失败')
  }
  if (!res.data) {
    throw new Error('批量审核结果为空')
  }
  return res.data
}
