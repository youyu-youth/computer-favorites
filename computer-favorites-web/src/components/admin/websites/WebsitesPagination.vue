<script setup lang="ts">
const props = defineProps<{
  currentPage: number
  totalPages: number
  visiblePages: Array<number | string>
  total: number
  pageSize: number
}>()

const emit = defineEmits<{
  (e: 'prev'): void
  (e: 'next'): void
  (e: 'goto', page: number | string): void
}>()

const resolveStart = (): number => {
  if (props.total <= 0) {
    return 0
  }
  return (props.currentPage - 1) * props.pageSize + 1
}

const resolveEnd = (): number => {
  return Math.min(props.currentPage * props.pageSize, props.total)
}
</script>

<template>
  <div
    class="mt-8 flex items-center justify-between border-t border-gray-200 dark:border-dark-border pt-6"
  >
    <!-- 移动端简易分页 -->
    <div class="flex items-center justify-between w-full sm:hidden">
      <button
        @click="$emit('prev')"
        :disabled="currentPage === 1"
        class="relative inline-flex items-center px-4 py-2 border border-gray-300 dark:border-dark-border text-sm font-medium rounded-md text-gray-700 dark:text-gray-300 bg-white dark:bg-dark-card hover:bg-gray-50 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      >
        Previous
      </button>
      <p class="text-sm text-gray-500 dark:text-gray-400">
        Page <span class="font-medium text-gray-900 dark:text-white">{{ currentPage }}</span> of
        <span class="font-medium text-gray-900 dark:text-white">{{ totalPages }}</span>
      </p>
      <button
        @click="$emit('next')"
        :disabled="currentPage === totalPages"
        class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 dark:border-dark-border text-sm font-medium rounded-md text-gray-700 dark:text-gray-300 bg-white dark:bg-dark-card hover:bg-gray-50 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      >
        Next
      </button>
    </div>

    <!-- 桌面端完整分页 -->
    <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
      <div>
        <p class="text-sm text-gray-700 dark:text-gray-400">
          Showing
          <span class="font-medium text-gray-900 dark:text-white">{{ resolveStart() }}</span>
          to
          <span class="font-medium text-gray-900 dark:text-white">{{ resolveEnd() }}</span>
          of
          <span class="font-medium text-gray-900 dark:text-white">{{ total }}</span>
          results
        </p>
      </div>
      <div>
        <nav
          class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px"
          aria-label="Pagination"
        >
          <button
            @click="$emit('prev')"
            :disabled="currentPage === 1"
            class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 dark:border-dark-border bg-white dark:bg-dark-card text-sm font-medium text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <span class="sr-only">Previous</span>
            <i class="fas fa-chevron-left w-5 h-5 flex items-center justify-center"></i>
          </button>

          <template v-for="(page, index) in visiblePages">
            <span
              v-if="page === '...'"
              :key="`ellipsis-${index}`"
              class="relative inline-flex items-center px-4 py-2 border border-gray-300 dark:border-dark-border bg-white dark:bg-dark-card text-sm font-medium text-gray-700 dark:text-gray-400"
            >
              ...
            </span>
            <button
              v-else
              :key="`page-${page}`"
              @click="$emit('goto', page)"
              :class="[
                page === currentPage
                  ? 'z-10 bg-gray-100 dark:bg-gray-800 border-gray-300 dark:border-dark-border text-brand-orange dark:text-brand-orange font-bold'
                  : 'bg-white dark:bg-dark-card border-gray-300 dark:border-dark-border text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 font-medium',
                'relative inline-flex items-center px-4 py-2 border text-sm transition-colors cursor-pointer',
              ]"
            >
              {{ page }}
            </button>
          </template>

          <button
            @click="$emit('next')"
            :disabled="currentPage === totalPages"
            class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 dark:border-dark-border bg-white dark:bg-dark-card text-sm font-medium text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <span class="sr-only">Next</span>
            <i class="fas fa-chevron-right w-5 h-5 flex items-center justify-center"></i>
          </button>
        </nav>
      </div>
    </div>
  </div>
</template>
