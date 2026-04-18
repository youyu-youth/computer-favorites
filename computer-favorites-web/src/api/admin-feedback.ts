import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import { resolveApiErrorMessage } from '@/api/error-code-map'
import type { ApiResult } from '@/api/types'
import type {
  AdminFeedbackDetail,
  AdminFeedbackHandleResult,
  AdminFeedbackPage,
  AdminFeedbackQuery,
} from '@/types/feedback'
import { getJson, putJson } from '@/utils/http'

const ADMIN_FEEDBACK_ERROR_MAP = {
  400: '请求参数不合法，请检查后重试。',
  40401: '反馈记录不存在',
  40402: '已关闭反馈不支持继续回复',
  40403: '该反馈已关闭，请勿重复操作',
  100202: '登录状态已失效，请重新登录。',
  100203: '无权限访问该资源。',
  500: '服务暂时不可用，请稍后重试。',
} as const

function buildFeedbackListQueryString(query: AdminFeedbackQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))
  params.set('hasImages', String(query.hasImages))
  params.set('hasContact', String(query.hasContact))

  if (query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  if (typeof query.status === 'number') {
    params.set('status', String(query.status))
  }

  if (typeof query.type === 'number') {
    params.set('type', String(query.type))
  }

  return params.toString()
}

function resolveApiResultData<T>(result: ApiResult<T> | undefined, fallbackMessage: string): T {
  if (!result) {
    throw new Error(fallbackMessage)
  }

  if (result.code !== 200) {
    throw new Error(result.msg || fallbackMessage)
  }

  if (!result.data) {
    throw new Error(fallbackMessage)
  }

  return result.data
}

function toFeedbackApiError(error: unknown, fallbackMessage: string): never {
  throw new Error(resolveApiErrorMessage(error, ADMIN_FEEDBACK_ERROR_MAP, fallbackMessage))
}

export async function getAdminFeedbackList(query: AdminFeedbackQuery): Promise<AdminFeedbackPage> {
  try {
    const queryString = buildFeedbackListQueryString(query)
    const result = await getJson<ApiResult<AdminFeedbackPage>>(`/api/admin/feedback/list?${queryString}`, {
      headers: buildAdminAuthHeaders(),
    })
    return resolveApiResultData(result, '反馈列表为空')
  } catch (error) {
    toFeedbackApiError(error, '反馈列表加载失败')
  }
}

export async function getAdminFeedbackDetail(feedbackId: number): Promise<AdminFeedbackDetail> {
  try {
    const result = await getJson<ApiResult<AdminFeedbackDetail>>(`/api/admin/feedback/${feedbackId}`, {
      headers: buildAdminAuthHeaders(),
    })
    return resolveApiResultData(result, '反馈详情为空')
  } catch (error) {
    toFeedbackApiError(error, '反馈详情加载失败')
  }
}

export async function replyAdminFeedback(
  feedbackId: number,
  payload: { reply: string },
): Promise<AdminFeedbackHandleResult> {
  try {
    const result = await putJson<ApiResult<AdminFeedbackHandleResult>>(
      `/api/admin/feedback/${feedbackId}/reply`,
      payload,
      {
        headers: buildAdminAuthHeaders(),
      },
    )
    return resolveApiResultData(result, '反馈处理结果为空')
  } catch (error) {
    toFeedbackApiError(error, '处理反馈失败')
  }
}

export async function closeAdminFeedback(feedbackId: number): Promise<AdminFeedbackHandleResult> {
  try {
    const result = await putJson<ApiResult<AdminFeedbackHandleResult>>(
      `/api/admin/feedback/${feedbackId}/close`,
      undefined,
      {
        headers: buildAdminAuthHeaders(),
      },
    )
    return resolveApiResultData(result, '反馈处理结果为空')
  } catch (error) {
    toFeedbackApiError(error, '处理反馈失败')
  }
}
