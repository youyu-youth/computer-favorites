import { deleteJson, getJson, putJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import type {
  AdminUserPage,
  AdminUserPageQuery,
  AdminUserDetail,
  AdminUserStats,
} from '@/types/user'

function buildUserPageQueryString(query: AdminUserPageQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))

  if (query.keyword && query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  if (query.status !== undefined && query.status !== null && query.status !== '') {
    params.set('status', String(query.status))
  }

  return params.toString()
}

export async function getAdminUserPage(query: AdminUserPageQuery): Promise<AdminUserPage> {
  const queryString = buildUserPageQueryString(query)
  const res = await getJson<ApiResult<AdminUserPage>>(`/api/admin/user/list?${queryString}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询用户列表失败')
  }
  if (!res.data) {
    throw new Error('用户列表数据为空')
  }
  return res.data
}

export async function getAdminUserStats(): Promise<AdminUserStats> {
  const res = await getJson<ApiResult<AdminUserStats>>('/api/admin/user/stats', {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询用户统计失败')
  }
  if (!res.data) {
    throw new Error('用户统计数据为空')
  }
  return res.data
}

export async function getAdminUserDetail(id: number): Promise<AdminUserDetail> {
  const res = await getJson<ApiResult<AdminUserDetail>>(`/api/admin/user/${id}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询用户详情失败')
  }
  if (!res.data) {
    throw new Error('用户详情数据为空')
  }
  return res.data
}

export async function updateAdminUserStatus(id: number, status: 0 | 1): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/user/${id}/status`, { status }, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '切换用户状态失败')
  }
}

export async function resetAdminUserPassword(id: number, newPassword: string): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/user/${id}/password/reset`, { newPassword }, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '重置密码失败')
  }
}

export async function kickAdminUserSessions(id: number): Promise<void> {
  const res = await deleteJson<ApiResult<unknown>>(`/api/admin/user/${id}/sessions`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '踢出会话失败')
  }
}
