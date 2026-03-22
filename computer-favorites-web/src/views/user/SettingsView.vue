<script setup lang="ts">
import { reactive, ref, shallowRef, provide, onMounted } from 'vue'
// @ts-ignore
import SettingsSidebar from '@/components/user/settings/SettingsSidebar.vue'
// @ts-ignore
import BasicProfileSection from '@/components/user/settings/sections/BasicProfileSection.vue'
// @ts-ignore
import AccountSettingsSection from '@/components/user/settings/sections/AccountSettingsSection.vue'
// @ts-ignore
import PreferenceSettingsSection from '@/components/user/settings/sections/PreferenceSettingsSection.vue'
// @ts-ignore
import DataManagementSection from '@/components/user/settings/sections/DataManagementSection.vue'
// @ts-ignore
import MessageSettingsSection from '@/components/user/settings/sections/MessageSettingsSection.vue'
import { mockUserBasicInfo, mockUserDetailProfile, mockUserPreferenceSetting } from '@/components/user/settings/mock'
import { settingsStateKey } from '@/components/user/settings/context'
import { getCurrentUserProfile, updateCurrentUserProfile } from '@/services/profile'
import { useToast } from '@/composables/useToast'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'

defineOptions({
  name: 'SettingsView',
})

const tabs = [
  { id: 'basic', label: '基础资料', icon: 'i-lucide-user', component: BasicProfileSection },
  { id: 'account', label: '账号设置', icon: 'i-lucide-shield', component: AccountSettingsSection },
  { id: 'preference', label: '偏好设置', icon: 'i-lucide-sliders', component: PreferenceSettingsSection },
  { id: 'message', label: '消息设置', icon: 'i-lucide-bell', component: MessageSettingsSection },
  { id: 'data', label: '数据管理', icon: 'i-lucide-database', component: DataManagementSection },
]

const settingsState = reactive({
  basicInfo: { ...mockUserBasicInfo },
  profile: { ...mockUserDetailProfile },
  setting: { ...mockUserPreferenceSetting },
})
const appStore = useAppStore()
const authStore = useAuthStore()
const toast = useToast()
const savingProfile = ref(false)

provide(settingsStateKey, settingsState)

const firstTab = tabs[0]
if (!firstTab) {
  throw new Error('Settings tabs are not configured')
}

const activeTabId = ref(firstTab.id)
const activeComponent = shallowRef(firstTab.component)

const handleTabChange = (id: string) => {
  activeTabId.value = id
  const tab = tabs.find((t) => t.id === id)
  if (tab) {
    activeComponent.value = tab.component
  }
}

const handleSaveAllChanges = async () => {
  if (savingProfile.value) {
    return
  }

  savingProfile.value = true
  try {
    await updateCurrentUserProfile({
      nickname: settingsState.basicInfo.nickname,
      gender: settingsState.profile.gender,
      country: settingsState.profile.country,
      city: settingsState.profile.city,
      githubUrl: settingsState.profile.githubUrl,
      giteeUrl: settingsState.profile.giteeUrl,
      otherRepoLinks: settingsState.profile.otherRepoLinks,
      blogUrl: settingsState.profile.blogUrl,
      signature: settingsState.profile.signature,
      hobbyTags: settingsState.profile.hobbyTags,
      techStack: settingsState.profile.techStack,
    })

    authStore.setUserSnapshot({
      nickname: settingsState.basicInfo.nickname,
      avatar: settingsState.basicInfo.avatar,
    })

    try {
      await syncSettingsStateFromApi()
      toast.add({
        title: '保存成功',
        description: '个人资料已更新',
        type: 'success',
      })
    } catch {
      toast.add({
        title: '保存成功',
        description: '个人资料已保存，但刷新最新资料失败',
        type: 'warning',
      })
    }
  } catch (error) {
    const description = error instanceof Error ? error.message : '保存失败，请稍后重试'
    toast.add({
      title: '保存失败',
      description,
      type: 'error',
    })
  } finally {
    savingProfile.value = false
  }
}

const normalizeString = (value: string | undefined, fallback: string): string => {
  return typeof value === 'string' ? value : fallback
}

const normalizeNumber = (value: number | undefined, fallback: number): number => {
  return typeof value === 'number' ? value : fallback
}

const normalizeTheme = (value: string | undefined): string => {
  return value === 'light' || value === 'dark' || value === 'system' ? value : mockUserPreferenceSetting.theme
}

const normalizeLanguage = (value: string | undefined): string => {
  return value === 'zh-CN' || value === 'en-US' ? value : mockUserPreferenceSetting.language
}

const normalizeHomepageStyle = (value: string | undefined): string => {
  return value === 'card' || value === 'list' ? value : mockUserPreferenceSetting.homepageStyle
}

const normalizePageSize = (value: number | undefined): number => {
  return value === 10 || value === 20 || value === 50 ? value : mockUserPreferenceSetting.pageSize
}

const syncSettingsStateFromApi = async () => {
  const data = await getCurrentUserProfile()
  const user = data.user
  const profile = data.profile
  const setting = data.setting

  Object.assign(settingsState.basicInfo, {
    id: normalizeNumber(user?.id, mockUserBasicInfo.id),
    username: normalizeString(user?.username, mockUserBasicInfo.username),
    email: normalizeString(user?.email, mockUserBasicInfo.email),
    phone: normalizeString(user?.phone, mockUserBasicInfo.phone),
    avatar: normalizeString(user?.avatar, mockUserBasicInfo.avatar),
    nickname: normalizeString(user?.nickname, mockUserBasicInfo.nickname),
    emailVerified: normalizeNumber(user?.emailVerified, mockUserBasicInfo.emailVerified),
    phoneVerified: normalizeNumber(user?.phoneVerified, mockUserBasicInfo.phoneVerified),
  })

  Object.assign(settingsState.profile, {
    gender: normalizeNumber(profile?.gender, mockUserDetailProfile.gender),
    country: normalizeString(profile?.country, mockUserDetailProfile.country),
    city: normalizeString(profile?.city, mockUserDetailProfile.city),
    githubUrl: normalizeString(profile?.githubUrl, mockUserDetailProfile.githubUrl),
    giteeUrl: normalizeString(profile?.giteeUrl, mockUserDetailProfile.giteeUrl),
    otherRepoLinks: normalizeString(profile?.otherRepoLinks, mockUserDetailProfile.otherRepoLinks),
    blogUrl: normalizeString(profile?.blogUrl, mockUserDetailProfile.blogUrl),
    signature: normalizeString(profile?.signature, mockUserDetailProfile.signature),
    hobbyTags: normalizeString(profile?.hobbyTags, mockUserDetailProfile.hobbyTags),
    techStack: normalizeString(profile?.techStack, mockUserDetailProfile.techStack),
    favoriteWebsites: normalizeString(profile?.favoriteWebsites, mockUserDetailProfile.favoriteWebsites),
    uploadedWebsites: normalizeString(profile?.uploadedWebsites, mockUserDetailProfile.uploadedWebsites),
    contribution: normalizeString(profile?.contribution, mockUserDetailProfile.contribution),
  })

  Object.assign(settingsState.setting, {
    theme: normalizeTheme(setting?.theme),
    language: normalizeLanguage(setting?.language),
    emailNotice: normalizeNumber(setting?.emailNotice, mockUserPreferenceSetting.emailNotice),
    collectNotice: normalizeNumber(setting?.collectNotice, mockUserPreferenceSetting.collectNotice),
    commentNotice: normalizeNumber(setting?.commentNotice, mockUserPreferenceSetting.commentNotice),
    homepageStyle: normalizeHomepageStyle(setting?.homepageStyle),
    pageSize: normalizePageSize(setting?.pageSize),
  })
}

onMounted(async () => {
  try {
    await syncSettingsStateFromApi()
  } catch {
    toast.add({
      title: '资料加载失败',
      description: '获取最新资料失败，已显示本地默认信息',
      type: 'warning',
    })
  } finally {
    appStore.finishRouteTransition()
  }
})
</script>

<template>
  <div class="min-h-[calc(100vh-8rem)] bg-slate-50 transition-colors duration-300 dark:bg-black">
    <UContainer class="max-w-6xl py-6 md:py-10">
      <div class="flex justify-end pb-4">
        <UButton :loading="savingProfile" :disabled="savingProfile" variant="solid" class="cursor-pointer !bg-[#f59e0b] !text-white hover:!bg-[#d97706] active:!bg-[#d97706] focus-visible:!outline-[#f59e0b] disabled:cursor-not-allowed disabled:opacity-70" @click="handleSaveAllChanges">
          保存全部更改
        </UButton>
      </div>
      <div class="flex flex-col gap-8 md:flex-row">
        <!-- Sidebar -->
        <div class="w-full shrink-0 md:w-64">
          <SettingsSidebar
            :tabs="tabs"
            :active-id="activeTabId"
            @change="handleTabChange"
          />
        </div>

        <!-- Content Area -->
        <main class="min-w-0 flex-1">
          <transition name="fade" mode="out-in">
            <component :is="activeComponent" />
          </transition>
        </main>
      </div>
    </UContainer>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(4px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
