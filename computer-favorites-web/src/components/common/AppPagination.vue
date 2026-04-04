<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  currentPage: number
  totalPages: number
}>()

const emit = defineEmits<{
  'update:currentPage': [page: number]
}>()

const safeTotalPages = computed(() => {
  return Math.max(1, Number(props.totalPages || 0))
})

const safeCurrentPage = computed(() => {
  const normalizedCurrentPage = Number(props.currentPage || 1)
  return Math.min(Math.max(1, normalizedCurrentPage), safeTotalPages.value)
})

const visiblePageNumbers = computed(() => {
  const pages: number[] = []
  const maxVisiblePages = 5
  if (safeTotalPages.value <= maxVisiblePages) {
    for (let i = 1; i <= safeTotalPages.value; i++) {
      pages.push(i)
    }
  } else {
    let start = Math.max(1, safeCurrentPage.value - Math.floor(maxVisiblePages / 2))
    let end = start + maxVisiblePages - 1
    if (end > safeTotalPages.value) {
      end = safeTotalPages.value
      start = Math.max(1, end - maxVisiblePages + 1)
    }
    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
  }
  return pages
})

const changePage = (page: number) => {
  if (page >= 1 && page <= safeTotalPages.value && page !== safeCurrentPage.value) {
    emit('update:currentPage', page)
  }
}
</script>

<template>
  <div class="flex justify-center items-center mt-6 font-mono text-sm py-4">
    <div class="flex items-center -space-x-px">
      <!-- 跳转第一页 -->
      <button
        @click="changePage(1)"
        :disabled="safeCurrentPage === 1"
        class="relative z-0 hover:z-10 px-2 sm:px-3 py-1.5 min-w-[32px] sm:min-w-[36px] flex items-center justify-center border border-gray-200 dark:border-[#1f1f1f] bg-white dark:bg-[#0a0a0a] text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-white/5 hover:text-primary-500 dark:hover:text-primary-400 disabled:opacity-50 disabled:bg-gray-100 dark:disabled:bg-white/5 disabled:text-gray-400 dark:disabled:text-gray-600 disabled:cursor-not-allowed transition-colors rounded-l-md"
        title="第一页"
      >
        &laquo;
      </button>

      <!-- 上一页 -->
      <button
        @click="changePage(safeCurrentPage - 1)"
        :disabled="safeCurrentPage === 1"
        class="relative z-0 hover:z-10 px-2 sm:px-3 py-1.5 min-w-[32px] sm:min-w-[36px] flex items-center justify-center border border-gray-200 dark:border-[#1f1f1f] bg-white dark:bg-[#0a0a0a] text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-white/5 hover:text-primary-500 dark:hover:text-primary-400 disabled:opacity-50 disabled:bg-gray-100 dark:disabled:bg-white/5 disabled:text-gray-400 dark:disabled:text-gray-600 disabled:cursor-not-allowed transition-colors"
        title="上一页"
      >
        &lsaquo;
      </button>

      <!-- 桌面端页码 -->
      <div class="hidden sm:flex -space-x-px">
        <button
          v-for="page in visiblePageNumbers"
          :key="page"
          @click="changePage(page)"
          class="px-3 py-1.5 min-w-[36px] flex items-center justify-center border border-gray-200 dark:border-[#1f1f1f] transition-colors"
          :class="
            safeCurrentPage === page
              ? 'bg-primary-50 text-primary-600 border-primary-200 dark:bg-primary-500/20 dark:text-primary-400 dark:border-primary-500/30 font-bold z-10 relative'
              : 'bg-white dark:bg-[#0a0a0a] text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-white/5 hover:text-primary-500 dark:hover:text-primary-400 relative z-0 hover:z-10'
          "
        >
          {{ page }}
        </button>
      </div>

      <!-- 移动端页码 -->
      <div
        class="sm:hidden relative z-0 flex items-center justify-center px-4 py-1.5 border border-gray-200 dark:border-[#1f1f1f] bg-white dark:bg-[#0a0a0a] text-gray-600 dark:text-gray-400 font-bold min-w-[80px]"
      >
        {{ safeCurrentPage
        }}<span class="font-normal text-xs text-gray-400 dark:text-gray-500 mx-1">/</span
        >{{ safeTotalPages }}
      </div>

      <!-- 下一页 -->
      <button
        @click="changePage(safeCurrentPage + 1)"
        :disabled="safeCurrentPage === safeTotalPages"
        class="relative z-0 hover:z-10 px-2 sm:px-3 py-1.5 min-w-[32px] sm:min-w-[36px] flex items-center justify-center border border-gray-200 dark:border-[#1f1f1f] bg-white dark:bg-[#0a0a0a] text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-white/5 hover:text-primary-500 dark:hover:text-primary-400 disabled:opacity-50 disabled:bg-gray-100 dark:disabled:bg-white/5 disabled:text-gray-400 dark:disabled:text-gray-600 disabled:cursor-not-allowed transition-colors"
        title="下一页"
      >
        &rsaquo;
      </button>

      <!-- 跳转最后一页 -->
      <button
        @click="changePage(safeTotalPages)"
        :disabled="safeCurrentPage === safeTotalPages"
        class="relative z-0 hover:z-10 px-2 sm:px-3 py-1.5 min-w-[32px] sm:min-w-[36px] flex items-center justify-center border border-gray-200 dark:border-[#1f1f1f] bg-white dark:bg-[#0a0a0a] text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-white/5 hover:text-primary-500 dark:hover:text-primary-400 disabled:opacity-50 disabled:bg-gray-100 dark:disabled:bg-white/5 disabled:text-gray-400 dark:disabled:text-gray-600 disabled:cursor-not-allowed transition-colors rounded-r-md"
        title="最后一页"
      >
        &raquo;
      </button>
    </div>
  </div>
</template>
