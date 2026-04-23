import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import type { ApiResult } from '@/api/types'
import type {
  AdminAnnouncementCreatePayload,
  AdminAnnouncementEditPayload,
  AdminAnnouncementPage,
  AdminAnnouncementPageQuery,
  AdminAnnouncementStatusPayload,
} from '@/types/admin-announcement'
import { deleteJson, getJson, postJson, putJson } from '@/utils/http'

function buildAnnouncementPageQueryString(query: AdminAnnouncementPageQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))

  if (query.keyword && query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  if (typeof query.status === 'number') {
    params.set('status', String(query.status))
  }

  if (typeof query.type === 'number') {
    params.set('type', String(query.type))
  }

  if (typeof query.isTop === 'number') {
    params.set('isTop', String(query.isTop))
  }

  return params.toString()
}

export async function getAdminAnnouncementPage(query: AdminAnnouncementPageQuery): Promise<AdminAnnouncementPage> {
  const queryString = buildAnnouncementPageQueryString(query)
  const res = await getJson<ApiResult<AdminAnnouncementPage>>(`/api/admin/announcement/list?${queryString}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询公告列表失败')
  }
  if (!res.data) {
    throw new Error('公告列表数据为空')
  }
  return res.data
}

export async function createAdminAnnouncement(payload: AdminAnnouncementCreatePayload): Promise<number> {
  const res = await postJson<ApiResult<number>>('/api/admin/announcement', payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '创建公告失败')
  }
  if (typeof res.data !== 'number') {
    throw new Error('创建公告返回数据异常')
  }
  return res.data
}

export async function updateAdminAnnouncement(
  id: number,
  payload: AdminAnnouncementEditPayload,
): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/announcement/${id}`, payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '编辑公告失败')
  }
}

export async function updateAdminAnnouncementStatus(
  id: number,
  payload: AdminAnnouncementStatusPayload,
): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/announcement/${id}/status`, payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '更新公告状态失败')
  }
}

export async function deleteAdminAnnouncement(id: number): Promise<void> {
  const res = await deleteJson<ApiResult<unknown>>(`/api/admin/announcement/${id}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '删除公告失败')
  }
}
