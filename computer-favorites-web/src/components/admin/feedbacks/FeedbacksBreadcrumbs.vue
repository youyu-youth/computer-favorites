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
            class="inline-flex items-center rounded-sm hover:text-blue-600 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 dark:hover:text-blue-400"
          >
            <i class="fas fa-home mr-1.5 text-[13px] opacity-80" aria-hidden="true"></i>
            控制台
          </RouterLink>
        </li>
        <li class="flex items-center" aria-hidden="true">
          <i class="fas fa-chevron-right mx-1 text-[10px] text-gray-400 opacity-60"></i>
        </li>
        <li class="font-semibold text-gray-900 dark:text-gray-100" aria-current="page">反馈处理</li>
      </ol>
    </nav>

    <div class="mt-4 overflow-hidden rounded-2xl border border-gray-200 bg-white px-5 py-5 shadow-[0_14px_34px_rgba(15,23,42,0.06)] dark:border-dark-border dark:bg-dark-card">
      <div class="min-w-0">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <div
            class="inline-flex items-center gap-2 rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.22em] text-blue-700 dark:border-blue-400/20 dark:bg-blue-500/10 dark:text-blue-200"
          >
            <span class="h-1.5 w-1.5 rounded-full bg-blue-500"></span>
            Feedback Desk
          </div>
          <div
            class="inline-flex items-center gap-2 rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.2em] text-emerald-700 dark:border-emerald-400/20 dark:bg-emerald-500/10 dark:text-emerald-200"
          >
            <i class="fas fa-headset text-[10px]"></i>
            Service Flow
          </div>
        </div>

        <h1 class="text-2xl font-semibold tracking-tight text-gray-950 dark:text-gray-50">
          用户反馈处理工作台
        </h1>
        <p class="mt-2 max-w-3xl text-sm leading-6 text-gray-600 dark:text-gray-300">
          聚焦用户建议、Bug 反馈、投诉与体验评价，在同一页完成筛选、查看、回复和关闭，保证工单流转清晰且可追溯。
        </p>
      </div>
    </div>
  </section>
</template>
