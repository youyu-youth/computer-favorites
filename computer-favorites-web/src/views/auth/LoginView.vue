<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import PreviewSandbox from '@/components/auth/PreviewSandbox.vue'
import AuthCard from '@/components/auth/AuthCard.vue'
import AppFooter from '@/components/common/AppFooter.vue'

const appStore = useAppStore()
</script>

<template>
  <div class="min-h-screen w-full bg-white dark:bg-black">
    <div class="min-h-screen w-full lg:grid lg:grid-cols-[minmax(0,1fr)_480px] xl:grid-cols-[minmax(0,1fr)_520px]">
      <div class="hidden lg:block lg:h-full">
        <PreviewSandbox />
      </div>
      <div
        class="relative flex min-h-screen items-start justify-center border-l border-slate-200/60 bg-slate-50 px-4 py-10 lg:pt-[114px] dark:border-white/10 dark:bg-black sm:px-8"
      >
        <div class="pointer-events-none absolute inset-0 overflow-hidden">
          <div class="absolute -right-24 top-10 h-72 w-72 rounded-full bg-emerald-500/10 blur-3xl" />
          <div class="absolute -left-24 bottom-10 h-72 w-72 rounded-full bg-amber-700/10 blur-3xl" />
          <svg class="absolute inset-0 h-full w-full opacity-40 dark:opacity-25" aria-hidden="true">
            <defs>
              <pattern id="grid-24-right" width="24" height="24" patternUnits="userSpaceOnUse">
                <path d="M 24 0 L 0 0 0 24" fill="none" stroke="currentColor" stroke-width="1" />
              </pattern>
            </defs>
            <rect width="100%" height="100%" fill="url(#grid-24-right)" class="text-slate-200 dark:text-white/10" />
          </svg>
        </div>

        <div class="fixed right-4 top-4 z-30 sm:right-6 sm:top-6 lg:absolute lg:z-10">
          <button
            type="button"
            @click="appStore.toggleTheme"
            class="inline-flex h-10 w-10 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-600 transition-colors hover:bg-slate-50 dark:border-white/10 dark:bg-black dark:text-slate-300 dark:hover:bg-white/5"
            aria-label="切换主题"
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
              />
            </svg>
            <svg v-else class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
              />
            </svg>
          </button>
        </div>

        <div class="relative z-10 w-full max-w-md">
          <AuthCard />
          <div class="mt-8 overflow-hidden rounded-xl border border-slate-200/70 dark:border-white/10">
            <AppFooter />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
