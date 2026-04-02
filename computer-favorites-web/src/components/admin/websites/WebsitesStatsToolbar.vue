<script setup lang="ts">
import type { AdminWebsiteStatusValue } from '@/types/admin-website'

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
  selectedCount: number
  selectableTotal: number
  allSelectableSelected: boolean
  batchStatusUpdating: boolean
}>()

const emit = defineEmits<{
  (e: 'update:viewMode', val: string): void
  (e: 'update:deletedFilter', val: DeletedFilterValue): void
  (e: 'refresh'): void
  (e: 'toggle-select-all'): void
  (e: 'clear-selection'): void
  (e: 'batch-status', val: AdminWebsiteStatusValue): void
}>()

const switchView = (mode: string) => {
  emit('update:viewMode', mode)
}

const handleDeletedFilterChange = (event: Event) => {
  const value = Number((event.target as HTMLSelectElement).value) as DeletedFilterValue
  emit('update:deletedFilter', value)
}

const handleBatchStatus = (status: AdminWebsiteStatusValue) => {
  emit('batch-status', status)
}
</script>

<template>
  <div class="space-y-3 mb-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
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

        <button @click="emit('update:viewMode', 'add')" class="bg-brand-orange text-white px-4 py-1.5 rounded-md text-sm font-medium hover:bg-orange-600 transition-colors cursor-pointer flex items-center gap-1.5 shadow-sm">
          <i class="fas fa-plus text-xs"></i>
          <span>添加网站</span>
        </button>
      </div>
    </div>

    <div
      v-if="selectedCount > 0"
      class="rounded-lg border border-orange-200 bg-orange-50 px-4 py-3 dark:border-orange-900/40 dark:bg-orange-900/10"
    >
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div class="text-sm text-orange-800 dark:text-orange-200">
          已选择 {{ selectedCount }} 个网站
          <span class="text-xs text-orange-700/80 dark:text-orange-300/80">（当前页可选 {{ selectableTotal }} 个）</span>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <button
            type="button"
            @click="emit('toggle-select-all')"
            class="cursor-pointer rounded-md border border-orange-300 bg-white px-3 py-1.5 text-xs font-medium text-orange-700 transition-colors hover:bg-orange-100 dark:border-orange-700 dark:bg-transparent dark:text-orange-200 dark:hover:bg-orange-900/30"
          >
            {{ allSelectableSelected ? '取消全选' : '全选当前页' }}
          </button>
          <button
            type="button"
            @click="emit('clear-selection')"
            class="cursor-pointer rounded-md border border-gray-300 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-border"
          >
            清空选择
          </button>

          <button
            type="button"
            :disabled="batchStatusUpdating"
            @click="handleBatchStatus(1)"
            class="cursor-pointer rounded-md border border-emerald-300 bg-emerald-50 px-3 py-1.5 text-xs font-medium text-emerald-700 transition-colors hover:bg-emerald-100 disabled:cursor-not-allowed disabled:opacity-60 dark:border-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-200 dark:hover:bg-emerald-900/35"
          >
            批量上架
          </button>

          <button
            type="button"
            :disabled="batchStatusUpdating"
            @click="handleBatchStatus(0)"
            class="cursor-pointer rounded-md border border-amber-300 bg-amber-50 px-3 py-1.5 text-xs font-medium text-[#2d241f] transition-colors hover:bg-amber-100 disabled:cursor-not-allowed disabled:opacity-60 dark:border-amber-700 dark:bg-amber-900/20 dark:text-[#e95322] dark:hover:bg-amber-900/35"
          >
            批量下架
          </button>
        </div>
      </div>
    </div>
  </div>
</template>


