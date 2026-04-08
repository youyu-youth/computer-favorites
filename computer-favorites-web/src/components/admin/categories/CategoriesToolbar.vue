<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  searchKeyword: string
  selectedCount: number
}>()

const emit = defineEmits<{
  (e: 'update:searchKeyword', keyword: string): void
  (e: 'refresh'): void
  (e: 'create'): void
}>()

const localKeyword = ref(props.searchKeyword)

watch(
  () => props.searchKeyword,
  (val) => {
    localKeyword.value = val
  }
)

let searchTimer: number | null = null
const onSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => {
    emit('update:searchKeyword', localKeyword.value)
  }, 300)
}
</script>

<template>
  <div class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
    <div class="flex items-center gap-2">
      <h2 class="text-lg font-semibold text-gray-900 dark:text-white">全部分类</h2>
      <span
        v-if="selectedCount > 0"
        class="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-semibold text-blue-700 dark:bg-blue-900/20 dark:text-blue-200"
      >
        已选 {{ selectedCount }} 项
      </span>
    </div>

    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:gap-2">
      <div class="relative w-full max-w-[320px] sm:w-64">
        <div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
          <i class="fas fa-search text-gray-400"></i>
        </div>
        <input
          type="text"
          v-model="localKeyword"
          @input="onSearchInput"
          class="block w-full rounded-md border-gray-300 pl-10 pr-3 focus:border-blue-500 focus:ring-blue-500 sm:text-sm dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder-gray-500"
          placeholder="搜索分类名称..."
          aria-label="搜索分类名称"
        />
        <button
          v-if="localKeyword"
          type="button"
          @click="
            () => {
              localKeyword = ''
              onSearchInput()
            }
          "
          class="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
        >
          <i class="fas fa-times-circle"></i>
        </button>
      </div>

      <div class="flex items-center justify-between sm:justify-end gap-2 shrink-0">
        <button
          type="button"
          @click="emit('refresh')"
          class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-md border border-gray-300 bg-white text-gray-500 shadow-sm hover:bg-gray-50 hover:text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 dark:border-dark-border dark:bg-dark-bg dark:text-gray-400 dark:hover:bg-dark-card dark:hover:text-gray-300"
          title="刷新数据"
        >
          <i class="fas fa-sync-alt text-sm"></i>
        </button>

        <button
          type="button"
          @click="emit('create')"
          class="inline-flex h-9 shrink-0 items-center justify-center rounded-md border border-transparent bg-blue-600 px-3.5 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 dark:bg-blue-500 dark:hover:bg-blue-600 dark:focus:ring-offset-gray-900"
        >
          <i class="fas fa-plus mr-1.5 opacity-80"></i>
          新建分类
        </button>
      </div>
    </div>
  </div>
</template>
