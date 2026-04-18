import type { ApiResult } from '@/api/types'
import { HttpError } from '@/utils/http'

export type ApiCodeMessageMap = Record<number, string>

function isApiResultBody(value: unknown): value is ApiResult<unknown> {
  return typeof value === 'object' && value !== null && 'code' in value
}

function resolveHttpStatusMessage(status: number): string {
  if (status === 400) {
    return '请求参数不合法，请检查后重试。'
  }
  if (status === 401) {
    return '登录状态已失效，请重新登录。'
  }
  if (status === 403) {
    return '无权限访问该资源。'
  }
  if (status >= 500) {
    return '服务暂时不可用，请稍后重试。'
  }
  return `请求失败（HTTP ${status}）`
}

export function resolveApiErrorMessage(
  error: unknown,
  codeMessageMap: ApiCodeMessageMap,
  fallback: string,
): string {
  if (error instanceof HttpError) {
    if (isApiResultBody(error.body) && typeof error.body.code === 'number') {
      return codeMessageMap[error.body.code] || error.body.msg || fallback
    }
    return resolveHttpStatusMessage(error.status)
  }

  if (error instanceof Error && error.message) {
    return error.message
  }

  return fallback
}
