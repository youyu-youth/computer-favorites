import { deleteJson, getJson, postJson, putJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'

export type AdminLoginRequest = {
  username: string
  password: string
  deviceType: string
}

export type AdminLoginUserInfo = {
  userId?: number
  username?: string
  nickname?: string
  avatar?: string
}

export type AdminTokenResponse = {
  accessToken: string
  tokenName?: string
  expireTime?: string
  userInfo?: AdminLoginUserInfo
}

type AdminLoginRawData = {
  tokenValue?: string
  tokenName?: string
  expireTime?: string
  userInfo?: AdminLoginUserInfo
}

export type AdminSessionResponse = {
  userId?: number
  username?: string
  nickname?: string
  avatar?: string
  tokenValue?: string
  deviceType?: string
  timeoutSeconds?: number
  expireTime?: string
}

const parseAdminTokenResponse = (
  res: ApiResult<AdminLoginRawData>,
  fallbackMessage: string,
): AdminTokenResponse => {
  if (res.code !== 200) {
    throw new Error(res.msg || fallbackMessage)
  }

  const accessToken = res.data?.tokenValue
  if (!accessToken) {
    throw new Error('管理员登录响应缺少token')
  }

  return {
    accessToken,
    tokenName: res.data?.tokenName,
    expireTime: res.data?.expireTime,
    userInfo: res.data?.userInfo,
  }
}

export async function adminLogin(req: AdminLoginRequest): Promise<AdminTokenResponse> {
  const res = await postJson<ApiResult<AdminLoginRawData>>('/api/admin/auth/login', req, {
    headers: buildAdminAuthHeaders({ includeToken: false }),
  })
  return parseAdminTokenResponse(res, '管理员登录失败')
}

export async function adminCurrentSession(): Promise<AdminSessionResponse> {
  const res = await getJson<ApiResult<AdminSessionResponse>>('/api/admin/auth/session/current', {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询管理员会话失败')
  }
  if (!res.data) {
    throw new Error('管理员会话为空')
  }
  return res.data
}

export async function adminRenewSession(): Promise<void> {
  const res = await putJson<ApiResult<null>>('/api/admin/auth/session/renew', undefined, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '管理员会话续期失败')
  }
}

export async function adminLogout(): Promise<void> {
  const res = await deleteJson<ApiResult<null>>('/api/admin/auth/session', {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '管理员退出失败')
  }
}
