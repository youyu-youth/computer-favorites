<script setup lang="ts">
import { computed, inject } from 'vue'
import { settingsStateKey } from '@/components/user/settings/context'

const settingsState = inject(settingsStateKey)
if (!settingsState) {
  throw new Error('Settings state is not provided')
}

const setting = settingsState.setting
const emailNoticeEnabled = computed({
  get: () => setting.emailNotice === 1,
  set: (value: boolean) => {
    setting.emailNotice = value ? 1 : 0
  },
})
const collectNoticeEnabled = computed({
  get: () => setting.collectNotice === 1,
  set: (value: boolean) => {
    setting.collectNotice = value ? 1 : 0
  },
})
const commentNoticeEnabled = computed({
  get: () => setting.commentNotice === 1,
  set: (value: boolean) => {
    setting.commentNotice = value ? 1 : 0
  },
})

defineOptions({
  name: 'MessageSettingsSection',
})
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-1">
      <h3 class="text-xl font-medium text-slate-900 dark:text-white">消息设置</h3>
      <p class="text-sm text-slate-500 dark:text-slate-400">控制您接收各类通知的频率与方式。</p>
    </div>

    <!-- 通知管理 -->
    <div
      class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-0 dark:bg-black dark:shadow-none"
    >
      <h4 class="mb-4 text-base font-medium text-slate-900 dark:text-white">提醒配置</h4>

      <div class="space-y-6">
        <div class="flex items-center justify-between">
          <div class="space-y-1">
            <p class="text-sm font-medium text-slate-900 dark:text-white">邮件通知</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">接收系统重要更新与安全提醒</p>
          </div>
          <USwitch v-model="emailNoticeEnabled" color="primary" class="dark:bg-white/10" />
        </div>

        <USeparator class="dark:border-white/10" />

        <div class="flex items-center justify-between">
          <div class="space-y-1">
            <p class="text-sm font-medium text-slate-900 dark:text-white">收藏提醒</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">当您的网站被他人收藏时通知我</p>
          </div>
          <USwitch v-model="collectNoticeEnabled" color="primary" class="dark:bg-white/10" />
        </div>

        <USeparator class="dark:border-white/10" />

        <div class="flex items-center justify-between">
          <div class="space-y-1">
            <p class="text-sm font-medium text-slate-900 dark:text-white">评论与互动</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">当有新评论或新回复时通知我</p>
          </div>
          <USwitch v-model="commentNoticeEnabled" color="primary" class="dark:bg-white/10" />
        </div>
      </div>
    </div>
  </div>
</template>
