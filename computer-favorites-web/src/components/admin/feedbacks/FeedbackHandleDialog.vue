<script setup lang="ts">
import { computed } from 'vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import UTextarea from '@/components/ui-adapter/UTextarea.vue'
import type { AdminFeedbackHandleForm } from '@/types/feedback'

const props = defineProps<{
  open: boolean
  form: AdminFeedbackHandleForm
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'update:form', payload: Partial<AdminFeedbackHandleForm>): void
  (e: 'submit'): void
}>()

const isReplyAction = computed(() => props.form.action === 'reply')

const dialogTitle = computed(() => (isReplyAction.value ? '回复反馈' : '关闭反馈'))

const dialogDescription = computed(() =>
  isReplyAction.value
    ? '请填写清晰回复内容，提交后该工单状态将变更为已处理。'
    : '关闭后该工单不再继续跟进，适用于已无需回复或问题已结束的场景。',
)

const dialogUi = {
  overlay: 'bg-black/45 backdrop-blur-[1px] z-[120]',
  content:
    'w-[min(92vw,620px)] overflow-hidden rounded-[24px] border border-gray-200 bg-white shadow-[0_20px_48px_rgba(15,23,42,0.28)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_28px_60px_rgba(2,6,23,0.62)]',
  header: 'border-b border-gray-200 bg-gray-50 px-5 py-4 dark:border-dark-border dark:bg-dark-bg',
  title: 'text-lg font-semibold tracking-tight text-gray-950 dark:text-white',
  description: 'mt-1 text-sm text-gray-500 dark:text-gray-400',
  body: 'px-5 py-5',
  footer:
    'border-t border-gray-200 bg-gray-50 px-5 py-4 dark:border-dark-border dark:bg-dark-bg flex flex-col-reverse gap-2 sm:flex-row sm:justify-end',
} as const
</script>

<template>
  <UModal
    :open="open"
    :title="dialogTitle"
    :description="dialogDescription"
    :ui="dialogUi"
    @update:open="(value) => emit('update:open', value)"
  >
    <template #body>
      <div class="space-y-5">
        <section
          v-if="isReplyAction"
          class="rounded-2xl border border-emerald-200 bg-emerald-50/70 px-4 py-4 dark:border-emerald-400/20 dark:bg-emerald-500/10"
        >
          <div class="flex items-start gap-3">
            <div
              class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-white text-emerald-600 shadow-sm dark:bg-white/10 dark:text-emerald-200"
            >
              <i class="fas fa-reply"></i>
            </div>
            <div class="min-w-0">
              <p class="text-sm font-semibold text-emerald-800 dark:text-emerald-100">回复后自动归档为已处理</p>
              <p class="mt-1 text-sm leading-6 text-emerald-700 dark:text-emerald-200">
                建议直接说明已确认的结论、处理进度或后续跟进方式，避免给用户模糊回复。
              </p>
            </div>
          </div>
        </section>

        <section v-if="isReplyAction">
          <div class="mb-3 flex items-center justify-between gap-3">
            <label class="text-sm font-semibold text-gray-900 dark:text-white" for="feedback-handle-reply">
              回复内容
            </label>
            <span class="text-xs text-gray-400 dark:text-gray-500">
              {{ props.form.reply.trim().length }}/500
            </span>
          </div>
          <UTextarea
            id="feedback-handle-reply"
            :modelValue="props.form.reply"
            :rows="6"
            maxlength="500"
            placeholder="请输入至少 10 个字符，清晰说明问题判断、当前处理结果或后续计划。"
            @update:modelValue="(value) => emit('update:form', { reply: value })"
          />
        </section>

        <section
          v-else
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4 dark:border-dark-border dark:bg-dark-bg"
        >
          <div class="flex items-start gap-3">
            <div
              class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-white text-slate-600 shadow-sm dark:bg-white/10 dark:text-slate-200"
            >
              <i class="fas fa-box-archive"></i>
            </div>
            <div class="min-w-0">
              <p class="text-sm font-semibold text-gray-900 dark:text-white">关闭前请确认无需继续跟进</p>
              <p class="mt-1 text-sm leading-6 text-gray-600 dark:text-gray-300">
                当前表结构不记录单独的关闭备注，因此关闭动作只会更新工单状态，并保留已有回复内容。
              </p>
            </div>
          </div>
        </section>
      </div>
    </template>

    <template #footer>
      <UButton color="neutral" variant="soft" @click="emit('update:open', false)">取消</UButton>
      <UButton :color="isReplyAction ? 'success' : 'neutral'" @click="emit('submit')">
        <i :class="isReplyAction ? 'fas fa-paper-plane text-xs' : 'fas fa-box-archive text-xs'"></i>
        {{ isReplyAction ? '确认回复' : '确认关闭' }}
      </UButton>
    </template>
  </UModal>
</template>
