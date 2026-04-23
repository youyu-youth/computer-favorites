<script setup lang="ts">
import { computed } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import type { AdminAnnouncementViewItem } from '@/types/admin-announcement'

const props = defineProps<{
  rows: AdminAnnouncementViewItem[]
  selectedIds: number[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'selection-change', value: number[]): void
  (e: 'view', id: number): void
  (e: 'edit', id: number): void
  (e: 'toggle-status', id: number): void
  (e: 'toggle-top', id: number): void
}>()

const selectedRows = computed<AdminAnnouncementViewItem[]>(() => {
  return props.rows.filter((item) => props.selectedIds.includes(item.id))
})

const selectedSet = computed(() => new Set(props.selectedIds))

const handleSelectionUpdate = (value: AdminAnnouncementViewItem[] | AdminAnnouncementViewItem | null | undefined): void => {
  const rows = Array.isArray(value) ? value : value ? [value] : []
  emit('selection-change', rows.map((item) => item.id))
}

const toggleOne = (announcementId: number, checked: boolean) => {
  const nextIds = checked
    ? [...props.selectedIds, announcementId]
    : props.selectedIds.filter((id) => id !== announcementId)
  emit('selection-change', [...new Set(nextIds)])
}

const handleToggleOne = (announcementId: number, event: Event) => {
  const target = event.target as HTMLInputElement
  toggleOne(announcementId, target.checked)
}

const formatDate = (value: string | null): string => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const selectionCheckboxPt = {
  pcHeaderCheckbox: {
    root: { class: 'relative inline-flex h-4 w-4 shrink-0 cursor-pointer items-center justify-center align-bottom' },
    input: { class: 'absolute inset-0 z-10 m-0 h-full w-full cursor-pointer opacity-0' },
    box: { class: 'flex h-4 w-4 items-center justify-center rounded border border-gray-300 bg-white transition-colors dark:border-dark-border dark:bg-dark-bg [&[data-p-checked=true]]:border-[#e95322] [&[data-p-checked=true]]:bg-[#e95322] [&[data-p-checked=true]]:dark:border-[#e95322] [&[data-p-checked=true]]:dark:bg-[#e95322]' },
    icon: { class: 'h-2.5 w-2.5 text-white transition-opacity' },
  },
  pcRowCheckbox: {
    root: { class: 'relative inline-flex h-4 w-4 shrink-0 cursor-pointer items-center justify-center align-bottom' },
    input: { class: 'absolute inset-0 z-10 m-0 h-full w-full cursor-pointer opacity-0' },
    box: { class: 'flex h-4 w-4 items-center justify-center rounded border border-gray-300 bg-white transition-colors dark:border-dark-border dark:bg-dark-bg [&[data-p-checked=true]]:border-[#e95322] [&[data-p-checked=true]]:bg-[#e95322] [&[data-p-checked=true]]:dark:border-[#e95322] [&[data-p-checked=true]]:dark:bg-[#e95322]' },
    icon: { class: 'h-2.5 w-2.5 text-white transition-opacity' },
  },
} as const

const resolveTypeTone = (type: number) => {
  if (type === 1) {
    return 'border-blue-100 bg-blue-50 text-blue-700 dark:border-blue-900/40 dark:bg-blue-900/20 dark:text-blue-200'
  }
  if (type === 2) {
    return 'border-rose-100 bg-rose-50 text-rose-700 dark:border-rose-900/40 dark:bg-rose-900/20 dark:text-rose-200'
  }
  return 'border-cyan-100 bg-cyan-50 text-cyan-700 dark:border-cyan-900/40 dark:bg-cyan-900/20 dark:text-cyan-200'
}

const resolveStatusTone = (status: number) => {
  return status === 1
    ? 'border-emerald-100 bg-emerald-50 text-emerald-700 dark:border-emerald-500/20 dark:bg-emerald-500/10 dark:text-emerald-200'
    : 'border-slate-200 bg-slate-50 text-slate-600 dark:border-slate-700 dark:bg-slate-800/70 dark:text-slate-200'
}
</script>

<template>
  <section class="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm dark:border-dark-border dark:bg-dark-card">
    <div class="border-b border-gray-100 px-4 py-3 dark:border-dark-border/70">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h3 class="text-base font-semibold text-gray-950 dark:text-white">公告列表</h3>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">桌面端优先使用高密度表格，移动端自动切换为卡片视图。</p>
        </div>
      </div>
    </div>

    <div v-if="loading" class="px-6 py-12 text-center">
      <i class="fas fa-spinner fa-spin text-lg text-gray-400 dark:text-gray-500"></i>
      <p class="mt-3 text-sm text-gray-500 dark:text-gray-400">正在整理公告数据...</p>
    </div>

    <div v-else-if="rows.length === 0" class="px-6 py-12 text-center">
      <i class="fas fa-bullhorn text-2xl text-gray-300 dark:text-gray-600"></i>
      <p class="mt-4 text-sm text-gray-500 dark:text-gray-400">当前筛选条件下暂无公告记录，可尝试调整筛选或新建公告。</p>
    </div>

    <template v-else>
      <!-- 桌面端 DataTable -->
      <div class="hidden overflow-x-auto md:block">
        <DataTable
          :value="rows"
          :selection="selectedRows"
          :loading="loading"
          dataKey="id"
          class="min-w-[980px]"
          :pt="{
            table: { class: 'w-full border-separate border-spacing-0' },
            thead: { class: 'bg-gray-50 dark:bg-dark-bg/60' },
            headerRow: { class: 'border-b border-gray-200 dark:border-dark-border' },
            bodyRow: {
              class: 'border-b border-gray-100 align-top transition-colors hover:bg-gray-50/80 dark:border-dark-border/60 dark:hover:bg-dark-bg/60',
            },
            emptyMessage: { class: 'px-4 py-10 text-center text-sm text-gray-500 dark:text-gray-400' },
            loadingOverlay: { class: 'bg-white/70 dark:bg-dark-card/70' },
          }"
          @update:selection="handleSelectionUpdate"
        >
          <Column
            selectionMode="multiple"
            :pt="selectionCheckboxPt"
            headerClass="px-3 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
            bodyClass="px-3 py-3"
          />

          <Column
            field="title"
            header="公告标题"
            headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
            bodyClass="px-4 py-3"
          >
            <template #body="{ data }">
              <button
                type="button"
                class="cursor-pointer text-left"
                @click="emit('view', data.id)"
              >
                <p class="line-clamp-2 text-sm font-semibold text-gray-950 transition-colors hover:text-[#e95322] dark:text-white dark:hover:text-[#ff7043]">
                  {{ data.title }}
                </p>
              </button>
            </template>
          </Column>

          <Column
            field="typeLabel"
            header="类型"
            headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
            bodyClass="px-4 py-3"
          >
            <template #body="{ data }">
              <span class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold" :class="resolveTypeTone(data.type)">
                {{ data.typeLabel }}
              </span>
            </template>
          </Column>

          <Column
            field="statusLabel"
            header="状态"
            headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
            bodyClass="px-4 py-3"
          >
            <template #body="{ data }">
              <span class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold" :class="resolveStatusTone(data.status)">
                {{ data.statusLabel }}
              </span>
            </template>
          </Column>

          <Column
            field="topLabel"
            header="置顶"
            headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
            bodyClass="px-4 py-3"
          >
            <template #body="{ data }">
              <span
                class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold"
                :class="
                  data.isTop === 1
                    ? 'border-amber-100 bg-amber-50 text-amber-700 dark:border-amber-500/20 dark:bg-amber-500/10 dark:text-amber-200'
                    : 'border-slate-200 bg-slate-50 text-slate-600 dark:border-slate-700 dark:bg-slate-800/70 dark:text-slate-200'
                "
              >
                {{ data.topLabel }}
              </span>
            </template>
          </Column>

          <Column
            field="publishTimeText"
            header="发布时间"
            headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
            bodyClass="px-4 py-3 text-sm text-gray-600 dark:text-gray-300"
          >
            <template #body="{ data }">
              {{ formatDate(data.publishTimeText) }}
            </template>
          </Column>

          <Column
            field="updateTime"
            header="更新时间"
            headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
            bodyClass="px-4 py-3 text-sm text-gray-500 dark:text-gray-400"
          >
            <template #body="{ data }">
              {{ formatDate(data.updateTime) }}
            </template>
          </Column>

          <Column
            header="操作"
            headerClass="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
            bodyClass="px-4 py-3 text-right whitespace-nowrap"
          >
            <template #body="{ data }">
              <div class="flex flex-wrap items-center justify-end gap-1.5">
                <button
                  type="button"
                  @click.stop="emit('view', data.id)"
                  class="cursor-pointer inline-flex h-8 items-center rounded bg-gray-50 px-2.5 text-xs font-medium text-gray-600 hover:bg-gray-100 dark:bg-gray-800/60 dark:text-gray-300 dark:hover:bg-gray-700"
                  title="查看"
                >
                  查看
                </button>
                <button
                  type="button"
                  @click.stop="emit('edit', data.id)"
                  class="cursor-pointer inline-flex h-8 items-center rounded bg-[#e95322]/10 px-2.5 text-xs font-medium text-[#e95322] hover:bg-[#e95322]/20 dark:bg-[#e95322]/20 dark:text-[#ff7043] dark:hover:bg-[#e95322]/30"
                  title="编辑"
                >
                  编辑
                </button>
                <button
                  type="button"
                  @click.stop="emit('toggle-status', data.id)"
                  class="cursor-pointer inline-flex h-8 items-center rounded bg-gray-50 px-2.5 text-xs font-medium text-gray-600 hover:bg-gray-100 dark:bg-gray-800/60 dark:text-gray-300 dark:hover:bg-gray-700"
                  :title="data.status === 1 ? '隐藏' : '显示'"
                >
                  {{ data.status === 1 ? '隐藏' : '显示' }}
                </button>
                <button
                  type="button"
                  @click.stop="emit('toggle-top', data.id)"
                  class="cursor-pointer inline-flex h-8 items-center rounded bg-amber-50 px-2.5 text-xs font-medium text-amber-700 hover:bg-amber-100 dark:bg-amber-900/20 dark:text-amber-300 dark:hover:bg-amber-900/40"
                  :title="data.isTop === 1 ? '取消置顶' : '置顶'"
                >
                  {{ data.isTop === 1 ? '取消置顶' : '置顶' }}
                </button>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>

      <!-- 移动端卡片视图 -->
      <div class="divide-y divide-gray-100 md:hidden dark:divide-dark-border/70">
        <article v-for="row in rows" :key="row.id" class="px-4 py-4">
          <div class="flex items-start gap-3">
            <label class="mt-0.5 inline-flex cursor-pointer items-center">
              <input
                type="checkbox"
                class="peer sr-only"
                :checked="selectedSet.has(row.id)"
                @change="handleToggleOne(row.id, $event)"
              />
              <span
                class="inline-flex h-4 w-4 items-center justify-center rounded border border-slate-300 bg-white text-white transition-colors peer-focus-visible:ring-2 peer-focus-visible:ring-[#e95322] peer-focus-visible:ring-offset-2 peer-checked:border-[#e95322] peer-checked:bg-[#e95322] dark:border-slate-500 dark:bg-slate-900 dark:peer-checked:border-[#e95322] dark:peer-checked:bg-[#e95322] dark:peer-focus-visible:ring-offset-slate-900"
              >
                <i class="fas fa-check text-[9px] opacity-0 transition-opacity peer-checked:opacity-100"></i>
              </span>
            </label>

            <div class="min-w-0 flex-1">
              <button type="button" class="w-full cursor-pointer text-left" @click="emit('view', row.id)">
                <div class="flex flex-wrap items-center gap-2">
                  <span class="inline-flex rounded-full border px-2 py-0.5 text-[11px] font-semibold" :class="resolveTypeTone(row.type)">
                    {{ row.typeLabel }}
                  </span>
                  <span class="inline-flex rounded-full border px-2 py-0.5 text-[11px] font-semibold" :class="resolveStatusTone(row.status)">
                    {{ row.statusLabel }}
                  </span>
                  <span
                    class="inline-flex rounded-full border px-2 py-0.5 text-[11px] font-semibold"
                    :class="
                      row.isTop === 1
                        ? 'border-amber-100 bg-amber-50 text-amber-700 dark:border-amber-500/20 dark:bg-amber-500/10 dark:text-amber-200'
                        : 'border-slate-200 bg-slate-50 text-slate-600 dark:border-slate-700 dark:bg-slate-800/70 dark:text-slate-200'
                    "
                  >
                    {{ row.topLabel }}
                  </span>
                </div>
                <h4 class="mt-3 text-sm font-semibold leading-6 text-gray-950 dark:text-white">{{ row.title }}</h4>
              </button>

              <div class="mt-3 grid grid-cols-1 gap-2 text-xs text-gray-500 dark:text-gray-400">
                <p>发布时间：{{ formatDate(row.publishTimeText) }}</p>
                <p>最近更新：{{ formatDate(row.updateTime) }}</p>
              </div>

              <div class="mt-4 flex flex-wrap gap-2">
                <button
                  type="button"
                  @click="emit('view', row.id)"
                  class="cursor-pointer inline-flex h-8 items-center rounded bg-gray-50 px-2.5 text-xs font-medium text-gray-600 hover:bg-gray-100 dark:bg-gray-800/60 dark:text-gray-300 dark:hover:bg-gray-700"
                >
                  查看
                </button>
                <button
                  type="button"
                  @click="emit('edit', row.id)"
                  class="cursor-pointer inline-flex h-8 items-center rounded bg-[#e95322]/10 px-2.5 text-xs font-medium text-[#e95322] hover:bg-[#e95322]/20 dark:bg-[#e95322]/20 dark:text-[#ff7043] dark:hover:bg-[#e95322]/30"
                >
                  编辑
                </button>
                <button
                  type="button"
                  @click="emit('toggle-status', row.id)"
                  class="cursor-pointer inline-flex h-8 items-center rounded bg-gray-50 px-2.5 text-xs font-medium text-gray-600 hover:bg-gray-100 dark:bg-gray-800/60 dark:text-gray-300 dark:hover:bg-gray-700"
                >
                  {{ row.status === 1 ? '隐藏' : '显示' }}
                </button>
              </div>
            </div>
          </div>
        </article>
      </div>
    </template>
  </section>
</template>
