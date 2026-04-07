import { deleteJson, getJson, postFormData, postJson, putJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type {
  UserWebsiteSubmissionCreatePayload,
  UserWebsiteSubmissionDetail,
  UserWebsiteSubmissionIconUploadResult,
  UserWebsiteSubmissionPage,
  UserWebsiteSubmissionQuery,
} from '@/types/user-website-submission'

function buildSubmissionQueryString(query: UserWebsiteSubmissionQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))

  if (query.keyword && query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  if (typeof query.auditStatus === 'number') {
    params.set('auditStatus', String(query.auditStatus))
  }

  return params.toString()
}

export async function submitUserWebsite(payload: UserWebsiteSubmissionCreatePayload): Promise<number> {
  const res = await postJson<ApiResult<number>>('/api/user/website/submission', payload)
  if (res.code !== 200) {
    throw new Error(res.msg || '投稿提交失败')
  }
  if (typeof res.data !== 'number') {
    throw new Error('投稿提交返回数据异常')
  }
  return res.data
}

export async function uploadUserSubmissionIcon(file: File): Promise<UserWebsiteSubmissionIconUploadResult> {
  const formData = new FormData()
  formData.append('file', file)

  const res = await postFormData<ApiResult<UserWebsiteSubmissionIconUploadResult>>(
    '/api/user/website/submission/icon',
    formData,
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '图标上传失败')
  }
  if (!res.data) {
    throw new Error('图标上传结果为空')
  }
  return res.data
}

export async function deleteUserSubmissionIcon(objectKey: string): Promise<void> {
  const encodedObjectKey = encodeURIComponent(objectKey)
  const res = await deleteJson<ApiResult<unknown>>(
    `/api/user/website/submission/icon?objectKey=${encodedObjectKey}`,
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '图标删除失败')
  }
}

export async function getMyWebsiteSubmissionPage(
  query: UserWebsiteSubmissionQuery,
): Promise<UserWebsiteSubmissionPage> {
  const queryString = buildSubmissionQueryString(query)
  const res = await getJson<ApiResult<UserWebsiteSubmissionPage>>(
    `/api/user/website/submission/list?${queryString}`,
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '查询投稿列表失败')
  }
  if (!res.data) {
    throw new Error('投稿列表为空')
  }
  return res.data
}

export async function getMyWebsiteSubmissionDetail(
  websiteId: number,
): Promise<UserWebsiteSubmissionDetail> {
  const res = await getJson<ApiResult<UserWebsiteSubmissionDetail>>(
    `/api/user/website/submission/${websiteId}`,
  )
  if (res.code !== 200) {
    throw new Error(res.msg || '查询投稿详情失败')
  }
  if (!res.data) {
    throw new Error('投稿详情为空')
  }
  return res.data
}

export async function updateMyWebsiteSubmission(
  websiteId: number,
  payload: UserWebsiteSubmissionCreatePayload,
): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/user/website/submission/${websiteId}`, payload)
  if (res.code !== 200) {
    throw new Error(res.msg || '更新投稿失败')
  }
}

export async function cancelMyWebsiteSubmission(websiteId: number): Promise<void> {
  const res = await deleteJson<ApiResult<unknown>>(`/api/user/website/submission/${websiteId}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '取消投稿失败')
  }
}

export async function resubmitMyWebsiteSubmission(websiteId: number): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/user/website/submission/${websiteId}/resubmit`)
  if (res.code !== 200) {
    throw new Error(res.msg || '重新提交失败')
  }
}
