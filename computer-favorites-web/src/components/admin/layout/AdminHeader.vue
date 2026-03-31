<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAdminAuthStore } from '@/stores/adminAuth'
import { storeToRefs } from 'pinia'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'

const appStore = useAppStore()
const adminAuthStore = useAdminAuthStore()
const router = useRouter()
const { isDark } = storeToRefs(appStore)

const mobileMenuOpen = ref(false)
const confirmVisible = ref(false)
const logoutLoading = ref(false)
const logoutError = ref('')

const toggleDarkMode = () => {
  appStore.toggleTheme()
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
    confirmVisible.value = false
    mobileMenuOpen.value = false
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
</script>

<template>
  <header class="sticky top-0 z-50 bg-white/80 dark:bg-dark-bg/80 backdrop-blur-md border-b border-gray-200 dark:border-dark-border">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo 与主导航 -->
        <div class="flex items-center gap-8">
          <div class="flex-shrink-0 flex items-center gap-2 cursor-pointer">
            <i class="fa-solid fa-ghost text-2xl text-gray-900 dark:text-white"></i>
            <span class="font-bold text-xl text-gray-900 dark:text-white tracking-tight">Glama Admin</span>
          </div>
          <nav class="hidden md:flex space-x-1">
            <a href="#" class="px-3 py-2 rounded-md text-sm font-medium text-gray-900 dark:text-white border-b-2 border-brand-orange">Websites</a>
            <a href="#" class="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white transition-colors">Users</a>
            <a href="#" class="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white transition-colors">Settings</a>
          </nav>
        </div>

        <!-- 右侧操作区 -->
        <div class="flex items-center gap-4">
          <button @click="toggleDarkMode" class="text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white w-8 h-8 rounded-full flex items-center justify-center bg-gray-100 dark:bg-dark-card transition-colors" title="切换明暗模式">
            <i class="fas" :class="isDark ? 'fa-sun' : 'fa-moon'"></i>
          </button>

          <Button
            type="button"
            @click="openLogoutConfirm"
            class="cursor-pointer bg-gray-900 text-white dark:bg-white dark:text-gray-900 px-4 py-2 rounded-md text-sm font-medium hover:bg-gray-800 dark:hover:bg-gray-100 transition-colors"
          >
            Sign Out
          </Button>

          <!-- 移动端菜单按钮 -->
          <button class="md:hidden text-gray-500 dark:text-gray-400 text-xl" @click="mobileMenuOpen = !mobileMenuOpen">
            <i class="fas fa-bars"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 移动端下拉菜单 -->
    <div v-show="mobileMenuOpen" class="md:hidden border-t border-gray-200 dark:border-dark-border bg-white dark:bg-dark-bg">
      <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3">
        <a href="#" class="block px-3 py-2 rounded-md text-base font-medium text-gray-900 dark:text-white bg-gray-100 dark:bg-dark-card">Websites</a>
        <a href="#" class="block px-3 py-2 rounded-md text-base font-medium text-gray-500 dark:text-gray-400">Users</a>
        <a href="#" class="block px-3 py-2 rounded-md text-base font-medium text-gray-500 dark:text-gray-400">Settings</a>
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
        header: { class: 'border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-5 py-4' },
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

