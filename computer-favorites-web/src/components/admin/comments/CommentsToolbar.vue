<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import AdminSelect from '@/components/admin/common/AdminSelect.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import type { AdminCommentStatus } from '@/types/admin-comment'

const props = defineProps<{
  keyword: string
  status: AdminCommentStatus | null
  selectedCount: number
}>()

const emit = defineEmits<{
  (e: 'update:keyword', value: string): void
  (e: 'update:status', value: AdminCommentStatus | null): void
  (e: 'batch-delete'): void
  (e: 'batch-restore'): void
  (e: 'clear-selection'): void
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
  { label: '显示', value: 1 },
  { label: '隐藏', value: 0 },
])
</script>

<template>
  <section class="mb-6 overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card">
    <div class="border-b border-gray-100 px-4 py-4 dark:border-dark-border/70">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
        <div class="min-w-0">
          <div class="flex items-center gap-2">
            <h2 class="text-lg font-semibold tracking-tight text-gray-950 dark:text-white">评论审核池</h2>
            <span
              v-if="selectedCount > 0"
              class="inline-flex items-center rounded-full border border-orange-200 bg-orange-50 px-2.5 py-0.5 text-xs font-semibold text-orange-600 dark:border-orange-400/20 dark:bg-orange-500/10 dark:text-orange-200"
            >
              已选 {{ selectedCount }} 条
            </span>
            <span
              v-else
              class="inline-flex items-center rounded-full border border-slate-200 bg-slate-50 px-2.5 py-0.5 text-xs font-semibold text-slate-600 dark:border-white/10 dark:bg-white/5 dark:text-slate-200"
            >
              单条处理模式
            </span>
          </div>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
            先筛选出需关注的评论，支持批量删除与恢复操作。
          </p>
        </div>

        <div class="flex flex-col gap-3 lg:flex-row lg:items-center">
          <div class="w-full min-w-0 lg:w-[340px]">
            <label class="sr-only" for="comment-keyword">搜索评论</label>
            <div class="relative flex items-center"
            >
              <i
                class="fas fa-search pointer-events-none absolute left-3 top-1/2 z-10 -translate-y-1/2 text-xs text-gray-400 dark:text-gray-500"
              ></i>
              <input
                id="comment-keyword"
                v-model="localKeyword"
                type="text"
                class="h-10 w-full rounded-xl border border-gray-200 bg-white py-2 pl-9 pr-3 text-sm text-gray-900 placeholder-gray-400 shadow-[0_1px_2px_rgba(15,23,42,0.04)] outline-none transition-all duration-200 focus:border-orange-300 focus:ring-[3px] focus:ring-orange-500/15 dark:border-dark-border dark:bg-dark-card dark:text-gray-100 dark:placeholder-gray-500 dark:focus:border-orange-500/40 dark:focus:ring-orange-500/10"
                placeholder="搜索评论内容或用户"
                @input="onKeywordInput"
              />
            </div>
          </div>

          <AdminSelect
            :modelValue="status"
            :options="statusOptions"
            placeholder="全部状态"
            @update:modelValue="(value) => emit('update:status', value as AdminCommentStatus | null)"
          />
        </div>
      </div>
    </div>

    <div class="flex flex-col gap-3 px-4 py-4 lg:flex-row lg:items-center lg:justify-between">
      <div class="text-sm text-gray-500 dark:text-gray-400">
        默认按创建时间倒序排列，举报次数 &gt; 0 的评论优先展示。
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <button
          v-if="selectedCount > 0"
          type="button"
          class="inline-flex h-9 cursor-pointer items-center rounded-lg border border-red-200 bg-red-50 px-3 text-xs font-medium text-red-700 transition-colors hover:bg-red-100 dark:border-red-400/20 dark:bg-red-500/10 dark:text-red-200 dark:hover:bg-red-500/20"
          @click="emit('batch-delete')"
        >
          <i class="fas fa-trash-can mr-1.5 text-[11px]"></i>
          批量删除 ({{ selectedCount }})
        </button>

        <button
          v-if="selectedCount > 0"
          type="button"
          class="inline-flex h-9 cursor-pointer items-center rounded-lg border border-emerald-200 bg-emerald-50 px-3 text-xs font-medium text-emerald-700 transition-colors hover:bg-emerald-100 dark:border-emerald-400/20 dark:bg-emerald-500/10 dark:text-emerald-200 dark:hover:bg-emerald-500/20"
          @click="emit('batch-restore')"
        >
          <i class="fas fa-rotate-left mr-1.5 text-[11px]"></i>
          批量恢复 ({{ selectedCount }})
        </button>

        <button
          v-if="selectedCount > 0"
          type="button"
          class="inline-flex h-9 cursor-pointer items-center rounded-lg border border-gray-200 bg-gray-50 px-3 text-xs font-medium text-gray-600 transition-colors hover:bg-gray-100 dark:border-dark-border dark:bg-dark-bg dark:text-gray-300 dark:hover:bg-dark-card"
          @click="emit('clear-selection')"
        >
          <i class="fas fa-xmark mr-1.5 text-[11px]"></i>
          清空选择
        </button>

        <UButton color="neutral" variant="soft" @click="emit('refresh')">
          <i class="fas fa-rotate-right text-xs"></i>
          刷新数据
        </UButton>
      </div>
    </div>
  </section>
</template>
