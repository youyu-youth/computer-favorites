<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, provide, reactive, ref, shallowRef } from 'vue'
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
const contentWrapperRef = ref<HTMLElement | null>(null)
const lockedContentHeight = ref<number | null>(null)
const stableContentMinHeight = ref(0)
const viewportMinHeight = ref('calc(100dvh - 4rem)')
let releaseHeightFrame = 0
let transitionSerial = 0

// 记录稳定的基线高度，保证较短分区也能贴合页脚区域
const syncStableContentMinHeight = (height?: number) => {
  const measuredHeight = typeof height === 'number' ? height : (contentWrapperRef.value?.offsetHeight ?? 0)
  if (measuredHeight > stableContentMinHeight.value) {
    stableContentMinHeight.value = Math.round(measuredHeight)
  }
}

// 通过视口高度兜底，确保设置页在短内容场景下也能与页脚贴合
const syncViewportMinHeight = () => {
  if (typeof window === 'undefined') {
    return
  }
  const viewportHeight = window.visualViewport?.height ?? window.innerHeight
  const footerElement = document.querySelector('footer, .u-footer') as HTMLElement | null
  const footerHeight = footerElement?.offsetHeight ?? 0
  const navbarHeight = 64
  const computedHeight = Math.max(Math.round(viewportHeight - navbarHeight - footerHeight), 0)
  viewportMinHeight.value = `${computedHeight}px`
}

// 切换期间通过最小高度锁定内容容器，避免页脚因内容高度瞬变而跳动
const contentWrapperStyle = computed(() => {
  const lockHeight = lockedContentHeight.value ?? 0
  const baseHeight = stableContentMinHeight.value
  const targetHeight = Math.max(lockHeight, baseHeight)
  if (targetHeight <= 0) {
    return undefined
  }
  return {
    minHeight: `${targetHeight}px`,
  }
})

// 统一封装高度锁定，保证高度值安全
const lockContentHeight = (height: number) => {
  lockedContentHeight.value = Math.max(0, Math.round(height))
}

// 在下一帧释放高度锁，避免与当前过渡帧竞争导致抖动
const releaseContentHeight = (serial: number) => {
  if (releaseHeightFrame) {
    cancelAnimationFrame(releaseHeightFrame)
  }
  releaseHeightFrame = requestAnimationFrame(() => {
    // 仅允许最后一次切换释放高度锁，防止快速切换时提前解锁
    if (serial === transitionSerial) {
      lockedContentHeight.value = null
    }
    releaseHeightFrame = 0
  })
}

// 离场前锁定当前容器高度，保持布局稳定
const handleBeforeLeave = (el: Element) => {
  const wrapperHeight = contentWrapperRef.value?.offsetHeight ?? 0
  const currentHeight = (el as HTMLElement).offsetHeight
  lockContentHeight(Math.max(wrapperHeight, currentHeight))
}

// 入场时若新内容更高，立即扩展锁定高度，避免下方内容被压缩
const handleEnter = (el: Element) => {
  const enteringHeight = (el as HTMLElement).offsetHeight
  const currentLockHeight = lockedContentHeight.value ?? 0
  if (enteringHeight > currentLockHeight) {
    lockContentHeight(enteringHeight)
  }
}

// 入场结束后释放锁定高度，回归自然文档流
const handleAfterEnter = (el: Element) => {
  const serialSnapshot = transitionSerial
  const enteredHeight = (el as HTMLElement).offsetHeight
  lockContentHeight(enteredHeight)
  syncStableContentMinHeight(enteredHeight)
  releaseContentHeight(serialSnapshot)
}

const handleTabChange = (id: string) => {
  if (id === activeTabId.value) {
    return
  }

  const tab = tabs.find((t) => t.id === id)
  if (!tab) {
    return
  }

  transitionSerial += 1

  const currentHeight = contentWrapperRef.value?.offsetHeight ?? 0
  if (currentHeight > 0) {
    lockContentHeight(currentHeight)
  }

  activeTabId.value = id
  activeComponent.value = tab.component
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
    createTime: normalizeString(profile?.createTime, ''),
    updateTime: normalizeString(profile?.updateTime, ''),
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

onBeforeUnmount(() => {
  if (releaseHeightFrame) {
    cancelAnimationFrame(releaseHeightFrame)
    releaseHeightFrame = 0
  }
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', syncViewportMinHeight)
  }
})

onMounted(async () => {
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', syncViewportMinHeight)
  }

  try {
    await syncSettingsStateFromApi()
  } catch {
    toast.add({
      title: '资料加载失败',
      description: '获取最新资料失败，已显示本地默认信息',
      type: 'warning',
    })
  } finally {
    await nextTick()
    syncStableContentMinHeight()
    syncViewportMinHeight()
    appStore.finishRouteTransition()
  }
})
</script>

<template>
  <div :style="{ minHeight: viewportMinHeight }" class="flex h-full w-full flex-col bg-slate-50 transition-colors duration-300 dark:bg-black">
    <UContainer class="flex min-h-full w-full max-w-6xl flex-1 flex-col pt-6 pb-0 md:pt-10 md:pb-0">
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
          <div ref="contentWrapperRef" class="settings-content-shell" :style="contentWrapperStyle">
            <Transition
              name="settings-switch"
              mode="out-in"
              @before-leave="handleBeforeLeave"
              @enter="handleEnter"
              @after-enter="handleAfterEnter"
            >
              <section :key="activeTabId" class="settings-content-panel">
                <component :is="activeComponent" />
              </section>
            </Transition>
          </div>
        </main>
      </div>
    </UContainer>
  </div>
</template>

<style scoped>
.settings-content-shell {
  position: relative;
  contain: layout paint;
}

.settings-content-panel {
  width: 100%;
}

.settings-switch-enter-active,
.settings-switch-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
  will-change: opacity, transform;
}

.settings-switch-enter-from,
.settings-switch-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

.settings-switch-leave-active {
  position: absolute;
  inset: 0;
  width: 100%;
  pointer-events: none;
}

@media (prefers-reduced-motion: reduce) {
  .settings-switch-enter-active,
  .settings-switch-leave-active {
    transition: none;
  }

  .settings-switch-enter-from,
  .settings-switch-leave-to {
    opacity: 1;
    transform: none;
  }
}
</style>
