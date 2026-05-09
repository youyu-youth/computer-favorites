import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import {
  deleteCurrentUserAvatar,
  getCurrentUserProfile,
  updateCurrentEmail,
  updateCurrentUsername,
  updateCurrentUserProfile,
  updateCurrentUserSetting,
  updateProfilePrivacy,
  uploadCurrentUserAvatar,
  type LoginUserProfileResponse,
  type UpdateEmailRequest,
} from '@/api/user'
import { useAppStore, type ThemeMode } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import type { SupportedLanguage } from '@/i18n'

export interface SettingsBasicInfo {
  id: number
  username: string
  email: string
  phone: string
  avatar: string
  nickname: string
  emailVerified: number
  phoneVerified: number
}

export interface SettingsDetailProfile {
  gender: number
  country: string
  city: string
  githubUrl: string
  giteeUrl: string
  otherRepoLinks: string
  blogUrl: string
  signature: string
  hobbyTags: string
  techStack: string
  favoriteWebsites: string
  uploadedWebsites: string
  contribution: string
  createTime: string
  updateTime: string
}

export interface SettingsPreferenceSetting {
  theme: ThemeMode
  language: SupportedLanguage
  emailNotice: number
  collectNotice: number
  commentNotice: number
  homepageStyle: 'card' | 'list'
  favoritesHidePassword: string
  pageSize: 10 | 20 | 50
}

export interface SettingsProfilePrivacy {
  profileVisibility: 'public' | 'logged' | 'private'
  showContribution: number
  showCollections: number
}

const createDefaultBasicInfo = (): SettingsBasicInfo => ({
  id: 0,
  username: '',
  email: '',
  phone: '',
  avatar: '',
  nickname: '',
  emailVerified: 0,
  phoneVerified: 0,
})

const createDefaultProfile = (): SettingsDetailProfile => ({
  gender: 3,
  country: '',
  city: '',
  githubUrl: '',
  giteeUrl: '',
  otherRepoLinks: '',
  blogUrl: '',
  signature: '',
  hobbyTags: '',
  techStack: '',
  favoriteWebsites: '',
  uploadedWebsites: '',
  contribution: '',
  createTime: '',
  updateTime: '',
})

const createDefaultSetting = (): SettingsPreferenceSetting => ({
  theme: 'dark',
  language: 'zh-CN',
  emailNotice: 1,
  collectNotice: 1,
  commentNotice: 1,
  homepageStyle: 'card',
  favoritesHidePassword: '',
  pageSize: 20,
})

const createDefaultPrivacy = (): SettingsProfilePrivacy => ({
  profileVisibility: 'public',
  showContribution: 1,
  showCollections: 1,
})

const normalizeTheme = (value: unknown): ThemeMode => {
  if (value === 'light' || value === 'dark' || value === 'system') {
    return value
  }
  return 'dark'
}

const normalizeLanguage = (value: unknown): SupportedLanguage => {
  if (value === 'zh-CN' || value === 'en-US') {
    return value
  }
  return 'zh-CN'
}

const normalizeHomepageStyle = (value: unknown): 'card' | 'list' => {
  if (value === 'card' || value === 'list') {
    return value
  }
  return 'card'
}

const normalizePageSize = (value: unknown): 10 | 20 | 50 => {
  if (value === 10 || value === 20 || value === 50) {
    return value
  }
  return 20
}

const normalizeNotice = (value: unknown): number => (value === 0 ? 0 : 1)

const normalizeProfileVisibility = (value: unknown): 'public' | 'logged' | 'private' => {
  if (value === 'public' || value === 'logged' || value === 'private') {
    return value
  }
  return 'public'
}

export const useSettingsStore = defineStore('settings', () => {
  const basicInfo = reactive<SettingsBasicInfo>(createDefaultBasicInfo())
  const profile = reactive<SettingsDetailProfile>(createDefaultProfile())
  const setting = reactive<SettingsPreferenceSetting>(createDefaultSetting())
  const privacy = reactive<SettingsProfilePrivacy>(createDefaultPrivacy())
  const loading = ref(false)
  const saving = ref(false)
  const updatingUsername = ref(false)
  const updatingEmail = ref(false)
  const uploadingAvatar = ref(false)
  const deletingAvatar = ref(false)

  const applyProfileResponse = (data: LoginUserProfileResponse) => {
    const user = data.user ?? {}
    const detail = data.profile ?? {}
    const preference = data.setting ?? {}

    Object.assign(basicInfo, {
      id: user.id ?? 0,
      username: user.username ?? '',
      email: user.email ?? '',
      phone: user.phone ?? '',
      avatar: user.avatar ?? '',
      nickname: user.nickname ?? user.username ?? '',
      emailVerified: user.emailVerified ?? 0,
      phoneVerified: user.phoneVerified ?? 0,
    })

    Object.assign(profile, {
      gender: detail.gender ?? 3,
      country: detail.country ?? '',
      city: detail.city ?? '',
      githubUrl: detail.githubUrl ?? '',
      giteeUrl: detail.giteeUrl ?? '',
      otherRepoLinks: detail.otherRepoLinks ?? '',
      blogUrl: detail.blogUrl ?? '',
      signature: detail.signature ?? '',
      hobbyTags: detail.hobbyTags ?? '',
      techStack: detail.techStack ?? '',
      favoriteWebsites: detail.favoriteWebsites ?? '',
      uploadedWebsites: detail.uploadedWebsites ?? '',
      contribution: detail.contribution ?? '',
      createTime: detail.createTime ?? '',
      updateTime: detail.updateTime ?? '',
    })

    Object.assign(setting, {
      theme: normalizeTheme(preference.theme),
      language: normalizeLanguage(preference.language),
      emailNotice: normalizeNotice(preference.emailNotice),
      collectNotice: normalizeNotice(preference.collectNotice),
      commentNotice: normalizeNotice(preference.commentNotice),
      homepageStyle: normalizeHomepageStyle(preference.homepageStyle),
      favoritesHidePassword: preference.favoritesHidePassword ?? '',
      pageSize: normalizePageSize(preference.pageSize),
    })

    Object.assign(privacy, {
      profileVisibility: normalizeProfileVisibility(preference.profileVisibility),
      showContribution: normalizeNotice(preference.showContribution),
      showCollections: normalizeNotice(preference.showCollections),
    })
  }

  const syncAuthSnapshot = () => {
    const authStore = useAuthStore()
    authStore.setUserSnapshot({
      userId: basicInfo.id || null,
      nickname: basicInfo.nickname || basicInfo.username,
      avatar: basicInfo.avatar,
    })
  }

  const fetchProfile = async () => {
    loading.value = true
    try {
      const data = await getCurrentUserProfile()
      applyProfileResponse(data)
      syncAuthSnapshot()
      const appStore = useAppStore()
      appStore.setThemeMode(setting.theme)
      appStore.setLanguage(setting.language)
    } finally {
      loading.value = false
    }
  }

  const saveProfile = async () => {
    await updateCurrentUserProfile({
      nickname: basicInfo.nickname,
      gender: profile.gender,
      country: profile.country,
      city: profile.city,
      githubUrl: profile.githubUrl,
      giteeUrl: profile.giteeUrl,
      otherRepoLinks: profile.otherRepoLinks,
      blogUrl: profile.blogUrl,
      signature: profile.signature,
      hobbyTags: profile.hobbyTags,
      techStack: profile.techStack,
    })
    syncAuthSnapshot()
  }

  const saveSetting = async () => {
    await updateCurrentUserSetting({
      theme: setting.theme,
      language: setting.language,
      emailNotice: setting.emailNotice,
      collectNotice: setting.collectNotice,
      commentNotice: setting.commentNotice,
      homepageStyle: setting.homepageStyle,
      favoritesHidePassword: setting.favoritesHidePassword,
      pageSize: setting.pageSize,
    })

    const appStore = useAppStore()
    appStore.setThemeMode(setting.theme)
    appStore.setLanguage(setting.language)
  }

  const savePrivacy = async () => {
    await updateProfilePrivacy({
      profileVisibility: privacy.profileVisibility,
      showContribution: privacy.showContribution,
      showCollections: privacy.showCollections,
    })
  }

  const saveAll = async () => {
    saving.value = true
    try {
      await saveProfile()
      await saveSetting()
      await savePrivacy()
    } finally {
      saving.value = false
    }
  }

  const updateUsername = async (username: string) => {
    updatingUsername.value = true
    try {
      await updateCurrentUsername({ username })
      basicInfo.username = username
      syncAuthSnapshot()
    } finally {
      updatingUsername.value = false
    }
  }

  const updateEmail = async (payload: UpdateEmailRequest) => {
    updatingEmail.value = true
    try {
      await updateCurrentEmail(payload)
      basicInfo.email = payload.email
      basicInfo.emailVerified = 1
    } finally {
      updatingEmail.value = false
    }
  }

  const uploadAvatar = async (file: File) => {
    uploadingAvatar.value = true
    try {
      const result = await uploadCurrentUserAvatar(file)
      basicInfo.avatar = result.avatarUrl
      syncAuthSnapshot()
      return result.avatarUrl
    } finally {
      uploadingAvatar.value = false
    }
  }

  const deleteAvatar = async () => {
    deletingAvatar.value = true
    try {
      await deleteCurrentUserAvatar()
      basicInfo.avatar = ''
      syncAuthSnapshot()
    } finally {
      deletingAvatar.value = false
    }
  }

  return {
    basicInfo,
    profile,
    setting,
    privacy,
    loading,
    saving,
    updatingUsername,
    updatingEmail,
    uploadingAvatar,
    deletingAvatar,
    fetchProfile,
    saveAll,
    saveProfile,
    saveSetting,
    savePrivacy,
    updateUsername,
    updateEmail,
    uploadAvatar,
    deleteAvatar,
  }
})
