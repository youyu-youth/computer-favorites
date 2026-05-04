<script setup lang="ts">
import { computed } from 'vue'
import type { AdminCommentStatistics } from '@/types/admin-comment'

const props = defineProps<{
  statistics: AdminCommentStatistics
  statusSummaryText: string
}>()

const cards = computed(() => [
  {
    key: 'total',
    label: '评论总量',
    value: props.statistics.total,
    icon: 'fas fa-comments',
    accentClass:
      'border-orange-200 bg-orange-50/80 text-orange-700 dark:border-orange-400/20 dark:bg-orange-500/10 dark:text-orange-200',
    helper: props.statusSummaryText,
  },
  {
    key: 'todayNew',
    label: '今日新增',
    value: props.statistics.todayNew,
    icon: 'fas fa-bolt',
    accentClass:
      'border-blue-200 bg-blue-50/80 text-blue-700 dark:border-blue-400/20 dark:bg-blue-500/10 dark:text-blue-200',
    helper: '今日新增评论量，高峰期需关注审核队列。',
  },
  {
    key: 'visible',
    label: '已显示',
    value: props.statistics.visible,
    icon: 'fas fa-eye',
    accentClass:
      'border-emerald-200 bg-emerald-50/80 text-emerald-700 dark:border-emerald-400/20 dark:bg-emerald-500/10 dark:text-emerald-200',
    helper: '正常展示中的评论数量。',
  },
  {
    key: 'hidden',
    label: '已隐藏',
    value: props.statistics.hidden,
    icon: 'fas fa-eye-slash',
    accentClass:
      'border-red-200 bg-red-50/80 text-red-700 dark:border-red-400/20 dark:bg-red-500/10 dark:text-red-200',
    helper: '被隐藏评论可随时恢复显示。',
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
