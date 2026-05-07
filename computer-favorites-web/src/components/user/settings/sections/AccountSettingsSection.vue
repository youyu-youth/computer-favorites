<script setup lang="ts">
import { computed, inject } from 'vue'
import { settingsStateKey } from '../context'

const settingsState = inject(settingsStateKey)
if (!settingsState) {
  throw new Error('Settings state is not provided')
}

const basicInfo = settingsState.basicInfo

const emailBound = computed(() => basicInfo.emailVerified === 1)
const phoneBound = computed(() => basicInfo.phoneVerified === 1)

const oauthBindings = [
  { id: 'github', label: 'GitHub', desc: '使用 GitHub 账号快捷登录与同步', bound: true, icon: 'i-lucide-github' },
  { id: 'gitee', label: 'Gitee', desc: '国内 Gitee 仓库快捷登录', bound: false, icon: 'i-lucide-git-branch' },
  { id: 'qq', label: 'QQ', desc: '通过 QQ 一键登录', bound: false, icon: 'i-lucide-message-square' },
] as const

defineOptions({
  name: 'AccountSettingsSection',
})
</script>

<template>
  <div class="space-y-8">
    <!-- Section Header -->
    <header class="space-y-2">
      <div class="flex items-center gap-3">
        <span class="block h-6 w-1 rounded-full bg-amber-500"></span>
        <h3 class="text-xl font-semibold tracking-tight text-slate-900 dark:text-white md:text-2xl">账号设置</h3>
      </div>
      <p class="text-sm text-slate-500 dark:text-slate-400">管理你的登录邮箱、手机号及账号安全策略。</p>
    </header>

    <!-- 联系方式 -->
    <section class="space-y-4">
      <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
        Contact · 联系方式
      </div>

      <!-- 邮箱 -->
      <div class="cf-row group flex items-center justify-between gap-4 rounded-xl border border-slate-200 bg-white/60 p-4 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25]">
        <div class="flex min-w-0 items-center gap-4">
          <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-amber-200 bg-amber-50 text-amber-600 dark:border-amber-400/20 dark:bg-amber-500/10 dark:text-amber-400">
            <UIcon name="i-lucide-mail" class="h-5 w-5" />
          </span>
          <div class="min-w-0 space-y-1">
            <div class="flex items-center gap-2">
              <p class="text-sm font-semibold text-slate-900 dark:text-white">电子邮箱</p>
              <span
                class="inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-medium"
                :class="emailBound
                  ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300'
                  : 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-300'"
              >
                <span class="inline-block h-1 w-1 rounded-full" :class="emailBound ? 'bg-emerald-500' : 'bg-rose-500'"></span>
                {{ emailBound ? '已绑定' : '未绑定' }}
              </span>
            </div>
            <p class="truncate font-mono text-sm tracking-wide text-slate-600 dark:text-slate-300">
              {{ emailBound ? basicInfo.email : '未绑定邮箱，无法找回密码' }}
            </p>
          </div>
        </div>
        <button
          type="button"
          class="cf-action-btn shrink-0 inline-flex h-9 cursor-pointer items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3.5 text-xs font-medium text-slate-700 transition-colors hover:border-amber-400 hover:text-amber-600 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-200 dark:hover:border-amber-400/40 dark:hover:text-amber-300"
        >
          <UIcon :name="emailBound ? 'i-lucide-pencil' : 'i-lucide-link'" class="h-3.5 w-3.5" />
          <span>{{ emailBound ? '修改' : '立即绑定' }}</span>
        </button>
      </div>

      <!-- 手机号 -->
      <div class="cf-row group flex items-center justify-between gap-4 rounded-xl border border-slate-200 bg-white/60 p-4 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25]">
        <div class="flex min-w-0 items-center gap-4">
          <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-amber-200 bg-amber-50 text-amber-600 dark:border-amber-400/20 dark:bg-amber-500/10 dark:text-amber-400">
            <UIcon name="i-lucide-smartphone" class="h-5 w-5" />
          </span>
          <div class="min-w-0 space-y-1">
            <div class="flex items-center gap-2">
              <p class="text-sm font-semibold text-slate-900 dark:text-white">手机号码</p>
              <span
                class="inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-medium"
                :class="phoneBound
                  ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300'
                  : 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-300'"
              >
                <span class="inline-block h-1 w-1 rounded-full" :class="phoneBound ? 'bg-emerald-500' : 'bg-rose-500'"></span>
                {{ phoneBound ? '已绑定' : '未绑定' }}
              </span>
            </div>
            <p class="truncate font-mono text-sm tracking-wide text-slate-600 dark:text-slate-300">
              {{ phoneBound ? basicInfo.phone : '未绑定手机号，无法通过短信找回密码' }}
            </p>
          </div>
        </div>
        <button
          type="button"
          class="cf-action-btn shrink-0 inline-flex h-9 cursor-pointer items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3.5 text-xs font-medium text-slate-700 transition-colors hover:border-amber-400 hover:text-amber-600 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-200 dark:hover:border-amber-400/40 dark:hover:text-amber-300"
        >
          <UIcon :name="phoneBound ? 'i-lucide-pencil' : 'i-lucide-link'" class="h-3.5 w-3.5" />
          <span>{{ phoneBound ? '修改' : '立即绑定' }}</span>
        </button>
      </div>
    </section>

    <div class="cf-hairline" aria-hidden="true"></div>

    <!-- 安全 -->
    <section class="space-y-4">
      <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
        Security · 账号安全
      </div>

      <div class="cf-row flex items-center justify-between gap-4 rounded-xl border border-slate-200 bg-white/60 p-4 dark:border-white/[0.10] dark:bg-[#16161d]">
        <div class="flex min-w-0 items-center gap-4">
          <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-slate-200 bg-slate-100 text-slate-600 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-300">
            <UIcon name="i-lucide-shield-check" class="h-5 w-5" />
          </span>
          <div class="min-w-0 space-y-1">
            <p class="text-sm font-semibold text-slate-900 dark:text-white">登录密码</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">
              <UIcon name="i-lucide-clock-3" class="-mt-0.5 mr-1 inline h-3 w-3" />
              上次修改：2026-02-15 · 建议每 90 天轮换一次
            </p>
          </div>
        </div>
        <button
          type="button"
          class="cf-action-btn shrink-0 inline-flex h-9 cursor-pointer items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3.5 text-xs font-medium text-slate-700 transition-colors hover:border-amber-400 hover:text-amber-600 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-200 dark:hover:border-amber-400/40 dark:hover:text-amber-300"
        >
          <UIcon name="i-lucide-key-round" class="h-3.5 w-3.5" />
          <span>修改密码</span>
        </button>
      </div>
    </section>

    <div class="cf-hairline" aria-hidden="true"></div>

    <!-- 第三方平台 -->
    <section class="space-y-4">
      <div class="flex items-center justify-between">
        <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
          OAuth · 第三方平台
        </div>
        <span class="text-[11px] text-slate-400 dark:text-slate-500">绑定后可实现一键登录</span>
      </div>

      <div class="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div
          v-for="bind in oauthBindings"
          :key="bind.id"
          class="group flex flex-col gap-3 rounded-xl border border-slate-200 bg-white/60 p-4 transition-colors hover:border-amber-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-amber-400/30 dark:hover:bg-[#1c1c25]"
        >
          <div class="flex items-center justify-between">
            <span class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-700 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-200">
              <UIcon :name="bind.icon" class="h-4 w-4" />
            </span>
            <span
              class="inline-flex items-center gap-1 text-[10px] font-medium"
              :class="bind.bound ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-400 dark:text-slate-500'"
            >
              <span class="inline-block h-1 w-1 rounded-full" :class="bind.bound ? 'bg-emerald-500' : 'bg-slate-400 dark:bg-slate-500'"></span>
              {{ bind.bound ? '已绑定' : '未绑定' }}
            </span>
          </div>
          <div class="space-y-1">
            <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ bind.label }}</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">{{ bind.desc }}</p>
          </div>
          <button
            type="button"
            class="mt-1 inline-flex h-8 cursor-pointer items-center justify-center gap-1 rounded-lg border text-xs font-medium transition-colors"
            :class="bind.bound
              ? 'border-slate-200 bg-white text-slate-600 hover:border-rose-300 hover:text-rose-500 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-300 dark:hover:border-rose-400/40 dark:hover:text-rose-400'
              : 'border-amber-300 bg-amber-50 text-amber-700 hover:border-amber-400 hover:bg-amber-100/70 dark:border-amber-400/30 dark:bg-amber-500/10 dark:text-amber-300 dark:hover:bg-amber-500/15'"
          >
            {{ bind.bound ? '解除绑定' : '立即绑定' }}
          </button>
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
