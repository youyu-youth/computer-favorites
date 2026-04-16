<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="mb-6">
    <nav class="flex items-center text-sm font-medium" aria-label="Breadcrumb">
      <ol class="flex min-w-0 items-center space-x-2 text-gray-500 dark:text-gray-400">
        <li>
          <RouterLink
            to="/computer/admin"
            class="inline-flex items-center rounded-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 hover:text-blue-600 dark:hover:text-blue-400"
          >
            <i class="fas fa-home mr-1.5 text-[13px] opacity-80" aria-hidden="true"></i>
            控制台
          </RouterLink>
        </li>

        <li class="flex items-center" aria-hidden="true">
          <i class="fas fa-chevron-right mx-1 text-[10px] text-gray-400 opacity-60"></i>
        </li>

        <li class="font-semibold text-gray-900 dark:text-gray-100" aria-current="page">举报处置</li>
      </ol>
    </nav>

    <div
      class="mt-4 overflow-hidden rounded-2xl border border-amber-200/70 bg-[linear-gradient(135deg,rgba(255,251,235,0.96),rgba(255,255,255,0.96))] px-5 py-5 shadow-[0_14px_34px_rgba(245,158,11,0.08)] dark:border-amber-500/20 dark:bg-[linear-gradient(135deg,rgba(28,23,17,0.96),rgba(15,19,23,0.96))]"
    >
      <div class="min-w-0">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <div
            class="inline-flex items-center gap-2 rounded-full border border-amber-300/80 bg-amber-50 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.22em] text-amber-700 dark:border-amber-400/20 dark:bg-amber-500/10 dark:text-amber-200"
          >
            <span class="h-1.5 w-1.5 rounded-full bg-amber-500"></span>
            Report Desk
          </div>
          <div
            class="inline-flex items-center gap-2 rounded-full border border-slate-300/80 bg-white/85 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.2em] text-slate-600 dark:border-white/15 dark:bg-white/8 dark:text-slate-200"
          >
            <i class="fas fa-shield-halved text-[10px]"></i>
            Real Time
          </div>
        </div>
        <h1 class="text-2xl font-semibold tracking-tight text-gray-950 dark:text-gray-50">
          用户举报处置工作台
        </h1>
        <p class="mt-2 max-w-2xl text-sm leading-6 text-gray-600 dark:text-gray-300">
          在同一页内完成风险概览、筛选检索、证据查看与处置决策，优先清理待处理举报并保留完整审核轨迹。
        </p>
      </div>
    </div>
  </section>
</template>
