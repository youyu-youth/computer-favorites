<script setup lang="ts">
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()
const setting = settingsStore.setting

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
            <p class="text-sm font-semibold text-slate-900 dark:text-white">邮件通知</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">接收系统重要更新与安全提醒</p>
          </div>
        </div>
        <button
          type="button"
          role="switch"
          :aria-checked="emailNoticeBool"
          aria-label="邮件通知开关"
          class="cf-toggle shrink-0"
          :class="{ 'is-on': emailNoticeBool }"
          @click="emailNoticeBool = !emailNoticeBool"
        >
          <span class="cf-toggle-thumb"></span>
        </button>
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
            <p class="text-sm font-semibold text-slate-900 dark:text-white">收藏提醒</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">当你的网站被他人收藏时通知我</p>
          </div>
        </div>
        <button
          type="button"
          role="switch"
          :aria-checked="collectNoticeBool"
          aria-label="收藏提醒开关"
          class="cf-toggle shrink-0"
          :class="{ 'is-on': collectNoticeBool }"
          @click="collectNoticeBool = !collectNoticeBool"
        >
          <span class="cf-toggle-thumb"></span>
        </button>
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
            <p class="text-sm font-semibold text-slate-900 dark:text-white">评论与互动</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">当有新评论、新回复或新点赞时通知我</p>
          </div>
        </div>
        <button
          type="button"
          role="switch"
          :aria-checked="commentNoticeBool"
          aria-label="评论与互动开关"
          class="cf-toggle shrink-0"
          :class="{ 'is-on': commentNoticeBool }"
          @click="commentNoticeBool = !commentNoticeBool"
        >
          <span class="cf-toggle-thumb"></span>
        </button>
      </div>

      <p class="pt-2 text-[11px] text-slate-400 dark:text-slate-500">
        <UIcon name="i-lucide-info" class="-mt-0.5 mr-1 inline h-3 w-3" />
        关闭通知后仍可在「站内消息中心」查看历史消息
      </p>
    </section>
  </div>
</template>

<style scoped>
/* 通知行专属开关：纯 CSS toggle，避免 PrimeVue unstyled 模式下的渲染异常 */
.cf-toggle {
  position: relative;
  display: inline-flex;
  align-items: center;
  width: 44px;
  height: 24px;
  flex-shrink: 0;
  padding: 0;
  border: 1px solid rgb(148 163 184 / 0.45);
  border-radius: 9999px;
  background: rgb(226 232 240);
  cursor: pointer;
  transition:
    background-color 200ms ease,
    border-color 200ms ease,
    box-shadow 200ms ease;
}
.cf-toggle:hover:not(:disabled) {
  border-color: rgb(148 163 184 / 0.7);
}
.cf-toggle:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgb(245 158 11 / 0.35);
}

.cf-toggle-thumb {
  position: absolute;
  top: 50%;
  left: 2px;
  width: 18px;
  height: 18px;
  border-radius: 9999px;
  background: white;
  box-shadow: 0 1px 3px rgb(15 23 42 / 0.25);
  transform: translateY(-50%);
  transition: transform 220ms cubic-bezier(0.34, 1.56, 0.64, 1);
}

.cf-toggle.is-on {
  background: rgb(245 158 11);
  border-color: rgb(217 119 6 / 0.6);
  box-shadow: 0 0 0 4px rgb(245 158 11 / 0.12);
}
.cf-toggle.is-on .cf-toggle-thumb {
  transform: translate(20px, -50%);
}

/* 暗黑模式 */
:where(html.dark) .cf-toggle {
  background: rgb(71 85 105 / 0.7);
  border-color: rgb(148 163 184 / 0.3);
}
:where(html.dark) .cf-toggle:hover:not(:disabled) {
  border-color: rgb(148 163 184 / 0.5);
}
:where(html.dark) .cf-toggle.is-on {
  background: rgb(245 158 11);
  border-color: rgb(245 158 11 / 0.7);
  box-shadow: 0 0 0 4px rgb(245 158 11 / 0.18);
}
:where(html.dark) .cf-toggle:focus-visible {
  box-shadow: 0 0 0 3px rgb(245 158 11 / 0.4);
}

@media (prefers-reduced-motion: reduce) {
  .cf-toggle,
  .cf-toggle-thumb {
    transition: none;
  }
}
</style>
