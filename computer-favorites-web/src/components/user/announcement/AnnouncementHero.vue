<script setup lang="ts">
import { BellRing, Clock3, Filter, Files } from 'lucide-vue-next'

defineProps<{
  totalCount: number
  latestPublishTime: string
  activeTypeLabel: string
}>()
</script>

<template>
  <section
    class="overflow-hidden bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.08)] dark:bg-black dark:shadow-[0_18px_48px_rgba(0,0,0,0.38)] sm:p-7"
  >
    <div class="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
      <div class="max-w-3xl">
        <div
          class="mb-4 inline-flex items-center gap-2 rounded-full border border-primary-500/35 bg-primary-500/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.28em] text-primary-700 dark:text-primary-300"
        >
          <BellRing class="h-3.5 w-3.5" />
          Announcement Log
        </div>
        <h1 class="text-3xl font-bold tracking-tight text-gray-900 dark:text-white sm:text-4xl">
          系统公告
        </h1>
        <p class="mt-3 max-w-2xl text-sm leading-7 text-gray-600 dark:text-gray-400 sm:text-[15px]">
          按发布时间查看平台功能更新、问题修复与系统变更。页面保留日志式阅读节奏，所有公告均完整展示标题与正文内容。
        </p>
      </div>

      <div class="grid grid-cols-1 gap-3 sm:grid-cols-3 lg:min-w-[420px]">
        <article
          class="border border-gray-200 bg-gray-50 p-4 dark:border-gray-800 dark:bg-black"
        >
          <div class="mb-2 flex items-center gap-2 text-xs uppercase tracking-[0.2em] text-gray-500 dark:text-gray-500">
            <Files class="h-4 w-4 text-primary-400" />
            公告数量
          </div>
          <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ totalCount }}</p>
        </article>

        <article
          class="border border-gray-200 bg-gray-50 p-4 dark:border-gray-800 dark:bg-black"
        >
          <div class="mb-2 flex items-center gap-2 text-xs uppercase tracking-[0.2em] text-gray-500 dark:text-gray-500">
            <Filter class="h-4 w-4 text-cyan-400" />
            当前分类
          </div>
          <p class="text-base font-semibold text-gray-900 dark:text-white">{{ activeTypeLabel }}</p>
        </article>

        <article
          class="border border-gray-200 bg-gray-50 p-4 dark:border-gray-800 dark:bg-black"
        >
          <div class="mb-2 flex items-center gap-2 text-xs uppercase tracking-[0.2em] text-gray-500 dark:text-gray-500">
            <Clock3 class="h-4 w-4 text-emerald-400" />
            最近发布
          </div>
          <p class="text-base font-semibold text-gray-900 dark:text-white">{{ latestPublishTime }}</p>
        </article>
      </div>
    </div>
  </section>
</template>
