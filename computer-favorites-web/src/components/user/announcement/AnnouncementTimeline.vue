<script setup lang="ts">
import Timeline from 'primevue/timeline'
import AnnouncementTimelineItem from '@/components/user/announcement/AnnouncementTimelineItem.vue'
import type { AnnouncementViewItem } from '@/types/user-announcement'

defineProps<{
  items: AnnouncementViewItem[]
}>()
</script>

<template>
  <section class="bg-white p-4 shadow-sm dark:bg-black sm:p-6">
    <div class="mb-6 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.22em] text-primary-700 dark:text-primary-300">
          Release Timeline
        </p>
        <h2 class="mt-2 text-2xl font-bold text-gray-900 dark:text-white">更新日志时间线</h2>
      </div>
      <p class="text-sm text-gray-600 dark:text-gray-400">按发布时间倒序展示全部可见公告</p>
    </div>

    <Timeline
      :value="items"
      align="left"
      layout="vertical"
      class="w-full"
      :pt="{
        root: { class: 'w-full' },
        event: {
          class:
            'grid grid-cols-[40px_minmax(0,1fr)] gap-x-3 pb-6 last:pb-0 sm:grid-cols-[160px_40px_minmax(0,1fr)] sm:gap-x-6',
        },
        eventOpposite: {
          class: 'hidden sm:flex sm:flex-col sm:items-end sm:justify-start sm:pt-1 text-right',
        },
        eventSeparator: { class: 'relative flex flex-col items-center' },
        eventMarker: {
          class:
            'relative z-[1] h-4 w-4 rounded-full border-4 border-white bg-primary-500 shadow-[0_0_0_4px_rgba(245,158,11,0.14)] dark:border-black',
        },
        eventConnector: {
          class: 'mt-0 w-px flex-1 bg-primary-500/35',
        },
        eventContent: { class: 'min-w-0 pt-0' },
      }"
    >
      <template #opposite="{ item }">
        <div class="relative pr-2 pl-4">
          <span
            class="pointer-events-none absolute bottom-0 left-1.5 top-0 w-px bg-gray-200 dark:bg-gray-700"
            aria-hidden="true"
          />
          <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ item.displayDate }}</p>
          <p class="mt-1 text-xs uppercase tracking-[0.2em] text-gray-500">
            {{ item.displayTime }}
          </p>
        </div>
      </template>

      <template #marker>
        <span class="block h-full w-full rounded-full" />
      </template>

      <template #content="{ item }">
        <AnnouncementTimelineItem :item="item" />
      </template>
    </Timeline>
  </section>
</template>
