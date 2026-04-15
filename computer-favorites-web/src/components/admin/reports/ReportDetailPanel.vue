<script setup lang="ts">
import { computed } from 'vue'
import UBadge from '@/components/ui-adapter/UBadge.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import type { AdminReportDetail } from '@/types/report'
import { ReportStatus, getReportStatusMeta, getReportTypeMeta } from '@/types/report'

const props = defineProps<{
  open: boolean
  report: AdminReportDetail | null
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'preview-image', payload: { url: string; title: string }): void
  (e: 'pass', reportId: number): void
  (e: 'reject', reportId: number): void
}>()

const isPending = computed(() => props.report?.status === ReportStatus.PENDING)
const reportTypeMeta = computed(() => (props.report ? getReportTypeMeta(props.report.type) : null))
const reportStatusMeta = computed(() =>
  props.report ? getReportStatusMeta(props.report.status) : null,
)

const detailModalUi = {
  overlay: 'bg-black/40 backdrop-blur-[2px] z-[120]',
  content:
    'w-[min(96vw,1080px)] rounded-[24px] border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-[0_24px_60px_rgba(15,23,42,0.24)] dark:shadow-[0_28px_60px_rgba(2,6,23,0.6)] overflow-hidden',
  header:
    'border-b border-gray-200 dark:border-dark-border bg-[linear-gradient(180deg,rgba(248,250,252,0.92),rgba(255,255,255,0.96))] dark:bg-[linear-gradient(180deg,rgba(15,19,23,0.96),rgba(22,29,34,0.96))] px-6 py-5',
  title: 'text-lg font-semibold tracking-tight text-gray-950 dark:text-white',
  description: 'mt-1 text-sm text-gray-500 dark:text-gray-400',
  body: 'px-6 py-6',
  footer:
    'border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-6 py-4 flex flex-col-reverse gap-2 sm:flex-row sm:justify-between sm:items-center',
} as const
</script>

<template>
  <UModal
    :open="open"
    title="举报详情"
    description="查看完整举报上下文、证据截图与 mock 处置轨迹。"
    :ui="detailModalUi"
    @update:open="(value) => emit('update:open', value)"
  >
    <template #body>
      <div v-if="report" class="space-y-6">
        <section class="grid gap-4 lg:grid-cols-[1.2fr_0.8fr]">
          <article
            class="overflow-hidden rounded-2xl border border-amber-200/70 bg-[linear-gradient(135deg,rgba(255,251,235,0.92),rgba(255,255,255,0.98))] p-5 dark:border-amber-400/20 dark:bg-[linear-gradient(135deg,rgba(40,28,14,0.92),rgba(22,29,34,0.98))]"
          >
            <div class="flex flex-wrap items-center gap-2">
              <UBadge
                v-if="reportStatusMeta"
                :color="reportStatusMeta.color"
                :value="reportStatusMeta.label"
              />
              <span
                v-if="reportTypeMeta"
                class="inline-flex items-center gap-2 rounded-full bg-white/80 px-3 py-1 text-xs font-medium text-gray-700 shadow-sm dark:bg-white/10 dark:text-gray-200"
              >
                <i :class="[reportTypeMeta.icon, 'text-[11px]']"></i>
                {{ reportTypeMeta.label }}
              </span>
              <span class="text-xs text-gray-500 dark:text-gray-400">案件编号 #{{ report.id }}</span>
            </div>

            <h3 class="mt-4 text-xl font-semibold tracking-tight text-gray-950 dark:text-white">
              {{ report.targetName }}
            </h3>
            <p class="mt-2 text-sm leading-7 text-gray-700 dark:text-gray-300">
              {{ report.reason }}
            </p>

            <div class="mt-5 grid gap-3 sm:grid-cols-3">
              <div class="rounded-2xl bg-white/75 p-3 shadow-sm dark:bg-white/5">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">举报人</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">{{ report.userName }}</p>
                <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">{{ report.userEmail }}</p>
              </div>
              <div class="rounded-2xl bg-white/75 p-3 shadow-sm dark:bg-white/5">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">目标状态</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                  {{ report.targetStatusLabel }}
                </p>
                <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">
                  {{ report.targetUrl || '评论类目标无外链地址' }}
                </p>
              </div>
              <div class="rounded-2xl bg-white/75 p-3 shadow-sm dark:bg-white/5">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">证据摘要</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                  {{ report.images.length }} 张截图
                </p>
                <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">{{ report.evidenceSummary }}</p>
              </div>
            </div>
          </article>

          <article class="rounded-2xl border border-gray-200 bg-gray-50/80 p-5 dark:border-dark-border dark:bg-dark-bg">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">案件轨迹</p>
                <h4 class="mt-1 text-base font-semibold text-gray-900 dark:text-white">审核时间线</h4>
              </div>
              <div
                class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-white text-gray-600 shadow-sm dark:bg-white/10 dark:text-gray-200"
              >
                <i class="fas fa-wave-square"></i>
              </div>
            </div>

            <ol class="mt-4 space-y-4">
              <li
                v-for="item in report.timeline"
                :key="item.id"
                class="relative pl-6"
              >
                <span
                  class="absolute left-0 top-1.5 h-3 w-3 rounded-full ring-4"
                  :class="
                    item.tone === 'done'
                      ? 'bg-emerald-500 ring-emerald-100 dark:ring-emerald-500/20'
                      : 'bg-amber-500 ring-amber-100 dark:ring-amber-500/20'
                  "
                ></span>
                <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ item.title }}</p>
                <p class="mt-1 text-sm leading-6 text-gray-600 dark:text-gray-300">{{ item.description }}</p>
                <p class="mt-1 text-xs text-gray-400 dark:text-gray-500">{{ item.time }}</p>
              </li>
            </ol>
          </article>
        </section>

        <section class="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
          <article>
            <div class="mb-3 flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">Evidence</p>
                <h4 class="mt-1 text-base font-semibold text-gray-900 dark:text-white">截图证据</h4>
              </div>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ report.images.length }} 张可查看</p>
            </div>

            <div v-if="report.images.length > 0" class="grid gap-3 sm:grid-cols-2">
              <button
                v-for="(image, index) in report.images"
                :key="image"
                type="button"
                class="group overflow-hidden rounded-2xl border border-gray-200 bg-gray-100 text-left shadow-sm transition-transform hover:-translate-y-0.5 dark:border-dark-border dark:bg-dark-bg"
                @click="emit('preview-image', { url: image, title: `${report.targetName} - 证据 ${index + 1}` })"
              >
                <div class="relative aspect-[16/10] overflow-hidden">
                  <img
                    :src="image"
                    :alt="`${report.targetName} 证据 ${index + 1}`"
                    class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                  />
                  <div class="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/60 to-transparent px-3 py-2 text-xs font-medium text-white">
                    点击查看大图
                  </div>
                </div>
              </button>
            </div>

            <div
              v-else
              class="rounded-2xl border border-dashed border-gray-300 bg-gray-50 px-4 py-8 text-center text-sm text-gray-500 dark:border-dark-border dark:bg-dark-bg dark:text-gray-400"
            >
              本条举报未提供截图证据，请结合文本描述与目标对象内容人工判定。
            </div>
          </article>

          <article class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-dark-border dark:bg-dark-card">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">Review</p>
                <h4 class="mt-1 text-base font-semibold text-gray-900 dark:text-white">处置建议</h4>
              </div>
              <div
                class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-100 text-slate-700 dark:bg-white/10 dark:text-slate-100"
              >
                <i class="fas fa-gavel"></i>
              </div>
            </div>

            <div class="mt-4 space-y-4">
              <div class="rounded-2xl bg-gray-50 px-4 py-4 dark:bg-dark-bg">
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">目标对象</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-white">
                  {{ reportTypeMeta?.targetTypeLabel }} ID：{{ report.targetId }}
                </p>
                <p class="mt-1 text-sm text-gray-600 dark:text-gray-300">
                  {{ report.uploaderName ? `发布者：${report.uploaderName}` : '暂无发布者信息' }}
                </p>
              </div>

              <div class="rounded-2xl bg-gray-50 px-4 py-4 dark:bg-dark-bg">
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">处理记录</p>
                <p class="mt-2 text-sm text-gray-700 dark:text-gray-200">
                  {{ report.handleResult || '当前尚未处理，可在右下角直接发起 mock 处置。' }}
                </p>
                <p class="mt-2 text-xs text-gray-500 dark:text-gray-400">
                  {{ report.handleTime ? `${report.handlerName || '管理员'} 于 ${report.handleTime} 完成处置` : `创建时间：${report.createTime}` }}
                </p>
              </div>
            </div>
          </article>
        </section>
      </div>
    </template>

    <template #footer>
      <div class="text-xs text-gray-500 dark:text-gray-400">
        {{ report ? `最后更新时间：${report.updateTime}` : '' }}
      </div>

      <div class="flex flex-col-reverse gap-2 sm:flex-row">
        <UButton color="neutral" variant="soft" @click="emit('update:open', false)">
          关闭详情
        </UButton>
        <UButton
          color="red"
          variant="soft"
          :disabled="!report || !isPending"
          @click="report && emit('reject', report.id)"
        >
          <i class="fas fa-ban text-xs"></i>
          驳回举报
        </UButton>
        <UButton
          color="success"
          variant="soft"
          :disabled="!report || !isPending"
          @click="report && emit('pass', report.id)"
        >
          <i class="fas fa-check text-xs"></i>
          通过举报
        </UButton>
      </div>
    </template>
  </UModal>
</template>
