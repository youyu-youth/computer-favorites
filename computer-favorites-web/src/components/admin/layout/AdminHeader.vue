<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore, type ThemeMode } from '@/stores/app'
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useAdminNavStore } from '@/stores/adminNav'
import { storeToRefs } from 'pinia'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import Avatar from 'primevue/avatar'
import { useToast } from '@/composables/useToast'

const appStore = useAppStore()
const adminAuthStore = useAdminAuthStore()
const adminNavStore = useAdminNavStore()
const router = useRouter()
const route = useRoute()
const toast = useToast()
const { isDark, themeMode } = storeToRefs(appStore)
const { activeMenuLabel, mobileSidebarOpen } = storeToRefs(adminNavStore)

const confirmVisible = ref(false)
const logoutLoading = ref(false)
const logoutError = ref('')
const profileMenuOpen = ref(false)
const themeModeMenuOpen = ref(false)
const profileMenuRef = ref<HTMLElement | null>(null)

const displayName = computed(() => {
  const nickname = adminAuthStore.userSnapshot.nickname.trim()
  if (nickname) {
    return nickname
  }
  const username = adminAuthStore.userSnapshot.username.trim()
  if (username) {
    return username
  }
  return '管理员'
})

const avatarInitial = computed(() => {
  return displayName.value.slice(0, 1).toUpperCase()
})

const showSidebarToggle = computed(() => {
  return route.meta.hideAdminSidebar !== true
})

const toggleDarkMode = () => {
  appStore.toggleTheme()
}

const toggleSidebar = () => {
  adminNavStore.toggleMobileSidebar()
}

const closeProfileMenu = () => {
  profileMenuOpen.value = false
  themeModeMenuOpen.value = false
}

const toggleProfileMenu = () => {
  profileMenuOpen.value = !profileMenuOpen.value
  if (!profileMenuOpen.value) {
    themeModeMenuOpen.value = false
  }
}

const toggleThemeModeMenu = () => {
  themeModeMenuOpen.value = !themeModeMenuOpen.value
}

const selectThemeMode = (mode: ThemeMode) => {
  appStore.setThemeMode(mode)
  closeProfileMenu()
}

const openSettings = () => {
  closeProfileMenu()
  toast.add({
    title: '设置功能开发中',
    description: '配置页将于后续迭代开放',
    type: 'info',
  })
}

const goToProfile = async () => {
  closeProfileMenu()
  await router.push({ name: 'adminProfile' })
}

const closeProfileMenuByOutside = (event: MouseEvent) => {
  if (!profileMenuRef.value) {
    return
  }
  const target = event.target as Node | null
  if (target && !profileMenuRef.value.contains(target)) {
    closeProfileMenu()
  }
}

const closeProfileMenuByEsc = (event: KeyboardEvent) => {
  if (event.key === 'Escape') {
    closeProfileMenu()
  }
}

const openLogoutConfirm = () => {
  logoutError.value = ''
  confirmVisible.value = true
}

const closeLogoutConfirm = () => {
  if (logoutLoading.value) {
    return
  }
  confirmVisible.value = false
}

const onDialogVisibleChange = (visible: boolean) => {
  if (visible) {
    confirmVisible.value = true
    return
  }
  closeLogoutConfirm()
}

const confirmLogout = async () => {
  if (logoutLoading.value) {
    return
  }

  logoutLoading.value = true
  logoutError.value = ''

  try {
    await adminAuthStore.logout()
    closeProfileMenu()
    confirmVisible.value = false
    adminNavStore.setMobileSidebarOpen(false)
    await router.replace({
      name: 'adminLogin',
      query: {
        logoutReset: Date.now().toString(),
      },
    })
  } catch (error) {
    logoutError.value = error instanceof Error ? error.message : '退出失败，请稍后重试'
  } finally {
    logoutLoading.value = false
  }
}

watch(
  () => route.fullPath,
  () => {
    closeProfileMenu()
  }
)

onMounted(() => {
  window.addEventListener('click', closeProfileMenuByOutside)
  window.addEventListener('keydown', closeProfileMenuByEsc)
})

onBeforeUnmount(() => {
  window.removeEventListener('click', closeProfileMenuByOutside)
  window.removeEventListener('keydown', closeProfileMenuByEsc)
})
</script>

<template>
  <header class="sticky top-0 z-50 bg-white/80 dark:bg-dark-bg/80 backdrop-blur-md border-b border-gray-200 dark:border-dark-border">
    <div class="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <div class="flex items-center gap-3 min-w-0">
          <button
            v-if="showSidebarToggle"
            type="button"
            class="md:hidden text-gray-500 dark:text-gray-300 w-9 h-9 rounded-md hover:bg-gray-100 dark:hover:bg-dark-card transition-colors cursor-pointer"
            @click="toggleSidebar"
          >
            <i class="fas" :class="mobileSidebarOpen ? 'fa-xmark' : 'fa-bars'"></i>
          </button>

          <div class="flex-shrink-0 flex items-center gap-2 cursor-pointer">
            <i class="fa-solid fa-ghost text-2xl text-gray-900 dark:text-white"></i>
            <span class="font-bold text-xl text-gray-900 dark:text-white tracking-tight">Glama Admin</span>
          </div>

          <span class="hidden sm:inline-flex items-center rounded-full bg-orange-50 dark:bg-orange-900/10 text-brand-orange text-xs font-semibold px-2.5 py-1">
            {{ activeMenuLabel }}
          </span>
        </div>

        <div class="flex items-center gap-2 sm:gap-4">
          <button @click="toggleDarkMode" class="text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white w-8 h-8 rounded-full flex items-center justify-center bg-gray-100 dark:bg-dark-card transition-colors" title="切换明暗模式">
            <i class="fas" :class="isDark ? 'fa-sun' : 'fa-moon'"></i>
          </button>

          <div ref="profileMenuRef" class="relative">
            <button
              type="button"
              class="inline-flex h-9 cursor-pointer items-center gap-2 rounded-full border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card px-2.5 pr-3 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-dark-bg transition-colors"
              aria-label="管理员菜单"
              aria-haspopup="true"
              :aria-expanded="profileMenuOpen"
              @click.stop="toggleProfileMenu"
            >
              <Avatar
                :label="avatarInitial"
                shape="circle"
                class="!w-6 !h-6 !text-xs !bg-[#e95322] !text-white"
              />
              <span class="hidden lg:inline-block max-w-24 truncate">{{ displayName }}</span>
              <i class="fas fa-chevron-down text-[10px] transition-transform" :class="profileMenuOpen ? 'rotate-180' : ''" aria-hidden="true"></i>
            </button>

            <Transition name="fade-down">
              <div
                v-if="profileMenuOpen"
                class="absolute right-0 mt-2 w-56 rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-lg py-1 z-[70]"
                aria-label="管理员快捷菜单"
              >
                <button
                  type="button"
                  class="w-full cursor-pointer flex items-center justify-between px-3 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-dark-bg transition-colors"
                  @click="openSettings"
                >
                  <span class="flex items-center gap-2">
                    <i class="fas fa-gear text-[13px]" aria-hidden="true"></i>
                    设置
                  </span>
                  <span class="text-[10px] text-gray-400 dark:text-gray-500">开发中</span>
                </button>

                <button
                  type="button"
                  class="w-full cursor-pointer flex items-center gap-2 px-3 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-dark-bg transition-colors"
                  @click="goToProfile"
                >
                  <i class="fas fa-id-card text-[13px]" aria-hidden="true"></i>
                  个人主页
                </button>

                <div class="my-1 border-t border-gray-100 dark:border-dark-border/60"></div>

                <button
                  type="button"
                  class="w-full cursor-pointer flex items-center justify-between px-3 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-dark-bg transition-colors"
                  @click="toggleThemeModeMenu"
                >
                  <span class="flex items-center gap-2">
                    <i class="fas fa-palette text-[13px]" aria-hidden="true"></i>
                    系统配色
                  </span>
                  <i class="fas fa-chevron-right text-[10px] transition-transform" :class="themeModeMenuOpen ? 'rotate-90 text-[#e95322]' : 'text-gray-400'" aria-hidden="true"></i>
                </button>

                <div v-if="themeModeMenuOpen" class="px-2 pb-2 pt-1 space-y-1">
                  <button
                    type="button"
                    class="w-full cursor-pointer rounded-md px-3 py-1.5 text-left text-sm transition-colors"
                    :aria-pressed="themeMode === 'light'"
                    :class="themeMode === 'light'
                      ? 'bg-orange-50 dark:bg-[#3b1b10] text-[#e95322] font-medium'
                      : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-bg'"
                    @click="selectThemeMode('light')"
                  >
                    浅色
                  </button>
                  <button
                    type="button"
                    class="w-full cursor-pointer rounded-md px-3 py-1.5 text-left text-sm transition-colors"
                    :aria-pressed="themeMode === 'dark'"
                    :class="themeMode === 'dark'
                      ? 'bg-orange-50 dark:bg-[#3b1b10] text-[#e95322] font-medium'
                      : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-bg'"
                    @click="selectThemeMode('dark')"
                  >
                    深色
                  </button>
                  <button
                    type="button"
                    class="w-full cursor-pointer rounded-md px-3 py-1.5 text-left text-sm transition-colors"
                    :aria-pressed="themeMode === 'system'"
                    :class="themeMode === 'system'
                      ? 'bg-orange-50 dark:bg-[#3b1b10] text-[#e95322] font-medium'
                      : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-bg'"
                    @click="selectThemeMode('system')"
                  >
                    跟随系统
                  </button>
                </div>
              </div>
            </Transition>
          </div>

          <Button
            type="button"
            @click="openLogoutConfirm"
            class="cursor-pointer bg-gray-900 text-white dark:bg-white dark:text-gray-900 px-4 py-2 rounded-md text-sm font-medium hover:bg-gray-800 dark:hover:bg-gray-100 transition-colors"
          >
            Sign Out
          </Button>
        </div>
      </div>
    </div>

    <Dialog
      :visible="confirmVisible"
      modal
      :draggable="false"
      :closable="!logoutLoading"
      :dismissableMask="!logoutLoading"
      :pt="{
        mask: { class: 'bg-black/45 backdrop-blur-[1px] z-[120]' },
        root: {
          class:
            'w-[min(92vw,420px)] rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-[0_16px_36px_rgba(15,23,42,0.28)] dark:shadow-[0_22px_44px_rgba(2,6,23,0.62)] overflow-hidden'
        },
        header: { class: 'border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-5 py-4 flex items-center justify-between' },
        content: { class: 'px-5 py-4 text-sm text-gray-600 dark:text-gray-300' },
        footer: {
          class:
            'px-5 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg flex justify-end gap-3'
        }
      }"
      @update:visible="onDialogVisibleChange"
    >
      <template #header>
        <div class="flex items-center gap-2">
          <i class="fas fa-shield-halved text-brand-orange"></i>
          <span class="text-base font-semibold text-gray-900 dark:text-gray-100">确认退出登录</span>
        </div>
      </template>

      <p>退出后需要重新验证管理员账号与密码才能进入管理端，是否继续？</p>
      <p v-if="logoutError" class="mt-2 text-sm text-red-500 dark:text-red-400">{{ logoutError }}</p>

      <template #footer>
        <Button
          type="button"
          :disabled="logoutLoading"
          @click="closeLogoutConfirm"
          class="cursor-pointer border border-gray-300 dark:border-dark-border text-gray-700 dark:text-gray-200 px-4 py-2 rounded-md text-sm font-medium hover:bg-gray-100 dark:hover:bg-dark-card transition-colors"
        >
          取消
        </Button>
        <Button
          type="button"
          :loading="logoutLoading"
          :disabled="logoutLoading"
          @click="confirmLogout"
          class="cursor-pointer bg-brand-orange text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-[#cf4519] transition-colors"
        >
          确认退出
        </Button>
      </template>
    </Dialog>
  </header>
</template>

<style scoped>
.fade-down-enter-active,
.fade-down-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}

.fade-down-enter-from,
.fade-down-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>

