<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import ProfileContributionSection from '@/components/user/profile/ProfileContributionSection.vue'
import ProfileHeatmapSection from '@/components/user/profile/ProfileHeatmapSection.vue'
import ProfileHeroSection from '@/components/user/profile/ProfileHeroSection.vue'
import ProfileSidebarCard from '@/components/user/profile/ProfileSidebarCard.vue'
import ProfileSkillsSection from '@/components/user/profile/ProfileSkillsSection.vue'
import { getCurrentUserProfile, type LoginUserProfileResponse } from '@/api/user'
import { useToast } from '@/composables/useToast'
import type { ProfileData, ProfileSiteItem, ProfileSocialLink } from '@/types/profile'

import githubIcon from '@/assets/icons/svg/github.svg'
import giteeIcon from '@/assets/icons/svg/gitee.svg'
import blogIcon from '@/assets/icons/svg/blog.svg'
import emailIcon from '@/assets/icons/svg/email.svg'

defineOptions({
  name: 'ProfileView',
})

const toast = useToast()
const loading = ref(true)
const errorText = ref('')
const profileData = ref<ProfileData | null>(null)

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

const normalizeSiteItems = (value: unknown, defaultIcon: string): ProfileSiteItem[] => {
  const arrayValue: Array<string | Record<string, unknown>> = Array.isArray(value)
    ? value
    : normalizeStringArray(value)

  return arrayValue
    .map((item, index): ProfileSiteItem | null => {
      if (typeof item === 'string') {
        const name = item.trim()
        if (!name) {
          return null
        }
        return {
          name: `网站 ${index + 1}`,
          description: name,
          icon: defaultIcon,
        }
      }

      const name = String(item.name || item.title || item.url || `网站 ${index + 1}`).trim()
      const description = String(item.description || item.url || item.link || name).trim()
      const icon = String(item.icon || defaultIcon)
      return {
        name: name || `网站 ${index + 1}`,
        description: description || name || `网站 ${index + 1}`,
        icon: icon || defaultIcon,
      }
    })
    .filter((item): item is ProfileSiteItem => Boolean(item))
}

const completionPercent = (payload: LoginUserProfileResponse) => {
  const fields = [
    payload.user?.nickname,
    payload.user?.avatar,
    payload.user?.email,
    payload.profile?.signature,
    payload.profile?.country,
    payload.profile?.city,
    payload.profile?.githubUrl,
    payload.profile?.giteeUrl,
    payload.profile?.blogUrl,
    payload.profile?.hobbyTags,
    payload.profile?.techStack,
    payload.profile?.favoriteWebsites,
    payload.profile?.uploadedWebsites,
    payload.setting?.language,
    payload.setting?.theme,
    payload.setting?.emailNotice,
    payload.setting?.collectNotice,
    payload.setting?.commentNotice,
    payload.setting?.homepageStyle,
    payload.setting?.pageSize,
  ]
  const completed = fields.filter(Boolean).length
  return Math.min(100, Math.round((completed / fields.length) * 100))
}

const mapToProfileData = (payload: LoginUserProfileResponse): ProfileData => {
  const username = payload.user?.username || 'guest'
  const displayName = payload.user?.nickname || username
  const city = payload.profile?.city?.trim()
  const country = payload.profile?.country?.trim()
  const rawLocation = [country, city].filter(Boolean).join(' · ')
  const location = rawLocation || '未设置地区'
  const website = payload.profile?.blogUrl
  const email = payload.user?.email
  const hobbyTags = normalizeStringArray(payload.profile?.hobbyTags)
  const techStack = normalizeStringArray(payload.profile?.techStack)
  const favoriteSites = normalizeSiteItems(payload.profile?.favoriteWebsites, 'i-lucide-link-2')
  const uploadedProjects = normalizeSiteItems(payload.profile?.uploadedWebsites, 'i-lucide-folder')
  const profileTags = hobbyTags

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
    email ? { label: '邮箱', imageIcon: emailIcon, url: `mailto:${email}` } : null,
  ]
  const socialLinkList = socialLinkCandidates.filter(
    (item): item is ProfileSocialLink => item !== null,
  )
  const socialLinks = socialLinkList.filter(
    (item, index, list) => list.findIndex((target) => target.url === item.url) === index,
  )

  const timeline = [
    payload.profile?.createTime
      ? { title: '资料创建时间', date: payload.profile.createTime }
      : null,
    payload.profile?.updateTime
      ? { title: '资料更新时间', date: payload.profile.updateTime }
      : null,
    rawLocation ? { title: '所在地区已更新', date: rawLocation } : null,
    payload.user?.email ? { title: '邮箱已绑定', date: payload.user.email } : null,
    payload.user?.phone ? { title: '手机号已绑定', date: payload.user.phone } : null,
  ].filter((item): item is NonNullable<typeof item> => Boolean(item))

  const contactCount = [
    payload.user?.email,
    payload.user?.phone,
    payload.profile?.githubUrl,
    payload.profile?.giteeUrl,
    website,
  ].filter(Boolean).length
  const contributionText = payload.profile?.contribution?.trim() || ''
  const contributionValueMatch = contributionText.match(/\d+/)
  const contributionValue = contributionValueMatch ? Number(contributionValueMatch[0]) : 0
  const contributions = [
    payload.profile?.signature ? { title: '个性签名', summary: payload.profile.signature } : null,
    contributionText ? { title: '贡献说明', summary: contributionText } : null,
  ].filter((item): item is NonNullable<typeof item> => Boolean(item))

  return {
    name: displayName,
    role: techStack[0] || '程序员',
    location,
    organization: country || '未设置国家',
    signature: payload.profile?.signature || '这个人很懒，还没有填写个性签名。',
    avatarUrl:
      payload.user?.avatar ||
      `https://api.dicebear.com/7.x/notionists/svg?seed=${encodeURIComponent(displayName)}&backgroundColor=f8f9fa`,
    tags: profileTags,
    timeline,
    socialLinks,
    favoriteSites:
      favoriteSites.length > 0
        ? favoriteSites
        : website
          ? [{ name: '个人网站', description: website, icon: 'i-lucide-link-2' }]
          : [],
    uploadedProjects,
    skills: techStack.map((item) => ({ name: item, level: '熟练' as const })),
    contributions:
      contributions.length > 0
        ? contributions
        : [{ title: '贡献记录', summary: '暂无贡献记录，继续保持创作吧。' }],
    stats: [
      { label: '资料完整度', value: `${completionPercent(payload)}%` },
      { label: '联系方式', value: `${contactCount}` },
      { label: '贡献值', value: `${contributionValue}` },
    ],
  }
}

const hasProfile = computed(() => Boolean(profileData.value))

const loadProfile = async () => {
  loading.value = true
  errorText.value = ''
  try {
    const data = await getCurrentUserProfile()
    profileData.value = mapToProfileData(data)
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取个人资料失败'
    errorText.value = message
    profileData.value = null
    toast.add({
      title: '获取资料失败',
      description: message,
      type: 'error',
    })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <div class="min-h-screen dark:bg-black transition-colors duration-300">
    <UContainer class="py-8 md:py-10">
      <div v-if="loading" class="space-y-4">
        <USkeleton class="h-52 w-full rounded-xl" />
        <USkeleton class="h-40 w-full rounded-xl" />
        <USkeleton class="h-40 w-full rounded-xl" />
      </div>

      <div v-else-if="hasProfile && profileData" class="flex flex-col lg:flex-row gap-6 lg:gap-8">
        <ProfileSidebarCard :profile="profileData" />

        <main class="flex-1 min-w-0 space-y-6">
          <ProfileHeroSection :profile="profileData" />
          <ProfileHeatmapSection />
          <ProfileSkillsSection :skills="profileData.skills" />
          <ProfileContributionSection
            :stats="profileData.stats"
            :contributions="profileData.contributions"
          />
        </main>
      </div>

      <UCard v-else class="!ring-0 rounded-xl bg-white dark:!bg-[#131418] shadow-sm dark:shadow-md">
        <div class="space-y-3">
          <p class="text-sm text-gray-600 dark:text-gray-300">
            {{ errorText || '暂无个人资料数据' }}
          </p>
          <UButton
            color="neutral"
            variant="soft"
            @click="loadProfile"
            class="!bg-gray-100 hover:!bg-gray-200 !text-gray-900 dark:!bg-white/10 dark:hover:!bg-white/20 dark:!text-white"
            >重新加载</UButton
          >
        </div>
      </UCard>
    </UContainer>
  </div>
</template>
