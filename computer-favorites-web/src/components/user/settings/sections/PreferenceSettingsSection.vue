<script setup lang="ts">
import { inject } from 'vue'
import { settingsStateKey } from '@/components/user/settings/context'

const settingsState = inject(settingsStateKey)
if (!settingsState) {
  throw new Error('Settings state is not provided')
}

const setting = settingsState.setting

const updateTheme = (val: 'light' | 'dark' | 'system') => {
  setting.theme = val
}

defineOptions({
  name: 'PreferenceSettingsSection'
})
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-1">
      <h3 class="text-xl font-medium text-slate-900 dark:text-white">偏好设置</h3>
      <p class="text-sm text-slate-500 dark:text-slate-400">调整系统语言、主题模式和首页默认样式。</p>
    </div>

    <div class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-0 dark:bg-black dark:shadow-none">
      <div class="space-y-8">

        <!-- 外观设置 -->
        <div>
          <h4 class="mb-4 text-base font-medium text-slate-900 dark:text-white">系统主题</h4>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div
              @click="updateTheme('light')"
              class="relative cursor-pointer rounded-xl border-2 p-4 text-center transition-all"
              :class="setting.theme === 'light' ? 'border-primary-600 bg-primary-50 dark:bg-primary-500/20 dark:border-white' : 'border-slate-200 hover:border-slate-300 dark:border-white/10 dark:bg-white/5 dark:hover:border-white/20'"
            >
              <div class="mx-auto mb-2 flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 text-primary-600 dark:bg-primary-500/20 dark:text-primary-300">
                <UIcon name="i-lucide-sun" class="h-5 w-5" />
              </div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">浅色模式</p>
            </div>

            <div
              @click="updateTheme('dark')"
              class="relative cursor-pointer rounded-xl border-2 p-4 text-center transition-all"
              :class="setting.theme === 'dark' ? 'border-primary-600 bg-primary-50 dark:bg-white/10 dark:border-white' : 'border-slate-200 hover:border-slate-300 dark:border-white/10 dark:bg-white/5 dark:hover:border-white/20'"
            >
              <div class="mx-auto mb-2 flex h-8 w-8 items-center justify-center rounded-full bg-slate-800 text-white dark:bg-black dark:text-white dark:ring-1 dark:ring-white/20">
                <UIcon name="i-lucide-moon" class="h-5 w-5" />
              </div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">深色模式</p>
            </div>

            <div
              @click="updateTheme('system')"
              class="relative cursor-pointer rounded-xl border-2 p-4 text-center transition-all"
              :class="setting.theme === 'system' ? 'border-primary-600 bg-primary-50 dark:bg-primary-500/20 dark:border-white' : 'border-slate-200 hover:border-slate-300 dark:border-white/10 dark:bg-white/5 dark:hover:border-white/20'"
            >
              <div class="mx-auto mb-2 flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 text-primary-600 dark:bg-primary-500/20 dark:text-primary-300">
                <UIcon name="i-lucide-monitor" class="h-5 w-5" />
              </div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">跟随系统</p>
            </div>
          </div>
        </div>

        <UDivider class="dark:border-white/10" />

        <!-- 其他偏好 -->
        <div class="space-y-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">系统语言</p>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">界面默认显示的语言</p>
            </div>
            <USelect
              v-model="setting.language"
              :options="[{ label: '简体中文', value: 'zh-CN' }, { label: 'English', value: 'en-US' }]"
              class="w-32 dark:ring-0 dark:bg-white/5"
            />
          </div>

          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">默认主页视图</p>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">资源广场默认展示的形式</p>
            </div>
            <USelect
              v-model="setting.homepageStyle"
              :options="[{ label: '网格视图', value: 'card' }, { label: '列表视图', value: 'list' }]"
              class="w-32 dark:ring-0 dark:bg-white/5"
            />
          </div>

          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">默认分页大小</p>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">列表每页显示的数据条数</p>
            </div>
            <USelect
              v-model="setting.pageSize"
              :options="[{ label: '10 条', value: 10 }, { label: '20 条', value: 20 }, { label: '50 条', value: 50 }]"
              class="w-32 dark:ring-0 dark:bg-white/5"
            />
          </div>
        </div>

      </div>
    </div>
  </div>
</template>
