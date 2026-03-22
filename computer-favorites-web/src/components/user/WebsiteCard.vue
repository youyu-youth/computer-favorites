<script setup lang="ts">
import type { Website } from '@/types/website'

defineProps<{
  website: Website
}>()

const emit = defineEmits<{
  copyLink: []
  visit: []
}>()

const handleCopyLink = () => {
  emit('copyLink')
}

const handleVisit = () => {
  emit('visit')
}
</script>

<template>
  <div
    class="group relative bg-white dark:bg-dark-card rounded-xl border border-gray-200 dark:border-dark-border p-5 hover:border-primary-400 dark:hover:border-primary-500 transition-all duration-300 shadow-sm hover:shadow-md flex flex-col h-full"
  >
    <!-- Card Header -->
    <div class="flex justify-between items-start mb-3">
      <h3
        class="text-lg font-bold text-gray-900 dark:text-gray-100 group-hover:text-primary-500 transition-colors"
      >
        {{ website.title }}
      </h3>
      <div
        class="flex items-center text-xs font-medium text-primary-600 dark:text-primary-400 bg-primary-50 dark:bg-primary-500/10 px-2 py-1 rounded-md"
      >
        <svg class="w-3.5 h-3.5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M17.657 18.657A8 8 0 016.343 7.343S7 9 9 10c0-2 .5-5 2.986-7C14 5 16.09 5.777 17.656 7.343A7.975 7.975 0 0120 13a7.975 7.975 0 01-2.343 5.657z"
          ></path>
        </svg>
        {{ website.hot }}K
      </div>
    </div>

    <!-- Card Content -->
    <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-3 mb-4 flex-grow">
      {{ website.desc }}
    </p>

    <!-- Card Footer -->
    <div
      class="flex items-center justify-between mt-auto pt-4 border-t border-gray-100 dark:border-gray-800/50"
    >
      <!-- Tag -->
      <span
        class="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-medium bg-blue-50 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400 border border-blue-100 dark:border-blue-800/50"
      >
        {{ website.tag }}
      </span>

      <!-- Actions -->
      <div class="flex gap-2 text-gray-400 dark:text-gray-500">
        <button
          class="p-1.5 rounded hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-primary-500 transition-colors"
          title="复制链接"
          @click="handleCopyLink"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
            ></path>
          </svg>
        </button>
        <button
          class="p-1.5 rounded hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-primary-500 transition-colors"
          title="访问网站"
          @click="handleVisit"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
            ></path>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>
