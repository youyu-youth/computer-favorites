<script setup lang="ts">
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()
const setting = settingsStore.setting

const themeOptions = [
  { value: 'light', label: '浅色模式', icon: 'i-lucide-sun', desc: '明亮通透' },
  { value: 'dark', label: '深色模式', icon: 'i-lucide-moon', desc: '低光护眼' },
  { value: 'system', label: '跟随系统', icon: 'i-lucide-monitor-smartphone', desc: '自动切换' },
] as const

const updateTheme = (val: string) => {
  setting.theme = val
}

const languageOptions = [
  { label: '简体中文', value: 'zh-CN' },
  { label: 'English', value: 'en-US' },
]

const homepageOptions = [
  { label: '网格视图', value: 'card' },
  { label: '列表视图', value: 'list' },
]

const pageSizeOptions = [
  { label: '10 条 / 页', value: 10 },
  { label: '20 条 / 页', value: 20 },
  { label: '50 条 / 页', value: 50 },
]

defineOptions({
  name: 'PreferenceSettingsSection',
})
</script>

<template>
  <div class="space-y-8">
    <!-- Section Header -->
    <header class="space-y-2">
      <div class="flex items-center gap-3">
        <span class="block h-6 w-1 rounded-full bg-amber-500"></span>
        <h3 class="text-xl font-semibold tracking-tight text-slate-900 dark:text-white md:text-2xl">偏好设置</h3>
      </div>
      <p class="text-sm text-slate-500 dark:text-slate-400">调整界面主题、语言、默认视图和分页粒度。</p>
    </header>

    <!-- 系统主题 -->
    <section class="space-y-4">
      <div class="flex items-center justify-between">
        <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
          Appearance · 系统主题
        </div>
        <span class="text-[11px] text-slate-400 dark:text-slate-500">点击卡片预览即可切换</span>
      </div>

      <div class="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <button
          v-for="opt in themeOptions"
          :key="opt.value"
          type="button"
          class="cf-theme-card group relative flex cursor-pointer flex-col gap-3 rounded-xl border-2 p-4 text-left transition-all duration-200"
          :class="setting.theme === opt.value
            ? 'border-amber-400 bg-amber-50/70 shadow-[0_0_0_4px_rgb(245_158_11/0.08)] dark:border-amber-400/60 dark:bg-amber-500/10'
            : 'border-slate-200 bg-white/60 hover:border-amber-200 hover:bg-amber-50/30 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-amber-400/30 dark:hover:bg-amber-500/[0.06]'"
          @click="updateTheme(opt.value)"
        >
          <!-- 预览迷你 UI -->
          <div
            class="relative h-16 overflow-hidden rounded-lg ring-1"
            :class="opt.value === 'light'
              ? 'bg-slate-50 ring-slate-200'
              : opt.value === 'dark'
                ? 'bg-[#0e0e10] ring-white/10'
                : 'bg-gradient-to-r from-slate-50 to-[#0e0e10] ring-slate-200 dark:ring-white/10'"
          >
            <span
              class="absolute left-3 top-3 inline-block h-1.5 w-10 rounded-full"
              :class="opt.value === 'dark' ? 'bg-amber-400' : 'bg-amber-500'"
            ></span>
            <span
              class="absolute left-3 top-6 inline-block h-1 w-16 rounded-full"
              :class="opt.value === 'light' ? 'bg-slate-300' : 'bg-white/30'"
            ></span>
            <span
              class="absolute left-3 top-9 inline-block h-1 w-12 rounded-full"
              :class="opt.value === 'light' ? 'bg-slate-200' : 'bg-white/15'"
            ></span>
            <span
              class="absolute right-3 top-3 inline-block h-3 w-3 rounded-full ring-2"
              :class="opt.value === 'light' ? 'bg-amber-400 ring-white' : 'bg-amber-400 ring-[#0e0e10]'"
            ></span>
          </div>

          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span
                class="flex h-7 w-7 items-center justify-center rounded-lg"
                :class="setting.theme === opt.value
                  ? 'bg-amber-500 text-white'
                  : 'bg-slate-100 text-slate-500 dark:bg-[#1c1c25] dark:text-slate-400'"
              >
                <UIcon :name="opt.icon" class="h-3.5 w-3.5" />
              </span>
              <div>
                <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ opt.label }}</p>
                <p class="text-[11px] text-slate-500 dark:text-slate-400">{{ opt.desc }}</p>
              </div>
            </div>
            <UIcon
              v-if="setting.theme === opt.value"
              name="i-lucide-check"
              class="h-4 w-4 text-amber-500"
            />
          </div>
        </button>
      </div>
    </section>

    <div class="cf-hairline" aria-hidden="true"></div>

    <!-- 其他偏好 -->
    <section class="space-y-4">
      <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
        Defaults · 默认行为
      </div>

      <div class="space-y-3">
        <div class="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white/60 p-4 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25] sm:flex-row sm:items-center sm:justify-between">
          <div class="flex items-center gap-3">
            <span class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-slate-50 text-slate-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-400">
              <UIcon name="i-lucide-languages" class="h-4 w-4" />
            </span>
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">系统语言</p>
              <p class="text-xs text-slate-500 dark:text-slate-400">界面默认显示的语言</p>
            </div>
          </div>
          <USelect v-model="setting.language" :options="languageOptions" class="w-full sm:w-44" />
        </div>

        <div class="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white/60 p-4 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25] sm:flex-row sm:items-center sm:justify-between">
          <div class="flex items-center gap-3">
            <span class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-slate-50 text-slate-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-400">
              <UIcon name="i-lucide-layout-dashboard" class="h-4 w-4" />
            </span>
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">默认主页视图</p>
              <p class="text-xs text-slate-500 dark:text-slate-400">资源广场首屏的展示形态</p>
            </div>
          </div>
          <USelect v-model="setting.homepageStyle" :options="homepageOptions" class="w-full sm:w-44" />
        </div>

        <div class="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white/60 p-4 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25] sm:flex-row sm:items-center sm:justify-between">
          <div class="flex items-center gap-3">
            <span class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-slate-50 text-slate-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-400">
              <UIcon name="i-lucide-list" class="h-4 w-4" />
            </span>
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">默认分页大小</p>
              <p class="text-xs text-slate-500 dark:text-slate-400">列表每页显示的数据条数</p>
            </div>
          </div>
          <USelect v-model="setting.pageSize" :options="pageSizeOptions" class="w-full sm:w-44" />
        </div>

        <div class="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white/60 p-4 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25] sm:flex-row sm:items-center sm:justify-between">
          <div class="flex items-center gap-3">
            <span class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-slate-50 text-slate-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-400">
              <UIcon name="i-lucide-lock-keyhole" class="h-4 w-4" />
            </span>
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">收藏夹访问密码</p>
              <p class="text-xs text-slate-500 dark:text-slate-400">空值表示不启用收藏夹访问密码</p>
            </div>
          </div>
          <UInput
            v-model="setting.favoritesHidePassword"
            type="password"
            maxlength="64"
            autocomplete="new-password"
            placeholder="未设置"
            class="w-full sm:w-56"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.cf-hairline {
  height: 1px;
  background-image: linear-gradient(
    90deg,
    transparent 0%,
    rgb(15 23 42 / 0.08) 20%,
    rgb(245 158 11 / 0.25) 50%,
    rgb(15 23 42 / 0.08) 80%,
    transparent 100%
  );
}

:where(html.dark) .cf-hairline {
  background-image: linear-gradient(
    90deg,
    transparent 0%,
    rgb(255 255 255 / 0.06) 20%,
    rgb(245 158 11 / 0.35) 50%,
    rgb(255 255 255 / 0.06) 80%,
    transparent 100%
  );
}
</style>
