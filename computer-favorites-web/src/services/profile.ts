import { deleteJson, getJson, postFormData, postJson, putJson } from '@/utils/http'

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

export interface UpdateUserSettingRequest {
  theme?: 'light' | 'dark' | 'system'
  language?: 'zh-CN' | 'en-US'
  emailNotice?: number
  collectNotice?: number
  commentNotice?: number
  homepageStyle?: 'card' | 'list'
  pageSize?: 10 | 20 | 50
}

export interface UpdateUsernameRequest {
  username: string
}

export interface SendEmailUpdateCodeRequest {
  email: string
}

export interface UpdateEmailRequest {
  email: string
  emailCode: string
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

export async function updateCurrentUserSetting(payload: UpdateUserSettingRequest): Promise<void> {
  const res = await putJson<ApiResult<null>>('/api/user/profile/setting', payload)
  if (res.code !== 200) {
    throw new Error(res.msg || '保存偏好设置失败')
  }
}

export async function updateCurrentUsername(payload: UpdateUsernameRequest): Promise<void> {
  const res = await putJson<ApiResult<null>>('/api/user/profile/username', payload)
  if (res.code !== 200) {
    throw new Error(res.msg || '用户名修改失败')
  }
}

export async function sendEmailUpdateCode(payload: SendEmailUpdateCodeRequest): Promise<void> {
  const res = await postJson<ApiResult<null>>('/api/user/profile/email/code/send', payload)
  if (res.code !== 200) {
    throw new Error(res.msg || '验证码发送失败')
  }
}

export async function verifyEmailUpdateCode(payload: UpdateEmailRequest): Promise<void> {
  const res = await postJson<ApiResult<null>>('/api/user/profile/email/code/verify', payload)
  if (res.code !== 200) {
    throw new Error(res.msg || '验证码校验失败')
  }
}

export async function updateCurrentEmail(payload: UpdateEmailRequest): Promise<void> {
  const res = await putJson<ApiResult<null>>('/api/user/profile/email', payload)
  if (res.code !== 200) {
    throw new Error(res.msg || '邮箱修改失败')
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
