<script setup lang="ts">
import { computed } from 'vue'
import DataTable, { type DataTableSortEvent } from 'primevue/datatable'
import Column from 'primevue/column'
import type { AdminTagItem, AdminTagSortField, AdminTagSortOrder } from '@/types/admin-tag'

const props = defineProps<{
  rows: AdminTagItem[]
  selectedRowIds: number[]
  sortField: AdminTagSortField
  sortOrder: AdminTagSortOrder
}>()

const emit = defineEmits<{
  (e: 'selection-change', selectedRowIds: number[]): void
  (e: 'sort-change', payload: { field: AdminTagSortField; order: AdminTagSortOrder }): void
  (e: 'edit', tag: AdminTagItem): void
  (e: 'delete', tag: AdminTagItem): void
}>()

const selectedRows = computed<AdminTagItem[]>(() => {
  return props.rows.filter((item) => props.selectedRowIds.includes(item.id))
})

const handleSelectionUpdate = (value: AdminTagItem[] | AdminTagItem | null | undefined): void => {
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

const resolveUsageTone = (useCount: number): string => {
  if (useCount <= 0) {
    return 'text-amber-600 dark:text-amber-300'
  }
  if (useCount >= 20) {
    return 'text-emerald-600 dark:text-emerald-300'
  }
  return 'text-sky-600 dark:text-sky-300'
}

const handleSort = (event: DataTableSortEvent): void => {
  const field = (
    typeof event.sortField === 'string' ? event.sortField : 'updateTime'
  ) as AdminTagSortField
  const order: AdminTagSortOrder = event.sortOrder === 1 ? 1 : -1
  emit('sort-change', { field, order })
}

const selectionCheckboxPt = {
  pcHeaderCheckbox: {
    root: { class: 'cf-tag-table-checkbox-root' },
    input: { class: 'cf-tag-table-checkbox-input' },
    box: { class: 'cf-tag-table-checkbox-box' },
    icon: { class: 'cf-tag-table-checkbox-icon' },
  },
  pcRowCheckbox: {
    root: { class: 'cf-tag-table-checkbox-root' },
    input: { class: 'cf-tag-table-checkbox-input' },
    box: { class: 'cf-tag-table-checkbox-box' },
    icon: { class: 'cf-tag-table-checkbox-icon' },
  },
} as const
</script>

<template>
  <section class="border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card">
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
            <i class="fas fa-tags text-2xl text-gray-300 dark:text-gray-600"></i>
            <p class="mt-3 text-sm text-gray-500 dark:text-gray-400">未找到符合条件的标签</p>
          </div>
        </template>

        <Column
          selectionMode="multiple"
          :pt="selectionCheckboxPt"
          headerClass="px-3 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-3 py-3"
        />

        <Column
          field="id"
          header="ID"
          sortable
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3 text-sm text-gray-700 dark:text-gray-300"
        />

        <Column
          field="name"
          header="标签名称"
          sortable
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div class="flex items-center gap-2">
              <span
                class="h-3 w-3 rounded-none border border-black/10 dark:border-white/20"
                :style="{ backgroundColor: data.color }"
              ></span>
              <span class="text-sm font-medium text-gray-800 dark:text-gray-200">{{
                data.name
              }}</span>
            </div>
          </template>
        </Column>

        <Column
          field="color"
          header="颜色"
          sortable
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div
              class="inline-flex items-center gap-2 rounded-none border border-gray-200 px-2 py-1 text-xs font-medium text-gray-600 dark:border-dark-border dark:text-gray-300"
            >
              <span
                class="h-3 w-3 rounded-none border border-black/10 dark:border-white/20"
                :style="{ backgroundColor: data.color }"
              ></span>
              {{ data.color }}
            </div>
          </template>
        </Column>

        <Column
          field="useCount"
          header="使用次数"
          sortable
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <span class="text-sm font-semibold" :class="resolveUsageTone(data.useCount)">
              {{ data.useCount }}
            </span>
          </template>
        </Column>

        <Column
          field="createTime"
          header="创建时间"
          sortable
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3 text-sm text-gray-600 dark:text-gray-300"
        >
          <template #body="{ data }">
            {{ formatDateTime(data.createTime) }}
          </template>
        </Column>

        <Column
          field="updateTime"
          header="更新时间"
          sortable
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3 text-sm text-gray-600 dark:text-gray-300"
        >
          <template #body="{ data }">
            {{ formatDateTime(data.updateTime) }}
          </template>
        </Column>

        <Column
          header="操作"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div class="flex items-center gap-2">
              <UButton
                class="rounded-none"
                color="neutral"
                variant="soft"
                size="sm"
                @click="emit('edit', data)"
              >
                编辑
              </UButton>
              <UButton
                class="rounded-none"
                color="red"
                variant="soft"
                size="sm"
                @click="emit('delete', data)"
              >
                删除
              </UButton>
            </div>
          </template>
        </Column>
      </DataTable>
    </div>
  </section>
</template>

<style scoped>
:deep(.p-datatable-thead > tr > th) {
  vertical-align: middle;
  text-align: center;
}

:deep([data-pc-section='columnheadercontent']),
:deep(.p-column-header-content),
:deep(.p-datatable-column-header-content) {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: center;
  gap: 0.375rem;
  white-space: nowrap;
}

:deep([data-pc-section='sort']),
:deep(.p-sortable-column-icon),
:deep(.p-datatable-sort-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  margin: 0;
  line-height: 1;
  vertical-align: middle;
}

:deep([data-pc-section='sorticon']) {
  display: block;
}

:deep(.cf-tag-table-checkbox-root) {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1rem;
  height: 1rem;
  vertical-align: middle;
}

:deep(.cf-tag-table-checkbox-input) {
  position: absolute;
  inset: 0;
  margin: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

:deep(.cf-tag-table-checkbox-box) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1rem;
  height: 1rem;
  border: 1px solid rgb(148 163 184 / 0.85);
  background-color: transparent;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease;
}

:deep(.cf-tag-table-checkbox-root[data-p-checked='true'] .cf-tag-table-checkbox-box) {
  border-color: rgb(14 165 233 / 1);
  background-color: rgb(14 165 233 / 1);
}

:deep(.cf-tag-table-checkbox-icon) {
  width: 0.75rem;
  height: 0.75rem;
  color: #ffffff;
}

:deep(html.dark .cf-tag-table-checkbox-box) {
  border-color: rgb(100 116 139 / 0.95);
}

:deep(html.dark .cf-tag-table-checkbox-root[data-p-checked='true'] .cf-tag-table-checkbox-box) {
  border-color: rgb(2 132 199 / 1);
  background-color: rgb(2 132 199 / 1);
}
</style>
