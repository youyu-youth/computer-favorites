import { deleteJson, getJson, postJson, putJson } from '@/utils/http'

export type LoginRequest = {
  username: string
  password: string
  deviceType: string
}

export type LoginByEmailCodeRequest = {
  email: string
  emailCode: string
  deviceType: string
}

export type RegisterRequest = {
  username: string
  email: string
  password: string
  emailCode: string
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

const parseAuthTokenResponse = (res: ApiResult<LoginRawData>, fallbackMessage: string): AuthTokenResponse => {
  if (res.code !== 200) {
    throw new Error(res.msg || fallbackMessage)
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

export async function login(req: LoginRequest): Promise<AuthTokenResponse> {
  const res = await postJson<ApiResult<LoginRawData>>('/api/auth/login', req)
  return parseAuthTokenResponse(res, '登录失败')
}

export async function loginByEmailCode(req: LoginByEmailCodeRequest): Promise<AuthTokenResponse> {
  const res = await postJson<ApiResult<LoginRawData>>('/api/auth/login/code', req)
  return parseAuthTokenResponse(res, '验证码登录失败')
}

export async function register(req: RegisterRequest): Promise<unknown> {
  const res = await postJson<ApiResult<null>>('/api/auth/register', req)
  if (res.code !== 200) {
    throw new Error(res.msg || '注册失败')
  }
  return res.data
}

export async function sendRegisterCode(email: string): Promise<void> {
  const res = await postJson<ApiResult<null>>('/api/auth/register/code', { email })
  if (res.code !== 200) {
    throw new Error(res.msg || '验证码发送失败')
  }
}

export async function sendLoginCode(email: string): Promise<void> {
  const res = await postJson<ApiResult<null>>('/api/auth/login/code/send', { email })
  if (res.code !== 200) {
    throw new Error(res.msg || '验证码发送失败')
  }
}

export async function checkUsernameAvailable(username: string): Promise<boolean> {
  const query = encodeURIComponent(username)
  const res = await getJson<ApiResult<boolean>>(`/api/auth/register/username/check?username=${query}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '用户名校验失败')
  }
  return Boolean(res.data)
}

export async function logout(): Promise<void> {
  const res = await deleteJson<ApiResult<null>>('/api/auth/session')
  if (res.code !== 200) {
    throw new Error(res.msg || '退出失败')
  }
}

export async function renewSession(): Promise<void> {
  const res = await putJson<ApiResult<null>>('/api/auth/session/renew')
  if (res.code !== 200) {
    throw new Error(res.msg || '续期失败')
  }
}
