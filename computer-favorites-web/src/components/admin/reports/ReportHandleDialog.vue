<script setup lang="ts">
import { computed } from 'vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import USwitch from '@/components/ui-adapter/USwitch.vue'
import UTextarea from '@/components/ui-adapter/UTextarea.vue'
import type { AdminReportHandleAction, AdminReportHandleForm } from '@/types/report'
import { REPORT_HANDLE_ACTION_OPTIONS } from '@/types/report'

const props = defineProps<{
  open: boolean
  mode: 'single' | 'batch'
  selectedCount: number
  form: AdminReportHandleForm
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'update:form', payload: Partial<AdminReportHandleForm>): void
  (e: 'submit'): void
}>()

const dialogTitle = computed(() =>
  props.mode === 'batch' ? '批量处置举报' : '处置举报',
)

const dialogDescription = computed(() =>
  props.mode === 'batch'
    ? `本次将对 ${props.selectedCount} 条待处理举报批量执行处置，请确认说明文案准确。`
    : '请填写处理说明，并决定是否执行联动治理动作。',
)

const actionCardClass = (value: AdminReportHandleAction) => {
  if (props.form.action !== value) {
    return 'border-gray-200 bg-white hover:border-amber-300 dark:border-dark-border dark:bg-dark-bg dark:hover:border-amber-400/30'
  }

  return value === 'pass'
    ? 'border-emerald-300 bg-emerald-50 dark:border-emerald-400/30 dark:bg-emerald-500/10'
    : 'border-red-300 bg-red-50 dark:border-red-400/30 dark:bg-red-500/10'
}

const dialogUi = {
  overlay: 'bg-black/45 backdrop-blur-[1px] z-[120]',
  content:
    'w-[min(92vw,640px)] rounded-[24px] border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-[0_20px_48px_rgba(15,23,42,0.28)] dark:shadow-[0_28px_60px_rgba(2,6,23,0.62)] overflow-hidden',
  header:
    'border-b border-gray-200 dark:border-dark-border bg-[linear-gradient(180deg,rgba(248,250,252,0.96),rgba(255,255,255,0.98))] dark:bg-[linear-gradient(180deg,rgba(15,19,23,0.96),rgba(22,29,34,0.98))] px-5 py-4',
  title: 'text-lg font-semibold tracking-tight text-gray-950 dark:text-white',
  description: 'mt-1 text-sm text-gray-500 dark:text-gray-400',
  body: 'px-5 py-5',
  footer:
    'px-5 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg flex flex-col-reverse gap-2 sm:flex-row sm:justify-end',
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
        <section>
          <p class="mb-3 text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">
            Action
          </p>
          <div class="grid gap-3 sm:grid-cols-2">
            <button
              v-for="option in REPORT_HANDLE_ACTION_OPTIONS"
              :key="option.value"
              type="button"
              class="cursor-pointer rounded-2xl border p-4 text-left transition-colors"
              :class="actionCardClass(option.value)"
              @click="emit('update:form', { action: option.value, executeAction: option.value === 'pass' })"
            >
              <div class="flex items-center justify-between gap-3">
                <h4 class="text-sm font-semibold text-gray-900 dark:text-white">{{ option.label }}</h4>
                <span
                  class="inline-flex h-8 w-8 items-center justify-center rounded-xl"
                  :class="
                    option.value === 'pass'
                      ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-200'
                      : 'bg-red-100 text-red-700 dark:bg-red-500/10 dark:text-red-200'
                  "
                >
                  <i :class="option.value === 'pass' ? 'fas fa-check' : 'fas fa-ban'"></i>
                </span>
              </div>
              <p class="mt-2 text-sm leading-6 text-gray-600 dark:text-gray-300">{{ option.description }}</p>
            </button>
          </div>
        </section>

        <section>
          <div class="mb-3 flex items-center justify-between gap-3">
            <label class="text-sm font-semibold text-gray-900 dark:text-white" for="report-handle-result">
              处理说明
            </label>
            <span class="text-xs text-gray-400 dark:text-gray-500">
              {{ props.form.handleResult.trim().length }}/500
            </span>
          </div>
          <UTextarea
            id="report-handle-result"
            :modelValue="props.form.handleResult"
            :rows="5"
            maxlength="500"
            placeholder="请输入至少 10 个字符，说明为什么通过或驳回该举报。"
            @update:modelValue="(value) => emit('update:form', { handleResult: value })"
          />
        </section>

        <section
          class="flex items-start justify-between gap-4 rounded-2xl border border-gray-200 bg-gray-50/90 px-4 py-4 dark:border-dark-border dark:bg-dark-bg"
        >
          <div class="min-w-0">
            <p class="text-sm font-semibold text-gray-900 dark:text-white">执行联动动作</p>
            <p class="mt-1 text-sm leading-6 text-gray-600 dark:text-gray-300">
              开启后会在通过举报时同步触发治理动作，用于真实执行网站下架或评论隐藏流程。
            </p>
          </div>
          <USwitch
            :modelValue="props.form.executeAction"
            :disabled="props.form.action !== 'pass'"
            @update:modelValue="(value) => emit('update:form', { executeAction: value })"
          />
        </section>
      </div>
    </template>

    <template #footer>
      <UButton color="neutral" variant="soft" @click="emit('update:open', false)">取消</UButton>
      <UButton
        :color="props.form.action === 'pass' ? 'success' : 'red'"
        @click="emit('submit')"
      >
        <i :class="props.form.action === 'pass' ? 'fas fa-check text-xs' : 'fas fa-ban text-xs'"></i>
        {{ props.form.action === 'pass' ? '确认通过' : '确认驳回' }}
      </UButton>
    </template>
  </UModal>
</template>
