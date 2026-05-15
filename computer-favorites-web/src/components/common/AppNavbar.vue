<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import PrimeButton from 'primevue/button'
import FeedbackDialog from '@/components/user/FeedbackDialog.vue'
import { logout as logoutApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useToast } from '@/composables/useToast'
import { useUserMessageUnread } from '@/composables/useUserMessageUnread'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  Bell,
  Bookmark,
  FolderOpen,
  FolderPlus,
  IdCard,
  LogOut,
  Menu,
  Settings,
  User,
  X,
} from 'lucide-vue-next'
import CreateFolderDialog from '@/components/user/CreateFolderDialog.vue'
import { useFolderStore } from '@/stores/folder'

const { t } = useI18n()
const appStore = useAppStore()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const toast = useToast()
const folderStore = useFolderStore()
const profileMenuRef = ref<HTMLElement | null>(null)
const profileMenuOpen = ref(false)
const logoutLoading = ref(false)
const mobileMenuOpen = ref(false)
const feedbackDialogOpen = ref(false)
const createFolderDialogOpen = ref(false)
const avatarLoadFailed = ref(false)
const { unreadMessageCount, hasUnreadMessage, refreshUnreadCount, clearUnreadCount } =
  useUserMessageUnread()

const displayName = computed(() => authStore.userSnapshot.nickname || t('user.profile.title'))
const avatarSrc = computed(() => {
  if (avatarLoadFailed.value) return undefined
  const url = authStore.userSnapshot.avatar
  return url || undefined
})
const initials = computed(() => {
  const text = displayName.value.trim()
  return text ? text.slice(0, 2).toUpperCase() : 'ME'
})
const avatarFallbackBg = computed(() => {
  const colors = [
    'bg-amber-500', 'bg-blue-500', 'bg-emerald-500', 'bg-rose-500',
    'bg-violet-500', 'bg-cyan-500', 'bg-orange-500', 'bg-pink-500',
  ]
  let hash = 0
  for (let i = 0; i < displayName.value.length; i++) {
    hash = displayName.value.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
})

const handleAvatarError = () => {
  avatarLoadFailed.value = true
}
const isHomeRoute = computed(() => route.name === 'home')

const goToLogin = () => {
  mobileMenuOpen.value = false
  router.push({
    name: 'login',
    query: { redirect: route.fullPath },
  })
}

const goToHome = () => {
  mobileMenuOpen.value = false
  router.push({ name: 'home' })
}

const goToProfile = () => {
  profileMenuOpen.value = false
  mobileMenuOpen.value = false
  router.push({ name: 'profile' })
}

const openCreateFolderDialog = async () => {
  profileMenuOpen.value = false
  mobileMenuOpen.value = false
  await folderStore.loadFolderOptions()
  createFolderDialogOpen.value = true
}

const goToWebsiteSubmissions = () => {
  profileMenuOpen.value = false
  mobileMenuOpen.value = false
  router.push({ name: 'websitesUpload' })
}

const goToCollection = () => {
  profileMenuOpen.value = false
  mobileMenuOpen.value = false
  router.push({ name: 'collection' })
}

const goToMessageCenter = () => {
  profileMenuOpen.value = false
  mobileMenuOpen.value = false
  router.push({ name: 'messageCenter' })
}

const goToSettings = () => {
  profileMenuOpen.value = false
  mobileMenuOpen.value = false
  router.push({ name: 'settings' })
}

const goToAccount = () => {
  profileMenuOpen.value = false
  mobileMenuOpen.value = false
  router.push({ name: 'account' })
}

const goToUploadWebsite = async () => {
  profileMenuOpen.value = false
  mobileMenuOpen.value = false

  const uploadRedirect = router.resolve({ name: 'uploadWebsite' }).fullPath

  if (!authStore.isAuthed) {
    await router.push({
      name: 'login',
      query: { redirect: uploadRedirect },
    })
    return
  }

  const valid = await authStore.ensureSession()
  if (!valid) {
    await router.push({
      name: 'login',
      query: { redirect: uploadRedirect },
    })
    return
  }

  await router.push({ name: 'uploadWebsite' })
}

const openFeedbackDialog = async () => {
  profileMenuOpen.value = false
  mobileMenuOpen.value = false

  if (!authStore.isAuthed) {
    await router.push({
      name: 'login',
      query: { redirect: route.fullPath },
    })
    return
  }

  const valid = await authStore.ensureSession()
  if (!valid) {
    await router.push({
      name: 'login',
      query: { redirect: route.fullPath },
    })
    return
  }

  feedbackDialogOpen.value = true
}

const toggleProfileMenu = () => {
  profileMenuOpen.value = !profileMenuOpen.value
}

const handleFolderCreate = async () => {
  mobileMenuOpen.value = false
  profileMenuOpen.value = false
  toast.add({
    title: t('common.success'),
    description: '收藏夹已创建',
    type: 'success',
  })
  await folderStore.refreshFolderData()
}

const handlePendingFeature = () => {
  mobileMenuOpen.value = false
  toast.add({
    title: t('common.info'),
    description: t('common.warning'),
    type: 'info',
  })
}

const logout = async () => {
  if (logoutLoading.value) {
    return
  }
  profileMenuOpen.value = false
  logoutLoading.value = true
  let requestFailed = false
  try {
    await logoutApi()
  } catch (error) {
    requestFailed = true
    console.error(error)
  }
  authStore.clear()
  logoutLoading.value = false
  toast.add({
    title: t('auth.logout.success'),
    description: requestFailed ? t('auth.session.expired') : t('auth.logout.success'),
    type: 'success',
  })
  await router.push({ name: 'login' })
}

const closeProfileMenuByOutside = (event: MouseEvent) => {
  if (!profileMenuRef.value) {
    return
  }
  const target = event.target as Node | null
  if (target && !profileMenuRef.value.contains(target)) {
    profileMenuOpen.value = false
  }
}

watch(
  () => route.fullPath,
  () => {
    profileMenuOpen.value = false
    mobileMenuOpen.value = false
  },
)

watch(
  () => authStore.userSnapshot.userId,
  () => {
    avatarLoadFailed.value = false
  },
)

watch(
  () => authStore.isAuthed,
  (isAuthed) => {
    if (!isAuthed) {
      profileMenuOpen.value = false
      return
    }
    void authStore.loadCurrentUser()
  },
  { immediate: true },
)

watch(
  () => authStore.isSessionValid,
  (isSessionValid) => {
    if (!isSessionValid) {
      clearUnreadCount()
      return
    }
    void refreshUnreadCount()
  },
  { immediate: true },
)

watch(profileMenuOpen, (isOpen) => {
  if (!isOpen || !authStore.isSessionValid) {
    return
  }
  void refreshUnreadCount()
})

watch(mobileMenuOpen, (isOpen) => {
  if (!isOpen || !authStore.isSessionValid) {
    return
  }
  void refreshUnreadCount()
})

onMounted(() => {
  window.addEventListener('click', closeProfileMenuByOutside)
})

onBeforeUnmount(() => {
  window.removeEventListener('click', closeProfileMenuByOutside)
})
</script>

<template>
  <header
    data-testid="navbar-root"
    class="cf-navbar fixed inset-x-0 top-0 z-50 bg-white/65 backdrop-blur-xl backdrop-saturate-150 transition-colors duration-300 ease-[cubic-bezier(0.16,1,0.3,1)] supports-[backdrop-filter]:bg-white/50 dark:bg-black/55 dark:supports-[backdrop-filter]:bg-black/40"
  >
    <div class="h-16 w-full px-2 sm:px-4 lg:px-6">
      <div class="grid h-full grid-cols-[auto_1fr_auto] items-center gap-2 sm:gap-3">
        <div class="flex min-w-0 items-center gap-3 justify-self-start">
          <RouterLink :to="{ name: 'home' }" class="group flex items-center gap-2.5">
            <span
              class="grid h-9 w-9 place-items-center rounded-xl bg-primary-500/[0.08] text-primary-500 ring-1 ring-inset ring-primary-500/15 transition-all duration-300 ease-[cubic-bezier(0.16,1,0.3,1)] group-hover:bg-primary-500/[0.14] group-hover:ring-primary-500/30 dark:bg-primary-500/[0.12] dark:ring-primary-500/25"
            >
              <svg
                class="h-[18px] w-[18px]"
                fill="none"
                stroke="currentColor"
                stroke-width="1.75"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                ></path>
              </svg>
            </span>
            <span
              class="hidden text-[15px] font-semibold tracking-tight text-zinc-900 transition-colors duration-200 group-hover:text-zinc-950 dark:text-zinc-50 sm:inline"
            >
              计算机收藏夹
            </span>
          </RouterLink>
        </div>

        <nav class="hidden items-center gap-1 md:flex md:justify-self-center">
          <RouterLink
            :to="{ name: 'home' }"
            class="cf-nav-link"
            :class="isHomeRoute ? 'cf-nav-link-active' : ''"
          >
            <span class="cf-nav-link-label">{{ t('user.home.hero.browseButton') }}</span>
          </RouterLink>
          <button type="button" class="cf-nav-link" @click="handlePendingFeature">
            <span class="cf-nav-link-label">{{ t('user.home.filter.categories') }}</span>
          </button>
          <RouterLink :to="{ name: 'profile' }" class="cf-nav-link">
            <span class="cf-nav-link-label">{{ t('user.profile.title') }}</span>
          </RouterLink>
          <button type="button" class="cf-nav-link" @click="goToUploadWebsite">
            <span class="cf-nav-link-label">{{ t('user.home.hero.uploadButton') }}</span>
          </button>
          <RouterLink :to="{ name: 'announcement' }" class="cf-nav-link">
            <span class="cf-nav-link-label">平台公告</span>
          </RouterLink>
        </nav>

        <div class="flex items-center gap-2 sm:gap-3 justify-self-end">
          <button
            type="button"
            class="hidden h-9 cursor-pointer items-center gap-1.5 rounded-xl px-3 text-[13px] font-medium text-zinc-600 transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)] hover:bg-zinc-900/[0.04] hover:text-zinc-950 active:scale-[0.97] dark:text-zinc-400 dark:hover:bg-white/[0.06] dark:hover:text-zinc-50 md:inline-flex"
            @click="openFeedbackDialog"
          >
            <i class="fas fa-comment-dots text-[12px] opacity-80"></i>
            {{ t('user.home.feedback.navButton') }}
          </button>
          <button
            type="button"
            class="inline-flex h-9 w-9 cursor-pointer items-center justify-center rounded-xl text-zinc-600 transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)] hover:bg-zinc-900/[0.04] hover:text-zinc-950 active:scale-[0.94] dark:text-zinc-400 dark:hover:bg-white/[0.06] dark:hover:text-zinc-50 md:hidden"
            :aria-label="t('user.home.feedback.navButton')"
            @click="openFeedbackDialog"
          >
            <i class="fas fa-comment-dots text-[13px]"></i>
          </button>
          <button
            @click="appStore.toggleTheme"
            class="grid h-9 w-9 cursor-pointer place-items-center rounded-xl text-zinc-600 transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)] hover:bg-zinc-900/[0.04] hover:text-zinc-950 active:scale-[0.94] dark:text-zinc-400 dark:hover:bg-white/[0.06] dark:hover:text-zinc-50"
            :aria-label="t('common.theme.toggle')"
          >
            <svg
              v-if="!appStore.isDark"
              class="h-[18px] w-[18px]"
              fill="none"
              stroke="currentColor"
              stroke-width="1.75"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
              ></path>
            </svg>
            <svg
              v-else
              class="h-[18px] w-[18px]"
              fill="none"
              stroke="currentColor"
              stroke-width="1.75"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
              ></path>
            </svg>
          </button>
          <button
            v-if="!authStore.isSessionValid"
            @click="goToLogin"
            class="cf-cta-primary hidden h-9 cursor-pointer items-center gap-1.5 rounded-xl px-4 text-[13px] font-medium text-white transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)] active:scale-[0.97] sm:inline-flex"
          >
            <svg
              class="h-4 w-4"
              fill="none"
              stroke="currentColor"
              stroke-width="1.75"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"
              ></path>
            </svg>
            {{ t('auth.login.loginButton') }} / {{ t('auth.login.registerButton') }}
          </button>

          <div
            v-else
            ref="profileMenuRef"
            class="relative"
            @mouseenter="profileMenuOpen = true"
            @mouseleave="profileMenuOpen = false"
          >
            <button
              type="button"
              data-testid="profile-trigger"
              class="grid h-9 w-9 cursor-pointer place-items-center rounded-full text-sm text-zinc-700 transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)] hover:scale-[1.04] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500/40 focus-visible:ring-offset-2 focus-visible:ring-offset-white active:scale-[0.96] dark:text-zinc-300 dark:focus-visible:ring-offset-black"
              @click.stop="toggleProfileMenu"
            >
              <img
                v-if="avatarSrc"
                :src="avatarSrc"
                :alt="displayName"
                class="h-8 w-8 rounded-full object-cover ring-1 ring-inset ring-zinc-900/[0.08] dark:ring-white/10"
                @error="handleAvatarError"
              />
              <div
                v-else
                class="grid h-8 w-8 place-items-center rounded-full text-xs font-semibold text-white ring-1 ring-inset ring-zinc-900/[0.08] dark:ring-white/10"
                :class="avatarFallbackBg"
              >
                {{ initials }}
              </div>
            </button>
            <Transition name="fade">
              <div
                v-if="profileMenuOpen"
                class="absolute right-0 top-[100%]"
              >
                <div class="pt-3">
                  <div
                    data-testid="profile-dropdown"
                    class="w-56 overflow-hidden rounded-2xl bg-white/95 p-1.5 ring-1 ring-zinc-900/[0.06] shadow-[0_20px_44px_-18px_rgba(15,23,42,0.22),0_2px_6px_-2px_rgba(15,23,42,0.08)] backdrop-blur-xl backdrop-saturate-150 dark:bg-zinc-950/85 dark:ring-white/[0.08] dark:shadow-[0_20px_44px_-18px_rgba(0,0,0,0.7)]"
                  >
                    <button type="button" class="cf-menu-item" @click="goToProfile">
                      <User class="size-4" />
                      {{ t('user.profile.title') }}
                    </button>
                    <button type="button" class="cf-menu-item" @click="goToWebsiteSubmissions">
                      <FolderOpen class="size-4" />
                      {{ t('user.profile.mySubmissions') }}
                    </button>
                    <button type="button" class="cf-menu-item" @click="goToCollection">
                      <Bookmark class="size-4" />
                      我的收藏夹
                    </button>
                    <button type="button" class="cf-menu-item" @click="openCreateFolderDialog">
                      <FolderPlus class="size-4" />
                      创建收藏夹
                    </button>
                    <button type="button" class="cf-menu-item" @click="goToMessageCenter">
                      <Bell class="size-4" />
                      消息通知
                      <span
                        v-if="hasUnreadMessage"
                        class="ml-auto inline-flex h-[18px] min-w-[18px] items-center justify-center rounded-full bg-rose-500 px-1.5 text-[10px] font-semibold leading-none text-white ring-2 ring-white dark:ring-zinc-950"
                      >
                        {{ unreadMessageCount > 99 ? '99+' : unreadMessageCount }}
                      </span>
                    </button>
                    <button type="button" class="cf-menu-item" @click="goToSettings">
                      <Settings class="size-4" />
                      {{ t('settings.title') }}
                    </button>
                    <button type="button" class="cf-menu-item" @click="goToAccount">
                      <IdCard class="size-4" />
                      {{ t('settings.sidebar.account') }}
                    </button>
                    <div class="my-1 h-px bg-zinc-900/[0.06] dark:bg-white/[0.06]"></div>
                    <button
                      type="button"
                      :disabled="logoutLoading"
                      class="flex w-full cursor-pointer items-center gap-2.5 rounded-xl px-3 py-2 text-left text-[13px] font-medium text-rose-600 transition-all duration-150 ease-[cubic-bezier(0.16,1,0.3,1)] hover:bg-rose-500/[0.08] active:scale-[0.985] disabled:cursor-not-allowed disabled:opacity-60 dark:text-rose-400 dark:hover:bg-rose-500/[0.12]"
                      @click="logout"
                    >
                      <LogOut class="size-4" />
                      {{ logoutLoading ? t('common.loading') : t('auth.logout.button') }}
                    </button>
                  </div>
                </div>
              </div>
            </Transition>
          </div>

          <button
            type="button"
            class="grid h-9 w-9 cursor-pointer place-items-center rounded-xl text-zinc-700 transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)] hover:bg-zinc-900/[0.04] hover:text-zinc-950 active:scale-[0.94] dark:text-zinc-300 dark:hover:bg-white/[0.06] dark:hover:text-zinc-50 md:hidden"
            :aria-label="mobileMenuOpen ? '关闭导航' : '打开导航'"
            @click="mobileMenuOpen = !mobileMenuOpen"
          >
            <Menu v-if="!mobileMenuOpen" class="h-[18px] w-[18px]" :stroke-width="1.75" />
            <X v-else class="h-[18px] w-[18px]" :stroke-width="1.75" />
          </button>
        </div>
      </div>
    </div>

    <Transition name="fade">
      <div
        v-if="mobileMenuOpen"
        data-testid="navbar-mobile-menu"
        class="bg-white/95 px-3 pb-3 pt-2 backdrop-blur-xl backdrop-saturate-150 shadow-[inset_0_1px_0_0_rgba(15,23,42,0.06)] dark:bg-black/90 dark:shadow-[inset_0_1px_0_0_rgba(255,255,255,0.06)] md:hidden"
      >
        <PrimeButton
          text
          class="!mb-1 !flex !w-full !cursor-pointer !justify-start !rounded-lg !px-3 !py-2 !text-sm !font-medium"
          :class="
            isHomeRoute
              ? '!text-primary-500 dark:!text-primary-400'
              : '!text-gray-600 dark:!text-gray-300'
          "
          @click="goToHome"
        >
          {{ t('user.home.hero.browseButton') }}
        </PrimeButton>
        <PrimeButton
          text
          class="!mb-1 !flex !w-full !cursor-pointer !justify-start !rounded-lg !px-3 !py-2 !text-sm !font-medium !text-gray-600 dark:!text-gray-300"
          @click="handlePendingFeature"
        >
          {{ t('user.home.filter.categories') }}
        </PrimeButton>
        <PrimeButton
          text
          class="!mb-1 !flex !w-full !cursor-pointer !justify-start !rounded-lg !px-3 !py-2 !text-sm !font-medium !text-gray-600 dark:!text-gray-300"
          @click="goToProfile"
        >
          {{ t('user.profile.title') }}
        </PrimeButton>
        <PrimeButton
          text
          class="!mb-1 !flex !w-full !cursor-pointer !justify-start !rounded-lg !px-3 !py-2 !text-sm !font-medium !text-gray-600 dark:!text-gray-300"
          @click="goToWebsiteSubmissions"
        >
          {{ t('user.profile.mySubmissions') }}
        </PrimeButton>
        <PrimeButton
          text
          class="!mb-1 !flex !w-full !cursor-pointer !justify-start !rounded-lg !px-3 !py-2 !text-sm !font-medium !text-gray-600 dark:!text-gray-300"
          @click="goToCollection"
        >
          <div class="flex items-center gap-2">
            <Bookmark class="size-4" />
            我的收藏夹
          </div>
        </PrimeButton>
        <PrimeButton
          text
          class="!mb-1 !flex !w-full !cursor-pointer !justify-start !rounded-lg !px-3 !py-2 !text-sm !font-medium !text-gray-600 dark:!text-gray-300"
          @click="openCreateFolderDialog"
        >
          <div class="flex items-center gap-2">
            <FolderPlus class="size-4" />
            创建收藏夹
          </div>
        </PrimeButton>
        <PrimeButton
          text
          class="!mb-1 !flex !w-full !cursor-pointer !justify-start !rounded-lg !px-3 !py-2 !text-sm !font-medium !text-gray-600 dark:!text-gray-300"
          @click="goToMessageCenter"
        >
          <div class="flex w-full items-center justify-between">
            <span>消息通知</span>
            <span
              v-if="hasUnreadMessage"
              class="inline-flex h-[18px] min-w-[18px] items-center justify-center rounded-full bg-rose-500 px-1.5 text-[10px] font-semibold leading-none text-white ring-2 ring-white dark:ring-zinc-950"
            >
              {{ unreadMessageCount > 99 ? '99+' : unreadMessageCount }}
            </span>
          </div>
        </PrimeButton>
        <PrimeButton
          text
          class="!mb-1 !flex !w-full !cursor-pointer !justify-start !rounded-lg !px-3 !py-2 !text-sm !font-medium !text-gray-600 dark:!text-gray-300"
          @click="openFeedbackDialog"
        >
          <div class="flex items-center gap-2">
            <i class="fas fa-comment-dots text-xs"></i>
            {{ t('user.home.feedback.navButton') }}
          </div>
        </PrimeButton>
        <PrimeButton
          text
          class="!mb-1 !flex !w-full !cursor-pointer !justify-start !rounded-lg !px-3 !py-2 !text-sm !font-medium !text-gray-600 dark:!text-gray-300"
          @click="goToUploadWebsite"
        >
          {{ t('user.home.hero.uploadButton') }}
        </PrimeButton>
        <RouterLink
          :to="{ name: 'announcement' }"
          class="!mb-1 !flex !w-full !cursor-pointer !justify-start !rounded-lg !px-3 !py-2 !text-sm !font-medium !text-gray-600 transition-colors hover:!text-primary-500 dark:!text-gray-300 dark:hover:!text-primary-400"
          @click="mobileMenuOpen = false"
        >
          平台公告
        </RouterLink>
        <PrimeButton
          v-if="!authStore.isSessionValid"
          class="!mt-2 !flex !w-full !cursor-pointer !justify-center !rounded-lg !bg-primary-500 !px-3 !py-2 !text-sm !font-medium !text-white hover:!bg-primary-600"
          @click="goToLogin"
        >
          {{ t('auth.login.loginButton') }} / {{ t('auth.login.registerButton') }}
        </PrimeButton>
      </div>
    </Transition>
  </header>
  <FeedbackDialog v-model:open="feedbackDialogOpen" />
  <CreateFolderDialog
    v-model:open="createFolderDialogOpen"
    :parent-options="folderStore.folderOptions"
    @submit="handleFolderCreate"
  />
</template>

<style scoped>
/* 顶部导航玻璃材质：1px 内边框模拟物理折射，避免塑料感的纯实边框 */
.cf-navbar {
  box-shadow:
    inset 0 -1px 0 0 rgba(15, 23, 42, 0.06),
    inset 0 1px 0 0 rgba(255, 255, 255, 0.55);
}

.dark .cf-navbar {
  box-shadow:
    inset 0 -1px 0 0 rgba(255, 255, 255, 0.05),
    inset 0 1px 0 0 rgba(255, 255, 255, 0.04);
}

/* 中间链接：去掉色彩硬切换，依赖字体重量 + 下划指示器 */
.cf-nav-link {
  position: relative;
  display: inline-flex;
  align-items: center;
  height: 2.25rem;
  padding: 0 0.75rem;
  border-radius: 0.75rem;
  font-size: 0.8125rem;
  font-weight: 500;
  letter-spacing: -0.01em;
  color: rgb(82 82 91 / 1);
  cursor: pointer;
  transition:
    color 200ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 200ms cubic-bezier(0.16, 1, 0.3, 1);
}

.cf-nav-link:hover {
  color: rgb(24 24 27 / 1);
  background-color: rgb(24 24 27 / 0.035);
}

.cf-nav-link-active {
  color: rgb(24 24 27 / 1);
}

.cf-nav-link-label {
  position: relative;
  display: inline-block;
}

.cf-nav-link-label::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -0.4375rem;
  height: 2px;
  width: 0;
  border-radius: 999px;
  background-color: rgb(var(--cf-color-primary-500-rgb, 245 158 11));
  transform: translateX(-50%);
  transition:
    width 320ms cubic-bezier(0.16, 1, 0.3, 1),
    opacity 240ms cubic-bezier(0.16, 1, 0.3, 1);
  opacity: 0;
}

.cf-nav-link:hover .cf-nav-link-label::after {
  width: 60%;
  opacity: 0.55;
}

.cf-nav-link-active .cf-nav-link-label::after {
  width: 100%;
  opacity: 1;
}

.dark .cf-nav-link {
  color: rgb(161 161 170 / 1);
}

.dark .cf-nav-link:hover {
  color: rgb(244 244 245 / 1);
  background-color: rgb(255 255 255 / 0.04);
}

.dark .cf-nav-link-active {
  color: rgb(255 255 255 / 1);
}

/* 下拉菜单条目：圆角胶囊化，hover 用 4% 不透明度填充代替灰底卡片 */
.cf-menu-item {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 0.625rem;
  border-radius: 0.75rem;
  padding: 0.5rem 0.75rem;
  font-size: 0.8125rem;
  font-weight: 500;
  text-align: left;
  color: rgb(63 63 70 / 1);
  cursor: pointer;
  transition:
    color 150ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 150ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 150ms cubic-bezier(0.16, 1, 0.3, 1);
}

.cf-menu-item:hover {
  color: rgb(9 9 11 / 1);
  background-color: rgb(24 24 27 / 0.04);
}

.cf-menu-item:active {
  transform: scale(0.985);
}

.dark .cf-menu-item {
  color: rgb(212 212 216 / 1);
}

.dark .cf-menu-item:hover {
  color: rgb(250 250 250 / 1);
  background-color: rgb(255 255 255 / 0.06);
}

/* 主 CTA：用顶部高光 + 微阴影模拟物理深度，去掉刺眼的纯色 shadow-sm */
.cf-cta-primary {
  background-color: rgb(var(--cf-color-primary-500-rgb, 245 158 11));
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.22),
    inset 0 -1px 0 0 rgba(0, 0, 0, 0.08),
    0 1px 2px 0 rgba(15, 23, 42, 0.08),
    0 4px 12px -4px rgba(var(--cf-color-primary-500-rgb, 245 158 11), 0.35);
}

.cf-cta-primary:hover {
  background-color: rgb(var(--cf-color-primary-600-rgb, 217 119 6));
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.2),
    inset 0 -1px 0 0 rgba(0, 0, 0, 0.12),
    0 2px 4px 0 rgba(15, 23, 42, 0.1),
    0 8px 18px -6px rgba(var(--cf-color-primary-500-rgb, 245 158 11), 0.45);
}

/* 下拉/移动菜单 fade 过渡：使用 cubic-bezier(0.16,1,0.3,1) overshoot 缓动 */
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 240ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 240ms cubic-bezier(0.16, 1, 0.3, 1);
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.99);
}
</style>
