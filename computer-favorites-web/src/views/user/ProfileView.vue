<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import ProfileDashboardCharts from '@/components/user/profile/ProfileDashboardCharts.vue'
import ProfileDashboardSummary from '@/components/user/profile/ProfileDashboardSummary.vue'
import ProfileHeatmapSection from '@/components/user/profile/ProfileHeatmapSection.vue'
import ProfileImpactSection from '@/components/user/profile/ProfileImpactSection.vue'
import ProfileSidebarCard from '@/components/user/profile/ProfileSidebarCard.vue'
import ProfileSkillsSection from '@/components/user/profile/ProfileSkillsSection.vue'
import { getCurrentUserProfile, type LoginUserProfileResponse } from '@/api/user'
import { useToast } from '@/composables/useToast'
import { useUserApprovedSubmissions } from '@/composables/useUserApprovedSubmissions'
import { useProfileDashboardStore } from '@/stores/profileDashboard'
import type { ProfileData, ProfileSiteItem, ProfileSkill, ProfileSocialLink } from '@/types/profile'

import githubIcon from '@/assets/icons/svg/github.svg'
import giteeIcon from '@/assets/icons/svg/gitee.svg'
import blogIcon from '@/assets/icons/svg/blog.svg'
import emailIcon from '@/assets/icons/svg/email.svg'

defineOptions({
  name: 'ProfileView',
})

const toast = useToast()
const dashboardStore = useProfileDashboardStore()
const approvedSubmissions = useUserApprovedSubmissions({ limit: 5 })
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

const normalizeSkillLevel = (value: unknown): ProfileSkill['level'] => {
  return value === '基础' || value === '精通' ? value : '熟练'
}

const normalizeProfileSkillItem = (item: unknown): ProfileSkill | null => {
  if (typeof item === 'string') {
    const name = item.trim()
    return name ? { name, level: '熟练' } : null
  }

  if (typeof item !== 'object' || item === null) {
    return null
  }

  const record = item as Record<string, unknown>
  const name = String(record.name ?? record.title ?? '').trim()
  if (!name) {
    return null
  }

  const iconPng = String(
    record.iconPng ?? record.icon_png ?? record.iconUrl ?? record.icon_url ?? '',
  ).trim()
  const officialUrl = String(record.officialUrl ?? record.official_url ?? record.url ?? '').trim()
  const color = String(record.color ?? '').trim()
  const description = String(record.description ?? record.desc ?? '').trim()

  return {
    name,
    level: normalizeSkillLevel(record.level),
    ...(iconPng ? { iconPng } : {}),
    ...(officialUrl ? { officialUrl } : {}),
    ...(color ? { color } : {}),
    ...(description ? { description } : {}),
  }
}

const normalizeProfileSkills = (value: unknown): ProfileSkill[] => {
  if (Array.isArray(value)) {
    return value
      .map(normalizeProfileSkillItem)
      .filter((item): item is ProfileSkill => Boolean(item))
  }

  if (typeof value !== 'string') {
    return []
  }

  const input = value.trim()
  if (!input) {
    return []
  }

  if (
    (input.startsWith('[') && input.endsWith(']')) ||
    (input.startsWith('{') && input.endsWith('}'))
  ) {
    try {
      const parsed = JSON.parse(input)
      const parsedItems = Array.isArray(parsed) ? parsed : [parsed]
      return parsedItems
        .map(normalizeProfileSkillItem)
        .filter((item): item is ProfileSkill => Boolean(item))
    } catch {
      return []
    }
  }

  return input
    .split(/[，,、|]/)
    .map(normalizeProfileSkillItem)
    .filter((item): item is ProfileSkill => Boolean(item))
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
  const normalizeUrl = (value: string | null | undefined): string => (value ?? '').trim()
  const githubUrl = normalizeUrl(payload.profile?.githubUrl)
  const giteeUrl = normalizeUrl(payload.profile?.giteeUrl)
  const blogUrl = normalizeUrl(payload.profile?.blogUrl)
  const website = blogUrl
  const email = (payload.user?.email ?? '').trim()
  const hobbyTags = normalizeStringArray(payload.profile?.hobbyTags)
  const techStack = normalizeProfileSkills(payload.profile?.techStack)
  const favoriteSites = normalizeSiteItems(payload.profile?.favoriteWebsites, 'i-lucide-link-2')
  const profileTags = hobbyTags

  const socialLinkCandidates: Array<ProfileSocialLink | null> = [
    githubUrl ? { label: 'GitHub', imageIcon: githubIcon, url: githubUrl } : null,
    giteeUrl ? { label: 'Gitee', imageIcon: giteeIcon, url: giteeUrl } : null,
    blogUrl ? { label: '博客', imageIcon: blogIcon, url: blogUrl } : null,
    email ? { label: '邮箱', imageIcon: emailIcon, url: `mailto:${email}` } : null,
  ]
  const socialLinkList = socialLinkCandidates.filter(
    (item): item is ProfileSocialLink => item !== null,
  )
  const socialLinks = socialLinkList.filter(
    (item, index, list) => list.findIndex((target) => target.label === item.label) === index,
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
    role: techStack[0]?.name || '程序员',
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
    uploadedProjects: [],
    skills: techStack,
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

/**
 * 将真实审核通过的投稿合并进 profileData，作为「上传网站」卡片的真实数据源
 */
const displayProfile = computed<ProfileData | null>(() => {
  if (!profileData.value) {
    return null
  }
  return {
    ...profileData.value,
    uploadedProjects: approvedSubmissions.items.value,
  }
})

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
  // 并发触发看板 5 个卡片首屏拉取（单卡失败不影响其他）
  dashboardStore.loadInitial()
  // 并发拉取当前用户审核通过的投稿，作为侧边栏「上传网站」卡片真实数据源
  approvedSubmissions.load()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 transition-colors duration-300 dark:bg-black">
    <div class="mx-auto max-w-7xl px-3 py-6 sm:px-4 sm:py-8 lg:px-6 lg:py-10">
      <div v-if="loading" class="space-y-3">
        <div class="h-44 w-full animate-pulse rounded-md bg-black/5 dark:bg-white/5" />
        <div class="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
          <div
            v-for="i in 6"
            :key="i"
            class="h-28 w-full animate-pulse rounded-md bg-black/5 dark:bg-white/5"
          />
        </div>
        <div class="h-40 w-full animate-pulse rounded-md bg-black/5 dark:bg-white/5" />
      </div>

      <div
        v-else-if="hasProfile && displayProfile"
        class="flex flex-col gap-4 lg:flex-row lg:gap-5"
      >
        <ProfileSidebarCard :profile="displayProfile" />

        <main class="min-w-0 flex-1 space-y-4 lg:pt-3">
          <ProfileDashboardCharts />
          <ProfileHeatmapSection />
          <ProfileImpactSection />

          <ProfileSkillsSection :skills="displayProfile.skills" />
          <ProfileDashboardSummary />
        </main>
      </div>

      <section
        v-else
        class="rounded-md border border-black/5 bg-white p-4 dark:border-white/[0.06] dark:bg-black"
      >
        <div class="space-y-3">
          <p class="text-sm text-gray-600 dark:text-gray-300">
            {{ errorText || '暂无个人资料数据' }}
          </p>
          <button
            type="button"
            class="cursor-pointer rounded-sm border border-amber-500/30 bg-amber-500/10 px-3 py-1.5 font-mono text-[12px] text-amber-600 transition-colors hover:bg-amber-500/20 dark:text-amber-400"
            @click="loadProfile"
          >
            重新加载
          </button>
        </div>
      </section>
    </div>
  </div>
</template>
