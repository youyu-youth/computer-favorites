import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getPublicProfile, type ProfilePublicResponse } from '@/api/user-profile-public'
import type { ProfileData, ProfileSiteItem, ProfileSocialLink } from '@/types/profile'

import githubIcon from '@/assets/icons/svg/github.svg'
import giteeIcon from '@/assets/icons/svg/gitee.svg'
import blogIcon from '@/assets/icons/svg/blog.svg'

const PROFILE_LOGIN_REQUIRED_CODE = 100245
const PROFILE_PRIVATE_CODE = 100246
const USER_DISABLED_CODE = 100204

const normalizeStringArray = (value: unknown): string[] => {
  if (Array.isArray(value)) {
    return value.map((item) => String(item).trim()).filter((item) => item.length > 0)
  }

  if (typeof value !== 'string') {
    return []
  }

  const input = value.trim()
  if (!input) {
    return []
  }

  if (input.startsWith('[') && input.endsWith(']')) {
    try {
      const parsed = JSON.parse(input)
      if (Array.isArray(parsed)) {
        return parsed.map((item) => String(item).trim()).filter((item) => item.length > 0)
      }
    } catch {
      return []
    }
  }

  return input
    .split(/[，,、|]/)
    .map((item) => item.trim())
    .filter((item) => item.length > 0)
}

const emptySites = (): ProfileSiteItem[] => []

const resolveErrorCode = (error: unknown): number | null => {
  if (typeof error === 'object' && error !== null && 'code' in error) {
    const code = (error as { code?: unknown }).code
    return typeof code === 'number' ? code : null
  }
  return null
}

const mapToProfileData = (payload: ProfilePublicResponse): ProfileData => {
  const username = payload.user?.username || 'guest'
  const displayName = payload.user?.nickname || username
  const city = payload.profile?.city?.trim()
  const country = payload.profile?.country?.trim()
  const rawLocation = [country, city].filter(Boolean).join(' · ')
  const location = rawLocation || '未设置地区'
  const hobbyTags = normalizeStringArray(payload.profile?.hobbyTags)
  const techStack = normalizeStringArray(payload.profile?.techStack)
  const website = payload.profile?.blogUrl

  const socialLinkCandidates: Array<ProfileSocialLink | null> = [
    payload.profile?.githubUrl
      ? { label: 'GitHub', imageIcon: githubIcon, url: payload.profile.githubUrl }
      : null,
    payload.profile?.giteeUrl
      ? { label: 'Gitee', imageIcon: giteeIcon, url: payload.profile.giteeUrl }
      : null,
    payload.profile?.blogUrl
      ? { label: '博客', imageIcon: blogIcon, url: payload.profile.blogUrl }
      : null,
    website ? { label: '个人网站', icon: 'i-lucide-globe', url: website } : null,
  ]
  const socialLinkList = socialLinkCandidates.filter(
    (item): item is ProfileSocialLink => item !== null,
  )
  const socialLinks = socialLinkList.filter(
    (item, index, list) => list.findIndex((target) => target.url === item.url) === index,
  )

  const timeline = [
    payload.user?.joinDate ? { title: '加入平台', date: payload.user.joinDate } : null,
    rawLocation ? { title: '所在地区', date: rawLocation } : null,
  ].filter((item): item is NonNullable<typeof item> => Boolean(item))

  const showContribution = payload.isOwn || payload.privacy?.showContribution !== 0
  const profileHealthFields = [
    payload.user?.nickname,
    payload.user?.avatar,
    payload.profile?.signature,
    payload.profile?.country,
    payload.profile?.city,
    payload.profile?.githubUrl,
    payload.profile?.giteeUrl,
    payload.profile?.blogUrl,
    payload.profile?.hobbyTags,
    payload.profile?.techStack,
  ]
  const completion = Math.min(
    100,
    Math.round((profileHealthFields.filter(Boolean).length / profileHealthFields.length) * 100),
  )

  return {
    name: displayName,
    role: techStack[0] || '程序员',
    location,
    organization: country || '未设置国家',
    signature: payload.profile?.signature || '这个人很懒，还没有填写个性签名。',
    avatarUrl:
      payload.user?.avatar ||
      `https://api.dicebear.com/7.x/notionists/svg?seed=${encodeURIComponent(displayName)}&backgroundColor=f8f9fa`,
    tags: hobbyTags,
    timeline,
    socialLinks,
    favoriteSites: payload.privacy?.showCollections === 0 && !payload.isOwn ? emptySites() : emptySites(),
    uploadedProjects: emptySites(),
    skills: techStack.map((item) => ({ name: item, level: '熟练' as const })),
    contributions: showContribution
      ? [{ title: '公开主页', summary: payload.profile?.signature || '该用户暂未填写贡献说明。' }]
      : [],
    stats: [
      { label: '资料完整度', value: `${completion}%` },
      { label: '社交链接', value: `${socialLinks.length}` },
      { label: '公开标签', value: `${hobbyTags.length + techStack.length}` },
    ],
  }
}

export const useProfilePublicStore = defineStore('profilePublic', () => {
  const rawProfile = ref<ProfilePublicResponse | null>(null)
  const loading = ref(false)
  const errorCode = ref<number | null>(null)
  const errorMessage = ref('')

  const profileData = computed(() => (rawProfile.value ? mapToProfileData(rawProfile.value) : null))
  const isOwn = computed(() => rawProfile.value?.isOwn === true)
  const privacy = computed(() => rawProfile.value?.privacy ?? null)
  const showContribution = computed(() => isOwn.value || privacy.value?.showContribution !== 0)
  const showCollections = computed(() => isOwn.value || privacy.value?.showCollections !== 0)
  const isPrivate = computed(() => errorCode.value === PROFILE_PRIVATE_CODE)
  const isLoginRequired = computed(() => errorCode.value === PROFILE_LOGIN_REQUIRED_CODE)
  const isNotFound = computed(() => errorCode.value === USER_DISABLED_CODE)

  const reset = () => {
    rawProfile.value = null
    errorCode.value = null
    errorMessage.value = ''
  }

  const load = async (username: string) => {
    loading.value = true
    reset()
    try {
      rawProfile.value = await getPublicProfile(username)
    } catch (error) {
      errorCode.value = resolveErrorCode(error)
      errorMessage.value = error instanceof Error ? error.message : '获取公开主页失败'
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    rawProfile,
    profileData,
    privacy,
    loading,
    errorCode,
    errorMessage,
    isOwn,
    showContribution,
    showCollections,
    isPrivate,
    isLoginRequired,
    isNotFound,
    load,
    reset,
  }
})
