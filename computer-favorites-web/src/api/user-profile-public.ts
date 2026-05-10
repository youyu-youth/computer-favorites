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
  const res = await getJson<ApiResult<ProfilePublicResponse>>(
    `/api/user/profile/public/${safeUsername}`,
  )
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

// =============== user-15 公开收藏夹 ===============

/** 公开主页顶层收藏夹列表项（对齐后端 PublicFolderItemVO） */
export interface PublicFolderItem {
  id: number
  name: string
  icon: string | null
  color: string | null
  parentId: number
  sort: number
  websiteCount: number
  /** 子文件夹数量（仅可见的；他人视图已过滤 is_public=0 与 is_hide=1） */
  childrenCount: number
}

/** 对话框/查看全部页中的子收藏夹（对齐后端 PublicFolderChildVO） */
export interface PublicFolderChild {
  id: number
  name: string
  icon: string | null
  color: string | null
  parentId: number
  websiteCount: number
}

/** 对话框/查看全部页中的网站卡片（对齐后端 PublicFolderWebsiteVO） */
export interface PublicFolderWebsite {
  id: number
  title: string
  url: string
  cover: string | null
  description: string | null
  categoryName: string | null
  /** 标签名列表（本期为 null，预留扩展） */
  tagNames: string[] | null
  clickCount: number
  likeCount: number
  collectCount: number
  /** 评分（0-5，保留 1 位小数） */
  score: number | null
}

/** 单收藏夹一层子项分页结果（对齐后端 PublicFolderChildrenVO） */
export interface PublicFolderChildrenResponse {
  subFolders: PublicFolderChild[]
  websites: {
    list: PublicFolderWebsite[]
    total: number
    pageNum: number
    pageSize: number
  }
}

/** 公开收藏夹树节点（对齐后端 PublicFolderTreeVO.TreeNode） */
export interface PublicFolderTreeNode {
  id: number
  name: string
  icon: string | null
  color: string | null
  parentId: number
  sort: number
  websiteCount: number
  children: PublicFolderTreeNode[]
}

/** 公开收藏夹全量树（对齐后端 PublicFolderTreeVO） */
export interface PublicFolderTreeResponse {
  roots: PublicFolderTreeNode[]
}

/** 单夹子项分页查询参数 */
export interface PublicFolderChildrenQuery {
  pageNum?: number
  pageSize?: number
}

/**
 * 公开主页顶层收藏夹列表（默认最多 5 个）。
 * 他人视图过滤 is_hide=1 / is_public=0 / 空夹；本人视图全量。
 */
export async function getPublicFolders(username: string, limit = 5): Promise<PublicFolderItem[]> {
  const safeUsername = encodeURIComponent(username)
  const url = `/api/user/profile/public/${safeUsername}/folders?limit=${limit}`
  const res = await getJson<ApiResult<PublicFolderItem[]>>(url)
  if (res.code !== 200) {
    throw Object.assign(new Error(res.msg || '获取公开收藏夹列表失败'), { code: res.code })
  }
  return res.data || []
}

/**
 * 公开主页单收藏夹一层子项（对话框使用）。
 */
export async function getPublicFolderChildren(
  username: string,
  folderId: number,
  query: PublicFolderChildrenQuery = {},
): Promise<PublicFolderChildrenResponse> {
  const safeUsername = encodeURIComponent(username)
  const params = new URLSearchParams()
  if (query.pageNum !== undefined) {
    params.set('pageNum', String(query.pageNum))
  }
  if (query.pageSize !== undefined) {
    params.set('pageSize', String(query.pageSize))
  }
  const qs = params.toString()
  const url = `/api/user/profile/public/${safeUsername}/folders/${folderId}/children${qs ? `?${qs}` : ''}`
  const res = await getJson<ApiResult<PublicFolderChildrenResponse>>(url)
  if (res.code !== 200 || !res.data) {
    throw Object.assign(new Error(res.msg || '获取收藏夹子项失败'), { code: res.code })
  }
  return res.data
}

/**
 * 公开主页全量公开收藏夹树（"查看全部"页使用）。
 */
export async function getPublicFolderTree(username: string): Promise<PublicFolderTreeResponse> {
  const safeUsername = encodeURIComponent(username)
  const url = `/api/user/profile/public/${safeUsername}/folders/tree`
  const res = await getJson<ApiResult<PublicFolderTreeResponse>>(url)
  if (res.code !== 200 || !res.data) {
    throw Object.assign(new Error(res.msg || '获取公开收藏夹树失败'), { code: res.code })
  }
  return res.data
}
