<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { logout as logoutApi } from '@/services/auth'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useToast } from '@/composables/useToast'
import { RouterLink, useRoute, useRouter } from 'vue-router'

const appStore = useAppStore()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const toast = useToast()
const profileMenuRef = ref<HTMLElement | null>(null)
const profileMenuOpen = ref(false)
const logoutLoading = ref(false)

const displayName = computed(() => authStore.userSnapshot.nickname || '我的账号')
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
  router.push({
    name: 'login',
    query: { redirect: route.fullPath },
  })
}

const goToProfile = () => {
  profileMenuOpen.value = false
  router.push({ name: 'profile' })
}

const goToSettings = () => {
  profileMenuOpen.value = false
  router.push({ name: 'settings' })
}

const goToAccount = () => {
  profileMenuOpen.value = false
  router.push({ name: 'account' })
}

const toggleProfileMenu = () => {
  profileMenuOpen.value = !profileMenuOpen.value
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
    title: '退出成功',
    description: requestFailed ? '本地登录状态已清理' : '已安全退出当前账号',
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
  <nav
    class="fixed inset-x-0 top-0 z-50 glass-nav border-b border-gray-200 bg-white/70 dark:border-dark-border dark:bg-dark-bg/80"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Left: Logo -->
        <RouterLink
          :to="{ name: 'home' }"
          class="flex items-center gap-3"
        >
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
          <span class="font-bold text-xl tracking-tight text-gray-900 dark:text-white"
            >Computer Favorites</span
          >
        </RouterLink>

        <!-- Center: Navigation Links (Desktop) -->
        <div class="hidden md:flex items-center space-x-6">
          <RouterLink
            :to="{ name: 'home' }"
            :class="[
              'text-sm font-medium transition-colors',
              isHomeRoute
                ? 'text-primary-500 dark:text-primary-400'
                : 'text-gray-600 hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400',
            ]"
            >热门网站</RouterLink
          >
          <a
            href="#"
            class="text-sm font-medium text-gray-600 hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400 transition-colors"
            >网站分类</a
          >
          <RouterLink
            :to="{ name: 'profile' }"
            class="text-sm font-medium text-gray-600 hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400 transition-colors"
            >个人主页</RouterLink
          >
          <a
            href="#"
            class="text-sm font-medium text-gray-600 hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400 transition-colors"
            >上传网站</a
          >
        </div>

        <!-- Right: Actions -->
        <div class="flex items-center gap-4">
          <!-- Theme Toggle -->
          <button
            @click="appStore.toggleTheme"
            class="p-2 rounded-full hover:bg-gray-200 dark:hover:bg-gray-800 transition-colors text-gray-500 dark:text-gray-400"
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
            class="hidden sm:flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-primary-500 hover:bg-primary-600 rounded-md transition-colors shadow-sm"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"
              ></path>
            </svg>
            登录 / 注册
          </button>

          <div
            v-else
            ref="profileMenuRef"
            class="relative"
          >
            <button
              type="button"
              class="inline-flex h-10 items-center gap-2 rounded-full border border-gray-200 bg-white px-2.5 pr-3 text-sm text-gray-700 transition-colors hover:bg-gray-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-400 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-200 dark:hover:bg-gray-800"
              @click.stop="toggleProfileMenu"
            >
              <UAvatar :src="avatarSrc" :alt="`用户-${displayName}`" size="sm">
                <span class="text-xs font-semibold">{{ initials }}</span>
              </UAvatar>
              <span class="hidden sm:inline max-w-24 truncate">{{ displayName }}</span>
              <UIcon name="i-lucide-chevron-down" class="size-4" />
            </button>

            <Transition name="fade">
              <div
                v-if="profileMenuOpen"
                class="absolute right-0 mt-2 w-52 overflow-hidden rounded-xl border border-gray-200 bg-white p-1 shadow-lg dark:border-gray-700 dark:bg-gray-900"
              >
                <button
                  type="button"
                  class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-sm text-gray-700 transition-colors hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-800"
                  @click="goToProfile"
                >
                  <UIcon name="i-lucide-user" class="size-4" />
                  个人主页
                </button>
                <button
                  type="button"
                  class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-sm text-gray-700 transition-colors hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-800"
                  @click="goToSettings"
                >
                  <UIcon name="i-lucide-settings" class="size-4" />
                  设置
                </button>
                <button
                  type="button"
                  class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-sm text-gray-700 transition-colors hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-800"
                  @click="goToAccount"
                >
                  <UIcon name="i-lucide-id-card" class="size-4" />
                  账户信息
                </button>
                <button
                  type="button"
                  :disabled="logoutLoading"
                  class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-sm text-red-600 transition-colors hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-60 dark:text-red-400 dark:hover:bg-red-500/10"
                  @click="logout"
                >
                  <UIcon name="i-lucide-log-out" class="size-4" />
                  {{ logoutLoading ? '退出中...' : '退出登录' }}
                </button>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
