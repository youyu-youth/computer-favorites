<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import type { AdminCommentListItem } from '@/types/admin-comment'
import { AdminCommentStatus, getCommentStatusMeta } from '@/types/admin-comment'

const props = defineProps<{
  rows: AdminCommentListItem[]
  loading: boolean
  selectedIds: number[]
}>()

const emit = defineEmits<{
  (e: 'view', commentId: number): void
  (e: 'delete', commentId: number): void
  (e: 'restore', commentId: number): void
  (e: 'selection-change', ids: number[]): void
}>()

const CONTENT_PREVIEW_LENGTH = 20

const tooltipVisibleId = ref<number | null>(null)
const tooltipContent = ref('')
const tooltipStyle = ref<Record<string, string>>({})

let tooltipShowTimer: number | null = null
let tooltipHideTimer: number | null = null

const headerClass = 'px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400'
const bodyClass = 'px-4 py-3'

const formatDateTime = (value: string | null) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}/${m}/${d}`
}

const shouldShowTooltip = (content: string) => content.length > CONTENT_PREVIEW_LENGTH

const resolveContentExcerpt = (content: string) =>
  shouldShowTooltip(content) ? `${content.slice(0, CONTENT_PREVIEW_LENGTH)}…` : content

const clearTooltipTimers = () => {
  if (tooltipShowTimer) { window.clearTimeout(tooltipShowTimer); tooltipShowTimer = null }
  if (tooltipHideTimer) { window.clearTimeout(tooltipHideTimer); tooltipHideTimer = null }
}

const updateTooltipPosition = (event: MouseEvent | FocusEvent) => {
  const el = event.currentTarget
  if (!(el instanceof HTMLElement)) return
  const rect = el.getBoundingClientRect()
  tooltipStyle.value = { top: `${rect.bottom + 8}px`, left: `${Math.max(12, rect.left)}px` }
}

const scheduleShowTooltip = (id: number, content: string, event: MouseEvent | FocusEvent) => {
  if (!shouldShowTooltip(content)) return
  clearTooltipTimers()
  updateTooltipPosition(event)
  tooltipShowTimer = window.setTimeout(() => {
    tooltipVisibleId.value = id
    tooltipContent.value = content
  }, 200)
}

const scheduleHideTooltip = () => {
  clearTooltipTimers()
  tooltipHideTimer = window.setTimeout(() => {
    tooltipVisibleId.value = null
    tooltipContent.value = ''
  }, 100)
}

const selectedRows = () =>
  props.rows.filter((item) => props.selectedIds.includes(item.id))

const handleSelectionUpdate = (value: AdminCommentListItem[] | AdminCommentListItem) => {
  const rows = Array.isArray(value) ? value : value ? [value] : []
  emit('selection-change', rows.map((item) => item.id))
}

onBeforeUnmount(() => { clearTooltipTimers() })
</script>

<template>
  <section class="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card">
    <!-- Desktop Table -->
    <div class="hidden overflow-x-auto lg:block">
      <DataTable
        :value="rows"
        :loading="loading"
        dataKey="id"
        class="min-w-[1200px]"
        :selection="selectedRows()"
        @update:selection="handleSelectionUpdate"
        :pt="{
          table: { class: 'w-full border-separate border-spacing-0' },
          thead: { class: 'bg-gray-50 dark:bg-dark-bg/70' },
          headerRow: { class: 'border-b border-gray-200 dark:border-dark-border' },
          bodyRow: {
            class: 'border-b border-gray-100 transition-colors hover:bg-orange-50/20 dark:border-dark-border/70 dark:hover:bg-dark-bg/30',
          },
          emptyMessage: { class: 'px-4 py-10 text-center text-sm text-gray-500 dark:text-gray-400' },
          loadingOverlay: { class: 'bg-white/70 dark:bg-dark-card/70' },
        }"
      >
        <template #empty>
          <div class="py-10 text-center">
            <div
              class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl border border-dashed border-orange-200 bg-orange-50 text-orange-500 dark:border-orange-400/20 dark:bg-orange-500/10 dark:text-orange-200"
            >
              <i class="fas fa-comments text-xl"></i>
            </div>
            <p class="mt-4 text-sm font-medium text-gray-700 dark:text-gray-200">当前筛选下暂无评论记录</p>
            <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">可以尝试清空筛选条件或重新拉取最新数据。</p>
          </div>
        </template>

        <Column
          selectionMode="multiple"
          :headerClass="headerClass"
          bodyClass="px-4 py-3"
          :pt="{
            headerCheckbox: {
              root: { class: 'flex items-center justify-center' },
              input: { class: 'h-4 w-4 rounded border-gray-300 text-orange-600 focus:ring-orange-500 dark:border-dark-border dark:bg-dark-card' },
            },
            rowCheckbox: {
              root: { class: 'flex items-center justify-center' },
              input: { class: 'h-4 w-4 rounded border-gray-300 text-orange-600 focus:ring-orange-500 dark:border-dark-border dark:bg-dark-card' },
            },
          }"
        />



        <Column field="content" header="评论内容" :headerClass="headerClass" :bodyClass="bodyClass">
          <template #body="{ data }">
            <div class="max-w-[320px]">
              <span
                v-if="data.reportCount > 0"
                class="inline-flex items-center gap-1 rounded-full bg-red-100 px-2 py-0.5 text-[11px] font-semibold text-red-600 dark:bg-red-500/10 dark:text-red-300"
              >
                <i class="fas fa-flag text-[9px]"></i>
                {{ data.reportCount }} 次举报
              </span>
              <div
                class="relative mt-1 inline-flex max-w-full"
                @mouseenter="scheduleShowTooltip(data.id, data.content, $event)"
                @mouseleave="scheduleHideTooltip()"
                @focusin="scheduleShowTooltip(data.id, data.content, $event)"
                @focusout="scheduleHideTooltip()"
              >
                <button
                  type="button"
                  class="cursor-help text-left text-sm font-medium leading-6 text-gray-900 outline-none dark:text-gray-100"
                >
                  {{ resolveContentExcerpt(data.content) }}
                </button>
              </div>
            </div>
          </template>
        </Column>

        <Column field="websiteName" header="所属网站" :headerClass="headerClass" :bodyClass="bodyClass">
          <template #body="{ data }">
            <div class="flex items-center gap-2 max-w-[180px]">
              <img
                :src="data.websiteIcon"
                :alt="data.websiteName"
                class="h-5 w-5 rounded object-cover"
                @error="($event.target as HTMLImageElement).style.display = 'none'"
              />
              <span class="truncate text-sm text-gray-700 dark:text-gray-200">{{ data.websiteName }}</span>
            </div>
          </template>
        </Column>

        <Column field="userName" header="评论用户" :headerClass="headerClass" :bodyClass="bodyClass">
          <template #body="{ data }">
            <div class="flex items-center gap-3">
              <img :src="data.userAvatar" :alt="data.userName" class="h-9 w-9 rounded-xl border border-gray-200 object-cover dark:border-dark-border" />
              <span class="truncate text-sm font-medium text-gray-900 dark:text-gray-100 max-w-[120px]">{{ data.userName }}</span>
            </div>
          </template>
        </Column>

        <Column field="likeCount" header="点赞" :headerClass="headerClass" :bodyClass="'text-center ' + bodyClass">
          <template #body="{ data }">
            <span class="inline-flex items-center gap-1 text-sm text-gray-600 dark:text-gray-300">
              <i class="fas fa-heart text-[11px] text-red-400 dark:text-red-300"></i>
              {{ data.likeCount }}
            </span>
          </template>
        </Column>

        <Column field="replyCount" header="回复" :headerClass="headerClass" :bodyClass="'text-center ' + bodyClass">
          <template #body="{ data }">
            <span class="inline-flex items-center gap-1 text-sm text-gray-600 dark:text-gray-300">
              <i class="fas fa-comment text-[11px] text-blue-400 dark:text-blue-300"></i>
              {{ data.replyCount }}
            </span>
          </template>
        </Column>

        <Column field="status" header="状态" :headerClass="headerClass" :bodyClass="bodyClass">
          <template #body="{ data }">
            <span
              class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold"
              :class="getCommentStatusMeta(data.status).toneClass"
            >
              <i :class="getCommentStatusMeta(data.status).icon" class="text-[10px]"></i>
              {{ getCommentStatusMeta(data.status).label }}
            </span>
          </template>
        </Column>

        <Column field="createTime" header="创建时间" :headerClass="headerClass" :bodyClass="bodyClass + ' text-sm text-gray-500 dark:text-gray-400'">
          <template #body="{ data }">
            {{ formatDateTime(data.createTime) }}
          </template>
        </Column>

        <Column header="操作" :headerClass="'text-right ' + headerClass" bodyClass="whitespace-nowrap px-4 py-3 text-right">
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
              v-if="data.status === AdminCommentStatus.VISIBLE"
              type="button"
              class="mr-1.5 inline-flex h-8 cursor-pointer items-center rounded-lg bg-red-50 px-2.5 text-xs font-medium text-red-700 transition-colors hover:bg-red-100 dark:bg-red-500/10 dark:text-red-200 dark:hover:bg-red-500/20"
              @click.stop="emit('delete', data.id)"
            >
              <i class="fas fa-eye-slash mr-1 text-[11px]"></i>
              隐藏
            </button>

            <button
              v-if="data.status === AdminCommentStatus.HIDDEN"
              type="button"
              class="inline-flex h-8 cursor-pointer items-center rounded-lg bg-emerald-50 px-2.5 text-xs font-medium text-emerald-700 transition-colors hover:bg-emerald-100 dark:bg-emerald-500/10 dark:text-emerald-200 dark:hover:bg-emerald-500/20"
              @click.stop="emit('restore', data.id)"
            >
              <i class="fas fa-eye mr-1 text-[11px]"></i>
              显示
            </button>
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- Mobile Cards -->
    <div class="space-y-3 p-4 lg:hidden">
      <div
        v-if="rows.length === 0 && !loading"
        class="rounded-2xl border border-dashed border-gray-300 bg-gray-50 px-4 py-10 text-center text-sm text-gray-500 dark:border-dark-border dark:bg-dark-bg dark:text-gray-400"
      >
        当前筛选下暂无评论记录
      </div>

      <article
        v-for="item in rows"
        :key="item.id"
        class="rounded-2xl border border-gray-200 bg-white p-4 shadow-[0_10px_28px_rgba(15,23,42,0.05)] dark:border-dark-border dark:bg-dark-card"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="flex flex-wrap items-center gap-2">
            <span
              class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold"
              :class="getCommentStatusMeta(item.status).toneClass"
            >
              <i :class="getCommentStatusMeta(item.status).icon" class="text-[10px]"></i>
              {{ getCommentStatusMeta(item.status).label }}
            </span>
            <span
              v-if="item.reportCount > 0"
              class="inline-flex items-center gap-1 rounded-full bg-red-100 px-2 py-0.5 text-[11px] font-semibold text-red-600 dark:bg-red-500/10 dark:text-red-300"
            >
              <i class="fas fa-flag text-[9px]"></i>
              {{ item.reportCount }} 举报
            </span>
          </div>
          <span class="text-xs font-mono text-gray-400 dark:text-gray-500">#{{ item.id }}</span>
        </div>

        <p class="mt-3 text-sm font-medium leading-6 text-gray-900 dark:text-gray-100">
          {{ resolveContentExcerpt(item.content) }}
        </p>

        <div class="mt-4 grid grid-cols-2 gap-3 rounded-2xl bg-gray-50 px-3 py-3 text-sm dark:bg-dark-bg">
          <div>
            <p class="text-xs uppercase tracking-[0.16em] text-gray-400 dark:text-gray-500">所属网站</p>
            <div class="mt-1 flex items-center gap-1.5">
              <img :src="item.websiteIcon" class="h-4 w-4 rounded" @error="($event.target as HTMLImageElement).style.display = 'none'" />
              <p class="truncate text-gray-800 dark:text-gray-100">{{ item.websiteName }}</p>
            </div>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.16em] text-gray-400 dark:text-gray-500">评论用户</p>
            <p class="mt-1 text-gray-800 dark:text-gray-100">{{ item.userName }}</p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.16em] text-gray-400 dark:text-gray-500">点赞</p>
            <p class="mt-1 text-gray-800 dark:text-gray-100">
              <i class="fas fa-heart text-[10px] text-red-400 mr-1"></i>{{ item.likeCount }}
            </p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.16em] text-gray-400 dark:text-gray-500">回复</p>
            <p class="mt-1 text-gray-800 dark:text-gray-100">
              <i class="fas fa-comment text-[10px] text-blue-400 mr-1"></i>{{ item.replyCount }}
            </p>
          </div>
          <div class="col-span-2">
            <p class="text-xs uppercase tracking-[0.16em] text-gray-400 dark:text-gray-500">创建时间</p>
            <p class="mt-1 text-gray-800 dark:text-gray-100">{{ formatDateTime(item.createTime) }}</p>
          </div>
        </div>

        <div class="mt-4 flex flex-col gap-2 sm:flex-row">
          <button
            type="button"
            class="inline-flex h-10 cursor-pointer items-center justify-center rounded-xl bg-slate-100 px-4 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-200 dark:bg-white/10 dark:text-slate-100 dark:hover:bg-white/20"
            @click="emit('view', item.id)"
          >
            <i class="fas fa-eye mr-2 text-xs"></i>
            查看详情
          </button>

          <button
            v-if="item.status === AdminCommentStatus.VISIBLE"
            type="button"
            class="inline-flex h-10 cursor-pointer items-center justify-center rounded-xl bg-red-50 px-4 text-sm font-medium text-red-700 transition-colors hover:bg-red-100 dark:bg-red-500/10 dark:text-red-200 dark:hover:bg-red-500/20"
            @click="emit('delete', item.id)"
          >
            <i class="fas fa-eye-slash mr-2 text-xs"></i>
            隐藏
          </button>

          <button
            v-if="item.status === AdminCommentStatus.HIDDEN"
            type="button"
            class="inline-flex h-10 cursor-pointer items-center justify-center rounded-xl bg-emerald-50 px-4 text-sm font-medium text-emerald-700 transition-colors hover:bg-emerald-100 dark:bg-emerald-500/10 dark:text-emerald-200 dark:hover:bg-emerald-500/20"
            @click="emit('restore', item.id)"
          >
            <i class="fas fa-eye mr-2 text-xs"></i>
            显示
          </button>
        </div>
      </article>
    </div>
  </section>

  <Teleport to="body">
    <div
      v-if="tooltipVisibleId !== null && tooltipContent"
      class="pointer-events-none fixed z-[160] rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm leading-6 text-gray-700 shadow-[0_14px_30px_rgba(15,23,42,0.18)] dark:border-dark-border dark:bg-dark-card dark:text-gray-100"
      :style="{
        ...tooltipStyle,
        width: 'max-content',
        maxWidth: 'min(32rem, calc(100vw - 2rem))',
        whiteSpace: 'normal',
        overflowWrap: 'anywhere',
      }"
    >
      {{ tooltipContent }}
    </div>
  </Teleport>
</template>
