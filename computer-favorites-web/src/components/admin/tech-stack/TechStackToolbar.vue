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
  (e: 'batchDelete'): void
}>()

const localKeyword = ref(props.searchKeyword)

watch(
  () => props.searchKeyword,
  (val) => {
    localKeyword.value = val
  },
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
  <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
    <div class="flex items-center gap-2.5">
      <h2 class="text-base font-semibold text-gray-800 dark:text-gray-200">
        技术栈列表
      </h2>
      <span
        v-if="selectedCount > 0"
        class="inline-flex items-center rounded-md bg-[#f55911]/10 px-2 py-0.5 text-xs font-semibold text-[#f55911] dark:bg-[#f55911]/15 dark:text-[#f78166]"
      >
        已选 {{ selectedCount }} 项
      </span>
    </div>

    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:gap-2">
      <div class="relative w-full max-w-[320px] sm:w-72">
        <div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5">
          <i class="fas fa-search text-gray-400 text-sm"></i>
        </div>
        <input
          type="text"
          v-model="localKeyword"
          @input="onSearchInput"
          class="block h-9 w-full rounded-lg border border-gray-300 bg-white pl-10 pr-9 text-sm text-gray-700 outline-none transition-all placeholder:text-gray-400 focus:border-[#f55911]/50 focus:ring-2 focus:ring-[#f55911]/15 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-[#f78166]/50 dark:focus:ring-[#f78166]/10"
          placeholder="搜索名称或描述..."
          aria-label="搜索技术栈"
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
          class="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 transition-colors hover:text-gray-600 dark:hover:text-gray-300"
        >
          <i class="fas fa-times-circle text-sm"></i>
        </button>
      </div>

      <div class="flex items-center justify-between sm:justify-end gap-2 shrink-0">
        <button
          v-if="selectedCount > 0"
          type="button"
          @click="emit('batchDelete')"
          class="inline-flex h-9 shrink-0 items-center justify-center rounded-lg border border-red-300 bg-white px-3.5 text-sm font-medium text-red-600 shadow-sm transition-all hover:bg-red-50 hover:border-red-400 focus:outline-none focus:ring-2 focus:ring-red-500/30 focus:ring-offset-2 dark:border-red-900/50 dark:bg-dark-bg dark:text-red-400 dark:hover:bg-red-900/20 dark:focus:ring-offset-gray-900"
        >
          <i class="fas fa-trash-alt mr-1.5 text-xs"></i>
          批量删除 ({{ selectedCount }})
        </button>

        <button
          type="button"
          @click="emit('refresh')"
          class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-gray-300 bg-white text-gray-500 shadow-sm transition-all hover:bg-gray-50 hover:text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#f55911]/30 focus:ring-offset-2 dark:border-dark-border dark:bg-dark-bg dark:text-gray-400 dark:hover:bg-dark-card dark:hover:text-gray-300 dark:focus:ring-offset-gray-900"
          title="刷新数据"
        >
          <i class="fas fa-sync-alt text-sm"></i>
        </button>

        <button
          type="button"
          @click="emit('create')"
          class="inline-flex h-9 shrink-0 items-center justify-center rounded-lg border border-transparent bg-[#f55911] px-3.5 text-sm font-semibold text-white shadow-sm transition-all hover:bg-[#e04e0a] hover:shadow focus:outline-none focus:ring-2 focus:ring-[#f55911]/40 focus:ring-offset-2 active:scale-[0.98] dark:focus:ring-offset-gray-900 sm:hidden"
        >
          <i class="fas fa-plus mr-1.5 text-xs"></i>
          新建
        </button>
      </div>
    </div>
  </div>
</template>
