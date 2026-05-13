/**
 * @author yyyouth zg
 * @date 2026-05-08
 * 用户主页看板接口（user-15）
 */
import { getJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'

/** 时间范围 */
export type TrendRange = '7d' | '30d' | '90d'

/** 指标 */
export type TrendMetric = 'pv' | 'likes' | 'favorites' | 'comments' | 'contribution'

export interface DashboardRequestContext {
  username?: string
}

/** 贡献概览 */
export interface DashboardOverview {
  totalSubmit: number
  totalComment: number
  totalCollect: number
  totalLike: number
  totalScore: number
  totalBrowse: number
  /** 后端序列化为字符串（BigDecimal） */
  totalContribution: number | string
  levelCode: 'S' | 'A' | 'B' | 'C'
  streakDays: number
  maxStreakDays: number
  rankPercent: number | string
  lastActiveDate?: string | null
}

/** 热力图单元格 */
export interface ContributionCell {
  date: string
  count: number
  level: 0 | 1 | 2 | 3 | 4
}

/** 热力图月份标签 */
export interface ContributionMonthLabel {
  label: string
  colOffset: number
}

/** 贡献热力图 */
export interface ContributionGraph {
  year: number
  total: number
  maxDaily: number
  cells: ContributionCell[]
  months: ContributionMonthLabel[]
}

/** 分类偏好单项 */
export interface CategoryShare {
  categoryId: number | null
  name: string
  weight: number | string
  pct: number | string
}

/** 分类偏好分布 */
export interface CategoryDistribution {
  totalWeight: number | string
  items: CategoryShare[]
}

/** 技术雷达维度 */
export interface TechRadarDim {
  name: string
  value: number
}

/** 技术语言占比 */
export interface TechLanguage {
  techId: number | null
  name: string
  pct: number | string
}

/** 技术雷达 */
export interface TechRadar {
  dimensions: TechRadarDim[]
  languages: TechLanguage[]
}

/** 趋势点 */
export interface TrendPoint {
  date: string
  value: number | string
}

/** 趋势序列 */
export interface TrendSeries {
  range: TrendRange
  metric: TrendMetric
  points: TrendPoint[]
  delta: number | string
}

const buildDashboardUrl = (
  section: string,
  context?: DashboardRequestContext,
  query?: Record<string, string | number | undefined>,
) => {
  const base = context?.username
    ? `/api/user/profile/public/${encodeURIComponent(context.username)}/dashboard/${section}`
    : `/api/user/profile/dashboard/${section}`
  const params = new URLSearchParams()
  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value !== undefined) {
      params.set(key, String(value))
    }
  })
  const qs = params.toString()
  return qs ? `${base}?${qs}` : base
}

/** 查询贡献概览 */
export async function getDashboardOverview(context?: DashboardRequestContext): Promise<DashboardOverview> {
  const res = await getJson<ApiResult<DashboardOverview>>(buildDashboardUrl('overview', context))
  if (res.code !== 200 || !res.data) {
    throw new Error(res.msg || '查询贡献概览失败')
  }
  return res.data
}

/** 查询贡献热力图；year 省略取当前年 */
export async function getContributionGraph(
  year?: number,
  context?: DashboardRequestContext,
): Promise<ContributionGraph> {
  const url = buildDashboardUrl('contribution-graph', context, { year })
  const res = await getJson<ApiResult<ContributionGraph>>(url)
  if (res.code !== 200 || !res.data) {
    throw new Error(res.msg || '查询贡献热力图失败')
  }
  return res.data
}

/** 查询分类偏好分布 */
export async function getCategoryDistribution(
  context?: DashboardRequestContext,
): Promise<CategoryDistribution> {
  const res = await getJson<ApiResult<CategoryDistribution>>(
    buildDashboardUrl('category-distribution', context),
  )
  if (res.code !== 200 || !res.data) {
    throw new Error(res.msg || '查询分类偏好失败')
  }
  return res.data
}

/** 查询本人投稿分类分布 */
export async function getSubmittedCategoryDistribution(
  context?: DashboardRequestContext,
): Promise<CategoryDistribution> {
  const res = await getJson<ApiResult<CategoryDistribution>>(
    buildDashboardUrl('submitted-category-distribution', context),
  )
  if (res.code !== 200 || !res.data) {
    throw new Error(res.msg || '查询投稿分类失败')
  }
  return res.data
}

/** 查询技术雷达 */
export async function getTechRadar(context?: DashboardRequestContext): Promise<TechRadar> {
  const res = await getJson<ApiResult<TechRadar>>(buildDashboardUrl('tech-radar', context))
  if (res.code !== 200 || !res.data) {
    throw new Error(res.msg || '查询技术雷达失败')
  }
  return res.data
}

/** 查询趋势折线 */
export async function getTrendSeries(
  range: TrendRange = '30d',
  metric: TrendMetric = 'pv',
  context?: DashboardRequestContext,
): Promise<TrendSeries> {
  const url = buildDashboardUrl('trend-series', context, { range, metric })
  const res = await getJson<ApiResult<TrendSeries>>(url)
  if (res.code !== 200 || !res.data) {
    throw new Error(res.msg || '查询趋势失败')
  }
  return res.data
}
