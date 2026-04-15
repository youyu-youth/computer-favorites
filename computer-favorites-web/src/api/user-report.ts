import { getJson, postJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type { ReportFormData } from '@/types/report'

/**
 * 举报提交响应
 */
export interface ReportSubmitResponse {
  reportId: number
  status: number
  createTime: string
}

/**
 * 举报列表项
 */
export interface ReportListItem {
  id: number
  type: number
  targetId: number
  targetName: string
  reason: string
  images: string[] | null
  status: number
  handleResult: string | null
  handleTime: string | null
  createTime: string
}

/**
 * 举报列表查询参数
 */
export interface ReportQueryParams {
  pageNum?: number
  pageSize?: number
  type?: number
  status?: number
}

/**
 * 举报列表响应
 */
export interface ReportListResponse {
  total: number
  pageNum: number
  pageSize: number
  records: ReportListItem[]
}

/**
 * 举报详情
 */
export interface ReportDetail {
  id: number
  type: number
  targetId: number
  targetName: string
  targetUrl: string
  reason: string
  images: string[] | null
  status: number
  handleResult: string | null
  handlerName: string | null
  handleTime: string | null
  createTime: string
}

/**
 * 提交举报
 */
export function submitReport(data: ReportFormData): Promise<ApiResult<ReportSubmitResponse>> {
  return postJson('/api/user/report', data)
}

/**
 * 我的举报列表
 */
export function getMyReports(
  params: ReportQueryParams,
): Promise<ApiResult<ReportListResponse>> {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.set(key, String(value))
    }
  })

  const query = searchParams.toString()
  return getJson(`/api/user/report/my${query ? `?${query}` : ''}`)
}

/**
 * 举报详情
 */
export function getReportDetail(id: number): Promise<ApiResult<ReportDetail>> {
  return getJson(`/api/user/report/${id}`)
}
