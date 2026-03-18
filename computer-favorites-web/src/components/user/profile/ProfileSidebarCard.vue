<script setup lang="ts">
import type { ProfileData } from '@/types/profile'

defineProps<{
  profile: ProfileData
}>()
</script>

<template>
  <aside class="w-full lg:w-80 shrink-0">
    <div class="lg:sticky lg:top-24 space-y-4">
      <UCard class="!ring-0 shadow-sm dark:shadow-md bg-white dark:bg-gray-900 rounded-xl">
        <div class="flex flex-col items-center text-center gap-3">
          <UAvatar :src="profile.avatarUrl" :alt="profile.name" class="!w-32 !h-32 !rounded-full ring-2 ring-primary/30" :ui="{ rounded: 'rounded-full' }" />
          <div>
            <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-100">{{ profile.name }}</h2>
            <p class="text-sm text-gray-600 dark:text-gray-300">{{ profile.role }}</p>
          </div>
          <div class="w-full space-y-1 text-sm text-gray-600 dark:text-gray-300">
            <div class="flex items-center justify-center gap-2">
              <UIcon name="i-lucide-map-pin" class="size-4 text-primary-500" />
              <span>{{ profile.location }}</span>
            </div>
          </div>
        </div>
      </UCard>

      <UCard class="!ring-0 shadow-sm dark:shadow-md bg-white dark:bg-gray-900 rounded-xl">
        <div class="flex flex-wrap gap-2">
          <UBadge
            v-for="tag in profile.tags"
            :key="tag"
            color="neutral"
            variant="soft"
            class="rounded-md"
          >
            {{ tag }}
          </UBadge>
        </div>
      </UCard>

      <UCard class="!ring-0 shadow-sm dark:shadow-md bg-white dark:bg-gray-900 rounded-xl">
        <div class="space-y-4">
          <div
            v-for="item in profile.timeline"
            :key="`${item.title}-${item.date}`"
            class="relative pl-6"
          >
            <span class="absolute left-0 top-1 size-2 rounded-full bg-primary-500" />
            <p class="text-sm font-medium text-gray-800 dark:text-gray-100">{{ item.title }}</p>
            <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.date }}</p>
          </div>
        </div>
      </UCard>
    </div>
  </aside>
</template>
