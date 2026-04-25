<script setup lang="ts">
import { computed } from 'vue'
import { CornerDownRight, Pin } from 'lucide-vue-next'
import type { AnnouncementViewItem } from '@/types/user-announcement'

const props = withDefaults(
  defineProps<{
    item: AnnouncementViewItem
    emphasis?: 'default' | 'pinned'
  }>(),
  {
    emphasis: 'default',
  },
)

const articleClasses = computed(() => {
  if (props.emphasis === 'pinned') {
    return 'border-primary-500/55 bg-primary-50/35 dark:bg-black'
  }

  return 'border-gray-200 bg-white dark:border-gray-800 dark:bg-black'
})

const typeTagClasses = computed(() => {
  if (props.item.type === 1) {
    return 'border-emerald-500/35 bg-emerald-500/14 text-emerald-700 dark:text-emerald-200'
  }

  if (props.item.type === 2) {
    return 'border-rose-500/35 bg-rose-500/14 text-rose-700 dark:text-rose-200'
  }

  return 'border-primary-500/35 bg-primary-500/14 text-primary-700 dark:text-primary-100'
})
</script>

<template>
  <article
    class="relative overflow-hidden border p-4 shadow-[0_8px_20px_rgba(15,23,42,0.08)] transition-transform duration-200 hover:-translate-y-0.5 hover:shadow-[0_12px_26px_rgba(15,23,42,0.16)] dark:shadow-[0_12px_30px_rgba(0,0,0,0.28)] dark:hover:shadow-[0_18px_40px_rgba(0,0,0,0.45)] sm:p-5"
    :class="articleClasses"
  >
    <div class="mb-4 flex flex-wrap items-center gap-2">
      <span
        class="inline-flex items-center rounded-full border px-3 py-1 text-xs font-medium"
        :class="typeTagClasses"
      >
        {{ item.typeLabel }}
      </span>
      <span
        v-if="item.isTop === 1"
        class="inline-flex items-center gap-1 rounded-full border border-amber-500/35 bg-amber-500/14 px-3 py-1 text-xs font-medium text-amber-700 dark:text-amber-200"
      >
        <Pin class="h-3.5 w-3.5" />
        置顶公告
      </span>
      <span
        class="inline-flex items-center gap-1 rounded-full border border-gray-300 bg-gray-100 px-3 py-1 text-xs text-gray-600 dark:border-gray-700 dark:bg-black dark:text-gray-400 sm:hidden"
      >
        <CornerDownRight class="h-3.5 w-3.5 text-primary-400" />
        {{ item.displayDate }} {{ item.displayTime }}
      </span>
    </div>

    <div class="space-y-4">
      <div>
        <h3 class="text-xl font-bold leading-8 tracking-tight text-gray-900 dark:text-white">
          {{ item.title }}
        </h3>
        <p class="mt-2 hidden text-sm text-gray-500 dark:text-gray-500 sm:block">
          发布时间 {{ item.displayDate }} {{ item.displayTime }}
        </p>
      </div>

      <div class="whitespace-pre-line text-sm leading-7 text-gray-700 dark:text-gray-300 sm:text-[15px]">
        {{ item.content }}
      </div>
    </div>
  </article>
</template>
