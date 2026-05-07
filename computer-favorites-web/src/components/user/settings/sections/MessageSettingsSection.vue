<script setup lang="ts">
import { computed, inject } from 'vue'
import { settingsStateKey } from '../context'

const settingsState = inject(settingsStateKey)
if (!settingsState) {
  throw new Error('Settings state is not provided')
}

const setting = settingsState.setting

// number(0/1) <-> boolean 双向桥接
const makeBoolBridge = (key: 'emailNotice' | 'collectNotice' | 'commentNotice') =>
  computed<boolean>({
    get: () => setting[key] === 1,
    set: (val: boolean) => {
      setting[key] = val ? 1 : 0
    },
  })

const emailNoticeBool = makeBoolBridge('emailNotice')
const collectNoticeBool = makeBoolBridge('collectNotice')
const commentNoticeBool = makeBoolBridge('commentNotice')

defineOptions({
  name: 'MessageSettingsSection',
})
</script>

<template>
  <div class="space-y-8">
    <!-- Section Header -->
    <header class="space-y-2">
      <div class="flex items-center gap-3">
        <span class="block h-6 w-1 rounded-full bg-amber-500"></span>
        <h3 class="text-xl font-semibold tracking-tight text-slate-900 dark:text-white md:text-2xl">消息设置</h3>
      </div>
      <p class="text-sm text-slate-500 dark:text-slate-400">控制接收各类通知的频率与方式。开关将立即生效。</p>
    </header>

    <section class="space-y-3">
      <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
        Notifications · 提醒配置
      </div>

      <!-- 邮件通知 -->
      <div class="cf-notice-row flex items-center justify-between gap-4 rounded-xl border border-slate-200 bg-white/60 p-4 transition-all duration-200 hover:border-amber-300 hover:bg-amber-50/30 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-amber-400/30 dark:hover:bg-amber-500/[0.06]">
        <div class="flex min-w-0 items-center gap-3">
          <span
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border transition-colors"
            :class="emailNoticeBool
              ? 'border-amber-300 bg-amber-100 text-amber-600 dark:border-amber-400/30 dark:bg-amber-500/15 dark:text-amber-400'
              : 'border-slate-200 bg-slate-50 text-slate-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-400'"
          >
            <UIcon name="i-lucide-mail" class="h-4 w-4" />
          </span>
          <div class="min-w-0 space-y-1">
            <div class="flex items-center gap-2">
              <p class="text-sm font-semibold text-slate-900 dark:text-white">邮件通知</p>
              <span
                class="text-[10px] font-medium"
                :class="emailNoticeBool ? 'text-amber-600 dark:text-amber-400' : 'text-slate-400 dark:text-slate-500'"
              >
                {{ emailNoticeBool ? 'ON' : 'OFF' }}
              </span>
            </div>
            <p class="text-xs text-slate-500 dark:text-slate-400">接收系统重要更新与安全提醒</p>
          </div>
        </div>
        <USwitch v-model="emailNoticeBool" class="shrink-0" />
      </div>

      <!-- 收藏提醒 -->
      <div class="cf-notice-row flex items-center justify-between gap-4 rounded-xl border border-slate-200 bg-white/60 p-4 transition-all duration-200 hover:border-amber-300 hover:bg-amber-50/30 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-amber-400/30 dark:hover:bg-amber-500/[0.06]">
        <div class="flex min-w-0 items-center gap-3">
          <span
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border transition-colors"
            :class="collectNoticeBool
              ? 'border-amber-300 bg-amber-100 text-amber-600 dark:border-amber-400/30 dark:bg-amber-500/15 dark:text-amber-400'
              : 'border-slate-200 bg-slate-50 text-slate-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-400'"
          >
            <UIcon name="i-lucide-bookmark" class="h-4 w-4" />
          </span>
          <div class="min-w-0 space-y-1">
            <div class="flex items-center gap-2">
              <p class="text-sm font-semibold text-slate-900 dark:text-white">收藏提醒</p>
              <span
                class="text-[10px] font-medium"
                :class="collectNoticeBool ? 'text-amber-600 dark:text-amber-400' : 'text-slate-400 dark:text-slate-500'"
              >
                {{ collectNoticeBool ? 'ON' : 'OFF' }}
              </span>
            </div>
            <p class="text-xs text-slate-500 dark:text-slate-400">当你的网站被他人收藏时通知我</p>
          </div>
        </div>
        <USwitch v-model="collectNoticeBool" class="shrink-0" />
      </div>

      <!-- 评论与互动 -->
      <div class="cf-notice-row flex items-center justify-between gap-4 rounded-xl border border-slate-200 bg-white/60 p-4 transition-all duration-200 hover:border-amber-300 hover:bg-amber-50/30 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-amber-400/30 dark:hover:bg-amber-500/[0.06]">
        <div class="flex min-w-0 items-center gap-3">
          <span
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border transition-colors"
            :class="commentNoticeBool
              ? 'border-amber-300 bg-amber-100 text-amber-600 dark:border-amber-400/30 dark:bg-amber-500/15 dark:text-amber-400'
              : 'border-slate-200 bg-slate-50 text-slate-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-400'"
          >
            <UIcon name="i-lucide-message-circle" class="h-4 w-4" />
          </span>
          <div class="min-w-0 space-y-1">
            <div class="flex items-center gap-2">
              <p class="text-sm font-semibold text-slate-900 dark:text-white">评论与互动</p>
              <span
                class="text-[10px] font-medium"
                :class="commentNoticeBool ? 'text-amber-600 dark:text-amber-400' : 'text-slate-400 dark:text-slate-500'"
              >
                {{ commentNoticeBool ? 'ON' : 'OFF' }}
              </span>
            </div>
            <p class="text-xs text-slate-500 dark:text-slate-400">当有新评论、新回复或新点赞时通知我</p>
          </div>
        </div>
        <USwitch v-model="commentNoticeBool" class="shrink-0" />
      </div>

      <p class="pt-2 text-[11px] text-slate-400 dark:text-slate-500">
        <UIcon name="i-lucide-info" class="-mt-0.5 mr-1 inline h-3 w-3" />
        关闭通知后仍可在「站内消息中心」查看历史消息
      </p>
    </section>
  </div>
</template>
