<script setup lang="ts">
const props = defineProps<{
  currentPage: number
  totalPages: number
  visiblePages: Array<number | string>
  total: number
  pageSize: number
}>()

defineEmits<{
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
  <div class="mt-8 flex items-center justify-between border-t border-gray-200 pt-6 dark:border-dark-border">
    <div class="flex w-full items-center justify-between sm:hidden">
      <button
        class="relative inline-flex cursor-pointer items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-gray-800"
        :disabled="currentPage === 1"
        @click="$emit('prev')"
      >
        上一页
      </button>
      <p class="text-sm text-gray-500 dark:text-gray-400">
        第 <span class="font-medium text-gray-900 dark:text-white">{{ currentPage }}</span> / <span class="font-medium text-gray-900 dark:text-white">{{ totalPages }}</span> 页
      </p>
      <button
        class="relative ml-3 inline-flex cursor-pointer items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-gray-800"
        :disabled="currentPage === totalPages"
        @click="$emit('next')"
      >
        下一页
      </button>
    </div>

    <div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
      <div>
        <p class="text-sm text-gray-700 dark:text-gray-400">
          显示
          <span class="font-medium text-gray-900 dark:text-white">{{ resolveStart() }}</span>
          到
          <span class="font-medium text-gray-900 dark:text-white">{{ resolveEnd() }}</span>
          ，共
          <span class="font-medium text-gray-900 dark:text-white">{{ total }}</span>
          条
        </p>
      </div>
      <div>
        <nav class="relative z-0 -space-x-px rounded-md shadow-sm" aria-label="Pagination">
          <button
            class="relative inline-flex items-center rounded-l-md border border-gray-300 bg-white px-2 py-2 text-sm font-medium text-gray-500 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-400 dark:hover:bg-gray-800"
            :disabled="currentPage === 1"
            @click="$emit('prev')"
          >
            <span class="sr-only">Previous</span>
            <i class="fas fa-chevron-left flex h-5 w-5 items-center justify-center"></i>
          </button>

          <template v-for="(page, index) in visiblePages" :key="page === '...' ? `ellipsis-${index}` : `page-${page}`">
            <span
              v-if="page === '...'"
              class="relative inline-flex items-center border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 dark:border-dark-border dark:bg-dark-card dark:text-gray-400"
            >
              ...
            </span>
            <button
              v-else
              class="relative inline-flex cursor-pointer items-center border px-4 py-2 text-sm transition-colors"
              :class="[
                page === currentPage
                  ? 'z-10 border-gray-300 bg-gray-100 font-bold text-brand-orange dark:border-dark-border dark:bg-gray-800 dark:text-brand-orange'
                  : 'border-gray-300 bg-white font-medium text-gray-500 hover:bg-gray-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-400 dark:hover:bg-gray-800',
              ]"
              @click="$emit('goto', page)"
            >
              {{ page }}
            </button>
          </template>

          <button
            class="relative inline-flex items-center rounded-r-md border border-gray-300 bg-white px-2 py-2 text-sm font-medium text-gray-500 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-400 dark:hover:bg-gray-800"
            :disabled="currentPage === totalPages"
            @click="$emit('next')"
          >
            <span class="sr-only">Next</span>
            <i class="fas fa-chevron-right flex h-5 w-5 items-center justify-center"></i>
          </button>
        </nav>
      </div>
    </div>
  </div>
</template>
