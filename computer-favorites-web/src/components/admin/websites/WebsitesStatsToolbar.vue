<script setup lang="ts">

const props = defineProps<{
    viewMode: string
}>()

const emit = defineEmits<{
    (e: 'update:viewMode', val: string): void
}>()

const switchView = (mode: string) => {
    emit('update:viewMode', mode)
}
</script>

<template>
  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
    <div class="text-sm text-gray-500 dark:text-gray-400 flex items-center gap-2">
      <span>20,362 servers. Last updated 2026-03-28 13:56</span>
      <span class="w-2 h-2 rounded-full bg-brandGreen"></span>
    </div>

    <div class="flex flex-wrap items-center gap-3">
      <button class="bg-white dark:bg-darkCard border border-gray-300 dark:border-darkBorder text-gray-700 dark:text-gray-300 px-4 py-1.5 rounded-md text-sm font-medium hover:bg-gray-50 dark:hover:bg-darkBorder transition-colors">
        Deep Search
      </button>

      <div class="relative">
        <select class="appearance-none bg-white dark:bg-darkCard border border-gray-300 dark:border-darkBorder text-gray-700 dark:text-gray-300 pl-4 pr-8 py-1.5 rounded-md text-sm font-medium focus:outline-none focus:ring-1 focus:ring-brandOrange cursor-pointer">
          <option>Search Relevance</option>
          <option>Most Popular</option>
          <option>Newest</option>
        </select>
        <i class="fas fa-chevron-down absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-xs pointer-events-none"></i>
      </div>

      <!-- 视图切换按钮 -->
      <div class="flex items-center bg-white dark:bg-darkCard border border-gray-300 dark:border-darkBorder rounded-md overflow-hidden">
        <button @click="switchView('grid')" :class="{'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white': props.viewMode === 'grid', 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-darkBorder': props.viewMode !== 'grid'}" class="p-2 transition-colors cursor-pointer" title="网格视图">
          <i class="fas fa-th-large"></i>
        </button>
        <button @click="switchView('list')" :class="{'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white': props.viewMode === 'list', 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-darkBorder': props.viewMode !== 'list'}" class="p-2 transition-colors cursor-pointer" title="列表视图">
          <i class="fas fa-list"></i>
        </button>
      </div>

      <button class="bg-gray-900 text-white dark:bg-[#1f2937] dark:hover:bg-gray-700 border dark:border-gray-600 px-4 py-1.5 rounded-md text-sm font-medium hover:bg-gray-800 transition-colors cursor-pointer">
        Add Server
      </button>
    </div>
  </div>
</template>
