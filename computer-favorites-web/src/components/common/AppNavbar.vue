<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Avatar from 'primevue/avatar'
import InputText from 'primevue/inputtext'
import type { MenuItem } from 'primevue/menuitem'
import { logout as logoutApi } from '@/services/auth'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useToast } from '@/composables/useToast'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ChevronDown, Menu, X } from 'lucide-vue-next'

const { t } = useI18n()
const appStore = useAppStore()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const toast = useToast()

interface PopupMenuController {
  toggle: (event: Event) => void
  hide: () => void
}

const profileMenuRef = ref<PopupMenuController | null>(null)
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
  profileMenuRef.value?.hide()
  mobileMenuOpen.value = false
  router.push({ name: 'profile' })
}

const goToSettings = () => {
  profileMenuRef.value?.hide()
  mobileMenuOpen.value = false
  router.push({ name: 'settings' })
}

const goToAccount = () => {
  profileMenuRef.value?.hide()
  mobileMenuOpen.value = false
  router.push({ name: 'account' })
}

const toggleProfileMenu = (event: MouseEvent) => {
  profileMenuRef.value?.toggle(event)
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
  mobileMenuOpen.value = false
  profileMenuRef.value?.hide()
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

const profileMenuItems = computed<MenuItem[]>(() => [
  {
    label: t('user.profile.title'),
    icon: 'pi pi-user',
    command: () => {
      goToProfile()
    },
  },
  {
    label: t('settings.title'),
    icon: 'pi pi-cog',
    command: () => {
      goToSettings()
    },
  },
  {
    label: t('settings.sidebar.account'),
    icon: 'pi pi-id-card',
    command: () => {
      goToAccount()
    },
  },
  {
    separator: true,
  },
  {
    label: logoutLoading.value ? t('common.loading') : t('auth.logout.button'),
    icon: 'pi pi-sign-out',
    command: () => {
      void logout()
    },
    disabled: logoutLoading.value,
  },
])

watch(
  () => route.fullPath,
  () => {
    profileMenuRef.value?.hide()
    mobileMenuOpen.value = false
  },
)

watch(
  () => authStore.isAuthed,
  (isAuthed) => {
    if (!isAuthed) {
      profileMenuRef.value?.hide()
      return
    }
    void authStore.loadCurrentUser()
  },
  { immediate: true },
)
</script>

<template>
  <header class="fixed inset-x-0 top-0 z-50 border-b border-border-default bg-surface-page/95 backdrop-blur-md">
    <nav class="mx-auto flex h-16 w-full max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
      <div class="flex items-center gap-6">
        <RouterLink :to="{ name: 'home' }" class="text-xl font-black tracking-tighter text-text-primary sm:text-2xl">
          Monolith Pro
        </RouterLink>

        <div class="hidden items-center gap-5 md:flex">
          <RouterLink
            :to="{ name: 'home' }"
            :class="[
              'cursor-pointer border-b-2 pb-1 text-sm font-headline font-bold tracking-tight transition-colors',
              isHomeRoute
                ? 'border-primary-500 text-text-primary'
                : 'border-transparent text-text-secondary hover:text-text-primary',
            ]"
          >
            {{ t('user.home.hero.browseButton') }}
          </RouterLink>
          <button
            type="button"
            class="cursor-pointer text-sm font-headline font-bold tracking-tight text-text-secondary transition-colors hover:text-text-primary"
            @click="handlePendingFeature"
          >
            {{ t('user.home.filter.categories') }}
          </button>
          <button
            type="button"
            class="cursor-pointer text-sm font-headline font-bold tracking-tight text-text-secondary transition-colors hover:text-text-primary"
            @click="handlePendingFeature"
          >
            {{ t('user.home.hero.uploadButton') }}
          </button>
        </div>
      </div>

      <div class="flex items-center gap-2 sm:gap-3">
        <div class="relative hidden lg:block">
          <span class="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-sm text-text-secondary">search</span>
          <InputText
            class="w-64 rounded-full border border-border-default bg-surface-card py-1.5 pl-10 pr-4 text-xs text-text-primary placeholder:text-text-secondary focus:border-primary-500 focus:ring-1 focus:ring-primary-500"
            placeholder="Search treasure..."
          />
        </div>

        <button
          type="button"
          @click="appStore.toggleTheme"
          class="inline-flex h-10 w-10 cursor-pointer items-center justify-center rounded-full text-text-secondary transition-colors hover:bg-surface-card hover:text-text-primary"
          :aria-label="t('common.theme.toggle')"
        >
          <svg
            v-if="!appStore.isDark"
            class="h-5 w-5"
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
          <svg v-else class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
            ></path>
          </svg>
        </button>

        <UButton
          v-if="!authStore.isSessionValid"
          color="primary"
          class="hidden !rounded-full !px-4 !py-1.5 sm:inline-flex"
          @click="goToLogin"
        >
          {{ t('auth.login.loginButton') }} / {{ t('auth.login.registerButton') }}
        </UButton>

        <div v-else class="relative hidden sm:block">
          <button
            type="button"
            class="inline-flex h-10 cursor-pointer items-center gap-2 rounded-full border border-border-default bg-surface-card px-2.5 pr-3 text-sm text-text-primary transition-colors hover:border-primary-500/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500/30"
            @click.stop="toggleProfileMenu"
          >
            <Avatar :image="avatarSrc" shape="circle" size="small" class="h-7 w-7 overflow-hidden rounded-full">
              <span class="text-xs font-semibold">{{ initials }}</span>
            </Avatar>
            <span class="hidden max-w-24 truncate md:inline">{{ displayName }}</span>
            <ChevronDown class="size-4" />
          </button>
          <UMenu ref="profileMenuRef" :model="profileMenuItems" />
        </div>

        <button
          type="button"
          class="inline-flex h-10 w-10 cursor-pointer items-center justify-center rounded-full border border-border-default bg-surface-card text-text-primary transition-colors hover:border-primary-500/40 hover:text-primary-500 md:hidden"
          :aria-label="mobileMenuOpen ? 'Close navigation' : 'Open navigation'"
          @click="mobileMenuOpen = !mobileMenuOpen"
        >
          <Menu v-if="!mobileMenuOpen" class="h-5 w-5" />
          <X v-else class="h-5 w-5" />
        </button>
      </div>
    </nav>

    <Transition name="fade">
      <div
        v-if="mobileMenuOpen"
        class="border-t border-border-default bg-surface-page px-4 pb-4 pt-3 md:hidden"
      >
        <button
          type="button"
          :class="[
            'w-full cursor-pointer rounded-md border px-3 py-2 text-left text-sm font-headline font-bold tracking-tight transition-colors',
            isHomeRoute
              ? 'border-primary-500 bg-primary-500/5 text-primary-500'
              : 'border-transparent text-text-secondary hover:text-text-primary',
          ]"
          @click="goToHome"
        >
          {{ t('user.home.hero.browseButton') }}
        </button>
        <button
          type="button"
          class="mt-1 w-full cursor-pointer rounded-md border border-transparent px-3 py-2 text-left text-sm font-headline font-bold tracking-tight text-text-secondary transition-colors hover:text-text-primary"
          @click="handlePendingFeature"
        >
          {{ t('user.home.filter.categories') }}
        </button>
        <button
          type="button"
          class="mt-1 w-full cursor-pointer rounded-md border border-transparent px-3 py-2 text-left text-sm font-headline font-bold tracking-tight text-text-secondary transition-colors hover:text-text-primary"
          @click="handlePendingFeature"
        >
          {{ t('user.home.hero.uploadButton') }}
        </button>
        <button
          v-if="authStore.isSessionValid"
          type="button"
          class="mt-1 w-full cursor-pointer rounded-md border border-transparent px-3 py-2 text-left text-sm font-headline font-bold tracking-tight text-text-secondary transition-colors hover:text-text-primary"
          @click="goToProfile"
        >
          {{ t('user.profile.title') }}
        </button>
        <button
          v-if="authStore.isSessionValid"
          type="button"
          class="mt-1 w-full cursor-pointer rounded-md border border-transparent px-3 py-2 text-left text-sm font-headline font-bold tracking-tight text-text-secondary transition-colors hover:text-text-primary"
          @click="goToSettings"
        >
          {{ t('settings.title') }}
        </button>
        <button
          v-if="authStore.isSessionValid"
          type="button"
          class="mt-1 w-full cursor-pointer rounded-md border border-transparent px-3 py-2 text-left text-sm font-headline font-bold tracking-tight text-text-secondary transition-colors hover:text-text-primary"
          @click="goToAccount"
        >
          {{ t('settings.sidebar.account') }}
        </button>
        <UButton
          v-if="!authStore.isSessionValid"
          color="primary"
          class="!mt-2 !w-full !rounded-md"
          @click="goToLogin"
        >
          {{ t('auth.login.loginButton') }} / {{ t('auth.login.registerButton') }}
        </UButton>
        <UButton
          v-else
          color="neutral"
          variant="soft"
          class="!mt-2 !w-full !rounded-md"
          @click="void logout()"
        >
          {{ logoutLoading ? t('common.loading') : t('auth.logout.button') }}
        </UButton>
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
