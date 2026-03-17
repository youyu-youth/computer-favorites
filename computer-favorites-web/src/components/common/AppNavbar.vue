<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import { useRouter } from 'vue-router'

const appStore = useAppStore()
const router = useRouter()

const goToLogin = () => {
  router.push({ name: 'login' })
}
</script>

<template>
  <nav
    class="sticky top-0 z-50 glass-nav bg-white/70 dark:bg-dark-bg/80 border-b border-gray-200 dark:border-dark-border"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Left: Logo -->
        <div class="flex items-center gap-3 cursor-pointer">
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
        </div>

        <!-- Center: Navigation Links (Desktop) -->
        <div class="hidden md:flex items-center space-x-6">
          <a
            href="#"
            class="text-sm font-medium text-gray-600 hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400 transition-colors"
            >热门网站</a
          >
          <a
            href="#"
            class="text-sm font-medium text-gray-600 hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400 transition-colors"
            >网站分类</a
          >
          <a
            href="#"
            class="text-sm font-medium text-gray-600 hover:text-primary-500 dark:text-gray-300 dark:hover:text-primary-400 transition-colors"
            >我的收藏</a
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
          <!-- Login / Register -->
          <button
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
        </div>
      </div>
    </div>
  </nav>
</template>
