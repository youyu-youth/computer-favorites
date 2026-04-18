<script setup lang="ts">
import { computed } from 'vue'
import UBadge from '@/components/ui-adapter/UBadge.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import type { AdminFeedbackDetail } from '@/types/feedback'
import { FeedbackStatus, getFeedbackStatusMeta, getFeedbackTypeMeta } from '@/types/feedback'

const props = defineProps<{
  open: boolean
  feedback: AdminFeedbackDetail | null
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'preview-image', payload: { url: string; title: string }): void
  (e: 'reply', feedbackId: number): void
  (e: 'close', feedbackId: number): void
}>()

const canHandle = computed(() => props.feedback?.status !== FeedbackStatus.CLOSED)

const detailModalUi = {
  overlay: 'bg-black/40 backdrop-blur-[2px] z-[120]',
  content:
    'flex max-h-[min(82vh,760px)] w-[min(94vw,860px)] flex-col overflow-hidden rounded-[20px] border border-gray-200 bg-white shadow-[0_18px_42px_rgba(15,23,42,0.24)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_24px_52px_rgba(2,6,23,0.58)] sm:rounded-[22px]',
  header:
    'border-b border-gray-200 bg-gray-50 px-4 py-4 dark:border-dark-border dark:bg-dark-bg sm:px-5 sm:py-4',
  title: 'text-lg font-semibold tracking-tight text-gray-950 dark:text-white',
  description: 'mt-1 text-sm text-gray-500 dark:text-gray-400',
  body: 'flex-1 overflow-y-auto px-4 py-4 sm:px-5 sm:py-5',
  footer:
    'border-t border-gray-200 bg-gray-50 px-4 py-3 dark:border-dark-border dark:bg-dark-bg sm:px-5 sm:py-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between',
} as const

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
</script>

<template>
  <UModal
    :open="open"
    title="反馈详情"
    description="查看完整反馈上下文、图片证据与当前工单流转状态。"
    :ui="detailModalUi"
    @update:open="(value) => emit('update:open', value)"
  >
    <template #body>
      <div v-if="feedback" class="space-y-4">
        <section class="grid gap-4 xl:grid-cols-[1.18fr_0.82fr]">
          <article class="overflow-hidden rounded-2xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card sm:p-5">
            <div class="flex flex-wrap items-center gap-2">
              <UBadge
                :color="getFeedbackStatusMeta(feedback.status).color"
                :value="getFeedbackStatusMeta(feedback.status).label"
              />
              <span
                class="inline-flex items-center gap-2 rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700 dark:bg-white/10 dark:text-slate-200"
              >
                <i :class="[getFeedbackTypeMeta(feedback.type).icon, 'text-[11px]']"></i>
                {{ getFeedbackTypeMeta(feedback.type).label }}
              </span>
              <span class="text-xs text-gray-500 dark:text-gray-400">工单编号 #{{ feedback.id }}</span>
            </div>

            <div class="mt-4 flex items-start gap-4">
              <img
                :src="feedback.avatar"
                :alt="feedback.userName"
                class="h-14 w-14 rounded-2xl border border-gray-200 object-cover dark:border-dark-border"
              />
              <div class="min-w-0">
                <h3 class="text-xl font-semibold tracking-tight text-gray-950 dark:text-white">
                  {{ feedback.userName }}
                </h3>
                <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  {{ feedback.userEmail || '游客用户，当前未绑定邮箱' }}
                </p>
                <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">
                  {{ feedback.contact || '未留下联系方式' }}
                </p>
              </div>
            </div>

            <div class="mt-5 rounded-2xl border border-blue-100 bg-blue-50/70 px-4 py-4 dark:border-blue-400/20 dark:bg-blue-500/10">
              <p class="text-xs font-semibold uppercase tracking-[0.18em] text-blue-600 dark:text-blue-200">Summary</p>
              <p class="mt-2 text-sm leading-6 text-blue-700 dark:text-blue-100">
                {{ feedback.summaryText }}
              </p>
            </div>

            <div class="mt-5">
              <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">反馈正文</p>
              <p class="mt-3 text-sm leading-7 text-gray-700 dark:text-gray-300">
                {{ feedback.content }}
              </p>
            </div>

            <div class="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              <div class="rounded-2xl bg-gray-50 p-3 dark:bg-dark-bg">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">提交时间</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                  {{ formatDateTime(feedback.createTime) }}
                </p>
              </div>
              <div class="rounded-2xl bg-gray-50 p-3 dark:bg-dark-bg">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">回复时间</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                  {{ formatDateTime(feedback.replyTime) }}
                </p>
              </div>
              <div class="rounded-2xl bg-gray-50 p-3 dark:bg-dark-bg sm:col-span-2 lg:col-span-1">
                <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">最后更新</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                  {{ formatDateTime(feedback.updateTime) }}
                </p>
              </div>
            </div>
          </article>

          <article class="rounded-2xl border border-gray-200 bg-gray-50/80 p-4 dark:border-dark-border dark:bg-dark-bg sm:p-5">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">Flow</p>
                <h4 class="mt-1 text-base font-semibold text-gray-900 dark:text-white">工单轨迹</h4>
              </div>
              <div
                class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-white text-gray-600 shadow-sm dark:bg-white/10 dark:text-gray-200"
              >
                <i class="fas fa-list-check"></i>
              </div>
            </div>

            <ol class="mt-4 space-y-3">
              <li
                v-for="item in feedback.timeline"
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
                <p class="mt-1 text-sm leading-5 text-gray-600 dark:text-gray-300">{{ item.description }}</p>
                <p class="mt-1 text-xs text-gray-400 dark:text-gray-500">{{ item.time }}</p>
              </li>
            </ol>
          </article>
        </section>

        <section class="grid gap-4 xl:grid-cols-[1.05fr_0.95fr]">
          <article>
            <div class="mb-3 flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">Evidence</p>
                <h4 class="mt-1 text-base font-semibold text-gray-900 dark:text-white">附件截图</h4>
              </div>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ feedback.images.length }} 张可查看</p>
            </div>

            <div v-if="feedback.images.length > 0" class="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <button
                v-for="(image, index) in feedback.images"
                :key="image"
                type="button"
                class="group cursor-pointer overflow-hidden rounded-2xl border border-gray-200 bg-gray-100 text-left shadow-sm transition-transform hover:-translate-y-0.5 dark:border-dark-border dark:bg-dark-bg"
                @click="emit('preview-image', { url: image, title: `${feedback.userName} - 附件 ${index + 1}` })"
              >
                <div class="relative aspect-[16/10] overflow-hidden">
                  <img
                    :src="image"
                    :alt="`${feedback.userName} 附件 ${index + 1}`"
                    class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                  />
                  <div class="absolute inset-x-0 bottom-0 bg-black/55 px-3 py-2 text-xs font-medium text-white">
                    点击查看大图
                  </div>
                </div>
              </button>
            </div>

            <div
              v-else
              class="rounded-2xl border border-dashed border-gray-300 bg-gray-50 px-4 py-8 text-center text-sm text-gray-500 dark:border-dark-border dark:bg-dark-bg dark:text-gray-400"
            >
              当前反馈未提供图片附件，请结合正文与联系方式进行判断。
            </div>
          </article>

          <article class="rounded-2xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card sm:p-5">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">Reply</p>
                <h4 class="mt-1 text-base font-semibold text-gray-900 dark:text-white">处理结果</h4>
              </div>
              <div
                class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-100 text-slate-700 dark:bg-white/10 dark:text-slate-100"
              >
                <i class="fas fa-comment-dots"></i>
              </div>
            </div>

            <div class="mt-4 space-y-4">
              <div class="rounded-2xl bg-gray-50 px-4 py-4 dark:bg-dark-bg">
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">当前状态</p>
                <p class="mt-2 text-sm font-medium text-gray-900 dark:text-white">
                  {{ getFeedbackStatusMeta(feedback.status).label }}
                </p>
                <p class="mt-1 text-sm text-gray-600 dark:text-gray-300">
                  {{ getFeedbackTypeMeta(feedback.type).helper }}
                </p>
              </div>

              <div class="rounded-2xl bg-gray-50 px-4 py-4 dark:bg-dark-bg">
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">回复内容</p>
                <p class="mt-2 text-sm leading-6 text-gray-700 dark:text-gray-200">
                  {{ feedback.reply || '当前尚未回复，可直接从下方操作区进入回复流程。' }}
                </p>
              </div>
            </div>
          </article>
        </section>
      </div>

      <div v-else class="py-12 text-center text-sm text-gray-500 dark:text-gray-400">
        正在加载反馈详情...
      </div>
    </template>

    <template #footer>
      <div class="order-2 text-xs text-gray-500 dark:text-gray-400 sm:order-1">
        {{ feedback ? `最后更新时间：${formatDateTime(feedback.updateTime)}` : '' }}
      </div>

      <div class="order-1 flex w-full flex-col-reverse gap-2 sm:order-2 sm:w-auto sm:flex-row">
        <UButton color="neutral" variant="soft" class="w-full sm:w-auto" @click="emit('update:open', false)">
          关闭详情
        </UButton>
        <UButton
          color="neutral"
          variant="outline"
          class="w-full sm:w-auto"
          :disabled="!feedback || !canHandle"
          @click="feedback && emit('close', feedback.id)"
        >
          <i class="fas fa-box-archive text-xs"></i>
          关闭工单
        </UButton>
        <UButton
          color="success"
          variant="soft"
          class="w-full sm:w-auto"
          :disabled="!feedback || !canHandle"
          @click="feedback && emit('reply', feedback.id)"
        >
          <i class="fas fa-reply text-xs"></i>
          回复反馈
        </UButton>
      </div>
    </template>
  </UModal>
</template>
