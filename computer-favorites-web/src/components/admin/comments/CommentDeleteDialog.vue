<script setup lang="ts">
import { computed } from 'vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import UTextarea from '@/components/ui-adapter/UTextarea.vue'
import type { AdminCommentHandleForm } from '@/types/admin-comment'
import type { AdminCommentAction } from '@/types/admin-comment'

const props = defineProps<{
  open: boolean
  form: AdminCommentHandleForm
  isBatch: boolean
  batchCount: number
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'update:form', payload: Partial<AdminCommentHandleForm>): void
  (e: 'submit'): void
}>()

const isHideAction = computed(() => props.form.action === 'hide')

const dialogTitle = computed(() => {
  if (props.isBatch) {
    return isHideAction.value ? `批量隐藏评论 (${props.batchCount} 条)` : `批量显示评论 (${props.batchCount} 条)`
  }
  return isHideAction.value ? '隐藏评论' : '显示评论'
})

const dialogDescription = computed(() =>
  isHideAction.value
    ? '隐藏后评论将对用户不可见，管理员仍可在已隐藏列表中查看和恢复显示。'
    : '显示后评论将重新对用户可见，请确认该评论内容符合社区规范。',
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
          v-if="isHideAction"
          class="rounded-2xl border border-red-200 bg-red-50/70 px-4 py-4 dark:border-red-400/20 dark:bg-red-500/10"
        >
          <div class="flex items-start gap-3">
            <div
              class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-white text-red-600 shadow-sm dark:bg-white/10 dark:text-red-200"
            >
              <i class="fas fa-triangle-exclamation"></i>
            </div>
            <div class="min-w-0">
              <p class="text-sm font-semibold text-red-800 dark:text-red-100">隐藏操作需谨慎</p>
              <p class="mt-1 text-sm leading-6 text-red-700 dark:text-red-200">
                {{ isBatch ? `即将隐藏 ${batchCount} 条评论，隐藏后不会通知用户。` : '隐藏后该评论将对用户不可见，用户端不再展示。' }}
              </p>
            </div>
          </div>
        </section>

        <section
          v-else
          class="rounded-2xl border border-emerald-200 bg-emerald-50/70 px-4 py-4 dark:border-emerald-400/20 dark:bg-emerald-500/10"
        >
          <div class="flex items-start gap-3">
            <div
              class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-white text-emerald-600 shadow-sm dark:bg-white/10 dark:text-emerald-200"
            >
              <i class="fas fa-eye"></i>
            </div>
            <div class="min-w-0">
              <p class="text-sm font-semibold text-emerald-800 dark:text-emerald-100">确认显示评论</p>
              <p class="mt-1 text-sm leading-6 text-emerald-700 dark:text-emerald-200">
                {{ isBatch ? `即将显示 ${batchCount} 条评论，显示后将重新对用户可见。` : '显示后该评论将重新出现在用户端，请确认内容合规。' }}
              </p>
            </div>
          </div>
        </section>

        <section>
          <div class="mb-3 flex items-center justify-between gap-3">
            <label class="text-sm font-semibold text-gray-900 dark:text-white" for="comment-handle-reason">
              操作备注
            </label>
            <span class="text-xs text-gray-400 dark:text-gray-500">
              {{ props.form.reason.trim().length }}/200
            </span>
          </div>
          <UTextarea
            id="comment-handle-reason"
            :modelValue="props.form.reason"
            :rows="4"
            maxlength="200"
            placeholder="可选填写操作原因，便于后续审计查阅。"
            @update:modelValue="(value) => emit('update:form', { reason: value })"
          />
        </section>
      </div>
    </template>

    <template #footer>
      <UButton color="neutral" variant="soft" @click="emit('update:open', false)">取消</UButton>
      <UButton :color="isHideAction ? 'error' : 'success'" @click="emit('submit')">
        <i :class="isHideAction ? 'fas fa-eye-slash text-xs' : 'fas fa-eye text-xs'"></i>
        {{ isHideAction ? '确认隐藏' : '确认显示' }}
      </UButton>
    </template>
  </UModal>
</template>
