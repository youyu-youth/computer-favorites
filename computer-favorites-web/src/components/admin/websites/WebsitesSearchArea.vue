<script setup lang="ts">
const props = defineProps<{
  searchQuery: string
}>()

const emit = defineEmits<{
  (e: 'update:searchQuery', val: string): void
}>()

const toggleInput = (event: Event) => {
  emit('update:searchQuery', (event.target as HTMLInputElement).value)
}
</script>

<template>
  <div>
    <!-- 搜索框 -->
    <div class="relative mb-6">
      <i
        class="fas fa-search absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg"
      ></i>
      <input
        type="text"
        :value="searchQuery"
        @input="toggleInput"
        placeholder="Search for MCP servers, tools, and more"
        class="w-full bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border text-gray-900 dark:text-gray-200 rounded-lg focus:ring-brand-orange focus:border-brand-orange block pl-12 p-3 outline-none transition-colors shadow-sm"
      />
    </div>

    <!-- 公告 Banner -->
    <div
      class="bg-brand-orange text-white px-4 py-3 rounded-md mb-8 shadow-sm flex items-center justify-between"
    >
      <p class="text-sm font-medium">
        <span class="font-bold">New!</span> You can now sort servers by usage in the last 30 days to
        find the most popular servers.
      </p>
    </div>
  </div>
</template>
