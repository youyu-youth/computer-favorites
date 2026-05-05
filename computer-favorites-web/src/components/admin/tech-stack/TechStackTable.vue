<script setup lang="ts">
import { computed, ref } from 'vue'
import DataTable, { type DataTableSortEvent } from 'primevue/datatable'
import Column from 'primevue/column'
import type {
  AdminTechStackItem,
  AdminTechStackSortField,
  AdminTechStackSortOrder,
} from '@/types/tech-stack'

const props = defineProps<{
  rows: AdminTechStackItem[]
  selectedRowIds: number[]
  sortField: AdminTechStackSortField
  sortOrder: AdminTechStackSortOrder
}>()

const emit = defineEmits<{
  (e: 'selection-change', selectedRowIds: number[]): void
  (e: 'sort-change', payload: { field: AdminTechStackSortField; order: AdminTechStackSortOrder }): void
  (e: 'edit', item: AdminTechStackItem): void
  (e: 'delete', item: AdminTechStackItem): void
  (e: 'toggle-status', item: AdminTechStackItem): void
}>()

const iconLoadErrors = ref<Set<number>>(new Set())

const selectedRows = computed<AdminTechStackItem[]>(() => {
  return props.rows.filter((item) => props.selectedRowIds.includes(item.id))
})

const handleSelectionUpdate = (value: AdminTechStackItem[] | AdminTechStackItem | null | undefined): void => {
  const rows = Array.isArray(value) ? value : value ? [value] : []
  emit(
    'selection-change',
    rows.map((item) => item.id),
  )
}

const handleSort = (event: DataTableSortEvent): void => {
  const field = (
    typeof event.sortField === 'string' ? event.sortField : 'sort'
  ) as AdminTechStackSortField
  const order: AdminTechStackSortOrder = event.sortOrder === 1 ? 1 : -1
  emit('sort-change', { field, order })
}

const onIconError = (id: number) => {
  iconLoadErrors.value = new Set(iconLoadErrors.value).add(id)
}

const isIconLoaded = (id: number): boolean => {
  return !iconLoadErrors.value.has(id)
}

const getFirstLetter = (name: string): string => {
  return name.charAt(0).toUpperCase()
}

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
    box: { class: 'flex h-4 w-4 items-center justify-center rounded border border-gray-300 bg-white transition-colors dark:border-dark-border dark:bg-dark-bg [&[data-p-checked=true]]:border-[#f55911] [&[data-p-checked=true]]:bg-[#f55911] [&[data-p-checked=true]]:dark:border-[#f78166] [&[data-p-checked=true]]:dark:bg-[#f78166]' },
    icon: { class: 'h-2.5 w-2.5 text-white transition-opacity' },
  },
  pcRowCheckbox: {
    root: { class: 'relative inline-flex h-4 w-4 shrink-0 cursor-pointer items-center justify-center align-bottom' },
    input: { class: 'absolute inset-0 z-10 m-0 h-full w-full cursor-pointer opacity-0' },
    box: { class: 'flex h-4 w-4 items-center justify-center rounded border border-gray-300 bg-white transition-colors dark:border-dark-border dark:bg-dark-bg [&[data-p-checked=true]]:border-[#f55911] [&[data-p-checked=true]]:bg-[#f55911] [&[data-p-checked=true]]:dark:border-[#f78166] [&[data-p-checked=true]]:dark:bg-[#f78166]' },
    icon: { class: 'h-2.5 w-2.5 text-white transition-opacity' },
  },
} as const
</script>

<template>
  <section class="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card">
    <div class="overflow-x-auto">
      <DataTable
        :value="rows"
        :selection="selectedRows"
        dataKey="id"
        removableSort
        :sortField="sortField"
        :sortOrder="sortOrder"
        class="min-w-[1060px]"
        :pt="{
          table: { class: 'w-full border-separate border-spacing-0' },
          thead: { class: 'bg-gray-50/80 dark:bg-dark-bg/50' },
          headerRow: { class: 'border-b border-gray-200 dark:border-dark-border' },
          bodyRow: {
            class:
              'border-b border-gray-100/80 transition-all duration-150 hover:bg-[#f55911]/[0.03] dark:border-dark-border/60 dark:hover:bg-[#f55911]/[0.04]',
          },
          emptyMessage: {
            class: 'px-4 py-12 text-center text-sm text-gray-500 dark:text-gray-400',
          },
        }"
        @update:selection="handleSelectionUpdate"
        @sort="handleSort"
      >
        <template #empty>
          <div class="py-12 text-center">
            <div class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gray-100 dark:bg-gray-800">
              <i class="fas fa-layer-group text-2xl text-gray-300 dark:text-gray-600"></i>
            </div>
            <p class="text-sm font-medium text-gray-500 dark:text-gray-400">未找到技术栈</p>
            <p class="mt-1 text-xs text-gray-400 dark:text-gray-500">尝试调整搜索条件或新增技术栈</p>
          </div>
        </template>

        <Column
          selectionMode="multiple"
          :pt="{ ...selectionCheckboxPt, ...normalColPt }"
          headerClass="px-4 py-3.5 text-[11px] font-semibold uppercase tracking-widest text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3.5"
        />

        <Column
          field="name"
          header="技术栈"
          sortable
          :pt="sortableColPt"
          headerClass="px-4 py-3.5 text-[11px] font-semibold uppercase tracking-widest text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3.5"
        >
          <template #body="{ data }">
            <div class="flex items-center gap-3">
              <div class="relative">
                <div
                  v-if="data.color"
                  class="absolute -inset-1 rounded-xl opacity-20 blur-sm"
                  :style="{ backgroundColor: data.color }"
                ></div>
                <div
                  v-if="data.iconPng && isIconLoaded(data.id)"
                  class="relative h-10 w-10 shrink-0 overflow-hidden rounded-xl border border-gray-100 bg-white p-1.5 dark:border-dark-border dark:bg-dark-bg"
                >
                  <img
                    :src="data.iconPng"
                    :alt="data.name"
                    class="h-full w-full object-contain"
                    @error="onIconError(data.id)"
                  />
                </div>
                <div
                  v-else
                  class="relative flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-sm font-bold text-white"
                  :style="{ backgroundColor: data.color || '#6B7280' }"
                >
                  {{ getFirstLetter(data.name) }}
                </div>
              </div>
              <div class="min-w-0">
                <span class="block text-sm font-semibold text-gray-800 dark:text-gray-200 truncate">
                  {{ data.name }}
                </span>
                <span
                  v-if="data.officialUrl"
                  class="block text-[11px] text-gray-400 dark:text-gray-500 truncate max-w-[160px]"
                >
                  {{ data.officialUrl.replace(/^https?:\/\//, '').replace(/\/$/, '') }}
                </span>
              </div>
            </div>
          </template>
        </Column>

        <Column
          field="description"
          header="描述"
          :pt="normalColPt"
          headerClass="px-4 py-3.5 text-[11px] font-semibold uppercase tracking-widest text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3.5"
        >
          <template #body="{ data }">
            <span class="text-sm text-gray-600 dark:text-gray-400 max-w-[200px] truncate block" :title="data.description">
              {{ data.description || '-' }}
            </span>
          </template>
        </Column>

        <Column
          field="color"
          header="主题色"
          :pt="normalColPt"
          headerClass="px-4 py-3.5 text-[11px] font-semibold uppercase tracking-widest text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3.5"
        >
          <template #body="{ data }">
            <div v-if="data.color" class="flex items-center gap-2.5">
              <span
                class="inline-block h-6 w-6 rounded-lg border border-gray-200/80 shadow-sm dark:border-dark-border"
                :style="{ backgroundColor: data.color }"
              ></span>
              <span class="text-xs font-mono text-gray-500 dark:text-gray-400 tracking-wide">{{ data.color }}</span>
            </div>
            <span v-else class="text-sm text-gray-400 dark:text-gray-500">-</span>
          </template>
        </Column>

        <Column
          field="userCount"
          header="用户"
          :pt="normalColPt"
          headerClass="px-4 py-3.5 text-[11px] font-semibold uppercase tracking-widest text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3.5"
        >
          <template #body="{ data }">
            <span
              class="inline-flex items-center gap-1.5 rounded-md px-2 py-1 text-xs font-medium"
              :class="data.userCount > 50
                ? 'bg-[#f55911]/8 text-[#f55911] dark:bg-[#f55911]/10 dark:text-[#f78166]'
                : 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400'"
            >
              <i class="fas fa-users text-[9px]"></i>
              {{ data.userCount }}
            </span>
          </template>
        </Column>

        <Column
          field="sort"
          header="排序"
          sortable
          :pt="sortableColPt"
          headerClass="px-4 py-3.5 text-[11px] font-semibold uppercase tracking-widest text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3.5"
        >
          <template #body="{ data }">
            <span class="inline-flex h-6 min-w-[28px] items-center justify-center rounded-md bg-gray-100 px-1.5 text-xs font-mono text-gray-600 dark:bg-gray-800 dark:text-gray-400">
              {{ data.sort }}
            </span>
          </template>
        </Column>

        <Column
          field="status"
          header="状态"
          :pt="normalColPt"
          headerClass="px-4 py-3.5 text-[11px] font-semibold uppercase tracking-widest text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3.5"
        >
          <template #body="{ data }">
            <button
              type="button"
              class="group relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-[#f55911]/30 focus:ring-offset-2 dark:focus:ring-offset-gray-900"
              :class="data.status === 'ACTIVE' ? 'bg-emerald-500' : 'bg-gray-300 dark:bg-gray-600'"
              :aria-pressed="data.status === 'ACTIVE'"
              :aria-label="data.status === 'ACTIVE' ? '点击禁用' : '点击启用'"
              @click="emit('toggle-status', data)"
            >
              <span
                class="pointer-events-none inline-block h-5 w-5 rounded-full bg-white shadow-sm transition-transform duration-200 ease-in-out"
                :class="data.status === 'ACTIVE' ? 'translate-x-5' : 'translate-x-0.5'"
              ></span>
            </button>
          </template>
        </Column>

        <Column
          header="操作"
          :pt="{ headerContent: { class: 'flex items-center justify-end' } }"
          headerClass="px-4 py-3.5 text-right text-[11px] font-semibold uppercase tracking-widest text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3.5 text-right whitespace-nowrap"
        >
          <template #body="{ data }">
            <div class="flex items-center justify-end gap-1">
              <button
                type="button"
                @click.stop="emit('edit', data)"
                class="inline-flex h-8 w-8 items-center justify-center rounded-lg text-gray-400 transition-all hover:bg-[#f55911]/8 hover:text-[#f55911] cursor-pointer dark:hover:bg-[#f55911]/10 dark:hover:text-[#f78166]"
                title="编辑"
              >
                <i class="fas fa-pen text-xs"></i>
              </button>
              <button
                type="button"
                @click.stop="emit('delete', data)"
                class="inline-flex h-8 w-8 items-center justify-center rounded-lg text-gray-400 transition-all hover:bg-red-50 hover:text-red-500 cursor-pointer dark:hover:bg-red-900/15 dark:hover:text-red-400"
                title="删除"
              >
                <i class="fas fa-trash-alt text-xs"></i>
              </button>
            </div>
          </template>
        </Column>
      </DataTable>
    </div>
  </section>
</template>
