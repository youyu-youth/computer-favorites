import { deleteJson, getJson, postFormData, postJson, putJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import type {
  AdminTechStackBatchDeletePayload,
  AdminTechStackCreatePayload,
  AdminTechStackEditPayload,
  AdminTechStackIconUploadResult,
  AdminTechStackPage,
  AdminTechStackPageQuery,
  AdminTechStackStats,
} from '@/types/tech-stack'

function buildTechStackPageQueryString(query: AdminTechStackPageQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))

  if (query.keyword && query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  if (typeof query.status === 'number') {
    params.set('status', String(query.status))
  }

  if (query.sortField) {
    params.set('sortField', query.sortField)
  }

  if (typeof query.sortOrder === 'number') {
    params.set('sortOrder', String(query.sortOrder))
  }

  return params.toString()
}

export async function getAdminTechStackPage(query: AdminTechStackPageQuery): Promise<AdminTechStackPage> {
  const queryString = buildTechStackPageQueryString(query)
  const res = await getJson<ApiResult<AdminTechStackPage>>(`/api/admin/tech-stack/list?${queryString}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询技术栈列表失败')
  }
  if (!res.data) {
    throw new Error('技术栈列表数据为空')
  }
  return res.data
}

export async function getAdminTechStackStats(): Promise<AdminTechStackStats> {
  const res = await getJson<ApiResult<AdminTechStackStats>>('/api/admin/tech-stack/stats', {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询技术栈统计失败')
  }
  if (!res.data) {
    throw new Error('技术栈统计数据为空')
  }
  return res.data
}

export async function createAdminTechStack(payload: AdminTechStackCreatePayload): Promise<number> {
  const res = await postJson<ApiResult<number>>('/api/admin/tech-stack', payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '创建技术栈失败')
  }
  if (typeof res.data !== 'number') {
    throw new Error('创建技术栈返回数据异常')
  }
  return res.data
}

export async function updateAdminTechStack(id: number, payload: AdminTechStackEditPayload): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/tech-stack/${id}`, payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '编辑技术栈失败')
  }
}

export async function updateAdminTechStackStatus(id: number, status: number): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/tech-stack/${id}/status`, { status }, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '更新技术栈状态失败')
  }
}

export async function deleteAdminTechStack(id: number): Promise<void> {
  const res = await deleteJson<ApiResult<unknown>>(`/api/admin/tech-stack/${id}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '删除技术栈失败')
  }
}

export async function batchDeleteAdminTechStack(payload: AdminTechStackBatchDeletePayload): Promise<number> {
  const headers = buildAdminAuthHeaders()
  headers.set('Content-Type', 'application/json')

  const res = await deleteJson<ApiResult<number>>('/api/admin/tech-stack/batch', {
    headers,
    body: JSON.stringify(payload),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '批量删除技术栈失败')
  }
  if (typeof res.data === 'number') {
    return res.data
  }
  return 0
}

export async function uploadAdminTechStackIcon(file: File): Promise<AdminTechStackIconUploadResult> {
  const formData = new FormData()
  formData.append('file', file)

  const res = await postFormData<ApiResult<AdminTechStackIconUploadResult>>(
    '/api/admin/tech-stack/icon',
    formData,
    { headers: buildAdminAuthHeaders() },
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '上传技术栈图标失败')
  }
  if (!res.data) {
    throw new Error('上传技术栈图标返回数据为空')
  }
  return res.data
}

export async function deleteAdminTechStackIcon(objectKey: string): Promise<void> {
  const encodedObjectKey = encodeURIComponent(objectKey)
  const res = await deleteJson<ApiResult<unknown>>(
    `/api/admin/tech-stack/icon?objectKey=${encodedObjectKey}`,
    { headers: buildAdminAuthHeaders() },
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '删除技术栈图标失败')
  }
}
