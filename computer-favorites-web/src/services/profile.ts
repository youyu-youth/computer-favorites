import { deleteJson, getJson, postFormData, putJson } from '@/utils/http'

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

export interface UpdateUserProfileRequest {
  nickname?: string
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
}

export interface UserAvatarUploadResponse {
  avatarUrl: string
  objectKey: string
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

export async function updateCurrentUserProfile(payload: UpdateUserProfileRequest): Promise<void> {
  try {
    const res = await putJson<ApiResult<null>>('/api/user/profile', payload)
    if (res.code !== 200) {
      throw new Error(res.msg || '保存个人资料失败')
    }
    return
  } catch (error) {
    const message = error instanceof Error ? error.message : ''
    const shouldRetry = message.includes('No static resource api/user/profile')
    if (!shouldRetry) {
      throw error
    }

    const retryRes = await putJson<ApiResult<null>>('/api/user/profile/update', payload)
    if (retryRes.code !== 200) {
      throw new Error(retryRes.msg || '保存个人资料失败')
    }
  }
}

export async function uploadCurrentUserAvatar(file: File): Promise<UserAvatarUploadResponse> {
  const formData = new FormData()
  formData.append('file', file)
  const res = await postFormData<ApiResult<UserAvatarUploadResponse>>('/api/user/profile/avatar', formData)
  if (res.code !== 200 || !res.data) {
    throw new Error(res.msg || '头像上传失败')
  }
  return res.data
}

export async function deleteCurrentUserAvatar(): Promise<void> {
  const res = await deleteJson<ApiResult<null>>('/api/user/profile/avatar')
  if (res.code !== 200) {
    throw new Error(res.msg || '头像删除失败')
  }
}
