<script setup lang="ts">
import type { ProfileSiteItem } from '@/types/profile'

defineProps<{
  title: string
  items: ProfileSiteItem[]
}>()
</script>

<template>
  <section class="space-y-4">
    <div class="flex items-center gap-2">
      <UIcon name="i-lucide-grid-2x2" class="size-5 text-primary-500" />
      <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">{{ title }}</h3>
    </div>
    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
      <UCard v-for="item in items" :key="item.name" class="h-full !ring-0 shadow-sm dark:shadow-md bg-white dark:bg-gray-900 rounded-xl">
        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <h4 class="text-sm font-semibold text-gray-900 dark:text-gray-100">{{ item.name }}</h4>
            <UIcon :name="item.icon" class="size-5 text-primary-500" />
          </div>
          <p class="text-xs leading-6 text-gray-600 dark:text-gray-300">{{ item.description }}</p>
        </div>
      </UCard>
    </div>
  </section>
</template>
