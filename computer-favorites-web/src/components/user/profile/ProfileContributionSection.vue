<script setup lang="ts">
import type { ProfileContribution, ProfileStat } from '@/types/profile'

defineProps<{
  stats: ProfileStat[]
  contributions: ProfileContribution[]
}>()
</script>

<template>
  <section class="space-y-4">
    <div class="flex items-center gap-2">
      <UIcon name="i-lucide-chart-column-big" class="size-5 text-primary-500" />
      <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">贡献概览</h3>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
      <UCard v-for="stat in stats" :key="stat.label" class="!ring-0 shadow-sm dark:shadow-md bg-white dark:!bg-[#131418] rounded-xl">
        <div class="space-y-1">
          <p class="text-xs text-gray-500 dark:text-gray-400">{{ stat.label }}</p>
          <p class="text-2xl font-semibold text-gray-900 dark:text-gray-100">{{ stat.value }}</p>
        </div>
      </UCard>
    </div>

    <UCard class="!ring-0 shadow-sm dark:shadow-md bg-white dark:!bg-[#131418] rounded-xl">
      <div class="space-y-4">
        <article
          v-for="item in contributions"
          :key="item.title"
          class="space-y-1"
        >
          <h4 class="text-sm font-semibold text-gray-900 dark:text-gray-100">{{ item.title }}</h4>
          <p class="text-sm text-gray-600 dark:text-gray-400">{{ item.summary }}</p>
        </article>
      </div>
    </UCard>
  </section>
</template>
