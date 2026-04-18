<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import UBadge from '@/components/ui-adapter/UBadge.vue'
import type { AdminFeedbackListItem } from '@/types/feedback'
import {
  FeedbackStatus,
  getFeedbackStatusMeta,
  getFeedbackTypeMeta,
} from '@/types/feedback'

defineProps<{
  rows: AdminFeedbackListItem[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'view', feedbackId: number): void
  (e: 'reply', feedbackId: number): void
  (e: 'close', feedbackId: number): void
}>()

const SUMMARY_PREVIEW_LENGTH = 15

const tooltipVisibleId = ref<number | null>(null)
const tooltipContent = ref('')
const tooltipStyle = ref<Record<string, string>>({})

let tooltipShowTimer: number | null = null
let tooltipHideTimer: number | null = null

const formatDateTime = (value: string | null) => {
  if (!value) {
    return '-'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}/${month}/${day}`
}

const shouldShowSummaryTooltip = (content: string) => content.length > SUMMARY_PREVIEW_LENGTH

const resolveContentExcerpt = (content: string) => {
  return shouldShowSummaryTooltip(content) ? `${content.slice(0, SUMMARY_PREVIEW_LENGTH)}…` : content
}

const clearTooltipTimers = () => {
  if (tooltipShowTimer) {
    window.clearTimeout(tooltipShowTimer)
    tooltipShowTimer = null
  }

  if (tooltipHideTimer) {
    window.clearTimeout(tooltipHideTimer)
    tooltipHideTimer = null
  }
}

const updateTooltipPosition = (event: MouseEvent | FocusEvent) => {
  const currentTarget = event.currentTarget
  if (!(currentTarget instanceof HTMLElement)) {
    return
  }

  const rect = currentTarget.getBoundingClientRect()
  tooltipStyle.value = {
    top: `${rect.bottom + 8}px`,
    left: `${Math.max(12, rect.left)}px`,
  }
}

const scheduleShowTooltip = (feedbackId: number, content: string, event: MouseEvent | FocusEvent) => {
  if (!shouldShowSummaryTooltip(content)) {
    return
  }

  clearTooltipTimers()
  updateTooltipPosition(event)

  tooltipShowTimer = window.setTimeout(() => {
    tooltipVisibleId.value = feedbackId
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

onBeforeUnmount(() => {
  clearTooltipTimers()
})
</script>

<template>
  <section class="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card">
    <div class="hidden overflow-x-auto lg:block">
      <DataTable
        :value="rows"
        :loading="loading"
        dataKey="id"
        class="min-w-[1180px]"
        :pt="{
          table: { class: 'w-full border-separate border-spacing-0' },
          thead: { class: 'bg-gray-50 dark:bg-dark-bg/70' },
          headerRow: { class: 'border-b border-gray-200 dark:border-dark-border' },
          bodyRow: {
            class: 'border-b border-gray-100 transition-colors hover:bg-blue-50/30 dark:border-dark-border/70 dark:hover:bg-dark-bg/30',
          },
          emptyMessage: { class: 'px-4 py-10 text-center text-sm text-gray-500 dark:text-gray-400' },
          loadingOverlay: { class: 'bg-white/70 dark:bg-dark-card/70' },
        }"
      >
        <template #empty>
          <div class="py-10 text-center">
            <div
              class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl border border-dashed border-blue-200 bg-blue-50 text-blue-500 dark:border-blue-400/20 dark:bg-blue-500/10 dark:text-blue-200"
            >
              <i class="fas fa-inbox text-xl"></i>
            </div>
            <p class="mt-4 text-sm font-medium text-gray-700 dark:text-gray-200">当前筛选下暂无反馈记录</p>
            <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">可以尝试清空筛选条件或重新拉取最新反馈数据。</p>
          </div>
        </template>

        <Column
          field="status"
          header="状态"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <UBadge
              :color="getFeedbackStatusMeta(data.status).color"
              :value="getFeedbackStatusMeta(data.status).label"
            />
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
              class="inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-full px-3 py-1 text-xs font-medium leading-none"
              :class="getFeedbackTypeMeta(data.type).toneClass"
            >
              <i :class="[getFeedbackTypeMeta(data.type).icon, 'text-[11px] leading-none']"></i>
              {{ getFeedbackTypeMeta(data.type).shortLabel }}
            </div>
          </template>
        </Column>

        <Column
          field="content"
          header="反馈摘要"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div class="max-w-[360px]">
              <span
                v-if="data.images.length > 0"
                class="inline-flex items-center gap-1 rounded-full bg-gray-100 px-2 py-0.5 text-[11px] text-gray-600 dark:bg-white/10 dark:text-gray-300"
              >
                <i class="fas fa-camera text-[10px]"></i>
                {{ data.images.length }}
              </span>
              <div
                class="relative mt-2 inline-flex max-w-full"
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

        <Column
          field="userName"
          header="提交用户"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div class="flex items-center gap-3">
              <img :src="data.avatar" :alt="data.userName" class="h-10 w-10 rounded-2xl border border-gray-200 object-cover dark:border-dark-border" />
              <div class="max-w-[190px] min-w-0">
                <p class="truncate text-sm font-medium text-gray-900 dark:text-gray-100">{{ data.userName }}</p>
                <p class="mt-1 truncate text-xs text-gray-500 dark:text-gray-400">
                  {{ data.userEmail || '游客未留邮箱' }}
                </p>
              </div>
            </div>
          </template>
        </Column>

        <Column
          field="contact"
          header="联系方式"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div class="max-w-[180px]">
              <p class="truncate text-sm text-gray-700 dark:text-gray-200">{{ data.contact || '未留下联系方式' }}</p>
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
                data.status !== FeedbackStatus.CLOSED
                  ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 dark:bg-emerald-500/10 dark:text-emerald-200 dark:hover:bg-emerald-500/20'
                  : 'cursor-not-allowed bg-gray-100 text-gray-400 dark:bg-white/5 dark:text-gray-500'
              "
              :disabled="data.status === FeedbackStatus.CLOSED"
              @click.stop="emit('reply', data.id)"
            >
              <i class="fas fa-reply mr-1 text-[11px]"></i>
              回复
            </button>

            <button
              type="button"
              class="inline-flex h-8 cursor-pointer items-center rounded-lg px-2.5 text-xs font-medium transition-colors"
              :class="
                data.status !== FeedbackStatus.CLOSED
                  ? 'bg-slate-100 text-slate-700 hover:bg-slate-200 dark:bg-white/10 dark:text-slate-100 dark:hover:bg-white/20'
                  : 'cursor-not-allowed bg-gray-100 text-gray-400 dark:bg-white/5 dark:text-gray-500'
              "
              :disabled="data.status === FeedbackStatus.CLOSED"
              @click.stop="emit('close', data.id)"
            >
              <i class="fas fa-box-archive mr-1 text-[11px]"></i>
              关闭
            </button>
          </template>
        </Column>
      </DataTable>
    </div>

    <div class="space-y-3 p-4 lg:hidden">
      <div
        v-if="rows.length === 0 && !loading"
        class="rounded-2xl border border-dashed border-gray-300 bg-gray-50 px-4 py-10 text-center text-sm text-gray-500 dark:border-dark-border dark:bg-dark-bg dark:text-gray-400"
      >
        当前筛选下暂无反馈记录
      </div>

      <article
        v-for="item in rows"
        :key="item.id"
        class="rounded-2xl border border-gray-200 bg-white p-4 shadow-[0_10px_28px_rgba(15,23,42,0.05)] dark:border-dark-border dark:bg-dark-card"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <div class="flex flex-wrap items-center gap-2">
              <UBadge :color="getFeedbackStatusMeta(item.status).color" :value="getFeedbackStatusMeta(item.status).label" />
              <span
                class="inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-medium"
                :class="getFeedbackTypeMeta(item.type).toneClass"
              >
                <i :class="[getFeedbackTypeMeta(item.type).icon, 'text-[11px]']"></i>
                {{ getFeedbackTypeMeta(item.type).shortLabel }}
              </span>
            </div>
            <p class="mt-3 text-sm font-semibold leading-6 text-gray-900 dark:text-gray-100">
              {{ resolveContentExcerpt(item.content) }}
            </p>
          </div>
        </div>

        <div class="mt-4 grid grid-cols-2 gap-3 rounded-2xl bg-gray-50 px-3 py-3 text-sm dark:bg-dark-bg">
          <div>
            <p class="text-xs uppercase tracking-[0.16em] text-gray-400 dark:text-gray-500">提交用户</p>
            <p class="mt-1 text-gray-800 dark:text-gray-100">{{ item.userName }}</p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.16em] text-gray-400 dark:text-gray-500">图片数量</p>
            <p class="mt-1 text-gray-800 dark:text-gray-100">{{ item.images.length }} 张</p>
          </div>
          <div class="col-span-2">
            <p class="text-xs uppercase tracking-[0.16em] text-gray-400 dark:text-gray-500">联系方式</p>
            <p class="mt-1 truncate text-gray-800 dark:text-gray-100">{{ item.contact || '未留下联系方式' }}</p>
          </div>
          <div class="col-span-2">
            <p class="text-xs uppercase tracking-[0.16em] text-gray-400 dark:text-gray-500">提交时间</p>
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
            type="button"
            class="inline-flex h-10 cursor-pointer items-center justify-center rounded-xl px-4 text-sm font-medium transition-colors"
            :class="
              item.status !== FeedbackStatus.CLOSED
                ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 dark:bg-emerald-500/10 dark:text-emerald-200 dark:hover:bg-emerald-500/20'
                : 'cursor-not-allowed bg-gray-100 text-gray-400 dark:bg-white/5 dark:text-gray-500'
            "
            :disabled="item.status === FeedbackStatus.CLOSED"
            @click="emit('reply', item.id)"
          >
            <i class="fas fa-reply mr-2 text-xs"></i>
            回复
          </button>
          <button
            type="button"
            class="inline-flex h-10 cursor-pointer items-center justify-center rounded-xl px-4 text-sm font-medium transition-colors"
            :class="
              item.status !== FeedbackStatus.CLOSED
                ? 'bg-slate-100 text-slate-700 hover:bg-slate-200 dark:bg-white/10 dark:text-slate-100 dark:hover:bg-white/20'
                : 'cursor-not-allowed bg-gray-100 text-gray-400 dark:bg-white/5 dark:text-gray-500'
            "
            :disabled="item.status === FeedbackStatus.CLOSED"
            @click="emit('close', item.id)"
          >
            <i class="fas fa-box-archive mr-2 text-xs"></i>
            关闭
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
