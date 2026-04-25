import { deleteJson, getJson, postFormData, postJson, putJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type {
  UserAnnouncementDetail,
  UserAnnouncementPage,
  UserAnnouncementQuery,
  UserFeedbackCreateRequest,
  UserFeedbackImageUploadResult,
  UserFeedbackPage,
  UserFeedbackQuery,
  UserMessageBatchReadResult,
  UserMessagePage,
  UserMessageQuery,
} from '@/types/notification'

function assertSuccess<T>(response: ApiResult<T>, fallback: string): T {
  if (response.code !== 200) {
    throw new Error(response.msg || fallback)
  }
  if (response.data === undefined || response.data === null) {
    throw new Error(fallback)
  }
  return response.data
}

function buildMessageQueryString(query: UserMessageQuery): string {
  const params = new URLSearchParams()

  if (typeof query.pageNum === 'number' && query.pageNum > 0) {
    params.set('pageNum', String(query.pageNum))
  }
  if (typeof query.pageSize === 'number' && query.pageSize > 0) {
    params.set('pageSize', String(query.pageSize))
  }
  if (query.isRead === 0 || query.isRead === 1) {
    params.set('isRead', String(query.isRead))
  }
  if (typeof query.type === 'number') {
    params.set('type', String(query.type))
  }

  return params.toString()
}

function appendQuery(path: string, queryString: string): string {
  if (!queryString) {
    return path
  }
  return `${path}?${queryString}`
}

function buildFeedbackQueryString(query: UserFeedbackQuery): string {
  const params = new URLSearchParams()

  if (typeof query.pageNum === 'number' && query.pageNum > 0) {
    params.set('pageNum', String(query.pageNum))
  }
  if (typeof query.pageSize === 'number' && query.pageSize > 0) {
    params.set('pageSize', String(query.pageSize))
  }
  if (query.status === 0 || query.status === 1 || query.status === 2) {
    params.set('status', String(query.status))
  }

  return params.toString()
}

function buildAnnouncementQueryString(query: UserAnnouncementQuery): string {
  const params = new URLSearchParams()

  if (typeof query.pageNum === 'number' && query.pageNum > 0) {
    params.set('pageNum', String(query.pageNum))
  }
  if (typeof query.pageSize === 'number' && query.pageSize > 0) {
    params.set('pageSize', String(query.pageSize))
  }
  if (typeof query.type === 'number') {
    params.set('type', String(query.type))
  }
  if (query.isTop === 0 || query.isTop === 1) {
    params.set('isTop', String(query.isTop))
  }

  return params.toString()
}

export async function getUserMessagePage(query: UserMessageQuery): Promise<UserMessagePage> {
  const queryString = buildMessageQueryString(query)
  const response = await getJson<ApiResult<UserMessagePage>>(appendQuery('/api/user/messages', queryString))
  return assertSuccess(response, '查询消息列表失败')
}

export async function markUserMessageRead(id: number): Promise<void> {
  const response = await putJson<ApiResult<null>>(`/api/user/messages/${id}/read`)
  if (response.code !== 200) {
    throw new Error(response.msg || '标记消息已读失败')
  }
}

export async function batchReadUserMessages(ids: number[]): Promise<UserMessageBatchReadResult> {
  const response = await putJson<ApiResult<UserMessageBatchReadResult>>('/api/user/messages/read/batch', {
    ids,
  })
  return assertSuccess(response, '批量标记消息已读失败')
}

export async function createUserFeedback(payload: UserFeedbackCreateRequest): Promise<void> {
  const response = await postJson<ApiResult<null>>('/api/user/feedbacks', payload)
  if (response.code !== 200) {
    throw new Error(response.msg || '提交反馈失败')
  }
}

export async function uploadUserFeedbackImage(file: File): Promise<UserFeedbackImageUploadResult> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await postFormData<ApiResult<UserFeedbackImageUploadResult>>(
    '/api/user/feedbacks/images',
    formData,
  )
  return assertSuccess(response, '反馈图片上传失败')
}

export async function deleteUserFeedbackImage(objectKey: string): Promise<void> {
  const response = await deleteJson<ApiResult<null>>(
    `/api/user/feedbacks/images?objectKey=${encodeURIComponent(objectKey)}`,
  )
  if (response.code !== 200) {
    throw new Error(response.msg || '反馈图片删除失败')
  }
}

export async function getMyFeedbackPage(query: UserFeedbackQuery): Promise<UserFeedbackPage> {
  const queryString = buildFeedbackQueryString(query)
  const response = await getJson<ApiResult<UserFeedbackPage>>(appendQuery('/api/user/feedbacks/mine', queryString))
  return assertSuccess(response, '查询我的反馈失败')
}

export async function getAnnouncementPage(
  query: UserAnnouncementQuery,
): Promise<UserAnnouncementPage> {
  const queryString = buildAnnouncementQueryString(query)
  const response = await getJson<ApiResult<UserAnnouncementPage>>(appendQuery('/api/user/announcements', queryString))
  return assertSuccess(response, '查询公告列表失败')
}

export async function getAnnouncementDetail(id: number): Promise<UserAnnouncementDetail> {
  const response = await getJson<ApiResult<UserAnnouncementDetail>>(`/api/user/announcements/${id}`)
  return assertSuccess(response, '查询公告详情失败')
}
