<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import AdminSelect from '@/components/admin/common/AdminSelect.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import UDateTimePicker from '@/components/ui-adapter/UDateTimePicker.vue'
import UInput from '@/components/ui-adapter/UInput.vue'
import {
  AUDIT_MODULE_LABELS,
  AUDIT_ACTION_LABELS,
  AUDIT_USER_TYPE_LABELS,
} from '@/types/audit-log'

const props = defineProps<{
  module: string | number | null
  action: string | number | null
  userType: string | number | null
  result: string | number | null
  startTime: string | null
  endTime: string | null
  keyword: string
}>()

const emit = defineEmits<{
  (e: 'update:module', val: string | number | null): void
  (e: 'update:action', val: string | number | null): void
  (e: 'update:userType', val: string | number | null): void
  (e: 'update:result', val: string | number | null): void
  (e: 'update:startTime', val: string | null): void
  (e: 'update:endTime', val: string | null): void
  (e: 'update:keyword', val: string): void
  (e: 'refresh'): void
  (e: 'export'): void
  (e: 'reset'): void
}>()

const moduleOptions = Object.entries(AUDIT_MODULE_LABELS).map(([value, label]) => ({
  label,
  value,
}))

const actionOptions = Object.entries(AUDIT_ACTION_LABELS).map(([value, label]) => ({
  label,
  value,
}))

const userTypeOptions = Object.entries(AUDIT_USER_TYPE_LABELS).map(([value, label]) => ({
  label,
  value,
}))

const resultOptions = [
  { label: '成功', value: 1 },
  { label: '失败', value: 0 },
]

const localKeyword = ref(props.keyword)

watch(
  () => props.keyword,
  (value) => {
    localKeyword.value = value
  },
)

const parseDate = (str: string | null): Date | null => {
  if (!str) return null
  const d = new Date(str)
  return Number.isNaN(d.getTime()) ? null : d
}

const formatDate = (date: Date | null): string | null => {
  if (!date) return null
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const startDate = computed<Date | null>({
  get: () => parseDate(props.startTime),
  set: (val) => emit('update:startTime', formatDate(val)),
})

const endDate = computed<Date | null>({
  get: () => parseDate(props.endTime),
  set: (val) => emit('update:endTime', formatDate(val)),
})

let keywordTimer: number | null = null

const onKeywordInput = () => {
  if (keywordTimer) {
    window.clearTimeout(keywordTimer)
  }

  keywordTimer = window.setTimeout(() => {
    emit('update:keyword', localKeyword.value)
  }, 300)
}

</script>

<template>
  <section
    class="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card"
  >
    <div
      class="bg-gradient-to-r from-[rgb(var(--cf-color-primary-50-rgb)/1)] to-transparent dark:from-[rgb(var(--cf-color-primary-500-rgb)/0.08)] dark:to-transparent px-4 py-3 sm:px-5 sm:py-4"
    >
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div class="min-w-0">
          <h2
            class="text-lg font-semibold tracking-tight text-gray-900 dark:text-white"
          >
            审计日志筛选
          </h2>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
            按模块、动作、用户类型等条件筛选审计记录
          </p>
        </div>

        <div class="flex flex-shrink-0 items-center gap-2">
          <UButton
            icon="fas fa-rotate-left"
            variant="soft"
            @click="emit('reset')"
          >
            清除
          </UButton>
          <UButton
            icon="fas fa-arrows-rotate"
            variant="soft"
            @click="emit('refresh')"
          >
            刷新
          </UButton>
          <UButton
            color="neutral"
            icon="fas fa-file-export"
            variant="soft"
            @click="emit('export')"
          >
            导出
          </UButton>
        </div>
      </div>
    </div>

    <div class="p-4 sm:p-5">
      <div class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <div>
          <label class="mb-1 block text-xs font-medium text-gray-500 dark:text-gray-400">
            模块
          </label>
          <AdminSelect
            :modelValue="module"
            :options="moduleOptions"
            placeholder="全部模块"
            @update:modelValue="(val) => emit('update:module', val)"
          />
        </div>

        <div>
          <label class="mb-1 block text-xs font-medium text-gray-500 dark:text-gray-400">
            动作
          </label>
          <AdminSelect
            :modelValue="action"
            :options="actionOptions"
            placeholder="全部动作"
            @update:modelValue="(val) => emit('update:action', val)"
          />
        </div>

        <div>
          <label class="mb-1 block text-xs font-medium text-gray-500 dark:text-gray-400">
            用户类型
          </label>
          <AdminSelect
            :modelValue="userType"
            :options="userTypeOptions"
            placeholder="全部类型"
            @update:modelValue="(val) => emit('update:userType', val)"
          />
        </div>

        <div>
          <label class="mb-1 block text-xs font-medium text-gray-500 dark:text-gray-400">
            操作结果
          </label>
          <AdminSelect
            :modelValue="result"
            :options="resultOptions"
            placeholder="全部结果"
            @update:modelValue="(val) => emit('update:result', val)"
          />
        </div>

        <div>
          <label class="mb-1 block text-xs font-medium text-gray-500 dark:text-gray-400">
            开始时间
          </label>
          <UDateTimePicker v-model="startDate" placeholder="选择开始时间" />
        </div>

        <div>
          <label class="mb-1 block text-xs font-medium text-gray-500 dark:text-gray-400">
            结束时间
          </label>
          <UDateTimePicker v-model="endDate" placeholder="选择结束时间" />
        </div>

        <div class="sm:col-span-2">
          <label class="mb-1 block text-xs font-medium text-gray-500 dark:text-gray-400">
            搜索
          </label>
          <div class="relative">
            <i
              class="fas fa-search pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-xs text-gray-400"
            ></i>
            <UInput
              v-model="localKeyword"
              class="pl-8"
              placeholder="搜索ID/用户/IP..."
              @update:modelValue="onKeywordInput"
            />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
