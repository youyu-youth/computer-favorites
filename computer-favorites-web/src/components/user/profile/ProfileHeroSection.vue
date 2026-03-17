<script setup lang="ts">
import type { ProfileData } from '@/types/profile'

defineProps<{
  profile: ProfileData
}>()
</script>

<template>
  <UCard class="!ring-0 shadow-sm dark:shadow-md bg-white dark:bg-gray-900 rounded-xl">
    <div class="space-y-5">
      <div class="space-y-2">
        <h1 class="text-3xl md:text-4xl font-bold tracking-tight text-gray-900 dark:text-gray-100">
          Hello, I'm {{ profile.name }}
        </h1>
        <p class="text-base md:text-lg text-gray-700 dark:text-gray-200">{{ profile.role }}</p>
      </div>

      <p class="text-sm md:text-base leading-7 text-gray-600 dark:text-gray-300">
        {{ profile.signature }}
      </p>

      <div class="flex flex-wrap gap-3">
        <UButton
          v-for="link in profile.socialLinks"
          :key="link.label"
          :to="link.url"
          color="neutral"
          variant="soft"
          size="sm"
        >
          <UIcon :name="link.icon" class="size-4" />
          <span>{{ link.label }}</span>
        </UButton>
      </div>
    </div>
  </UCard>
</template>
