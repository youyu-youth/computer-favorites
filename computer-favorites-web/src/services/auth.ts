import { postJson } from '@/utils/http'

export type LoginRequest = {
  username: string
  password: string
  deviceType: string
}

export type RegisterRequest = {
  email: string
  password: string
}

export type AuthTokenResponse = {
  accessToken: string
  tokenName?: string
  expireTime?: string
}

type ApiResult<T> = {
  code: number
  msg: string
  data?: T
}

type LoginRawData = {
  tokenValue?: string
  tokenName?: string
  expireTime?: string
}

export async function login(req: LoginRequest): Promise<AuthTokenResponse> {
  const res = await postJson<ApiResult<LoginRawData>>('/api/auth/login', req)
  if (res.code !== 200) {
    throw new Error(res.msg || '登录失败')
  }

  const accessToken = res.data?.tokenValue
  if (!accessToken) {
    throw new Error('登录响应缺少token')
  }

  return {
    accessToken,
    tokenName: res.data?.tokenName,
    expireTime: res.data?.expireTime,
  }
}

export async function register(req: RegisterRequest): Promise<unknown> {
  return postJson('/api/auth/register', req)
}
