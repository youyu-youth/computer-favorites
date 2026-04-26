import { postJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'

export async function verifyPassword(password: string): Promise<boolean> {
  const res = await postJson<ApiResult<boolean>>('/api/user/folder/verify-password', { password })
  if (res.code === 200) {
    return res.data === true
  }
  return false
}
