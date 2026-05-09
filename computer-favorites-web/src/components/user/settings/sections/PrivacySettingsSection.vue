<script setup lang="ts">
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()
const privacy = settingsStore.privacy

const visibilityOptions = [
  { label: '公开', value: 'public' },
  { label: '仅登录用户', value: 'logged' },
  { label: '私密', value: 'private' },
]

const showContributionBool = computed<boolean>({
  get: () => privacy.showContribution === 1,
  set: (value) => {
    privacy.showContribution = value ? 1 : 0
  },
})

const showCollectionsBool = computed<boolean>({
  get: () => privacy.showCollections === 1,
  set: (value) => {
    privacy.showCollections = value ? 1 : 0
  },
})

defineOptions({
  name: 'PrivacySettingsSection',
})
</script>

<template>
  <div class="space-y-8">
    <header class="space-y-2">
      <div class="flex items-center gap-3">
        <span class="block h-6 w-1 rounded-full bg-amber-500"></span>
        <h3 class="text-xl font-semibold tracking-tight text-slate-900 dark:text-white md:text-2xl">隐私设置</h3>
      </div>
      <p class="text-sm text-slate-500 dark:text-slate-400">控制个人主页对外可见范围，以及他人访问时可看到的数据模块。</p>
    </header>

    <section class="space-y-3">
      <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
        Profile Privacy · 主页公开范围
      </div>

      <div class="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white/60 p-4 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25] sm:flex-row sm:items-center sm:justify-between">
        <div class="flex items-center gap-3">
          <span class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-slate-50 text-slate-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-400">
            <UIcon name="i-lucide-eye" class="h-4 w-4" />
          </span>
          <div>
            <p class="text-sm font-medium text-slate-900 dark:text-white">主页可见性</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">公开主页 `/computer/profile/{username}` 的访问范围</p>
          </div>
        </div>
        <USelect v-model="privacy.profileVisibility" :options="visibilityOptions" class="w-full sm:w-48" />
      </div>

      <div class="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white/60 p-4 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25] sm:flex-row sm:items-center sm:justify-between">
        <div class="flex items-center gap-3">
          <span class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-slate-50 text-slate-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-400">
            <UIcon name="i-lucide-activity" class="h-4 w-4" />
          </span>
          <div>
            <p class="text-sm font-medium text-slate-900 dark:text-white">展示贡献数据</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">关闭后他人不可见贡献看板、热力图与贡献概览</p>
          </div>
        </div>
        <USwitch v-model="showContributionBool" />
      </div>

      <div class="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white/60 p-4 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25] sm:flex-row sm:items-center sm:justify-between">
        <div class="flex items-center gap-3">
          <span class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-slate-50 text-slate-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-400">
            <UIcon name="i-lucide-star" class="h-4 w-4" />
          </span>
          <div>
            <p class="text-sm font-medium text-slate-900 dark:text-white">展示收藏列表</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">关闭后他人访问主页时隐藏侧边栏收藏网站列表</p>
          </div>
        </div>
        <USwitch v-model="showCollectionsBool" />
      </div>
    </section>
  </div>
</template>
