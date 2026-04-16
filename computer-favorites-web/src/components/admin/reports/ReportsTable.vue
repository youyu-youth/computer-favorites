<script setup lang="ts">
import { computed } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import UBadge from '@/components/ui-adapter/UBadge.vue'
import type { AdminReportListItem } from '@/types/report'
import { ReportStatus, getReportStatusMeta, getReportTypeMeta } from '@/types/report'

const props = defineProps<{
  rows: AdminReportListItem[]
  selectedIds: number[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'selection-change', ids: number[]): void
  (e: 'view', reportId: number): void
  (e: 'pass', reportId: number): void
  (e: 'reject', reportId: number): void
}>()

const selectedRows = computed<AdminReportListItem[]>(() =>
  props.rows.filter((item) => props.selectedIds.includes(item.id)),
)

const handleSelectionUpdate = (value: AdminReportListItem[] | AdminReportListItem | null | undefined) => {
  const rows = Array.isArray(value) ? value : value ? [value] : []
  emit(
    'selection-change',
    rows.map((row) => row.id),
  )
}

const formatDateTime = (value: string | null) => {
  if (!value) {
    return '-'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const resolveReasonTitle = (reason: string) => {
  const [title] = reason.split(':')
  return title?.trim() || '举报原因'
}

const resolveReasonExcerpt = (reason: string) => {
  const [, ...rest] = reason.split(':')
  return (rest.join(':').trim() || reason).slice(0, 56)
}

const selectionCheckboxPt = {
  pcHeaderCheckbox: {
    root: { class: 'relative inline-flex h-4 w-4 shrink-0 cursor-pointer items-center justify-center align-bottom' },
    input: { class: 'absolute inset-0 z-10 m-0 h-full w-full cursor-pointer opacity-0' },
    box: {
      class:
        'flex h-4 w-4 items-center justify-center rounded border border-gray-300 bg-white transition-colors dark:border-dark-border dark:bg-dark-bg [&[data-p-checked=true]]:border-amber-500 [&[data-p-checked=true]]:bg-amber-500',
    },
    icon: { class: 'h-2.5 w-2.5 text-white transition-opacity' },
  },
  pcRowCheckbox: {
    root: { class: 'relative inline-flex h-4 w-4 shrink-0 cursor-pointer items-center justify-center align-bottom' },
    input: { class: 'absolute inset-0 z-10 m-0 h-full w-full cursor-pointer opacity-0' },
    box: {
      class:
        'flex h-4 w-4 items-center justify-center rounded border border-gray-300 bg-white transition-colors dark:border-dark-border dark:bg-dark-bg [&[data-p-checked=true]]:border-amber-500 [&[data-p-checked=true]]:bg-amber-500',
    },
    icon: { class: 'h-2.5 w-2.5 text-white transition-opacity' },
  },
} as const
</script>

<template>
  <section class="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card">
    <div class="overflow-x-auto">
      <DataTable
        :value="rows"
        :selection="selectedRows"
        :loading="loading"
        dataKey="id"
        class="min-w-[1160px]"
        :pt="{
          table: { class: 'w-full border-separate border-spacing-0' },
          thead: { class: 'bg-gray-50 dark:bg-dark-bg/70' },
          headerRow: { class: 'border-b border-gray-200 dark:border-dark-border' },
          bodyRow: {
            class: 'border-b border-gray-100 transition-colors hover:bg-amber-50/30 dark:border-dark-border/70 dark:hover:bg-dark-bg/30',
          },
          emptyMessage: { class: 'px-4 py-10 text-center text-sm text-gray-500 dark:text-gray-400' },
          loadingOverlay: { class: 'bg-white/70 dark:bg-dark-card/70' },
        }"
        @update:selection="handleSelectionUpdate"
      >
        <template #empty>
          <div class="py-10 text-center">
            <div
              class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl border border-dashed border-amber-200 bg-amber-50 text-amber-500 dark:border-amber-400/20 dark:bg-amber-500/10 dark:text-amber-200"
            >
              <i class="fas fa-flag text-xl"></i>
            </div>
            <p class="mt-4 text-sm font-medium text-gray-700 dark:text-gray-200">当前筛选下暂无举报记录</p>
            <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">可以尝试清空筛选条件或重新拉取最新举报数据。</p>
          </div>
        </template>

        <Column
          selectionMode="multiple"
          :pt="selectionCheckboxPt"
          headerClass="px-3 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-3 py-3"
        />

        <Column
          field="status"
          header="状态"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div>
              <UBadge
                :color="getReportStatusMeta(data.status).color"
                :value="getReportStatusMeta(data.status).label"
              />
            </div>
          </template>
        </Column>

        <Column
          field="type"
          header="类型"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div
              class="inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-full bg-slate-100 px-3 py-1 text-xs font-medium leading-none text-slate-700 dark:bg-white/10 dark:text-slate-200"
            >
              <i :class="[getReportTypeMeta(data.type).icon, 'text-[11px] leading-none']"></i>
              {{ getReportTypeMeta(data.type).shortLabel }}
            </div>
          </template>
        </Column>

        <Column
          field="reason"
          header="举报摘要"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div class="max-w-[320px]">
              <div class="flex items-center gap-2">
                <span
                  class="inline-flex items-center rounded-full border border-amber-200 bg-amber-50 px-2 py-0.5 text-[11px] font-semibold text-amber-700 dark:border-amber-400/20 dark:bg-amber-500/10 dark:text-amber-200"
                >
                  {{ resolveReasonTitle(data.reason) }}
                </span>
                <span
                  v-if="data.images.length > 0"
                  class="inline-flex items-center gap-1 rounded-full bg-gray-100 px-2 py-0.5 text-[11px] text-gray-600 dark:bg-white/10 dark:text-gray-300"
                >
                  <i class="fas fa-camera text-[10px]"></i>
                  {{ data.images.length }}
                </span>
              </div>
              <p class="mt-2 text-sm font-medium leading-6 text-gray-900 dark:text-gray-100">
                {{ resolveReasonExcerpt(data.reason) }}
              </p>
            </div>
          </template>
        </Column>

        <Column
          field="userName"
          header="举报人"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div class="max-w-[170px]">
              <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ data.userName }}</p>
              <p class="mt-1 truncate text-xs text-gray-500 dark:text-gray-400">{{ data.userEmail }}</p>
            </div>
          </template>
        </Column>

        <Column
          field="targetName"
          header="目标对象"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div class="max-w-[220px]">
              <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ data.targetName }}</p>
              <p class="mt-1 truncate text-xs text-gray-500 dark:text-gray-400">
                {{ data.uploaderName ? `发布者：${data.uploaderName}` : '目标作者信息待补充' }}
              </p>
            </div>
          </template>
        </Column>

        <Column
          field="createTime"
          header="提交时间"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3 text-sm text-gray-500 dark:text-gray-400"
        >
          <template #body="{ data }">
            {{ formatDateTime(data.createTime) }}
          </template>
        </Column>

        <Column
          field="handleTime"
          header="处理信息"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div v-if="data.status === ReportStatus.PENDING" class="text-sm text-gray-500 dark:text-gray-400">
              等待管理员处理
            </div>
            <div v-else class="max-w-[180px]">
              <p class="text-sm font-medium text-gray-900 dark:text-gray-100">
                {{ data.handlerName || '管理员' }}
              </p>
              <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">
                {{ formatDateTime(data.handleTime) }}
              </p>
            </div>
          </template>
        </Column>

        <Column
          header="操作"
          headerClass="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="whitespace-nowrap px-4 py-3 text-right"
        >
          <template #body="{ data }">
            <button
              type="button"
              class="mr-1.5 inline-flex h-8 cursor-pointer items-center rounded-lg bg-slate-100 px-2.5 text-xs font-medium text-slate-700 transition-colors hover:bg-slate-200 dark:bg-white/10 dark:text-slate-100 dark:hover:bg-white/20"
              @click.stop="emit('view', data.id)"
            >
              <i class="fas fa-eye mr-1 text-[11px]"></i>
              查看
            </button>

            <button
              type="button"
              class="mr-1.5 inline-flex h-8 cursor-pointer items-center rounded-lg px-2.5 text-xs font-medium transition-colors"
              :class="
                data.status === ReportStatus.PENDING
                  ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 dark:bg-emerald-500/10 dark:text-emerald-200 dark:hover:bg-emerald-500/20'
                  : 'bg-gray-100 text-gray-400 cursor-not-allowed dark:bg-white/5 dark:text-gray-500'
              "
              :disabled="data.status !== ReportStatus.PENDING"
              @click.stop="emit('pass', data.id)"
            >
              <i class="fas fa-check mr-1 text-[11px]"></i>
              通过
            </button>

            <button
              type="button"
              class="inline-flex h-8 cursor-pointer items-center rounded-lg px-2.5 text-xs font-medium transition-colors"
              :class="
                data.status === ReportStatus.PENDING
                  ? 'bg-red-50 text-red-700 hover:bg-red-100 dark:bg-red-500/10 dark:text-red-200 dark:hover:bg-red-500/20'
                  : 'bg-gray-100 text-gray-400 cursor-not-allowed dark:bg-white/5 dark:text-gray-500'
              "
              :disabled="data.status !== ReportStatus.PENDING"
              @click.stop="emit('reject', data.id)"
            >
              <i class="fas fa-ban mr-1 text-[11px]"></i>
              驳回
            </button>
          </template>
        </Column>
      </DataTable>
    </div>
  </section>
</template>
