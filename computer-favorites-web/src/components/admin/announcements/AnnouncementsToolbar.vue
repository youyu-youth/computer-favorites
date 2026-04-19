<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import AdminSelect from '@/components/admin/common/AdminSelect.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UInput from '@/components/ui-adapter/UInput.vue'
import type { AdminAnnouncementStatus, AdminAnnouncementType } from '@/types/admin-announcement'
import {
  ADMIN_ANNOUNCEMENT_STATUS_OPTIONS,
  ADMIN_ANNOUNCEMENT_TOP_OPTIONS,
  ADMIN_ANNOUNCEMENT_TYPE_OPTIONS,
} from '@/types/admin-announcement'

const props = defineProps<{
  keyword: string
  status: AdminAnnouncementStatus | null
  type: AdminAnnouncementType | null
  isTop: boolean | null
  selectedCount: number
}>()

const emit = defineEmits<{
  (e: 'update:keyword', value: string): void
  (e: 'update:status', value: AdminAnnouncementStatus | null): void
  (e: 'update:type', value: AdminAnnouncementType | null): void
  (e: 'update:isTop', value: boolean | null): void
  (e: 'refresh'): void
  (e: 'create'): void
  (e: 'batch-show'): void
  (e: 'batch-hide'): void
  (e: 'batch-cancel-top'): void
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
  }, 220)
}

const topOptions = computed(() => ADMIN_ANNOUNCEMENT_TOP_OPTIONS)
const statusOptions = computed(() => ADMIN_ANNOUNCEMENT_STATUS_OPTIONS)
const typeOptions = computed(() => ADMIN_ANNOUNCEMENT_TYPE_OPTIONS)
const hasSelection = computed(() => props.selectedCount > 0)
</script>

<template>
  <section class="mb-6 overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm dark:border-dark-border dark:bg-dark-card">
    <div class="border-b border-gray-100 px-4 py-4 dark:border-dark-border/70">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
        <div class="min-w-0">
          <div class="flex flex-wrap items-center gap-2">
            <h2 class="text-lg font-semibold tracking-tight text-gray-950 dark:text-white">公告工作台</h2>
            <span
              class="inline-flex items-center rounded-full border border-slate-200 bg-slate-50 px-2.5 py-0.5 text-xs font-semibold text-slate-600 dark:border-white/10 dark:bg-white/5 dark:text-slate-200"
            >
              双栏运营台
            </span>
          </div>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
            优先筛选目标公告，再在右侧预览区确认内容和状态后完成操作。
          </p>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <UButton color="neutral" variant="soft" @click="emit('refresh')">
            <i class="fas fa-rotate-right text-xs"></i>
            刷新数据
          </UButton>
          <UButton color="primary" @click="emit('create')">
            <i class="fas fa-plus text-xs"></i>
            新建公告
          </UButton>
        </div>
      </div>
    </div>

    <div class="px-4 py-4">
      <div class="grid grid-cols-1 gap-3 xl:grid-cols-[minmax(0,1.2fr)_repeat(3,minmax(0,0.65fr))]">
        <div class="min-w-0">
          <label class="sr-only" for="announcement-keyword">搜索公告</label>
          <div class="relative">
            <i class="fas fa-magnifying-glass pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-xs text-gray-400"></i>
            <UInput
              id="announcement-keyword"
              v-model="localKeyword"
              class="pl-8"
              placeholder="搜索公告标题、内容摘要或公告编号"
              @update:modelValue="onKeywordInput"
            />
          </div>
        </div>

        <AdminSelect
          :modelValue="status"
          :options="statusOptions"
          placeholder="全部状态"
          @update:modelValue="(value) => emit('update:status', value as AdminAnnouncementStatus | null)"
        />

        <AdminSelect
          :modelValue="type"
          :options="typeOptions"
          placeholder="全部类型"
          @update:modelValue="(value) => emit('update:type', value as AdminAnnouncementType | null)"
        />

        <AdminSelect
          :modelValue="isTop === null ? null : Number(isTop)"
          :options="topOptions"
          placeholder="全部置顶状态"
          @update:modelValue="
            (value) =>
              emit(
                'update:isTop',
                value === null ? null : value === 1,
              )
          "
        />
      </div>
    </div>

    <div class="border-t border-gray-100 px-4 py-4 dark:border-dark-border/70">
      <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <div class="text-sm text-gray-500 dark:text-gray-400">
          <span v-if="hasSelection">当前已选 {{ selectedCount }} 条公告，可执行批量操作。</span>
          <span v-else>默认按置顶优先展示，同优先级下按发布时间倒序排列。</span>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <UButton color="neutral" variant="soft" :disabled="!hasSelection" @click="emit('batch-show')">
            <i class="fas fa-eye text-xs"></i>
            批量显示
          </UButton>
          <UButton color="neutral" variant="soft" :disabled="!hasSelection" @click="emit('batch-hide')">
            <i class="fas fa-eye-slash text-xs"></i>
            批量隐藏
          </UButton>
          <UButton color="warning" variant="soft" :disabled="!hasSelection" @click="emit('batch-cancel-top')">
            <i class="fas fa-thumbtack text-xs"></i>
            批量取消置顶
          </UButton>
        </div>
      </div>
    </div>
  </section>
</template>
