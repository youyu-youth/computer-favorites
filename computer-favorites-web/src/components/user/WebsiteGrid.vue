<script setup lang="ts">
import type { Website } from '@/types/website'
import WebsiteCard from './WebsiteCard.vue'

defineProps<{
  websites: Website[]
  searchQuery: string
}>()

const emit = defineEmits<{
  'update:searchQuery': [value: string]
  'copy-link': [website: Website]
  visit: [website: Website]
}>()

const handleSearchInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:searchQuery', target.value)
}
</script>

<template>
  <div>
    <!-- Search Bar and Title -->
    <div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <h2 class="text-2xl font-bold flex items-center gap-2 text-gray-800 dark:text-white">
        Favorites
        <svg class="w-6 h-6 text-pink-500 fill-current" viewBox="0 0 24 24">
          <path
            d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
          />
        </svg>
      </h2>

      <!-- Search Bar -->
      <div class="relative w-full sm:w-72 md:w-96">
        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </div>
        <input
          :value="searchQuery"
          type="text"
          class="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-dark-border rounded-lg leading-5 bg-white dark:bg-[#121212] text-gray-900 dark:text-gray-100 placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-primary-500 focus:border-primary-500 sm:text-sm transition-colors"
          placeholder="搜索网站..."
          @input="handleSearchInput"
        />
      </div>
    </div>

    <!-- Cards Grid -->
    <div v-if="websites.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <WebsiteCard
        v-for="site in websites"
        :key="site.id"
        :website="site"
        @copy-link="emit('copy-link', site)"
        @visit="emit('visit', site)"
      />
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-20">
      <svg
        class="mx-auto h-12 w-12 text-gray-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
        ></path>
      </svg>
      <h3 class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">未找到匹配的网站</h3>
      <p class="mt-1 text-sm text-gray-500">请尝试调整筛选条件或搜索关键词。</p>
    </div>
  </div>
</template>
