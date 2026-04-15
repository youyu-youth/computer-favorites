<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UInput from '@/components/ui-adapter/UInput.vue'
import USwitch from '@/components/ui-adapter/USwitch.vue'
import type { ReportStatus, ReportType } from '@/types/report'

const props = defineProps<{
  keyword: string
  status: ReportStatus | null
  type: ReportType | null
  pendingOnly: boolean
  selectedCount: number
  pendingSelectionOnly: boolean
}>()

const emit = defineEmits<{
  (e: 'update:keyword', value: string): void
  (e: 'update:status', value: ReportStatus | null): void
  (e: 'update:type', value: ReportType | null): void
  (e: 'update:pendingOnly', value: boolean): void
  (e: 'refresh'): void
  (e: 'batch-pass'): void
  (e: 'batch-reject'): void
  (e: 'clear-selection'): void
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
  { label: '全部状态', value: '' },
  { label: '待处理', value: '0' },
  { label: '已处理', value: '1' },
  { label: '已驳回', value: '2' },
])

const typeOptions = computed(() => [
  { label: '全部类型', value: '' },
  { label: '网站举报', value: '1' },
  { label: '评论举报', value: '2' },
])
</script>

<template>
  <section class="mb-6 overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card">
    <div
      class="border-b border-gray-100 bg-[linear-gradient(180deg,rgba(248,250,252,0.9),rgba(255,255,255,0.96))] px-4 py-4 dark:border-dark-border/70 dark:bg-[linear-gradient(180deg,rgba(15,19,23,0.92),rgba(22,29,34,0.96))]"
    >
      <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
        <div class="min-w-0">
          <div class="flex items-center gap-2">
            <h2 class="text-lg font-semibold tracking-tight text-gray-950 dark:text-white">举报队列</h2>
            <span
              v-if="selectedCount > 0"
              class="inline-flex items-center rounded-full border border-blue-200 bg-blue-50 px-2.5 py-0.5 text-xs font-semibold text-blue-700 dark:border-blue-400/20 dark:bg-blue-500/10 dark:text-blue-200"
            >
              已选 {{ selectedCount }} 项
            </span>
          </div>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
            支持按状态、类型和关键词快速收缩案件范围。
          </p>
        </div>

        <div class="flex flex-col gap-3 lg:flex-row lg:items-center">
          <div class="w-full min-w-0 lg:w-[320px]">
            <label class="sr-only" for="report-keyword">搜索举报</label>
            <div class="relative">
              <i
                class="fas fa-search pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-xs text-gray-400"
              ></i>
              <UInput
                id="report-keyword"
                v-model="localKeyword"
                class="pl-8"
                placeholder="搜索举报人、目标对象、原因"
                @update:modelValue="onKeywordInput"
              />
            </div>
          </div>

          <div class="grid grid-cols-1 gap-3 sm:grid-cols-3">
            <div class="relative">
              <select
                :value="status ?? ''"
                class="h-10 w-full cursor-pointer appearance-none rounded-lg border border-gray-200 bg-white pl-3 pr-9 text-sm text-gray-700 outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100"
                @change="
                  emit(
                    'update:status',
                    ($event.target as HTMLSelectElement).value === ''
                      ? null
                      : (Number(($event.target as HTMLSelectElement).value) as ReportStatus),
                  )
                "
              >
                <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
              <i class="fas fa-chevron-down pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-400"></i>
            </div>

            <div class="relative">
              <select
                :value="type ?? ''"
                class="h-10 w-full cursor-pointer appearance-none rounded-lg border border-gray-200 bg-white pl-3 pr-9 text-sm text-gray-700 outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100"
                @change="
                  emit(
                    'update:type',
                    ($event.target as HTMLSelectElement).value === ''
                      ? null
                      : (Number(($event.target as HTMLSelectElement).value) as ReportType),
                  )
                "
              >
                <option v-for="option in typeOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
              <i class="fas fa-chevron-down pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-400"></i>
            </div>

            <div
              class="flex items-center justify-between rounded-xl border border-gray-200 bg-gray-50/80 px-3 py-2 dark:border-dark-border dark:bg-dark-bg"
            >
              <div class="mr-3 min-w-0">
                <p class="text-xs font-semibold uppercase tracking-[0.18em] text-gray-400 dark:text-gray-500">
                  Pending
                </p>
                <p class="truncate text-sm font-medium text-gray-800 dark:text-gray-100">仅看待处理</p>
              </div>
              <USwitch
                :modelValue="pendingOnly"
                @update:modelValue="(value) => emit('update:pendingOnly', value)"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      class="flex flex-col gap-3 px-4 py-4 lg:flex-row lg:items-center lg:justify-between"
      :class="selectedCount > 0 ? 'bg-amber-50/70 dark:bg-amber-500/5' : ''"
    >
      <div v-if="selectedCount > 0" class="min-w-0">
        <p class="text-sm font-medium text-gray-900 dark:text-white">
          当前选中 {{ selectedCount }} 条举报
        </p>
        <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">
          {{ pendingSelectionOnly ? '可直接发起批量通过或驳回。' : '已包含非待处理记录，建议先清空后重新选择。' }}
        </p>
      </div>
      <div v-else class="text-sm text-gray-500 dark:text-gray-400">
        当前工作台默认展示完整案件池，可通过筛选快速收窄范围。
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <UButton color="neutral" variant="soft" @click="emit('refresh')">
          <i class="fas fa-rotate-right text-xs"></i>
          刷新数据
        </UButton>
        <UButton
          color="success"
          variant="soft"
          :disabled="selectedCount === 0 || !pendingSelectionOnly"
          @click="emit('batch-pass')"
        >
          <i class="fas fa-check text-xs"></i>
          批量通过
        </UButton>
        <UButton
          color="red"
          variant="soft"
          :disabled="selectedCount === 0 || !pendingSelectionOnly"
          @click="emit('batch-reject')"
        >
          <i class="fas fa-ban text-xs"></i>
          批量驳回
        </UButton>
        <UButton
          v-if="selectedCount > 0"
          color="neutral"
          variant="ghost"
          @click="emit('clear-selection')"
        >
          清空选择
        </UButton>
      </div>
    </div>
  </section>
</template>
