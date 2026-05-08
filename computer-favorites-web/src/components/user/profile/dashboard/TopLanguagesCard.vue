<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * GitHub Stats 风格 Top Languages：单行堆叠条 + 色块图例
 */
import StatCard from './StatCard.vue'
import type { LangShare } from './types'

defineProps<{
  title: string
  langs: LangShare[]
}>()
</script>

<template>
  <StatCard :title="title">
    <div class="flex flex-col gap-3">
      <div class="flex h-2 w-full overflow-hidden rounded-sm bg-black/5 dark:bg-white/5">
        <div
          v-for="lang in langs"
          :key="lang.name"
          class="h-full transition-[width]"
          :style="{ width: `${lang.pct}%`, backgroundColor: lang.color }"
        />
      </div>
      <ul class="grid grid-cols-2 gap-x-4 gap-y-1.5 text-[12.5px] sm:grid-cols-3">
        <li
          v-for="lang in langs"
          :key="lang.name"
          class="flex items-center gap-2 font-mono"
        >
          <span class="size-2 shrink-0 rounded-full" :style="{ backgroundColor: lang.color }" />
          <span class="truncate text-gray-700 dark:text-gray-300">{{ lang.name }}</span>
          <span class="ml-auto text-gray-500 dark:text-gray-400">{{ lang.pct.toFixed(1) }}%</span>
        </li>
      </ul>
    </div>
  </StatCard>
</template>
