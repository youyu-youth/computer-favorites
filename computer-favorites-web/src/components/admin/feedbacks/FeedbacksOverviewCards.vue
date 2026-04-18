<script setup lang="ts">
import { computed } from 'vue'
import type { AdminFeedbackStatistics } from '@/types/feedback'

const props = defineProps<{
  statistics: AdminFeedbackStatistics
  statusSummaryText: string
}>()

const cards = computed(() => [
  {
    key: 'pending',
    label: '待处理工单',
    value: props.statistics.pending,
    icon: 'fas fa-inbox',
    accentClass:
      'border-amber-200 bg-amber-50/80 text-amber-700 dark:border-amber-400/20 dark:bg-amber-500/10 dark:text-amber-200',
    helper: props.statusSummaryText,
  },
  {
    key: 'last24Hours',
    label: '24h 新增',
    value: props.statistics.last24Hours,
    icon: 'fas fa-clock-rotate-left',
    accentClass:
      'border-blue-200 bg-blue-50/80 text-blue-700 dark:border-blue-400/20 dark:bg-blue-500/10 dark:text-blue-200',
    helper: '最近新增越多，越需要优先压缩待处理队列。',
  },
  {
    key: 'bug',
    label: 'Bug 反馈',
    value: props.statistics.bugCount,
    icon: 'fas fa-bug',
    accentClass:
      'border-red-200 bg-red-50/80 text-red-700 dark:border-red-400/20 dark:bg-red-500/10 dark:text-red-200',
    helper: '这类反馈通常最需要截图和复现场景支持。',
  },
  {
    key: 'replyRate',
    label: '回复覆盖率',
    value: `${props.statistics.replyRate}%`,
    icon: 'fas fa-reply-all',
    accentClass:
      'border-emerald-200 bg-emerald-50/80 text-emerald-700 dark:border-emerald-400/20 dark:bg-emerald-500/10 dark:text-emerald-200',
    helper: `${props.statistics.withImagesCount} 条带图，${props.statistics.withContactCount} 条可直接回访。`,
  },
])
</script>

<template>
  <section class="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
    <article
      v-for="card in cards"
      :key="card.key"
      class="group relative overflow-hidden rounded-2xl border bg-white p-4 shadow-[0_10px_28px_rgba(15,23,42,0.06)] transition-transform duration-200 hover:-translate-y-0.5 dark:bg-dark-card"
      :class="card.accentClass"
    >
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0">
          <p class="text-xs font-semibold uppercase tracking-[0.18em] opacity-80">{{ card.label }}</p>
          <p class="mt-3 text-3xl font-semibold tracking-tight text-gray-950 dark:text-white">
            {{ card.value }}
          </p>
          <p class="mt-2 max-w-[18rem] text-sm leading-6 text-gray-600 dark:text-gray-300">
            {{ card.helper }}
          </p>
        </div>

        <div
          class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border border-white/70 bg-white/70 text-lg shadow-sm dark:border-white/10 dark:bg-white/10"
        >
          <i :class="card.icon"></i>
        </div>
      </div>
    </article>
  </section>
</template>
