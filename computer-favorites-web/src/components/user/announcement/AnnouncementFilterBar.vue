<script setup lang="ts">
import { computed } from 'vue'
import { Sparkles } from 'lucide-vue-next'
import type {
  AnnouncementFilterOption,
  AnnouncementFilterValue,
} from '@/types/user-announcement'

const activeType = defineModel<AnnouncementFilterValue>({ required: true })

const props = defineProps<{
  onlyTop: boolean
  resultCount: number
  options: AnnouncementFilterOption[]
}>()

const emit = defineEmits<{
  (e: 'update:onlyTop', value: boolean): void
}>()

const handleTopToggle = () => {
  emit('update:onlyTop', !props.onlyTop)
}

const resolveFilterButtonClass = (
  optionValue: AnnouncementFilterValue,
  selected: boolean,
): string => {
  if (!selected) {
    return 'border-gray-300 bg-white text-gray-600 hover:border-gray-400 hover:text-gray-900 dark:border-gray-800 dark:bg-black dark:text-gray-300 dark:hover:border-gray-600 dark:hover:text-white'
  }

  if (optionValue === 1) {
    return 'border-emerald-400 bg-emerald-500/12 text-emerald-700 shadow-[0_0_0_1px_rgba(16,185,129,0.12)] dark:bg-emerald-500/18 dark:text-emerald-200 dark:shadow-[0_0_0_1px_rgba(52,211,153,0.12)]'
  }

  if (optionValue === 2) {
    return 'border-rose-400 bg-rose-500/12 text-rose-700 shadow-[0_0_0_1px_rgba(244,63,94,0.12)] dark:bg-rose-500/18 dark:text-rose-200 dark:shadow-[0_0_0_1px_rgba(251,113,133,0.12)]'
  }

  if (optionValue === 3) {
    return 'border-primary-500 bg-primary-500/12 text-primary-700 shadow-[0_0_0_1px_rgba(245,158,11,0.12)] dark:bg-primary-500/18 dark:text-primary-100'
  }

  return 'border-cyan-400 bg-cyan-500/12 text-cyan-700 shadow-[0_0_0_1px_rgba(14,116,144,0.12)] dark:bg-cyan-500/18 dark:text-cyan-100 dark:shadow-[0_0_0_1px_rgba(34,211,238,0.12)]'
}

const toggleClasses = computed(() => {
  return props.onlyTop
    ? 'border-primary-500 bg-primary-50 text-primary-700 dark:bg-black dark:text-primary-200'
    : 'border-gray-300 bg-white text-gray-600 hover:border-gray-400 hover:text-gray-900 dark:border-gray-800 dark:bg-black dark:text-gray-300 dark:hover:border-gray-600 dark:hover:text-white'
})
</script>

<template>
  <section
    class="flex flex-col gap-4 bg-white p-4 shadow-sm dark:bg-black sm:p-5"
  >
    <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
      <div class="min-w-0">
        <div class="no-scrollbar flex items-center gap-2 overflow-x-auto pb-1">
          <button
            v-for="option in options"
            :key="option.value"
            type="button"
            class="inline-flex cursor-pointer items-center justify-center whitespace-nowrap border px-4 py-2 text-sm font-medium transition-colors"
            :class="resolveFilterButtonClass(option.value, activeType === option.value)"
            @click="activeType = option.value"
          >
            {{ option.label }}
          </button>
        </div>
      </div>

      <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
        <button
          type="button"
          class="inline-flex cursor-pointer items-center justify-center gap-2 border px-4 py-2 text-sm font-medium transition-colors"
          :class="toggleClasses"
          @click="handleTopToggle"
        >
          <span
            class="inline-flex h-5 w-9 items-center rounded-full p-0.5 transition-colors"
            :class="onlyTop ? 'bg-primary-500 dark:bg-primary-400' : 'bg-gray-300 dark:bg-gray-600'"
          >
            <span
              class="h-4 w-4 rounded-full bg-white transition-transform"
              :class="onlyTop ? 'translate-x-4' : 'translate-x-0'"
            />
          </span>
          仅看置顶
        </button>

        <div
          class="inline-flex items-center gap-2 rounded-full border border-primary-500/25 bg-primary-500/10 px-4 py-2 text-sm text-primary-700 dark:text-primary-200"
        >
          <Sparkles class="h-4 w-4" />
          当前展示 {{ resultCount }} 条公告
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}

.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
