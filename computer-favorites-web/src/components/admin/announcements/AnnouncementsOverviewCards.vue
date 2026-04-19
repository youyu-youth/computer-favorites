<script setup lang="ts">
import { computed } from 'vue'
import type { AdminAnnouncementStatistics } from '@/types/admin-announcement'

const props = defineProps<{
  statistics: AdminAnnouncementStatistics
}>()

const cards = computed(() => [
  {
    key: 'total',
    label: '公告总数',
    value: props.statistics.total,
    icon: 'fas fa-bullhorn',
    tone: 'text-blue-600 bg-blue-50 border-blue-100 dark:text-blue-200 dark:bg-blue-900/20 dark:border-blue-900/40',
  },
  {
    key: 'visible',
    label: '显示中',
    value: props.statistics.visible,
    icon: 'fas fa-eye',
    tone: 'text-emerald-600 bg-emerald-50 border-emerald-100 dark:text-emerald-200 dark:bg-emerald-500/10 dark:border-emerald-500/20',
  },
  {
    key: 'hidden',
    label: '已隐藏',
    value: props.statistics.hidden,
    icon: 'fas fa-eye-slash',
    tone: 'text-slate-600 bg-slate-50 border-slate-200 dark:text-slate-200 dark:bg-slate-800/60 dark:border-slate-700',
  },
  {
    key: 'top',
    label: '置顶中',
    value: props.statistics.top,
    icon: 'fas fa-thumbtack',
    tone: 'text-amber-600 bg-amber-50 border-amber-100 dark:text-amber-200 dark:bg-amber-500/10 dark:border-amber-500/20',
  },
])
</script>

<template>
  <section class="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
    <article
      v-for="card in cards"
      :key="card.key"
      class="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm transition-colors dark:border-dark-border dark:bg-dark-card"
    >
      <div class="flex items-start justify-between gap-4">
        <div>
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ card.label }}</p>
          <p class="mt-3 text-3xl font-semibold tracking-tight text-gray-950 dark:text-white">{{ card.value }}</p>
        </div>

        <div
          class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border"
          :class="card.tone"
        >
          <i :class="card.icon" class="text-sm"></i>
        </div>
      </div>
    </article>
  </section>
</template>
