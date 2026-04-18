<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import AdminSelect from '@/components/admin/common/AdminSelect.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UInput from '@/components/ui-adapter/UInput.vue'
import USwitch from '@/components/ui-adapter/USwitch.vue'
import type { FeedbackStatus, FeedbackType } from '@/types/feedback'

const props = defineProps<{
  keyword: string
  status: FeedbackStatus | null
  type: FeedbackType | null
  hasImages: boolean
  hasContact: boolean
}>()

const emit = defineEmits<{
  (e: 'update:keyword', value: string): void
  (e: 'update:status', value: FeedbackStatus | null): void
  (e: 'update:type', value: FeedbackType | null): void
  (e: 'update:hasImages', value: boolean): void
  (e: 'update:hasContact', value: boolean): void
  (e: 'refresh'): void
}>()

const localKeyword = ref(props.keyword)

watch(
  () => props.keyword,
  (value) => {
    localKeyword.value = value
  },
)

let keywordTimer: number | null = null

const onKeywordInput = () => {
  if (keywordTimer) {
    window.clearTimeout(keywordTimer)
  }

  keywordTimer = window.setTimeout(() => {
    emit('update:keyword', localKeyword.value)
  }, 240)
}

const statusOptions = computed(() => [
  { label: '全部状态', value: null },
  { label: '待处理', value: 0 },
  { label: '已处理', value: 1 },
  { label: '已关闭', value: 2 },
])

const typeOptions = computed(() => [
  { label: '全部类型', value: null },
  { label: '建议', value: 1 },
  { label: 'Bug 反馈', value: 2 },
  { label: '投诉', value: 3 },
  { label: '使用感受', value: 4 },
])
</script>

<template>
  <section class="mb-6 overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card">
    <div class="border-b border-gray-100 px-4 py-4 dark:border-dark-border/70">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
        <div class="min-w-0">
          <div class="flex items-center gap-2">
            <h2 class="text-lg font-semibold tracking-tight text-gray-950 dark:text-white">反馈工单池</h2>
            <span
              class="inline-flex items-center rounded-full border border-slate-200 bg-slate-50 px-2.5 py-0.5 text-xs font-semibold text-slate-600 dark:border-white/10 dark:bg-white/5 dark:text-slate-200"
            >
              单条处理模式
            </span>
          </div>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
            先筛选出高优先级工单，再进入详情查看上下文并完成回复或关闭。
          </p>
        </div>

        <div class="flex flex-col gap-3 lg:flex-row lg:items-center">
          <div class="w-full min-w-0 lg:w-[320px]">
            <label class="sr-only" for="feedback-keyword">搜索反馈</label>
            <div class="relative">
              <i
                class="fas fa-search pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-xs text-gray-400"
              ></i>
              <UInput
                id="feedback-keyword"
                v-model="localKeyword"
                class="pl-8"
                placeholder="搜索工单号、用户、联系方式或反馈内容"
                @update:modelValue="onKeywordInput"
              />
            </div>
          </div>

          <div class="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <AdminSelect
              :modelValue="status"
              :options="statusOptions"
              placeholder="全部状态"
              @update:modelValue="(value) => emit('update:status', value as FeedbackStatus | null)"
            />

            <AdminSelect
              :modelValue="type"
              :options="typeOptions"
              placeholder="全部类型"
              @update:modelValue="(value) => emit('update:type', value as FeedbackType | null)"
            />

            <div
              class="flex items-center justify-between rounded-xl border border-gray-200 bg-gray-50/80 px-3 py-2 dark:border-dark-border dark:bg-dark-bg"
            >
              <div class="mr-3 min-w-0">
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">
                  Images
                </p>
                <p class="truncate text-sm font-medium text-gray-800 dark:text-gray-100">仅看带图</p>
              </div>
              <USwitch
                :modelValue="hasImages"
                @update:modelValue="(value) => emit('update:hasImages', value)"
              />
            </div>

            <div
              class="flex items-center justify-between rounded-xl border border-gray-200 bg-gray-50/80 px-3 py-2 dark:border-dark-border dark:bg-dark-bg"
            >
              <div class="mr-3 min-w-0">
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">
                  Contact
                </p>
                <p class="truncate text-sm font-medium text-gray-800 dark:text-gray-100">仅看可回访</p>
              </div>
              <USwitch
                :modelValue="hasContact"
                @update:modelValue="(value) => emit('update:hasContact', value)"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="flex flex-col gap-3 px-4 py-4 lg:flex-row lg:items-center lg:justify-between">
      <div class="text-sm text-gray-500 dark:text-gray-400">
        默认按待处理优先展示，同状态下按创建时间倒序排列。
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <UButton color="neutral" variant="soft" @click="emit('refresh')">
          <i class="fas fa-rotate-right text-xs"></i>
          刷新数据
        </UButton>
      </div>
    </div>
  </section>
</template>
