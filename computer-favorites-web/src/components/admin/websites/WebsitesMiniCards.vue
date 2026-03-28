<script setup lang="ts">

const props = defineProps<{
    title: string;
    isConnector?: boolean;
    items: Array<{
        title: string;
        subtitle: string;
    }>
}>()
</script>

<template>
  <div class="mb-8">
    <h2 class="text-gray-500 dark:text-gray-400 text-sm font-medium mb-3">{{ title }}</h2>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div v-for="item in items" :key="item.title" class="bg-white dark:bg-darkCard border border-gray-200 dark:border-darkBorder rounded-md p-3 hover:border-gray-300 dark:hover:border-gray-600 transition-colors cursor-pointer group">
        <template v-if="isConnector">
          <div class="flex items-center gap-2 mb-1">
            <a href="#" class="text-brandBlue text-sm group-hover:underline truncate">{{ item.title }}</a>
            <span class="w-1.5 h-1.5 rounded-full bg-brandGreen flex-shrink-0"></span>
          </div>
          <span class="text-gray-500 dark:text-gray-500 text-xs truncate block">{{ item.subtitle }}</span>
        </template>
        <template v-else>
          <a href="#" class="text-brandBlue text-sm block mb-1 group-hover:underline">{{ item.title }}</a>
          <span class="text-gray-500 dark:text-gray-500 text-xs">{{ item.subtitle }}</span>
        </template>
      </div>
    </div>
  </div>
</template>
