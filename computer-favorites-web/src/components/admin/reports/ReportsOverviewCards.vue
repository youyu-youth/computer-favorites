<script setup lang="ts">
import { computed } from 'vue'
import type { AdminReportStatistics } from '@/types/report'

const props = defineProps<{
  statistics: AdminReportStatistics
  statusSummaryText: string
}>()

const cards = computed(() => [
  {
    key: 'pending',
    label: '待处理举报',
    value: props.statistics.pending,
    icon: 'fas fa-hourglass-half',
    accentClass:
      'border-amber-200/80 bg-amber-50/90 text-amber-700 dark:border-amber-500/20 dark:bg-amber-500/10 dark:text-amber-200',
    toneClass:
      'bg-[radial-gradient(circle_at_top,rgba(251,191,36,0.24),transparent_60%)] dark:bg-[radial-gradient(circle_at_top,rgba(245,158,11,0.2),transparent_60%)]',
    helper: props.statusSummaryText,
  },
  {
    key: 'last24Hours',
    label: '24h 新增',
    value: props.statistics.last24Hours,
    icon: 'fas fa-bolt',
    accentClass:
      'border-blue-200/80 bg-blue-50/90 text-blue-700 dark:border-blue-500/20 dark:bg-blue-500/10 dark:text-blue-200',
    toneClass:
      'bg-[radial-gradient(circle_at_top,rgba(59,130,246,0.2),transparent_60%)] dark:bg-[radial-gradient(circle_at_top,rgba(96,165,250,0.18),transparent_60%)]',
    helper: '优先清理新增案件，避免队列积压。',
  },
  {
    key: 'processed',
    label: '已处理',
    value: props.statistics.processed,
    icon: 'fas fa-shield-check',
    accentClass:
      'border-emerald-200/80 bg-emerald-50/90 text-emerald-700 dark:border-emerald-500/20 dark:bg-emerald-500/10 dark:text-emerald-200',
    toneClass:
      'bg-[radial-gradient(circle_at_top,rgba(16,185,129,0.18),transparent_60%)] dark:bg-[radial-gradient(circle_at_top,rgba(52,211,153,0.16),transparent_60%)]',
    helper: `处理率 ${props.statistics.processRate}%`,
  },
  {
    key: 'mix',
    label: '类型分布',
    value: `${props.statistics.websiteCount} / ${props.statistics.commentCount}`,
    icon: 'fas fa-layer-group',
    accentClass:
      'border-slate-200/80 bg-slate-50/90 text-slate-700 dark:border-white/10 dark:bg-white/5 dark:text-slate-100',
    toneClass:
      'bg-[radial-gradient(circle_at_top,rgba(148,163,184,0.2),transparent_60%)] dark:bg-[radial-gradient(circle_at_top,rgba(148,163,184,0.14),transparent_60%)]',
    helper: '前者为网站举报，后者为评论举报。',
  },
])
</script>

<template>
  <section class="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
    <article
      v-for="card in cards"
      :key="card.key"
      class="group relative overflow-hidden rounded-2xl border bg-white p-4 shadow-[0_10px_28px_rgba(15,23,42,0.06)] transition-transform duration-200 hover:-translate-y-0.5 dark:bg-dark-card"
      :class="[card.accentClass, card.toneClass]"
    >
      <div class="absolute right-3 top-3 opacity-10 transition-transform duration-300 group-hover:scale-110">
        <i :class="[card.icon, 'text-[48px]']"></i>
      </div>

      <div class="relative flex items-start justify-between gap-3">
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
          class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border border-white/70 bg-white/60 text-lg shadow-sm dark:border-white/10 dark:bg-white/10"
        >
          <i :class="card.icon"></i>
        </div>
      </div>
    </article>
  </section>
</template>
