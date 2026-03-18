import { getJson } from '@/utils/http'

type ApiResult<T> = {
  code: number
  msg: string
  data?: T
}

export interface LoginUserBasicInfo {
  id?: number
  username?: string
  email?: string
  phone?: string
  avatar?: string
  nickname?: string
  emailVerified?: number
  phoneVerified?: number
}

export interface LoginUserDetailProfile {
  gender?: number
  country?: string
  city?: string
  githubUrl?: string
  giteeUrl?: string
  otherRepoLinks?: string
  blogUrl?: string
  signature?: string
  hobbyTags?: string
  techStack?: string
  favoriteWebsites?: string
  uploadedWebsites?: string
  contribution?: string
  deleted?: number
  createTime?: string
  updateTime?: string
}

export interface LoginUserPreferenceSetting {
  theme?: string
  language?: string
  emailNotice?: number
  collectNotice?: number
  commentNotice?: number
  homepageStyle?: string
  pageSize?: number
}

export interface LoginUserProfileResponse {
  user?: LoginUserBasicInfo
  profile?: LoginUserDetailProfile
  setting?: LoginUserPreferenceSetting
}

export async function getCurrentUserProfile(): Promise<LoginUserProfileResponse> {
  const res = await getJson<ApiResult<LoginUserProfileResponse>>('/api/user/profile/current')
  if (res.code !== 200) {
    throw new Error(res.msg || '获取个人资料失败')
  }

  if (!res.data) {
    throw new Error('个人资料为空')
  }

  return res.data
}
