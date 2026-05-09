import { getJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type {
  CategoryDistribution,
  ContributionGraph,
  DashboardOverview,
  TechRadar,
  TrendMetric,
  TrendRange,
  TrendSeries,
} from '@/api/user-profile-dashboard'

export type ProfileVisibility = 'public' | 'logged' | 'private'

export interface ProfilePublicUser {
  id: number
  username: string
  nickname?: string
  avatar?: string
  joinDate?: string
}

export interface ProfilePublicProfile {
  signature?: string
  country?: string
  city?: string
  githubUrl?: string
  giteeUrl?: string
  blogUrl?: string
  hobbyTags?: string
  techStack?: string
}

export interface ProfilePublicPrivacy {
  profileVisibility: ProfileVisibility
  showContribution: number
  showCollections: number
}

export interface ProfilePublicResponse {
  user: ProfilePublicUser
  profile?: ProfilePublicProfile | null
  privacy: ProfilePublicPrivacy
  isOwn: boolean
}

export type PublicDashboardSection =
  | 'overview'
  | 'contribution-graph'
  | 'category-distribution'
  | 'tech-radar'
  | 'trend-series'

export type PublicDashboardResponse =
  | DashboardOverview
  | ContributionGraph
  | CategoryDistribution
  | TechRadar
  | TrendSeries

export interface PublicDashboardQuery {
  year?: number
  range?: TrendRange
  metric?: TrendMetric
}

export async function getPublicProfile(username: string): Promise<ProfilePublicResponse> {
  const safeUsername = encodeURIComponent(username)
  const res = await getJson<ApiResult<ProfilePublicResponse>>(`/api/user/profile/public/${safeUsername}`)
  if (res.code !== 200 || !res.data) {
    throw Object.assign(new Error(res.msg || '获取公开主页失败'), { code: res.code })
  }
  return res.data
}

export async function getPublicDashboard(
  username: string,
  section: PublicDashboardSection,
  query: PublicDashboardQuery = {},
): Promise<PublicDashboardResponse> {
  const safeUsername = encodeURIComponent(username)
  const params = new URLSearchParams()
  if (query.year !== undefined) {
    params.set('year', String(query.year))
  }
  if (query.range) {
    params.set('range', query.range)
  }
  if (query.metric) {
    params.set('metric', query.metric)
  }
  const qs = params.toString()
  const url = `/api/user/profile/public/${safeUsername}/dashboard/${section}${qs ? `?${qs}` : ''}`
  const res = await getJson<ApiResult<PublicDashboardResponse>>(url)
  if (res.code !== 200 || !res.data) {
    throw Object.assign(new Error(res.msg || '获取公开主页看板失败'), { code: res.code })
  }
  return res.data
}
