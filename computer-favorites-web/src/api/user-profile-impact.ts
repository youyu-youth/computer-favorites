/**
 * @author yyyouth zg
 * @date 2026-05-09
 * 上传网站影响力看板接口（user-15 扩展）
 *
 * 与 {@link ./user-profile-dashboard} 相同的 URL 构造规则：
 *  - context.username 存在 → /api/user/profile/public/{username}/dashboard/upload-impact
 *  - 否则 → /api/user/profile/dashboard/upload-impact
 */
import { getJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'

/** 时间范围 */
export type ImpactRange = '7d' | '30d' | '90d' | 'all'

/** 五指标计数组（单位：次） */
export interface MetricGroup {
  browse: number
  like: number
  collect: number
  comment: number
  score: number
}

/**
 * 五指标环比 delta（百分比保留 1 位小数）。
 * 后端 BigDecimal 序列化为 number；range=all 时全部为 null。
 */
export interface DeltaGroup {
  browse: number | null
  like: number | null
  collect: number | null
  comment: number | null
  score: number | null
}

/** 日序列单点 */
export interface DailyPoint {
  date: string
  browse: number
  like: number
  collect: number
  comment: number
  score: number
}

/** 上传网站影响力 VO */
export interface UploadImpact {
  range: ImpactRange
  websiteCount: number
  totals: MetricGroup
  rangeCounts: MetricGroup
  delta: DeltaGroup
  /** 7d/30d/90d 时为日序列；all 时为空数组 */
  points: DailyPoint[]
}

export interface ImpactRequestContext {
  username?: string
}

/**
 * 构造影响力接口 URL（区分公开 / 登录端）
 */
const buildImpactUrl = (range: ImpactRange, context?: ImpactRequestContext): string => {
  const base = context?.username
    ? `/api/user/profile/public/${encodeURIComponent(context.username)}/dashboard/upload-impact`
    : `/api/user/profile/dashboard/upload-impact`
  const params = new URLSearchParams()
  params.set('range', range)
  return `${base}?${params.toString()}`
}

/**
 * 查询上传网站影响力
 *
 * @param range   时间范围，默认 30d
 * @param context 请求上下文（含 username 时走公开端）
 */
export async function getUploadImpact(
  range: ImpactRange = '30d',
  context?: ImpactRequestContext,
): Promise<UploadImpact> {
  const res = await getJson<ApiResult<UploadImpact>>(buildImpactUrl(range, context))
  if (res.code !== 200 || !res.data) {
    throw new Error(res.msg || '查询上传网站影响力失败')
  }
  return res.data
}
