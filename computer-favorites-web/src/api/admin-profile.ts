import { getJson, putJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import type { AdminProfile, UpdateAdminProfileRequest } from '@/types/admin'

export async function getAdminProfile(): Promise<AdminProfile> {
  const res = await getJson<ApiResult<AdminProfile>>('/api/admin/profile/current', {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询管理员资料失败')
  }
  if (!res.data) {
    throw new Error('管理员资料为空')
  }
  return res.data
}

export async function updateAdminProfile(payload: UpdateAdminProfileRequest): Promise<void> {
  const res = await putJson<ApiResult<null>>('/api/admin/profile', payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '保存管理员资料失败')
  }
}
