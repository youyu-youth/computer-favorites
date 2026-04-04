<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import PrimeButton from 'primevue/button'
import Avatar from 'primevue/avatar'
import { logout as logoutApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useToast } from '@/composables/useToast'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ChevronDown, IdCard, LogOut, Menu, Settings, User, X } from 'lucide-vue-next'

const { t } = useI18n()
const appStore = useAppStore()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const toast = useToast()
const profileMenuRef = ref<HTMLElement | null>(null)
const profileMenuOpen = ref(false)
const logoutLoading = ref(false)
const mobileMenuOpen = ref(false)

const displayName = computed(() => authStore.userSnapshot.nickname || t('user.profile.title'))
const defaultAvatar = computed(
  () => `https://api.dicebear.com/7.x/notionists/svg?seed=${encodeURIComponent(displayName.value)}`,
)
const avatarSrc = computed(() => authStore.userSnapshot.avatar || defaultAvatar.value)
const initials = computed(() => {
  const text = displayName.value.trim()
  return text ? text.slice(0, 2).toUpperCase() : 'ME'
})
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

const toggleProfileMenu = () => {
  profileMenuOpen.value = !profileMenuOpen.value
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
    class="fixed inset-x-0 top-0 z-50 glass-nav border-b border-gray-200 bg-white/70 backdrop-blur-md dark:border-dark-border dark:bg-dark-bg/80"
  >
    <div class="mx-auto h-16 max-w-7xl px-4 sm:px-6 lg:px-8">
      <div class="flex h-full items-center justify-between gap-3">
        <div class="flex items-center gap-3">
          <RouterLink :to="{ name: 'home' }" class="flex items-center gap-3">
            <svg
              class="w-8 h-8 text-primary-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
              ></path>
            </svg>
            <span
              class="hidden text-xl font-bold tracking-tight text-gray-900 dark:text-white sm:inline"
              >Computer Favorites</span
            >
          </RouterLink>
        </div>

        <div class="hidden items-center space-x-6 md:flex">
          <RouterLink
            :to="{ name: 'home' }"
            :class="[
              'text-sm font-medium transition-colors',
              isHomeRoute
                ? 'text-primary-500 dark:text-primary-400'
                : 'text-gray-600 hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400',
            ]"
          >
            {{ t('user.home.hero.browseButton') }}
          </RouterLink>
          <button
            type="button"
            class="text-sm font-medium text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400"
            @click="handlePendingFeature"
          >
            {{ t('user.home.filter.categories') }}
          </button>
          <RouterLink
            :to="{ name: 'profile' }"
            class="text-sm font-medium text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400"
          >
            {{ t('user.profile.title') }}
          </RouterLink>
          <button
            type="button"
            class="text-sm font-medium text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400"
            @click="handlePendingFeature"
          >
            {{ t('user.home.hero.uploadButton') }}
          </button>
        </div>

        <div class="flex items-center gap-3 sm:gap-4">
          <button
            @click="appStore.toggleTheme"
            class="cursor-pointer rounded-full p-2 text-gray-500 transition-colors hover:bg-gray-200 dark:text-gray-400 dark:hover:bg-gray-800"
            :aria-label="t('common.theme.toggle')"
          >
            <svg
              v-if="!appStore.isDark"
              class="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
              ></path>
            </svg>
            <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
              ></path>
            </svg>
          </button>
          <button
            v-if="!authStore.isSessionValid"
            @click="goToLogin"
            class="hidden cursor-pointer items-center gap-2 rounded-md bg-primary-500 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-primary-600 sm:flex"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"
              ></path>
            </svg>
            {{ t('auth.login.loginButton') }} / {{ t('auth.login.registerButton') }}
          </button>

          <div v-else ref="profileMenuRef" class="relative">
            <button
              type="button"
              data-testid="profile-trigger"
              class="inline-flex h-10 cursor-pointer items-center gap-2 rounded-full border border-gray-200 bg-white px-2.5 pr-3 text-sm text-gray-700 transition-colors hover:bg-gray-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-400 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-white/5"
              @click.stop="toggleProfileMenu"
            >
              <Avatar
                :image="avatarSrc"
                shape="circle"
                size="small"
                class="h-7 w-7 overflow-hidden rounded-full"
              >
                <span class="text-xs font-semibold">{{ initials }}</span>
              </Avatar>
              <span class="hidden max-w-24 truncate sm:inline">{{ displayName }}</span>
              <ChevronDown class="size-4" />
            </button>
            <Transition name="fade">
              <div
                v-if="profileMenuOpen"
                data-testid="profile-dropdown"
                class="absolute right-0 mt-2 w-52 overflow-hidden rounded-xl border border-gray-200 bg-white p-1 shadow-lg dark:border-dark-border dark:bg-dark-card dark:shadow-none"
              >
                <button
                  type="button"
                  class="flex w-full cursor-pointer items-center gap-2 rounded-lg px-3 py-2 text-left text-sm text-gray-700 transition-colors hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/5 dark:hover:text-white"
                  @click="goToProfile"
                >
                  <User class="size-4" />
                  {{ t('user.profile.title') }}
                </button>
                <button
                  type="button"
                  class="flex w-full cursor-pointer items-center gap-2 rounded-lg px-3 py-2 text-left text-sm text-gray-700 transition-colors hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/5 dark:hover:text-white"
                  @click="goToSettings"
                >
                  <Settings class="size-4" />
                  {{ t('settings.title') }}
                </button>
                <button
                  type="button"
                  class="flex w-full cursor-pointer items-center gap-2 rounded-lg px-3 py-2 text-left text-sm text-gray-700 transition-colors hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/5 dark:hover:text-white"
                  @click="goToAccount"
                >
                  <IdCard class="size-4" />
                  {{ t('settings.sidebar.account') }}
                </button>
                <button
                  type="button"
                  :disabled="logoutLoading"
                  class="flex w-full cursor-pointer items-center gap-2 rounded-lg px-3 py-2 text-left text-sm text-red-600 transition-colors hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-60 dark:text-red-400 dark:hover:bg-red-500/10"
                  @click="logout"
                >
                  <LogOut class="size-4" />
                  {{ logoutLoading ? t('common.loading') : t('auth.logout.button') }}
                </button>
              </div>
            </Transition>
          </div>

          <button
            type="button"
            class="inline-flex h-10 w-10 cursor-pointer items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-700 transition-colors hover:bg-slate-100 dark:border-white/10 dark:bg-slate-900 dark:text-slate-200 dark:hover:bg-slate-800 md:hidden"
            :aria-label="mobileMenuOpen ? 'Close navigation' : 'Open navigation'"
            @click="mobileMenuOpen = !mobileMenuOpen"
          >
            <Menu v-if="!mobileMenuOpen" class="h-5 w-5" />
            <X v-else class="h-5 w-5" />
          </button>
        </div>
      </div>
    </div>

    <Transition name="fade">
      <div
        v-if="mobileMenuOpen"
        data-testid="navbar-mobile-menu"
        class="border-t border-slate-200 bg-white px-2 pb-3 pt-2 dark:border-white/10 dark:bg-slate-900 md:hidden"
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
          @click="handlePendingFeature"
        >
          {{ t('user.home.hero.uploadButton') }}
        </PrimeButton>
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
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity var(--cf-motion-fast) var(--cf-ease-standard),
    transform var(--cf-motion-fast) var(--cf-ease-standard);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
