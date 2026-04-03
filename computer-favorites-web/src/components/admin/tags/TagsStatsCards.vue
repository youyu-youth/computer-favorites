<script setup lang="ts">
import { computed } from 'vue'
import type { AdminTagStats } from '@/types/admin-tag'

const props = defineProps<{
  stats: AdminTagStats
}>()

const cards = computed(() => {
  return [
    {
      key: 'total',
      title: '标签总数',
      value: props.stats.total,
      tone: 'text-[#e95322] border-[#e95322]/35 bg-[#e95322]/10',
    },
    {
      key: 'in-use',
      title: '已使用',
      value: props.stats.inUse,
      tone: 'text-emerald-600 border-emerald-500/30 bg-emerald-500/10 dark:text-emerald-300',
    },
    {
      key: 'unused',
      title: '未使用',
      value: props.stats.unused,
      tone: 'text-amber-600 border-amber-500/30 bg-amber-500/10 dark:text-amber-300',
    },
    {
      key: 'today',
      title: '今日更新',
      value: props.stats.updatedToday,
      tone: 'text-sky-600 border-sky-500/30 bg-sky-500/10 dark:text-sky-300',
    },
  ]
})
</script>

<template>
  <section class="mb-4 p-4 sm:p-5">
    <div class="grid grid-cols-2 gap-3 lg:grid-cols-4">
      <article
        v-for="card in cards"
        :key="card.key"
        class="border border-gray-200 bg-white px-3 py-3 dark:border-dark-border dark:bg-dark-card"
      >
        <p class="text-xs text-gray-500 dark:text-gray-400">{{ card.title }}</p>
        <div class="mt-2 inline-flex items-center border px-2 py-1 text-sm font-semibold" :class="card.tone">
          {{ card.value }}
        </div>
      </article>
    </div>
  </section>
</template>
