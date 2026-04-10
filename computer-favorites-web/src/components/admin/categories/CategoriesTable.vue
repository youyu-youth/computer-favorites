<script setup lang="ts">
import { computed } from 'vue'
import DataTable, { type DataTableSortEvent } from 'primevue/datatable'
import Column from 'primevue/column'
import type { AdminCategoryItem, AdminCategorySortField, AdminCategorySortOrder } from '@/types/category'

const props = defineProps<{
  rows: AdminCategoryItem[]
  selectedRowIds: number[]
  sortField: AdminCategorySortField
  sortOrder: AdminCategorySortOrder
}>()

const emit = defineEmits<{
  (e: 'selection-change', selectedRowIds: number[]): void
  (e: 'sort-change', payload: { field: AdminCategorySortField; order: AdminCategorySortOrder }): void
  (e: 'edit', category: AdminCategoryItem): void
  (e: 'delete', category: AdminCategoryItem): void
}>()

const selectedRows = computed<AdminCategoryItem[]>(() => {
  return props.rows.filter((item) => props.selectedRowIds.includes(item.id))
})

const handleSelectionUpdate = (value: AdminCategoryItem[] | AdminCategoryItem | null | undefined): void => {
  const rows = Array.isArray(value) ? value : value ? [value] : []
  emit(
    'selection-change',
    rows.map((item) => item.id),
  )
}

const formatDateTime = (value: string): string => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '-'
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const handleSort = (event: DataTableSortEvent): void => {
  const field = (
    typeof event.sortField === 'string' ? event.sortField : 'updatedAt'
  ) as AdminCategorySortField
  const order: AdminCategorySortOrder = event.sortOrder === 1 ? 1 : -1
  emit('sort-change', { field, order })
}

// 表头内容对齐（unstyled 模式下 headerContent 无默认 flex）
const sortableColPt = {
  headerContent: { class: 'flex items-center gap-1.5 select-none' },
  sortIcon: { class: 'text-gray-400 dark:text-gray-500 text-xs' },
} as const

const normalColPt = {
  headerContent: { class: 'flex items-center' },
} as const

const selectionCheckboxPt = {
  pcHeaderCheckbox: {
    root: { class: 'relative inline-flex h-4 w-4 shrink-0 cursor-pointer items-center justify-center align-bottom' },
    input: { class: 'absolute inset-0 z-10 m-0 h-full w-full cursor-pointer opacity-0' },
    box: { class: 'flex h-4 w-4 items-center justify-center rounded border border-gray-300 bg-white transition-colors dark:border-dark-border dark:bg-dark-bg [&[data-p-checked=true]]:border-blue-600 [&[data-p-checked=true]]:bg-blue-600 [&[data-p-checked=true]]:dark:border-blue-500 [&[data-p-checked=true]]:dark:bg-blue-500' },
    icon: { class: 'h-2.5 w-2.5 text-white transition-opacity' },
  },
  pcRowCheckbox: {
    root: { class: 'relative inline-flex h-4 w-4 shrink-0 cursor-pointer items-center justify-center align-bottom' },
    input: { class: 'absolute inset-0 z-10 m-0 h-full w-full cursor-pointer opacity-0' },
    box: { class: 'flex h-4 w-4 items-center justify-center rounded border border-gray-300 bg-white transition-colors dark:border-dark-border dark:bg-dark-bg [&[data-p-checked=true]]:border-blue-600 [&[data-p-checked=true]]:bg-blue-600 [&[data-p-checked=true]]:dark:border-blue-500 [&[data-p-checked=true]]:dark:bg-blue-500' },
    icon: { class: 'h-2.5 w-2.5 text-white transition-opacity' },
  },
} as const
</script>

<template>
  <section class="border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card mt-4">
    <div class="overflow-x-auto">
      <DataTable
        :value="rows"
        :selection="selectedRows"
        dataKey="id"
        removableSort
        :sortField="sortField"
        :sortOrder="sortOrder"
        class="min-w-[980px]"
        :pt="{
          table: { class: 'w-full border-separate border-spacing-0' },
          thead: { class: 'bg-gray-50 dark:bg-dark-bg/60' },
          headerRow: { class: 'border-b border-gray-200 dark:border-dark-border' },
          bodyRow: {
            class:
              'border-b border-gray-100 transition-colors hover:bg-gray-50/70 dark:border-dark-border/70 dark:hover:bg-dark-bg/40',
          },
          emptyMessage: {
            class: 'px-4 py-10 text-center text-sm text-gray-500 dark:text-gray-400',
          },
        }"
        @update:selection="handleSelectionUpdate"
        @sort="handleSort"
      >
        <template #empty>
          <div class="py-8 text-center">
            <i class="fas fa-folder text-2xl text-gray-300 dark:text-gray-600"></i>
            <p class="mt-3 text-sm text-gray-500 dark:text-gray-400">未找到分类</p>
          </div>
        </template>

        <Column
          selectionMode="multiple"
          :pt="{ ...selectionCheckboxPt, ...normalColPt }"
          headerClass="px-3 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-3 py-3"
        />

        <Column
          field="id"
          header="ID"
          sortable
          :pt="sortableColPt"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3 text-sm text-gray-700 dark:text-gray-300"
        />

        <Column
          field="name"
          header="名称"
          sortable
          :pt="sortableColPt"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div
              class="flex items-center gap-2"
              :style="{ paddingLeft: `${Math.max(0, (data.depth || 0) * 20)}px` }"
            >
              <i v-if="data.icon" :class="[data.icon, 'text-gray-500']"></i>
              <i v-else class="fas fa-folder text-gray-400"></i>
              <span class="text-sm font-medium text-gray-800 dark:text-gray-200">{{ data.name }}</span>
            </div>
          </template>
        </Column>

        <Column
          field="description"
          header="描述"
          :pt="normalColPt"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <span class="text-sm text-gray-600 dark:text-gray-400 max-w-[200px] truncate block">
              {{ data.description || '-' }}
            </span>
          </template>
        </Column>

        <Column
          field="sort"
          header="排序"
          sortable
          :pt="sortableColPt"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <span class="text-sm text-gray-600 dark:text-gray-400">
              {{ data.sort }}
            </span>
          </template>
        </Column>

        <Column
          field="status"
          header="状态"
          :pt="normalColPt"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <span
              class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
              :class="[
                data.status === 'ACTIVE'
                  ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-300'
                  : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-400',
              ]"
            >
              {{ data.status === 'ACTIVE' ? '启用' : '禁用' }}
            </span>
          </template>
        </Column>

        <Column
          field="createdAt"
          header="创建时间"
          sortable
          :pt="sortableColPt"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3 text-sm text-gray-600 dark:text-gray-300"
        >
          <template #body="{ data }">
            {{ formatDateTime(data.createdAt) }}
          </template>
        </Column>

        <Column
          header="操作"
          :pt="{ headerContent: { class: 'flex items-center justify-end' } }"
          headerClass="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3 text-right whitespace-nowrap"
        >
          <template #body="{ data }">
            <button
              type="button"
              @click.stop="emit('edit', data)"
              class="mr-2 inline-flex h-8 items-center rounded bg-blue-50 px-2.5 text-xs font-medium text-blue-700 hover:bg-blue-100 dark:bg-blue-900/20 dark:text-blue-300 dark:hover:bg-blue-900/40"
              title="编辑"
            >
              <i class="fas fa-edit mr-1"></i> 编辑
            </button>
            <button
              type="button"
              @click.stop="emit('delete', data)"
              class="inline-flex h-8 items-center rounded bg-red-50 px-2.5 text-xs font-medium text-red-700 hover:bg-red-100 dark:bg-red-900/20 dark:text-red-300 dark:hover:bg-red-900/40"
              title="删除"
            >
              <i class="fas fa-trash-alt"></i>
            </button>
          </template>
        </Column>
      </DataTable>
    </div>
  </section>
</template>
