<script setup lang="ts">
type DeletedFilterValue = -1 | 0 | 1

type WebsiteStatsView = {
  total: number
  online: number
  offline: number
  pendingAudit: number
  rejectedAudit: number
  deleted: number
  latestUpdateTime: string
}

const props = defineProps<{
  viewMode: string
  deletedFilter: DeletedFilterValue
  stats: WebsiteStatsView
}>()

const emit = defineEmits<{
  (e: 'update:viewMode', val: string): void
  (e: 'update:deletedFilter', val: DeletedFilterValue): void
  (e: 'refresh'): void
}>()

const switchView = (mode: string) => {
  emit('update:viewMode', mode)
}

const handleDeletedFilterChange = (event: Event) => {
  const value = Number((event.target as HTMLSelectElement).value) as DeletedFilterValue
  emit('update:deletedFilter', value)
}
</script>

<template>
  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
    <div class="text-sm text-gray-500 dark:text-gray-400 flex items-center gap-2">
      <span>{{ stats.total }} websites，已上架 {{ stats.online }}，已删除 {{ stats.deleted }}。最近更新 {{ stats.latestUpdateTime }}</span>
      <span class="w-2 h-2 rounded-full bg-brand-green"></span>
    </div>

    <div class="flex flex-wrap items-center gap-3">
      <button @click="emit('refresh')" class="bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border text-gray-700 dark:text-gray-300 px-4 py-1.5 rounded-md text-sm font-medium hover:bg-gray-50 dark:hover:bg-dark-border transition-colors cursor-pointer">
        Refresh
      </button>

      <div class="relative">
        <select :value="deletedFilter" @change="handleDeletedFilterChange" class="appearance-none bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border text-gray-700 dark:text-gray-300 pl-4 pr-8 py-1.5 rounded-md text-sm font-medium focus:outline-none focus:ring-1 focus:ring-brand-orange cursor-pointer">
          <option :value="-1">全部数据</option>
          <option :value="0">仅未删除</option>
          <option :value="1">仅已删除</option>
        </select>
        <i class="fas fa-chevron-down absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-xs pointer-events-none"></i>
      </div>

      <!-- 视图切换按钮 -->
      <div class="flex items-center bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-md overflow-hidden">
        <button @click="switchView('grid')" :class="{'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white': props.viewMode === 'grid', 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-dark-border': props.viewMode !== 'grid'}" class="p-2 transition-colors cursor-pointer" title="网格视图">
          <i class="fas fa-th-large"></i>
        </button>
        <button @click="switchView('list')" :class="{'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white': props.viewMode === 'list', 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-dark-border': props.viewMode !== 'list'}" class="p-2 transition-colors cursor-pointer" title="列表视图">
          <i class="fas fa-list"></i>
        </button>
      </div>

      <button class="bg-gray-900 text-white dark:bg-[#1f2937] dark:hover:bg-gray-700 border dark:border-gray-600 px-4 py-1.5 rounded-md text-sm font-medium hover:bg-gray-800 transition-colors cursor-pointer">
        待审核 {{ stats.pendingAudit }}
      </button>
    </div>
  </div>
</template>


