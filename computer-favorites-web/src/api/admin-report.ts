import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import type { ApiResult } from '@/api/types'
import { getJson, putJson } from '@/utils/http'
import type {
  AdminReportBatchHandleResult,
  AdminReportDetail,
  AdminReportHandleForm,
  AdminReportHandleResult,
  AdminReportPage,
  AdminReportQuery,
  AdminReportStatistics,
} from '@/types/report'

type AdminReportBatchHandlePayload = AdminReportHandleForm & {
  reportIds: number[]
}

function buildReportListQueryString(query: AdminReportQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))
  params.set('pendingOnly', String(query.pendingOnly))

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

function buildStatisticsQueryString(startTime?: string, endTime?: string): string {
  const params = new URLSearchParams()

  if (startTime?.trim()) {
    params.set('startTime', startTime.trim())
  }

  if (endTime?.trim()) {
    params.set('endTime', endTime.trim())
  }

  return params.toString()
}

export async function getAdminReportList(query: AdminReportQuery): Promise<AdminReportPage> {
  const queryString = buildReportListQueryString(query)
  const res = await getJson<ApiResult<AdminReportPage>>(`/api/admin/report/list?${queryString}`, {
    headers: buildAdminAuthHeaders(),
  })

  if (res.code !== 200) {
    throw new Error(res.msg || '查询举报列表失败')
  }

  if (!res.data) {
    throw new Error('举报列表为空')
  }

  return res.data
}

export async function getAdminReportDetail(reportId: number): Promise<AdminReportDetail> {
  const res = await getJson<ApiResult<AdminReportDetail>>(`/api/admin/report/${reportId}`, {
    headers: buildAdminAuthHeaders(),
  })

  if (res.code !== 200) {
    throw new Error(res.msg || '查询举报详情失败')
  }

  if (!res.data) {
    throw new Error('举报详情为空')
  }

  return res.data
}

export async function handleAdminReport(
  reportId: number,
  payload: AdminReportHandleForm,
): Promise<AdminReportHandleResult> {
  const res = await putJson<ApiResult<AdminReportHandleResult>>(
    `/api/admin/report/${reportId}/handle`,
    payload,
    {
      headers: buildAdminAuthHeaders(),
    },
  )

  if (res.code !== 200) {
    throw new Error(res.msg || '处置举报失败')
  }

  if (!res.data) {
    throw new Error('处置结果为空')
  }

  return res.data
}

export async function batchHandleAdminReport(
  payload: AdminReportBatchHandlePayload,
): Promise<AdminReportBatchHandleResult> {
  const res = await putJson<ApiResult<AdminReportBatchHandleResult>>(
    '/api/admin/report/handle/batch',
    payload,
    {
      headers: buildAdminAuthHeaders(),
    },
  )

  if (res.code !== 200) {
    throw new Error(res.msg || '批量处置举报失败')
  }

  if (!res.data) {
    throw new Error('批量处置结果为空')
  }

  return res.data
}

export async function getAdminReportStatistics(
  params?: {
    startTime?: string
    endTime?: string
  },
): Promise<AdminReportStatistics> {
  const queryString = buildStatisticsQueryString(params?.startTime, params?.endTime)
  const requestPath = queryString
    ? `/api/admin/report/statistics?${queryString}`
    : '/api/admin/report/statistics'
  const res = await getJson<ApiResult<AdminReportStatistics>>(requestPath, {
    headers: buildAdminAuthHeaders(),
  })

  if (res.code !== 200) {
    throw new Error(res.msg || '查询举报统计失败')
  }

  if (!res.data) {
    throw new Error('举报统计为空')
  }

  return res.data
}
