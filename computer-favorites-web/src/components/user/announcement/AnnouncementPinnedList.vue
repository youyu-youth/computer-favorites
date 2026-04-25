<script setup lang="ts">
import { Pin } from 'lucide-vue-next'
import AnnouncementTimelineItem from '@/components/user/announcement/AnnouncementTimelineItem.vue'
import type { AnnouncementViewItem } from '@/types/user-announcement'

defineProps<{
  items: AnnouncementViewItem[]
}>()
</script>

<template>
  <section class="bg-white p-4 shadow-sm dark:bg-black sm:p-5">
    <div class="mb-5 flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <div
          class="inline-flex items-center gap-2 rounded-full border border-primary-500/35 bg-primary-500/10 px-3 py-1 text-xs font-semibold text-primary-700 dark:text-primary-300"
        >
          <Pin class="h-3.5 w-3.5" />
          重点公告
        </div>
        <h2 class="mt-3 text-xl font-bold text-gray-900 dark:text-white">置顶公告</h2>
        <p class="mt-2 text-sm leading-6 text-gray-600 dark:text-gray-400">
          这些公告会优先展示，同时也会保留在下方的完整时间线中，方便你按时间顺序继续阅读。
        </p>
      </div>
      <p class="text-sm text-gray-600 dark:text-gray-400">当前共 {{ items.length }} 条置顶内容</p>
    </div>

    <div class="grid grid-cols-1 gap-4 xl:grid-cols-2">
      <AnnouncementTimelineItem
        v-for="item in items"
        :key="item.id"
        :item="item"
        emphasis="pinned"
      />
    </div>
  </section>
</template>
