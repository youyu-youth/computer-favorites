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
const mobileMenuOpen = ref(false)

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
    title: '功能建设中',
    description: '该功能即将上线，敬请期待',
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
  <UHeader
    v-model:open="mobileMenuOpen"
    class="fixed inset-x-0 top-0 z-50 glass-nav border-b border-gray-200 bg-white/70 backdrop-blur-md dark:border-dark-border dark:bg-dark-bg/80"
    :ui="{
      container: 'max-w-7xl mx-auto h-16 px-4 sm:px-6 lg:px-8',
    }"
  >
    <template #left>
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
        <span class="hidden text-xl font-bold tracking-tight text-gray-900 dark:text-white sm:inline">Computer Favorites</span>
      </RouterLink>
    </template>

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
        热门网站
      </RouterLink>
      <button
        type="button"
        class="text-sm font-medium text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400"
        @click="handlePendingFeature"
      >
        网站分类
      </button>
      <RouterLink
        :to="{ name: 'profile' }"
        class="text-sm font-medium text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400"
      >
        个人主页
      </RouterLink>
      <button
        type="button"
        class="text-sm font-medium text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400"
        @click="handlePendingFeature"
      >
        上传网站
      </button>
    </div>

    <template #right>
      <div class="flex items-center gap-3 sm:gap-4">
        <button
          @click="appStore.toggleTheme"
          class="rounded-full p-2 text-gray-500 transition-colors hover:bg-gray-200 dark:text-gray-400 dark:hover:bg-gray-800"
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
          class="hidden items-center gap-2 rounded-md bg-primary-500 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-primary-600 sm:flex"
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

        <div v-else ref="profileMenuRef" class="relative">
          <button
            type="button"
            class="inline-flex h-10 items-center gap-2 rounded-full border border-gray-200 bg-white px-2.5 pr-3 text-sm text-gray-700 transition-colors hover:bg-gray-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-400 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-200 dark:hover:bg-gray-800"
            @click.stop="toggleProfileMenu"
          >
            <UAvatar :src="avatarSrc" :alt="`用户-${displayName}`" size="sm">
              <span class="text-xs font-semibold">{{ initials }}</span>
            </UAvatar>
            <span class="hidden max-w-24 truncate sm:inline">{{ displayName }}</span>
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
    </template>

    <template #body>
      <div class="space-y-1 px-2 pb-3">
        <UButton
          color="neutral"
          variant="ghost"
          class="w-full justify-start"
          :class="isHomeRoute ? 'text-primary-500 dark:text-primary-400' : ''"
          @click="goToHome"
        >
          热门网站
        </UButton>
        <UButton color="neutral" variant="ghost" class="w-full justify-start" @click="handlePendingFeature">
          网站分类
        </UButton>
        <UButton color="neutral" variant="ghost" class="w-full justify-start" @click="goToProfile">
          个人主页
        </UButton>
        <UButton color="neutral" variant="ghost" class="w-full justify-start" @click="handlePendingFeature">
          上传网站
        </UButton>
        <UButton
          v-if="!authStore.isSessionValid"
          color="primary"
          variant="solid"
          class="mt-2 w-full justify-center"
          @click="goToLogin"
        >
          登录 / 注册
        </UButton>
      </div>
    </template>
  </UHeader>
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
